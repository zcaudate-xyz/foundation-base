(ns pune.ui-section-show
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n :include [:fn [:entypo :icon]]]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [xt.event.base-form :as event-form]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-section :as ui-section]
             [melbourne.slim :as slim]]
   :export [MODULE]})

(defn.js SectionShowButton
  "creates the show button"
  {:added "0.1"}
  [#{design
     control
     routeEnabled
     routeKey
     routeShow
     routeTitle
     form}]
  (var closeFn
       (fn:>
         (jc/future-delayed [100]
           (. control (setRouteKey nil))
           (when form
             (event-form/reset-all form)))))
  (var enabled (xt/x:get-key routeEnabled (. control routeKey)))
  (var showButton
       (:? enabled
           [:% ui-button/Button
            {:design design
             :variant {:bg {:key "neutral"}}
             :key "cancel"
             :onPress closeFn
             :style {:fontSize 12
                     :textAlign "center"
                     :width 100
                     :paddingVertical 5}
             :text "Cancel"}]
           [:% ui-button/Button
            {:design design,
             :onPress (fn:> (. control (setRouteKey routeKey))),
             :style {:fontSize 12, :textAlign "center", :width 100
                     :paddingVertical 5},
             :text routeShow}]))
  (return showButton))

(defn.js SectionRoute
  "helper function for the security table"
  {:added "0.1"}
  [props]
  (var #{design
         control
         routeTitle
         routeEnabled
         children
         mini})
  (var enabled (xt/x:get-key routeEnabled (. control routeKey)))
  (var showButton (r/% -/SectionShowButton props))
  (return
   [:% ui-section/SectionFold
    #{design mini
      {:title routeTitle
       :visible enabled
       :action showButton}}
    children]))

(def.js MODULE (!:module))
