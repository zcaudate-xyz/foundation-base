(ns std.dom.component-test
  (:require [std.dom.common :as base]
            [std.dom.component :refer :all]
            [std.dom.impl :as impl]
            [std.dom.local :as local]
            [std.dom.mock :as mock]
            [std.dom.react :as react]
            [std.dom.type :as type])
  (:use code.test))

(defcomp :mock/pane-static
  [:static]
  ([_ {:keys [children]}]
   (base/dom-compile
    (apply vector :mock/pane {:class :pane} children))))

(defcomp :mock/box-static
  [:static]
  ([_ {:keys [title contents]}]
   (base/dom-compile
    [:mock/pane {:class :box}
     [:mock/label title]
     [:mock/label contents]])))

(defcomp :mock/box-local
  [:local {:initial {:title "TITLE"
                     :contents "CONTENTS"}}]
  ([_ _]
   (base/dom-compile
    [:mock/pane {:class :box}
     [:mock/label (:title (local/local :title))]
     [:mock/label (:contents (local/local :contents))]])))

(defcomp :mock/box-react
  [:react]
  ([_ {:keys [state]}]
   (base/dom-compile
    [:mock/pane {:class :box}
     [:mock/label (:title (react/react state))]
     [:mock/label (:contents (react/react state))]])))

(defcomp :mock/pane-react
  [:react]
  ([_ {:keys [choose s1 s2]}]
   (base/dom-compile
    [:mock/pane-static
     (case (react/react choose)
       :s1 [:mock/box-react {:state s1}]
       :s2 [:mock/box-react {:state s2}])])))


^{:refer std.dom.component/dom-component? :added "3.0"}
(fact "checks if dom is that of a component"

  (dom-component? (base/dom-compile [:mock/pane-static]))
  => true)

^{:refer std.dom.component/component-options :added "3.0"}
(fact "prepare component options given template and mixin maps"

  (component-options (fn [dom props]
                       (base/dom-compile [:mock/label "hello"]))
                     [{:pre-render (fn [dom] dom)}
                      {:post-render (fn [dom] dom)}])
  => (contains {:pre-render fn?
                :post-render fn?
                :template fn?}))

^{:refer std.dom.component/component-install :added "3.0"}
(fact "installs component given template and mixins"

  (component-install :mock/box-static
                     :static
                     (fn [_ {:keys [title contents]}]
                      (base/dom-compile
                       [:mock/pane {:class :box}
                        [:mock/label title]
                        [:mock/label contents]]))
                     {})
  => (contains-in [:mock/box-static {:template fn?,
                                     :tag :mock/box-static,
                                     :metaclass :dom/component,
                                     :metatype :dom/component,
                                     :class :static}]))

^{:refer std.dom.component/defcomp :added "3.0"}
(fact "defines a component"

  (defcomp :mock/box-local
    [:local {:initial {:title "TITLE"
                       :contents "CONTENTS"}}]
    ([_ _]
     (base/dom-compile
      [:mock/pane {:class :box}
       [:mock/label (:title (local/local :title))]
       [:mock/label (:contents (local/local :contents))]])))
  => (contains-in [:mock/box-local
                   {:initial {:title "TITLE",
                              :contents "CONTENTS"},
                    :pre-render fn?,
                    :pre-remove fn?,
                    :template fn?,
                    :tag :mock/box-local,
                    :metatype :dom/component,
                    :metaclass :dom/component,
                    :class :local}]))

^{:refer std.dom.component/dom-render-component :added "3.0"}
(fact "component dom element rendering function"

  (-> (base/dom-compile [:mock/pane-static
                         [:mock/box-static {:title "a" :content "A"}]
                         [:mock/box-static {:title "b" :content "B"}]])
      (dom-render-component)
      (base/dom-format))
  => [:+ :mock/pane-static
      [:+ :mock/box-static {:title "a", :content "A"}]
      [:+ :mock/box-static {:title "b", :content "B"}]])

