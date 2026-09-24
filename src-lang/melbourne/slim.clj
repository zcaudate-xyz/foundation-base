(ns melbourne.slim
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.react :as r]
             [melbourne.slim-core :as slim-core]
             [melbourne.slim-table-list :as slim-table-list]
             [melbourne.slim-table-toolbar :as slim-table-toolbar]
             [melbourne.slim-table :as slim-table]
             [melbourne.slim-entry :as slim-entry]
             [melbourne.slim-sheet :as slim-sheet]]
   :export [MODULE]})

(def.js TableToolbar slim-table-toolbar/TableToolbar)

(def.js Entry slim-entry/Entry)

(def.js Table slim-table/Table)

(def.js TableList slim-table-list/TableList)

(def.js TableStandard slim-table/TableStandard)

(def.js TableEmbedded slim-table/TableEmbedded)

(def.js Sheet slim-sheet/Sheet)

(def.js SheetHeader slim-sheet/SheetHeader)

(def.js SheetRow slim-sheet/SheetRow)

(def.js SheetBasic slim-sheet/SheetBasic)

(defn.js createEntry
  [props ...args]
  (return
   (r/% slim-entry/Entry props ...args)))

(defn.js entry
  [props impl opts]
  (return
   (r/% slim-entry/Entry (Object.assign {} props #{impl} opts))))

(def.js useLocalPrimitives slim-core/useLocalPrimitives)
(def.js useRoutePrimitives slim-core/useRoutePrimitives)
(def.js useListControl slim-core/useListControl)
(def.js useRouteControl slim-core/useRouteControl)
(def.js useLocalControl slim-core/useLocalControl)
(def.js getParentProps slim-core/getParentProps)
(def.js useParentControl slim-core/useParentControl)

(def.js MODULE (!:module))
