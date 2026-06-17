(ns hara.runtime.blender.impl-test
  (:require [hara.lang :as h]
            [hara.runtime.blender.impl :as impl]
            [hara.runtime.basic.type-common :as common])
  (:use code.test))

(def +blender-available+
  (delay (common/program-exists? "blender")))

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
  (when @+blender-available+
    (let [rt (-> (impl/blender:create {})
                 (impl/start-blender))
          result [(boolean (:process rt))
                  (boolean (:socket rt))
                  (boolean (:reader rt))
                  (boolean (:output rt))
                  (number? (.get ^java.util.concurrent.atomic.AtomicInteger (:msgid rt)))]
          _ (impl/stop-blender rt)]
      result))
  => (when @+blender-available+
       [true true true true true]))

^{:refer hara.runtime.blender.impl/raw-eval-blender :added "4.1"}
(fact "evaluates python code inside blender"
  (when @+blender-available+
    (let [rt (impl/blender {})]
      (try
        [(impl/raw-eval-blender rt "OUT = 1 + 2 + 3")
         (string? (impl/raw-eval-blender rt "OUT = str(bpy.data.meshes.new('Cube'))"))]
        (finally
          (impl/stop-blender rt)))))
  => (when @+blender-available+
       [6 true]))

^{:refer hara.runtime.blender.impl/raw-eval-blender :added "4.1"}
(fact "propagates python errors"
  (when @+blender-available+
    (let [rt (impl/blender {})]
      (try
        (impl/raw-eval-blender rt "OUT = 1 / 0")
        (catch clojure.lang.ExceptionInfo e
          (:error (ex-data e)))
        (finally
          (impl/stop-blender rt)))))
  => (when @+blender-available+
       #"division( or modulo)? by zero"))

^{:refer hara.runtime.blender.impl/invoke-ptr-blender :added "4.1"}
(fact "invokes a pointer through the blender runtime"
  (when @+blender-available+
    (let [rt (impl/blender {})]
      (try
        (number? (impl/invoke-ptr-blender
                  rt
                  (h/ptr :python {:module (ns-name *ns*)})
                  ['(+ 1 2 3)]))
        (finally
          (impl/stop-blender rt)))))
  => (when @+blender-available+
       true))

^{:refer hara.runtime.blender.impl/blender:create :added "4.1"}
(fact "creates a blender runtime record"
  (let [rt (impl/blender:create {})]
    [(boolean rt)
     (= :blender (:tag rt))])
  => [true true])

^{:refer hara.runtime.blender.impl/blender :added "4.1"}
(fact "creates and starts a blender runtime"
  (when @+blender-available+
    (let [rt (impl/blender {})]
      (try
        (boolean rt)
        (finally
          (impl/stop-blender rt)))))
  => (when @+blender-available+
       true))
