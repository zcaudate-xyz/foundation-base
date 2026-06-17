(ns hara.runtime.vscode.impl-test
  (:require [hara.lang :as h]
            [hara.runtime.vscode.impl :as impl]
            [std.lib.env :as env])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "code"))})

^{:refer hara.runtime.vscode.impl/vscode-exec :added "4.1"}
(fact "resolves the vscode executable"
  (let [exec (impl/vscode-exec)]
    (or (string? exec)
        (and (vector? exec)
             (every? string? exec))))
  => true)

^{:refer hara.runtime.vscode.impl/js-eval-wrap :added "4.1"}
(fact "wraps js code for eval"
  (let [wrapped (impl/js-eval-wrap "1 + 2 + 3")]
    [(boolean (re-find #"eval" wrapped))
     (boolean (re-find #"1 \+ 2 \+ 3" wrapped))])
  => [true true])

^{:refer hara.runtime.vscode.impl/start-vscode :added "4.1" :timeout 60000}
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

^{:refer hara.runtime.vscode.impl/raw-eval-vscode :added "4.1" :timeout 60000}
(fact "evaluates js code via vscode"
  (let [rt (impl/vscode {})]
    (try
      [(impl/raw-eval-vscode rt "1 + 2 + 3")
       (impl/raw-eval-vscode rt "typeof Array")]
      (finally
        (impl/stop-vscode rt))))
  => [6 "function"])

^{:refer hara.runtime.vscode.impl/raw-eval-vscode :added "4.1" :timeout 60000}
(fact "propagates js errors"
  (let [rt (impl/vscode {})]
    (try
      (impl/raw-eval-vscode rt "throw new Error('hello error')")
      (catch clojure.lang.ExceptionInfo e
        (:error (ex-data e)))
      (finally
        (impl/stop-vscode rt))))
  => #"hello error")

^{:refer hara.runtime.vscode.impl/invoke-ptr-vscode :added "4.1" :timeout 60000}
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

^{:refer hara.runtime.vscode.impl/vscode:create :added "4.1"}
(fact "creates a vscode runtime record"
  (let [rt (impl/vscode:create {})]
    [(boolean rt)
     (= :vscode (:tag rt))])
  => [true true])

^{:refer hara.runtime.vscode.impl/vscode :added "4.1" :timeout 60000}
(fact "creates and starts a vscode runtime"
  (let [rt (impl/vscode {})]
    (try
      (boolean rt)
      (finally
        (impl/stop-vscode rt))))
  => true)
