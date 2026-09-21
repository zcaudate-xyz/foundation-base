(ns melbourne.slim-entry-test
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
                     [js.react.ext-route :as ext-route]
                     [melbourne.ui-static :as ui-static]
                     [melbourne.slim-entry :as slim-entry]
                     [melbourne.base-validators :as validators]
                     [xt.lang.spec-base :as xt]
                     [xt.lang.common-lib :as lib]
                     [xt.lang.common-data :as data]
                     [xt.lang.common-string :as string]
                     [xt.lang.common-math :as math]
                     [xt.lang.common-tree :as tree]
                     [xt.lang.common-sort-by :as sort-by]
                     [xt.lang.common-trace :as trace]
                     [xt.event.base-form :as event-form]
                     [xt.event.base-route :as event-route]]
           :export [MODULE]})

^{:refer melbourne.slim-entry/EntryImplNotFound :added "4.0"}
(fact "creates a not found entry")

^{:refer melbourne.slim-entry/EntryFree :added "4.0"}
(fact "creates a free component"
  ^:hidden
  
  (defn.js EntryFreeDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryFree"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryFree
         {:design {:type "light"}
          :entry  entry
          :impl   {:key "title"
                   :component slim-entry/EntryContentTitle
                   :template ["currency_id"]}
          :custom {:title {:style  {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry  entry
          :impl   {:type "free"
                   :key "title"
                   :component slim-entry/EntryContentParagraph
                   :template ["currency_id"]}
          :custom {:title   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentRaw :added "4.0"}
(fact "creates a raw content view"
  ^:hidden
  
  (defn.js EntryContentRawDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentRaw"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentRaw
         {:design {:type "light"}
          :entry  entry}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :impl {:type "raw"}
          :entry  entry}]]]))))

^{:refer melbourne.slim-entry/EntryContentRawForm :added "4.0"}
(fact "creates a raw content view"
  ^:hidden
  
  (defn.js EntryContentRawFormDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:currency-id "STATS"
                      :balance 1000
                      :escrow 50.5})
               {}))
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentRawForm"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentRawForm
         {:design {:type "light"}
          :form form}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :impl {:type "raw_form"}
          :form form}]]]))))

^{:refer melbourne.slim-entry/EntryContentFill :added "4.0"}
(fact "adds space filler for layouts"
  ^:hidden
  
  (defn.js EntryContentFillDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentFill"} 
      [:% n/Row
       {:style {:height 30}}
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentFill
         {:design {:type "light"}
          :entry  entry}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :impl {:type "fill"}
          :entry  entry}]]]))))

^{:refer melbourne.slim-entry/entryLayoutDiv :added "4.0"}
(fact "creates a layout component")

