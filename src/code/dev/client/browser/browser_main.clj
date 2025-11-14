(ns code.dev.client.browser.browser-main
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]
             [js.react.ext-box :as box]
             [js.lib.puck :as puck]
             [js.lib.radix :as rx]
             [code.dev.client.ui-global :as global]
             [code.dev.client.ui-common :as ui]]})

(def.js config
  {:components
   {:HeadingBlock
    {:fields
     {:children
      {:type "text"}}}
    :render
    (fn [#{children}]
      (return
       [:h1 children]))}})

(def.js initialData {})

(def.js save
  (fn [data]
    (return nil)))

(defn.js BrowserMain
  []
  (return
   #_[:button "hello"]
   [:% (. rx/Button render) "HELLO"])
  )
