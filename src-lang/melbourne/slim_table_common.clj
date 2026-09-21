(ns melbourne.slim-table-common
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [js.react.ext-model :as ext-view]
             [js.react.ext-form :as ext-form]
             [js.react-native.ui-util :as ui-util]
             [xt.event.base-form :as event-form]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-static :as ui-static]
             [melbourne.slim-entry :as slim-entry]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js useTableEntry
  "uses the table entry"
  {:added "4.0"}
  [props]
  (var #{views displayKey control} props)
  #_(trace/LOG! {:out (data/get-in views ["topic_archive"
                                 "output"
                                 "current"])
           :remote (data/get-in views ["topic_archive"
                                 "remote"
                                 "current"])
           :in  (data/get-in views ["topic_archive"
                                 "input"
                                 "current"
                                 "data"])}
          #_#{views displayKey control})
  (var entryId (or (. control showDetail)
                   (. control showModify)))
  (var entries (or (. props entries)
                   (ext-view/listenView (. views [(or displayKey "list")]) "success")
                   []))
  (var remote-entries (or (data/get-in views [displayKey
                                           "remote"
                                           "current"])
                          []))
  (when (lib/is-object? remote-entries)
    (:= remote-entries []))
  (var entry   (or (j/find entries (fn:> [e] (== entryId (. e id))))
                   (j/find remote-entries (fn:> [e] (== entryId (. e id))))
                   {}))
  (return entry))

(defn.js TableDefaultNotFound
  "default not found placeholder"
  {:added "4.0"}
  [#{design}]
  (return [:% ui-text/H4 #{design} "NOT FOUND"]))

(defn.js TableDefaultIsLoading
  "default is loading placeholder"
  {:added "4.0"}
  [#{design}]
  (return [:% ui-text/H4 #{design} "LOADING..."]))

(defn.js TableBackButton
  "creates a back button"
  {:added "4.0"}
  [#{design
     control}]
  (return
   [:% ui-text/ButtonMinor
    #{design
      {:text  [:% n/Icon
               {:key "back",
                :name "chevron-thin-left"
                :style {:padding 0}}]
       :style {:width 30
               :height 27
               :paddingHorizontal 0
               :textAlign "center"}
       :onPress (fn []
                  (cond (. control showModify)
                        (. control (setShowModify false))

                        :else
                        (do (. control (setShowCreate false))
                            (. control (setShowDetail nil)))))}}]))

(defn.js tablePageHooks
  [props hooks]
  (cond (lib/is-function? hooks)
        (return (hooks props))

        (lib/is-array? hooks)
        (return
         (data/arr-foldl hooks
                      (fn [init f]
                        (var out (f init))
                        (return
                         (Object.assign init out)))
                      (Object.assign {} props)))))

(defn.js TableProgressBack
  [props]
  (var #{control
         entry} props)
  (r/useCountdown 1
                  (fn []
                    (when (data/is-empty? entry)
                      (. control (setShowDetail nil)))))
  (return
   [:% n/View
    {:style {:flex 1
             :alignItems "center"
             :justifyContent "center"}}
    [:% n/ActivityIndicator]]))

(defn.js tablePageView
  "displays the detail view"
  {:added "0.1"}
  [props page]
  (var #{[(:= display {})
          (:= displayKey "list")
          design
          views
          entries
          control
          components]} props)
  (var variant {:bg {:key "background"
                     :tone "sharpen"}})
  (var entry (or (and views 
                      (-/useTableEntry #{views
                                         entries
                                         displayKey
                                         control}))
                 (. props entry)))
  (var impl (. display [page]))
  (var implForm  (data/get-in impl ["form"]))
  (var form (or (. props form)
                implForm))
  (cond (lib/is-array? form)
        (do 
          (:= form (ext-form/makeForm
                    (fn:>
                      ((data/first implForm) entry props))
                    ((data/second implForm) entry props)))
          (ext-form/listenFormData form)))
  
  (var hooks (. components [(+ "hooks_" page)]))
  (var hprops (-/tablePageHooks (Object.assign {}
                                 props
                                 #{entry})
                                hooks))
  
  ;;
  ;; Short Circuit
  ;;
  
  (if (and (data/is-empty? entry)
           (== page "detail"))
    (return
     (r/% -/TableProgressBack (Object.assign {} props #{entry}))))


  ;;
  ;; Components
  ;;
  
  (var HeaderComponent
       (or (. components [(+ "header_" page)])
           (and impl
                (. impl header)
                slim-entry/Entry)))
  (var PageComponent
       (or (. components [(+ "entry_" page)])
           (and impl
                slim-entry/Entry)
           -/TableDefaultNotFound))

  (var implHeader (or (. impl header)
                      {}))
  (:= implHeader
      {:main  {:type "v"
               #_#_:variant {:bg {:key "primary"
                              :mix "background"
                              :ratio 7}}
               :style {:paddingTop 10
                       :paddingBottom 5
                       :minHeight 30
                       :maxWidth 500}
               :body (data/arrayify (. implHeader main))}})
  
  (var implHeaderActions (. impl header-actions))
  (var headerElem
       (:? HeaderComponent
           (r/% HeaderComponent
                (Object.assign {}
                 props
                 hprops
                 {:entry entry
                  :form form
                  :impl (:? (data/get-in impl ["header" "link"])
                            (Object.assign {:type "link"}
                                      (. impl header))

                            implHeader
                            {:type "card"
                             :body (Object.assign {:isHeader true}
                                             implHeader)}

                            :else nil)}))))

  (var headerActionsElem
       (:? (and (. control showHeader)
                implHeaderActions)
           [:% n/View
            {:style {}}
            (r/% slim-entry/Entry
                 (Object.assign {}
                  props
                  hprops
                  {:entry entry
                   :form form
                   :impl implHeaderActions}))]))
  
  (var pageElem
       (r/% PageComponent
            (Object.assign {}
             props
             hprops
             {:entry entry
              :form form
              :impl (. display [page])})))
  (return
   [:% ui-static/Div
    {:style {:flex 1
             #_#_:backgroundColor (:? (== page "detail")
                                  "red")}
     :design design}
    
    (:? (. control showHeader)
        [:% n/Row headerElem] )
    
    headerActionsElem
    (:? (and (. control showScroll)
             (not= false (. impl scroll)))
        (r/% ui-static/ScrollView
             #{design}
             pageElem)
        pageElem)]))

(comment

  #_(:? 
       [:% n/View
      {}
         #_[:% n/View
          {:style {:marginVertical 10
                   :marginHorizontal 4}}
          [:% -/TableBackButton
           #{design control}]]
      ])
  
  #_(:? (== false (data/get-in display [routeKey "scroll"]))
        
        (r/% ui-static/ScrollView
             #{design}
             routeElem)))

(def.js MODULE (!:module))
