(ns js.lib.datetime
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [print send]))

(l/script :js
  {:require [[js.core :as j]]
   :import  [["javascript-time-ago" :as TimeAgo]
             ["javascript-time-ago/locale/en" :as TimeAgoEn]
             ["dateformat" :as DateFormat]]})

(def.js __init__
  ((fn [] (TimeAgo.addDefaultLocale TimeAgoEn))))

(def.js timeAgo (new TimeAgo "en-US"))

(defn.js ago
  "formats the time ago"
  {:added "4.0"}
  [us]
  (if us
    (return (. -/timeAgo (format (new Date (Math.floor (/ us 1000)))
                                 "mini")))
    (return " - ")))

(defn.js agoVerbose
  "formats the time ago in verbose"
  {:added "4.0"}
  [us]
  (if us
    (return (. -/timeAgo (format (new Date (Math.floor (/ us 1000)))
                                 "round-minute")))
    (return " - ")))

(defn.js formatDate
  "formats the date"
  {:added "4.0"}
  [date expr]
  (return (DateFormat date expr)))

(comment
  (./create-tests))
