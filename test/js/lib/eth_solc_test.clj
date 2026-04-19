(ns js.lib.eth-solc-test
  (:require [rt.solidity :as s]
            [rt.solidity.env-ganache :as env-ganache]
            [std.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

(l/script- :js
   {:runtime :basic
     :require [[xt.lang.common-lib :as k]
               [xt.lang.common-data :as xtd]
               [xt.lang.common-repl :as repl]
               [js.lib.eth-solc :as eth-solc :include [:fn]]
               [js.core :as j]]})

(fact:global
 {:setup    [(s/rt:stop-ganache-server)
             (Thread/sleep 1000)
             (s/rt:start-ganache-server)
             (Thread/sleep 3000)
             (l/rt:restart)
             (l/rt:scaffold :js)]
  :teardown [(l/rt:stop)]})


^{:refer js.lib.eth-solc/contract-wrap-body :added "4.0" :unchecked true}
(fact "wraps the body in a contract"
  ^:hidden
  
  (eth-solc/contract-wrap-body
   (prose/|
    "function test___hello() pure public returns(string memory) {"
    "  return \"HELLO WORLD\";"
    "}")
   "Test")
  => (prose/|
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
   (xtd/obj-keys
    (eth-solc/contract-compile
     (eth-solc/contract-wrap-body
      (@! (prose/|
           "function test___hello() pure public returns(string memory) {"
           "  return \"HELLO WORLD\";"
           "}"))
      "Test")
     "test.sol")))
  => ["contracts" "sources"]
  
  (!.js
   (xtd/obj-keys
    (eth-solc/contract-compile
     (eth-solc/contract-wrap-body
      "function test___WRONG() {\n  return \"HELLO WORLD\";\n}"
      "Test")
     "test.sol")))
  => ["errors" "sources"])
