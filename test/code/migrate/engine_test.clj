(ns code.migrate.engine-test
  (:require [code.migrate.catalog :as catalog]
            [code.migrate.engine :refer :all]
            [code.migrate.test :as migrate.test]
            [std.block :as block]
            [std.block.navigate :as nav])
  (:use code.test))

(def +root+
  (or (System/getenv "HARA_WORKSPACE_ROOT")
      "../../workspace"))

(def +catalog+
  (catalog/load-catalog "resources/code/migrate/catalog.edn"))

(def +cases+
  (:cases
   (catalog/read-edn
    "resources/code/migrate/conformance/bootstrap-pairs.edn")))

^{:refer code.migrate.test/fact-records
  :id equals-expectation
  :added "4.1"}
(fact "preserves the alternate historical equals expectation marker"
  (-> (migrate.test/fact-records
       "(ns demo-test (:use code.test))\n^{:refer demo/x} (fact \"demo\" (+ 1 1) = 2)")
      first :operations first :type)
  => :test-equal)

^{:refer code.migrate.test/gather-test-fact :added "4.1"}
(fact "preserves historical facts without refer metadata"
  (-> (migrate.test/fact-records
       "(ns demo-test (:use code.test))\n(fact \"plain\" (fn? identity) => true)")
      first
      (select-keys [:title :meta]))
  => {:title "plain" :meta {}})

