(ns code.migrate.engine-test
  (:require [code.migrate.catalog :as catalog]
            [code.migrate.engine :refer :all]
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
            (step--loop-1 2))))

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
          (scan--loop-1 items)))

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
  => "(defn f ([x] x) ([x y] y))")

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

^{:refer code.migrate.engine/rewrite-character-whitespace :added "4.1"}
(fact "rewrites std.block JVM string and character operations"
  [(rewrite-character-whitespace '(Character/isWhitespace c))
   (rewrite-string-starts-with '(.startsWith s ";"))
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
        ['Stream/next 'Edn/read 'Crypto/sha256 'Process/spawn
         'MessageDigest/getInstance 'ProcessBuilder/start])
  => [false false false false true true])

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
