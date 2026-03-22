(ns std.print.format.time
  (:require [std.lib.time :as time]
            [std.print.ansi :as ansi])
  (:import (java.text SimpleDateFormat)))

(defn t:time
  "only returns the time (not day) of an instant
 
   (t:time (System/currentTimeMillis))"
  {:added "3.0"}
  ([^long ms]
   (.format (SimpleDateFormat. "HH:mm:ss.SSS")
            (java.util.Date. ms))))

(defn t:ms
  "converts an ms time into readable
 
   (t:ms 1000)"
  {:added "3.0"}
  ([^long ms]
   (let [d   (java.util.Date. ms)
         df  (cond (< ms 1000)       "S'ms'"
                   (< ms (* 60 1000))       "s.S's'"
                   (< ms (* 60 60 1000))    "mm:ss'm'"
                   (< ms (* 24 60 60 1000)) "HH:mm:ss'h'"
                   :else "D'd' HH:mm:ss'h'")]
     (.format (SimpleDateFormat. ^String df) d))))

(defn t:ns
  "creates humanised time for nanoseconds
 
   (t:ns 10000 3)"
  {:added "3.0"}
  ([ns]
   (t:ns ns 4))
  ([ns digits]
   (let [full   (str ns)
         len    (count full)]
     (if (and (< len 12))
       (time/format-ns ns digits)
       (time/format-ms (quot ns 1000000))))))

(defn t:text
  "formats ns to string
 
   (t:text 1)"
  {:added "3.0"}
  ([ns]
   (if (zero? (quot ns 100000000))
     (t:ns ns)
     (t:ms (quot ns 1000000)))))

(defn t:style
  "sets the color for a time
 
   (t:style 10000)"
  {:added "3.0"}
  ([ns]
   (let [full   (str ns)
         len    (count full)]
     (cond (<= 1 len 6)  [:normal :cyan]
           (<= 7 len 8)  [:normal :blue]
           (<= 9 len 10) [:bold :yellow]
           :else [:bold :red]))))

(defn t
  "formats ns to string
 
   (t 1)"
  {:added "3.0"}
  ([ns]
   (ansi/style (t:text ns) (t:style ns))))
