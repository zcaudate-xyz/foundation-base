(ns melbourne.slim-link-test
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
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [melbourne.slim-link :as slim-link]
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
             [xt.event.base-model :as event-view]]
   :export [MODULE]})

^{:refer melbourne.slim-link/useViewLink :added "4.0"}
(fact "creates view link")

^{:refer melbourne.slim-link/FormLinkDropdown :added "4.0"}
(fact "creates a link dropdown from lookup"
  ^:hidden
  
  (defn.js FormLinkDropdownDemo
    []
    (var form   (ext-form/makeForm (fn:> {:account-id nil})
                                   {:account-id []}))
    (var views  (r/const {:account (ext-view/makeView
                                    {:defaultOutput []
                                     :defaultArgs []
                                     :defaultProcess (event-view/sorted-lookup "name")
                                     :handler
                                     (fn [args]
                                       (return
                                        (jc/future-delayed [100]
                                                          (return
                                                           (-> (data/arr-range 5)
                                                               (data/arr-map (fn:> [i]
                                                                            {:id   (+ "id-" i)
                                                                             :name (+ "name-" i)
                                                                             :balance (xt/x:random)})))))))})}))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
       {:label "melbourne.slim-link/FormLinkDropdown"
        :style {:height 200}} 
       [:% n/Row
        (r/% slim-link/FormLinkDropdown
             (Object.assign #{form views}
                       {:design {:type "light"}
                        :label "Account",
                        :field "account_id",
                        :fieldProps {:style {:width 500}
                                     :viewKey "account",
                                     :viewTemplate ["name"]}}))])])))



^{:refer melbourne.slim-link/FormLinkReadOnly :added "4.0"}
(fact "readonly link"
  ^:hidden

  (defn.js FormLinkReadOnlyDemo
    []
    (var form   (ext-form/makeForm (fn:> {:account-id "id-2"})
                                   {:account-id []}))
    (var views  (r/const {:account (ext-view/makeView
                                    {:defaultOutput []
                                     :defaultArgs []
                                     :defaultProcess (event-view/sorted-lookup "name")
                                     :handler
                                     (fn [args]
                                       (return
                                        (jc/future-delayed [100]
                                          (return
                                           (-> (data/arr-range 5)
                                               (data/arr-map (fn:> [i]
                                                            {:id   (+ "id-" i)
                                                             :name (+ "name-" i)
                                                             :balance (xt/x:random)})))))))})}))
    (var entry {:account-id "id-3"})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-link/FormLinkReadOnly"} 
[:% n/Row
       (r/% slim-link/FormLinkReadOnly
            (Object.assign #{form views entry}
                      {:design {:type "light"}
                       :label "Account",
                       :field "account_id",
                       :fieldProps {:style {:width 500}
                                    :viewKey "account",
                                    :viewTemplate ["name"]}}))]))))

^{:refer melbourne.slim-link/useViewLinkEntry :added "4.0"}
(fact "uses link entry")

^{:refer melbourne.slim-link/FormLinkEntryReadOnly :added "4.0"}
(fact  "readonly link"
  ^:hidden

  (defn.js FormLinkEntryReadOnlyDemo
    []
    (var form   (ext-form/makeForm (fn:> {:account-id "id-4"})
                                   {:account-id []}))
    (var views  (r/const {:account (ext-view/makeView
                                    {:defaultOutput []
                                     :defaultArgs []
                                     :defaultProcess (event-view/sorted-lookup "name")
                                     :handler
                                     (fn [args]
                                       (return
                                        (jc/future-delayed [100]
                                          (return
                                           (-> (data/arr-range 5)
                                               (data/arr-map (fn:> [i]
                                                            {:id   (+ "id-" i)
                                                             :name (+ "name-" i)
                                                             :balance (xt/x:random)})))))))})}))
    (var entry {:account-id "id-2"})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-link/FormLinkEntryReadOnly"} 
[:% n/Row
       (r/% slim-link/FormLinkEntryReadOnly
            (Object.assign #{form views entry}
                      {:design {:type "light"}
                       :label "Account",
                       :field "account_id",
                       :fieldProps {:style {:width 500}
                                    :viewKey "account",
                                    :viewTemplate ["name"]}}))])))

  (def.js MODULE (!:module)))


