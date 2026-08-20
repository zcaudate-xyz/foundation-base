(ns code.migrate.engine
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [code.migrate.catalog :as catalog]
            [code.query :as query]
            [std.block :as block]
            [std.block.layout.estimate :as estimate]
            [std.block.navigate :as nav])
  (:import (java.security MessageDigest)))

(defn sha256
  "returns the lowercase SHA-256 digest for a migration input"
  {:added "4.1"}
  [value]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256")
                        (.getBytes (str value) "UTF-8"))]
    (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))

(defn dependency-routes
  "compiles safe namespace replacement rules from a migration catalog"
  {:added "4.1"}
  ([migration-catalog]
   (dependency-routes migration-catalog nil))
  ([migration-catalog target]
   (cond->
    (merge
     (->> (:migration/targets migration-catalog)
          (mapcat (fn [entry]
                    (keep (fn [[source target]]
                            (when (and source target)
                              [source
                               {:target target
                                :rule/id :code-migrate/catalog-target}]))
                          [[(:target/source-namespace entry)
                            (:target/target-source-namespace entry)]
                           [(:target/test-namespace entry)
                            (:target/target-test-namespace entry)]])))
          (into {}))
     (->> (:migration/rules migration-catalog)
          (filter #(and (not= :draft (:rule/status %))
                        (= :dependency (:rule/kind %))
                        (#{:replace-namespace :replace-symbol}
                         (get-in % [:rule/rewrite :op]))))
          (map (fn [rule]
                 [(:rule/match rule)
                  {:target (or (get-in rule [:rule/rewrite :namespace])
                               (get-in rule [:rule/rewrite :symbol]))
                   :rule/id (:rule/id rule)}]))
          (into {})))
     (and (= :test (:unit/kind target))
          (:target/source-namespace target)
          (:target/target-source-namespace target))
     (assoc (:target/source-namespace target)
            {:target (:target/target-source-namespace target)
             :rule/id :clojure/test-source-namespace}))))

(defn rewrite-ns-form
  "rewrites one Clojure namespace form using target-owned overrides"
  {:added "4.1"}
  [form overrides output-namespace]
  (let [[head name & clauses] form
        name (or output-namespace name)
        clauses (remove #(and (seq? %)
                              (#{:refer-clojure :config} (first %)))
                        clauses)]
    (apply list head name
           (cond-> (vec clauses)
             (seq overrides)
             (conj (list :config {:override (vec overrides)}))))))

(defn rewrite-block-interface
  "folds Foundation's JVM block interfaces into native block protocols"
  {:added "4.1"}
  [form]
  (case (second form)
    IBlock
    '(defprotocol IBlock
       (block-type [block])
       (block-tag [block])
       (block-string [block])
       (block-length [block])
       (block-width [block])
       (block-height [block])
       (block-prefixed [block])
       (block-suffixed [block])
       (block-verify [block])
       (block-info [block]))

    IBlockModifier
    '(defprotocol IBlockModifier
       (block-modify [block accumulator input]))

    IBlockExpression
    '(defprotocol IBlockExpression
       (block-value [block])
       (block-value-string [block]))

    IBlockContainer
    '(defprotocol IBlockContainer
       (block-children [block])
       (replace-children [block children]))

    nil))

