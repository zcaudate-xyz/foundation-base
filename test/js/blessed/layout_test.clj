(ns js.blessed.layout-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :basic
   :require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.lib.valtio :as v]
              [js.blessed.layout :as layout]
              [js.blessed.ui-core :as ui-core]
              [js.lib.chalk :as chk]]
   :export  [MODULE]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.blessed.layout/PrimaryButton :added "4.0" :unchecked true}
(fact "creates a primary layout button"
  ^:hidden
  
  (defn.js PrimaryButtonDemo
    []
    (let [[route setRoute] (r/local "file")]
      (return
       [:% ui-core/Enclosed
        {:label "layout/PrimaryButton"}
        [:% layout/PrimaryButton
         {:top 2
          :index "f1"
          :label "File"
          :selected (== route "file")
          :route "file"
          :setRoute setRoute}]
        [:% layout/PrimaryButton
         {:top 4
          :index "f2"
          :label "Options"
          :selected (== route "options")
          :route "options"
          :setRoute setRoute}]]))))

^{:refer js.blessed.layout/layoutMenu :added "4.0" :unchecked true}
(fact "layout for menu entry"
  ^:hidden
  
  (layout/layoutMenu
   [{:index "f1"
     :label "File"
     :route "file"}
    {:index "f2"
     :label "Options"
     :route "options"}])
  #_#_=> [{"left" 0,
       "width" 12,
       "index" "f1",
       "label" "File",
       "route" "file",
       "name" "file"}
      {"left" 12,
       "width" 15,
       "index" "f2",
       "label" "Options",
       "route" "options",
       "name" "options"}])

^{:refer js.blessed.layout/layoutToggles :added "4.0" :unchecked true}
(fact "layout for toggle entry"
  ^:hidden
  
  (layout/layoutToggles
   [{:label "F"}
    {:type "separator"}
    {:label "D"}])
  #_#_=> [{"width" 3, "label" "F", "left" 0}
      {"width" 1, "type" "separator", "left" 3}
      {"width" 3, "label" "D", "left" 4}])

^{:refer js.blessed.layout/PrimaryMenu :added "4.0" :unchecked true}
(fact "creates a primary menu"
  ^:hidden
  
  (defn.js PrimaryMenuDemo
    []
    (var [route setRoute] (r/local "file"))
    (var entries (layout/layoutMenu [{:index "f1"
                                      :label "File"
                                      :route "file"}
                                      {:index "f2"
                                       :label "Options"
                                       :route "options"}]))
    (return
     [:% ui-core/Enclosed
      {:label "layout/PrimaryMenu"}
      [:box {:top 2}
       [:% layout/PrimaryMenu
        #{route
          setRoute
          {:entries entries}}]]])))

^{:refer js.blessed.layout/PrimaryToggle :added "4.0" :unchecked true}
(fact "creates a primary toggle"
  ^:hidden
  
  (defn.js PrimaryToggleDemo
    []
    (var [active setActive] (r/local true))
    (return
     [:% ui-core/Enclosed
      {:label "layout/PrimaryToggle"}
      [:box {:top 2}
       [:% layout/PrimaryToggle
        #{active setActive
          {:label "D"}}]]

      [:box {:top 4}
       [:% layout/PrimaryToggle
        #{active setActive
          {:label "F"}}]]])))

