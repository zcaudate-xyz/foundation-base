(ns pune.web.debug-contract
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react-native :as n :include [:fn]]]
   :export [MODULE]})

(defn.js ContractSingleHeader
  "constructs a single header"
  {:added "0.1"}
  [#{design
     prediction
     code
     contract
     latestPrice}]
  (var #{balance escrow} (or contract {}))
  (return
   [:% n/Row
    {:style {:flex 1}}
    [:% n/Text
     {:style []}
     (j/padEnd (+ " " (or latestPrice "- "))
               6
               " ")]
    [:% n/Text
     {:style []}
     
     " " code]
    [:% n/Padding {:style {:flex 1}}]
    [:% n/Text
     {:style []}
     (+ balance " (" escrow ")")]]))

(defn.js ContractSingleLine
  "constructs a single line"
  {:added "0.1"}
  [#{design line}]
  (var #{count unfilled price trade} line)
  (return
   [:% n/Row
    {:style {:flex 1}}
    [:% n/Text
     {:style []}
      (j/padEnd (+ " " (or price "- "))
                6
                " ")]
    [:% n/Text
     {:style []}
     " [" count "]"]
    [:% n/Padding {:style {:flex 1}}]
    [:% n/Text
     {:style []}
     (+ unfilled "")]]))

(defn.js ContractSingle
  "constructs a single contract"
  {:added "0.1"}
  [#{design
     prediction
     code
     contract
     escrow
     latestPrice
     additional
     order}]
  (var balance (or (and contract (xt/x:get-key contract "balance"))
                   0))
  (var buy  (or (xt/x:get-key order "buy") []))
  (var sell (or (xt/x:get-key order "sell") []))
  (return
   [:% n/View
    {:style {:flex 1}}
    [:% -/ContractSingleHeader
     #{design prediction code contract}]
    [:% n/Row {:style {:paddingTop 2}}]
    [:% n/Row
     [:% n/View
      {:style {:flex 1}}
      (j/map buy (fn:> [[rate line]]
                   [:% -/ContractSingleLine
                    {:key (+ "buy-" rate)
                     :design design
                     :line line}]))]
     [:% n/Padding {:style {:width 10}}]
     [:% n/View
      {:style {:flex 1}}
      (j/map sell (fn:> [[rate line]]
                    [:% -/ContractSingleLine
                     {:key (+ "sell-" rate)
                      :design design
                      :line line}]))]]]))

(def.js MODULE (!:module))
