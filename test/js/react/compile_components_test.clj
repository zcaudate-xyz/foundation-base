(ns js.react.compile-components-test
  (:use code.test)
  (:require [js.react.compile-components :as c]))

^{:refer js.react.compile-components/classify-tagged :added "4.0"}
(fact "classifies the hiccup form"
  ^:hidden
  
  (c/classify-tagged
   [:ui/container
    [:ui/text
     {:name :props/name}]])
  => {:tag :ui/container, :props {}, :children [[:ui/text {:name :props/name}]]}

  (c/classify-tagged
   [:div]
   true)
  => {:tag :div, :props {}, :children [:props/children]})

^{:refer js.react.compile-components/components-resolve :added "4.0"}
(fact "resolve component map"
  ^:hidden

  (c/components-resolve {:ui/button    [:ui/container
                                        [:ui/text
                                         {:name :props/name}]]
                         :ui/container [:div]
                         :ui/text      [:p]})
  => {:ui/button    [:ui/container
                     [:ui/text
                      {:name :props/name}]]
      :ui/container [:div]
      :ui/text      [:p]}


  (def *comp-1* {:ui/container [:div]
                 :ui/text      [:p]})
  (def *comp-2* {:ui/button    [:ui/container
                                [:ui/text
                                 {:name :props/name}]]})
  (c/components-resolve '*comp-1*)
  => {:ui/container [:div], :ui/text [:p]}

  (c/components-resolve '(merge *comp-2*
                                *comp-1*))
  => {:ui/button    [:ui/container
                     [:ui/text
                      {:name :props/name}]]
      :ui/container [:div]
      :ui/text      [:p]})

^{:refer js.react.compile-components/components-find-deps :added "4.0"}
(fact "creates a dependency tree for component map"
  ^:hidden
  
  (c/components-find-deps
   (c/components-expand
    {:ui/button    [:ui/container
                    [:ui/text
                     {:name :props/name}]]
     :ui/container [:div]
     :ui/text      [:p]}))
  => {:ui/button {:deps #{:ui/container :ui/text}},
      :ui/container {:deps #{}},
      :ui/text {:deps #{}}})

^{:refer js.react.compile-components/components-expand :added "4.0"}
(fact "expands a vector component to standardised form"
  ^:hidden
  
  (c/components-expand {:ui/button    [:ui/container
                                       [:ui/text
                                        {:name :props/name}]]
                        :ui/container [:div]
                        :ui/text      [:p]})
  => {:ui/button {:tag :ui/container,
                  :props {},
                  :children [[:ui/text {:name :props/name}]]
                  :deps #{:ui/container :ui/text}},
      :ui/container {:tag :div,
                     :props {},
                     :children [:props/children]
                     :deps #{}},
      :ui/text {:tag :p, :props {}, :children [:props/children]
                :deps #{}}})

^{:refer js.react.compile-components/compile-replace :added "4.0"}
(fact "compiles the layout with parameters"
  ^:hidden
  
  (c/compile-replace [:ui/container
                      [:ui/text
                       {:name :props/name}
                       :props/children]]
                     {:props/name "hello"}
                     [[:a]])
  => [:ui/container [:ui/text {:name "hello"} [:a]]])

^{:refer js.react.compile-components/find-namespaced-props :added "4.0"}
(fact "finds all the :prop/<val> keys"
  ^:hidden
  
  (c/find-namespaced-props
   [[:ui/text
     {:name :props/name}]])
  => #{:props/name})

^{:refer js.react.compile-components/get-tmpl-props :added "4.0"}
(fact "gets template and body props given input"
  ^:hidden
  
  (c/get-tmpl-props {:name "hello"
                     :location "home"}
                    [:props/name])
  => [{:props/name "hello"} {:location "home"}])

^{:refer js.react.compile-components/getter-symbol :added "4.0"}
(fact "creates the getter symbol"
  ^:hidden

  (c/getter-symbol :var/history-idx)
  => 'historyIdx

  (c/getter-symbol :var/error)
  => 'error)

^{:refer js.react.compile-components/setter-symbol :added "4.0"}
(fact "creates the setter symbol"
  ^:hidden
  
  (c/setter-symbol :var/history-idx)
  => 'setHistoryIdx

  (c/setter-symbol :var/error)
  => 'setError)

^{:refer js.react.compile-components/compile-walk-variables :added "4.0"}
(fact "replace :var/<name> as react states"
  ^:hidden

  (c/compile-walk-variables
   '(do (:var/showDisplay true)
        (helloWorld :var/showDisplay)))
  => '(do (setShowDisplay true)
          (helloWorld showDisplay)))

^{:refer js.react.compile-components/compile-element-action-do :added "4.0"}
(fact "does a do block"
  ^:hidden

  (c/compile-element-action-do
   '(do (:var/showDisplay true)
        (helloWorld :var/showDisplay))
   {})
  => '(do (setShowDisplay true)
          (helloWorld showDisplay)))

^{:refer js.react.compile-components/compile-element-action-set :added "4.0"}
(fact "compiles the :%/set action"
  ^:hidden
  
  (c/compile-element-action-set
   :var/history-idx
   '{:from (base-fn :var/dsl-code)
     :transform transform-fn})
  => '(setHistoryIdx (transform-fn (base-fn dslCode))))

^{:refer js.react.compile-components/compile-element-action-set-async :added "4.0"}
(fact "compiles the :%/set-async action"
  ^:hidden
  
  (c/compile-element-action-set-async
   :var/history-idx
   '{:from (async-fn :var/dsl-code)
     :pending :var/pending
     :error   :var/error
     :transform (fn [])})
  => '(do (setPending true)
        (. (async-fn dslCode)
           (then (fn [res] (return ((fn []) res))))
           (then (fn [res] (setHistoryIdx res)))
           (catch (fn [err] (setError err)))
           (finally (fn [] (setPending false))))))

^{:refer js.react.compile-components/compile-element-actions :added "4.0"}
(fact "compiles the element actions"
  ^:hidden
  
  (c/compile-element-actions
   '[{:%/set-async :var/output
      :from (async-fn :var/dsl-code)
      :pending :var/pending
      :error   :var/error
      :transform (fn [])}
     {:%/set :var/data
      :from (base-fn :var/input)
      :transform transform-fn}])
  => '(fn []
        (do (setPending true)
            (. (async-fn dslCode)
               (then (fn [res] (return ((fn []) res))))
               (then (fn [res] (setOutput res)))
               (catch (fn [err] (setError err)))
               (finally (fn [] (setPending false)))))
        (setData (transform-fn (base-fn input)))))

^{:refer js.react.compile-components/compile-element-directives :added "4.0"}
(fact "compiles element directives"
  ^:hidden

  (c/compile-element-directives
   {:type :input
    :get {:key :value}
    :set {:key :onChange
          :transform '(fn [e]
                        (return
                         e.target.value))}}
   {:%/value :var/output})
  => '{:value output,
       :onChange (fn [input] (setOutput ((fn [e] (return e.target.value)) input)))}

  (c/compile-element-directives
   {:type :action
    :key :onPress}
   '{:%/action [{:%/set-async :var/output
                  :from (async-fn :var/dsl-code)
                  :pending :var/pending
                  :error   :var/error
                  :transform (fn [])}]})
  => '{:onPress (fn []
                  (do (setPending true)
                      (. (async-fn dslCode)
                         (then (fn [res] (return ((fn []) res))))
                         (then (fn [res] (setOutput res)))
                         (catch (fn [err] (setError err)))
                         (finally (fn [] (setPending false))))))})

