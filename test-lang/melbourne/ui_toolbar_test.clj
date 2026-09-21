(ns melbourne.ui-toolbar-test
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
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [melbourne.ui-toolbar :as ui-toolbar]
             [melbourne.ui-text :as ui-text]]
   :export [MODULE]})

^{:refer melbourne.ui-toolbar/minorStandard :added "0.1"}
(fact "style for standard minor buttons")

^{:refer melbourne.ui-toolbar/accentStandard :added "0.1"}
(fact "style for standard accent button")

^{:refer melbourne.ui-toolbar/accentToggleSubtle :added "0.1"}
(fact "style for accent subtle toggle")

^{:refer melbourne.ui-toolbar/accentToggleDeep :added "0.1"}
(fact "style for accent deep toggle")

^{:refer melbourne.ui-toolbar/accentTabsSubtle :added "0.1"}
(fact "style for accent subtle tabs")

^{:refer melbourne.ui-toolbar/minorNoBanner :added "0.1"}
(fact "style for no banner minor button")

^{:refer melbourne.ui-toolbar/accentNoBanner :added "0.1"}
(fact "style for no banner accent button")

^{:refer melbourne.ui-toolbar/ToolbarOverlayTooltip :added "0.1"}
(fact "creates the toolbar overlay ()"
  ^:hidden
  
  (defn.js ToolbarOverlayTooltipDemo
    []
    (var [visible setVisible] (r/local false))
    (var hostRef (r/ref))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.ui-toolbar/ToolbarOverlayTooltip"
        :style {:height 100}} 
[:% ui-toolbar/Toolbar
        #{
          {:design {:type "dark"}}}
        [:% ui-text/ToggleAccent
         {:design {:type "dark"}
          :variant (ui-toolbar/accentStandard)
          :text "OVERLAY"
          :selected visible
          :onPress (fn:> (setVisible (not visible)))}]] 
[:% n/View {:ref hostRef}] 
[:% ui-toolbar/ToolbarOverlayTooltip
        #{visible
          setVisible
          hostRef
          {:design {:type "dark"}}}
        [:% n/View
         {:style {:height 50
                  :width 300
                  :backgroundColor "red"}}]])])))

^{:refer melbourne.ui-toolbar/ToolbarOverlay :added "0.1"}
(fact "creates the toolbar overlay"
  ^:hidden
  
  (defn.js ToolbarOverlayDemo
    []
    (var [visible setVisible] (r/local false))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.ui-toolbar/ToolbarOverlay"
        :style {:height 100}} 
[:% ui-toolbar/Toolbar
        {:design {:type "dark"}}
        [:% ui-text/ToggleMinor
         {:design {:type "dark"}
          :variant (ui-toolbar/minorStandard)
          :text "OVERLAY"
          :selected visible
          :onPress (fn:> (setVisible (not visible)))}]] 
[:% ui-toolbar/ToolbarOverlay
        #{visible
          setVisible
          {:design {:type "dark"}}}
        [:% n/View
         {:style {:height 50
                  :width 300
                  :backgroundColor "red"}}]])])))

^{:refer melbourne.ui-toolbar/Toolbar :added "0.1"}
(fact "creates the toolbar overlay ()"
  ^:hidden
  
  (defn.js ToolbarDemo
    []
    (var [value setValue] (r/local))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-toolbar/Toolbar"} 
[:% ui-toolbar/Toolbar
       {:design {:type "dark"}}
       [:% n/View
        {:style {:padding 10}}
        [:% ui-text/TabsMinor
         #{value setValue
           {:design {:type "dark"}
            :variant (ui-toolbar/minorStandard)
            :data ["ABC" "EFG" "HIJ"]}}]]
       [:% n/View
        {:style {:padding 10}}
        [:% ui-text/TabsAccent
         #{value setValue
           {:design {:type "dark"}
            :variant (ui-toolbar/accentStandard)
            :data ["ABC" "EFG" "HIJ"]}}]]] 
[:% ui-toolbar/Toolbar
       {:design {:type "dark"}
        :noBanner true}
       [:% n/View
        {:style {:padding 10}}
        [:% ui-text/TabsMinor
         #{value setValue
           {:design {:type "dark"}
            :variant (ui-toolbar/minorNoBanner)
            :data ["ABC" "EFG" "HIJ"]}}]]
       [:% n/View
        {:style {:padding 10}}
        [:% ui-text/TabsAccent
         #{value setValue
           {:design {:type "dark"}
            :variant (ui-toolbar/accentNoBanner)
            :data ["ABC" "EFG" "HIJ"]}}]]] 
[:% n/TextDisplay
       #{value}]))))

^{:refer melbourne.ui-toolbar/ToolbarAnnex :added "0.1"}
(fact "creates a toolbar annex"
  ^:hidden
  
  (defn.js ToolbarAnnexDemo
    []
    (var [visible setVisible] (r/local false))
    (var [mini setMini]  (r/local true))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.ui-toolbar/ToolbarAnnex"
        :style {:height 100}} 
[:% n/Row
        [:% n/Button
         {:title "Open"
          :onPress (fn:> (setVisible true))}]
        [:% n/Button
         {:title (+ "Mini " mini)
          :onPress (fn:> (setMini (not mini)))}]] 
[:% ui-toolbar/ToolbarAnnex
        #{visible
          setVisible
          {:component (fn:> [#{onClose}]
                        [:% n/Row
                         [:% n/View
                          {:style {:height 50
                                   :width 300
                                   :backgroundColor "red"}}
                          
                          [:% n/Button
                           {:title "Close"
                            :onPress onClose}]]])
           :mini mini
           :design {:type "dark"}}}])]))
  
  (def.js MODULE (!:module)))