^{:refer js.blessed.layout/PrimaryToggles :added "4.0" :unchecked true}
(fact "creates primary toggles"
  ^:hidden
  
  (defn.js PrimaryTogglesDemo
    []
    (var [activeD setActiveD] (r/local false))
    (var [activeF setActiveF] (r/local true))
    (var entries (layout/layoutToggles [{:active activeD
                                     :setActive setActiveD
                                     :label "D"}
                                    {:active activeF
                                     :setActive setActiveF
                                     :label "F"}]))
    (return
     [:% ui-core/Enclosed
      {:label "layout/PrimaryToggles"}
      [:box {:top 2}
       [:% layout/PrimaryToggles #{entries}]]])))

^{:refer js.blessed.layout/SecondaryButton :added "4.0" :unchecked true}
(fact "creates a secondary button"
  ^:hidden
  
  (defn.js SecondaryButtonDemo
    []
    (var [index setIndex] (r/local 1))
    (return
     [:% ui-core/Enclosed
      {:label "layout/SecondaryButton"}
      [:box {:top 2}
       [:% layout/SecondaryButton
        {:top 0
         :label "Hello"
         :index 1
         :setIndex setIndex
         :selected (== index 1)}]
       [:% layout/SecondaryButton
        {:top 2
         :label "World"
         :index 2
         :setIndex setIndex
         :selected (== index 2)}]]])))

^{:refer js.blessed.layout/SecondaryMenu :added "4.0" :unchecked true}
(fact "creates a secondary button"
  ^:hidden
  
  (defn.js SecondaryMenuDemo
    []
    (var [index setIndex] (r/local 1))
    (return
     [:% ui-core/Enclosed
      {:label "layout/SecondaryMenu"}
      [:box {:top 2}
       [:% layout/SecondaryMenu
        #{index setIndex
          {:label ""
           :items [{:label "Hello"}
                   {:label "World"}
                   {:label "Again"}
                   {:label "Foo"}
                   {:label "Bar"}]}}]]])))

^{:refer js.blessed.layout/LayoutHeaderBlock :added "4.0" :unchecked true}
(fact "constructs the header block")

^{:refer js.blessed.layout/LayoutFooterBlock :added "4.0" :unchecked true}
(fact "constructs the footer block")

^{:refer js.blessed.layout/LayoutBodyBlock :added "4.0" :unchecked true}
(fact "constructs the body block")

^{:refer js.blessed.layout/BlankRoute :added "4.0" :unchecked true}
(fact "constructs a blank Route Page")

^{:refer js.blessed.layout/LayoutBody :added "4.0" :unchecked true}
(fact "constructs the body")

^{:refer js.blessed.layout/LayoutStatus :added "4.0" :unchecked true}
(fact "constructs the status line"
  ^:hidden
  
  (defn.js LayoutStatusDemo
    []
    (var [busy setBusy] (r/local false))
    (var [status setStatus] (r/local {:content "Hello World"
                                       :type "info"}))
    (return
     [:% ui-core/Enclosed
      {:label "layout/LayoutStatus"}
      [:box {:top 2}
       [:% layout/LayoutStatus
        #{busy setBusy
          status setStatus}]]
      [:box {:top 4}
       [:button {:mouse true
                 :keys true
                 :shrink true
                 :bg "red"
                 :content " Set Error "
                 :onClick (fn:> (setStatus {:content "Hello Error"
                                            :type "error"}))}]]
      [:box {:top 6}
       [:button {:mouse true
                 :keys true
                 :shrink true
                 :bg "yellow"
                 :content " Set Warn "
                 :onClick (fn:> (setStatus {:content "Hello Warn"
                                            :type "warn"}))}]]
      [:box {:top 8}
       [:button {:mouse true
                 :keys true
                 :shrink true
                 :bg "blue"
                 :content " Set Info "
                 :onClick (fn:> (setStatus {:content "Hello Info"
                                            :type "info"}))}]]
      [:box {:top 10}
       [:button {:mouse true
                 :keys true
                 :shrink true
                 :content (+ " Busy: " busy)
                 :onClick (fn:> (setBusy (not busy)))}]]])))

^{:refer js.blessed.layout/LayoutNotify :added "4.0" :unchecked true}
(fact "constructs the notification panel"
  ^:hidden

  (defn.js LayoutNotifyDemo
    []
    (var data {:content "Hello World"
                :type "info"
                :show true
                :layout {:width 30
                         :height 10}})
    (var [notify setNotify] (r/local data))
    (return
     [:% ui-core/Enclosed
      {:label "layout/LayoutNotify"}
      [:box {:top 2}
       [:button {:mouse true
                 :keys true
                 :shrink true
                 :bg "black"
                 :content " Notify "
                 :onClick (fn:> (setNotify data))}]
       [:% layout/LayoutNotify
        #{notify setNotify}]]])))

^{:refer js.blessed.layout/LayoutHeader :added "4.0" :unchecked true}
(fact "constructs the header")

^{:refer js.blessed.layout/LayoutFooter :added "4.0" :unchecked true}
(fact "constructs the footer")

^{:refer js.blessed.layout/LayoutMain :added "4.0" :unchecked true}
(fact "constructs the main page"
  ^:hidden
  
  (defmacro.js ^{:style/indent 1}
    content-frame
    [label & content]
    `[:box ~(cond-> {:border {:type "none"}
                     :style  {:border {:fg "clear"}}})
      ~@content])

  ;;
  ;; Starter Section
  ;;


  (defn.js Starter1Panel
    ([]
     (return (-/content-frame "Starter 1"
                              [:box "Starter 1"]))))


  (defn.js Starter2Panel
    ([]
     (return (-/content-frame "Starter 2"
                              [:box "Starter 2"]))))


  (defn.js Starter3Panel
    ([]
     (return (-/content-frame "Starter 3"
                              [:box "Starter 3"]))))


  (def.js StarterItems
    [{:label "Starter 1"  :name "section-1" :view -/Starter1Panel}
     {:label "Starter 2"  :name "section-2" :view -/Starter2Panel}
     {:label "Starter 3"  :name "section-3" :view -/Starter3Panel}])

  ;;
  ;; Alt Section
  ;;

  (defn.js Alt1Panel
    ([]
     (return (-/content-frame "Alt 1"
                              [:box "Alt 1"]))))


  (defn.js Alt2Panel
    ([]
     (return (-/content-frame "Alt 2"
                              [:box "Alt 2"]))))


  (defn.js Alt3Panel
    ([]
     (return (-/content-frame "Alt 3"
                              [:box "Alt 3"]))))


  (def.js AltItems
    [{:label "Alt 1"  :name "section-1" :view -/Alt1Panel}
     {:label "Alt 2"  :name "section-2" :view -/Alt2Panel}
     {:label "Alt 3"  :name "section-3" :view -/Alt3Panel}])
  
  (defglobal.js Route
    (v/make {:current "starter"}))
  
  (defglobal.js RouteIndex
    (v/make {}))
    
  (defn.js LayoutMainDemo
    []
    (let [[route setRoute] (v/useProxyField -/Route "current")
          [index setIndex] (v/useProxyField -/RouteIndex route)])
    (return
     [:% layout/LayoutMain
      {:route route 
       :setRoute setRoute
       :index index 
       :setIndex setIndex
       :header {:menu [{:index "f1"
                        :route "starter"
                        :label "Starter"}
                       {:index "f2"
                        :route "alt"
                        :label "Alt"}]}
       :footer {:menu [{:index "f1"
                        :route "starter"
                        :label "Starter"}
                       {:index "f2"
                        :route "alt"
                        :label "Alt"
                        :hidden true}]
                :toggle [{:label "D"
                          :active true}
                         {:label "T"
                          :active true}
                         {:label "G"
                          :active true}
                         {:type "separator"}
                         {:label "C"}]}
       :sections {:starter {:items -/StarterItems
                            :label "Starter"}
                  :alt     {:items -/AltItems
                            :label "Alt"}}}]))
  
  )

(comment
  (./import))
