(ns melbourne.slim-table-test
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
             [js.react.ext-form :as ext-form]
             [js.react-native :as n]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-validators :as validators]
             [melbourne.ui-group :as ui-group]
             [melbourne.slim :as slim]
             [melbourne.slim-table :as slim-table]
             [melbourne.slim-entry :as slim-entry]
             [melbourne.ui-text :as ui-text]
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
             [xt.lang.common-trace :as trace]
             [xt.event.base-route :as event-route]]
   :export [MODULE]})

^{:refer melbourne.slim-table/TableModifyView :added "4.0"}
(fact "displays the modify view"
  ^:hidden

  (defn.js TableModifyViewDemo
    []
    (var control (slim/useLocalControl))
    (var actions {:modify (fn:> [e] (j/delayed [100]
                                      (alert (xt/x:json-encode e))))})
    (var entry {:id "id-0"})
    (var form (ext-form/makeForm
               (fn:> {:currency "STATS"
                      :name ""
                      :title ""})
               {:name  []
                :title []
                :currency []}))
    (var design {:type "light"})
    (var impl   {:type "form"
                 :header  {:main   {:type "title"
                                    :template  "EDIT CURRENCY"}}
                 :submit "modify"
                 :body [{:type "pair"
                         :title {:template "ID: "}
                         :text  {:template ["id"]}}
                        {:type "field"
                         :label "Currency"
                         :field "currency"
                         :options ["STATS" "XLM" "USD"]
                         :component "enum_single"}
                        {:type "field"
                         :label "Title"
                         :field "title"}]})
    (var components {})
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-table/TableModifyView"} 
[:% n/Row
        [:% ui-static/Div
         {:design {:type "light"}
          :style {:flex 1}}
         [:% slim-table/TableModifyView
          #{{:design {:type "light"}
             :mini true
             :display {:modify impl}}
            actions entry form control components}]]
        [:% ui-static/Div
         {:design {:type "dark"}
          :style {:flex 1}}
         [:% slim-table/TableModifyView
          #{{:design {:type "dark"}
             :mini true
             :display {:modify impl}}
            actions entry form control components}]]])])))

^{:refer melbourne.slim-table/TableDetailView :added "0.1"}
(fact "displays the detail view"
  ^:hidden

  (defn.js TableDetailViewDemo
    []
    (var entry {:id "id-0"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (var control (slim/useLocalControl))
    (var design {:type "light"})
    (var impl   {:type "card"
                 :header  {:main    {:type "title"
                                     :template  ["currency_id"]}}
                 :body    {:title   {:type "title"
                                     :template  ["currency_id"]}
                           :main    {:type "v"
                                     :body [{:type "pair"
                                             :title {:template "B"}
                                             :text  {:template ["balance"]}}
                                            {:type "pair"
                                             :title {:template "E"}
                                             :text  {:template ["escrow"]}}]}
                           :avatar  {:type "image"
                                     :text  {:template  ["currency_id"]}
                                     :image {:template  ["picture"]}}}})
    (var components {})
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-table/TableDetailView"} 
[:% n/Row
        [:% ui-static/Div
         {:design {:type "light"}}
         [:% slim-table/TableDetailView
          #{{:design {:type "light"}
             :style {:width 200}
             :display {:detail impl}}
            entry control components}]]
        [:% ui-static/Div
         {:design {:type "dark"}}
         [:% slim-table/TableDetailView
          #{{:design {:type "dark"}
             :style  {:width 200}
             :display {:detail impl}}
            entry control components}]]])])))

^{:refer melbourne.slim-table/TableCreateView :added "4.0"}
(fact "displays the create view"
  ^:hidden

  (defn.js TableCreateViewDemo
    []
    (var control (slim/useLocalControl))
    (var actions {:create (fn:> [e] (j/delayed [100]
                                      (alert (xt/x:json-encode e))))})
    (var form (ext-form/makeForm
               (fn:> {:currency "STATS"
                      :name ""
                      :title ""})
               {:name  []
                :title []
                :currency []}))
    (var design {:type "light"})
    (var impl   {:type "form"
                 :header  {:main   {:type "title"
                                    :template  "NEW CURRENCY"}}
                 :submit "create"
                 :body [{:type "field"
                         :label "Name"
                         :field "name"}
                        {:type "field"
                         :label "Currency"
                         :field "currency"
                         :options ["STATS" "XLM" "USD"]
                         :component "enum_single"}
                        {:type "field"
                         :label "Title"
                         :field "title"}]})
    (var components {})
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-table/TableCreateView"} 
[:% n/Row
        [:% ui-static/Div
         {:design {:type "light"}
          :style {:flex 1}}
         [:% slim-table/TableCreateView
          #{{:design {:type "light"}
             :mini true
             :display {:create impl}}
            actions form control components}]]
        [:% ui-static/Div
         {:design {:type "dark"}
          :style {:flex 1}}
         [:% slim-table/TableCreateView
          #{{:design {:type "dark"}
             :mini true
             :display {:create impl}}
            actions form control components}]]])])))

