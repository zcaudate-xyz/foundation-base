(ns melbourne.ui-entry-test
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
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-entry :as ui-entry]
             [melbourne.ui-group :as ui-group]
             [melbourne.ui-static :as ui-static]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

^{:refer melbourne.ui-entry/get-entry-data :added "4.0"}
(fact "gets the entry data")

^{:refer melbourne.ui-entry/EntryRowHeaderText :added "4.0"}
(fact "creates the entry header"
  ^:hidden
  
  (defn.js EntryRowHeaderTextDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryRowHeaderText"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% ui-entry/EntryRowHeaderText
         {:design {:type "light"}
          :style {:padding 10}
          :impl {:header {:format j/toUpperCase}}
          :column {:name "currency"
                   :data  ["currency_id"]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% ui-entry/EntryRowHeaderText
         {:design {:type "dark"}
          :style {:padding 10}
          :impl {:header {:format j/toUpperCase}}
          :column {:name "currency"
                   :data  ["currency_id"]}}]]]))))

^{:refer melbourne.ui-entry/EntryRowHeader :added "4.0"}
(fact "creates the entry row header"
  ^:hidden
  
  (defn.js EntryRowHeaderDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryRowHeader"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% ui-entry/EntryRowHeader
         {:design {:type "light"}
          :style {:padding 10}
          :impl {:type "row"
                 :header {:format j/toUpperCase}
                 :columns [{:name "currency"
                            :data  ["currency_id"]}
                           {:name "balance"
                            :data  ["balance"]}
                           {:name "escrow"
                            :data  ["escrow"]
                            :style {:textAlign "right"}}]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% ui-entry/EntryRowHeader
         {:design {:type "dark"}
          :style {:padding 10}
          :impl {:type "row"
                 :header {:format j/toUpperCase}
                 :columns [{:name "currency"
                            :data  ["currency_id"]}
                           {:name "balance"
                            :data  ["balance"]}
                           {:name "escrow"
                            :data  ["escrow"]
                            :style {:textAlign "right"}}]}}]]]))))

^{:refer melbourne.ui-entry/EntryRowText :added "4.0"}
(fact "creates the entry row text"
  ^:hidden
  
  (defn.js EntryRowTextDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryRowText"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% ui-entry/EntryRowText
         {:design {:type "light"}
          :column {:name "currency"
                   :data  ["currency_id"]}
          :entry entry}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% ui-entry/EntryRowText
         {:design {:type "dark"}
          :column {:name "currency"
                   :data  ["currency_id"]}
          :entry entry}]]]))))

^{:refer melbourne.ui-entry/EntryRow :added "4.0"}
(fact "creates the entry row"
  ^:hidden
  
  (defn.js EntryRowDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryRow"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% ui-entry/EntryRow
         {:design {:type "light"}
          :style {:padding 10}
          :impl {:type "row"
                 :columns [{:name "currency"
                            :data  ["currency_id"]}
                           {:name "balance"
                            :data  ["balance"]}
                           {:name "escrow"
                            :data  ["escrow"]
                            :style {:textAlign "right"}}]}
          :entry entry}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% ui-entry/EntryRow
         {:design {:type "dark"}
          :style {:padding 10}
          :impl {:type "row"
                 :columns [{:name "currency"
                            :data  ["currency_id"]}
                           {:name "balance"
                            :data  ["balance"]}
                           {:name "escrow"
                            :data  ["escrow"]
                            :style {:textAlign "right"}}]}
          :entry entry}]]]))))

^{:refer melbourne.ui-entry/EntryCardTitle :added "4.0"}
(fact "creates entry card title"
  ^:hidden
  
  (defn.js EntryCardTitleDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryCardTitle"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% ui-entry/EntryCardTitle
         {:design {:type "light"}
          :entry entry
          :section {:data  ["currency_id"]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% ui-entry/EntryCardTitle
         {:design {:type "dark"}
          :entry entry
          :section {:data  ["currency_id"]}}]]]))))

^{:refer melbourne.ui-entry/EntryCardAvatar :added "4.0"}
(fact "creates entry card avatar"
  ^:hidden
  
  (defn.js EntryCardAvatarDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryCardAvatar"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% ui-entry/EntryCardAvatar
         {:design {:type "light"}
          :entry entry
          :section {:data  ["currency_id"]
                    :format data/first}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% ui-entry/EntryCardAvatar
         {:design {:type "dark"}
          :entry entry
          :section {:data  ["currency_id"]
                    :format data/first}}]]]))))

^{:refer melbourne.ui-entry/EntryCardBodyPair :added "4.0"}
(fact "creates entry body pair"
  ^:hidden
  
  (defn.js EntryCardBodyPairDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryCardBodyPair"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :flexDirection "row"}}
        [:% ui-entry/EntryCardBodyPair
         {:design {:type "light"}
          :entry entry
          :column {:name "Currency"
                   :data  ["currency_id"]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :flexDirection "row"}}
        [:% ui-entry/EntryCardBodyPair
         {:design {:type "dark"}
          :entry entry
          :column {:name "Currency"
                   :data  ["currency_id"]}}]]]))))

