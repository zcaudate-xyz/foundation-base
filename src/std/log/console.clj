(ns std.log.console
  (:require [std.protocol.log :as protocol.log]
            [std.protocol.component :as protocol.component]
            [std.log.common :as common]
            [std.log.core :as core]
            [std.log.element :as element]
            [std.log.match :as match]
            [std.log.template :as template]
            [std.string :as str]
            [std.print.ansi :as ansi]
            [std.print.format :as format]
            [std.lib.env :as env]
            [std.lib :as h])
  (:import (java.text SimpleDateFormat)))

(def +style+
  {:header    {:component  {:display {:default true}
                            :position :top}
               :label      {:display true}
               :position   {:text    :white
                            :display {:default true}}
               :date       {:text    :green
                            :display {:default true}}
               :message    {:text    :white
                            :bold true
                            :display {:default true}}}
   :body      {:component  {:display {:default true}}
               :data       {:display {:default true}}
               :exception  {:display {:default true}}
               :console    {:display {:default true}}}
   :meter     {:component  {:display {:default true}
                            :text :white
                            :bold false}
               :trace      {:display {:default false}
                            :bold false
                            :text :blue}
               :form       {:display {:default true}
                            :bold true}}
   :status    {:component  {:display {:default true}}
               :outcome    {:display {:default false}
                            :bold    false}
               :duration   {:display {:default false :return true}
                            :bold    false}
               :start      {:bold    false
                            :display {:default false}}
               :end        {:bold    false
                            :display {:default false}}
               :props      {:bold    false
                            :display {:default false}}}})

(defn style-default
  "gets default style
 
   (style-default {})"
  {:added "3.0"}
  ([m] (style-default m nil))
  ([m path]
   (merge {:background :none :text :white :bold false :highlight false}
          (if path (get-in +style+ path))
          (if path (get-in m path) m))))

(def +levels+
  {:verbose {:background :white   :text :grey  :bold false :highlight true :upper true}
   :debug   {:background :cyan    :text :grey  :bold false :highlight true :upper true}
   :info    {:background :green   :text :grey  :bold false :highlight true :upper true}
   :warn    {:background :yellow  :text :white :bold false :highlight true :upper true}
   :error   {:background :red     :text :white :bold false :highlight true :upper true}
   :fatal   {:background :magenta :text :white :bold false :highlight true :upper true}})

(defn join-with
  "helper function for string concatenation"
  {:added "3.0"}
  ([arr]
   (join-with " " arr))
  ([sep arr]
   (join-with sep identity arr))
  ([sep predicate arr]
   (->> (filter predicate arr)
        (map str)
        (str/join sep))))

(defn console-pprint
  "prints the item in question"
  {:added "3.0"}
  ([data]
   (let [data (if (h/component? data)
                (assoc (h/comp:info data)
                       :class (type data))
                data)
         out (h/local :pprint-str data)]
     out)))

(defn console-format-line
  "format the output line"
  {:added "3.0"}
  ([output]
   (->> output
        (str/split-lines)
        (map (partial str "| "))
        (str/join "\n"))))

(defn console-display?
  "check if item is displayed"
  {:added "3.0"}
  ([style path type]
   (let [dpath     (conj path :display :default)
         default   (or (get-in +style+ dpath) true)
         display   (get-in style dpath default)]
     (if type
       (get-in style (conj path :display type) display)
       display))))

(defn console-render
  "renders the entry based on :console/style"
  {:added "3.0"}
  ([item f path]
   (console-render item f path nil))
  ([item f path base-style]
   (console-render item f path base-style []))
  ([{:log/keys [level type]
     :console/keys [style] :as item} f path base-style checks]
   (when (or (console-display? style path type)
             (some (fn [check] (check)) checks))
     (f item (style-default (merge base-style (get-in style path)))))))