^{:refer melbourne.slim-table/TableRouterView :added "0.1"}
(fact "creates a single router view"
  ^:hidden
  
  (defn.js TableRouterViewDemo
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
    (var display  {:brief  {:type "card"
                            :body {:title {:template ["currency_id"]}}}
                   :detail {:type "card"
                            :header {:main {:type "h"
                                            :style {:alignItems "center"}
                                            :body [{:type "title_h5"
                                                    :template ["currency_id"]}
                                                   {:type "fill"}
                                                   {:type "control"
                                                    :text  "EDIT"
                                                    :submit "modify"}]}}
                            :body   {:title  {:type "title"
                                              :template  ["currency_id"]}
                                     :main   {:type "v"
                                              :body [{:type "pair"
                                                      :title {:template "B"}
                                                      :text  {:template ["balance"]}}
                                                     {:type "pair"
                                                      :title {:template "E"}
                                                      :text  {:template ["escrow"]}}]}
                                     :avatar {:type "image"
                                              :text  {:template  ["currency_id"]}
                                              :image {:template  ["picture"]}}}}
                   :modify  {:type "form"
                             :header  {:main   {:type "title_h5"
                                                :template  "EDIT"}}
                             :form [(fn:> {:id "HELLO"
                                           :currency "STATS"
                                           :title "National Basketball Association"})
                                    (fn:> {:id []
                                           :currency []
                                           :title []})]
                             :submit "modify"
                             :control {:success "back"}
                             :body [{:type "pair"
                                     :title {:template "ID: "}
                                     :text  {:template ["id"]}}
                                    {:type "field"
                                     :label "Currency"
                                     :field "currency"
                                     :options ["STATS" "XLM" "USD"]
                                     :component "enum_single"}
                                    {:type "field"
                                     :label "Title"
                                     :field "title"}]}})
    (var actions {:modify (fn [id e]
                            (return (jc/future (return "hello"))))})
    (var components {})
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-table/TableRouterView"} 
[:% n/Row
        [:% ui-static/Div
         {:design {:type "light"}
          :style {:flex 1}}
         [:% slim-table/TableRouterView
          #{{:design {:type "light"}
             
             :mini true
             :routeKey (. control routeKey)}
            components actions display views control}]]
        [:% ui-static/Div
         {:design {:type "dark"}
          :style {:flex 1}}
         [:% slim-table/TableRouterView
          #{{:design {:type "dark"}
             
             :mini true
             :routeKey (. control routeKey)}
            components actions display views control}]]])])))

^{:refer melbourne.slim-table/TableRouter :added "0.1"}
(fact "creates the router for elements"
  ^:hidden

  (defn.js TableRouterDemo
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
    (var impl   {:type "card"
                 :body {:title  {:type "title"
                                 :template  ["currency_id"]}
                        :main   {:type "v"
                                 :body [{:type "pair"
                                         :title {:template "B"}
                                         :text  {:template ["balance"]}}
                                        {:type "pair"
                                         :title {:template "E"}
                                         :text  {:template ["escrow"]}}]}
                        :avatar {:type "image"
                                 :text  {:template  ["currency_id"]}
                                 :image {:template  ["picture"]}}}})
    (var EntryBrief
         (r/const
          (fn:> [props]
            (r/% slim-entry/Entry
                 (Object.assign {}
                  props
                  {:impl {:type "card"
                          :body {:title {:template ["currency_id"]}}}}
                  )))))
    (var EntryDetail
         (r/const
          (fn:> [props]
            (r/% slim-entry/Entry
                 (Object.assign {}  props #{impl})))))
    (var components {:entry-brief  EntryBrief
                     :entry-detail EntryDetail
                     :create (r/const (fn:> [props] (r/% ui-static/Text props "CREATE")))})
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
    (var design {:type "light"})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table/TableRouter"} 
[:% n/Row
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
           :onPress (fn:> (setShowList (not showList)))}}]] 
[:% n/Row
       [:% slim-table/TableRouter
        #{{:design {:type "light"}
           :mini true
           :display {:detail {:type "card"
                              :header  {:main    {:type "title"
                                                  :template  ["currency_id"]}}
                              :body    {:title   {:type "title"
                                                  :template  ["currency_id"]}
                                        :main    {:type "v"
                                                  :body [{:type "pair"
                                                          :title {:template "B"}
                                                          :text  {:template ["balance"]}}
                                                         {:type "pair"
                                                          :title {:template "E"}
                                                          :text  {:template ["escrow"]}}]}
                                        :avatar  {:type "image"
                                                  :text  {:template  ["currency_id"]}
                                                  :image {:template  ["picture"]}}}}}}
          components views control}]]))))

