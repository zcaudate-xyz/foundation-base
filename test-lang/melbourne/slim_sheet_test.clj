(ns melbourne.slim-sheet-test
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
   :require [[js.react-native :as n]
             [js.react.ext-form :as ext-form]
             [js.react :as r]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [melbourne.ui-static :as ui-static]
             [melbourne.slim-sheet :as slim-sheet]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

^{:refer melbourne.slim-sheet/SheetPagination :added "4.0"}
(fact "creates a sheet pagination"
  ^:hidden
  
  (defn.js SheetPaginationDemo
    []
    (var [showPage setShowPage] (r/local 3))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-sheet/SheetPagination"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-sheet/SheetPagination
         {:design {:type "light"}
          :control #{showPage setShowPage}
          :impl {:page {:total 200}}}]
        [:% slim-sheet/SheetPagination
         {:design {:type "light"}
          :control #{showPage setShowPage}
          :impl {:page {:total 70}}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-sheet/SheetPagination
         {:design {:type "dark"}
          :control #{showPage setShowPage}
          :impl {:page {:total 200}}}]
        [:% slim-sheet/SheetPagination
         {:design {:type "dark"}
          :control #{showPage setShowPage}
          :impl {:page {:total 70}}}]]]))))

^{:refer melbourne.slim-sheet/SheetGroupHeader :added "4.0"}
(fact "creates a sheet group header"
  ^:hidden
  
  (defn.js SheetGroupHeaderDemo
    []
    (var [showPage setShowPage] (r/local 3))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-sheet/SheetGroupHeader"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-sheet/SheetGroupHeader
         {:design {:type "light"}
          :group {:name "WORLD"}
          :impl {}}]
        [:% slim-sheet/SheetGroupHeader
         {:design {:type "light"}
          :group {:name "HELLO"}
          :impl {}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-sheet/SheetGroupHeader
         {:design {:type "dark"}
          :group {:name "WORLD"}
          :impl {}}]
        [:% slim-sheet/SheetGroupHeader
         {:design {:type "dark"}
          :group {:name "HELLO"}
          :impl {}}]]]))))

^{:refer melbourne.slim-sheet/SheetHeader :added "4.0"}
(fact "creates a sheet header"
  ^:hidden
  
  (defn.js SheetHeaderDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-sheet/SheetHeader"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-sheet/SheetHeader
         {:design {:type "light"}
          :style {:padding 10}
          :entry entry
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["currency_id"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-sheet/SheetHeader
         {:design {:type "dark"}
          :style {:padding 10}
          :entry entry
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["currency_id"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]]))))

^{:refer melbourne.slim-sheet/SheetRow :added "4.0"}
(fact "creates a sheet row"
  ^:hidden
  
  (defn.js SheetRowDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-sheet/SheetRow"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1}}
        [:% slim-sheet/SheetRow
         {:design {:type "light"}
          :style {:padding 10}
          :entry entry
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["currency_id"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1}}
        [:% slim-sheet/SheetRow
         {:design {:type "dark"}
          :style {:padding 10}
          :entry entry
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["currency_id"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]]))))

