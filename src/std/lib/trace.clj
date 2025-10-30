(ns std.lib.trace
  (:require [std.lib.env :as env]
            [std.lib.time :as time]
            [std.lib.foundation :as h]
            [std.lib.context.pointer :as pointer]
            [std.string.common :as str]
            [std.string.prose :as prose]))

(deftype Trace [type full history original]
  java.lang.Object
  (toString [_]
    (str "#trace " {:type type :history @history}))
  
  clojure.lang.IDeref
  (deref [_] {:type type :history @history}))

(defmethod print-method Trace
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn trace?
  "checks if object is a trace"
  {:added "4.0"}
  [obj]
  (instance? Trace obj))

(defn get-trace
  "gets a trace from var"
  {:added "4.0"}
  [var]
  (:code/trace (meta var)))

(defn ^Trace make-trace
  "makes a trace from var
 
   (make-trace #'get-trace :basic)
   => trace?"
  {:added "4.0"}
  [^clojure.lang.Var var type & [f]]
  (Trace. type
          (h/var-sym var)
          (atom [])
          (or f @var)))

(defn has-trace?
  "checks if var has trace"
  {:added "4.0"}
  [^clojure.lang.Var var]
  (boolean (:code/trace (meta var))))

(defn apply-trace
  "applies a trace with arguments
   
   (apply-trace identity
                (make-trace #'get-trace :basic)
                identity
                [1])
   => 1"
  {:added "4.0"}
  [f trace transform args]
  (let [in  args
        out (apply f args)]
    (swap! (.-history ^Trace trace) conj (transform {:in in :out out}))
    out))

(defn wrap-basic
  "wraps an identity transform"
  {:added "4.0"}
  [f trace]
  (fn [& args]
    (apply-trace f trace identity args)))

(defn wrap-print
  "wraps a print transform"
  {:added "4.0"}
  [f ^Trace trace]
  (fn [& args]
    (apply-trace f trace (fn [m]
                           (env/local :println [(str (java.util.Date.))])
                           (env/local :println
                                      (env/pp-str (apply list (.-full trace) (:in m))))
                           (env/local :println
                                      '=> (std.string.prose/indent-rest (pr-str (:out m))
                                                                  4))
                           m)
                 args)))

(defn wrap-stack
  "wraps a stack transform"
  {:added "4.0"}
  [f ^Trace trace]
  (fn [& args]
    (apply-trace f trace (fn [m]
                           (env/local :println
                                      (pr-str (h/trace (.-full trace))))
                           m)
                 args)))

(defn add-raw-trace
  "parent function for adding traces"
  {:added "4.0"}
  [var type]
  (let [attach-fn (fn [f trace]
                    (alter-meta! var assoc :code/trace trace)
                    (alter-var-root var (fn [_]
                                          (case type
                                            :basic (wrap-basic f trace)
                                            :print (wrap-print f trace)
                                            :stack (wrap-stack f trace)))))]
    
    (if-let [^Trace trace (:code/trace (meta var))]
      (cond  (= type (.-type ^Trace trace))
             nil

             :else
             (let [f (.-original trace)
                   new-trace (make-trace var type f)]
               (attach-fn f new-trace)
               true))
      (cond  (or (fn? @var)
                 (pointer/pointer? @var))
             (let [trace (make-trace var type)
                   f (.-original trace)]
               (attach-fn f trace)
               true)
             
             :else false))))

(defn add-base-trace
  "adds a base trace"
  {:added "4.0"}
  [var]
  (add-raw-trace var :basic))

(defn add-print-trace
  "adds a print trace"
  {:added "4.0"}
  [var]
  (add-raw-trace var :print))

(defn add-stack-trace
  "adds a stack trace"
  {:added "4.0"}
  [var]
  (add-raw-trace var :stack))

(defn remove-trace
  "removes a trace from var"
  {:added "4.0"}
  [var]
  (if-let [trace (:code/trace (meta var))]
    (do (alter-meta! var dissoc :code/trace)
        (alter-var-root var (fn [_] (.-original ^Trace trace)))
        true)
    false))

(defn trace-ns
  "adds a trace to entire namespace"
  {:added "4.0"}
  ([]
   (trace-ns *ns*))
  ([ns]
   (mapv add-base-trace (vals (ns-interns ns)))))

(defn trace-print-ns
  "adds a print trace to entire namespace"
  {:added "4.0"}
  ([]
   (trace-print-ns *ns*))
  ([ns]
   (mapv add-print-trace (vals (ns-interns ns)))))

(defn untrace-ns
  "removes traces for entire namespace"
  {:added "4.0"}
  ([]
   (untrace-ns *ns*))
  ([ns]
   (mapv remove-trace (vals (ns-interns ns)))))

(defn output-trace
  "outputs a trace form"
  {:added "4.0"}
  [var]
  (let [trace (or (:code/trace (meta var))
                  (h/error "No trace installed"))
        {:keys [in out]}  (or (first @(.-history ^Trace trace))
                              (h/error "No trace history"))]
    
    [(apply list (h/var-sym var) in)
     '=> out]))
