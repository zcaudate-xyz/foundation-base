(ns component.web-tama-slim
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :import [["@tamagui/config/v4" :as #{defaultConfig}]]
   :config {:id :test/tama-slim-demo
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.tamagui :as tm]
             [melbourne.tama-slim-test :as tama-slim-test]]
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

(defn.js TamaSlimPage
  [Target]
  (return
   [:% -/TamaProvider
    [:% Target]]))

(defn.js TamaSlimCommonExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimCommonDemo)))

(defn.js TamaSlimNumberExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimNumberDemo)))

(defn.js TamaSlimSelectExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimSelectDemo)))

(defn.js TamaSlimImageExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimImageDemo)))

(defn.js TamaSlimLinkExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimLinkDemo)))

(defn.js TamaSlimErrorExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimErrorDemo)))

(defn.js TamaSlimSubmitExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimSubmitDemo)))

(defn.js TamaSlimDialogExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimDialogDemo)))

(defn.js TamaSlimEntryExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimEntryDemo)))

(defn.js TamaSlimPopupExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimPopupDemo)))

(defn.js TamaSlimSheetExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimSheetDemo)))

(defn.js TamaSlimTablemExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimTablemDemo)))

(defn.js TamaSlimTablegExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimTablegDemo)))

(defn.js TamaSlimTableExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimTableDemo)))

(defn.js TamaSlimTablepExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimTablepDemo)))

(defn.js TamaSlimTablesExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimTablesDemo)))

(defn.js TamaSlimTablexExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimTablexDemo)))

(defn.js TamaSlimOverviewExamples
  []
  (return (-/TamaSlimPage tama-slim-test/TamaSlimDemo)))

(defn.js tama-controls
  []
  (return
   (tab ["201a-slim-common" -/TamaSlimCommonExamples]
        ["201b-slim-number" -/TamaSlimNumberExamples]
        ["201c-slim-select" -/TamaSlimSelectExamples]
        ["201e-slim-image" -/TamaSlimImageExamples]
        ["201f-slim-link" -/TamaSlimLinkExamples]
        ["201g-slim-error" -/TamaSlimErrorExamples]
        ["201h-slim-submit" -/TamaSlimSubmitExamples]
        ["201i-slim-dialog" -/TamaSlimDialogExamples]
        ["201j-slim-entry" -/TamaSlimEntryExamples]
        ["201k-slim-popup" -/TamaSlimPopupExamples]
        ["201l-slim-sheet" -/TamaSlimSheetExamples]
        ["201m-slim-tablem" -/TamaSlimTablemExamples]
        ["201n-slim-tableg" -/TamaSlimTablegExamples]
        ["201o-slim-table" -/TamaSlimTableExamples]
        ["201p-slim-tablep" -/TamaSlimTablepExamples]
        ["201q-slim-tables" -/TamaSlimTablesExamples]
        ["201r-slim-tablex" -/TamaSlimTablexExamples]
        ["201s-slim" -/TamaSlimOverviewExamples])))

(def.js MODULE (!:module))
