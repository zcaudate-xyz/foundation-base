tahto/runtime/vscode/impl_test.clj:1:(ns tahto.runtime.vscode.impl-test
  (:require [tahto.core :as h]
tahto/runtime/vscode/impl_test.clj:3:            [tahto.runtime.vscode.impl :as impl]
            [std.lib.env :as env])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "code"))})

tahto/runtime/vscode/impl_test.clj:9:^{:refer tahto.runtime.vscode.impl/vscode-exec :added "4.1"}
(fact "resolves the vscode executable"
  (let [exec (impl/vscode-exec)]
    (or (string? exec)
        (and (vector? exec)
             (every? string? exec))))
  => true)

tahto/runtime/vscode/impl_test.clj:17:^{:refer tahto.runtime.vscode.impl/js-eval-wrap :added "4.1"}
(fact "wraps js code for eval"
  (let [wrapped (impl/js-eval-wrap "1 + 2 + 3")]
    [(boolean (re-find #"eval" wrapped))
     (boolean (re-find #"1 \+ 2 \+ 3" wrapped))])
  => [true true])

tahto/runtime/vscode/impl_test.clj:24:^{:refer tahto.runtime.vscode.impl/start-vscode :added "4.1" :timeout 60000}
(fact "starts and stops a vscode process"
  (let [rt (-> (impl/vscode:create {})
               (impl/start-vscode))
        result [(boolean (:process rt))
                (boolean (:output rt))
                (boolean (:input rt))
                (number? @(:msgid rt))]
        _ (impl/stop-vscode rt)]
    result)
  => [true true true true])

tahto/runtime/vscode/impl_test.clj:36:^{:refer tahto.runtime.vscode.impl/raw-eval-vscode :added "4.1" :timeout 60000}
(fact "evaluates js code via vscode"
  (let [rt (impl/vscode {})]
    (try
      [(impl/raw-eval-vscode rt "1 + 2 + 3")
       (impl/raw-eval-vscode rt "typeof Array")]
      (finally
        (impl/stop-vscode rt))))
  => [6 "function"])

tahto/runtime/vscode/impl_test.clj:46:^{:refer tahto.runtime.vscode.impl/raw-eval-vscode :added "4.1" :timeout 60000
  :id test-raw-eval-vscode-errors}
(fact "propagates js errors"
  (let [rt (impl/vscode {})]
    (try
      (impl/raw-eval-vscode rt "throw new Error('hello error')")
      (catch clojure.lang.ExceptionInfo e
        (:error (ex-data e)))
      (finally
        (impl/stop-vscode rt))))
  => #"hello error")

tahto/runtime/vscode/impl_test.clj:58:^{:refer tahto.runtime.vscode.impl/invoke-ptr-vscode :added "4.1" :timeout 60000}
(fact "invokes a pointer through the vscode runtime"
  (let [rt (impl/vscode {})]
    (try
      (number? (impl/invoke-ptr-vscode
                rt
                (h/ptr :js {:module (ns-name *ns*)})
                ['(+ 1 2 3)]))
      (finally
        (impl/stop-vscode rt))))
  => true)

tahto/runtime/vscode/impl_test.clj:70:^{:refer tahto.runtime.vscode.impl/vscode:create :added "4.1"}
(fact "creates a vscode runtime record"
  (let [rt (impl/vscode:create {})]
    [(boolean rt)
     (= :vscode (:tag rt))])
  => [true true])

tahto/runtime/vscode/impl_test.clj:77:^{:refer tahto.runtime.vscode.impl/vscode :added "4.1" :timeout 60000}
(fact "creates and starts a vscode runtime"
  (let [rt (impl/vscode {})]
    (try
      (boolean rt)
      (finally
        (impl/stop-vscode rt))))
  => true)


tahto/runtime/vscode/impl_test.clj:87:^{:refer tahto.runtime.vscode.impl/stop-vscode :added "4.1"}
(fact "returns the runtime when stopping"
  (let [rt (impl/vscode:create {})]
    (identical? (impl/stop-vscode rt) rt))
  => true)

tahto/runtime/vscode/impl_test.clj:93:^{:refer tahto.runtime.vscode.impl/next-msgid :added "4.1"}
(fact "increments the message id counter"
  (let [rt (assoc (impl/vscode:create {}) :msgid (atom 0))]
    [(impl/next-msgid rt)
     (impl/next-msgid rt)
     @(:msgid rt)])
  => [1 2 2])

tahto/runtime/vscode/impl_test.clj:101:^{:refer tahto.runtime.vscode.impl/send-request :added "4.1" :timeout 60000}
(fact "sends a code request and returns a response"
  (let [rt (impl/vscode {})
        code (impl/js-eval-wrap "1 + 2 + 3")]
    (try
      (let [response (impl/send-request rt code)]
        [(number? (:id response))
         (= "ok" (:status response))
         (= 6 (:value response))])
      (finally
        (impl/stop-vscode rt))))
  => [true true true])

tahto/runtime/vscode/impl_test.clj:114:^{:refer tahto.runtime.vscode.impl/vscode-shared:create :added "4.1"}
(fact "creates a shared vscode runtime client"
  (let [shared (impl/vscode-shared:create {})]
    [(boolean shared)
tahto/runtime/vscode/impl_test.clj:118:     (= :tahto/rt.vscode (-> shared :client :type))
     (:temp shared)])
  => [true true true])
