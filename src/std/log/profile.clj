(ns std.log.profile
  (:require [std.lib.collection]
            [std.lib.foundation]
            [std.lib.time]
            [std.log.common :as common]
            [std.log.core :as core]
            [std.log.form :as form]
            [std.print.format.common :as format.common]
            [std.protocol.log :as protocol.log]))

(defn- wrap-indent
  ([thunk]
   (fn []
     (let [indent (+ format.common/*indent* format.common/*indent-step*)]
       (binding [format.common/*indent*  indent
                 common/*context* (-> common/*context*
                                      (assoc :console/indent indent))]
         (thunk))))))

(defn spy-fn
  "constructs a spy function with print on start and end"
  {:added "3.0"}
  ([logger thunk level context]
   (spy-fn logger thunk level context {}))
  ([logger thunk level context {:keys [start-fn wrap-fn write-fn end-fn error-fn] :as hooks}]
   (let [context (std.lib.collection/merge-nested common/*context* context)
         {:log/keys [indent trace label pipe return throw]} context
         start-time  (std.lib.time/time-ns)
         start-context {:log/type :start
                        :log/label (or label "SPY")
                        :log/timestamp start-time
                        :meter/start start-time}

         context-fn (fn [end-time]
                      {:log/type :end
                       :log/timestamp end-time
                       :log/label (or label "SPY")
                       :meter/start start-time
                       :meter/end end-time
                       :meter/outcome :success
                       :meter/duration (- end-time start-time)})]
     (try
       (let [_        (if start-fn (start-fn start-context))
             thunk    (cond-> thunk
                        indent (wrap-indent)
                        wrap-fn (wrap-fn))
             data     (thunk)
             end-time (std.lib.time/time-ns)
             end-context (context-fn end-time)
             _        (if end-fn (end-fn end-context data))
             entry    (core/logger-message level (merge context
                                                        end-context
                                                        {:log/value (if-not (false? return) data)})
                                           nil)
             write-fn (or write-fn protocol.log/-logger-write)
             output   (write-fn logger entry)]
         (if-not (false? pipe)
           data
           output))
       (catch Throwable t
         (let [end-time (std.lib.time/time-ns)
               error-context (context-fn end-time)
               _        (if error-fn (error-fn error-context t))
               entry    (core/logger-message level (merge context error-context) t)]
           (protocol.log/-logger-write logger entry)
           (if-not (false? throw)
             (throw t))))))))

(defn bind-trace
  "binds a trace id to the log"
  {:added "3.0"}
  ([trace thunk]
   (if (or trace common/*trace*)
     (let [trace-id (std.lib.foundation/flake)
           {:trace/keys [id root]} common/*context*]
       (binding [common/*context* (merge common/*context*
                                         {:trace/id trace-id
                                          :trace/root (or root trace-id)
                                          :trace/parent id})]
         (thunk)))
     (thunk))))

(defn spy-form
  "helper function for the `spy` macro"
  {:added "3.0"}
  ([level id form body logger context preset]
   (cond (not (form/log-check level))
         form

         :else
         (let [id        (std.lib.foundation/flake)
               meta      (form/log-meta form)
               thunk     `(fn [] ~body)]
           `(binding [common/*context* (merge common/*context*
                                              ~meta
                                              (form/log-runtime ~id ~(:log/namespace meta))
                                              {:console/indent format.common/*indent*})]
              (let [~'context  (std.lib.collection/merge-nested ~preset
                                               {:meter/form (quote ~body)}
                                               (form/to-context ~context))]
                (bind-trace (:log/trace ~'context)
                            (fn []
                              (spy-fn ~logger ~thunk ~level ~'context)))))))))

(defmacro defspy
  "creates a spy macro"
  {:added "3.0"}
  ([name level preset]
   `(defmacro ~name
      {:style/indent 1}
      ([~'body]
       (spy-form ~level nil ~'&form ~'body `~'(common/default-logger) {} ~preset))
      ([~'context ~'body]
       (spy-form ~level nil ~'&form ~'body `~'(common/default-logger) ~'context ~preset))
      ([~'logger ~'context ~'body]
       (spy-form ~level nil ~'&form ~'body ~'logger ~'context ~preset)))))

(defn on-grey
  "produces an on-grey style for console label"
  {:added "3.0"}
  ([color]
   {:text color
    :highlight false
    :bold true
    :background :grey}))

(defn on-white
  "produces an on-white style for console label"
  {:added "3.0"}
  ([color]
   {:text color
    :highlight true
    :bold false
    :background :white}))

(defn on-color
  "produces an on-color style for console label"
  {:added "3.0"}
  ([color]
   {:text :grey
    :highlight true
    :bold false
    :background color}))

(defn item-style
  "styles the actual log entry"
  {:added "3.0"}
  ([{:keys [label hide show retain] :as m}]
   (let [style  (dissoc m :label :hide :show :retain)
         style  (cond-> style
                  label (assoc-in [:header :label] label))
         style  (reduce (fn [style k]
                          (cond (keyword? k)
                                (assoc-in style [k :component :display :default] false)

                                (vector? k)
                                (assoc-in style (conj k :display :default) false)))
                        style
                        hide)
         style  (reduce (fn [style k]
                          (cond (keyword? k)
                                (assoc-in style [k :component :display :default] true)

                                (vector? k)
                                (assoc-in style (conj k :display :default) true)))
                        style
                        show)]
     (cond-> {:console/style style}
       retain (assoc :console/retain retain)))))

(defspy note   :debug  (merge {:log/label "NOTE" :log/throw true  :log/trace false :log/return true}
                              (item-style {:label (on-grey :yellow)
                                           :hide  [:meter :status [:header :date]]})))

(defspy spy    :debug  (merge {:log/label "SPY"    :log/throw true  :log/trace false :log/return true
                               :trace/display true}
                              (item-style {:label (on-grey :red)
                                           :hide  [:status [:header :date]]})))

(defspy track  :debug  (merge {:log/label "TRACK"  :log/throw true  :log/trace true  :log/return false
                               :trace/display true}
                              (item-style {:label (on-grey :red)
                                           :hide  [:status]})))

(defspy status :info   (merge {:log/label "STATUS" :log/throw true  :log/trace false :log/return false
                               :trace/display true}
                              (item-style {:label (on-grey :white)
                                           :show  [:meter]})
                              {:meter/reflect false}))

(defspy show   :info   (merge {:log/label "SHOW"   :log/throw true  :log/trace false :log/return true}
                              (item-style {:label (on-white :grey)
                                           :hide  [:meter :status [:header :date]]})))

(defspy todo   :info   (merge {:log/label "TODO"   :log/throw false  :log/trace false :log/return true}
                              (item-style {:label (on-grey :magenta)
                                           :hide [:status]})))

(defspy action :info    (merge {:log/label "ACTION" :log/throw true  :log/trace false :log/return false}
                               (item-style {:retain true
                                            :label (on-grey :white)
                                            :hide  [[:meter :form]
                                                    [:header :position]
                                                    [:header :date]]})))

(defn meter-fn
  "constructs a meter function"
  {:added "3.0"}
  ([logger thunk level {:console/keys [style] :as context}]
   (let [style     (std.lib.collection/merge-nested (:console/style common/*context*) style)
         ncontext  (merge common/*context* context)
         {:log/keys [label]
          :or {label "METER"}} ncontext
         start-fn (fn [ocontext]
                    (form/log-fn logger level nil nil
                                 (merge {}
                                        ocontext context
                                        {:log/label label
                                         :log/indent true
                                         :console/style (std.lib.collection/merge-nested-new
                                                         style
                                                         {:status {:component {:display {:default false}}}
                                                          :meter  {:form      {:display {:default true}}
                                                                   :trace     {:display {:default false}}}})})))
         write-fn  (fn [logger entry]
                     (->> (-> entry
                              (assoc :log/label label)
                              (merge context)
                              (update :console/style std.lib.collection/merge-nested-new
                                      {:header  {:component {:display {:default false}
                                                             :position :bottom}
                                                 :position  {:display {:default false}}
                                                 :date      {:display {:default false}}}
                                       :body    {:message   {:display {:default false}}}
                                       :meter   {:form      {:display {:default false}}}
                                       :status  {:outcome   {:display {:default true}}
                                                 :duration  {:display {:default true}}}
                                       :form    {:component {:display {:default false}}}}))
                          (protocol.log/-logger-write logger)))]
     (spy-fn logger thunk level ncontext {:start-fn start-fn
                                          :write-fn write-fn
                                          :wrap-fn wrap-indent}))))

(defn meter-form
  "helper to the `meter` macro"
  {:added "3.0"}
  ([level id form body logger context preset]
   (cond (not (form/log-check level))
         form

         :else
         (let [id        (std.lib.foundation/flake)
               meta      (form/log-meta form)
               thunk     `(fn [] ~body)]
           `(binding [common/*context* (merge common/*context*
                                              ~meta
                                              (form/log-runtime ~id ~(:log/namespace meta))
                                              {:meter/form (quote ~body)})]
              (let [~'context (std.lib.collection/merge-nested ~preset
                                              (form/to-context ~context))]
                (bind-trace (:log/trace ~'context)
                            (fn []
                              (meter-fn ~logger ~thunk ~level ~'context)))))))))

(defmacro defmeter
  "creates a meter macro"
  {:added "3.0"}
  ([name level preset]
   `(defmacro ~name
      {:style/indent 1}
      ([~'body]
       (meter-form ~level nil ~'&form ~'body `~'(common/default-logger) {} ~preset))
      ([~'context ~'body]
       (meter-form ~level nil ~'&form ~'body `~'(common/default-logger) ~'context ~preset))
      ([~'logger ~'context ~'body]
       (meter-form ~level nil ~'&form ~'body ~'logger ~'context ~preset)))))

(defmeter silent  :verbose  (merge {:log/label "SIILENT" :log/throw true  :log/trace false :log/return true}
                                   (item-style {:retain true
                                                :label (on-grey :white)
                                                :hide  [[:header :date]]})))

(defmeter trace   :debug    (merge {:log/label "TRACE" :log/throw true  :log/trace true  :log/return false
                                    :trace/display true}
                                   (item-style {:label (on-grey :red)
                                                :show  [[:meter :trace]]
                                                :hide  [:status]
                                                :header {:component {:display {:end true}}
                                                         :bottom    {:suffix "END"}}})))

(defmeter profile :debug     (merge {:log/label "PROFILE" :log/throw true  :log/trace false :log/return false
                                     :meter/reflect true}
                                    (item-style {:label (on-grey :red)
                                                 :hide  [:meter [:header :date]]})))

(defmeter meter   :info     (merge {:log/label "METER" :log/throw true  :log/trace false :log/return false
                                    :trace/display true}
                                   (item-style {:retain true
                                                :label (on-grey :white)
                                                :hide  [[:header :date]]})))

(defmeter block   :info     (merge {:log/label "BLOCK" :log/throw true  :log/trace false :log/return false}
                                   (item-style {:label (on-grey :green)
                                                :hide  [[:meter :form]]})))

(defmeter section :info     (merge {:log/label "SECTION" :log/throw true  :log/trace false :log/return false
                                    :meter/reflect true}
                                   (item-style {:label (on-grey :green)
                                                :hide  [[:meter :form]]})))

(defmeter task    :info     (merge {:log/label "TASK" :log/throw true  :log/trace false :log/return false}
                                   (item-style {:retain true
                                                :label (on-grey :white)
                                                :hide  [[:meter :form]
                                                        [:header :position]
                                                        [:header :date]]})))

