(ns js.react.compile-test
  (:use code.test)
  (:require [js.react.compile :as compile]
            [js.react.compile-components :as c]))

^{:refer js.react.compile/check-valid-variables :added "4.0"}
(fact "checks that all :var/<name> keywords have been defined"
  ^:hidden

  (compile/check-valid-variables
   {:var/a true
    :var/b true}
   [:var/a])
  => [:var/a]
  
  (compile/check-valid-variables
   {:var/a true
    :var/b true}
   :var/c)
  => (throws))

^{:refer js.react.compile/compile-states-deps :added "4.0"}
(fact "get dependencies for states"
  ^:hidden
  
  (compile/compile-states-deps
   '{:var/html-code   ""
     :var/dsl-code    {:%/args  [""]}
     
     :var/history     {:%/fn   useLocalHistory
                       :%/args ["task.translate-html"]}
     :var/history-idx 0
     :var/data        {:errored false
                       :warning false}
     :var/combined    {:%  (+ :var/dsl-code
                              :var/html-code)}})
  => #:var{:html-code #{}, :dsl-code #{}, :history #{}, :history-idx #{}, :data #{}, :combined #{:var/html-code :var/dsl-code}})

^{:refer js.react.compile/compile-states :added "4.0"}
(fact "compiles to react code"
  ^:hidden
  
  (compile/compile-states
   '{:var/html-code   ""
     :var/dsl-code    {:%/args  [""]}
     
     :var/history     {:%/fn   useLocalHistory
                       :%/args ["task.translate-html"]}
     :var/history-idx 0
     :var/data        {:errored false
                       :warning false}
     :var/combined    {:%  (+ :var/dsl-code
                              :var/html-code)}})
  
  => '((var [data setData] (React.useState {:errored false, :warning false}))
       (var [historyIdx setHistoryIdx] (React.useState 0))
       (var [history setHistory] (useLocalHistory "task.translate-html"))
       (var [dslCode setDslCode] (React.useState ""))
       (var [htmlCode setHtmlCode] (React.useState ""))
       (var combined (+ dslCode htmlCode))))

^{:refer js.react.compile/compile-layout-raw :added "4.0"}
(fact "compiles the layout fully"
  ^:hidden
  
  (compile/compile-layout-raw
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

^{:refer js.react.compile/compile-layout :added "4.0"}
(fact "compiles the ui"
  ^:hidden
  
  (compile/compile-layout
   [:ui/button
    {:name "hello"}
    [:a]]
   {:ui/button [:ui/container
                [:ui/text
                 {:name :props/name}
                 :props/children]]
    :ui/container [:div {:class ["p-8"]}]
    :ui/text      [:p]})
  => [:div {:class ["p-8"]} [:p {:name "hello"} [:a]]])

^{:refer js.react.compile/compile-full :added "4.0"}
(fact "TODO"
  ^:hidden

  (compile/compile-full
   
   '{:var/html-code   ""
     :var/dsl-code    {:%/args  [""]}
     
     :var/history     {:%/fn   useLocalHistory
                       :%/args ["task.translate-html"]}
     :var/history-idx 0
     :var/data        {:errored false
                       :warning false}
     :var/combined    {:%  (+ :var/dsl-code
                              :var/html-code)}}
   [:ui/button
    {:name :var/html-code}
    [:a]]
   
   {:ui/button [:ui/container
                [:ui/text
                 {:name :props/name}
                 :props/children]]
    :ui/container [:div {:class ["p-8"]}]
    :ui/text      [:p]})
  => '(do (var [data setData]
               (React.useState {:errored false
                                :warning false}))
          (var [historyIdx setHistoryIdx]
               (React.useState 0))
          (var [history setHistory]
               (useLocalHistory "task.translate-html"))
          (var [dslCode setDslCode]
               (React.useState ""))
          (var [htmlCode setHtmlCode]
               (React.useState ""))
          (var combined
               (+ dslCode htmlCode))
          (return [:div {:class ["p-8"]}
                   [:p {:name htmlCode} [:a]]])))


^{:refer js.react.compile/get-registry :added "4.0"}
(fact "gets the component registry")

^{:refer js.react.compile/put-registry :added "4.0"}
(fact "puts a component group into the registry")

^{:refer js.react.compile/del-registry :added "4.0"}
(fact "deletes a component group from the registry")

^{:refer js.react.compile/get-default-components :added "4.0"}
(fact "emits using a potentially cached entry")

^{:refer js.react.compile/compile-replace-actions :added "4.0"}
(fact "compiles to react code")

^{:refer js.react.compile/compile-triggers :added "4.0"}
(fact "compiles to react code")