^{:refer melbourne.slim-entry/EntryLayoutHorizontal :added "4.0"}
(fact "creates a horizontal layout"
  ^:hidden
  
  (defn.js EntryLayoutHorizontalDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutHorizontal"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryLayoutHorizontal
         {:design  {:type "light"}
          :impl {:body  [[:% slim-entry/EntryContentTitle
                          {:key "title"
                           :design {:type "light"}
                           :entry  entry
                           :impl   {:key "title"
                                    :template ["currency_id"]}
                           :custom {:title {:style  {:padding 10}}}}]
                         [:% slim-entry/EntryContentParagraph
                          {:key "body"
                           :design  {:type "light"}
                           :entry   entry
                           :impl    {:key "body"
                                     :template ["balance"]}
                           :custom  {:body   {:style {:padding 10}}}}]]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :custom  {:title {:style  {:padding 10}}
                    :body   {:style {:padding 10}}}
          :entry  entry
          :impl {:type "h"
                 :body  [{:type "title"
                          :key "title"
                          :template ["currency_id"]}
                         {:type "p"
                          :key "body"
                          :template ["balance"]}]}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutVertical :added "4.0"}
(fact "creates a vertical layout"
  ^:hidden
  
  (defn.js EntryLayoutVerticalDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutVertical"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryLayoutVertical
         {:design  {:type "light"}
          :impl {:body  [[:% slim-entry/EntryContentTitle
                          {:key "title"
                           :design {:type "light"}
                           :entry  entry
                           :impl   {:key "title"
                                    :template ["currency_id"]}
                           :custom {:title {:style  {:padding 10}}}}]
                         [:% slim-entry/EntryContentParagraph
                          {:key "body"
                           :design  {:type "light"}
                           :entry   entry
                           :impl    {:key "body"
                                     :template ["balance"]}
                           :custom  {:body   {:style {:padding 10}}}}]]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :custom  {:title {:style  {:padding 10}}
                    :body   {:style {:padding 10}}}
          :entry  entry
          :impl {:type "v"
                 :body  [{:type "title"
                          :key "title"
                          :template ["currency_id"]}
                         {:type "p"
                          :key "body"
                          :template ["balance"]}]}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutEnclosed :added "4.0"}
(fact "creates an enclosed layout"
  ^:hidden
  
  (defn.js EntryLayoutEnclosedDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutEnclosed"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryLayoutEnclosed
         {:design  {:type "light"}
          :impl {:label "ENCLOSED"
                 :body  [[:% slim-entry/EntryContentTitle
                          {:key "title"
                           :design {:type "light"}
                           :entry  entry
                           :impl   {:key "title"
                                    :template ["currency_id"]}
                           :custom {:title {:style  {:padding 10}}}}]
                         [:% slim-entry/EntryContentParagraph
                          {:key "body"
                           :design  {:type "light"}
                           :entry   entry
                           :impl    {:key "body"
                                     :template ["balance"]}
                           :custom  {:body   {:style {:padding 10}}}}]]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :custom  {:title {:style  {:padding 10}}
                    :body   {:style {:padding 10}}}
          :entry  entry
          :impl {:type "enclosed"
                 :label "ENCLOSED"
                 :body  [{:type "title"
                          :key "title"
                          
                          :template ["currency_id"]}
                         {:type "p"
                          :key "body"
                          :template ["balance"]}]}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutPortal :added "4.0"}
(fact "creates an portal layout"
  ^:hidden
  
  (defn.js EntryLayoutPortalDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutPortal"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% n/PortalSink
         {:name "light"}]
        [:% slim-entry/EntryLayoutPortal
         {:design  {:type "light"}
          :impl {:target "light"
                 :body  [[:% slim-entry/EntryContentTitle
                          {:key "title"
                           :design {:type "light"}
                           :entry  entry
                           :impl   {:key "title"
                                    :template ["currency_id"]}
                           :custom {:title {:style  {:padding 10}}}}]
                         [:% slim-entry/EntryContentParagraph
                          {:key "body"
                           :design  {:type "light"}
                           :entry   entry
                           :impl    {:key "body"
                                     :template ["balance"]}
                           :custom  {:body   {:style {:padding 10}}}}]]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% n/PortalSink
         {:name "dark"}]
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :custom  {:title {:style  {:padding 10}}
                    :body   {:style {:padding 10}}}
          :entry  entry
          :impl {:type "portal"
                 :target "dark"
                 :body  [{:type "title"
                          :key "title"
                          
                          :template ["currency_id"]}
                         {:type "p"
                          :key "body"
                          :template ["balance"]}]}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutPortalSink :added "4.0"}
(fact "creates a portal sink element"
  ^:hidden
  
  (defn.js EntryLayoutPortalSinkDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutPortalSink"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryLayoutPortalSink
         {:design  {:type "light"}
          :impl {:name "hello_light"}}]
        [:% slim-entry/EntryLayoutPortal
         {:design  {:type "light"}
          :impl {:target "hello_light"
                 :body  [[:% slim-entry/EntryContentTitle
                          {:key "title"
                           :design {:type "light"}
                           :entry  entry
                           :impl   {:key "title"
                                    :template ["currency_id"]}
                           :custom {:title {:style  {:padding 10}}}}]
                         [:% slim-entry/EntryContentParagraph
                          {:key "body"
                           :design  {:type "light"}
                           :entry   entry
                           :impl    {:key "body"
                                     :template ["balance"]}
                           :custom  {:body   {:style {:padding 10}}}}]]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :custom  {:title {:style  {:padding 10}}
                    :body   {:style {:padding 10}}}
          :entry  entry
          :impl {:type "portal_sink"
                 :name "hello_dark"}}]
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :custom  {:title {:style  {:padding 10}}
                    :body   {:style {:padding 10}}}
          :entry  entry
          :impl {:type "portal"
                 :target "hello_dark"
                 :body  [{:type "title"
                          :key "title"
                          
                          :template ["currency_id"]}
                         {:type "p"
                          :key "body"
                          :template ["balance"]}]}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutScroll :added "4.0"}
(fact "creates a scrollview"
  ^:hidden
  
  (defn.js EntryContentScrollDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentScroll"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 10}}
        [:% slim-entry/EntryLayoutScroll
         {:design  {:type "light"}
          :entry   entry
          :impl    {:key "body"}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :padding 10}}
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :entry   entry
          :impl    {:type "scroll"}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutPopup :added "4.0"}
(fact "creates a popup view"
  ^:hidden
  
  (defn.js EntryLayoutPopupDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     [:% n/Isolation
      (n/EnclosedCode 
       {:label "melbourne.slim-entry/EntryLayoutPopup"} 
       [:% n/Row
        [:% ui-static/Div
         {:design {:type "light"}
          :style {:flex 1
                  :padding 10}}
         #_[:% slim-entry/EntryLayoutPopup
            {:design  {:type "light"}
             :entry   entry
             :impl    {:key "body"
                       :text "HELLO"}}]]
        [:% ui-static/Div
         {:design {:type "dark"}
          :style {:flex 1
                  :padding 10}}
         [:% slim-entry/Entry
          {:design  {:type "dark"}
           :entry   entry
           :actions {:hello (fn:> (alert "HELLO"))}
           :impl    {:type "popup"
                     :text "HELLO"
                     :body  [{:type "v"
                              :style {:height 100
                                      :width 100}
                              :body [{:template "hello world"}
                                     {:type "action"
                                      :text "HELLO"
                                      :submit "hello"}]}]}}]]])])))

^{:refer melbourne.slim-entry/EntryLayoutDebug :added "4.0"}
(fact "creates a debug view"
  ^:hidden
  
  (defn.js EntryLayoutDebugDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutDebug"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 10}}
        [:% slim-entry/EntryLayoutDebug
         {:design  {:type "light"}
          :entry   entry
          :impl    {:key "body"
                    :body [[:% n/Text
                            {:key "name"}
                            "Hello World"]]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :padding 10}}
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :entry   entry
          :impl    {:type "debug"
                    :body [{:template "hellp world"}]}}]]]))))

^{:refer melbourne.slim-entry/EntryContentSeparator :added "4.0"}
(fact "creates a separator"
  ^:hidden
  
  (defn.js EntryContentSeparatorDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentSeparator"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 10}}
        [:% slim-entry/EntryContentSeparator
         {:design  {:type "light"}
          :entry   entry
          :impl    {:key "body"}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :padding 10}}
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :entry   entry
          :impl    {:type "separator"}}]]]))))

