(ns code.migrate.test
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [code.framework.test.fact :as fact]
            [code.migrate.engine :as engine]
            [code.query :as query]
            [code.test.compile :as compile]
            [std.block :as block]
            [std.block.layout.estimate :as estimate]
            [std.block.navigate :as nav]))

(defn test-ns-form
  "returns the namespace form from a Foundation test source"
  {:added "4.1"}
  [source]
  (let [root (nav/parse-root source)
        location (query/$* root '[(ns | _ & _)]
                            {:return :zipper :first true})]
    (some-> location nav/up nav/value)))

(defn code-test-entry?
  [entry]
  (or (contains? '#{code.test tahto.core} entry)
      (and (vector? entry)
           (contains? '#{code.test tahto.core} (first entry)))))

(defn native-test-ns
  "removes the Foundation runner and adds native checker dependencies"
  {:added "4.1"}
  [form]
  (let [[head name & clauses] form
        clauses (keep (fn [clause]
                        (if (and (seq? clause)
                                 (#{:use :require} (first clause)))
                          (let [entries (remove code-test-entry? (rest clause))]
                            (when (seq entries)
                              (cons (first clause) entries)))
                          clause))
                      clauses)
        required '([std.codec.base64 :as base64]
                   [code.test.base.process :as process]
                   [code.test.checker.common :as checker]
                   [code.test.checker.collection :as collection]
                   [code.test.checker.logic :as logic])
        require-clause (first (filter #(and (seq? %)
                                            (= :require (first %)))
                                      clauses))
        clauses (remove #(= % require-clause) clauses)
        require-clause (apply list :require
                              (concat (rest require-clause) required))]
    (apply list head name (cons require-clause clauses))))

(defn gather-test-fact
  "retains ordinary facts even when they have no refer metadata"
  {:added "4.1"}
  [navigation]
  (let [gathered (fact/gather-fact navigation)
        form (read-string (nav/string navigation))]
    (if gathered
      (assoc gathered :sexp form)
      (when (and (seq? form) (= 'fact (first form)))
        {:sexp form
         :intro (if (string? (second form)) (second form) "")}))))

(defn fact-records
  "compiles each Foundation fact while retaining its title and metadata"
  {:added "4.1"}
  [source]
  (let [root (nav/parse-root source)]
    (->> (fact/top-level-fact-navs root)
         (keep gather-test-fact)
         (mapv (fn [{:keys [sexp intro] :as gathered}]
                 (let [body (rest sexp)
                       body (if (string? (first body)) (rest body) body)
                       body (mapv (fn [form]
                                    (if (= '= form) '=> form))
                                  body)]
                   {:title intro
                    :meta (select-keys gathered [:refer :added :id :class :setup])
                   :operations (vec (compile/rewrite-top-level body))}))))))

(defn test-support-forms
  "retains top-level fixture and setup forms outside Foundation facts"
  {:added "4.1"}
  [source]
  (->> (block/children (block/parse-root source))
       (keep (fn [child]
               (when (block/expression? child)
                 (block/value child))))
       (remove (fn [form]
                 (and (seq? form)
                      (contains? '#{ns fact comment} (first form)))))
       vec))

(defn form-aliases-used
  "collects namespace aliases from forms which will actually be emitted"
  {:added "4.1"}
  [forms]
  (let [used (atom #{})]
    (engine/postwalk-code
     (fn [value]
       (when (and (symbol? value) (namespace value))
         (swap! used conj (symbol (namespace value))))
       value)
     forms)
    @used))

(defn runtime-require-entry
  "returns one quoted vector dependency from a historical setup require"
  {:added "4.1"}
  [form]
  (when (and (seq? form)
             (= 'require (first form))
             (= 2 (count form))
             (seq? (second form))
             (= 'quote (first (second form)))
             (vector? (second (second form))))
    (second (second form))))

(defn add-test-requires
  "adds lifted historical setup dependencies to a native test namespace"
  {:added "4.1"}
  [form entries]
  (let [[head name & clauses] form
        require-clause (first (filter #(and (seq? %)
                                            (= :require (first %)))
                                      clauses))
        other-clauses (remove #(= % require-clause) clauses)
        merged (apply list :require
                      (distinct (concat (rest require-clause) entries)))]
    (apply list head name (cons merged other-clauses))))

(defn normalize-root-form
  "normalizes portable scalar forms before source-tree migration"
  {:added "4.1"}
  [form target]
  (let [rules (set (:target/rules target))]
    (cond
      (and (contains? rules :clojure/character-literal) (char? form))
      (engine/rewrite-character-literal form)

      (and (contains? rules :clojure/nil-set)
           (set? form)
           (contains? form nil))
      (engine/rewrite-nil-set form)

      (and (contains? rules :clojure/ratio-literal) (ratio? form))
      (engine/rewrite-ratio-literal form)

      :else form)))

(defn quoted-value-form
  "returns a constructor expression that preserves nested reader metadata"
  {:added "4.1"}
  [value]
  (let [base (cond
               (list? value)
               (apply list 'list (map quoted-value-form value))

               (vector? value)
               (apply list 'vector (map quoted-value-form value))

               (set? value)
               (list 'set (apply list 'vector (map quoted-value-form value)))

               (map? value)
               (apply list 'Algo/ordered-map
                      (mapcat (fn [entry]
                                [(quoted-value-form (key entry))
                                 (quoted-value-form (val entry))])
                              value))

               :else
               (let [value (if (instance? clojure.lang.IObj value)
                             (with-meta value nil)
                             value)]
                 (if (symbol? value)
                   (list 'quote value)
                   value)))]
    (if (seq (meta value))
      (list 'with-meta base (meta value))
      base)))

(defn query-marker-metadata?
  "checks for compact code.query reader metadata such as :+%?"
  {:added "4.1"}
  [metadata]
  (boolean
   (some (fn [key]
           (and (keyword? key)
                (not (empty? (name key)))
                (every? (set "%?&-+") (name key))))
         (keys metadata))))

(defn materialize-literal-metadata
  "preserves query reader metadata on evaluated collection literals"
  {:added "4.1"}
  [form]
  (walk/postwalk
   (fn [node]
     (if (and (coll? node)
              (query-marker-metadata? (meta node)))
       (let [metadata (meta node)
             base (cond
                    (vector? node)
                    (apply list 'vector node)

                    (set? node)
                    (list 'set (vec node))

                    (map? node)
                    (apply list 'hash-map
                           (mapcat (fn [entry]
                                     [(key entry) (val entry)])
                                   node))

                    :else node)]
         (if (identical? base node)
           node
           (list 'with-meta base metadata)))
       node))
   form))

(defn materialize-quoted-metadata
  "preserves nested reader metadata carried inside quote forms"
  {:added "4.1"}
  [form]
  (let [form (walk/postwalk
              (fn [node]
                (if (and (coll? node)
                         (empty? node)
                         (query-marker-metadata? (meta node)))
                  (quoted-value-form node)
                  node))
              form)]
    (materialize-literal-metadata
     (walk/postwalk
      (fn [node]
        (if (and (seq? node)
                 (= 'quote (first node))
                 (= 2 (count node))
                 (some #(seq (meta %))
                       (tree-seq coll? seq (second node))))
          (quoted-value-form (second node))
          node))
      form))))

(defn materialize-quoted-collections
  "constructs quoted collections so native maps retain Foundation reader order"
  {:added "4.1"}
  [form]
  (walk/postwalk
   (fn [node]
     (if (and (seq? node)
              (= 'quote (first node))
              (= 2 (count node))
              (coll? (second node)))
       (quoted-value-form (second node))
       node))
   form))

(defn rewrite-block-test-compat
  ([form]
   (rewrite-block-test-compat form nil))
  ([form target]
   (let [navigate? (= :migration/std-block-navigate (:target/id target))
         form (if navigate?
                (walk/postwalk
                 (fn [node]
                   (cond
                     (and (seq? node)
                          (= 'quote (first node))
                          (seq? (second node))
                          (#{'clojure.core/defn 'std.foundation/defn}
                           (first (second node))))
                     (list
                      'quote
                      (walk/postwalk
                       (fn [value]
                         (cond
                           (= 'clojure.core/defn value)
                           'std.foundation/defn

                           (= 'clojure.core/if-let value)
                           'std.foundation/if-let

                           (= :right value)
                           '(std.foundation/keyword "right")

                           (= 'std.lib.zip/get value)
                           'zip/get

                           :else value))
                       (second node)))

                     (and (seq? node)
                          (= 'str (first node))
                          (= 2 (count node)))
                     (list 'display-navigator (second node))

                     (and (seq? node)
                          (= '-> (first node))
                          (or (= 'str (last node))
                              (= '(str) (last node))))
                     (concat (butlast node) (list 'display-navigator))

                     :else node))
                 form)
                form)]
   (walk/postwalk
    (fn [node]
      (cond
       (and target
            (seq? node)
            (= 'quote (first node))
            (seq? (second node))
            (= 'quote (first (second node)))
            (symbol? (second (second node)))
            (= (str (:target/test-namespace target))
               (namespace (second (second node)))))
       (list 'quote
             (list 'syntax-quote
                   (symbol (name (second (second node))))))

       (and (seq? node)
            (= 'slurp (first node))
            (= 2 (count node))
            (string? (second node)))
       (let [content (slurp (second node))
             encoded (.encodeToString (java.util.Base64/getEncoder)
                                      (.getBytes content "UTF-8"))]
         (list 'str/decode-utf8 (list 'base64/decode encoded)))

       (= :long node)
       :number

       (= :collection node)
       :container

       (and target
            (keyword? node)
            (nil? (namespace node))
            (str/starts-with? (name node) ":"))
       (keyword (str (:target/test-namespace target))
                (subs (name node) 1))

       (and (seq? node)
            (= 'construct/token (first node))
            (= 2 (count node))
            (ratio? (second node)))
       (let [value (second node)]
         (list 'construct/token-from-string
               (str value)
               (list '/ (double (numerator value))
                     (double (denominator value)))))

       (and (seq? node)
            (= '->> (first node))
            (= 3 (count node))
            (seq? (last node))
            (= '(apply str) (last node)))
       (list 'apply 'str
             (list 'map 'std.block.base/block-representation (second node)))

       (and (seq? node)
            (= '->> (first node))
            (seq? (last node))
            (= '(map str) (last node)))
       (concat (butlast node)
               (list '(map std.block.base/block-representation)))

       (and (seq? node)
            (= '->> (first node))
            (= 3 (count node))
            (= 'str (last node)))
       (list 'std.block.base/block-representation (second node))

       (and (seq? node)
            (= '-> (first node))
            (= '(str) (last node)))
       (concat (butlast node)
               (list '(std.block.base/block-representation)))

       (and (seq? node)
            (= '-> (first node))
            (= 'str (last node)))
       (concat (butlast node)
               (list 'std.block.base/block-representation))

       (and (seq? node)
            (= 'mapv (first node))
            (= 'str (second node))
            (= 3 (count node)))
       (list 'mapv 'std.block.base/block-representation (nth node 2))

       (and (seq? node)
            (= 'map (first node))
            (= 'str (second node))
            (= 3 (count node)))
       (list 'map 'std.block.base/block-representation (nth node 2))

       (and (seq? node)
            (= 'catch (first node))
            (= 'clojure.lang.ExceptionInfo (second node)))
       (cons 'catch (cons 'Throwable (nnext node)))

       (and (seq? node)
            (= 'apply (first node))
            (= 'str (second node))
            (= 3 (count node)))
       (list 'apply 'str
             (list 'map 'std.block.base/block-representation (nth node 2)))

       (and (seq? node)
            (= 'str (first node))
            (= 2 (count node)))
       (list 'std.block.base/block-representation (second node))

       :else node))
    form))))

(defn migrate-form
  [form migration-catalog target]
  (let [rules (set (:target/rules target))]
    (-> (cond-> form
          (contains? rules :foundation/block-test-compat)
          (rewrite-block-test-compat target)

          (contains? rules :clojure/quoted-form-metadata)
          materialize-quoted-metadata

          (contains? rules :clojure/quoted-collection-order-native)
          materialize-quoted-collections)
      (normalize-root-form target)
      pr-str
      (engine/migrate-source migration-catalog target)
      :output
      read-string)))

(defn checker-form
  "maps Foundation matcher shorthand to native checker constructors"
  {:added "4.1"}
  [expected]
  (walk/postwalk
   (fn [node]
     (cond
       (= 'var? node)
       '(fn [actual] (= :std.native.Var (type actual)))

       (and (seq? node) (= 'contains (first node)))
       (cons 'collection/contains (rest node))

       (and (seq? node) (= 'contains-in (first node)))
       (cons 'collection/contains-in (rest node))

       (and (seq? node) (= 'throws (first node)))
       (cons 'checker/throws (rest node))

       (and (seq? node) (= 'all (first node)))
       (cons 'logic/all (rest node))

       :else node))
   expected))

(defn print-meta-operation?
  "recognizes historical assertions which enabled Clojure metadata printing"
  [form]
  (and (seq? form)
       (= 'binding (first form))
       (= '[*print-meta* true] (second form))))

(defn construct-rep-operation?
  "recognizes the historical pr-str/read-string representation boundary"
  [form]
  (and (seq? form)
       (= 'construct/rep (first form))))

(defn compile-test-record
  "migrates one Foundation fact without losing its function boundary"
  {:added "4.1"}
  [{:keys [title meta operations]} migration-catalog target]
  (let [meta (cond-> meta
               (:refer meta)
               (update :refer #(migrate-form % migration-catalog target)))
        case-count (count (filter #(= :test-equal (:type %)) operations))
        state
        (reduce
         (fn [state operation]
           (case (:type operation)
             :form
             (if (= '=> (:form operation))
               state
               (update state :setup conj
                       (migrate-form (:form operation)
                                     migration-catalog target)))

             :test-equal
             (let [index    (inc (count (:cases state)))
                   print-meta? (print-meta-operation?
                                (get-in operation [:input :form]))
                   construct-rep? (construct-rep-operation?
                                   (get-in operation [:input :form]))
                   actual   (migrate-form (get-in operation [:input :form])
                                          migration-catalog target)
                   expected (migrate-form (get-in operation [:output :form])
                                          migration-catalog target)
                   actual (if construct-rep? (list 'pr-str actual) actual)
                   expected (if construct-rep? (list 'pr-str expected) expected)
                   expected (if print-meta?
                              (list 'migration-meta-tree
                                    (list 'read-string expected))
                              expected)]
               (update state :cases conj
                       {:name (cond
                                (str/blank? title) (str "case " index)
                                (= 1 case-count) title
                                :else (str title " #" index))
                        :test (list 'fn [] actual)
                        :expected (checker-form expected)}))

             (update state :diagnostics conj
                     {:type :migration/unsupported-test-operation
                      :operation (:type operation)})))
         {:setup (mapv #(migrate-form % migration-catalog target)
                       (:setup meta))
          :cases [] :diagnostics []}
         operations)]
    (assoc state :meta meta)))

(defn test-record-form
  "returns one native Test/run form and its owning setup"
  {:added "4.1"}
  [{:keys [setup cases]}]
  (let [run-form (list 'Test/run cases 'migration-test-check)]
    (if (seq setup)
      (apply list 'do (concat setup [run-form]))
      run-form)))

(defn layout-test-form
  "lays out one generated test form at the migration readability width"
  {:added "4.1"}
  [form]
  (binding [estimate/*readable-len* 80]
    (block/layout form)))

(defn test-form-string
  "uses readable layout only when it preserves the emitted form exactly"
  {:added "4.1"}
  [form]
  (let [render (fn [width]
                 (try
                   (binding [estimate/*readable-len* width]
                     (block/string (block/layout form)))
                   (catch Exception _
                     (pr-str form))))
        rendered (render 80)]
    (if (try
          (= form (read-string rendered))
          (catch Throwable _ false))
      rendered
      (pr-str form))))

(defn test-metadata-string
  "formats deterministic refer-first reader metadata"
  {:added "4.1"}
  [metadata]
  (let [entries (keep (fn [key]
                        (when (contains? metadata key)
                          [key (get metadata key)]))
                      [:refer :added :id :class])
        strings (mapv (fn [[key value]]
                        (str (pr-str key)
                             " "
                             (block/string (layout-test-form value))))
                      entries)
        single (str "^{" (str/join " " strings) "}")]
    (if (<= (count single) 80)
      single
      (str "^{" (first strings)
           "\n  " (str/join "\n  " (rest strings)) "}"))))

(defn test-record-string
  "formats one metadata-delimited native test block"
  {:added "4.1"}
  [record]
  (str (test-metadata-string (:meta record))
       "\n"
       (test-form-string (test-record-form record))))

(defn emit-test-run
  "emits setup forms and executable Test/run cases from compiled facts"
  {:added "4.1"}
  [source migration-catalog target]
  (let [operation-target (assoc target
                                :unit/kind :test
                                :target/rules
                                (vec (distinct
                                      (concat (:target/source-rules target)
                                              (:target/test-rules target)))))
        namespace-target (update operation-target
                                 :target/rules
                                 #(vec (remove #{:clojure/prune-unused-requires
                                                 :clojure/test-prune-unused-requires}
                                               %)))
        migrated-ns (migrate-form (test-ns-form source)
                                  migration-catalog namespace-target)
        support (mapv #(migrate-form % migration-catalog operation-target)
                      (test-support-forms source))
        records (mapv #(compile-test-record % migration-catalog operation-target)
                      (fact-records source))
        runtime-requires (vec (keep runtime-require-entry
                                    (mapcat :setup records)))
        records (mapv #(update % :setup
                               (fn [forms]
                                 (vec (remove runtime-require-entry forms))))
                      records)
        state {:setup (vec (mapcat :setup records))
               :cases (vec (mapcat :cases records))
               :diagnostics (vec (mapcat :diagnostics records))}
        used-aliases (form-aliases-used
                      (concat support
                              (mapcat (fn [record]
                                        (concat (:setup record)
                                                (map :test (:cases record))
                                                (map :expected (:cases record))))
                                      records)))
        migrated-ns (add-test-requires
                     migrated-ns
                     (concat runtime-requires
                             (:target/test-requires target)))
        migrated-ns (engine/prune-unused-requires-form migrated-ns
                                                       used-aliases)
        check-form '(defn migration-test-check
                      [test expected]
                      (let [result (select-keys (process/check test expected)
                                                [:pass :status :error])]
                        (if (not (:pass result))
                          (println :failure
                                   (assoc result
                                          :actual (try
                                                    (test)
                                                    (catch Throwable error
                                                      {:exception (str error)}))
                                          :expected expected)))
                        result))
        meta-tree-form
        '(defn migration-meta-tree
           [value]
           (let [metadata (select-keys (or (meta value) {})
                                       [:readable-len :tag :spec])
                 normalized
                 (cond (vector? value)
                       (mapv migration-meta-tree value)
                       (map? value)
                       (reduce-kv
                        (fn [output key item]
                          (assoc output
                                 (migration-meta-tree key)
                                 (migration-meta-tree item)))
                        {}
                        value)
                       (set? value)
                       (set (map migration-meta-tree value))
                       (seq? value)
                       (apply list (map migration-meta-tree value))
                       :else value)]
             {:meta metadata :value normalized}))]
    (assoc state
           :support support
           :output (str (test-form-string (native-test-ns migrated-ns))
                        "\n\n"
                        (test-form-string check-form)
                        "\n\n"
                        (test-form-string meta-tree-form)
                        "\n\n"
                        (when (seq support)
                          (str (str/join "\n\n"
                                         (map test-form-string
                                              support))
                               "\n\n"))
                        (str/join "\n\n" (map test-record-string records))
                        "\n"))))

(defn migrate-unit
  "migrates a test unit into native Test/run source"
  {:added "4.1"}
  [unit migration-catalog]
  (when-not (= :test (:unit/kind unit))
    (throw (ex-info "Test migration requires a :test unit"
                    {:unit/kind (:unit/kind unit)})))
  (let [target (engine/target-for-unit unit migration-catalog)
        emitted (emit-test-run (:source/string unit)
                               migration-catalog target)
        output (:output emitted)]
    (merge (select-keys unit [:unit/kind :source/path :target/path])
           {:input (:source/string unit)
            :source/checksum (engine/sha256 (:source/string unit))
            :output output
            :output/checksum (engine/sha256 output)
            :applied [:foundation/code-test-to-native-test]
            :diagnostics (:diagnostics emitted)
            :operations (+ (count (:support emitted))
                           (count (:setup emitted))
                           (count (:cases emitted)))
            :assertions (count (:cases emitted))
            :changed (not= (:source/string unit) output)})))
