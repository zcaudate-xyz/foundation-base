(ns melbourne.slim-table-list-test
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
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-group :as ui-group]
             [melbourne.slim :as slim]
             [melbourne.slim-table-list :as slim-table-list]
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

^{:refer melbourne.slim-table-list/TableListCardBrief :added "0.1"}
(fact "creates the mini row"
  ^:hidden
  
  (defn.js TableListCardBriefDemo
    []
    (var entry {:id "id-0"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (var control (slim/useLocalControl))
    (var design {:type "light"})
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
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListCardBrief"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:width 200}}
        [:% slim-table-list/TableListCardBrief
         #{{:design {:type "light"}}
           entry control}
         [:% slim-entry/Entry
          #{{:design {:type "light"}}
            entry impl}]]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:width 200}}
        [:% slim-table-list/TableListCardBrief
         #{{:design {:type "dark"}}
           entry control}
         [:% slim-entry/Entry
          #{{:design {:type "dark"}}
            entry impl}]]]]))))

^{:refer melbourne.slim-table-list/TableListCardNav :added "4.0"}
(fact "creates the mini row"
  ^:hidden
  
  (defn.js TableListCardNavDemo
    []
    (var entry {:id "id-0"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (var control (slim/useLocalControl))
    (var design {:type "light"})
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
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListCardNav"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:width 200}}
        [:% slim-table-list/TableListCardNav
         #{{:design {:type "light"}}
           entry control}
         [:% slim-entry/Entry
          #{{:design {:type "light"}}
            entry impl}]]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:width 200}}
        [:% slim-table-list/TableListCardNav
         #{{:design {:type "dark"}}
           entry control}
         [:% slim-entry/Entry
          #{{:design {:type "dark"}}
            entry impl}]]]]))))

^{:refer melbourne.slim-table-list/TableListCardSwipe :added "0.1"}
(fact "creates the table swipe row element (mini)"
  ^:hidden
  
  (defn.js TableListCardSwipeDemo
    []
    (var entry {:id "id-0"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (var control (slim/useLocalControl))
    (var design {:type "light"})
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
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListCardSwipe"} 
[:% n/Row
        [:% ui-static/Div
         {:design {:type "light"}}
         [:% slim-table-list/TableListCardSwipe
          #{{:design {:type "light"}
             :style {:width 200}}
            entry control}
          [:% slim-entry/Entry
           #{{:design {:type "light"}}
             entry impl}]]]
        [:% ui-static/Div
         {:design {:type "dark"}}
         [:% slim-table-list/TableListCardSwipe
          #{{:design {:type "dark"}
             :style {:width 200}}
            entry control}
          [:% slim-entry/Entry
           #{{:design {:type "dark"}}
             entry impl}]]]])])))

^{:refer melbourne.slim-table-list/TableListCardFold :added "0.1"}
(fact "displays a standard row"
  ^:hidden
  
  (defn.js TableListCardFoldDemo
    []
    (var entry {:id "id-0"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (var control (slim/useLocalControl))
    (var design {:type "light"})
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
    (var EntryDetail
         (r/const
          (fn:> [props]
            (r/% slim-entry/Entry
                 (Object.assign {} props #{impl})))))
    (var components {:entry-detail EntryDetail})
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListCardFold"} 
[:% n/Row
        [:% ui-static/Div
         {:design {:type "light"}}
         [:% slim-table-list/TableListCardFold
          #{{:design {:type "light"}
             :style {:width 200}
             :components components}
            entry control}
          [:% slim-entry/Entry
           #{{:design {:type "light"}
              :impl {:type "card"
                     :body {:title {:template ["currency_id"]}}}}
             entry}]]]
        [:% ui-static/Div
         {:design {:type "dark"}}
         [:% slim-table-list/TableListCardFold
          #{{:design {:type "dark"}
             :style {:width 200}
             :components components}
            entry control}
          [:% slim-entry/Entry
           #{{:design {:type "dark"}
              :impl {:type "card"
                     :body {:title {:template ["currency_id"]}}}}
             entry}]]]])])))

