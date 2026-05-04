(ns hara.runtime.jep-test
  (:require [hara.runtime.jep :as jep :refer :all]
             [hara.runtime.jep.bootstrap :as bootstrap]
            [hara.common.util :as ut]
             [std.concurrent :as cc]
             [hara.lang :as l]
             [std.lib.component :as component]
             [std.lib.foundation :as f])
  (:use code.test))

(def CANARY
  (bootstrap/jep-available?))

(fact:global
 {:setup    [(when CANARY
               (l/rt:restart))]
  :teardown [(when CANARY
               (l/rt:stop))]})

^{:refer hara.runtime.jep/jep-bus :added "3.0"
  ;; :setup [(assert (= 10 (!.py (+ 1 2 3 4))))]
  }
(fact "gets or creates a runtime bus for thread isolation"

  (jep/jep-bus)
  => cc/bus?)

^{:refer hara.runtime.jep/make-interpreter :added "3.0"
  :setup [(mapv (fn [itp]
                  (f/suppress (jep/close-interpreter itp)))
                @jep/*interpreters*)]}
(fact "makes a shared interpreter"

  (if CANARY
    (let [itp (jep/make-interpreter)]
      (try
        (class itp)
        (finally
          (jep/close-interpreter itp))))
    true)
  => (if CANARY jep.SharedInterpreter true))

^{:refer hara.runtime.jep/close-interpreter :added "3.0"}
(fact "closes the shared interpreter"
  (if CANARY
    (let [itp (jep/make-interpreter)]
      (close-interpreter itp)
      (contains? @jep/*interpreters* itp) => false)
    true))

^{:refer hara.runtime.jep/eval-exec-interpreter :added "3.0"
  :setup [(mapv (fn [itp]
                  (f/suppress (jep/close-interpreter itp)))
                @jep/*interpreters*)]}
(fact "executes script on the interpreter"

  (if CANARY
    (jep:temp-interpreter itp
                          (eval-exec-interpreter itp "a = 1\nb = 2")
                          (eval-get-interpreter itp "[a, b]"))
    true)
  => (if CANARY [1 2] true))

^{:refer hara.runtime.jep/eval-get-interpreter :added "3.0"}
(fact "gets a value from the interpreter"
  (if CANARY
    (jep:temp-interpreter itp
                          (eval-get-interpreter itp "1 + 1"))
    true)
  => (if CANARY 2 true))

^{:refer hara.runtime.jep/jep:temp-interpreter :added "3.0"}
(fact "gets a value from the interpreter"
  (if CANARY
    (jep:temp-interpreter itp
                          (eval-get-interpreter itp "1 + 1"))
    true)
  => (if CANARY 2 true))

^{:refer hara.runtime.jep/jep-handler :added "3.0"}
(fact "creates a loop handler from interpreter"

  (if CANARY
    (jep:temp-interpreter itp
                          (let [handler (jep-handler (atom itp))]
                            (handler {:op :exec :body "a = 1"})
                            (handler {:op :get :body "a"})))
    true)
  => (if CANARY 1 true))

^{:refer hara.runtime.jep/eval-command-jep :added "3.0"
  :setup    [(when CANARY
               (def +jep+ (component/start (jep/rt-jep:create {}))))]
  :teardown [(when CANARY
               (component/stop +jep+))]}
(fact "inputs command input jep context"

  (if CANARY
    (do
      @(eval-command-jep +jep+ {:op :exec :body "a = 1"})
      => nil

      @(eval-command-jep +jep+ {:op :get :body "a"})
      => 1
      true)
    true)
  => true)

^{:refer hara.runtime.jep/eval-command-fn :added "3.0"
  :setup    [(when CANARY
               (def +jep+ (component/start (jep/rt-jep:create {}))))]
  :teardown [(when CANARY
               (component/stop +jep+))]}
(fact "helper function to input command"
  (if CANARY
    ((eval-command-fn :get :body) +jep+ "1+1")
    true)
  => (if CANARY (any future? 2) true))

^{:refer hara.runtime.jep/start-jep :added "3.0"}
(fact "starts up the jep runtime"
  (if CANARY
    (let [jep (rt-jep:create {})]
      (start-jep jep)
      (try
        (rt-jep? jep) => true
        (finally (stop-jep jep))))
    true))

^{:refer hara.runtime.jep/stop-jep :added "3.0"}
(fact "stops the jep runtime"
  (when CANARY
    (let [jep (component/start (rt-jep:create {}))]
      (stop-jep jep)
      ;; check if stopped?
      )))

^{:refer hara.runtime.jep/kill-jep :added "3.0"}
(fact "kills the jep runtime"
  (when CANARY
    (let [jep (component/start (rt-jep:create {}))]
      (kill-jep jep))))

^{:refer hara.runtime.jep/invoke-ptr-jep :added "4.0"}
(fact "invokes a pointer in the runtime"

  (if CANARY
    (do
      (l/script- :python
        {:runtime :jep
         :config {}
         :require [[xt.lang.common-lib :as k]]
         :emit {:cache true}})
      (invoke-ptr-jep (l/rt :python)
                      (ut/lang-pointer :python
                                       {:module 'xt.lang.common-lib
                                        :id 'add})
                      [1 2]))
    true)
  => (if CANARY 3 true))

^{:refer hara.runtime.jep/rt-jep:create :added "3.0"}
(fact "creates a componentizable runtime"
  (rt-jep:create {})
  => rt-jep?)

^{:refer hara.runtime.jep/rt-jep :added "3.0"}
(fact "creates and starts the runtime"
  (if CANARY
    (let [jep (rt-jep {})]
      (try
        (rt-jep? jep) => true
        (finally (stop-jep jep))))
    true))

^{:refer hara.runtime.jep/rt-jep? :added "3.0"}
(fact "checks that object is a jep runtime"
  (rt-jep? (rt-jep:create {})) => true)