^{:refer std.dom.component/child-components :added "3.0"}
(fact "collects child components of component tree"

  (->> (base/dom-compile
        [:mock/pane-static
         [:mock/box-static {:title "a" :content "A"}]
         [:mock/label "hello"]])
       dom-render-component
       :shadow
       (child-components)
       (map base/dom-format))
  => [[:+ :mock/box-static {:title "a", :content "A"}]])

^{:refer std.dom.component/dom-remove-component :added "3.0"}
(fact "removes rendered component from dom"

  (-> (base/dom-compile [:mock/box-local])
      (dom-render-component)
      (dom-remove-component)
      (base/dom-format))
  => [:+ :mock/box-local])

^{:refer std.dom.component/dom-ops-component :added "3.0"}
(fact "constructs transform operations for component dom"

  (dom-ops-component :mock/box-static {} {})
  => []

  (dom-ops-component :mock/box-static {} {:a 1})
  => (contains-in [[:refresh base/dom?]])

  (dom-ops-component :mock/box-local {} {})
  => [])

^{:refer std.dom.component/dom-apply-component :added "3.0"}
(fact "applies operations to component dom"

  (-> (base/dom-compile [:mock/box-static])
      (impl/dom-render)
      (dom-apply-component
       [[:refresh (base/dom-compile
                   [:mock/box-static {:a 1}])]])
      (base/dom-format))
  => [:+ :mock/box-static {:a 1}])

^{:refer std.dom.component/dom-replace-component :added "3.0"}
(fact "default replace operation for components"

  (-> (base/dom-compile [:mock/box-static])
      (impl/dom-render)
      (impl/dom-replace (base/dom-compile
                         [:mock/box-static {:a 1}]))
      (base/dom-format))
  => [:+ :mock/box-static {:a 1}])

^{:refer std.dom.component/dom-state-handler :added "3.0"}
(fact "updates the state with given value"

  (dom-state-handler nil {:state (atom {})
                          :cursor [:data]
                          :new "hello"
                          :transform keyword})
  => {:data :hello})

(comment
  (def -choose- (atom :s1))

  (def -s1- (atom {:title "s1" :contents "S1"}))

  (def -s2- (atom {:title "s2" :contents "S2"}))

  (def -dom- (-> (impl/dom-compile [:mock/pane-react {:choose -choose-
                                                      :s1 -s1-
                                                      :s2 -s2-}])
                 (impl/dom-render)))


  (reset! -choose- :s1)
  (impl/dom-elem -dom-))

(comment

  (doto (base/dom-compile [:mock/box-local])
    (impl/dom-render)
    (-> :shadow prn)
    (-> :cache :local/state (reset! {:title "title" :contents "contents"}))
    (-> :shadow prn))

  (-> (impl/dom-compile [:mock/pane-static
                         [:mock/pane-static
                          [:mock/label "hello"]
                          [:mock/label "world"]]])
      (impl/dom-render)
      (impl/dom-elem))
  => [:mock/pane {:class :pane}
      [:mock/pane {:class :pane}
       [:mock/label "hello"]
       [:mock/label "world"]]]

  (-> (impl/dom-compile [:mock/pane-static
                         [:mock/pane-static
                          [:mock/pane-static
                           [:mock/label "hello"]
                           [:mock/label "world"]]]])
      (impl/dom-render)
      (impl/dom-elem))
  => [:mock/pane {:class :pane}
      [:mock/pane {:class :pane}
       [:mock/pane {:class :pane}
        [:mock/label "hello"]
        [:mock/label "world"]]]]

  (->
   (impl/dom-render)
      :shadow)



  (meta/elem-meta :mock/box-local)


  (dom-component? (impl/dom-compile [:mock/pane-static]))

  (meta/elem-meta (:tag (impl/dom-compile [:mock/pane-static])))
  => {:template fn?,
      :tag :mock/pane-static,
      :type :component,
      :react :static}

  (-> (impl/dom-compile [:mock/pane-static])
      (impl/dom-init)))

(comment
  (./import)
  (./scaffold)
  (./arrange))
