(ns code.migrate.engine-test
  (:require [code.migrate.catalog :as catalog]
            [code.migrate.engine :refer :all])
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
     (rewrite-symbol routes 'ifn?)])
  => [{:value 'std.foundation
       :rule/id :foundation/std-lib-foundation}
      {:value 'std.foundation/inc
       :rule/id :foundation/std-lib-foundation}
      {:value 'fn
       :rule/id :clojure/anonymous-function-reader}
      {:value 'fn?
       :rule/id :clojure/callable-predicate}]

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
  => ["(ns std.lib.zip (:config {:override [get find prewalk postwalk]}))\n(defstruct Zipper [context prefix display parent left right depth changed?])\n(def value nil)"
      []])

^{:refer code.migrate.engine/rewrite-test-override-symbols :added "4.1"}
(fact "aliases and qualifies test calls hidden by native overrides"
  (:output
   (migrate-unit
    {:unit/kind :test
     :source/path "test/std/lib/zip_test.clj"
     :source/string
     "(ns std.lib.zip-test (:require [std.lib.zip :refer :all]) (:refer-clojure :exclude [find get]))\n(get zip)\n(find zip pred)"}
    +catalog+))
  => "(ns std.lib.zip-test (:require [std.lib.zip :refer :all :as zip]) (:config {:override [get find prewalk postwalk]}))\n(zip/get zip)\n(zip/find zip pred)")

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
    (:output (migrate-source "(try (pred value) (catch Throwable error))"
                             +catalog+
                             target)))
  => "(try (pred value) (catch Throwable error nil))"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output
     (migrate-source
      "(->> (iterate move zip) (drop 1) (take-while right-element) (filter pred) (first))"
      +catalog+
      target)))
  => "(call zip (comp first (filter pred) (take-while right-element) (drop 1) iter (partial iterate move)))"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output (migrate-source "(merge zip body)" +catalog+ target)))
  => "(reduce (fn [output entry] (assoc output (key entry) (val entry))) zip body)"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output
     (migrate-source
      "[(h/wrapped x nil nil pr-str) (h/wrapped? x)]"
      +catalog+
      target)))
  => "[(result :success x) (result? x)]")

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
          checks))])

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
  => [false true true false false])