(defn console-header-label
  "constructs the header label"
  {:added "3.0"}
  ([{:log/keys [label level state]}
    {:keys [min-width highlight upper] :as style
     :or {min-width 3}}]
   (let [text  (cond-> (or label level)
                 :then      (h/strn)
                 upper      (str/upper-case))
         [text len]  (element/style-heading text style)
         state  (cond (nil? state) nil

                      (coll? state) (vec state)

                      :else
                      [state])
         state-color (:background style)
         state-color (if (= state-color :grey)
                       (:text style)
                       state-color)]
     (str text
          (apply str (repeat (- min-width len) " "))
          (if state
            (str (ansi/white " (")
                 (ansi/style (join-with ", " (map h/strn state))
                             #{state-color})
                 (ansi/white ")")))))))

(defn console-header-position
  "constructs the header position"
  {:added "3.0"}
  ([{:log/keys [tag function namespace line column] :as m} style]
   (element/elem-position (h/unqualify-keys m) style)))

(defn console-header-date
  "constructs the header date"
  {:added "3.0"}
  ([{:log/keys [timestamp]} style]
   (let [ansi   (element/style-ansi style)
         output (format " :: %s" (java.util.Date. ^long (/ timestamp 1000000)))]
     (ansi/style output ansi))))

(defn console-header-message
  "constructs the header message
 
   (console-header-message {:log/template \"the value is {{log/value}}\"
                            :log/value \"HELLO\"}
                           {:text :blue :background :white})
   => \"[22;47;34mthe value is HELLO[0m\""
  {:added "3.0"}
  ([{:log/keys [message template class] :as item} style]
   (let [ansi      (element/style-ansi style)
         message   (cond message message
                         (or template class) (template/render-message item))
         message-str (if-not (empty? message)
                       (ansi/style message ansi))]
     message-str)))

(defn console-header
  "constructs the log header"
  {:added "3.0"}
  ([item]
   (let [{:log/keys [label function
                     namespace line column level timestamp]} item
         label-str     (console-render item console-header-label [:header :label]
                                       (get +levels+ level))
         position-str  (console-render item console-header-position [:header :position])
         date-str      (console-render item console-header-date [:header :date])
         message-str   (console-render item console-header-message [:header :message])]
     (join-with "\n"
                [(join-with " " [label-str position-str date-str])
                 message-str]))))

(defn console-meter-trace
  "constructs the meter trace"
  {:added "3.0"}
  ([{:trace/keys [id root parent display] :as item}
    {:keys [text bold] :as style}]
   (if (or (and id display)
           common/*trace*)
     (let [format-fn   #(subs % 12)
           label-str   (ansi/style ":" [:red])
           data-str    (cond (= root id)
                             (ansi/style (format-fn id) [:red])

                             :else ;;(= root parent)
                             (str (ansi/style (format-fn parent) [:red])
                                  (ansi/white " -> ")
                                  (ansi/style (format-fn id) [:red])))]
       (str label-str " " data-str)))))

(defn console-meter-form
  "constructs the meter form"
  {:added "3.0"}
  ([{:meter/keys [form] :as item} style]
   (if form
     (let [label-str (ansi/style ":" [:grey :bold])
           data-str  (console-pprint form)]
       (str label-str " " data-str)))))

(defn console-meter
  "constructs the log meter"
  {:added "3.0"}
  ([{:meter/keys [display] :as item}]
   (cond (and (false? display)
              (nil? common/*trace*))
         nil

         :else
         (let [trace-str  (console-render item console-meter-trace [:meter :trace]
                                          {}
                                          [(fn [] common/*trace*)])
               form-str   (console-render item console-meter-form [:meter :form])]
           (join-with "\n" [trace-str form-str])))))

(defn console-status-outcome
  "constructs the status outcome"
  {:added "3.0"}
  ([{:log/keys [label]
     :meter/keys [outcome reflect] :as item} style]
   (if (not= outcome :hidden)
     (let [ansi      (element/style-ansi style)
           label-str (if reflect (ansi/style (str label ":") ansi))
           data-str  (cond (= outcome :success)
                           (ansi/green "SUCCESS")

                           (= outcome :error)
                           (ansi/red "ERROR")

                           :else
                           (ansi/blue (str/upper-case (h/strn outcome))))]
       (join-with " " [label-str data-str])))))

(defn console-status-duration
  "constructs the status duration"
  {:added "3.0"}
  ([{:meter/keys [duration] :as item} style]
   (if duration
     (element/elem-ns-duration "DURATION:" duration style))))

(defn console-status-start
  "constructs the status start time"
  {:added "3.0"}
  ([{:meter/keys [start] :as item} style]
   (if start
     (element/elem-daily-instant "START:"
                                 (quot start 1000000)
                                 style))))

(defn console-status-end
  "constructs the status end time"
  {:added "3.0"}
  ([{:meter/keys [end] :as item} style]
   (if end
     (element/elem-daily-instant "END:"
                                 (quot end 1000000)
                                 style))))

(defn console-status-props
  "constructs the status props"
  {:added "3.0"}
  ([{:meter/keys [props] :as item} style]
   (let [ansi  (element/style-ansi style)
         pairs (mapv (fn [[k v]]
                       (let [label-str (ansi/style (str (.toUpperCase (h/strn k)) ":") ansi)
                             data-str  (if (string? v)
                                         v
                                         (h/local :pprint-str v))]
                         (str label-str " " data-str)))
                     props)]
     (join-with " " pairs))))

(defn console-status
  "constructs the log status"
  {:added "3.0"}
  ([{:log/keys [type level]
     :meter/keys [start duration outcome]
     :console/keys [style] :as item}]
   (let [outcome-str  (if outcome
                        (console-render item console-status-outcome [:status :outcome]
                                        (get-in style [:header :label])))
         duration-str (if duration
                        (console-render item console-status-duration [:status :duration]))
         start-str    (if (and start duration (> duration 10000000000))
                        (console-render item console-status-start [:status :start]))
         end-str      (if (and start duration (> duration 10000000000))
                        (console-render item console-status-end [:status :end]))
         props-str    (console-render item console-status-duration [:status :props])]
     (join-with "  " [outcome-str duration-str start-str end-str]))))

(defn console-body-console-text
  "constructs the return text from the console"
  {:added "3.0"}
  ([text]
   (let [text (->> text
                   (str/split-lines)
                   (map ansi/style:remove)
                   (reverse)
                   (drop-while empty?)
                   (reverse)
                   (drop-while empty?)
                   (str/join "\n"))
         text (-> text
                  (str/replace #"\n\n+" "\n\n"))]
     (str "\n" text "\n"))))

(defn console-body-console
  "constructs the body console"
  {:added "3.0"}
  ([{:log/keys [console] :as item} _]
   (let [console-str (if-not (empty? console)
                       (-> (console-body-console-text console)
                           (console-format-line)))]
     console-str)))

(defn console-body-data-context
  "remove system contexts from the log entry"
  {:added "3.0"}
  ([item]
   (h/filter-keys (fn [k]
                    (not (#{"log" "meter" "fn" "trace" "console"} (namespace k))))
                  item)))

(defn console-body-data
  "constructs the body data"
  {:added "3.0"}
  ([{:fn/keys [console] :log/keys [value] :as item} _]
   (let [context     (console-body-data-context item)
         value        (if console (console value) value)
         data        (cond (and (empty? context) value)
                           value

                           :else
                           (merge context (if (or (map? value) (nil? value)) value {:log/value value})))
         data-str    (if-not (or (nil? data)
                                 (and (map? data) (empty? data)))
                       (-> (console-pprint data)
                           (console-format-line)))]
     data-str)))

(defn console-body-exception
  "constructn the body exception"
  {:added "3.0"}
  ([{:log/keys [exception] :as item} _]
   (if exception
     (-> (console-pprint exception)
         (console-format-line)))))

(defn console-body
  "constructs the log body"
  {:added "3.0"}
  ([item]
   (let [console-str   (console-render item console-body-console [:body :console])
         data-str      (console-render item console-body-data [:body :data])
         exception-str (console-render item console-body-exception [:body :exception])]
     (join-with "\n" [console-str data-str exception-str]))))

(defn console-format
  "formats the entire log"
  {:added "3.0"}
  ([{:log/keys [type] :console/keys [style indent retain] :as item}]
   (let [header-str   (if (console-display? style [:header :component] type)
                        (console-header item))
         meter-str    (if (or (console-display? style [:meter :component] type)
                              common/*trace*)
                        (console-meter item))
         body-str     (if (console-display? style [:body :component] type)
                        (console-body item))
         status-str   (if (console-display? style [:status :component] type)
                        (console-status item))
         header-bottom (-> style :header :component :position (= :bottom))
         results (cond-> [meter-str body-str status-str]
                   (and header-bottom header-str)
                   (conj (str header-str (-> style :header :bottom :suffix)))

                   (and (not header-bottom) header-str)
                   (->> (cons header-str)))]
     (str (-> (join-with "\n" results)
              (format/indent indent))
          "\n"
          (if (or (not retain)
                  header-bottom)
            "\n")))))

(defn logger-process-console
  "processes the label for logger"
  {:added "3.0"}
  ([_ items]
   (let [item-fn (fn [{:log/keys [display raw] :as item}]
                   (try
                     (cond raw raw

                           (false? display)
                           nil

                           :else
                           (console-format item))
                     (catch Throwable t
                       (.printStackTrace t))))
         items (keep item-fn items)]
     (apply h/local :print items))))

(defn console-write
  "write function for the console logger"
  {:added "3.0"}
  ([{:log/keys [display raw] :as item}]
   (let [entry (-> (cond raw raw

                         (false? display)
                         nil

                         :else
                         (console-format item))
                   (h/explode))]
     (if entry (h/local :print entry)))))

(defrecord ConsoleLogger [instance]

  Object
  (toString [logger] (str "#log.console" (core/logger-info logger)))

  protocol.log/ILogger
  (-logger-write [_ entry]
    (if (match/match @instance entry)
      (console-write entry))))

(defmethod print-method ConsoleLogger
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defmethod protocol.log/-create :console
  ([m]
   (-> (core/logger-init m)
       (map->ConsoleLogger))))

(defn console-logger
  "creates a console logger
 
   (console-logger)"
  {:added "3.0"}
  ([] (console-logger nil))
  ([m]
   (-> (assoc m :type :console)
       (protocol.log/-create)
       (protocol.component/-start))))
