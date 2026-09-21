(ns melbourne.ui-image-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn]]
             [melbourne.ui-image :as ui-image]
             [melbourne.ui-button :as ui-button]]
   :export [MODULE]})

^{:refer melbourne.ui-image/selectImage :added "0.1"}
(fact "select image from media library"
  ^:hidden
  
  (defn.js SelectImageDemo
    []
    (var [active setActive] (r/local (fn:> false)))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-image/selectImage"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-button/Button
         {:text "Click"
          :onPress (fn:> (ui-image/selectImage
                          {:setBlob console.log
                           :setPhoto console.log}))}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-button/Button
         {:text "Click"
          :onPress (fn:> (ui-image/selectImage
                          {:setBlob console.log
                           :setPhoto console.log}))}]]]))))

^{:refer melbourne.ui-image/ImagePicker :added "0.1"}
(fact "picks an image"
  ^:hidden
  
  (defn.js ImagePickerDemo
    []
    (var [active setActive] (r/local (fn:> false)))
    (var [data setData] (r/local nil))
    (var [photo setPhoto] (r/local nil))
    (var [blob setBlob] (r/local nil))
    (var uri (or (and photo (. photo ["uri"]))
                 (and data
                      (or (. data  ["url"])
                          (. data  ["thumbnailUrl"])))))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-image/ImagePicker"} 
[:% n/Row
       [:% n/Row
        {
         :style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-image/ImagePicker
         #{photo setPhoto blob setBlob data setData
           {:design {:type "light"}}}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-image/ImagePicker
         #{photo setPhoto blob setBlob data setData
           {:design {:type "dark"
                    }
            :style {:backgroundColor "blue"
                    :height 50 :width 50}}}]]] 
[:% n/TextDisplay
       {:content (+  data
                     " "
                     ;;uri
                     )}])))
  
  (defn.js ImagePickerSecondaryDemo
    []
    (var [active setActive] (r/local (fn:> false)))
    (var [data setData] (r/local nil))
    (var [photo setPhoto] (r/local nil))
    (var [blob setBlob] (r/local nil))
    (var uri (or (and photo (. photo ["uri"]))
                 (and data
                      (or (. data  ["url"])
                          (. data  ["thumbnailUrl"])))))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-image/ImagePicker"} 
[:% n/Row
       [:% n/Row
        {
         :style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-image/ImagePicker
         #{photo setPhoto blob setBlob data setData
           {:design {:type "light"
                    :mode "secondary"}
            :size 100
            :border 5}}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-image/ImagePicker
         #{photo setPhoto blob setBlob data setData
           {:design {:type "dark"
                    :mode "secondary"}
            :size 100
            :border 5}}]]] 
[:% n/TextDisplay
       {:content (+  data
                     " "
                     ;;uri
                     )}])))
  
  (def.js MODULE (!:module)))