^{:refer melbourne.slim-sheet/SheetBasicRows :added "4.0"}
(fact "creates a basic sheet"
  ^:hidden
  
  (defn.js SheetBasicRowsDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-sheet/SheetBasicRows"} 
[:% n/View
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-sheet/SheetBasicRows
         {:design {:type "light"}
          :entries [{"currency_id" "STATS"
                     :balance 1000
                     :escrow 50.5}
                    {"currency_id" "DOGE"
                     :balance 1000
                     :escrow 50.5}]
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["currency_id"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-sheet/SheetBasicRows
         {:design {:type "dark"}
          :entries [{"currency_id" "STATS"
                     :balance 1000
                     :escrow 50.5}
                    {"currency_id" "DOGE"
                     :balance 1000
                     :escrow 50.5}]
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["currency_id"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]]))))

^{:refer melbourne.slim-sheet/SheetBasic :added "4.0"}
(fact "creates a basic sheet"
  ^:hidden
  
  (defn.js SheetBasicDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-sheet/SheetBasic"} 
[:% n/View
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-sheet/SheetBasic
         {:design {:type "light"}
          :entries [{"currency_id" "STATS"
                     :balance 1000
                     :escrow 50.5}
                    {"currency_id" "DOGE"
                     :balance 1000
                     :escrow 50.5}]
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["currency_id"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-sheet/SheetBasic
         {:design {:type "dark"}
          :entries [{"currency_id" "STATS"
                     :balance 1000
                     :escrow 50.5}
                    {"currency_id" "DOGE"
                     :balance 1000
                     :escrow 50.5}]
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["currency_id"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]]))))

^{:refer melbourne.slim-sheet/SheetGroupRows :added "4.0"
  :setup [(def +group+
            {:name "STATS"
             :entries (shuffle
                       [{"currency_id" "STATS"
                         :name "ABC"
                         :balance 506
                         :escrow 50.5}
                        {"currency_id" "STATS"
                         :name "HIJ"
                         :balance 130400
                         :escrow 1250.5}
                        {"currency_id" "STATS"
                         :name "NOP"
                         :balance 1000
                         :escrow 50.5}
                        {"currency_id" "STATS"
                         :name "TUV"
                         :balance 79
                         :escrow 37}])})]}
(fact "creates a group row of sheets"
  ^:hidden
  
  (defn.js SheetGroupRowsDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-sheet/SheetGroupRows"} 
[:% n/View
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-sheet/SheetGroupRows
         {:design {:type "light"}
          :group (@! +group+)
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["name"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-sheet/SheetGroupRows
         {:design {:type "dark"}
          :group (@! +group+)
          :impl  {:header  {:format j/toUpperCase}
                  :columns [{:name "title"
                             :template ["name"]}
                            {:name "balance"
                             :template ["balance"]}
                            {:name "escrow"
                             :template ["escrow"]
                             :style {:textAlign "right"}}]}}]]]))))

^{:refer melbourne.slim-sheet/groupEntries :added "4.0"}
(fact "group entries for display")

^{:refer melbourne.slim-sheet/Sheet :added "4.0"
  :setup [(def +entries+
            (shuffle
             [{"currency_id" "STATS"
               :name "ABC"
               :balance 506
               :escrow 50.5}
              {"currency_id" "DOGE"
               :name "EFG"
               :balance 34050
               :escrow 50.5}

              {"currency_id" "STATS"
               :name "HIJ"
               :balance 130400
               :escrow 1250.5}
              {"currency_id" "DOGE"
               :name "KLM"
               :balance 100
               :escrow 0.5}

              {"currency_id" "STATS"
               :name "NOP"
               :balance 1000
               :escrow 50.5}

              {"currency_id" "DOGE"
               :name "QRS"
               :balance 490
               :escrow 34.0}

              {"currency_id" "STATS"
               :name "TUV"
               :balance 79
               :escrow 37}
              {"currency_id" "DOGE"
               :name "WXY"
               :balance 456
               :escrow 63}]))]}
(fact "creates a sheet"
  ^:hidden
  
  (defn.js SheetDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-sheet/Sheet"} 
[:% n/View
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-sheet/Sheet
         {:design {:type "light"}
          :impl   {:groups  {:split ["currency_id"]}
                   :items   {:sort (fn:> [arr] (sort-by/sort-by arr ["balance" "name"]))}
                   :header  {:format j/toUpperCase}
                   :columns [{:name "name"
                              :template ["name"]}
                             {:name "balance"
                              :template ["balance"]}
                             {:name "escrow"
                              :template ["escrow"]
                              :style {:textAlign "right"}}]}
          :entries  (@! +entries+)}]]])))
  
  (def.js MODULE (!:module)))