^{:refer js.react.compile-components/compile-element :added "4.0"}
(fact "expands the template"
  ^:hidden
  
  (c/compile-element
   [:ui/button {:name "hello"} [:a]]
   (c/components-expand
    {:ui/button [:ui/container
                 [:ui/text
                  {:name :props/name}
                  :props/children]]
     :ui/container [:div]
     :ui/text      [:p]}))
  => [:ui/container {} [:ui/text {:name "hello"} [:a]]])

^{:refer js.react.compile-components/compile-element-loop :added "4.0"}
(fact "will loop until there are no dependencies"
  ^:hidden
  
  (c/compile-element-loop
   [:ui/button {:name "hello"} [:a]]
   (c/components-expand
    {:ui/button [:ui/container
                 [:ui/text
                  {:name :props/name}
                  :props/children]]
     :ui/container [:div]
     :ui/text      [:p]})
   c/compile-element)
  => [:div {} [:ui/text {:name "hello"} [:a]]]

  (c/compile-element-loop
   [:ui/toolbar.history-clear]
   (c/components-expand
    {:ui/toolbar.history-clear
     [:ui/button
      {:%/action [{:%/set :var/combined
                    :from 'i}]}
      [:ui/text ]]
     :ui/button    {:tag :button
                    :props {:class ["p-8"]}
                    :children [:props/children]
                    :view {:type :action
                           :key :onClick}}
     :ui/container [:div]
     :ui/text      [:p]})
   c/compile-element)
  => '[:button
       {:class ["p-8"],
        :onClick (fn [] (setCombined i))} [:ui/text]]



  (c/compile-element-loop
   [:ui/toolbar.history-clear
    {:%/actions [{:%/set :var/combined
                  :from 'i}]}]
   (c/components-expand
    {:ui/toolbar.history-clear [:ui/button
                                [:ui/text ]]
     :ui/button    {:tag :button
                    :props {:class ["p-8"]}
                    :children [:props/children]
                    :view {:type :action
                           :key :onClick}}
     :ui/container [:div]
     :ui/text      [:p]})
   c/compile-element))
