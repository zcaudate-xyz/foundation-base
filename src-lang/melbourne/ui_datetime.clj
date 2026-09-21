(ns melbourne.ui-datetime
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]
             [js.react :as r :include [:fn]]
             [melbourne.ui-helper :as ui-helper]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-theme :as base-theme]
             [melbourne.base-font :as base-font]
             [melbourne.ui-picker-basic :as ui-picker-basic]]
   :export [MODULE]})

(defn.js DatePicker
  "creates a picker"
  {:added "0.1"}
  ([props]
   (var [month setMonth] (r/local "JAN"))
   (return
    (r/% ui-picker-basic/PickerBasic
         (Object.assign {} props
                      {:items ["JAN" "FEB" "MAR"]
                       :value month
                       :setMonth setMonth})))))

(defn.js TimePicker
  "creates a picker"
  {:added "0.1"}
  ([props]
   (var [month setMonth] (r/local "JAN"))
   (return
    (r/% ui-picker-basic/PickerBasic
         (Object.assign {} props
                      {:items ["JAN" "FEB" "MAR"]
                       :value month
                       :setMonth setMonth})))))

(def.js MODULE (!:module))