^{:refer code.migrate.test/fact-records :added "4.1" :id map-reader-order}
(fact "preserves Foundation reader order inside quoted fact maps"
  (-> (migrate.test/fact-records
       "(ns demo-test (:use code.test))\n(fact \"map\" (identity '{:server 1 :db 2}) => map?)")
      first :operations first (get-in [:input :form]) second second seq (->> (map key)))
  => '(:server :db))

^{:refer code.migrate.test/test-support-forms :added "4.1"}
(fact "preserves ordinary top-level test fixtures outside facts"
  (migrate.test/test-support-forms
   "(ns demo-test (:use code.test))\n(def fixture 1)\n(fact \"demo\" fixture => 1)")
  => '[(def fixture 1)])

^{:refer code.migrate.test/form-aliases-used :added "4.1"}
(fact "prunes aliases used only by historical comments"
  (let [form '(ns demo
                (:require [std.lib.trace :as trace]
                          [std.lib.env :as env]
                          [work.flow.make.project :refer :all]))]
    (prune-unused-requires-form
     form
     (migrate.test/form-aliases-used '[(env/p "active")])))
  => '(ns demo
        (:require [std.lib.env :as env]
                  [work.flow.make.project :refer :all])))

^{:refer code.migrate.test/form-aliases-used :added "4.1" :id quoted-alias}
(fact "does not require aliases used only as quoted data"
  (migrate.test/form-aliases-used
   '[(quote (component/with [sys system] (start sys)))
     (env/p "active")])
  => '#{env})

^{:refer code.migrate.test/runtime-require-entry :added "4.1"}
(fact "lifts fact-local require aliases into the native test namespace"
  (let [entry (migrate.test/runtime-require-entry
               '(require '[work.flow.make.common :as common]))]
    [entry
     (migrate.test/add-test-requires
      '(ns demo (:require [work.flow.make.project :refer :all]))
      [entry])])
  => '[[work.flow.make.common :as common]
       (ns demo
         (:require [work.flow.make.project :refer :all]
                   [work.flow.make.common :as common]))])

^{:refer code.migrate.test/test-form-string :added "4.1"}
(fact "falls back from readable layout when wrapping changes a form"
  (let [form '(fn []
                (common/with:triggers
                 [+triggers+]
                 (common/triggers-purge)
                 (deref +triggers+)))
        rendered (migrate.test/test-form-string form)
        fallback (migrate.test/test-form-string
                  '(Test/run [{:test [:path]}] migration-test-check))]
    [(read-string rendered)
     (clojure.string/includes? rendered "+triggers+")
     fallback])
  => ['(fn []
         (common/with:triggers
          [+triggers+]
          (common/triggers-purge)
          (deref +triggers+)))
      true
      "(Test/run [{:test [:path]}] migration-test-check)"])

^{:refer code.migrate.test/test-form-string :added "4.1" :id escaped-string-fallback}
(fact "falls back when readable layout loses nested string escapes"
  (= '(f ["\"doc\""])
     (read-string (migrate.test/test-form-string '(f ["\"doc\""]))))
  => true)

^{:refer code.migrate.engine/rewrite-prose-pipe :added "4.1"}
(fact "rewrites variadic Foundation prose lines"
  (rewrite-prose-pipe '(prose/| "a" "b"))
  => '(std.foundation.string/join "\n" ["a" "b"]))

^{:refer code.migrate.engine/rewrite-java-temp-file :added "4.1"}
(fact "rewrites JVM temporary files to the native filesystem"
  (rewrite-java-temp-file
   '(java.io.File/createTempFile "test" ".clj"))
  => '(std.lib.fs/temp-file
       "/"
       {:prefix "test" :suffix ".clj"}))

^{:refer code.migrate.engine/rewrite-file-spit :added "4.1"}
(fact "rewrites text file writes to native filesystem effects"
  (rewrite-file-spit '(spit tmp content))
  => '(deref
       (File/write tmp
                   (std.foundation.string/encode-utf8 content)
                   {:mode :replace})))

^{:refer code.migrate.engine/rewrite-file-slurp :added "4.1"}
(fact "rewrites dynamic text file reads to native filesystem effects"
  (rewrite-file-slurp '(slurp tmp))
  => '(std.foundation.string/decode-utf8 (deref (File/read tmp))))

^{:refer code.migrate.engine/rewrite-ns-form :added "4.1"}
(fact "supports independently renamed source and test namespaces"
  (rewrite-ns-form
   '(ns code.project.common-test
      (:require [code.project.common :refer :all]))
   []
   'tool.project.common-test)
  => '(ns tool.project.common-test
        (:require [code.project.common :refer :all])))

^{:refer code.migrate.test/materialize-quoted-metadata :added "4.1"}
(fact "preserves query markers on quoted and evaluated collection literals"
  (let [literal (with-meta #{:a :b} {:& true})
        quoted  (list 'quote
                      (with-meta (list 'symbol '_) {:% true}))
        literal-form (migrate.test/materialize-quoted-metadata literal)
        quoted-form  (migrate.test/materialize-quoted-metadata quoted)]
    [(meta (eval literal-form))
     (eval literal-form)
     (meta (eval quoted-form))
     (eval quoted-form)])
  => [{:& true} #{:a :b} {:% true} '(symbol _)])

^{:refer code.migrate.engine/migrate-source :added "4.1"}
(fact "migrates the executable source and test corpus exactly"
  (mapv (fn [case]
          (:output (migrate-source (:case/input case) +catalog+)))
        +cases+)
  => (mapv :case/output +cases+)

  (mapv (fn [case]
          (-> case
              :case/input
              (migrate-source +catalog+)
              :output
              (migrate-source +catalog+)
              :changed))
        +cases+)
  => [false false])

^{:refer code.migrate.engine/rewrite-symbol :added "4.1"}
(fact "rewrites exact and qualified dependency symbols"
  (let [routes (dependency-routes +catalog+)]
    [(rewrite-symbol routes 'std.lib.foundation)
     (rewrite-symbol routes 'std.lib.foundation/inc)
     (rewrite-symbol routes 'fn*)
     (rewrite-symbol routes 'ifn?)
     (mapv (comp :value #(rewrite-symbol routes %))
           '[clojure.core/for
             clojure.core/doseq
             clojure.core/dotimes
             clojure.core/while])])
  => [{:value 'std.foundation
       :rule/id :foundation/std-lib-foundation}
      {:value 'std.foundation/inc
       :rule/id :foundation/std-lib-foundation}
      {:value 'fn
       :rule/id :clojure/anonymous-function-reader}
      {:value 'fn?
       :rule/id :clojure/callable-predicate}
      ['std.foundation/for
       'std.foundation/doseq
       'std.foundation/dotimes
       'std.foundation/while]]

  (rewrite-symbol (dependency-routes +catalog+) 'std.lib.zip.Zipper)
  => {:value 'std.lib.zip/Zipper
      :rule/id :foundation/zipper-class}

  (let [first-pass (:output
                    (migrate-source "#(+ %1 %2)" +catalog+))
        second-pass (:output
                     (migrate-source "#(+ %1 %2)" +catalog+))]
    [first-pass second-pass])
  => ["(fn [migration-argument-0 migration-argument-1] (+ migration-argument-0 migration-argument-1))"
      "(fn [migration-argument-0 migration-argument-1] (+ migration-argument-0 migration-argument-1))"])

^{:refer code.migrate.engine/rewrite-target-forms :added "4.1"}
(fact "applies only structural adaptations enabled by the target"
  (let [source "(ns std.lib.zip (:refer-clojure :exclude [find get]))\n(defrecord Zipper [context prefix display] Object (toString [obj] (str obj)))\n(defmethod print-method Zipper ([v w] (.write w (str v))))\n(def value h/NIL)"
        target (catalog/target-by-id +catalog+ :migration/std-lib-zip)
        result (migrate-source source +catalog+ target)]
    [(:output result) (:diagnostics result)])
  => ["(ns std.lib.zip (:config {:override [get find prewalk postwalk]}))\n(defstruct Zipper\n  [context prefix display parent left right depth changed? tag position])\n(def value nil)"
      []]

  (read-string
   (:output
    (migrate-source
     "(format \"<%d,%d> %s\" row col (apply str (zip/status nav)))"
     +catalog+
     (catalog/target-by-id +catalog+ :migration/std-block-navigate))))
  => '(str "<" row "," col "> "
           (apply str
                  (map std.block.base/block-representation
                       (zip/status nav))))

  (rewrite-zip-cursor-comparator
   '(defonce +base+ {:cursor (quote |)}))
  => '(defonce +base+ {:cursor (quote |) :cursor-equal? =})

  (rewrite-zip-cursor-comparator
   '(defn from-status [data]
      (zero? (compare (:cursor context) data))))
  => '(defn from-status [data]
        ((:cursor-equal? context) (:cursor context) data))

  (rewrite-block-cursor-comparator
   '(defn navigator [block]
      (merge zip/+base+ {:cursor (construct/cursor)})))
  => '(defn navigator [block]
        (merge zip/+base+
               {:cursor (construct/cursor)
                :cursor-equal?
                (fn [left right]
                  (and (= (base/block-type left) (base/block-type right))
                       (= (base/block-tag left) (base/block-tag right))
                       (= (base/block-string left) (base/block-string right))))})))

^{:refer code.migrate.engine/layout-rewritten-source :added "4.1"}
(fact "lays out only rewritten forms and remains idempotent"
  (let [source "(ns demo)\n\n;; keep\n(def stable\n  {:a 1})\n\n(defn find\n  \"doc\"\n  {:added \"3.0\"}\n  ([zip move pred]\n   (pred zip)))\n"
        target (catalog/target-by-id +catalog+ :migration/std-lib-zip)
        first-pass (:output (migrate-source source +catalog+ target))
        second-pass (:output (migrate-source first-pass +catalog+ target))]
    [(clojure.string/includes? first-pass
                               ";; keep\n(def stable\n  {:a 1})")
     (clojure.string/includes? first-pass
                               "(defn find\n  \"doc\"\n  {:added \"3.0\"}\n  [zip move pred]\n  (pred zip))")
     (nil? (re-find #"(?m)^[ \t]+$" first-pass))
     (= first-pass second-pass)])
  => [true true true true])

^{:refer code.migrate.engine/layout-source-form :added "4.1"}
(fact "falls back when Foundation layout hooks reject a migrated form"
  [(block/string (layout-source-form [:path] true))
   (block/string (layout-source-form '(def x {:a [:path]}) true))]
  => ["[:path]" "(def x\n  {:a [:path]})"])

^{:refer code.migrate.engine/rewrite-unused-requires :added "4.1"}
(fact "emits the minimal namespace after aliased dependencies disappear"
  (let [target (catalog/target-by-id +catalog+ :migration/std-block-check)
        output (:output
                (migrate-source
                 "(ns std.block.check\n  (:require [std.lib.collection :as c]\n            [std.lib.foundation :as h]))\n(def checks\n  {:list c/form?})\n"
                 +catalog+
                 target))]
    [(first (clojure.string/split-lines output))
     (clojure.string/includes? output "{:list form?}")
     (clojure.string/includes? output
                               "(def checks\n  {:list form?})")
     (clojure.string/includes? output ":require")])
  => ["(ns std.block.check)" true true false])

^{:refer code.migrate.engine/rewrite-navigation-template-vars :added "4.1"}
(fact "expands navigation accessors before native macro evaluation"
  (rewrite-navigation-template-vars
   '(f/template-vars [nav-template]
      (block? base/block?)
      (tag base/block-tag)))
  => '(do
        (defn block?
          ([zip] (block? zip :right))
          ([zip step]
           (if-let [elem (std.lib.zip/get zip)]
             (base/block? elem))))
        (defn tag
          ([zip] (tag zip :right))
          ([zip step]
           (if-let [elem (std.lib.zip/get zip)]
             (base/block-tag elem))))))

^{:refer code.migrate.engine/rewrite-block-interface :added "4.1"}
(fact "folds every JVM block interface into its native protocol contract"
  (rewrite-block-interface '(definterface IBlock (_type []) (_tag [])))
  => '(defprotocol IBlock
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

  (rewrite-block-interface '(definterface IBlockModifier (_modify [acc input])))
  => '(defprotocol IBlockModifier
        (block-modify [block accumulator input]))

  (rewrite-block-interface '(definterface IBlockExpression (_value [])))
  => '(defprotocol IBlockExpression
        (block-value [block])
        (block-value-string [block]))

  (rewrite-block-interface '(definterface IBlockContainer (_children [])))
  => '(defprotocol IBlockContainer
        (block-children [block])
        (replace-children [block children])))

^{:refer code.migrate.engine/remove-block-interface-remnants :added "4.1"}
(fact "removes adjacent JVM interfaces after folding their contract"
  (let [root (nav/parse-root
              "(defprotocol IBlock (block-type [block]))\n(definterface A (a []))\n(definterface B (b []))\n(comment (require 'jvm.namespace))")
        output (-> root remove-block-interface-remnants nav/root-string)]
    [(clojure.string/includes? output "defprotocol IBlock")
     (clojure.string/includes? output "definterface")
     (clojure.string/includes? output "jvm.namespace")])
  => [true false false])

^{:refer code.migrate.engine/migration-source-input :added "4.1"}
(fact "uses a reviewed native template for semantic type folds"
  (let [applied (atom [])
        output (migration-source-input
                "original"
                {:unit/kind :source
                 :target/source-template
                 "resources/code/migrate/templates/std_block_type.hal"}
                applied)]
    [(clojure.string/starts-with? output "(ns std.block.type")
     @applied])
  => [true [:foundation/reviewed-native-template]]

  (let [applied (atom [])]
    [(migration-source-input
      "original-test-form"
      {:unit/kind :test
       :target/source-template
       "resources/code/migrate/templates/std_block_type.hal"}
      applied)
     @applied])
  => ["original-test-form" []])

^{:refer code.migrate.engine/rewrite-test-override-symbols :added "4.1"}
(fact "aliases and qualifies test calls hidden by native overrides"
  (:output
   (migrate-unit
    {:unit/kind :test
     :source/path "test/std/lib/zip_test.clj"
     :source/string
     "(ns std.lib.zip-test (:require [std.lib.zip :refer :all]) (:refer-clojure :exclude [find get]))\n(get zip)\n(find zip pred)"}
    +catalog+))
  => "(ns std.lib.zip-test (:require [std.lib.zip :refer :all :as zip]) (:config {:override [get find prewalk postwalk]}))\n(zip/get zip)\n(zip/find zip pred)"

  (let [target (catalog/target-by-id +catalog+ :migration/std-block-navigate)]
    (:output
     (migrate-source
      "(ns std.block.navigate-test (:require [std.block.navigate :refer :all] [std.block.type :as type]))\n(type nav)"
      +catalog+
      (assoc target
             :unit/kind :test
             :target/rules (:target/test-rules target)))))
  => "(ns std.block.navigate-test (:require [std.block.navigate :refer :all :as navigate] [std.block.type :as type]) (:config {:override [next replace type]}))\n(navigate/type nav)")

^{:refer code.migrate.engine/rewrite-optional-rest-parameter :added "4.1"}
(fact "lowers optional rest-vector parameters to native bindings"
  (rewrite-optional-rest-parameter
   '(defn layout-main [form & [opts]] (f form opts)))
  => '(defn layout-main [form & migration-optional-arguments]
        (let [opts (first (drop 0 migration-optional-arguments))]
          (f form opts))))

^{:refer code.migrate.engine/rewrite-print-meta-binding :added "4.1"}
(fact "removes the host-only print metadata binding"
  (rewrite-print-meta-binding
   '(binding [*print-meta* true] (pr-str value)))
  => '(migration-meta-tree value))

^{:refer code.migrate.engine/rewrite-layout-optional-opts :added "4.1"}
(fact "materializes omitted layout options as a native map"
  (rewrite-layout-optional-opts
   '(defn layout-main [form & migration-optional-arguments]
      (let [opts (first (drop 0 migration-optional-arguments))]
        (f form opts))))
  => '(defn layout-main [form & migration-optional-arguments]
        (let [opts (or (first (drop 0 migration-optional-arguments)) {})]
          (f form opts))))

^{:refer code.migrate.engine/rewrite-layout-map-pairs :added "4.1"}
(fact "materializes native map entries before sequence layout"
  (rewrite-layout-map-pairs
   '(defn layout-two-column [pairs]
      (let [pairs (if col-sort (sort-by first pairs) pairs)] pairs)))
  => '(defn layout-two-column [pairs]
        (let [pairs (if col-sort
                      (sort-by first
                               (if (map? pairs)
                                 (if (Algo/ordered-map? pairs) (seq pairs) (sort (seq pairs)))
                                 pairs))
                      (if (map? pairs)
                        (if (Algo/ordered-map? pairs) (seq pairs) (sort (seq pairs)))
                        pairs))]
          pairs)))

^{:refer code.migrate.test/materialize-quoted-collections :added "4.1"}
(fact "materializes quoted maps with native insertion order"
  (clojure.string/includes?
   (pr-str
    (migrate.test/materialize-quoted-collections
     '(f '(let [config {:server 1 :db 2}] config))))
   "Algo/ordered-map")
  => true)

^{:refer code.migrate.engine/rewrite-layout-readable-width :added "4.1"}
(fact "preserves Clojure map separator width during native layout estimation"
  (rewrite-layout-readable-width
   '(defn get-max-width [form] (count (pr-str form))))
  => '(defn get-max-width [form]
        (letfn [(separator-width [value]
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
             (separator-width form)))))

^{:refer code.migrate.engine/rewrite-map-destructuring :added "4.1" :id symbolic-map-keys}
(fact "lowers symbolic map destructuring parameters"
  (rewrite-map-destructuring
   '(defn form [{:syms [fsym doc]}] [fsym doc]))
  => '(defn form [migration-map-param-1]
        (let [fsym (get migration-map-param-1 'fsym)
              doc (get migration-map-param-1 'doc)]
          [fsym doc])))

^{:refer code.migrate.engine/rewrite-nested-sequential-parameters :added "4.1"}
(fact "lowers short nested parameters with rest and as bindings"
  (rewrite-nested-sequential-parameters
   '(defn annotate [[tag attrs & more :as form]] (f tag attrs more form)))
  => '(defn annotate [migration-argument-0]
        (let [migration-argument-0-value migration-argument-0
              form migration-argument-0-value
              tag (first (drop 0 migration-argument-0-value))
              attrs (first (drop 1 migration-argument-0-value))
              more (drop 2 migration-argument-0-value)]
          (f tag attrs more form))))

^{:refer code.migrate.engine/rewrite-merge-meta :added "4.1"}
(fact "lowers Foundation metadata merging to native primitives"
  (rewrite-merge-meta '(c/merge-meta value metadata))
  => '(with-meta value (merge-nested (meta value) metadata)))

^{:refer code.migrate.engine/rewrite-fixed-apply :added "4.1"}
(fact "lowers fixed apply arguments to one native sequence"
  (rewrite-fixed-apply '(apply list value more))
  => '(cons value more))

^{:refer code.migrate.test/rewrite-block-test-compat :added "4.1" :id bare-thread-str}
(fact "rewrites a bare threaded block string conversion"
  (migrate.test/rewrite-block-test-compat
   '(-> value bind/layout-main str)
   {:target/overrides []})
  => '(-> value bind/layout-main std.block.base/block-representation))

^{:refer code.migrate.engine/rewrite-layout-default-form-shadow :added "4.1"}
(fact "renames duplicate layout let bindings for native lexical scope"
  (rewrite-layout-default-form-shadow
   '(defn f [form opts]
      (let [a (g form) form (h form) b (i form)]
        (j form b))))
  => '(defn f [form opts]
        (let [a (g form)
              annotated-form (h form)
              b (i annotated-form)]
          (j annotated-form b))))

^{:refer code.migrate.engine/rewrite-map-destructuring :added "4.1"}
(fact "lowers map destructuring with keys, defaults, renames, and aliases"
  (rewrite-map-destructuring
   '(defn f [{:keys [a b] :or {b 2} :as opts}]
      (let [{x :renamed} opts] [a b x opts])))
  => '(defn f [migration-map-param-2]
        (let [opts migration-map-param-2
              a (get migration-map-param-2 :a)
              b (if (has? migration-map-param-2 :b)
                  (get migration-map-param-2 :b)
                  2)]
          (let [migration-map-value-1 opts
                x (get migration-map-value-1 :renamed)]
            [a b x opts]))))

^{:refer code.migrate.engine/rewrite-duplicate-let-bindings :added "4.1"}
(fact "renames sequential Clojure let shadows"
  (rewrite-duplicate-let-bindings
   '(let [spec a spec (f spec) x spec] [spec x]))
  => '(let [spec a spec--1 (f spec) x spec--1] [spec--1 x]))

^{:refer code.migrate.engine/quoted-location? :added "4.1"}
(fact "does not structurally migrate definitions inside quoted code data"
  (let [catalog +catalog+
        target  (catalog/target-by-id catalog :migration/std-block-layout)
        output  (:output
                 (migrate-source
                  "(quote (defn f [{:keys [a]}] a))"
                  catalog
                  (assoc target
                         :unit/kind :source
                         :target/rules (:target/source-rules target))))]
    output)
  => "(quote (defn f [{:keys [a]}] a))")

^{:refer code.migrate.engine/postwalk-code :added "4.1"}
(fact "keeps quoted definitions intact inside rewritten executable forms"
  (postwalk-code
   (fn [node]
     (if (and (seq? node) (= 'defn (first node))) :rewritten node))
   '(fn [] (quote (defn f [{:keys [a]}] a))))
  => '(fn [] (quote (defn f [{:keys [a]}] a))))

^{:refer code.migrate.engine/rewrite-layout-hiccup-boolean :added "4.1"}
(fact "normalizes the layout hiccup predicate result"
  (rewrite-layout-hiccup-boolean
   '(defn layout-hiccup-like [value] (and value [:child])))
  => '(defn layout-hiccup-like [value] (boolean (and value [:child]))))

^{:refer code.migrate.engine/rewrite-function-recur :added "4.1"}
(fact "rewrites function and loop recur into deterministic named calls"
  (rewrite-function-recur
   '(defn step [x]
      (if x
        (recur (dec x))
        (loop [y 2]
          (if y (recur (dec y)) x))))
   'step
   )
  => '(defn step [x]
        (if x
          (step (dec x))
          (letfn [(step--loop-1 [y]
                    (if y (step--loop-1 (dec y)) x))]
            (let [y 2]
              (step--loop-1 y)))))

  (rewrite-function-recur
   '(defn scan [items]
      (loop [[item & more] items]
        (if item (recur more) nil)))
   'scan)
  => '(defn scan [items]
        (letfn [(scan--loop-1 [migration-loop-value-0]
                  (let [item (first migration-loop-value-0)
                        more (rest migration-loop-value-0)]
                    (if item (scan--loop-1 more) nil)))]
          (let [migration-loop-value-0 items]
            (scan--loop-1 migration-loop-value-0))))

  (rewrite-function-recur '(:left zip) 'step)
  => '(:left zip)

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output (migrate-source "(defonce state (atom {}))" +catalog+ target)))
  => "(defonce state (atom {}))"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output
     (migrate-source
      "(defn find \"doc\" {:added \"3.0\"} ([zip move pred] (pred zip)))"
      +catalog+
      target)))
  => "(defn find \"doc\" {:added \"3.0\"} [zip move pred] (pred zip))"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output
     (migrate-source "(defn f ([x] x) ([x y] y))" +catalog+ target)))
  => "(defn f ([x] x) ([x y] y))"

  (let [target (catalog/target-by-id +catalog+ :migration/code-query-compile)]
    (:output
     (migrate-source
      "(defn f ([{:keys [element] evaluate? :%}] element))"
      +catalog+
      target)))
  => "(defn f [{:keys [element], evaluate? :%}] element)")

^{:refer code.migrate.engine/rewrite-target-forms
  :id keyword-call-preserved
  :added "4.1"}
(fact "preserves native keyword lookup syntax"
  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output (migrate-source "(:left zip)" +catalog+ target)))
  => "(:left zip)"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output (migrate-source "(h/apply-with value f 1 2)" +catalog+ target)))
  => "(h/apply-with value f 1 2)"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output (migrate-source "(-> value (h/apply-with f 1 2))" +catalog+ target)))
  => "(-> value (h/apply-with f 1 2))"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (read-string
     (:output (migrate-source "(try (pred value) (catch Throwable error))"
                              +catalog+
                              target))))
  => '(try (pred value) (catch Throwable error nil))

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (read-string
     (:output
      (migrate-source
       "(->> (iterate move zip) (drop 1) (take-while right-element) (filter pred) (first))"
       +catalog+
       target))))
  => '(apply-with zip
        (comp first
              (filter pred)
              (take-while right-element)
              (drop 1)
              iter
              (partial iterate move)))

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (read-string
     (:output (migrate-source "(merge zip body)" +catalog+ target))))
  => '(reduce (fn [output entry]
                (assoc output (key entry) (val entry)))
              zip
              body)

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (read-string
     (:output
      (migrate-source
       "[(h/wrapped x nil nil pr-str) (h/wrapped? x)]"
       +catalog+
       target))))
  => '[(result :success x) (result? x)])

^{:refer code.migrate.engine/dependency-routes
  :id test-source-namespace
  :added "4.1"}
(fact "redirects renamed source dependencies throughout migrated tests"
  (let [target (assoc (catalog/target-by-id
                       +catalog+
                       :migration/work-flow-make-makefile)
                      :unit/kind :test)
        routes (dependency-routes +catalog+ target)]
    [(rewrite-symbol routes 'std.make.makefile)
     (rewrite-symbol routes 'std.make.makefile/emit-headers)])
  => [{:value 'work.flow.make.makefile
       :rule/id :clojure/test-source-namespace}
      {:value 'work.flow.make.makefile/emit-headers
       :rule/id :clojure/test-source-namespace}])

^{:refer code.migrate.engine/dependency-routes
  :id catalog-target-namespace
  :added "4.1"}
(fact "routes dependencies through every migrated catalog target"
  (let [routes (dependency-routes +catalog+)]
    [(rewrite-symbol routes 'std.make.compile)
     (rewrite-symbol routes 'std.make.compile/compile)
     (rewrite-symbol routes 'std.make.common-test)])
  => [{:value 'work.flow.make.artifact
       :rule/id :code-migrate/catalog-target}
      {:value 'work.flow.make.artifact/compile
       :rule/id :code-migrate/catalog-target}
      {:value 'work.flow.make.common-test
       :rule/id :code-migrate/catalog-target}])

^{:refer code.migrate.engine/rewrite-character-whitespace :added "4.1"}
(fact "rewrites std.block JVM string and character operations"
  [(rewrite-character-whitespace '(Character/isWhitespace c))
   (rewrite-string-starts-with '(.startsWith s ";"))
   (rewrite-character-array-string '(String. caret-chars))
   (rewrite-join-lines '(prose/join-lines lines))
   (rewrite-some '(some predicate values))
   (rewrite-map-juxt '(collection/map-juxt [:index identity] values))
   (rewrite-file-slurp '(slurp "fixture.txt"))
   (rewrite-iobj-instance '(instance? clojure.lang.IObj value))
   (rewrite-iobj-metadata '(instance? clojure.lang.IObj value))
   (rewrite-character-literal \")
   (rewrite-character-literal \\)
   (rewrite-character-literal \space)
   (rewrite-nil-set #{nil \@ \space})
   (rewrite-ratio-literal 3/4)
   (rewrite-token-checks '(def *token-checks* {}))
   (rewrite-escaped-character-first '(first "\\\\t"))
   (rewrite-escaped-character-first '(first "\\\\\\\""))
   (rewrite-nil-membership '(contains? *boundaries* c))
   (rewrite-check-tag '(defn tag [checks input] :old))]
  => '[(String/blank? (str c))
       (String/starts-with? s ";")
       (apply str caret-chars)
       (apply str (interpose "\n" lines))
       (first (filter predicate values))
       (into {}
             (map (fn [migration-map-juxt-value]
                    [(get migration-map-juxt-value :index)
                     (identity migration-map-juxt-value)])
                  values))
       (std.foundation.string/decode-utf8 (deref (File/read "fixture.txt")))
       (satisfies? IObjType value)
       (and (satisfies? IObjType value) (seq (meta value)))
       (first (pr-str ""))
       (first (pr-str (first (pr-str ""))))
       \space
       (set [\@ \space nil])
       (/ 3 4)
       (def *token-checks*
         {:nil nil?
          :boolean boolean?
          :number number?
          :keyword keyword?
          :symbol symbol?
          :string string?
          :char char?})
       \tab
       (first (pr-str ""))
       (or (nil? c) (has? *boundaries* c))
       (defn tag [checks input]
         (reduce-kv
          (fn [out tag check]
            (if out out (if (check input) tag nil)))
          nil
          checks))]

  (let [target (catalog/target-by-id +catalog+ :migration/std-block-check)
        output (:output
                (migrate-source
                 "(ns demo)\n(def boundaries #{\\space nil})"
                 +catalog+
                 target))]
    [(clojure.string/starts-with? output "(ns demo")
     (clojure.string/starts-with? output "(do ")])
  => [true false])

^{:refer code.migrate.engine/rewrite-exception-constructor :added "4.1"}
(fact "rewrites JVM exception construction and integer parsing to native forms"
  [(rewrite-exception-constructor '(Exception. "boom"))
   (rewrite-integer-parse '(Integer/parseInt (name value)))
   (rewrite-number-format-catch
    '(catch java.lang.NumberFormatException error))]
  => '[(ex-info "boom" {}) (parse-long (name value))
       (catch Throwable error nil)])

^{:refer code.migrate.engine/rewrite-keep-indexed :added "4.1"}
(fact "expands direct and thread-last keep-indexed calls"
  [(rewrite-keep-indexed '(keep-indexed f values))
   (rewrite-keep-indexed '(->> values (keep-indexed f)))]
  => '[(keep identity (map-indexed f values))
       (keep identity (map-indexed f values))])

^{:refer code.migrate.engine/rewrite-process-path-binding :added "4.1"}
(fact "adapts the remaining native sequential compatibility edges"
  [(rewrite-process-path-binding
    '(defn process-path
       ([path] path)
       ([[x y & xs :as more] out]
        (if more (recur (cons y xs) out) [x y xs more out]))))
   (rewrite-list-compatible '(list? element))
   (rewrite-fn-form-compatible '(fn? element))]
  => '[(defn process-path
         ([path] path)
         ([migration-path out]
          (let [more migration-path
                x (first more)
                y (second more)
                xs (drop 2 more)]
            (if more
              (process-path (vec (cons y xs)) out)
              [x y xs more out]))))
       (form? element)
       (and (std.foundation/fn? element) (not (form? element)))])

^{:refer code.migrate.engine/diagnostics :added "4.1"}
(fact "reports unresolved host interop deterministically"
  (:diagnostics
   (migrate-source "(ns demo)\n(System/currentTimeMillis)\n"
                   +catalog+))
  => [{:type :migration/host-interop
       :symbol 'System/currentTimeMillis
       :safety :manual}]

  (mapv host-symbol?
        ['... '.write 'System/currentTimeMillis
         'String/blank? 'String/starts-with?])
  => [false true true false false]

  (mapv host-symbol?
        ['Stream/next 'Edn/read 'Json/write 'Crypto/sha256 'Process/spawn 'OS/time-ms
         'MessageDigest/getInstance 'ProcessBuilder/start])
  => [false false false false false false true true])

^{:refer code.migrate.engine/dependency-routes :added "4.1"}
(fact "records draft compatibility rules without activating their routes"
  (let [by-id (into {}
                    (map (juxt :rule/id identity))
                    (:migration/rules +catalog+))]
    (mapv (fn [id]
            [(:rule/status (get by-id id))
             (get-in by-id [id :rule/rewrite])])
          [:clojure/walk-foundation
           :clojure/java-io-stream
           :clojure/edn-native
           :clojure/pprint-pretty
           :clojure/message-digest-crypto
           :clojure/process-builder-native]))
  => [[:draft {:op :replace-namespace :namespace 'std.foundation}]
      [:draft {:op :replace-namespace :namespace 'Stream}]
      [:draft {:op :replace-namespace :namespace 'Edn}]
      [:draft {:op :replace-namespace :namespace 'std.foundation.pretty}]
      [:draft {:op :native-crypto-sha256}]
      [:draft {:op :native-process-spawn}]]

  (mapv #(contains? (dependency-routes +catalog+) %)
        ['clojure.walk 'clojure.java.io 'clojure.edn 'clojure.pprint])
  => [false false false false]

  (let [promoted (update +catalog+ :migration/rules
                         (fn [rules]
                           (mapv #(dissoc % :rule/status) rules)))
        routes   (dependency-routes promoted)]
    [(rewrite-symbol routes 'clojure.walk/postwalk)
     (rewrite-symbol routes 'clojure.java.io/reader)
     (rewrite-symbol routes 'clojure.edn/read-string)
     (rewrite-symbol routes 'clojure.pprint/pprint)])
  => [{:value 'std.foundation/postwalk
       :rule/id :clojure/walk-foundation}
      {:value 'Stream/reader
       :rule/id :clojure/java-io-stream}
      {:value 'Edn/read-string
       :rule/id :clojure/edn-native}
      {:value 'std.foundation.pretty/pprint
       :rule/id :clojure/pprint-pretty}])
