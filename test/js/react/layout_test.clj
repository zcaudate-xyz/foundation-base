(ns js.react.layout-test
  (:use code.test)
  (:require [js.react.layout :as layout]))

^{:refer js.react.layout/ui-template-classify :added "4.0"}
(fact "classifies a hiccup element"
  ^:hidden

  (layout/ui-template-classify
   [:p]
   true)
  => {:tag :p, :props {}, :children [:*/children]}
  
  (layout/ui-template-classify
   [:p {:class "red"}]
   true)
  => {:tag :p, :props {:class "red"}, :children [:*/children]}

  (layout/ui-template-classify
   [:p [:a] [:b]]
   true)
  => {:tag :p, :props {}, :children [[:a] [:b]]}

  (layout/ui-template-classify
   [:% 'HELLO
    {:class "red"}
    [:p]
    [:p]]
   true)
  => {:tag 'HELLO,
      :props {:class "red"},
      :children [[:p] [:p]]})

^{:refer js.react.layout/ui-template-controls-layout :added "4.0"}
(fact "templates the layout controls"
  ^:hidden
  
  (layout/ui-template-controls-layout
   [:*/pad {:class ["hello"]}]
   ["grow"])
  => [:div {:class ["grow" "hello"]}]

  (layout/ui-template-controls-layout
   [:*/pad {:class "p-8 size-full"}]
   ["grow"])
  => [:div {:class ["grow" "p-8" "size-full"]}]

  (layout/ui-template-controls-layout
   [:*/pad  {:gap 3
             :h :full}]
   ["grow"])
  => [:div {:class ["grow" "gap-3" "h-full"]}])

^{:refer js.react.layout/ui-template-controls :added "4.0"}
(fact "templates the control directives"
  ^:hidden

  (layout/ui-template-controls
   [:*/v  {:gap 3
           :h :full}]
   {})
  => [:div {:class ["flex" "flex-col" "grow" "gap-3" "h-full"]}]

  (layout/ui-template-controls
   [:*/h  {:gap 3
           :h :full}]
   {})
  => [:div {:class ["flex" "flex-row" "grow" "gap-3" "h-full"]}]
  
  (layout/ui-template-controls
   '[:*/for [[i p] pages]
     [:div
      [:h2 p.title]]]
   {})
  => '(. pages (map (fn [p i] (return [:<> {:key i} [:div [:h2 p.title]]])))))

^{:refer js.react.layout/ui-template-replace :added "4.0"}
(fact "TODO")

^{:refer js.react.layout/ui-template-namespaced :added "4.0"}
(fact "template namespaced tags"
  ^:hidden
  
  (layout/ui-template-namespaced
   [:ui/paragraph {:class "red"}]
   {:ui/paragraph {:tag :p, :props {}, :children [[:a] [:b]]}})
  => [:p {:class "red"} [:a] [:b]]
  
  (layout/ui-template-namespaced
   [:ui/paragraph {:class "red"}]
   {:ui/paragraph {:tag 'Container, :props {}, :children [[:div {:class "container"}
                                                           :*/children]]}})
  => '[:% Container {:class "red"}
       [:div {:class "container"} nil]])

^{:refer js.react.layout/ui-template-components-resolve :added "4.0"}
(fact "TODO")

^{:refer js.react.layout/ui-template-components-classify :added "4.0"}
(fact "TODO")

^{:refer js.react.layout/ui-template-components-expand :added "4.0"}
(fact "TODO")

^{:refer js.react.layout/ui-template :added "4.0"}
(fact "compiling all the "
  ^:hidden
  
  (def app-components
    '{:app/top     [:div
                    {:class "flex flex-col bg-gray-200"}]
      :app/header  [:div
                    {:class ["navbar" "bg-base-100" "shadow-sm"]}]
      :app/body    [-/Body]
      :app/footer  [:nav
                    {:class ["bg-white" "shadow-md" "p-4" "sticky" "top-0" "z-50"]}
                    [:div {:class "flex items-center"}
                     [:a {:class "text-2xl font-display text-brand-dark font-bold"}
                      :*/children]]]})
  
  (layout/ui-template
   [:app/top
    [:app/header
     "HELLO WORLD"]
    [:app/body]
    [:app/footer]]
   app-components)
  => '[:div {:class "flex flex-col bg-gray-200"}
       [:<>
        [:div {:class ["navbar" "bg-base-100" "shadow-sm"]}
         [:<> "HELLO WORLD"]]
        [:% -/Body {} nil]
        [:nav {:class ["bg-white" "shadow-md" "p-4" "sticky" "top-0" "z-50"]}
         [:div {:class "flex items-center"}
          [:a {:class "text-2xl font-display text-brand-dark font-bold"} nil]]]]])
