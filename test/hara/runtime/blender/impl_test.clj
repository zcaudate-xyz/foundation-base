(ns hara.runtime.blender.impl-test
  (:require [hara.lang :as h]
            [hara.lang.type-shared :as shared]
            [hara.runtime.blender.impl :as impl]
            [std.lib.env :as env])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "blender"))})

^{:refer hara.runtime.blender.impl/blender-exec :added "4.1"}
(fact "resolves the blender executable"
  (impl/blender-exec)
  => string?)

^{:refer hara.runtime.blender.impl/blender-bootstrap :added "4.1"}
(fact "generates python bootstrap code"
  (let [bootstrap (impl/blender-bootstrap 12345)]
    [(boolean (re-find #"def server_blender" bootstrap))
     (boolean (re-find #"def client_blender" bootstrap))
     (boolean (re-find #"return_eval" bootstrap))
     (boolean (re-find #"HARA_BLENDER_READY" bootstrap))
     (boolean (re-find #"server_blender\(12345" bootstrap))])
  => [true true true true true])

^{:refer hara.runtime.blender.impl/start-blender :added "4.1"}
(fact "starts and stops a blender process"
  (let [rt (-> (impl/blender:create {})
               (impl/start-blender))
        result [(boolean (:process rt))
                (boolean (:socket rt))
                (boolean (:reader rt))
                (boolean (:output rt))
                (number? (.get ^java.util.concurrent.atomic.AtomicInteger (:msgid rt)))]
        _ (impl/stop-blender rt)]
    result)
  => [true true true true true])

^{:refer hara.runtime.blender.impl/raw-eval-blender :added "4.1"}
(fact "evaluates python code inside blender"
  (let [rt (impl/blender {})]
    (try
      [(impl/raw-eval-blender rt "OUT = 1 + 2 + 3")
       (string? (impl/raw-eval-blender rt "OUT = str(bpy.data.meshes.new('Cube'))"))]
      (finally
        (impl/stop-blender rt))))
  => [6 true])

^{:refer hara.runtime.blender.impl/raw-eval-blender :added "4.1"}
(fact "propagates python errors"
  (let [rt (impl/blender {})]
    (try
      (impl/raw-eval-blender rt "OUT = 1 / 0")
      (catch clojure.lang.ExceptionInfo e
        (:error (ex-data e)))
      (finally
        (impl/stop-blender rt))))
  => #"division( or modulo)? by zero")

^{:refer hara.runtime.blender.impl/invoke-ptr-blender :added "4.1"}
(fact "invokes a pointer through the blender runtime"
  (let [rt (impl/blender {})]
    (try
      (number? (impl/invoke-ptr-blender
                rt
                (h/ptr :python {:module (ns-name *ns*)})
                ['(+ 1 2 3)]))
      (finally
        (impl/stop-blender rt))))
  => true)

^{:refer hara.runtime.blender.impl/blender:create :added "4.1"}
(fact "creates a blender runtime record"
  (let [rt (impl/blender:create {})]
    [(boolean rt)
     (= :blender (:tag rt))])
  => [true true])

^{:refer hara.runtime.blender.impl/blender :added "4.1"}
(fact "creates and starts a blender runtime"
  (let [rt (impl/blender {})]
    (try
      (boolean rt)
      (finally
        (impl/stop-blender rt))))
  => true)

^{:refer hara.runtime.blender.impl/blender-shared :added "4.1"}
(fact "two shared blender runtimes with the same id share the process"
  (let [rt1 (impl/blender-shared:create {:id :shared-blender-test})
        rt2 (impl/blender-shared:create {:id :shared-blender-test})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      [(= (shared/rt-get-inner rt1) (shared/rt-get-inner rt2))
       (boolean (:process (shared/rt-get-inner rt1)))
       (impl/raw-eval-blender (shared/rt-get-inner rt1) "OUT = 1 + 2 + 3")]
      (finally
        (std.lib.component/stop rt1)
        (std.lib.component/stop rt2))))
  => [true true 6])

(fact "stopping one shared blender runtime keeps the process alive"
  (let [rt1 (impl/blender-shared:create {:id :shared-blender-ref-test})
        rt2 (impl/blender-shared:create {:id :shared-blender-ref-test})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      (std.lib.component/stop rt1)
      (impl/raw-eval-blender (shared/rt-get-inner rt2) "OUT = 1 + 2 + 3")
      (finally
        (std.lib.component/stop rt2))))
  => 6)

(fact "shared blender runtimes with different ids do not share a process"
  (let [rt1 (impl/blender-shared:create {:id :blender-a})
        rt2 (impl/blender-shared:create {:id :blender-b})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      (not= (shared/rt-get-inner rt1) (shared/rt-get-inner rt2))
      (finally
        (std.lib.component/stop rt1)
        (std.lib.component/stop rt2))))
  => true)
