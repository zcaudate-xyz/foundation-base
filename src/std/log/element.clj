(ns std.log.element
  (:require [std.lib.foundation]
            [std.print.ansi :as ansi]
            [std.print.format.time :as time]))

(defn style-ansi
  "constructs the style used in console
 
   (style-ansi {:text :blue :background :white})
   => [:normal :on-white :blue]"
  {:added "3.0"}
  ([{:keys [text background bold] :as style
     :or {background :none}}]
   (concat (if bold [:bold] [:normal])
           (if (= background :none)
             []
             [(keyword (str "on-" (name background)))])
           [(or text :white)])))

(defn style-heading
  "returns the text for a style
 
   (style-heading \"Hello\" {:highlight true
                           :text :blue
                           :background :white})
   => [\"[22;47;34m Hello [0m\" 7]"
  {:added "3.0"}
  ([text {:keys [highlight] :as style}]
   (let [text  (if highlight
                 (str " " text " ")
                 (str text " "))
         len    (count text)
         ansi   (style-ansi style)]
     [(ansi/style text ansi) len])))

(defn elem-daily-instant
  "returns a styled label and instant 
 
   (elem-daily-instant \"TIME:\" (h/time-ms) {})
   => string?"
  {:added "3.0"}
  ([label ms style]
   (let [ansi      (style-ansi style)
         elem-str  (ansi/style label ansi)
         data-str  (time/t:time ms)]
     (str elem-str " " data-str))))

(defn elem-ns-duration
  "returns a styled label and duration
 
   (elem-ns-duration \"DURATION:\" 100000 {})
   => string?"
  {:added "3.0"}
  ([label ns style]
   (let [ansi      (style-ansi style)
         elem-str  (ansi/style label ansi)
         data-str  (time/t ns)]
     (str elem-str " " data-str))))

(defn elem-position
  "creates a style position element
 
   (elem-position {:tag :hello
                   :namespace 'user
                   :function 'add
                   :column 10
                   :line 11}
                  {:text :white})
   => string?"
  {:added "3.0"}
  ([{:keys [tag function namespace line column]} style]
   (let [ansi      (style-ansi style)
         fn-label  (if function
                     (str namespace "/" function)
                     namespace)
         pos-label (format "%s%s @ %d,%d"
                           (if tag
                             (str (std.lib.foundation/strn tag) " :: ")
                             "")
                           fn-label line column)]
     (ansi/white "[" (ansi/style pos-label ansi) "]"))))