^{:refer melbourne.slim-table/Table :added "0.1"}
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
    (var impl   {:type "card"
                 :sections {:body    [{:columns [{:name "B"
                                                  :template ["balance"]}
                                                 {:name "E"
                                                  :template ["escrow"]}]}]
                            :avatar {:template  ["currency_id"]
                                     :image ["picture"]}}})
    (var EntryBrief
         (r/const
          (fn:> [props]
            (r/% slim-entry/Entry
                 (Object.assign {} props
                              {:impl {:type "card"
                                      :body {:title {:template ["currency_id"]}}}})))))
    (var components {:entry-brief  EntryBrief
                     :create (r/const (fn:> [props] (r/% ui-static/Text props "CREATE")))})
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
    (var design {:type "light"})
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-table/Table"} 
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
           :onPress (fn:> (setShowList (not showList)))}}]] 
      [:% n/Row
       [:% slim-table/Table
        #{{:design {:type "light"}
           :display {:create {:type "fold"}}}
          components views control}]]))))

^{:refer melbourne.slim-table/TableStandard :added "4.0"}
(fact "creates the table"
  ^:hidden
  
  (defn.js TableStandardDemo
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
    (var impl   {:type "card"
                 :sections {:body    [{:columns [{:name "B"
                                                  :template ["balance"]}
                                                 {:name "E"
                                                  :template ["escrow"]}]}]
                            :avatar {:template  ["currency_id"]
                                     :image ["picture"]}}})
    (var EntryBrief
         (r/const
          (fn:> [props]
            (r/% slim-entry/Entry
                 (Object.assign {} props
                              {:impl {:type "card"
                                      :body {:title {:template ["currency_id"]}}}})))))
    (var components {:entry-brief  EntryBrief
                     :create (r/const (fn:> [props] (r/% ui-static/Text props "CREATE")))})
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
    (var design {:type "light"})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table/TableStandard"} 
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
           :onPress (fn:> (setShowList (not showList)))}}]] 
[:% n/Row
       [:% slim-table/TableStandard
        #{{:design {:type "light"}
           :display {:create {:type "fold"}}}
          components views control}]]))))

^{:refer melbourne.slim-table/TableEmbedded :added "4.0"}
(fact "creates the table"
  ^:hidden
  
  (defn.js TableEmbeddedDemo
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
    (var impl   {:type "card"
                 :sections {:body    [{:columns [{:name "B"
                                                  :template ["balance"]}
                                                 {:name "E"
                                                  :template ["escrow"]}]}]
                            :avatar {:template  ["currency_id"]
                                     :image ["picture"]}}})
    (var EntryBrief
         (r/const
          (fn:> [props]
            (r/% slim-entry/Entry
                 (Object.assign {} props
                              {:impl {:type "card"
                                      :body {:title {:template ["currency_id"]}}}})))))
    (var components {:entry-brief  EntryBrief
                     :create (r/const (fn:> [props] (r/% ui-static/Text props "CREATE")))})
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
    (var design {:type "light"})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table/TableEmbedded"} 
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
           :onPress (fn:> (setShowList (not showList)))}}]] 
[:% n/Row
       [:% slim-table/TableEmbedded
        #{{:design {:type "light"}
           :display {:create {:type "fold"}}}
          components views control}]])))

  (def.js MODULE (!:module)))
