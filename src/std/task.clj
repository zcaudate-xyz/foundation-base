(ns std.task
  (:require [std.protocol.invoke :as protocol.invoke]
            [std.task.process :as process]
            [std.lib :as h :refer [defimpl definvoke]]))

(defmulti task-defaults
  "creates default settings for task groups
 
   (task-defaults :namespace)"
  {:added "3.0"}
  identity)

(defmethod task-defaults :default
  ([_]
   {:main {:arglists '([] [entry])}}))

(declare task-info task-status)

(defn- task-string
  ([task]
   (str "#task" (task-status task) " " (task-info task))))

(defimpl Task [type name main construct arglists item result summary]
  :invoke process/invoke
  :string task-string
  :final true)

(defn task-status
  "displays the task-status"
  {:added "3.0"}
  ([^Task task]
   (.type task)))

(defn task-info
  "displays the task-body"
  {:added "3.0"}
  ([^Task task]
   {:fn (symbol (.name task))}))

(defn single-function-print
  "if not `:bulk`, then print function output
 
   (single-function-print {})
   => {:print {:function true}}"
  {:added "3.0"}
  ([params]
   (if (and (not (:bulk params))
            (-> params :print :function nil?))
     (assoc-in params [:print :function] true)
     params)))

(defn task
  "creates a task
 
   (task :namespace \"list-interns\" ns-interns)
 
   (task :namespace
         \"list-interns\"
         {:main {:fn clojure.core/ns-interns}})"
  {:added "3.0"}
  ([m]
   (map->Task m))
  ([type name arg]
   (let [[params main] (if (map? arg)
                         [arg (-> arg :main :fn)]
                         [{} arg])
         defaults     (task-defaults type)
         params       (h/merge-nested defaults params)
         count        (or (-> params :main :argcount) 4)
         [main args?] (process/main-function main count)]
     (task (h/merge-nested defaults
                           params
                           {:main {:fn main
                                   :args? args?}
                            :name name
                            :type type})))))

(defn task?
  "check if object is a task
 
   (-> (task :namespace \"list-interns\" ns-interns)
       (task?))
   => true"
  {:added "3.0"}
  ([x]
   (instance? Task x)))

(definvoke invoke-intern-task
  "creates a form defining a task
 
   (invoke-intern-task '-task- '{:template :namespace
                                 :main {:fn clojure.core/ns-aliases}})"
  {:added "3.0"}
  [:method {:multi protocol.invoke/-invoke-intern
            :val :task}]
  ([name config]
   (invoke-intern-task :task name config nil))
  ([_ name config _]
   (let [template (:template config)
         body `(task ~template ~(str name) ~config)
         arglists (or (:arglists config)
                      (-> (task-defaults template) :arglists))
         name (with-meta name
                (merge (meta name)
                       (cond-> config
                         arglists (assoc :arglists (list 'quote arglists)))))]
     (list 'def name body))))

(defmacro deftask
  "defines a top level task
 
   (deftask -list-aliases-
     {:template :namespace
     :main clojure.core/ns-aliases
      :item {:post (comp vec sort keys)}
      :doc  \"returns all aliases\"})"
  {:added "3.0"}
  ([name config & body]
   (invoke-intern-task :task name config body)))

(defn process-ns-args
  [args]
  (loop [m     {}
         [k v]  args]
    (if (not k)
      m
      (let [k (try (read-string k)
                   (catch Throwable t))
            v (try (read-string v)
                   (catch Throwable t))]
        (cond (not (keyword? k))
              (recur m (rest args))


              (keyword? v)
              (recur (assoc m k true)
                     (rest args))
              
              :else
              (recur (case k
                       :only    (assoc m :ns v)
                       :with    (assoc m :ns [v])
                       (assoc m k v))
                     (rest (rest args))))))))
