(ns js.lib.eth-solc
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify])
  (:refer-clojure :exclude [compile]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]]
   :import [["solc" :as solc]
            ["solc" :as solc]]})

(def$.js compile solc.compile)

(defn.js contract-wrap-body
  "wraps the body in a contract"
  {:added "4.0"}
  [code name prefix]
  (return
   (k/arr-join ["// SPDX-License-Identifier: GPL-3.0"
                "pragma solidity >=0.7.0 <0.9.0;"
                (or prefix "")
                (+ "contract " name " {")
                code
                "}"]
               "\n")))

(defn.js contract-compile
  "compiles a single contract"
  {:added "4.0"}
  [code file]
  (var input
       {:language "Solidity"
        :sources {file
                  {:content code}}
        :settings
        {:outputSelection
         {"*" {"*" ["*"]}}}})
  (return
   (k/json-decode (-/compile (k/json-encode input)))))


