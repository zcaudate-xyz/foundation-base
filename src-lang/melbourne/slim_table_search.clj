(ns melbourne.slim-table-search
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react.ext-model :as ext-view]
             [js.react.ext-route :as ext-route]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [js.react-native.ui-util :as ui-util]
             [js.react-native.ui-router :as ui-router]
             [melbourne.ui-toolbar :as ui-toolbar]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-swiper :as ui-swiper]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-section :as ui-section]
             [melbourne.slim-entry :as slim-entry]
             [melbourne.slim-table-common :as slim-table-common]
             [melbourne.slim-table-list :as slim-table-list]
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

(defn.js TableListSearch
  "creates the table list view"
  {:added "0.1"}
  [props]
  (var #{[entries
          (:.. rprops)]} props)
  (var #{[design
          mini
          actions
          views
          display
          components
          control
          style
          (:= displayKey "list")]} rprops)
  (var impl (or (data/get-in display ["list"])
                {}))
  (:= impl (:? (. impl props)
               (Object.assign {} impl ((. impl props) impl props))
               impl))
  (var #{[top
          bottom
          (:= filterFn lib/identity)
          (:= sortFn lib/identity)]} impl)
  (:= entries (-> (or entries
                      (ext-view/listenView (. views [displayKey]) "success")
                      [])
                  (sortFn (. control orderBy))))
  (var topElem
       (:? top
         (r/% slim-entry/Entry (Object.assign {} props {:impl top}))))
  (var bottomElem
       (:? bottom
           (r/% slim-entry/Entry (Object.assign {} props {:impl bottom}))))
  (var centerElem
       (:? (== "row" (. impl type))
           (r/% slim-sheet/Sheet (Object.assign {} props #{impl entries}))

           
           :else
           (r/% slim-table-list/TableListView (Object.assign {} props #{impl entries}))))
  (return
   [:% n/View
    topElem
    centerElem
    bottomElem]))

(def.js MODULE (!:module))
