(ns melbourne.slim-test
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
             [js.react.ext-model :as ext-view]
             [js.react-native :as n]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-group :as ui-group]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-static :as ui-static]
             [melbourne.slim :as slim]
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
             [xt.lang.common-trace :as trace]
             [xt.event.base-route :as event-route]]
   :export [MODULE]})

^{:refer melbourne.slim/Table :adopt true :added "0.1"}
(fact "creates the table"
  ^:hidden
  
  (defn.js TableDemo
    []
    (var views   (r/const {:list (ext-view/makeView
                                  {:defaultArgs []
                                   :handler
                                   (fn:>
                                     (jc/future-delayed [100]
                                       (return
                                        [{:id "id-0"
                                          :currency-id "STATS"
                                          :balance 1000
                                          :escrow 50.5}
                                         {:id "id-1"
                                          :currency-id "USA"
                                          :balance 300
                                          :escrow 10.5}
                                         {:id "id-2"
                                          :currency-id "XLM"
                                          :balance 50
                                          :escrow 0.0}])))})}))
    (var control (slim/useLocalControl))
    (var EntryBrief
         (r/const
          (fn [props]
            (return
             (r/% slim/Entry
                  (Object.assign {}
                   props
                   {:impl {:type "card"
                           :body {:title {:type "title"
                                          :template ["currency_id"]}}}}))))))
    (var EntryDetail
         (r/const
          (fn [props]
            (var nprops (Object.assign {} props
                                     {:impl
                                      {:type "card"
                                       :body {:main   {:type "v"
                                                       :body [{:type "pair"
                                                               :title {:template "B"}
                                                               :text  {:template ["balance"]}}
                                                              {:type "pair"
                                                               :title {:template "E"}
                                                               :text {:template ["escrow"]}}]}
                                              :avatar {:type "image"
                                                       :text  {:template ["currency_id"]}
                                                       :image {:template ["picture"]}}}}}))
            (return (r/% slim/Entry
                         nprops)))))
    (var components {:entry-brief  EntryBrief
                     :entry-detail EntryDetail
                     :create (r/const (fn [props]
                                        (return (r/% ui-static/Text props "CREATE"))))})
    (var #{orderBy
           setOrderBy
           showHeader
           setShowHeader
           showDetail
           setShowDetail
           showModify
           setShowModify
           showCreate
           setShowCreate
           routeKey
           showList
           setShowList} control)
    (var [mini setMini] (r/local true))
    (var design {:type "light"})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim/Table"} 
[:% n/Row
       [:% ui-text/ToggleMinor
        #{design
          {:text "Create"
           :selected showCreate
           :onPress (fn:> (setShowCreate (not showCreate)))}}]
       [:% ui-text/ToggleMinor
        #{design
          {:text "List"
           :selected showList
           :onPress (fn:> (setShowList (not showList)))}}]
       [:% ui-text/ToggleMinor
        #{design
          {:text "Mini"
           :selected mini
           :onPress (fn:> (setMini (not mini)))}}]] 
[:% n/Row
       [:% slim/Table
        #{{:design {:type "light"}
           :display {:type "fold"}
           :mini mini}
          components views control}]
       [:% slim/Table
        #{{:design {:type "dark"}
           :display {:type "fold"}
           :mini mini}
          components views control}]]))))

^{:refer melbourne.slim/createEntry :added "4.0"}
(fact "makes an entry"
  ^:hidden
  
  (defn.js CreateEntryDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.slim/createEntry"} 
[:% n/Row
       (slim/createEntry
        {:design {:type "light"}
         :impl  {:type "raw"}
         :entry {:a 1 :b 2}})]))))

^{:refer melbourne.slim/entry :added "4.0"}
(fact "makes entry display"
  ^:hidden
  
  (defn.js EntryDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.slim/entry"} 
[:% n/Row
       (slim/entry
        {:design {:type "light"}
         :entry {:a 1 :b 2}}
        {:type "raw"})]))))

^{:refer melbourne.slim/useLocalPrimitives :added "4.0"}
(fact "creates crud control primitives")

^{:refer melbourne.slim/useRoutePrimitives :added "4.0"}
(fact "creates crud control on route")

^{:refer melbourne.slim/useListControl :added "4.0"}
(fact "creates show list control based on show")

^{:refer melbourne.slim/useRouteControl :added "4.0"}
(fact "uses the basic route controls"
  ^:hidden
  
  (defn.js UseRouteControlDemo
    []
    (var controls (slim/useRouteControl
                   (r/const (event-route/make-route "hello"))))
    (var #{orderBy
           setOrderBy
           showHeader
           setShowHeader
           showDetail
           setShowDetail
           showModify
           setShowModify
           showCreate
           setShowCreate
           routeKey
           showList
           setShowList} controls)
    (var design {:type "light"})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim/UseRouteControl"} 
[:% ui-static/Div
       #{design
         {:style {:flexDirection "row"}}}
       [:% ui-text/ToggleMinor
        #{design
          {:text "OrderBy"
           :selected orderBy
           :onPress (fn:> (setOrderBy (:? (== orderBy "name")
                                          "time"
                                          "name")))}}]
       [:% ui-text/ToggleMinor
        #{design
          {:text "Header"
           :selected showHeader
           :onPress (fn:> (setShowHeader (not showHeader)))}}]
       [:% ui-text/ToggleMinor
        #{design
          {:text "Detail"
           :selected showDetail
           :onPress (fn:> (setShowDetail (not showDetail)))}}]
       [:% ui-text/ToggleMinor
        #{design
          {:text "Modify"
           :selected showModify
           :onPress (fn:> (setShowModify (not showModify)))}}]
       [:% ui-text/ToggleMinor
        #{design
          {:text "Create"
           :selected showCreate
           :onPress (fn:> (setShowCreate (not showCreate)))}}]
       [:% ui-text/ToggleMinor
        #{design
          {:text "List"
           :selected showList
           :onPress (fn:> (setShowList (not showList)))}}]]))))

^{:refer melbourne.slim/useLocalControl :added "4.0"}
(fact "uses the local controls")

^{:refer melbourne.slim/getParentProps :added "4.0"}
(fact "gets the parent props")

^{:refer melbourne.slim/useParentControl :added "4.0"}
(fact "packages parent props"

  (def.js MODULE (!:module)))