(defn remove-block-interface-remnants
  "removes folded JVM interfaces and their namespace reset residue"
  {:added "4.1"}
  [root]
  (loop [current root]
    (if-let [location
             (first
              (filter
               (fn [location]
                 (let [form (nav/value location)]
                   (and (seq? form)
                        (#{'definterface 'comment} (first form)))))
               (query/select current [seq?])))]
      (recur (-> location
                 nav/delete
                 nav/root-string
                 nav/parse-root))
      current)))

(defn namespace-aliases-used
  "returns aliases referenced by qualified symbols in a migrated block tree"
  {:added "4.1"}
  [root]
  (->> (query/select root [symbol?])
       (map nav/value)
       (keep namespace)
       (map symbol)
       set))

(defn prune-unused-requires-form
  "removes aliased require entries unused by the migrated namespace body"
  {:added "4.1"}
  [form used]
  (let [[head name & clauses] form
        clauses (keep
                 (fn [clause]
                   (if (and (seq? clause)
                            (= :require (first clause)))
                     (let [entries
                           (filter
                            (fn [entry]
                              (let [options (when (vector? entry)
                                              (apply hash-map (rest entry)))
                                    alias   (:as options)]
                                (or (nil? alias)
                                    (contains? used alias))))
                            (rest clause))]
                       (when (seq entries)
                         (cons :require entries)))
                     clause))
                 clauses)]
    (apply list head name clauses)))

(defn rewrite-unused-requires
  "prunes unused aliased requires when explicitly enabled by a target"
  {:added "4.1"}
  [root target applied]
  (if (contains? (set (:target/rules target))
                 :clojure/prune-unused-requires)
    (let [used (namespace-aliases-used root)]
      (query/modify
       root
       [seq?]
       (fn [location]
         (let [form (nav/value location)]
           (if (= 'ns (first form))
             (let [rewritten (prune-unused-requires-form form used)]
               (when (not= form rewritten)
                 (swap! applied conj :clojure/prune-unused-requires))
               (nav/replace location rewritten))
             location)))))
    root))

(defn add-require-alias
  "adds a target alias to an existing namespace dependency"
  {:added "4.1"}
  [form source-namespace alias]
  (let [[head name & clauses] form
        clauses (map (fn [clause]
                       (if (and (seq? clause)
                                (= :require (first clause)))
                         (cons :require
                               (map (fn [entry]
                                      (if (and (vector? entry)
                                               (= source-namespace (first entry))
                                               (not (some #{:as} entry)))
                                        (into entry [:as alias])
                                        entry))
                                    (rest clause)))
                         clause))
                     clauses)]
    (apply list head name clauses)))

(defn rewrite-test-override-symbols
  "qualifies test calls hidden by native Foundation overrides"
  {:added "4.1"}
  [root target applied]
  (if (and (= :test (:unit/kind target))
           (contains? (set (:target/rules target))
                      :foundation/qualified-overrides))
    (let [overrides (set (:target/overrides target))
          alias     (:target/test-alias target)]
      (query/modify
       root
       [symbol?]
       (fn [location]
         (let [value (nav/value location)
               inside-ns?
               (loop [current (:parent location)]
                 (if current
                   (let [form (nav/value current)]
                     (if (and (seq? form) (= 'ns (first form)))
                       true
                       (recur (:parent current))))
                   false))]
           (if (and (contains? overrides value)
                    (not inside-ns?))
             (do (swap! applied conj :foundation/qualified-overrides)
                 (nav/replace location
                              (symbol (name alias) (name value))))
             location)))))
    root))

(defn rewrite-single-arity-defn
  "normalizes one wrapped Clojure defn arity to native defn syntax"
  {:added "4.1"}
  [form]
  (apply list (concat (butlast form) (last form))))

(defn parsed-form-block
  "constructs a replacement block through the parser so map keys retain order"
  {:added "4.1"}
  [form]
  (block/parse-first (pr-str form)))

(declare quoted-location?)

(defn rewrite-single-arity-defns
  "normalizes only defn forms containing exactly one wrapped arity"
  {:added "4.1"}
  [root target applied]
  (if (contains? (set (:target/rules target))
                 :clojure/single-arity-defn)
    (query/modify
     root
     [seq?]
     (fn [location]
       (let [form    (nav/value location)
             arities (filter #(and (seq? %)
                                    (vector? (first %)))
                             (drop 2 form))]
         (if (and (not (quoted-location? location))
                  (= 'defn (first form))
                  (= 1 (count arities))
                  (= (last form) (first arities)))
           (do (swap! applied conj :clojure/single-arity-defn)
               (nav/replace location
                            (parsed-form-block
                             (rewrite-single-arity-defn form))))
           location))))
    root))

(defn rewrite-loop-parameters
  "replaces rest-destructured loop parameters with native first/next bindings"
  {:added "4.1"}
  [bindings]
  (reduce-kv
   (fn [output index [binding _]]
     (if (and (vector? binding) (= '& (second binding)))
       (let [parameter (symbol (str "migration-loop-value-" index))]
         (-> output
             (update :parameters conj parameter)
             (update :destructure into
                     [(first binding) (list 'first parameter)
                      (nth binding 2) (list 'rest parameter)])))
       (update output :parameters conj binding)))
   {:parameters [] :destructure []}
   (vec bindings)))

(defn rewrite-optional-rest-parameter
  "lowers a nested variadic vector pattern into ordinary native bindings"
  {:added "4.1"}
  [form]
  (let [parameter-index (first
                         (keep-indexed (fn [index value]
                                         (when (vector? value) index))
                                       form))
        parameters      (when parameter-index (nth form parameter-index))
        amp-index       (when parameters
                          (.indexOf ^java.util.List parameters '&))
        pattern         (when (and amp-index (not (neg? amp-index)))
                          (nth parameters (inc amp-index) nil))]
    (if (vector? pattern)
      (let [temporary  'migration-optional-arguments
            parameters (vec (concat (take amp-index parameters)
                                    ['& temporary]))
            bindings   (vec
                        (mapcat (fn [index name]
                                  [name (list 'first
                                              (list 'drop index temporary))])
                                (range)
                                pattern))]
        (apply list
               (concat (take parameter-index form)
                       [parameters
                        (apply list 'let bindings
                               (drop (inc parameter-index) form))])))
      form)))

(defn rewrite-print-meta-binding
  "removes Clojure's printer switch because native pr-str retains metadata"
  {:added "4.1"}
  [form]
  (let [body (nnext form)]
    (if (and (= 1 (count body))
             (seq? (first body))
             (= 'pr-str (first (first body)))
             (= 2 (count (first body))))
      (list 'migration-meta-tree (second (first body)))
      (if (= 1 (count body))
        (first body)
        (cons 'do body)))))

(defn rewrite-layout-optional-opts
  "materializes layout-main's omitted options as a native empty map"
  {:added "4.1"}
  [form]
  (let [body     (last form)
        bindings (second body)
        bindings (assoc bindings 1 (list 'or (second bindings) {}))
        body     (apply list 'let bindings (nnext body))]
    (apply list (concat (butlast form) [body]))))

(defn rewrite-layout-map-pairs
  "materializes native map entries before two-column sequence operations"
  {:added "4.1"}
  [form]
  (let [normalized '(if (map? pairs)
                      (if (Algo/ordered-map? pairs)
                        (seq pairs)
                        (sort (seq pairs)))
                      pairs)]
    (walk/postwalk
     (fn [node]
       (if (= '(if col-sort (sort-by first pairs) pairs) node)
         (list 'if 'col-sort
               (list 'sort-by 'first normalized)
               normalized)
         node))
     form)))

(defn rewrite-layout-readable-width
  "accounts for Clojure's printed commas when estimating native map widths"
  {:added "4.1"}
  [form]
  (apply list
         (concat
          (butlast form)
          ['(letfn [(separator-width [value]
                      (cond
                        (map? value)
                        (+ (max 0 (dec (count value)))
                           (reduce + 0
                                   (map separator-width
                                        (mapcat identity (seq value)))))

                        (or (vector? value) (set? value) (seq? value))
                        (reduce + 0 (map separator-width value))

                        :else 0))]
              (+ (count (pr-str form))
                 (separator-width form)))])))

(defn rewrite-layout-hiccup-boolean
  "normalizes Foundation's truthy layout result to its documented boolean API"
  {:added "4.1"}
  [form]
  (apply list
         (concat (butlast form)
                 [(list 'boolean (last form))])))

(defn rewrite-function-recur
  "rewrites function and loop recur into deterministic named calls"
  {:added "4.1"}
  ([form function-name]
   (rewrite-function-recur form function-name (atom 0)))
  ([form recur-target counter]
   (cond
     (seq? form)
     (let [head (first form)]
       (with-meta
         (cond
           (= 'recur head)
           (apply list recur-target
                  (map #(rewrite-function-recur % recur-target counter)
                       (rest form)))

           (and (= 'catch head) (= 3 (count form)))
           (apply list
                  (concat (map #(rewrite-function-recur % recur-target counter)
                               form)
                          [nil]))

           (= 'loop head)
           (let [bindings  (partition 2 (second form))
                 loop-name (symbol (str (name recur-target)
                                        "--loop-"
                                        (swap! counter inc)))
                 {:keys [parameters destructure]}
                 (rewrite-loop-parameters bindings)
                 initial    (map #(rewrite-function-recur (second %)
                                                          recur-target
                                                          counter)
                                 bindings)
                 body       (map #(rewrite-function-recur % loop-name counter)
                                 (nnext form))
                 body       (if (seq destructure)
                              [(apply list 'let destructure body)]
                              body)
                 definition (apply list loop-name parameters body)]
             (list 'letfn [definition]
                   (list 'let
                         (vec (mapcat vector parameters initial))
                         (apply list loop-name parameters))))

           :else
           (apply list
                  (map #(rewrite-function-recur % recur-target counter)
                       form)))
         (meta form)))

     (vector? form)
     (with-meta
       (mapv #(rewrite-function-recur % recur-target counter) form)
       (meta form))

     (map? form)
     (with-meta
       (into (empty form)
             (map (fn [[key value]]
                    [(rewrite-function-recur key recur-target counter)
                     (rewrite-function-recur value recur-target counter)])
                  form))
       (meta form))

     :else form)))

(defn rewrite-struct-merge
  "associates map entries into a native struct without degrading its type"
  {:added "4.1"}
  [form]
  (let [[_ target source] form]
    (list 'reduce
          (list 'fn ['output 'entry]
                (list 'assoc
                      'output
                      (list 'key 'entry)
                      (list 'val 'entry)))
          target
          source)))

(defn rewrite-native-result
  "rewrites Foundation Wrapped calls to the native Result wrapper surface"
  {:added "4.1"}
  [form]
  (case (first form)
    h/wrapped
    (list 'result :success (second form))

    h/wrapped?
    (cons 'result? (rest form))

    form))

(defn rewrite-character-whitespace
  "rewrites JVM character whitespace checks to the native String surface"
  {:added "4.1"}
  [form]
  (list 'String/blank? (list 'str (second form))))

(defn rewrite-iobj-instance
  "rewrites the JVM metadata marker interface to Hara's native protocol"
  {:added "4.1"}
  [form]
  (list 'satisfies? 'IObjType (nth form 2)))

(defn rewrite-iobj-metadata
  "checks that a native metadata-capable value carries metadata"
  {:added "4.1"}
  [form]
  (let [value (nth form 2)]
    (list 'and
          (list 'satisfies? 'IObjType value)
          (list 'seq (list 'meta value)))))

(defn rewrite-exception-constructor
  "rewrites the JVM Exception constructor to native exception data"
  {:added "4.1"}
  [form]
  (list 'ex-info (second form) {}))

(defn rewrite-integer-parse
  "rewrites JVM integer parsing to the native numeric parser"
  {:added "4.1"}
  [form]
  (list 'parse-long (second form)))

(defn rewrite-number-format-catch
  "rewrites the JVM parse exception catch to native Throwable handling"
  {:added "4.1"}
  [form]
  (apply list 'catch 'Throwable
         (concat (drop 2 form)
                 (when (= 3 (count form)) [nil]))))

(defn rewrite-keep-indexed
  "expands Clojure keep-indexed into native map-indexed and keep calls"
  {:added "4.1"}
  [form]
  (if (and (= '->> (first form))
           (= 3 (count form))
           (seq? (nth form 2))
           (= 'keep-indexed (first (nth form 2))))
    (let [input    (second form)
          function (second (nth form 2))]
      (list 'keep 'identity (list 'map-indexed function input)))
    (let [[_ function input] form]
      (list 'keep 'identity (list 'map-indexed function input)))))

(defn rewrite-process-path-binding
  "lowers process-path's nested sequential parameter into a let binding"
  {:added "4.1"}
  [form]
  (->> form
       (walk/postwalk
        (fn [node]
          (if (and (seq? node)
                   (vector? (first node))
                   (= 2 (count (first node)))
                   (vector? (first (first node))))
            (let [parameters (first node)
                  source     'migration-path]
              (list (vec [source (second parameters)])
                    (apply list
                           'let
                           ['more source
                            'x (list 'first 'more)
                            'y (list 'second 'more)
                            'xs (list 'drop 2 'more)]
                           (rest node))))
            node)))
       (#(rewrite-function-recur % 'process-path))
       (walk/postwalk
        (fn [node]
          (if (and (seq? node)
                   (= 'process-path (first node))
                   (= 3 (count node))
                   (seq? (second node))
                   (= 'cons (first (second node))))
            (list 'process-path
                  (list 'vec (second node))
                  (nth node 2))
            node)))))

(defn safe-sequential-bindings
  "lowers a Clojure sequential pattern to bounds-safe native bindings"
  {:added "4.1"}
  [pattern value temporary]
  (let [amp-index (.indexOf ^java.util.List pattern '&)
        as-index  (.indexOf ^java.util.List pattern :as)
        stops     (filter #(not (neg? %)) [amp-index as-index])
        end       (if (seq stops) (apply min stops) (count pattern))
        positional (subvec pattern 0 end)
        rest-name (when-not (neg? amp-index) (nth pattern (inc amp-index)))
        as-name   (when-not (neg? as-index) (nth pattern (inc as-index)))]
    (vec
     (concat
      [temporary value]
      (when as-name [as-name temporary])
      (mapcat (fn [index name]
                [name (list 'first (list 'drop index temporary))])
              (range)
              positional)
      (when rest-name
        [rest-name (list 'drop (count positional) temporary)])))))

(defn rewrite-nested-sequential-parameters
  "lowers strict nested vector parameters to bounds-safe native let bindings"
  {:added "4.1"}
  [form]
  (let [parameter-index (first
                         (keep-indexed (fn [index value]
                                         (when (vector? value) index))
                                       form))
        parameters      (when parameter-index (nth form parameter-index))
        nested          (keep-indexed (fn [index value]
                                        (when (vector? value) [index value]))
                                      parameters)]
    (if (seq nested)
      (let [rewritten (reduce (fn [output [index _]]
                                (assoc output index
                                       (symbol (str "migration-argument-" index))))
                              parameters
                              nested)
            bindings  (vec
                       (mapcat (fn [[index pattern]]
                                 (let [temporary (nth rewritten index)]
                                   (safe-sequential-bindings
                                    pattern temporary
                                    (symbol (str (name temporary) "-value")))))
                               nested))]
        (apply list
               (concat (take parameter-index form)
                       [rewritten
                        (apply list 'let bindings
                               (drop (inc parameter-index) form))])))
      form)))

(defn rewrite-merge-meta
  "lowers Foundation merge-meta to native metadata primitives"
  {:added "4.1"}
  [[_ value metadata]]
  (list 'with-meta value
        (list 'merge-nested (list 'meta value) metadata)))

(defn rewrite-fixed-apply
  "lowers Clojure apply with fixed arguments to one explicit native sequence"
  {:added "4.1"}
  [[_ function fixed arguments]]
  (if (= 'list function)
    (list 'cons fixed arguments)
    (list 'apply function (list 'concat [fixed] arguments))))

(defn rewrite-layout-default-form-shadow
  "renames layout-default-fn's duplicate let binding for native lexical scope"
  {:added "4.1"}
  [form]
  (let [body            (last form)
        bindings        (second body)
        duplicate-index (first
                         (keep-indexed
                          (fn [index value]
                            (when (and (even? index)
                                       (pos? index)
                                       (= 'form value))
                              index))
                          bindings))
        replace-form    (fn [value]
                          (walk/postwalk
                           #(if (= 'form %) 'annotated-form %)
                           value))
        bindings        (vec
                         (concat
                          (subvec bindings 0 duplicate-index)
                          ['annotated-form (nth bindings (inc duplicate-index))]
                          (map replace-form
                               (subvec bindings (+ 2 duplicate-index)))))
        body            (apply list 'let bindings
                               (map replace-form (nnext body)))]
    (apply list (concat (butlast form) [body]))))

(declare postwalk-code)

(defn rewrite-duplicate-let-bindings
  "renames sequential Clojure let shadows and their later references"
  {:added "4.1"}
  [form]
  (postwalk-code
   (fn [node]
     (if (and (seq? node) (= 'let (first node)) (vector? (second node)))
       (let [state
             (reduce
              (fn [{:keys [bindings seen renames]} [binding value]]
                (let [value   (walk/postwalk #(get renames % %) value)
                      count   (get seen binding 0)
                      renamed (if (and (symbol? binding) (pos? count))
                                (symbol (str (clojure.core/name binding)
                                             \- \- count))
                                binding)]
                  {:bindings (conj bindings renamed value)
                   :seen (assoc seen binding (inc count))
                   :renames (if (= binding renamed)
                              renames
                              (assoc renames binding renamed))}))
              {:bindings [] :seen {} :renames {}}
              (partition 2 (second node)))
             body (map #(walk/postwalk
                         (fn [value] (get (:renames state) value value))
                         %)
                       (nnext node))]
         (apply list 'let (:bindings state) body))
       node))
   form))

(defn rewrite-heal-core-destructuring
  "lowers optional and short sequential destructuring used by heal.core"
  {:added "4.1"}
  [form]
  (walk/postwalk
   (fn [node]
     (cond
       (and (seq? node)
            (= 'defn (first node))
            (= 'get-block-lines (second node)))
       (let [parameter-index (first
                              (keep-indexed
                               (fn [index value]
                                 (when (vector? value) index))
                               node))]
         (if (= '[lines [start end] start-col & [end-col]]
                (nth node parameter-index))
           (apply list
                  (concat
                   (take parameter-index node)
                   ['[lines migration-line start-col & migration-end-cols]
                   (apply list
                          'let
                          (vec (concat
                                (safe-sequential-bindings
                                 '[start end] 'migration-line 'migration-line-value)
                                (safe-sequential-bindings
                                 '[end-col] 'migration-end-cols 'migration-end-cols-value)))
                          (drop (inc parameter-index) node))]))
           node))

       (and (seq? node)
            (= 'let (first node))
            (vector? (second node))
            (contains? #{'[e1 e2 e3 & more] '[e1 e2 & more]}
                       (first (second node))))
       (let [bindings (second node)
             pattern (first bindings)
             temporary (if (= 3 (.indexOf ^java.util.List pattern '&))
                         'migration-errors-three
                         'migration-errors-two)]
         (apply list
                'let
                (vec (concat (safe-sequential-bindings
                              pattern (second bindings) temporary)
                             (drop 2 bindings)))
                (drop 2 node)))

       :else node))
   form))

(defn rewrite-heal-content-loop
  "lowers heal-content's bounded tail loop without retaining recursive frames"
  {:added "4.1"}
  [form]
  (let [parameter-index (first
                         (keep-indexed
                          (fn [index value]
                            (when (vector? value) index))
                          form))
        body '(reduce
               (fn [current _]
                 (let [next-content (heal-content-single-pass current)]
                   (if (= next-content current)
                     (reduced current)
                     next-content)))
               content
               (range 51))]
    (apply list
           (concat (take (inc parameter-index) form)
                   [body]))))

(defn rewrite-list-compatible
  "rewrites Clojure list checks to Hara's native form predicate"
  {:added "4.1"}
  [form]
  (list 'form? (second form)))

(defn rewrite-fn-form-compatible
  "narrows native callable checks to Clojure fn? semantics for forms"
  {:added "4.1"}
  [form]
  (let [value (second form)]
    (list 'and
          (list 'std.foundation/fn? value)
          (list 'not (list 'form? value)))))

(defn rewrite-string-starts-with
  "rewrites JVM String.startsWith calls to the native String surface"
  {:added "4.1"}
  [form]
  (cons 'String/starts-with? (rest form)))

(defn rewrite-character-array-string
  "rewrites JVM character-array construction to a native string fold"
  {:added "4.1"}
  [form]
  (list 'apply 'str (second form)))

(defn rewrite-join-lines
  "rewrites Foundation prose line joining to the native string surface"
  {:added "4.1"}
  [form]
  (list 'apply 'str
        (list 'interpose "\n" (second form))))

(defn rewrite-prose-pipe
  "rewrites Foundation's variadic prose line constructor"
  {:added "4.1"}
  [form]
  (list 'std.foundation.string/join "\n" (vec (rest form))))

(defn rewrite-string-join [form]
  (list 'apply 'str
        (list 'interpose (second form) (nth form 2))))

(defn rewrite-thread-string-join [form]
  (let [join-step (last form)
        values (cons '->> (butlast (rest form)))]
    (list 'apply 'str
          (list 'interpose (second join-step) values))))

(defn rewrite-safe-sequential-get [form]
  (let [values (second form)
        index (nth form 2)]
    (list 'if
          (list '>= index 0)
          (list 'first (list 'drop index values))
          nil)))

(defn map-vals-form [function values]
  (list 'reduce-kv
        (list 'fn ['output 'key 'value]
              (list 'assoc 'output 'key (list function 'value)))
        {}
        values))

(defn rewrite-map-vals [form]
  (if (= '->> (first form))
    (let [step (last form)
          values (cons '->> (butlast (rest form)))]
      (map-vals-form (second step) values))
    (map-vals-form (second form) (nth form 2))))

(defn rewrite-some [form]
  (list 'first (list 'filter (second form) (nth form 2))))

(defn rewrite-map-juxt [form]
  (let [selectors (second form)
        values (nth form 2)
        value 'migration-map-juxt-value
        select (fn [selector]
                 (if (keyword? selector)
                   (list 'get value selector)
                   (list selector value)))]
    (list 'into {}
          (list 'map
                (list 'fn [value]
                      (vec (map select selectors)))
                values))))

(defn rewrite-file-slurp [form]
  (list 'std.foundation.string/decode-utf8
        (list 'deref (list 'File/read (second form)))))

(defn rewrite-file-spit [form]
  (list 'deref
        (list 'File/write
              (second form)
              (list 'std.foundation.string/encode-utf8 (nth form 2))
              {:mode :replace})))

(defn rewrite-java-temp-file [form]
  (let [[_ prefix suffix] form]
    (list 'std.lib.fs/temp-file
          "/"
          {:prefix prefix :suffix suffix})))

(defn rewrite-heal-line-format [form]
  (list 'std.foundation.string/pad-left
        (list 'str (nth form 2)) 4 " "))

(defn rewrite-rename-keys [form]
  (list 'reduce-kv
        '(fn [output from to]
           (if (has? output from)
             (assoc (dissoc output from) to (get output from))
             output))
        (second form) (nth form 2)))

(defn map-parameter-bindings [pattern source]
  (let [defaults  (:or pattern)
        value-for (fn [binding-symbol selector]
                    (let [value (list 'get source selector)]
                      (if (contains? defaults binding-symbol)
                        (list 'if (list 'has? source selector)
                              value
                              (get defaults binding-symbol))
                        value)))]
    (vec
     (concat
      (when-let [alias (:as pattern)]
        [alias (if (= alias 'entry) (list 'into {} source) source)])
      (mapcat (fn [binding-symbol]
                [binding-symbol
                 (value-for binding-symbol
                            (keyword (clojure.core/name binding-symbol)))])
              (:keys pattern))
      (mapcat (fn [binding-symbol]
                [binding-symbol
                 (value-for binding-symbol
                            (list 'quote
                                  (symbol (clojure.core/name binding-symbol))))])
              (:syms pattern))
      (mapcat (fn [binding-symbol]
                [binding-symbol
                 (value-for binding-symbol
                            (clojure.core/name binding-symbol))])
              (:strs pattern))
      (mapcat (fn [[binding-symbol selector]]
                [binding-symbol (value-for binding-symbol selector)])
              (remove (fn [[key _]]
                        (contains? #{:as :keys :syms :strs :or} key))
                      pattern))))))

(defn postwalk-code
  "walks executable forms without descending through quote boundaries"
  {:added "4.1"}
  [function form]
  (if (and (seq? form) (= 'quote (first form)))
    form
    (function (walk/walk #(postwalk-code function %) identity form))))

(defn rewrite-map-destructuring
  "lowers Clojure map patterns in function parameters and let bindings"
  {:added "4.1"}
  [form]
  (let [counter (atom 0)
        lower-parameters
        (fn [parameters body]
          (let [lowered (mapv (fn [parameter]
                                (if (map? parameter)
                                  (symbol (str "migration-map-param-"
                                               (swap! counter inc)))
                                  parameter))
                              parameters)
                bindings (vec (mapcat (fn [pattern source]
                                        (if (map? pattern)
                                          (map-parameter-bindings pattern source)
                                          []))
                                      parameters lowered))]
            [lowered (if (seq bindings)
                       [(apply list 'let bindings body)]
                       body)]))]
    (postwalk-code
     (fn [node]
       (cond
         (and (seq? node) (= 'let (first node)) (vector? (second node)))
         (let [bindings (partition 2 (second node))
               lowered  (vec
                         (mapcat
                          (fn [[pattern value]]
                            (if (map? pattern)
                              (let [source (symbol (str "migration-map-value-"
                                                       (swap! counter inc)))]
                                (concat [source value]
                                        (map-parameter-bindings pattern source)))
                              [pattern value]))
                          bindings))]
           (apply list 'let lowered (nnext node)))

         (and (seq? node) (= 'fn (first node)) (vector? (second node)))
         (let [[parameters body] (lower-parameters (second node) (nnext node))]
           (apply list 'fn parameters body))

         (and (seq? node) (= 'defn (first node)))
         (let [parameter-index (first
                                (keep-indexed (fn [index value]
                                                (when (vector? value) index))
                                              node))]
           (if parameter-index
             (let [[parameters body]
                   (lower-parameters (nth node parameter-index)
                                     (drop (inc parameter-index) node))]
               (apply list
                      (concat (take parameter-index node)
                              [parameters]
                              body)))
             (apply list
                    (map (fn [value]
                           (if (and (seq? value) (vector? (first value)))
                             (let [[parameters body]
                                   (lower-parameters (first value) (rest value))]
                               (apply list parameters body))
                             value))
                         node))))

         :else node))
     form)))

(defn rewrite-nested-map-parameters [form]
  (let [counter (atom 0)]
    (walk/postwalk
     (fn [node]
       (if (and (seq? node) (= 'fn (first node))
                (vector? (second node)) (some map? (second node)))
         (let [parameters (second node)
               lowered (mapv (fn [parameter]
                               (if (map? parameter)
                                 (symbol (str "migration-map-param-"
                                              (swap! counter inc)))
                                 parameter))
                             parameters)
               bindings (vec (mapcat (fn [pattern source]
                                       (if (map? pattern)
                                         (map-parameter-bindings pattern source)
                                         []))
                                     parameters lowered))]
           (list* 'fn lowered
                  (list (list* 'let bindings (nnext node)))))
         node))
     form)))

(defn rewrite-pair-delimiter-copies [form]
  (walk/postwalk
   (fn [node]
     (cond
       (and (seq? node) (= 'let (first node))
            (vector? (second node)) (= 'open (first (second node))))
       (let [bindings (second node)]
         (list* 'let
                (assoc bindings 1 (list 'into {} (second bindings)))
                (nnext node)))

       (= '(pop stack) node)
       '(vec (butlast stack))

       :else
       node))
   (rewrite-nested-map-parameters form)))

(defn rewrite-print-rainbow-loop [form]
  (walk/postwalk
   (fn [node]
     (if (and (seq? node)
              (= 'loop (first node))
              (= '[chars (seq content) line-num 1 col-num 1] (second node)))
       '(do
          (reduce
           (fn [[line-num col-num] char]
             (let [color (get color-map [line-num col-num])]
               (if color
                 (Printer/p (str color char +reset-color+))
                 (Printer/p char))
               (if (= char \newline)
                 [(inc line-num) 1]
                 [line-num (inc col-num)])))
           [1 1]
           (seq content))
          nil)
       node))
   form))

(defn rewrite-parse-delimiters-loop [_]
  '(defn parse-delimiters
     "gets all the delimiters in the file"
     {:added "4.0"}
     [content]
     (let [state
           (reduce
            (fn [state char]
              (let [line-num    (get state :line-num)
                    col-num     (get state :col-num)
                    in-comment? (get state :in-comment?)
                    in-string?  (get state :in-string?)
                    escaped?    (get state :escaped?)]
                (cond
                  (= char \newline)
                  (assoc state :line-num (inc line-num) :col-num 1
                         :in-comment? false :escaped? false)

                  escaped?
                  (assoc state :col-num (inc col-num) :escaped? false)

                  (= char \\)
                  (assoc state :col-num (inc col-num) :escaped? true)

                  in-comment?
                  (assoc state :col-num (inc col-num))

                  in-string?
                  (if (= char \")
                    (assoc state :col-num (inc col-num)
                           :in-comment? false :in-string? false)
                    (assoc state :col-num (inc col-num)
                           :in-comment? false :in-string? true))

                  :else
                  (let [info (delimiter-info char)]
                    (cond
                      (= char \;)
                      (assoc state :col-num (inc col-num)
                             :in-comment? true :in-string? false)

                      (= char \")
                      (assoc state :col-num (inc col-num)
                             :in-comment? false :in-string? true)

                      info
                      (assoc state
                             :col-num (inc col-num)
                             :delimiters
                             (conj (get state :delimiters)
                                   (merge {:char (str char)
                                           :line line-num
                                           :col col-num}
                                          info)))

                      :else
                      (assoc state :col-num (inc col-num)))))))
            {:line-num 1 :col-num 1
             :in-comment? false :in-string? false :escaped? false
             :delimiters []}
            (seq content))]
       (get state :delimiters))))

(defn rewrite-character-literal
  "rewrites ambiguous Clojure delimiter characters to portable native expressions"
  {:added "4.1"}
  [character]
  (case (int character)
    34 (list 'first (list 'pr-str ""))
    92 (list 'first
             (list 'pr-str
                   (list 'first (list 'pr-str ""))))
    character))

(defn rewrite-nil-set
  "rewrites a Clojure set containing nil to native runtime construction"
  {:added "4.1"}
  [values]
  (list 'set (vec (sort-by pr-str values))))

(defn rewrite-ratio-literal
  "rewrites a Clojure ratio literal to native numeric division"
  {:added "4.1"}
  [value]
  (list '/ (numerator value) (denominator value)))

(defn rewrite-token-checks
  "replaces Clojure host numeric categories with the native Hara taxonomy"
  {:added "4.1"}
  [form]
  (list 'def
        (second form)
        {:nil 'nil?
         :boolean 'boolean?
         :number 'number?
         :keyword 'keyword?
         :symbol 'symbol?
         :string 'string?
         :char 'char?}))

(defn escaped-character-string?
  "checks for a tab or quote escape amplified by the Foundation block reader"
  {:added "4.1"}
  [value]
  (and (string? value)
       (or (re-matches #"\\+t" value)
           (re-matches #"\\+\"" value))))

(defn rewrite-escaped-character-first
  "normalizes escaped one-character strings produced by the Foundation reader"
  {:added "4.1"}
  [form]
  (let [value (second form)]
    (cond (re-matches #"\\+t" value)
          \tab

          (re-matches #"\\+\"" value)
          (rewrite-character-literal \")

          :else form)))

(defn rewrite-nil-membership
  "preserves Clojure membership for a set whose nil member is significant"
  {:added "4.1"}
  [form]
  (list 'or
        (list 'nil? (nth form 2))
        (list 'has? (second form) (nth form 2))))

(defn rewrite-check-tag
  "rewrites map sequence destructuring to native reduce-kv traversal"
  {:added "4.1"}
  [form]
  (let [body '(reduce-kv
               (fn [out tag check]
                 (if out
                   out
                   (if (check input) tag nil)))
               nil
               checks)]
    (apply list (concat (butlast form) [body]))))

(defn rewrite-zero-arity-thread-steps
  "normalizes empty Clojure thread calls to native callable steps"
  {:added "4.1"}
  [form]
  (apply list
         (first form)
         (second form)
         (map (fn [step]
                (if (and (seq? step) (= 1 (count step)))
                  (first step)
                  step))
              (drop 2 form))))

(defn rewrite-iterator-first-pipeline
  "moves a lazy first-search pipeline onto a consumptive native iterator"
  {:added "4.1"}
  [form]
  (let [[_ iterate-form & steps] form
        [_ move seed]          iterate-form
        normalized             (map (fn [step]
                                      (if (and (seq? step)
                                               (= 1 (count step)))
                                        (first step)
                                        step))
                                    steps)]
    (list 'apply-with
          seed
          (apply list
                 'comp
                 (concat (reverse normalized)
                         ['iter (list 'partial 'iterate move)])))))

(defn rewrite-navigation-template-vars
  "expands Foundation navigation accessors without native macro evaluation"
  {:added "4.1"}
  [form]
  (apply list
         'do
         (map (fn [entry]
                (let [[sym accessor] entry]
                  (list 'defn
                        sym
                        (list ['zip]
                              (list sym 'zip :right))
                        (list ['zip 'step]
                              (list 'if-let
                                    ['elem (list 'std.lib.zip/get 'zip)]
                                    (list accessor 'elem))))))
              (drop 2 form))))

(defn iterator-first-pipeline?
  "checks for the bounded lazy search shape requiring iterator consumption"
  {:added "4.1"}
  [form]
  (let [[thread iterate-form & steps] form
        step-head (fn [step]
                    (if (seq? step) (first step) step))]
    (and (= '->> thread)
         (seq? iterate-form)
         (= 'iterate (first iterate-form))
         (= 3 (count iterate-form))
         (= ['drop 'take-while 'filter 'first]
            (mapv step-head steps)))))

(defn rewrite-navigator-format
  "replaces the JVM formatter used by the historical navigator display"
  {:added "4.1"}
  [form]
  (let [[_ _ row col status] form]
    (list 'str "<" row "," col "> "
          (list 'apply 'str
                (list 'map 'std.block.base/block-representation
                      (nth status 2))))))

(defn cursor-compare-form?
  "checks for the JVM Comparable cursor lookup used by std.lib.zip"
  {:added "4.1"}
  [form]
  (and (seq? form)
       (= 'zero? (first form))
       (= 2 (count form))
       (seq? (second form))
       (= 'compare (first (second form)))
       (= 3 (count (second form)))))

(defn rewrite-zip-cursor-comparator
  "adds and uses a configurable native cursor comparator in std.lib.zip"
  {:added "4.1"}
  [form]
  (cond
    (and (= 'defonce (first form)) (= '+base+ (second form)))
    (list 'defonce '+base+
          (assoc (nth form 2) :cursor-equal? '=))

    (and (= 'defn (first form)) (= 'from-status (second form)))
    (walk/postwalk
     (fn [node]
       (if (cursor-compare-form? node)
         (let [[_ [_ left right]] node]
           (list (list :cursor-equal? 'context) left right))
         node))
     form)

    :else form))

(defn rewrite-block-cursor-comparator
  "configures std.block.navigate to compare logical block identity"
  {:added "4.1"}
  [form]
  (walk/postwalk
   (fn [node]
     (if (and (map? node) (contains? node :cursor))
       (assoc node
              :cursor-equal?
              '(fn [left right]
                 (and (= (base/block-type left) (base/block-type right))
                      (= (base/block-tag left) (base/block-tag right))
                      (= (base/block-string left) (base/block-string right)))))
       node))
   form))

(defn quoted-location?
  "returns true when a structural rewrite cursor is inside quoted code data"
  {:added "4.1"}
  [location]
  (loop [cursor (nav/up location)]
    (if (or (nil? cursor) (= :root (nav/tag cursor)))
      false
      (let [value (nav/value cursor)]
        (if (and (seq? value) (= 'quote (first value)))
          true
          (recur (nav/up cursor)))))))

(defn rewrite-target-forms
  "applies explicitly enabled structural adaptations for one target"
  {:added "4.1"}
  [root target applied]
  (let [rules (set (:target/rules target))]
    (query/modify
     root
     [seq?]
     (fn [location]
       (if (= :root (nav/tag location))
         location
         (let [original (nav/value location)
               form     (if (some rules
                                  [:clojure/character-literal
                                   :clojure/nil-set
                                   :clojure/ratio-literal])
                        (walk/postwalk
                         (fn [value]
                           (cond
                             (char? value)
                             (let [rewritten (rewrite-character-literal value)]
                               (when (not= value rewritten)
                                 (swap! applied conj :clojure/character-literal))
                               rewritten)

                             (and (contains? rules :clojure/nil-set)
                                  (set? value)
                                  (contains? value nil))
                             (do (swap! applied conj :clojure/nil-set)
                                 (rewrite-nil-set value))

                             (and (contains? rules :clojure/ratio-literal)
                                  (ratio? value))
                             (do (swap! applied conj :clojure/ratio-literal)
                                 (rewrite-ratio-literal value))

                             :else value))
                         original)
                        original)]
         (cond
           (and (contains? rules :foundation/form-predicate)
                (contains? #{'c/form? 'form?} (first form))
                (= 2 (count form)))
           (do (swap! applied conj :foundation/form-predicate)
               (nav/replace location
                            (parsed-form-block
                             (list 'or
                                   (list 'list? (second form))
                                   (list 'seq? (second form))))))

           (and (contains? rules :clojure/apply-fixed-arguments-native)
                (not (quoted-location? location))
                (= 'apply (first form))
                (= 4 (count form)))
           (do (swap! applied conj :clojure/apply-fixed-arguments-native)
               (nav/replace location
                            (parsed-form-block (rewrite-fixed-apply form))))

           (and (contains? rules :clojure/map-destructuring-native)
                (not (quoted-location? location))
                (contains? #{'defn 'fn 'let} (first form))
                (not= form (rewrite-map-destructuring form)))
           (let [rewritten (rewrite-map-destructuring form)
                 rewritten (if (and (= 'defn (first rewritten))
                                    (= 'layout-default-fn (second rewritten)))
                             (rewrite-layout-default-form-shadow rewritten)
                             rewritten)
                 rewritten (if (and (contains? rules :foundation/layout-map-pairs-native)
                                    (= 'defn (first rewritten))
                                    (= 'layout-two-column (second rewritten)))
                             (rewrite-layout-map-pairs rewritten)
                             rewritten)
                 rewritten (rewrite-duplicate-let-bindings rewritten)]
             (swap! applied conj :clojure/map-destructuring-native)
             (when (not= rewritten
                         (rewrite-map-destructuring form))
               (swap! applied conj :clojure/duplicate-let-binding-native))
             (when (= 'layout-default-fn (second form))
               (swap! applied conj :foundation/layout-default-shadow-native))
             (when (= 'layout-two-column (second form))
               (swap! applied conj :foundation/layout-map-pairs-native))
             (nav/replace location (parsed-form-block rewritten)))

           (and (contains? rules :clojure/print-meta-binding-native)
                (= 'binding (first form))
                (= '[*print-meta* true] (second form)))
           (do (swap! applied conj :clojure/print-meta-binding-native)
               (nav/replace location
                            (parsed-form-block
                             (rewrite-print-meta-binding form))))

           (and (contains? rules :foundation/layout-hiccup-boolean-native)
                (not (quoted-location? location))
                (= 'defn (first form))
                (= 'layout-hiccup-like (second form)))
           (do (swap! applied conj :foundation/layout-hiccup-boolean-native)
               (nav/replace location
                            (parsed-form-block
                             (rewrite-layout-hiccup-boolean form))))

           (and (contains? rules :foundation/layout-readable-width-native)
                (not (quoted-location? location))
                (= 'defn (first form))
                (= 'get-max-width (second form)))
           (do (swap! applied conj :foundation/layout-readable-width-native)
               (nav/replace location
                            (parsed-form-block
                             (rewrite-layout-readable-width form))))

           (and (contains? rules :foundation/layout-default-shadow-native)
                (not (quoted-location? location))
                (= 'defn (first form))
                (= 'layout-default-fn (second form)))
           (do (swap! applied conj :foundation/layout-default-shadow-native)
               (nav/replace location
                            (parsed-form-block
                             (rewrite-layout-default-form-shadow form))))

           (and (contains? rules :clojure/map-entry-traversal)
                (= 'defn (first form))
                (= 'tag (second form)))
           (do (swap! applied conj :clojure/map-entry-traversal)
               (nav/replace location (rewrite-check-tag form)))

           (and (contains? rules :clojure/nil-membership)
                (= 'contains? (first form))
                (= '*boundaries* (second form)))
           (do (swap! applied conj :clojure/nil-membership)
               (nav/replace location (rewrite-nil-membership form)))

           (and (contains? rules :clojure/escaped-character-first)
                (= 'first (first form))
                (escaped-character-string? (second form)))
           (do (swap! applied conj :clojure/escaped-character-first)
               (nav/replace location
                            (rewrite-escaped-character-first form)))

           (and (contains? rules :foundation/native-number-taxonomy)
                (= 'def (first form))
                (= '*token-checks* (second form)))
           (do (swap! applied conj :foundation/native-number-taxonomy)
               (nav/replace location (rewrite-token-checks form)))

           (and (or (contains? rules :clojure/source-namespace-overrides)
                    (contains? rules :clojure/test-namespace-overrides))
                (= 'ns (first form)))
           (do (swap! applied conj (if (= :test (:unit/kind target))
                                     :clojure/test-namespace-overrides
                                     :clojure/source-namespace-overrides))
               (let [rewritten (rewrite-ns-form
                                form
                                (:target/overrides target)
                                (if (= :test (:unit/kind target))
                                  (or (:target/target-test-namespace target)
                                      (:target/target-source-namespace target))
                                  (:target/target-source-namespace target)))
                     rewritten (if (and (= :test (:unit/kind target))
                                        (:target/test-alias target))
                                 (add-require-alias
                                  rewritten
                                  (:target/source-namespace target)
                                  (:target/test-alias target))
                                 rewritten)]
                 (nav/replace location rewritten)))

           (and (contains? rules :foundation/block-interfaces-to-protocol)
                (= 'definterface (first form)))
           (do (swap! applied conj :foundation/block-interfaces-to-protocol)
               (if-let [rewritten (rewrite-block-interface form)]
                 (nav/replace location rewritten)
                 (nav/delete location)))

           (and (contains? rules :foundation/defrecord-native-struct)
                (= 'defrecord (first form)))
           (do (swap! applied conj :foundation/defrecord-native-struct)
               (nav/replace location
                            (list 'defstruct
                                  (second form)
                                  (get-in target
                                          [:target/struct-fields (second form)]
                                          (nth form 2)))))

           (and (contains? rules :foundation/drop-host-printer)
                (= 'defmethod (first form))
                (= 'print-method (second form)))
           (do (swap! applied conj :foundation/drop-host-printer)
               (nav/delete location))

           (and (contains? rules :clojure/empty-catch-body)
                (= 'catch (first form))
                (= 3 (count form)))
           (do (swap! applied conj :clojure/empty-catch-body)
               (nav/replace location (apply list (concat form [nil]))))

           (and (contains? rules :foundation/navigation-template-vars)
                (= 'f/template-vars (first form)))
           (do (swap! applied conj :foundation/navigation-template-vars)
               (nav/replace location
                            (rewrite-navigation-template-vars form)))

           (and (contains? rules :foundation/navigator-format)
                (= 'format (first form))
                (= "<%d,%d> %s" (second form))
                (= 5 (count form)))
           (do (swap! applied conj :foundation/navigator-format)
               (nav/replace location (rewrite-navigator-format form)))

           (and (contains? rules :foundation/zip-cursor-comparator)
                (or (and (= 'defonce (first form))
                         (= '+base+ (second form)))
                    (and (= 'defn (first form))
                         (= 'from-status (second form)))))
           (do (swap! applied conj :foundation/zip-cursor-comparator)
               (nav/replace location (rewrite-zip-cursor-comparator form)))

           (and (contains? rules :foundation/block-cursor-comparator)
                (= 'defn (first form))
                (= 'navigator (second form)))
           (do (swap! applied conj :foundation/block-cursor-comparator)
               (nav/replace location (rewrite-block-cursor-comparator form)))

           (and (contains? rules :clojure/keep-indexed-native)
                (or (and (= 'keep-indexed (first form))
                         (= 3 (count form)))
                    (and (= '->> (first form))
                         (= 3 (count form))
                         (seq? (nth form 2))
                         (= 'keep-indexed (first (nth form 2))))))
           (do (swap! applied conj :clojure/keep-indexed-native)
               (nav/replace location (rewrite-keep-indexed form)))

           (and (contains? rules :clojure/process-path-binding-native)
                (= 'defn (first form))
                (= 'process-path (second form)))
           (do (swap! applied conj :clojure/process-path-binding-native)
               (nav/replace location
                            (parsed-form-block
                            (rewrite-process-path-binding form))))

           (and (contains? rules :clojure/optional-rest-parameter-native)
                (not (quoted-location? location))
                (= 'defn (first form))
                (not= form (rewrite-optional-rest-parameter form)))
           (let [rewritten (rewrite-optional-rest-parameter form)]
             (swap! applied conj :clojure/optional-rest-parameter-native)
             (if (and (contains? rules :foundation/layout-optional-opts-native)
                      (= 'layout-main (second form)))
               (do (swap! applied conj :foundation/layout-optional-opts-native)
                   (nav/replace location
                                (parsed-form-block
                                 (rewrite-layout-optional-opts rewritten))))
               (nav/replace location (parsed-form-block rewritten))))

           (and (contains? rules :clojure/nested-sequential-parameter-native)
                (not (quoted-location? location))
                (contains? #{'defn 'fn} (first form))
                (not= form (rewrite-nested-sequential-parameters form)))
           (let [rewritten (rewrite-nested-sequential-parameters form)]
             (swap! applied conj :clojure/nested-sequential-parameter-native)
             (nav/replace location (parsed-form-block rewritten)))

           (and (contains? rules :foundation/merge-meta-native)
                (= 'c/merge-meta (first form)))
           (do (swap! applied conj :foundation/merge-meta-native)
               (nav/replace location
                            (parsed-form-block (rewrite-merge-meta form))))

           (and (contains? rules :foundation/heal-core-destructuring-native)
                (= 'defn (first form))
                (contains? #{'get-block-lines
                             'heal-content-complex-edits
                             'heal-content-single-pass}
                           (second form)))
           (do (swap! applied conj :foundation/heal-core-destructuring-native)
               (nav/replace location
                            (parsed-form-block
                             (rewrite-heal-core-destructuring form))))

           (and (contains? rules :foundation/heal-content-reduce-native)
                (= 'defn (first form))
                (= 'heal-content (second form)))
           (do (swap! applied conj :foundation/heal-content-reduce-native)
               (nav/replace location
                            (parsed-form-block
                             (rewrite-heal-content-loop form))))

           (and (contains? rules :foundation/heal-print-loop-native)
                (= 'defn (first form))
                (= 'print-rainbow (second form)))
           (do (swap! applied conj :foundation/heal-print-loop-native)
               (nav/replace location (rewrite-print-rainbow-loop form)))

           (and (contains? rules :foundation/heal-parse-delimiters-native)
                (= 'defn (first form))
                (= 'parse-delimiters (second form)))
           (do (swap! applied conj :foundation/heal-parse-delimiters-native)
               (nav/replace location (rewrite-parse-delimiters-loop form)))

           (and (contains? rules :clojure/list-cons-compatible)
                (= 'list? (first form))
                (= 2 (count form)))
           (do (swap! applied conj :clojure/list-cons-compatible)
               (nav/replace location (rewrite-list-compatible form)))

           (and (contains? rules :clojure/fn-form-compatible)
                (= 'fn? (first form))
                (= 2 (count form)))
           (do (swap! applied conj :clojure/fn-form-compatible)
               (nav/replace location (rewrite-fn-form-compatible form)))

           (and (contains? rules :clojure/named-recur)
                (= 'defn (first form))
                (some #(and (seq? %) (= 'recur (first %)))
                      (tree-seq coll? seq form)))
           (do (swap! applied conj :clojure/named-recur)
               (nav/replace location
                            (rewrite-function-recur form (second form))))

           (and (contains? rules :foundation/struct-merge)
                (= 'merge (first form))
                (= 'zip (second form))
                (= 3 (count form)))
           (do (swap! applied conj :foundation/struct-merge)
               (nav/replace location (rewrite-struct-merge form)))

           (and (contains? rules :foundation/native-result)
                (#{'h/wrapped 'h/wrapped?} (first form)))
           (do (swap! applied conj :foundation/native-result)
               (nav/replace location (rewrite-native-result form)))

           (and (contains? rules :clojure/character-whitespace)
                (= 'Character/isWhitespace (first form)))
           (do (swap! applied conj :clojure/character-whitespace)
               (nav/replace location (rewrite-character-whitespace form)))

           (and (contains? rules :clojure/string-starts-with)
                (= '.startsWith (first form)))
           (do (swap! applied conj :clojure/string-starts-with)
               (nav/replace location (rewrite-string-starts-with form)))

           (and (contains? rules :clojure/character-array-string)
                (= 'String. (first form))
                (= 2 (count form)))
           (do (swap! applied conj :clojure/character-array-string)
               (nav/replace location (rewrite-character-array-string form)))

           (and (contains? rules :foundation/prose-join-lines-native)
                (= 'prose/join-lines (first form))
                (= 2 (count form)))
           (do (swap! applied conj :foundation/prose-join-lines-native)
               (nav/replace location (rewrite-join-lines form)))

           (and (contains? rules :foundation/prose-pipe-native)
                (= 'prose/| (first form)))
           (do (swap! applied conj :foundation/prose-pipe-native)
               (nav/replace location (rewrite-prose-pipe form)))

           (and (contains? rules :clojure/string-join-native)
                (= '->> (first form))
                (seq? (last form))
                (= 'clojure.string/join (first (last form)))
                (= 2 (count (last form))))
           (do (swap! applied conj :clojure/string-join-native)
               (nav/replace location (rewrite-thread-string-join form)))

           (and (contains? rules :clojure/string-join-native)
                (= 'clojure.string/join (first form))
                (= 3 (count form)))
           (do (swap! applied conj :clojure/string-join-native)
               (nav/replace location (rewrite-string-join form)))

           (and (contains? rules :clojure/safe-sequential-get)
                (= 'get (first form))
                (= 3 (count form))
                (contains? #{'delimiters 'lines} (second form)))
           (do (swap! applied conj :clojure/safe-sequential-get)
               (nav/replace location (rewrite-safe-sequential-get form)))

           (and (contains? rules :foundation/map-vals-native)
                (or (and (= 'collection/map-vals (first form))
                         (= 3 (count form)))
                    (and (= '->> (first form))
                         (seq? (last form))
                         (= 'collection/map-vals (first (last form)))
                         (= 2 (count (last form))))))
           (do (swap! applied conj :foundation/map-vals-native)
               (nav/replace location (rewrite-map-vals form)))

           (and (contains? rules :foundation/heal-map-entry-sort-native)
                (= '(sort lu) form))
           (do (swap! applied conj :foundation/heal-map-entry-sort-native)
               (nav/replace location '(sort-by first (seq lu))))

           (and (contains? rules :foundation/prose-spaces-native)
                (= 'prose/spaces (first form))
                (= 2 (count form)))
           (do (swap! applied conj :foundation/prose-spaces-native)
               (nav/replace location
                            (list 'apply 'str
                                  (list 'repeat (second form) " "))))

           (and (contains? rules :clojure/some-native)
                (= 'some (first form)) (= 3 (count form)))
           (do (swap! applied conj :clojure/some-native)
               (nav/replace location (rewrite-some form)))

           (and (contains? rules :foundation/map-juxt-native)
                (= 'collection/map-juxt (first form)) (= 3 (count form)))
           (do (swap! applied conj :foundation/map-juxt-native)
               (nav/replace location (rewrite-map-juxt form)))

           (and (contains? rules :foundation/test-file-slurp-native)
                (= 'slurp (first form)) (= 2 (count form)))
           (do (swap! applied conj :foundation/test-file-slurp-native)
               (nav/replace location (rewrite-file-slurp form)))

           (and (contains? rules :foundation/test-file-spit-native)
                (= 'spit (first form)) (= 3 (count form)))
           (do (swap! applied conj :foundation/test-file-spit-native)
               (nav/replace location (rewrite-file-spit form)))

           (and (contains? rules :clojure/java-temp-file-native)
                (= 'java.io.File/createTempFile (first form))
                (= 3 (count form)))
           (do (swap! applied conj :clojure/java-temp-file-native)
               (nav/replace location (rewrite-java-temp-file form)))

           (and (contains? rules :foundation/heal-line-format-native)
                (= 'format (first form)) (= "%4d " (second form)))
           (do (swap! applied conj :foundation/heal-line-format-native)
               (nav/replace location (rewrite-heal-line-format form)))

           (and (contains? rules :foundation/rename-keys-native)
                (= 'collection/rename-keys (first form)) (= 3 (count form)))
           (do (swap! applied conj :foundation/rename-keys-native)
               (nav/replace location (rewrite-rename-keys form)))

           (and (contains? rules :clojure/nested-map-parameter-destructuring)
                (= 'defn (first form))
                (= 'pair-delimiters (second form)))
           (do (swap! applied conj :clojure/nested-map-parameter-destructuring)
               (nav/replace location (rewrite-pair-delimiter-copies form)))

           (and (contains? rules :foundation/string-trim-right-native)
                (contains? #{'clojure.string/trimr
                             'std.string.common/trimr
                             'std.foundation.string/trimr}
                           (first form)))
           (do (swap! applied conj :foundation/string-trim-right-native)
               (nav/replace location
                            (list 'std.foundation.string/trim-right (second form))))

           (and (contains? rules :clojure/character-array-native)
                (= 'char-array (first form)))
           (do (swap! applied conj :clojure/character-array-native)
               (nav/replace location
                            (list 'apply 'Arr/new
                                  (list 'repeat (second form) (nth form 2)))))

           (and (contains? rules :clojure/array-set-native)
                (= 'aset (first form)))
           (do (swap! applied conj :clojure/array-set-native)
               (nav/replace location (cons 'Arr/set (rest form))))

           (and (contains? rules :clojure/iobj-metadata-native)
                (= 'instance? (first form))
                (= 'clojure.lang.IObj (second form))
                (= 3 (count form)))
           (do (swap! applied conj :clojure/iobj-metadata-native)
               (nav/replace location (rewrite-iobj-metadata form)))

           (and (contains? rules :clojure/iobj-native)
                (= 'instance? (first form))
                (= 'clojure.lang.IObj (second form))
                (= 3 (count form)))
           (do (swap! applied conj :clojure/iobj-native)
               (nav/replace location (rewrite-iobj-instance form)))

           (and (contains? rules :clojure/exception-constructor-native)
                (= 'Exception. (first form))
                (= 2 (count form)))
           (do (swap! applied conj :clojure/exception-constructor-native)
               (nav/replace location (rewrite-exception-constructor form)))

           (and (contains? rules :clojure/integer-parse-native)
                (= 'Integer/parseInt (first form))
                (= 2 (count form)))
           (do (swap! applied conj :clojure/integer-parse-native)
               (nav/replace location (rewrite-integer-parse form)))

           (and (contains? rules :clojure/number-format-catch-native)
                (= 'catch (first form))
                (= 'java.lang.NumberFormatException (second form)))
           (do (swap! applied conj :clojure/number-format-catch-native)
               (nav/replace location (rewrite-number-format-catch form)))

           (and (contains? rules :clojure/iterator-first-pipeline)
                (iterator-first-pipeline? form))
           (do (swap! applied conj :clojure/iterator-first-pipeline)
               (nav/replace location
                            (rewrite-iterator-first-pipeline form)))

           (and (contains? rules :clojure/thread-zero-arity-step)
                (#{'-> '->>} (first form))
                (some #(and (seq? %) (= 1 (count %)))
                      (drop 2 form)))
           (do (swap! applied conj :clojure/thread-zero-arity-step)
               (nav/replace location
                            (rewrite-zero-arity-thread-steps form)))

           (not= original form)
           (nav/replace location form)

             :else
             location)))))))

(defn rewrite-symbol
  "returns a replacement symbol and its rule id when a route applies"
  {:added "4.1"}
  [routes value]
  (if (symbol? value)
    (let [value-ns   (namespace value)
          value-name (name value)]
      (cond (contains? routes value)
            {:value (:target (get routes value))
             :rule/id (:rule/id (get routes value))}

            (and value-ns
                 (contains? routes (symbol value-ns)))
            (let [route (get routes (symbol value-ns))]
              {:value (symbol (str (:target route)) value-name)
               :rule/id (:rule/id route)})

            :else
            {:value value}))
    {:value value}))

(defn rewrite-anonymous-function
  "converts a reader function value into a deterministic native fn form"
  {:added "4.1"}
  [form]
  (let [state (reduce (fn [{:keys [renames parameters index rest?] :as state}
                           parameter]
                        (cond
                          (= '& parameter)
                          (assoc state
                                 :parameters (conj parameters parameter)
                                 :rest? true)

                          :else
                          (let [replacement (if rest?
                                              'migration-arguments
                                              (symbol
                                               (str "migration-argument-"
                                                    index)))]
                            {:renames (assoc renames parameter replacement)
                             :parameters (conj parameters replacement)
                             :index (if rest? index (inc index))
                             :rest? rest?})))
                      {:renames {}
                       :parameters []
                       :index 0
                       :rest? false}
                      (second form))
        rewrite (fn [value]
                  (if (and (symbol? value)
                           (contains? (:renames state) value))
                    (get (:renames state) value)
                    value))]
    (apply list
           'fn
           (:parameters state)
           (map #(walk/postwalk rewrite %) (nnext form)))))

(defn rewrite-dependencies
  "rewrites exact and qualified dependency symbols through code.query"
  {:added "4.1"}
  [root migration-catalog target applied]
  (let [routes (dependency-routes migration-catalog target)
        reader-route (get routes 'fn*)
        root (if reader-route
               (query/modify
                root
                [seq?]
                (fn [location]
                  (let [form (nav/value location)]
                    (if (= 'fn* (first form))
                      (do (swap! applied conj (:rule/id reader-route))
                          (nav/replace location
                                       (rewrite-anonymous-function form)))
                      location))))
               root)]
    (query/modify
     root
     [symbol?]
     (fn [location]
       (let [before (nav/value location)
             {:keys [value rule/id]} (rewrite-symbol routes before)]
         (if (= before value)
           location
           (do (swap! applied conj id)
               (nav/replace location value))))))))

(defn rewrite-refer-metadata
  "quotes symbolic :refer values required by native Hara test metadata"
  {:added "4.1"}
  [root applied]
  (loop [cursor root]
    (if-let [refer-location (nav/find-next-token cursor :refer)]
      (let [value-location (nav/right-token refer-location)
            value          (some-> value-location nav/value)
            metadata?      (= :map (some-> refer-location nav/up nav/tag))]
        (if (and metadata? (symbol? value))
          (let [updated (nav/replace value-location (list 'quote value))]
            (swap! applied conj :clojure/quoted-refer-metadata)
            (recur updated))
          (recur refer-location)))
      (-> cursor nav/root-string nav/parse-root))))

(def +native-class-names+
  #{"String" "Stream" "Edn" "Json" "Crypto" "Process" "Arr" "File" "Printer" "OS" "Algo"})

(defn native-class-symbol?
  "checks for a qualified symbol owned by a known std.native class"
  {:added "4.1"}
  [value]
  (and (symbol? value)
       (contains? +native-class-names+ (namespace value))))

(defn host-symbol?
  "checks for a JVM interop symbol requiring an explicit adaptation"
  {:added "4.1"}
  [value]
  (boolean
   (and (symbol? value)
        (not (native-class-symbol? value))
        (not (contains? #{"." ".." "..."} (name value)))
        (or (str/starts-with? (name value) ".")
            (re-find #"^[A-Z][A-Za-z0-9.]*[/.]" (str value))))))

(defn diagnostics
  "returns deterministic unresolved host-interop diagnostics"
  {:added "4.1"}
  [root]
  (->> (query/select root [symbol?])
       (map nav/value)
       (filter host-symbol?)
       distinct
       (sort-by str)
       (mapv (fn [value]
               {:type :migration/host-interop
                :symbol value
                :safety :manual}))))

(def +source-definition-heads+
  '#{def defn defn- defmacro defonce defrecord defstruct declare defmethod})

(defn source-form-identity
  "returns a stable identity for aligning original and rewritten top-level forms"
  {:added "4.1"}
  [form index]
  (cond
    (and (seq? form) (#{'ns 'ns+} (first form)))
    [:namespace (second form)]

    (and (seq? form)
         (contains? +source-definition-heads+ (first form))
         (symbol? (second form)))
    [:definition (second form)]

    :else
    [:position index]))

(defn source-form-records
  "indexes top-level source forms without discarding surrounding source blocks"
  {:added "4.1"}
  [source]
  (loop [children (vec (block/children (block/parse-root source)))
         counts {}
         index 0
         output []]
    (if (empty? children)
      output
      (let [child (first children)]
        (if (block/expression? child)
          (let [form       (block/value child)
                base       (source-form-identity form index)
                occurrence (get counts base 0)]
            (recur (vec (rest children))
                   (assoc counts base (inc occurrence))
                   (inc index)
                   (conj output {:key [base occurrence]
                                 :form form
                                 :string (block/string child)
                                 :multiline (pos? (block/height child))})))
          (recur (vec (rest children)) counts index output))))))

(defn layout-source-form
  "lays out one rewritten source form at the migration readability width"
  {:added "4.1"}
  [form multiline]
  (binding [estimate/*readable-len* 80]
    (try
      (block/parse-first
       (block/string
        (block/layout
         (if multiline
           (with-meta form (assoc (meta form) :readable-len 1))
           form))))
      (catch Exception error
        (block/parse-first (pr-str form))))))

(defn layout-rewritten-source
  "lays out rewritten top-level forms while retaining untouched source blocks"
  {:added "4.1"}
  [input output]
  (let [originals (into {}
                        (map (juxt :key identity)
                             (source-form-records input)))]
    (loop [children (vec (block/children (block/parse-root output)))
           counts {}
           index 0
           rendered []]
      (if (empty? children)
        (apply str rendered)
        (let [child (first children)]
          (if (block/expression? child)
            (let [form       (block/value child)
                  base       (source-form-identity form index)
                  occurrence (get counts base 0)
                  key        [base occurrence]
                  original   (get originals key)
                  text       (cond
                               (= (:form original ::missing) form)
                               (:string original)

                               :else
                               (str/replace
                                (block/string
                                 (layout-source-form form
                                                     (:multiline original)))
                                #"(?m)^[ \t]+$"
                                ""))]
              (recur (vec (rest children))
                     (assoc counts base (inc occurrence))
                     (inc index)
                     (conj rendered text)))
            (recur (vec (rest children))
                   counts
                   index
                   (conj rendered (block/string child)))))))))

(defn clean-blank-lines
  "removes indentation from otherwise empty generated source lines"
  {:added "4.1"}
  [source]
  (str/replace source #"(?m)^[ \t]+$" ""))

(defn migration-source-input
  "selects a reviewed native template for semantic, non-mechanical targets"
  {:added "4.1"}
  [source target applied]
  (if-let [template (when (= :source (:unit/kind target))
                      (:target/source-template target))]
    (do (swap! applied conj :foundation/reviewed-native-template)
        (slurp template))
    source))

(defn migrate-source
  "migrates one source string and returns reproducible evidence"
  {:added "4.1"}
  ([source migration-catalog]
   (migrate-source source migration-catalog nil))
  ([source migration-catalog target]
   (let [applied           (atom [])
        working-source    (migration-source-input source target applied)
        initial           (nav/parse-root working-source)
        override-result   (if target
                            (rewrite-test-override-symbols initial target applied)
                            initial)
        arity-result      (if target
                            (rewrite-single-arity-defns override-result
                                                        target
                                                        applied)
                            override-result)
        target-result     (if target
                            (rewrite-target-forms arity-result target applied)
                            initial)
        structural-result (if (and target
                                   (contains? (set (:target/rules target))
                                              :foundation/block-interfaces-to-protocol))
                            (remove-block-interface-remnants target-result)
                            target-result)
        dependency-result (rewrite-dependencies structural-result
                                                migration-catalog
                                                target
                                                applied)
        require-result    (if target
                            (rewrite-unused-requires dependency-result
                                                     target
                                                     applied)
                            dependency-result)
        final             (rewrite-refer-metadata require-result applied)
        raw-output        (nav/root-string final)
        output            (if (= :source (:unit/kind target))
                            (clean-blank-lines
                             (layout-rewritten-source working-source raw-output))
                            raw-output)]
     {:input source
      :source/checksum (sha256 source)
      :output/checksum (sha256 output)
      :output output
      :applied (vec (distinct @applied))
      :diagnostics (diagnostics final)
      :changed (not= source output)})))

(defn target-for-unit
  "returns the catalog target owning a normalized migration unit"
  {:added "4.1"}
  [unit migration-catalog]
  (let [path-key (if (= :test (:unit/kind unit))
                   :target/test-path
                   :target/source-path)]
    (when-let [target (first (filter #(= (:source/path unit) (get % path-key))
                                     (:migration/targets migration-catalog)))]
      (let [pathway (:unit/kind unit)
            rule-key (if (= :test pathway)
                       :target/test-rules
                       :target/source-rules)]
        (assoc target
               :unit/kind pathway
               :target/rules (get target rule-key []))))))

(defn migrate-unit
  "migrates a normalized source or test unit"
  {:added "4.1"}
  [unit migration-catalog]
  (let [target (target-for-unit unit migration-catalog)]
    (merge (select-keys unit
                        [:unit/kind :source/path :target/path])
           (migrate-source (:source/string unit)
                           migration-catalog
                           target))))
