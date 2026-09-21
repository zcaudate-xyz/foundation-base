(ns melbourne.slim-sheet
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-static :as ui-static]
             [melbourne.base-font :as base-font]
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

(defn.js SheetPagination
  "creates a sheet pagination"
  {:added "4.0"}
  [props]
  (var #{entries
         impl
         design
         control} props)
  (var #{[(:= page {})]} impl)
  (var #{[(:= display 20)
          (:= total (:? entries
                        (xt/x:len entries)
                        0))]} page)
  (var setShowPage (data/get-in control ["setShowPage"]))
  (var showPage    (or (data/get-in control ["showPage"])
                       1))
  (var pageCount (+ (xt/x:m-floor (/ (- total 1) display))
                    1))

  (var isMini (< pageCount 7))
  (var isLeftEdge  (< showPage 4))
  (var isRightEdge (> showPage (- pageCount 3)))
  (var toggleFn
       (fn:> [idx ellipsis]
         (r/% (or (. page component)
                  ui-text/ToggleTabMinor)
              {:design design
               :key (+ idx 1)
               :style {:marginHorizontal 0}
               :selected (== showPage (+ idx 1))
               :text (:? ellipsis
                         [:% n/Icon
                          {:key  (+ idx 1)
                           :name "dots-three-horizontal"}]
                         (j/toString (+ idx 1)))
               :onPress (fn:> (setShowPage (+ idx 1)))})))
  
  (return
   [:% n/View
    {:key pageCount}
    (:? isMini
        [:% n/Row
         (data/arr-map (data/arr-range pageCount) toggleFn)]
        
        isLeftEdge
        [:% n/Row
         (data/arr-map (data/arr-range 4) toggleFn)
         (toggleFn (+ showPage 1) true)
         (toggleFn (- pageCount 1))]

        isRightEdge
        [:% n/Row
         (toggleFn 0)
         (toggleFn (- showPage 3) true)
         (data/arr-map (data/arr-range [(- pageCount 4) pageCount])
                    toggleFn)]
        
        :else
        [:% n/Row
         (toggleFn 0)
         (toggleFn (- showPage 3) true)
         (data/arr-map (data/arr-range [(- showPage 2) (+ showPage 1)])
                    toggleFn)
         (toggleFn (+ showPage 1) true)
         (toggleFn (- pageCount 1))])]))

(defn.js SheetGroupHeader
  "creates a sheet group header"
  {:added "4.0"}
  [#{[design
      variant
      group
      style
      styleContainer
      (:.. iprops)]}]
  (var #{[name
          (:= format lib/identity)]} group)
  (return
   [:% n/View
    {:style {:marginTop 10}}
    [:% n/Row
     {:style styleContainer}
     [:% ui-text/H6
      #{design 
        {#_#_:variant {:fg {:key "background"}
                   :bg {:key "neutral"}}
         :style [{:fontWeight 900
                  :borderRadius 2
                  :paddingVertical 3}
                 (:.. (data/arrayify style))]}}
      (format name)]]
    [:% ui-static/Separator
     #{design
       {:variant {:fg {:key "background"
                       :mix "primary"
                       :ratio 1}}
        :style {:marginBottom 10}}}]]))

;;
;; Entry Row
;;

(defn.js SheetHeader
  "creates a sheet header"
  {:added "4.0"}
  [#{[design
      impl
      (:= variant {:fg {:key "background"}
                   :bg {:key "primary"}})
      (:= custom {})
      style
      (:.. iprops)]}]
  
  (var #{[columns
          (:= header {})]} impl)
  (var columnFn
       (fn [column i]
         (var #{key} column)
         (var #{[style
                 (:.. rprops)]} (or (data/get-in custom [key])
                                    {}))
         (var Component slim-entry/EntryContentTitleH5)
         (var oprops (Object.assign {}
                      #{[design
                         :impl  (Object.assign {}
                                 column
                                 header
                                 {:variant variant
                                  :template (. column name)}
                                 (. column header))
                         :style [{:paddingVertical 5
                                  :paddingHorizontal 10}
                                 (:.. (data/arrayify style))]]}
                      rprops))
         (return
          [:% n/View
           {:key i
            :style [{:flex 1}
                    (. column style)]}
           (r/% Component oprops)])))
  (return
   [:% ui-static/Div
    #{[design variant
       :style [{:flexDirection "row"
                :maxWidth 500
                :marginBottom 10
                :paddingHorizontal 10
                :alignItems "center"}
               (:.. (data/arrayify style))]]}
    (j/map columns columnFn)]))

