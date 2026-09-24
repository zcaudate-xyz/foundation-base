(ns lang-main.ui-components.sections.web-tama
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :import [["@tamagui/config/v4" :as #{defaultConfig}]]
   :config {:id :test/tama-demo
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.tamagui :as tm]
             [melbourne.tama-test :as tama-test]]
   :export [MODULE]})

(defrun.js __import__
  (:- :import (quote [React]) :from "'react'"))

(def.js TAMA-CONFIG (tm/createTamagui defaultConfig))

(defn.js TamaProvider
  [props]
  (return
   [:% tm/TamaguiProvider
    {:config -/TAMA-CONFIG
     :defaultTheme "light"}
    (. props children)]))

(defn.js TamaEntryExamples
  []
  (return
   [:% -/TamaProvider
    [:<>
     [:% tama-test/EntryDemo]
     [:% tama-test/CreateEntryDemo]
     [:% tama-test/EntryOptionsDemo]]]))

(defn.js TamaTableExamples
  []
  (return
   [:% -/TamaProvider
    [:<>
     [:% tama-test/TableDemo]
     [:% tama-test/TableToolbarDemo]
     [:% tama-test/TableListDemo]
     [:% tama-test/TableStandardDemo]
     [:% tama-test/TableEmbeddedDemo]]]))

(defn.js TamaSheetExamples
  []
  (return
   [:% -/TamaProvider
    [:<>
     [:% tama-test/SheetHeaderDemo]
     [:% tama-test/SheetRowDemo]
     [:% tama-test/SheetBasicDemo]
     [:% tama-test/SheetDemo]]]))

(defn.js tama-controls
  []
  (return
   (tab ["201a-tama-entry" -/TamaEntryExamples]
        ["201b-tama-table" -/TamaTableExamples]
        ["201c-tama-sheet" -/TamaSheetExamples])))

(def.js MODULE (!:module))
