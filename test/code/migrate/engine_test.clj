(ns code.migrate.engine-test
  (:require [code.migrate.catalog :as catalog]
            [code.migrate.engine :refer :all])
  (:use code.test))

(def +root+
  (or (System/getenv "HARA_WORKSPACE_ROOT")
      "../../workspace"))

(def +catalog+
  (catalog/load-catalog
   (str +root+
        "/technology/hara-specs-registry/01-lang/007-code-migration/draft/code-migration.edn")))

(def +cases+
  (:cases
   (catalog/read-edn
    (str +root+
         "/technology/hara-specs-registry/01-lang/007-code-migration/draft/conformance/bootstrap-pairs.edn"))))

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
     (rewrite-symbol routes 'std.lib.foundation/inc)])
  => [{:value 'std.foundation
       :rule/id :foundation/std-lib-foundation}
      {:value 'std.foundation/inc
       :rule/id :foundation/std-lib-foundation}])

^{:refer code.migrate.engine/rewrite-target-forms :added "4.1"}
(fact "applies only structural adaptations enabled by the target"
  (let [source "(ns std.lib.zip (:refer-clojure :exclude [find get]))\n(defrecord Zipper [context prefix display] Object (toString [obj] (str obj)))\n(defmethod print-method Zipper ([v w] (.write w (str v))))\n(def value h/NIL)"
        target (catalog/target-by-id +catalog+ :migration/std-lib-zip)
        result (migrate-source source +catalog+ target)]
    [(:output result) (:diagnostics result)])
  => ["(ns std.lib.zip (:config {:override [get find prewalk postwalk]}))\n(defstruct Zipper [context prefix display parent left right depth changed?])\n(def value nil)"
      []])

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
  => "(defonce state (atom {}))")

^{:refer code.migrate.engine/rewrite-foundation-call :added "4.1"}
(fact "ports foundation call as a self-contained native expression"
  (rewrite-foundation-call '(h/call value))
  => 'value

  (rewrite-foundation-call '(h/call value f 1 2))
  => '((fn [migration-object migration-function
            migration-argument-0 migration-argument-1]
         (if (nil? migration-function)
           migration-object
           (migration-function migration-object
                               migration-argument-0
                               migration-argument-1)))
       value f 1 2)

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output (migrate-source "(h/call value f 1 2)" +catalog+ target)))
  => "((fn [migration-object migration-function migration-argument-0 migration-argument-1] (if (nil? migration-function) migration-object (migration-function migration-object migration-argument-0 migration-argument-1))) value f 1 2)"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output (migrate-source "(-> value (h/call f 1 2))" +catalog+ target)))
  => "(-> value ((fn [migration-object migration-function migration-argument-0 migration-argument-1] (if (nil? migration-function) migration-object (migration-function migration-object migration-argument-0 migration-argument-1))) f 1 2))"

  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output (migrate-source "(try (pred value) (catch Throwable error))"
                             +catalog+
                             target)))
  => "(try (pred value) (catch Throwable error nil))")

^{:refer code.migrate.engine/rewrite-target-forms
  :id keyword-call-preserved
  :added "4.1"}
(fact "preserves native keyword lookup syntax"
  (let [target (catalog/target-by-id +catalog+ :migration/std-lib-zip)]
    (:output (migrate-source "(:left zip)" +catalog+ target)))
  => "(:left zip)")

^{:refer code.migrate.engine/diagnostics :added "4.1"}
(fact "reports unresolved host interop deterministically"
  (:diagnostics
   (migrate-source "(ns demo)\n(System/currentTimeMillis)\n"
                   +catalog+))
  => [{:type :migration/host-interop
       :symbol 'System/currentTimeMillis
       :safety :manual}]

  (mapv host-symbol? ['... '.write 'System/currentTimeMillis])
  => [false true true])