^{:refer melbourne.ui-entry/EntryCardBodyGroup :added "4.0"}
(fact "creates entry body group"
  ^:hidden
  
  (defn.js EntryCardBodyGroupDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryCardBodyGroup"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :flexDirection "row"}}
        [:% ui-entry/EntryCardBodyGroup
         {:design {:type "light"}
          :entry entry
          :group {:columns [{:name "Currency"
                             :data  ["currency_id"]}
                            {:name "Balance"
                             :data  ["balance"]}]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :flexDirection "row"}}
        [:% ui-entry/EntryCardBodyGroup
         {:design {:type "dark"}
          :entry entry
          :group {:columns [{:name "Currency"
                             :data  ["currency_id"]}
                            {:name "Balance"
                             :data  ["balance"]}]}}]]]))))

^{:refer melbourne.ui-entry/EntryCardBody :added "4.0"}
(fact "creates entry card body"
  ^:hidden
  
  (defn.js EntryCardBodyDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryCardBody"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% ui-entry/EntryCardBody
         {:design {:type "light"}
          :entry entry
          :section {:data  (fn:> [e]
                             (+ "B: " (. e balance)
                                "\n"
                                "E: " (. e escrow)))}}]
        
        [:% ui-static/Separator
         {:design {:type "light"}
          :style {:margin 5}}]
        [:% ui-entry/EntryCardBody
         {:design {:type "light"}
          :entry entry
          :section [{:columns [{:name "Currency"
                                :data  ["currency_id"]}
                               {:name "Balance"
                                :data  ["balance"]}]}
                    {:columns [{:name "Escrow"
                                :data  ["escrow"]}
                               ]}]}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% ui-entry/EntryCardBody
         {:design {:type "dark"}
          :entry entry
          :section {:data  (fn:> [e]
                             (+ "B: " (. e balance)
                                "\n"
                                "E: " (. e escrow)))}}]
        [:% ui-static/Separator
         {:design {:type "dark"}
          :style {:margin 5}}]
        [:% ui-entry/EntryCardBody
         {:design {:type "dark"}
          :entry entry
          :section [{:columns [{:name "Currency"
                                :data  ["currency_id"]}
                               {:name "Balance"
                                :data  ["balance"]}]}
                    {:columns [{:name "Escrow"
                                :data  ["escrow"]}
                               ]}]}]]]))))

^{:refer melbourne.ui-entry/EntryCard :added "4.0"}
(fact "creates the entry row"
  ^:hidden
  
  (defn.js EntryCardDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/EntryCard"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% ui-entry/EntryCard
         {:design {:type "light"}
          :entry entry
          :impl {:type "card"
                 :sections {:title  {:data  ["currency_id"]}
                            :body   {:data  (fn:> [e]
                                              (+ "B: " (. e balance)
                                                 "\n"
                                                 "E: " (. e escrow)))}
                            :avatar {:data  ["currency_id"]
                                     :image ["picture"]}}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% ui-entry/EntryCard
         {:design {:type "dark"}
          :entry entry
          :impl {:type "card"
                 :sections {:title   {:data  ["currency_id"]}
                            :body   {:data  (fn:> [e]
                                              (+ "B: " (. e balance)
                                                 "\n"
                                                 "E: " (. e escrow)))}
                            :avatar {:data  ["currency_id"]
                                     :image ["picture"]}}}}]]])))
  
  (def.js MODULE (!:module)))

^{:refer melbourne.ui-entry/Entry :added "4.0"}
(fact "creates an entry"
  ^:hidden
  
  (defn.js EntryDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (var implCard {:type "card"
                   :sections {:title  {:data  ["currency_id"]}
                              :body   {:data  (fn:> [e]
                                                (+ "B: " (. e balance)
                                                   "\n"
                                                   "E: " (. e escrow)))}
                              :avatar {:data  ["currency_id"]
                                       :image ["picture"]}}})
    (var implRow {:type "row"
                  :columns [{:name "currency"
                             :data  ["currency_id"]}
                            {:name "balance"
                             :data  ["balance"]}
                            {:name "escrow"
                             :data  ["escrow"]
                             :style {:textAlign "right"}}]})
    (var [type setType] (r/local "card"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-entry/Entry"
       :style {:height 200}} 
[:% ui-group/Tabs
       {:data ["card" "row"]
        :value type
        :setValue setType}] 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% ui-entry/Entry
         {:design {:type "light"}
          :entry entry
          :impl (:? (== type "card")
                    implCard
                    implRow)}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% ui-entry/Entry
         {:design {:type "dark"}
          :entry entry
          :impl (:? (== type "card")
                    implCard
                    implRow)}]]]))))

^{:refer melbourne.ui-entry/EntryTable :added "4.0"}
(fact "creates a static table")
