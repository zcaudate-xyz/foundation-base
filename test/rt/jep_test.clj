(ns rt.jep-test
  (:use code.test)
  (:require [rt.jep :as jep :refer :all]
            [std.lib :as h]
            [std.lang :as l]
            [std.concurrent :as cc]))

(l/script- :python
  {:runtime :jep
   :config {}
   :require [[xt.lang.base-lib :as k]]
   :emit {:cache true}})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer rt.jep/jep-bus :added "3.0"
  ;; :setup [(assert (= 10 (!.py (+ 1 2 3 4))))]
  }
(fact "gets or creates a runtime bus for thread isolation"
  ^:hidden
  
  (jep/jep-bus)
  => cc/bus?)

^{:refer rt.jep/make-interpreter :added "3.0"
  :setup [(mapv (fn [itp]
                  (h/suppress (jep/close-interpreter itp)))
                @jep/*interpreters*)]}
(fact "makes a shared interpreter"

  (let [itp (jep/make-interpreter)]
    (try
      (class itp)
      (finally
        (jep/close-interpreter itp))))
  => jep.SharedInterpreter)

^{:refer rt.jep/close-interpreter :added "3.0"}
(fact "closes the shared interpreter"
  (let [itp (jep/make-interpreter)]
    (close-interpreter itp)
    (contains? @jep/*interpreters* itp) => false))

^{:refer rt.jep/eval-exec-interpreter :added "3.0"
  :setup [(mapv (fn [itp]
                  (h/suppress (jep/close-interpreter itp)))
                @jep/*interpreters*)]}
(fact "executes script on the interpreter"
  ^:hidden

  (jep:temp-interpreter itp
                        (eval-exec-interpreter itp "a = 1\nb = 2")
                        (eval-get-interpreter itp "[a, b]"))
  => [1 2])

^{:refer rt.jep/eval-get-interpreter :added "3.0"}
(fact "gets a value from the interpreter"
  (jep:temp-interpreter itp
                        (eval-get-interpreter itp "1 + 1"))
  => 2)

^{:refer rt.jep/jep:temp-interpreter :added "3.0"}
(fact "gets a value from the interpreter"
  (jep:temp-interpreter itp
                        (eval-get-interpreter itp "1 + 1"))
  => 2)

^{:refer rt.jep/jep-handler :added "3.0"}
(fact "creates a loop handler from interpreter"

  (jep:temp-interpreter itp
                        (let [handler (jep-handler (atom itp))]
                          (handler {:op :exec :body "a = 1"})
                          (handler {:op :get :body "a"})))
  => 1)

^{:refer rt.jep/eval-command-jep :added "3.0"
  :setup    [(def +jep+ (h/start (jep/rt-jep:create {})))]
  :teardown [(h/stop +jep+)]}
(fact "inputs command input jep context"

  @(eval-command-jep +jep+ {:op :exec :body "a = 1"})
  => nil

  @(eval-command-jep +jep+ {:op :get :body "a"})
  => 1)

^{:refer rt.jep/eval-command-fn :added "3.0"
  :setup    [(def +jep+ (h/start (jep/rt-jep:create {})))]
  :teardown [(h/stop +jep+)]}
(fact "helper function to input command"
  ((eval-command-fn :get :body) +jep+ "1+1")
  => (any future? 2))

^{:refer rt.jep/start-jep :added "3.0"}
(fact "starts up the jep runtime"
  (let [jep (rt-jep:create {})]
    (start-jep jep)
    (try
      (rt-jep? jep) => true
      (finally (stop-jep jep)))))

^{:refer rt.jep/stop-jep :added "3.0"}
(fact "stops the jep runtime"
  (let [jep (h/start (rt-jep:create {}))]
    (stop-jep jep)
    ;; check if stopped?
    ))

^{:refer rt.jep/kill-jep :added "3.0"}
(fact "kills the jep runtime"
  (let [jep (h/start (rt-jep:create {}))]
    (kill-jep jep)))

^{:refer rt.jep/invoke-ptr-jep :added "4.0"}
(fact "invokes a pointer in the runtime"
  ^:hidden
  
  (invoke-ptr-jep (l/rt :python)
                  k/add
                  [1 2])
  => 3)

^{:refer rt.jep/rt-jep:create :added "3.0"}
(fact "creates a componentizable runtime"
  (rt-jep:create {})
  => rt-jep?)

^{:refer rt.jep/rt-jep :added "3.0"}
(fact "creates and starts the runtime"
  (let [jep (rt-jep {})]
    (try
      (rt-jep? jep) => true
      (finally (stop-jep jep)))))

^{:refer rt.jep/rt-jep? :added "3.0"}
(fact "checks that object is a jep runtime"
  (rt-jep? (rt-jep:create {})) => true)
