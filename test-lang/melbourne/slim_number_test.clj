(ns melbourne.slim-number-test
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
             [melbourne.slim-number :as slim-number]]
   :export [MODULE]})

^{:refer melbourne.slim-number/FormSpinner :added "0.1"}
(fact "creates a Spinner"
  ^:hidden
  
  (defn.js FormSpinnerDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:price 50})
               {:price []}))
    (var [max min step decimal] [1000 0 2 2])
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-number/FormSpinner"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-number/FormSpinner
         #{max min step decimal form
           {:brand {:type "light"}
            :label "Price"
            :field "price"}}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-number/FormSpinner
         #{max min step decimal form
           {:brand {:type "dark"}
            :label "Price"
            :field "price"}}]]]))))

^{:refer melbourne.slim-number/FormSlider :added "0.1"}
(fact "creates a Slider"
  ^:hidden
  
  (defn.js FormSliderDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:price 50})
               {:price []}))
    (var [max min step decimal] [100 0 1])
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-number/FormSlider"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-number/FormSlider
         #{max min step form
           {:length 150
            :brand {:type "light"}
            :label "Price"
            :field "price"
            :fieldProps
            {:addons [#_(physical-addon/tagAll
                       {:style {:paddingHorizontal 20
                                :height 80
                                :width 400}})]}}}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-number/FormSlider
         #{max min step form
           
           {:length 150
            :brand {:type "dark"}
            :label "Price"
            :field "price"}}]]])))
  
  (def.js MODULE (!:module)))
