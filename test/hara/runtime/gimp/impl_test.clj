tahto/runtime/gimp/impl_test.clj:1:(ns tahto.runtime.gimp.impl-test
  (:require [tahto.core :as h]
tahto/runtime/gimp/impl_test.clj:3:            [tahto.runtime.gimp.impl :as impl]
            [std.lib.component :as component]
            [std.lib.env :as env])
  (:use code.test))

(fact:global
 {:skip (not (or (env/program-exists? "gimp")
                 (env/program-exists? "gimp-console")))
  :setup    [(def +rt+ (impl/gimp {}))]
  :teardown [(component/stop +rt+)]})

tahto/runtime/gimp/impl_test.clj:14:^{:refer tahto.runtime.gimp.impl/gimp-bootstrap :added "4.1"}
(fact "generates python bootstrap code"
  (let [bootstrap (impl/gimp-bootstrap 12345)]
    [(boolean (re-find #"def server_gimp" bootstrap))
     (boolean (re-find #"def client_gimp" bootstrap))
     (boolean (re-find #"return_eval" bootstrap))
tahto/runtime/gimp/impl_test.clj:20:     (boolean (re-find #"TAHTO_GIMP_READY" bootstrap))
     (boolean (re-find #"server_gimp\(12345" bootstrap))])
  => [true true true true true])

tahto/runtime/gimp/impl_test.clj:24:^{:refer tahto.runtime.gimp.impl/gimp-exec :added "4.1"}
(fact "resolves the gimp executable"
  (impl/gimp-exec)
  => string?)

tahto/runtime/gimp/impl_test.clj:29:^{:refer tahto.runtime.gimp.impl/start-gimp :added "4.1"}
(fact "starts a gimp process"
  [(boolean (:process +rt+))
   (boolean (:socket +rt+))
   (boolean (:reader +rt+))
   (boolean (:output +rt+))
   (number? (.get ^java.util.concurrent.atomic.AtomicInteger (:msgid +rt+)))]
  => [true true true true true])

tahto/runtime/gimp/impl_test.clj:38:^{:refer tahto.runtime.gimp.impl/stop-gimp :added "4.1"}
(fact "stops a gimp process without error"
  (impl/stop-gimp {:id "test" :port 12345})
  => {:id "test" :port 12345})

tahto/runtime/gimp/impl_test.clj:43:^{:refer tahto.runtime.gimp.impl/raw-eval-gimp :added "4.1"}
(fact "evaluates python code inside gimp"
  [(impl/raw-eval-gimp +rt+ "OUT = 1 + 2 + 3")
   (string? (impl/raw-eval-gimp +rt+ "OUT = str(Gimp.version())"))]
  => [6 true])

tahto/runtime/gimp/impl_test.clj:49:^{:refer tahto.runtime.gimp.impl/raw-eval-gimp :added "4.1"
  :id test-raw-eval-gimp-errors}
(fact "propagates python errors"
  (try
    (impl/raw-eval-gimp +rt+ "OUT = 1 / 0")
    (catch clojure.lang.ExceptionInfo e
      (:error (ex-data e))))
  => #"division( or modulo)? by zero")

tahto/runtime/gimp/impl_test.clj:58:^{:refer tahto.runtime.gimp.impl/invoke-ptr-gimp :added "4.1"}
(fact "invokes a pointer through the gimp runtime"
  (number? (impl/invoke-ptr-gimp
            +rt+
            (h/ptr :python {:module (ns-name *ns*)})
            ['(+ 1 2 3)]))
  => true)

tahto/runtime/gimp/impl_test.clj:66:^{:refer tahto.runtime.gimp.impl/gimp:create :added "4.1"}
(fact "creates a gimp runtime record"
  (let [rt (impl/gimp:create {})]
    [(boolean rt)
     (= :gimp (:tag rt))])
  => [true true])

tahto/runtime/gimp/impl_test.clj:73:^{:refer tahto.runtime.gimp.impl/gimp :added "4.1"}
(fact "creates and starts a gimp runtime"
  (boolean +rt+)
  => true)
