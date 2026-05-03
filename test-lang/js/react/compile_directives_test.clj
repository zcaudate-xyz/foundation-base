(ns js.react.compile-directives-test
  (:require [js.react.compile-directives :as d])
  (:use code.test))

^{:refer js.react.compile-directives/compile-ui-tailwind :added "4.0" :unchecked true}
(fact "templates the layout controls"

  (d/compile-ui-tailwind
   [:*/pad {:class ["hello"]}]
   ["grow"])
  => [:div {:class ["grow" "hello"]}]

  (d/compile-ui-tailwind
   [:*/pad {:class "p-8 size-full"}]
   ["grow"])
  => [:div {:class ["grow" "p-8" "size-full"]}]

  (d/compile-ui-tailwind
   [:*/pad  {:gap 3
             :h :full}]
   ["grow"])
  => [:div {:class ["grow" "gap-3" "h-full"]}])

^{:refer js.react.compile-directives/compile-directives :added "4.0" :unchecked true}
(fact "templates the control directives"

  (d/compile-directives
   [:*/v  {:gap 3
           :h :full}]
   {})
  => [:div {:class ["flex" "flex-col" "grow" "gap-3" "h-full"]}]

  (d/compile-directives
   [:*/h  {:gap 3
           :h :full}]
   {})
  => [:div {:class ["flex" "flex-row" "grow" "gap-3" "h-full"]}]

  (d/compile-directives
   '[:*/for [[i p] pages]
     [:div
      [:h2 p.title]]]
   {})
  => '(. pages (map (fn [p i] (return [:<> {:key i} [:div [:h2 p.title]]])))))




^{:refer js.react.compile-directives/compile-directives :added "4.0" :unchecked true}
(fact "templates the control directives")
