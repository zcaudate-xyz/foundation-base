(ns melbourne.slim-table-toolbar
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.ui-util :as ui-util]
             [melbourne.ui-toolbar :as ui-toolbar]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-text-dialog :as ui-text-dialog]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js TableToolbar
  "creates the table toolbar"
  {:added "0.1"}
  [#{[(:= toolbarOpts {})
      (:.. rprops)]}]
  (var #{design
         mini
         control
         children
         actions} rprops)
  (var #{[(:= showCreate true)
          (:= showOrderBy true)]}
       toolbarOpts)
  (var createElem
       [:% ui-text/ToggleAccent
        #{[design
           :variant (ui-toolbar/accentStandard)
           :text [:% n/Icon
                  {:key "create"
                   :name (:? (not (. control showList))
                             "left"
                             "plus")}]
           :onPress  (fn []
                       (cond (or (not (. control showList))
                                 (. control showCreate))
                             (do (. control (setShowCreate false))
                                 (. control (setShowList true)))
                             
                             :else
                             (. control (setShowCreate true))))]}])
  (var editElem
       [:% ui-text/ToggleAccent
        #{[design
           :variant (ui-toolbar/accentStandard)
           :text "EDIT"
           :onPress  (fn [])]}])
  (var deleteElem
       [:% ui-text-dialog/ConfirmDialog
        #{[design
           :variant (ui-toolbar/accentStandard)
           :text "DELETE"
           :onPress  (fn:> []
                       (. actions (delete (. control showDetail))))]}])
  (var orderByElem
       [:% ui-util/Fade
        {:visible (or (not mini)
                      (. control showList))}
        [:% ui-text/TabsMinor
         #{[design
            :variant (ui-toolbar/minorStandard)
            :styleContainer {:margin 0}
            :style {:marginHorizontal 3}
            :data ["name" "time"]
            :value (. control orderBy)
            :setValue (. control setOrderBy)
            :format j/toUpperCase]}]])
  (return
   [:% ui-toolbar/Toolbar
    #{design
      {:style {:paddingTop 3
               :height 36}}}
    (:? showCreate createElem)
    children
    (:? (and (not (. control showDetail))
             showOrderBy) orderByElem)
    [:% n/Fill]]))

(def.js MODULE (!:module))
