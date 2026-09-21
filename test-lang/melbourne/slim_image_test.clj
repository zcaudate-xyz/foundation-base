(ns melbourne.slim-image-test
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
   :require [[js.react-native :as n :include [:fn]]
             [js.react.ext-form :as ext-form]
             [melbourne.slim-image :as slim-image]]
   :export [MODULE]})

^{:refer melbourne.slim-image/FormImage :added "0.1"}
(fact "constructs a image info demo"
  ^:hidden
  
  (defn.js FormImageDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:price nil})
               {:price []}))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-image/FormImage"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-image/FormImage
         {:brand {:type "light"}
          :label "Picture"
          :form form
          :field "price"}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-image/FormImage
         {:brand  {:type "dark"}
          :label "Picture"
          :form form
          :field "price"}]]])))
  
  (def.js MODULE (!:module))
  )
