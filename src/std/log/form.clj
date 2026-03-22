(ns std.log.form
  (:require [clojure.string]
            [std.concurrent.thread :as thread]
            [std.lib.foundation :as f]
            [std.log.common :as common]
            [std.log.core :as core]
            [std.protocol.log :as protocol.log]))

(defn log-meta
  "gets the metadata information on macroexpand"
  {:added "3.0"}
  ([form]
   (let [{:keys [line column]} (meta form)
         ns    (str (.getName *ns*))]
     (cond-> {:log/namespace ns
              :log/line      line
              :log/column    column}))))

(defn log-function-name
  "gets the function name
 
   (log-function-name \"ns$function_name\")
   => \"function-name\""
  {:added "3.0"}
  ([name]
   (-> (clojure.string/split name #"\$")
       (second)
       (f/demunge))))

(defn log-runtime-raw
  "returns the runtime information of the log entry
 
   (set (keys (log-runtime-raw nil \"std.log.form-test\")))
   => #{:log/function :log/method :log/filename}"
  {:added "3.0"}
  ([id ns]
   (let [e        (RuntimeException. "")
         frames   (.getStackTrace e)]
     (->> frames
          (keep (fn [^StackTraceElement frame]
                  (let [name (.getClassName frame)]
                    (if (and (not (.startsWith name "std.log.form$log_runtime"))
                             (.startsWith name (munge ns))
                             (.contains name "$"))
                      {:log/function (log-function-name name)
                       :log/method   (.getMethodName frame)
                       :log/filename (.getFileName frame)}))))
          first))))

(def log-runtime (memoize log-runtime-raw))

(defn log-check
  "checks to see if form should be ignored
 
   (log-check :debug)
   => true"
  {:added "3.0"}
  ([level]
   (if (or (not common/*static*)
           (>= (get common/+levels+ level)
               (get common/+levels+ common/*level*)))
     true false)))

(defn to-context
  "converts a string to a log context
 
   (to-context \"hello\")
   => #:log{:message \"hello\"}"
  {:added "3.0"}
  ([context]
   (cond (string? context)
         {:log/message context}

         (map? context)
         context

         :else (throw (ex-info "Invalid Context" {:context context})))))

(defn log-fn
  "actual log function for function application"
  {:added "3.0"}
  ([logger level data exception context]
   (let [context (merge common/*context* (to-context context))
         {:log/keys [label pipe return]} context
         label   (or label (.toUpperCase (name level)))
         context (cond-> context
                   :then (assoc :log/label label)
                   (not (false? return)) (assoc :log/value  data))
         entry  (core/logger-message level context exception)
         output (protocol.log/-logger-write logger entry)]
     (if-not (false? pipe)
       data
       output))))

(defn log-form
  "function for not including log forms on static compilation"
  {:added "3.0"}
  ([level id form logger context data exception preset]
   (if (log-check level)
     (let [id   (or id (f/flake))
           meta (log-meta form)]
       `(binding [common/*context* (merge common/*context*
                                          {:log/pipe false}
                                          ~meta
                                          (log-runtime ~id ~(:log/namespace meta))
                                          ~preset)]
          (log-fn ~logger ~level ~data ~exception ~context))))))

(defmacro with-context
  "applies a context to the current one"
  {:added "3.0"}
  ([context & body]
   `(binding [common/*context* (merge common/*context* ~context)]
      ~@body)))

(defn log-context
  "creates a log-context form
 
   (log-context \"hello\")
   => seq?"
  {:added "3.0"}
  ([body]
   `(let [~'context (log-meta ~'&form)]
      (list (quote ~'std.log.form/with-context)
            (list (quote ~'merge)
                  ~'context
                  (list (quote ~'std.log.form/log-runtime)
                        (list (quote ~'container.registry.h/flake))
                        (list :log/namespace ~'context)))
            ~body))))

(defmacro log
  "produces a log"
  {:added "3.0"}
  ([data]
   (log-form :info nil &form `(common/default-logger) {} data nil nil))
  ([context data]
   (log-form :info nil &form `(common/default-logger) context data nil nil))
  ([level context data]
   (log-form level nil &form `(common/default-logger) context data nil nil))
  ([logger level context data]
   (log-form level nil &form logger context data nil nil))
  ([logger level context data exception]
   (log-form level nil &form logger context data nil nil)))

(defn log-data-form
  "creates a standard form given a type"
  {:added "3.0"}
  ([level]
   (list 'defmacro (symbol (name level))
         (list '[data]
               (list 'log-form level nil '&form
                     (list 'quote '(std.log.common/default-logger))
                     {} 'data nil nil))
         (list '[context data]
               (list 'log-form level nil '&form
                     (list 'quote '(std.log.common/default-logger))
                     'context 'data nil nil))
         (list '[logger context data]
               (list 'log-form level nil '&form 'logger 'context 'data nil nil)))))

(defmacro deflog-data
  "creates a standard form given a type"
  {:added "3.0"}
  ([levels]
   (mapv log-data-form (if (list? levels)
                         (eval levels)
                         levels))))

(defn log-error-form
  "creates forms for  `warn`, `fatal`"
  {:added "3.0"}
  ([level]
   (list 'defmacro (symbol (name level))
         (list '[exception]
               (list 'log-form level nil '&form
                     (list 'quote '(std.log.common/default-logger))
                     {}  nil 'exception nil))
         (list '[context exception]
               (list 'log-form level nil '&form
                     (list 'quote '(std.log.common/default-logger))
                     'context  nil 'exception nil))
         (list '[logger context exception]
               (list 'log-form level nil '&form 'logger 'context  nil 'exception nil)))))

(defmacro deflog-error
  "creates a standard form given a type"
  {:added "3.0"}
  ([levels]
   (mapv log-error-form (if (list? levels)
                          (eval levels)
                          levels))))

(deflog-data  [:verbose :debug :info :warn])
(deflog-error [:error :fatal])