(comment
  {:type "field"
           :mini false
           :component "link_readonly"
           :label "Organisation"
           :field "organisation_id"
           :view  "organisation"
           :fieldProps
           {:viewKey "organisation"
            :viewTemplate ["name"]}})


(comment

  
^{:refer melbourne.slim-table-list/TableList :adopt true :added "0.1"}
(fact "creates the table list view"
  ^:hidden
  
  (defn.js TableListSearchDemo
    []
    (var views   (r/const {:list (ext-view/makeView
                                  {:handler
                                   (fn [args]
                                     (return
                                      (jc/future-delayed [100]
                                        (return
                                         (-> (data/arr-range 40)
                                             (data/arr-map (fn:> [i]
                                                          {:id (+ "id-" i)
                                                           :balance (xt/x:random)
                                                           :escrow  args})))))))})}))
    (var control (slim/useLocalControl))
    (var impl   {:type "card"
                 :body {:title  {:type "title"
                                 :template  ["currency_id"]}
                        :main   {:type "v"
                                 :body [{:type "h"
                                         :body [{:type "title"
                                                 :template "B"}
                                                {:template ["balance"]
                                                 :style {:marginLeft 10}
                                                 #_#_:format (fn:> [n] (j/toFixed n 2))}]}
                                        {:type "h"
                                         :body [{:type "title"
                                                 :template "E"}
                                                {:template ["escrow"]
                                                 :style {:marginLeft 10}}]}]}
                        :avatar {:type "image"
                                 :text  {:template  ["currency_id"]}
                                 :image {:template  ["picture"]}}}})
    (var EntryBrief
         (r/const
          (fn:> [props]
            (r/% slim-entry/Entry
                 (Object.assign {}
                  props
                  {:impl impl})))))
    (var [example setExample] (r/local "A"))
    (var components {:entry-brief  EntryBrief})
    (r/watch [example]
      (when example
        (j/delayed [100]
          (ext-view/refresh-args (. views list)
                                 [example]))))
    
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-table-search/TableListSearch"} 
[:% ui-input/Input
        {:design {:type "light"}
         :value example
         :onChangeText setExample}] 
[:% n/Row
        {:style {:height 400}}
        [:% n/ScrollView
         [:% slim-table-list/TableList
          #{{:mini true
             :design  {:type "light"}
             :style   {:minWidth 200}
             :display {:brief {:card {:component "mini"}}
                       :list  {}}}
            views control components}]]])]))
  
  (def.js MODULE (!:module)))

  
  ^{:refer js.react.ext-view/listenViewOutput :adopt true :added "4.0"}
  (fact "uses an async entry"

    (defn.js ListenViewOutputPane
      [#{view
         types}]
      (var output (ext-view/listenViewOutput
                   view types {}))
      (var getCount (r/useGetCount))
      (return
       [:% n/TextDisplay
        {:content (n/format-entry
                   {:types types
                    :result output
                    :count (getCount)
                    :view  (data/obj-pick view ["input" "output"])})}]))
    
    (defn.js ListenViewOutputDemo
      []
      (var view (ext-view/makeView
                 {:handler (fn:> [x y z]
                             (jc/future-delayed [500]
                                               (return (+ x y z))))
                  :defaultArgs [1 2 3]
                  :options {:init false}}))
      (var [types setTypes] (r/local ["pending" "disabled"]))
      (r/init []
              (ext-view/refresh-view view))
      (return
       (n/EnclosedCode 
{:label "js.react.ext-view/listenViewOutput"} 
[:% n/Row
         [:% n/Button
          {:title "R"
           :onPress (fn:> (ext-view/refresh-args
                           view
                           [(Math.random)
                            (Math.random)
                            (Math.random)]))}]
         [:% n/Text " "]
         [:% n/Button
          {:title "D"
           :onPress (fn []
                      (event-view/set-input view {})
                      (ext-view/refresh-view view))}]
         [:% n/TabsMulti
          {:data ["input" "output" "pending" "elapsed" "disabled"]
           :values types
           :setValues setTypes}]] 
[:% -/ListenViewOutputPane
         #{view types
           {:key types}}])))
    
    (def.js MODULE (!:module))
    
    (def +++
      (h/suppress
       (!.js
        (Object.assign test.web-00-rn.main/I09_RAW
                  (component.web-native/raw-controls)))))))
