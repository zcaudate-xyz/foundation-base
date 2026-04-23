(ns js.lib.eth-solc
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:refer-clojure :exclude [compile]))

(l/script :js
  {:require [[js.core :as j]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-string :as str]]})

(defn.js compile
  [input]
  (var root (or (. process env ["PWD"])
                (. process (cwd))))
  (var solc (or (!:G solc)
                (require (+ root "/node_modules/solc"))))
  (:= (!:G solc) solc)
  (return (. solc (compile input))))

(defn.js contract-wrap-body
  "wraps the body in a contract"
  {:added "4.0"}
  [code name prefix]
  (return
   (str/join "\n"
             ["// SPDX-License-Identifier: GPL-3.0"
              "pragma solidity >=0.7.0 <0.9.0;"
              (or prefix "")
              (+ "contract " name " {")
              code
              "}"])))

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
   (xt/x:json-decode (-/compile (xt/x:json-encode input)))))