^{:refer melbourne.slim-table-list/TableListCard :added "0.1"}
(fact "creates the table card"
  ^:hidden
  
  (defn.js TableListCardDemo
    []
    (var entry {:id "id-0"
                :currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (var control (slim/useLocalControl))
    (var design {:type "light"})
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
                 (Object.assign {} props
                              {:impl {:type "card"
                                      :body {:title {:template ["currency_id"]}}}})))))
    (var EntryDetail
         (r/const
          (fn:> [props]
            (r/% slim-entry/Entry
                 (Object.assign {}  props #{impl})))))
    (var components {:entry-brief  EntryBrief
                     :entry-detail EntryDetail})
    (var [type setType]   (r/local "fold"))
    (var [swipe setSwipe] (r/local "left"))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListCard"} 
[:% n/Row
        [:% ui-text/TabsAccent
         {:value type
          :setValue setType
          :data ["fold" "mini" "swipe"]}]
        [:% ui-text/TabsAccent
         {:value swipe
          :setValue setSwipe
          :data ["left" "right"]}]] 
[:% n/Row
        [:% ui-static/Div
         {:design {:type "light"}}
         [:% slim-table-list/TableListCard
          #{{:key swipe
             :design {:type "light"}
             :style {:width 200}
             :display {:brief {:card {:component type}}
                       :swipe {:direction swipe
                               :showDelete false}}}
            entry control components}
          [:% slim-entry/Entry
           #{{:design {:type "light"}
              :impl {:type "card"
                     :body {:title {:template ["currency_id"]}}}}
             entry}]]]
        [:% ui-static/Div
         {:design {:type "dark"}}
         [:% slim-table-list/TableListCard
          #{{:key swipe
             :design {:type "dark"}
             :style  {:width 200}
             :display {:brief {:card {:component type}}
                       :swipe {:direction swipe
                               :showDelete false}}}
            entry control components}
          [:% slim-entry/Entry
           #{{:design {:type "dark"}
              :impl {:type "card"
                     :body {:title {:template ["currency_id"]}}}}
             entry}]]]])])))

^{:refer melbourne.slim-table-list/TableListViewEntries :added "4.0"
  :setup [(def +entries+
            (shuffle
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
               :escrow 37}]))]}
(fact "creates a group row of sheets"
  ^:hidden
  
  (defn.js TableListViewEntriesDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListViewEntries"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-table-list/TableListViewEntries
         {:design {:type "light"}
          :entries (@! +entries+)
          :display {:brief  {:type "v"
                             :body [{:template ["name"]}
                                    {:template ["balance"]}
                                    {:template ["escrow"]
                                     :style {:textAlign "right"}}]}}
          :impl   {}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-table-list/TableListViewEntries
         {:design {:type "dark"}
          :entries (@! +entries+)
          :display {:brief  {:type "v"
                             :body [{:template ["name"]}
                                    {:template ["balance"]}
                                    {:template ["escrow"]
                                     :style {:textAlign "right"}}]}}
          :impl {}}]]]))))

^{:refer melbourne.slim-table-list/TableListViewGroup :added "4.0"
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
  
  (defn.js TableListViewGroupDemo
    []
    (var entry {:currency-id "STATS"
                :balance 1000
                :escrow 50.5})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListViewGroup"} 
[:% n/Row
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-table-list/TableListViewGroup
         {:design {:type "light"}
          :group (@! +group+)
          :display {:brief  {:type "v"
                             :body [{:template ["name"]}
                                    {:template ["balance"]}
                                    {:template ["escrow"]
                                     :style {:textAlign "right"}}]}}
          :impl {}}]]
       [:% ui-static/Div
        {:design {:type "dark"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-table-list/TableListViewGroup
         {:design {:type "dark"}
          :group (@! +group+)
          :display {:brief  {:type "v"
                             :body [{:template ["name"]}
                                    {:template ["balance"]}
                                    {:template ["escrow"]
                                     :style {:textAlign "right"}}]}}
          :impl {}}]]]))))

^{:refer melbourne.slim-table-list/TableListViewBase :added "0.1"}
(fact "TODO")

^{:refer melbourne.slim-table-list/usePageEntries :added "4.0"}
(fact "breaks down entries into paged results")

^{:refer melbourne.slim-table-list/TableListViewPagedScaffold :added "0.1"}
(fact "TODO")

^{:refer melbourne.slim-table-list/TableListViewPaged :added "4.0"
  :setup [(def +entries+
            (mapv (fn [i]
                    {:id   (str "id-" i)
                     :name (str "name-" i)
                     :amount (rand)})
                  (range 100)))]}
