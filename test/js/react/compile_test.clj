(ns js.react.compile-test
  (:use code.test)
  (:require [js.react.compile :as compile]
            [js.react.compile-components :as c]))

^{:refer js.react.compile/ui-compile-layout :added "4.0"}
(fact "compiles the layout fully"
  ^:hidden
  
  (compile/ui-compile-layout
   [:ui/button
    {:name "hello"}
    [:a]]
   (c/components-expand
    {:ui/button [:ui/container
                 [:ui/text
                  {:name :props/name}
                  :props/children]]
     :ui/container [:div {:class ["p-8"]}]
     :ui/text      [:p]}))
  => [:div {:class ["p-8"]}
      [:p {:name "hello"}
       [:a]]])

^{:refer js.react.compile/ui-compile :added "4.0"}
(fact "compiles the ui"
  ^:hidden
  
  )
