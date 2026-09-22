(ns melbourne.slim-image
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :playground/web-basic
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react.ext-form :as ext-form]
             [js.react-native :as n]
             [melbourne.slim-common :as slim-common]
             [melbourne.ui-image :as ui-image]
             [melbourne.base-palette :as base-palette]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [xt.event.base-form :as event-form]]
   :export [MODULE]})

(defn.js FormImage
  "constructs a image info demo"
  {:added "0.1"}
  [#{[design
      mini
      palette
      form
      meta
      label
      styleLabel
      labelHide labelNone
      field
      fieldProps
      minWidth
      (:= options [])]}]
  (var #{value result} (ext-form/listenField form field
                                             (Object.assign {:slim/type "image"
                                                        :fn/type   "field"}
                                                       meta)))
  (when (xtd/is-empty? value)
    (:= value {}))
  (when (lib/is-string? value)
    (:= value (xt/x:json-decode value)))
  
  
  (:= palette (base-palette/getPalette design palette))
  (var #{mainNeutral} palette)
  (var [data setData]   (r/local value))
  (var [photo setPhoto] (r/local))
  (var [blob setBlob]   (r/local))
  (var [waiting setWaiting] (r/local))
  #_(var uri (or (and photo (. photo ["uri"]))
               (and (xtd/not-empty? data)
                    (or (. data  ["url"])
                        (. data  ["thumbnailUrl"])))))
  (return 
   [:% slim-common/FormEnclosed
    #{design
      mini
      styleLabel
      labelHide labelNone
      label
      minWidth}
    [:% n/View
     {:style {:marginLeft 1
              :marginVertical 3}}
     [:% ui-image/ImagePicker
      #{[design
         photo setPhoto
         blob setBlob
         data setData
         waiting setWaiting
         :size (:? mini 120 140)
         (:.. fieldProps)]}]]]))

(def.js MODULE (!:module))