(defn.js SheetRow
  "creates a sheet row"
  {:added "4.0"}
  [#{[design
      variant
      impl
      style
      (:= custom {})
      (:.. iprops)]}]
  (var #{[columns]} impl)
  (var columnFn
       (fn [column i]
         (var #{key} column)
         (var #{[style
                 (:.. rprops)]} (or (data/get-in custom [key])
                                    {}))
         (var Component (or (and (. column type)
                                 slim-entry/Entry)
                            slim-entry/EntryContentParagraph))
         (var oprops (Object.assign {}
                      iprops
                      #{[design
                         variant
                         :impl  column
                         :style [{:paddingHorizontal 10}
                                 (:.. (data/arrayify style))]]}
                      rprops))
         (return
          [:% n/View
           {:key i
            :style [{:flex 1}
                    (. column style)]}
           (r/% Component oprops)])))
  (return
   [:% ui-static/Div
    #{[design variant
       :style [{:flexDirection "row"
                :alignItems "center"
                :maxWidth 500
                :marginBottom 3
                :paddingLeft 10
                :paddingRight 20}
               (:.. (data/arrayify style))]]}
    (j/map columns columnFn)]))

(defn.js SheetBasicRows
  "creates a basic sheet"
  {:added "4.0"}
  [props]
  (var #{design
         entries
         impl
         style} props)
  (return
   [:% n/View
    {:style {:flex 1}}
    (j/map entries
           (fn:> [entry i]
             (r/% -/SheetRow
                  (Object.assign {}
                   props
                   {:key (+ (or (. entry id)
                                "")
                            i)
                    :entry entry}))))]))

(defn.js SheetBasic
  "creates a basic sheet"
  {:added "4.0"}
  [props]
  (var #{design
         entries
         impl
         style} props)
  (return
   [:<>
    (r/% -/SheetHeader props)
    [:% ui-static/ScrollView
     {:design design
      :style {:marginTop 10}}
     (r/% -/SheetBasicRows props)]]))

(defn.js SheetGroupRows
  [props]
  (var #{[group
          (:.. rprops)]} props)
  (var #{entries} group)
  (return
   [:% n/View
    (r/% -/SheetGroupHeader props)
    (r/% -/SheetBasicRows (Object.assign rprops #{entries}))]))

(defn.js groupEntries
  [entries impl]
  (var itemsImpl   (Object.assign {:reverse false
                              :sort lib/identity
                              :filter lib/identity}
                             (. impl items)))
  (var groupsImpl  (Object.assign {:reverse false
                              :split  data/id-fn
                              :sort   lib/identity
                              :filter lib/T}
                             (. impl groups)))
  (var groups (-> (or entries [])
                  (data/arr-group-by (data/template-fn (. groupsImpl split))
                                  lib/identity)
                  (data/obj-map (. itemsImpl sort))
                  (data/obj-pairs)
                  ((. groupsImpl sort))))
  (return groups))

(defn.js Sheet
  "creates a sheet"
  {:added "4.0"}
  [props]
  (var #{noHeader
         impl
         entries} props)
  (var itemsImpl   (Object.assign {:reverse false
                              :sort lib/identity
                              :filter lib/identity}
                             (. impl items)))
  (var isGrouped   (lib/not-nil? (. impl groups)))
  (var isPaged     (lib/not-nil? (. impl page)))

  (cond isGrouped
        (do (var groups (-/groupEntries entries impl))
            (return [:% n/View
                     (:? (not noHeader)
                         (r/% -/SheetHeader props))
                     (j/map groups
                            (fn:> [[name entries]]
                              (r/% -/SheetGroupRows
                                   (Object.assign {} props
                                                {:group #{name entries
                                                          {:format (. impl groups format)}}}
                                                ))))]))
        
        isPaged
        (do (return [:% n/View]))
        
        :else
        (do (return [:% n/View
                     (:? (not noHeader)
                         (r/% -/SheetHeader props))
                     (r/% -/SheetBasicRows (Object.assign {} props {:entries (. itemsImpl (sort entries))}))])))
  
  
  (return
   (r/% -/SheetBasicRows props)))

(def.js MODULE (!:module))
