(ns pune.ui-metamask-user
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [pune.ui-metamask-basic :as mm :include [:onboarding :provider]]
             [js.lib.eth-lib :as eth-lib :include [:fn]]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-section :as ui-section]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js MetamaskUser
  []
  (return [:% n/View]))

(def.js MODULE (!:module))
