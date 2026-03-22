(ns std.pretty.color
  (:require [std.print.ansi :as ansi]))

;; ## Coloring Multimethods

(defmulti -document
  "Constructs a pretty print document, which may be colored if `:print-color` is true.
 
   (-document printer/+defaults+ :string \"hello there\")
   => [:span [:pass \"[36m\"] \"hello there\" [:pass \"[0m\"]]
 
   (-document printer/+defaults+ :keyword :something)
   => [:span [:pass \"[34m\"] :something [:pass \"[0m\"]]"
  {:added "3.0"}
  (fn [options element text]
    (when (:print-color options)
      (:color-markup options))))

(defmulti -text
  "Produces text colored according to the active color scheme. This is mostly
   useful to clients which want to produce output which matches data printed by
   Puget, but which is not directly printed by the library. Note that this
   function still obeys the `:print-color` option.
 
   (-text printer/+defaults+ :string \"hello there\")
   => \"[36mhello there[0m\"
 
   (-text printer/+defaults+ :keyword :hello)
   => \"[34m:hello[0m\""
  {:added "3.0"}
  (fn [options element text]
    (when (:print-color options)
      (:color-markup options))))


;; ## Default Markup

;; The default transformation when there's no markup specified is to return the
;; text unaltered.


(defmethod -document nil
  ([options element text]
   text))

(defmethod -text nil
  ([options element text]
   text))

(defmethod -document :ansi
  ([options element text]
   (if-let [codes (-> options :color-scheme (get element) seq)]
     [:span [:pass (apply ansi/encode codes)] text [:pass (ansi/encode :reset)]]
     text)))

(defmethod -text :ansi
  ([options element text]
   (if-let [codes (-> options :color-scheme (get element) seq)]
     (str (apply ansi/encode codes) text (ansi/encode :reset))
     text)))
