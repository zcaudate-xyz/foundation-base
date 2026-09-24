(ns pune.common.ext-util
  (:use code.test)
    (:require [lang.core :as  l]
              [std.lib :as h]))

(l/script :js
  {:require [[js.react :as r]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js useResendDelay
  "creates a resend delay"
  {:added "0.1"}
  [resend
   opts]
  (var #{[(:= delay 45)]} (or opts {}))
  (var [t] (r/useNow 1000))
  (var #{updated} (or resend {}))
  (return {:disabled (and (lib/is-number? updated)
                          (< (- t updated)
                             (* delay 1000)))
           :seconds  (Math.max (Math.ceil (- delay (/ (- t updated) 1000)))
                            0)
           :updated  updated}))

(defn.js useGroupKey
  [#{groupLu
     routeKey}]
  (var groupKey (. groupLu [routeKey]))
  (var groupRef (r/useFollowRef groupKey))
  (var [groupPrev setGroupPrev] (r/local groupKey))
  (var onComplete
       (fn []
         (setGroupPrev (r/curr groupRef))))
  (return #{groupKey
            onComplete}))

(def.js MODULE (!:module))
