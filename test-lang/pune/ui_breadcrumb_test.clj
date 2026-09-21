(ns pune.ui-breadcrumb-test
  (:use code.test)
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
             [js.react :as r :include [:fn]]
             [js.react.ext-form :as ext-form]
             [js.react-native :as n :include [:fn]]
             [melbourne.ui-static :as ui-static]
             [pune.ui-breadcrumb :as ui-breadcrumb]
             [pune.ui-page :as ui-page]
             [melbourne.base-validators :as validators]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

^{:refer pune.ui-breadcrumb/Breadcrumb :added "0.1"}
(fact "constructs an empty section"
  ^:hidden
  
  (defn.js BreadcrumbDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.ui-breadcrumb/Breadcrumb"} 
[:% n/Row
       [:% n/View
        {:style {:width 300}}
        [:% ui-page/PageLayoutHeader
         {:design {:type "light"}
          :noBreadcrumb true}
         [:% ui-breadcrumb/Breadcrumb
          {:root ["HOME"]
           :path ["A" "B"]
           :design {:type "light"}}]]
        [:% ui-page/PageLayoutHeader
         {:design {:type "light"}
          :noBanner true
          :noBreadcrumb true}
         [:% ui-breadcrumb/Breadcrumb
          {:root ["HOME"]
           :path ["A" "B"]
           :noBanner true
           :design {:type "light"}}]]]
       [:% n/View
        {:style {:width 300}}
        [:% ui-page/PageLayoutHeader
         {:design {:type "dark"}
          :noBreadcrumb true}
         [:% ui-breadcrumb/Breadcrumb
          {:root ["HOME"]
           :path ["A" "B"]
           :design {:type "dark"}}]]
        [:% ui-page/PageLayoutHeader
         {:design {:type "dark"}
          :noBanner true
          :noBreadcrumb true}
         [:% ui-breadcrumb/Breadcrumb
          {:root ["HOME"]
           :path ["A" "B"]
           :noBanner true
           :design {:type "dark"}}]]]])))
  
  (def.js MODULE (!:module)))
