(ns hara.runtime.basic.impl.process-lua-test
  (:require [hara.runtime.basic.impl.process-lua :refer :all]
            [hara.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :oneshot})

^{:refer hara.runtime.basic.impl.process-lua/CANARY :adopt true  :added "4.0"}
(fact "EVALUATE lua code"
  
  (!.lua (+ 1 2 3 4))
  => 10)

^{:refer hara.runtime.basic.impl.process-lua/default-oneshot-wrap :adopt true :added "4.0"}
(fact "wraps with the eval wrapper"

  (default-oneshot-wrap "1")
  => string?)

^{:refer hara.runtime.basic.impl.process-lua/default-basic-client :adopt true :added "4.0"}
(fact "wraps with the eval wrapper"

  (default-basic-client 19000)
  => string?)

^{:refer hara.runtime.basic.impl.process-lua/default-basic-client :added "4.1"
  :id test-default-basic-client-error-propagation}
(fact "constructs shared bootstrap without swallowing client loop errors"
  (let [out (default-basic-client 19000)]
    [(boolean (re-find #"cjson = require" out))
     (boolean (re-find #"function return_eval" out))
     (boolean (re-find #"local function client_basic" out))
     (boolean (re-find #"(?s)client_basic\(host,port,opts\).*pcall\(function" out))])
  => [true true true false])

^{:refer hara.runtime.basic.impl.process-lua/default-body-wrap :added "4.1"}
(fact "marks the wrapper fn as inner"
  (-> (default-body-wrap '[(defn add-10 [x] (return (+ x 10)))
                           (add-10 5)])
      second
      second
      meta
      :inner)
  => true)

^{:refer hara.runtime.basic.impl.process-lua/normalize-forms :added "4.1"}
(fact "normalizes a top-level do body"
  (normalize-forms '(do (defn add-10 [x] (return (+ x 10)))
                        (add-10 5))
                   {})
  => '((defn add-10 [x] (return (+ x 10)))
       (add-10 5))

  (normalize-forms '[1 2 3] {:bulk true})
  => '[1 2 3])

^{:refer hara.runtime.basic.impl.process-lua/default-body-transform :added "4.0"}
(fact "transform code for return"

  (default-body-transform [1 2 3] {})
  => '(do
        (defn OUT-FN [] (return [1 2 3]))
        (return (OUT-FN)))

  (default-body-transform [1 2 3] {:bulk true})
  => '(do
        (defn OUT-FN [] 1 2 (return 3))
        (return (OUT-FN)))

  (l/emit-as :lua [(default-body-transform '(do (defn add-10 [x] (return (+ x 10)))
                                                (add-10 5))
                                          {})])
  => #"local function add_10\(x\)")

^{:refer hara.runtime.basic.impl.process-lua/lua-basic-script-globalize-entry :added "4.1"}
(fact "converts local Lua declarations to globals for basic runtime"
  (lua-basic-script-globalize-entry "local function add_10(x) return x + 10 end"
                                    {:entry {:op-key :defn}})
  => "function add_10(x) return x + 10 end"

  (lua-basic-script-globalize-entry "local x = 1"
                                    {:entry {:op-key :def}})
  => "x = 1"

  (lua-basic-script-globalize-entry "function add_10(x) return x + 10 end"
                                    {:entry {:op-key :defn}})
  => "function add_10(x) return x + 10 end"

  (lua-basic-script-globalize-entry "local x = 1"
                                    {:entry {:op-key :other}})
  => "local x = 1")