^{:refer melbourne.slim-entry/entryContentText :added "4.0"}
(fact "creates either a title or context component")

^{:refer melbourne.slim-entry/EntryContentTitleH1 :added "0.1"}
(fact "creates a h1 title"
  ^:hidden
  
  (defn.js EntryContentTitleH1Demo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentTitleH1"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentTitleH1
         {:design {:type "light"}
          :entry  entry
          :impl   {:key "title_h1"
                   :template ["currency_id"]}
          :custom {:title {:style  {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry  entry
          :impl   {:type "title_h1"
                   :key "title"
                   :template ["currency_id"]}
          :custom {:title   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentTitleH2 :added "0.1"}
(fact "creates a h2 title"
  ^:hidden
  
  (defn.js EntryContentTitleH2Demo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentTitleH2"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentTitleH2
         {:design {:type "light"}
          :entry  entry
          :impl   {:key "title_h2"
                   :template ["currency_id"]}
          :custom {:title {:style  {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry  entry
          :impl   {:type "title_h2"
                   :key "title"
                   :template ["currency_id"]}
          :custom {:title   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentTitleH3 :added "4.0"}
(fact "creates a h3 title"
  ^:hidden
  
  (defn.js EntryContentTitleH3Demo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentTitleH3"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentTitleH3
         {:design {:type "light"}
          :entry  entry
          :impl   {:key "title_h3"
                   :template ["currency_id"]}
          :custom {:title {:style  {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry  entry
          :impl   {:type "title_h3"
                   :key "title"
                   :template ["currency_id"]}
          :custom {:title   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentTitleH4 :added "4.0"}
(fact "creates a h4 title"
  ^:hidden
  
  (defn.js EntryContentTitleH4Demo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentTitleH4"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentTitleH4
         {:design {:type "light"}
          :entry  entry
          :impl   {:key "title_h4"
                   :template ["currency_id"]}
          :custom {:title {:style  {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry  entry
          :impl   {:type "title_h4"
                   :key "title"
                   :template ["currency_id"]}
          :custom {:title   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentTitleH5 :added "4.0"}
(fact "creates a h5 title"
  ^:hidden
  
  (defn.js EntryContentTitleH5Demo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentTitleH5"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentTitleH5
         {:design {:type "light"}
          :entry  entry
          :impl   {:key "title_h5"
                   :template ["currency_id"]}
          :custom {:title {:style  {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry  entry
          :impl   {:type "title_h5"
                   :key "title"
                   :template ["currency_id"]}
          :custom {:title   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentTitle :added "4.0"}
(fact "creates a h6 title"
  ^:hidden
  
  (defn.js EntryContentTitleDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentTitle"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentTitle
         {:design {:type "light"}
          :entry  entry
          :impl   {:key "title"
                   :template ["currency_id"]}
          :custom {:title {:style  {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry  entry
          :impl   {:type "title"
                   :key "title"
                   :template ["currency_id"]}
          :custom {:title   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentBold :added "0.1"}
(fact "creates a h3 title"
  ^:hidden
  
  (defn.js EntryContentBoldDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentBold"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentBold
         {:design {:type "light"}
          :entry  entry
          :impl   {:key "bold_"
                   :template ["currency_id"]}
          :custom {:bold {:style  {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry  entry
          :impl   {:type "bold_"
                   :key "bold"
                   :template ["currency_id"]}
          :custom {:bold   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentParagraph :added "4.0"}
(fact "creates a paragraph"
  ^:hidden
  
  (defn.js EntryContentParagraphDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentParagraph"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentParagraph
         {:design  {:type "light"}
          :entry   entry
          :impl    {:key "body"
                    :template ["balance"]}
          :custom  {:body   {:style {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :entry   entry
          :impl    {:type "p"
                    :key "body"
                    :template ["balance"]}
          :custom  {:body   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentIcon :added "4.0"}
(fact "creates a paragraph"
  ^:hidden
  
  (defn.js EntryContentIconDemo
    []
    (var entry {:name "home"})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentIcon"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentIcon
         {:design  {:type "light"}
          :entry   entry
          :impl    {:key "body"
                    :template ["name"]}
          :custom  {:body   {:style {:padding 10}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design  {:type "dark"}
          :entry   entry
          :impl    {:type "icon"
                    :key "body"
                    :template ["name"]}
          :custom  {:body   {:style {:padding 10}}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentImage :added "4.0"}
(fact "creates entry card avatar"
  ^:hidden
  
  (defn.js EntryContentImageDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentImage"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentImage
         {:design {:type "light"}
          :entry entry
          :impl {:text {:template ["currency_id"]}
                 :format data/first}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :impl {:type "image"
                 :text {:template ["currency_id"]}
                 :format data/first}}]]]))))

^{:refer melbourne.slim-entry/EntryContentPair :added "4.0"}
(fact "creates entry content pair"
  ^:hidden
  
  (defn.js EntryContentPairDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentPair"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentPair
         {:design {:type "light"}
          :entry entry
          :impl {:title {:template "currency: "}
                 :text {:template ["currency_id"]}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :custom {:title {:style {:fontWeight "900"}}}
          :impl {:type "pair"
                 :title {:type "title"
                         :key "title"
                         :template "currency"
                         :format (fn:> [s] (+ (. s (toUpperCase)) ": "))}
                 :text  {:template ["currency_id"]}}}]]]))))

^{:refer melbourne.slim-entry/EntryContentField :added "4.0"}
(fact "creates an entry field"
  ^:hidden
  
  (defn.js EntryContentFieldDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:currency ["XLM" "STATS"]
                      :currency1 "USD"
                      :name    ""
                      :about   ""})
               {:currency  []
                :currency1 []
                :name      []
                :about     []}))
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentField"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentField
         {:design {:type "light"}
          :form form
          :mini true
          :impl {:type "field"
                 :label "Name"
                 :field "name"}}]
        [:% slim-entry/EntryContentField
         {:design {:type "light"}
          :form form
          :mini true
          :impl {:type "field"
                 :field "currency"
                 :component "enum_multi"
                 :label "Currency"
                 :options ["XLM" "USD" "STATS"]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :form form
          :mini true
          :impl {:type "v"
                 :body [{:type "field"
                         :label "Name"
                         :field "name"}
                        {:type "field"
                         :field "currency"
                         :component "enum_multi"
                         :label "Currency"
                         :options ["XLM" "USD" "STATS"]}]}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutFormFade :added "4.0"}
(fact "creates an entry field"
  ^:hidden
  
  (defn.js EntryLayoutFormFadeDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:visible true})
               {:visible true}))
    
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutFormFade"} 
      [:% n/Row
       [:% n/Button
        {:title "T"
         :onPress (fn:> (event-form/set-field
                         form
                         "visible"
                         (not (event-form/get-field form "visible"))))}]] 
      [:% ui-static/Div
       {:design {:type "light"}
        :style {:flex 1}}
       [:% slim-entry/EntryLayoutFormFade
        {:design {:type "light"}
         :form form
         :mini true
         :impl {:template ["visible"]
                :watch ["visible"]
                :body  [[:% n/View
                         {:style {:height 100
                                  :width 100
                                  :backgroundColor "red"}}]]}}]]))))

^{:refer melbourne.slim-entry/EntryLayoutFormFold :added "4.0"}
(fact "creates an entry field"
  ^:hidden
  
  (defn.js EntryLayoutFormFoldDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:visible true})
               {:visible true}))
    
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutFormFold"} 
      [:% n/Row
       [:% n/Button
        {:title "T"
         :onPress (fn:> (event-form/set-field
                         form
                         "visible"
                         (not (event-form/get-field form "visible"))))}]] 
      [:% ui-static/Div
       {:design {:type "light"}
        :style {:flex 1}}
       [:% slim-entry/EntryLayoutFormFold
        {:design {:type "light"}
         :form form
         :mini true
         :impl {:template ["visible"]
                :watch ["visible"]
                :body [[:% n/View
                        {:style {:height 100
                                 :width 100
                                 :backgroundColor "red"}}]]}}]]))))

^{:refer melbourne.slim-entry/entrySubmitType :added "4.0"}
(fact "picks submit type based on submit key")

^{:refer melbourne.slim-entry/entryOnSubmit :added "4.0"}
(fact "creates onSubmit function based on submitType")

^{:refer melbourne.slim-entry/entryControlFn :added "4.0"}
(fact "creates the control function")

^{:refer melbourne.slim-entry/entryOnControl :added "4.0"}
(fact "creates the control onPress/onSuccess lambda")

^{:refer melbourne.slim-entry/EntryContentControl :added "4.0"}
(fact "creates an control button"
  ^:hidden
  
  (defn.js EntryContentControlDemo
    []
    (var entry {:id "hello"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentControl"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentControl
         {:design {:type "light"}
          :entry entry
          :custom {:hello {:text "Hello"}}
          :control {:setShowDetail (fn:> [e] (alert (xt/x:json-encode e)))}
          :impl {:key "hello"
                 :submit "detail"}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :custom {:hello {:text "Hello"}}
          :control {:setShowDetail (fn:> [e] (alert (xt/x:json-encode e)))}
          :impl {:type "control"
                 :key "hello"
                 :submit "detail"}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutControl :added "4.0"}
(fact "creates an control button"
  ^:hidden
  
  (defn.js EntryLayoutControlDemo
    []
    (var entry {:id "hello"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutControl"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryLayoutControl
         {:design {:type "light"}
          :entry entry
          :custom {:hello {:text "Hello"}}
          :control {:setShowDetail (fn:> [e] (alert (xt/x:json-encode e)))}
          :impl {:key "hello"
                 :submit "detail"
                 :body [[:% n/Text "PRESS"]]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :custom {:hello {:text "Hello"}}
          :control {:setShowDetail (fn:> [e] (alert (xt/x:json-encode e)))}
          :impl {:type "control_layout"
                 :key "hello"
                 :submit "detail"
                 :body [{:type "raw"}]}}]]]))))

^{:refer melbourne.slim-entry/EntryContentLink :added "4.0"}
(fact "creates an control button"
  ^:hidden
  
  (defn.js EntryContentLinkDemo
    []
    (var route (ext-route/makeRoute "hello/world"))
    (var url   (ext-route/listenRouteUrl route))
    (var entry {:a 1 :b 2})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentLink"} 
      [:% n/Text url] 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentLink
         {:design {:type "light"}
          :entry entry
          :route route
          :impl {:template "hello/world/again"
                 :text "PRESS"}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :route route
          :impl {:type "link"
                 :template "hello/world"
                 :text "PRESS"}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutLink :added "4.0"}
(fact "creates an control button"
  ^:hidden
  
  (defn.js EntryLayoutLinkDemo
    []
    (var route (ext-route/makeRoute "hello/world"))
    (var url   (ext-route/listenRouteUrl route))
    (var entry {:a 1 :b 2})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutLink"} 
      [:% n/Text url] 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryLayoutLink
         {:design {:type "light"}
          :entry entry
          :route route
          :impl {:template "hello/world/again"
                 :body [[:% n/Text "PRESS"]]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :route route
          :impl {:type "link_layout"
                 :template "hello/world"
                 :body [{:type "raw"}]}}]]]))))

^{:refer melbourne.slim-entry/EntryContentRoute :added "4.0"}
(fact "creates a content route"
  ^:hidden
  
  (defn.js EntryContentRouteDemo
    []
    (var entry {:id "hello"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentRoute"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        #_[:% slim-entry/EntryContentRoute
           {:design {:type "light"}
            :entry entry
            :custom {:hello {:text "Hello"}}
            :route {:setShowDetail (fn:> [e] (alert (xt/x:json-encode e)))}
            :impl {:key "hello"
                   :submit "detail"}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        #_[:% slim-entry/Entry
           {:design {:type "dark"}
            :entry entry
            :custom {:hello {:text "Hello"}}
            :route {:setShowDetail (fn:> [e] (alert (xt/x:json-encode e)))}
            :impl {:type "route"
                   :key "hello"
                   :submit "detail"}}]]]))))

^{:refer melbourne.slim-entry/EntryContentRouteToggle :added "0.1"}
(fact "route content toggle"
  ^:hidden

  (defn.js EntryContentRouteToggleDemo
    []
    (var entry {:id "hello"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentRouteToggle"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        #_[:% slim-entry/EntryContentRoute
           {:design {:type "light"}
            :entry entry
            :custom {:hello {:text "Hello"}}
            :route {:setShowDetail (fn:> [e] (alert (xt/x:json-encode e)))}
            :impl {:key "hello"
                   :submit "detail"}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        #_[:% slim-entry/Entry
           {:design {:type "dark"}
            :entry entry
            :custom {:hello {:text "Hello"}}
            :route {:setShowDetail (fn:> [e] (alert (xt/x:json-encode e)))}
            :impl {:type "route"
                   :key "hello"
                   :submit "detail"}}]]]))))

^{:refer melbourne.slim-entry/EntryContentAction :added "4.0"}
(fact "creates an action"
  ^:hidden
  
  (defn.js EntryContentActionDemo
    []
    (var entry {:id "hello"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentAction"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentAction
         {:design {:type "light"}
          :entry entry
          :actions {:print (fn:> [e] (alert (xt/x:json-encode e)))}
          :impl {:submit "print"
                 :text "HELLO"
                 :submitType "entry"}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :actions {:print (fn:> [e] (alert (xt/x:json-encode e)))}
          :impl {:type "action"
                 :text "HELLO"
                 :submit "print"
                 :submitType "entry"}}]]]))))

^{:refer melbourne.slim-entry/EntryContentSubmit :added "4.0"}
(fact "creates a layout form"
  ^:hidden
  
  (defn.js EntryContentSubmitDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:name "NBA"
                      :title "National Basketball Association"
                      :description ""})
               {:name  [(validators/is-required)]
                :title [(validators/is-required)]
                :description []}))
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryContentSubmit"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryContentSubmit
         {:actions {:create (fn:> [data] (alert (xt/x:json-encode data)))}
          :form form
          :impl {:submit "create"}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :actions {:create (fn:> [data] (alert (xt/x:json-encode data)))}
          :mini true
          :form form
          :impl {:type "submit"
                 :submit "create"}}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutCard :added "4.0"}
(fact "creates a card entry"
  ^:hidden
  
  (defn.js EntryLayoutCardDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutCard"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryLayoutCard
         {:design {:type "light"}
          :entry entry
          :impl {:body
                 {:title
                  [:% slim-entry/EntryContentTitle
                   {:design {:type "light"}
                    :entry  entry
                    :impl   {:key "title"
                             :template ["currency_id"]}
                    :custom {:title   {:style {:padding 10}}}}]}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :custom {:title   {:style {:padding 10}}}
          :impl {:type "card"
                 :body {:title
                        {:type "title"
                         :key "title"
                         :template ["currency_id"]}} }}]]]))))

^{:refer melbourne.slim-entry/EntryLayoutForm :added "4.0"}
(fact "creates a layout form"
  ^:hidden
  
  (defn.js EntryLayoutFormDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:name "NBA"
                      :title "National Basketball Association"
                      :description ""})
               {:name  [(validators/is-required)]
                :title [(validators/is-required)]
                :description []}))
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-entry/EntryLayoutForm"} 
      [:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/EntryLayoutForm
         {:actions {:create (fn:> [data] (alert (xt/x:json-encode data)))}
          :form form
          :impl {:submit "create"
                 :body [[:% slim-entry/EntryContentField
                         {:key 0
                          :design {:type "light"}
                          :form form
                          :mini true
                          :impl {:type "field"
                                 :label "Name"
                                 :field "name"}}]
                        [:% slim-entry/EntryContentField
                         {:key 1
                          :design {:type "light"}
                          :form form
                          :mini true
                          :impl {:type "field"
                                 :label "Title"
                                 :field "title"}}]]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :actions {:create (fn:> [data] (alert (xt/x:json-encode data)))}
          :mini true
          :form form
          :impl {:type "form"
                 :submit "create"
                 :body [{:type "field"
                         :label "Name"
                         :field "name"}
                        {:type "field"
                         :label "Title"
                         :field "title"}]}}]]]))))

^{:refer melbourne.slim-entry/compileEntryPopup :added "4.0"}
(fact "compiles the Entry Popup")

^{:refer melbourne.slim-entry/compileEntry :added "4.0"}
(fact "compiles the entry")

^{:refer melbourne.slim-entry/Entry :added "4.0"}
(fact "creates the entry"
  ^:hidden
  
  (defn.js EntryDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-entry/Entry"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "light"}
          :entry entry
          :impl {:type "title"
                 :template ["currency_id"]}}]
        [:% slim-entry/Entry
         {:design {:type "light"}
          :entry entry
          :impl {:type "p"
                 :template ["currency_id"]}}]
        [:% slim-entry/Entry
         {:design {:type "light"}
          :impl {:type "separator"}}]
        [:% slim-entry/Entry
         {:design {:type "light"}
          :entry entry
          :impl {:type "image"
                 :text {:template ["currency_id"]
                        :format data/first}}}]
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :impl {:type "card"
                 :body
                 {:main
                  {:type "v"
                   :body [{:key "entry"
                           :type "p"
                           :template ["currency_id"]}
                          {:type "h"
                           :body [{:key "entry"
                                   :type "title"
                                   :template ["escrow"]}
                                  {
                                   :type "p"
                                   :template ["balance"]}]}]}}}}]
        
        [:% slim-entry/Entry
         {:design {:type "light"}
          :entry entry
          :custom {:row {:style {:padding 5}}
                   :entry {:style {:padding 3}}}
          :impl {:key "row"
                 :type "v"
                 :body [{:type "h"
                         :body [{
                                 :type "p"
                                 :template ["currency_id"]}
                                {
                                 :type "title"
                                 :template ["escrow"]}
                                {
                                 :type "p"
                                 :template ["balance"]}]}
                        {:type "v"
                         :body [{
                                 :type "p"
                                 :template ["currency_id"]}
                                {
                                 :type "title"
                                 :template ["balance"]}]}]}}]]
       
       
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :impl {:type "title"
                 :template ["currency_id"]}}]
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :impl {:type "p"
                 :template ["currency_id"]}}]
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :impl {:type "separator"}}]
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :impl {:type "image"
                 :text {:template ["currency_id"]
                        :format data/first}}}]
        [:% slim-entry/Entry
         {:design {:type "light"}
          :entry entry
          :impl {:type "card"
                 :body
                 {:main
                  {:type "v"
                   :body [{:key "entry"
                           :type "p"
                           :template ["currency_id"]}
                          {:type "h"
                           :body [{:key "entry"
                                   :type "title"
                                   :template ["escrow"]}
                                  {
                                   :type "p"
                                   :template ["balance"]}]}]}}}}]
        [:% slim-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :custom {:row {:style {:padding 5}}
                   :entry {:style {:padding 3}}}
          :impl {:key "row"
                 :type "v"
                 :body [{:type "h"
                         :body [{:key "entry"
                                 :type "p"
                                 :template ["currency_id"]}
                                {
                                 :type "title"
                                 :template ["escrow"]}
                                {
                                 :type "p"
                                 :template ["balance"]}]}
                        {:type "v"
                         :body [{
                                 :type "p"
                                 :template ["currency_id"]}
                                {
                                 :type "title"
                                 :template ["balance"]}]}]}}]]])))

  (def.js MODULE (!:module)))
