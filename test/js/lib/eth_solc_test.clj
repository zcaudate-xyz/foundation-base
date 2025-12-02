(ns js.lib.eth-solc-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [rt.solidity :as s]
            [rt.solidity.env-ganache :as env-ganache]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.lib.eth-solc :as eth-solc :include [:fn]]
             [js.core :as j]]})

(fact:global
 {:setup    [(s/rt:stop-ganache-server)
             (Thread/sleep 1000)
             (s/rt:start-ganache-server)
             (Thread/sleep 500)
             (l/rt:restart)
             (l/rt:scaffold :js)]
  :teardown [(l/rt:stop)]})


^{:refer js.lib.eth-solc/contract-wrap-body :added "4.0" :unchecked true}
(fact "wraps the body in a contract"
  ^:hidden
  
  (eth-solc/contract-wrap-body
   (std.string/|
    "function test___hello() pure public returns(string memory) {"
    "  return \"HELLO WORLD\";"
    "}")
   "Test")
  => (std.string/|
   "// SPDX-License-Identifier: GPL-3.0"
   "pragma solidity >=0.7.0 <0.9.0;"
   ""
   "contract Test {"
   "function test___hello() pure public returns(string memory) {"
   "  return \"HELLO WORLD\";"
   "}"
   "}"))

^{:refer js.lib.eth-solc/contract-compile :added "4.0" :unchecked true}
(fact "compiles a single contract"
  ^:hidden

  (!.js
   (k/obj-keys
    (eth-solc/contract-compile
     (eth-solc/contract-wrap-body
      (@! (std.string/|
           "function test___hello() pure public returns(string memory) {"
           "  return \"HELLO WORLD\";"
           "}"))
      "Test")
     "test.sol")))
  => ["contracts" "sources"]
  
  (!.js
   (k/obj-keys
    (eth-solc/contract-compile
     (eth-solc/contract-wrap-body
      "function test___WRONG() {\n  return \"HELLO WORLD\";\n}"
      "Test")
     "test.sol")))
  => ["errors" "sources"])