(fact "creates a paged table"
  ^:hidden
  
  (defn.js TableListViewPagedDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListViewPaged"} 
[:% n/View
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-table-list/TableListViewPaged
         {:design {:type "light"}
          :control {}
          :impl   {:page    {:display 5} 
                   :header  {:format j/toUpperCase}}
          :display {:brief  {:type "v"
                             :body [{:template ["name"]}
                                    {:template ["amount"]}]}}
          :entries  (@! +entries+)}]]]))))

^{:refer melbourne.slim-table-list/useRemotePagedEntries :added "4.0"}
(fact "gets the remotely controlled entries")

^{:refer melbourne.slim-table-list/TableListViewRemotePagedScaffold :added "0.1"}
(fact "TODO")

^{:refer melbourne.slim-table-list/TableListViewRemotePaged :added "4.0"}
(fact "creates the remote paging view"
  ^:hidden
  
  (defn.js TableListViewRemotePagedDemo
    []
    (var views {:list (ext-view/makeView
                       {:handler (fn:> [showPage display]
                                   (jc/future-delayed [200]
                                     (return
                                      (data/arr-map (data/arr-range display)
                                                 (fn:> [i]
                                                   {:id   (+ "id-" (+ (* (- showPage 2) display)
                                                                      display
                                                                      i))
                                                    :name (+ "name-" (+ (* (- showPage 2) display)
                                                                        display
                                                                        i))
                                                    :amount (xt/x:random)})))))
                        :options {:init false}})})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListViewRemotePaged"} 
[:% n/View
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-table-list/TableListViewRemotePaged
         {:design {:type "light"}
          :control {}
          :views  views
          :impl   {:page    {:display 5
                             :total 100} 
                   :header  {:format j/toUpperCase}}
          :display {:brief  {:type "v"
                             :body [{:template ["name"]}
                                    {:template ["amount"]}]}}}]]]))))

^{:refer melbourne.slim-table-list/TableListView :added "4.0"
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
(fact "creates a table list view"
  ^:hidden
  
  (defn.js TableListViewDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableListView"} 
[:% n/View
       [:% ui-static/Div
        {:design {:type "light"}
         :style {:flex 1
                 :padding 5}}
        [:% slim-table-list/TableListView
         {:design {:type "light"}
          :impl   {:groups  {:split ["currency_id"]}
                   :items   {:sort (fn:> [arr] (sort-by/sort-by arr [["name" true] "balance"]))}
                   :header  {:format j/toUpperCase}}
          :display {:brief  {:type "v"
                             :body [{:template ["name"]}
                                    {:template ["balance"]}
                                    {:template ["escrow"]
                                     :style {:textAlign "right"}}]}}
          :entries  (@! +entries+)}]]]))))

^{:refer melbourne.slim-table-list/TableList :added "0.1"}
(fact "creates the table list view"
  ^:hidden
  
  (defn.js TableListDemo
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
                          :body {:title {:template ["currency_id"]}}}})))))
    (var EntryDetail
         (r/const
          (fn:> [props]
            (r/% slim-entry/Entry
                 (Object.assign {} props #{impl})))))
    (var components {:entry-brief  EntryBrief
                     :entry-detail EntryDetail})
    (var [type setType]   (r/local "fold"))
    (var [swipe setSwipe] (r/local "left"))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-table-list/TableList"} 
[:% n/Row
        [:% ui-text/TabsAccent
         {:value type
          :setValue setType
          :data ["fold" "swipe" "mini"]}]
        [:% ui-text/TabsAccent
         {:value swipe
          :setValue setSwipe
          :data ["left" "right"]}]] 
[:% n/Row
        [:% slim-table-list/TableList
         #{{:mini true
            :design {:type "light"}
            :style {:width 200}
            :display {:brief {:card {:component type}}
                      :swipe {:direction swipe
                              :showDelete false}}}
           views control components}]
        [:% slim-table-list/TableList
         #{{:mini true
            :design {:type "dark"}
            :style {:width 200}
            :display {:brief {:card {:component type}}
                      :swipe {:direction swipe
                              :showDelete false}}}
           views control components}]])]))

  (def.js MODULE (!:module)))