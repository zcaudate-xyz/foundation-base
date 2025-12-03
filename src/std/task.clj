(ns std.task
  (:require [std.protocol.invoke :as protocol.invoke]
            [std.task.process :as process]
            [std.lib :as h :refer [defimpl definvoke]]))

(defmulti task-defaults
  "creates default settings for task groups"
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
  "if not `:bulk`, then print function output"
  {:added "3.0"}
  ([params]
   (if (and (not (:bulk params))
            (-> params :print :function nil?))
     (assoc-in params [:print :function] true)
     params)))

(defn task
  "creates a task"
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
  "check if object is a task"
  {:added "3.0"}
  ([x]
   (instance? Task x)))

(definvoke invoke-intern-task
  "creates a form defining a task"
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
  "defines a top level task"
  {:added "3.0"}
  ([name config & body]
   (invoke-intern-task :task name config body)))

(defn process-ns-args
  "processes arguments for tasks"
  {:added "4.0"}
  [args]
  (loop [m     {}
         [k v :as args]  args]
    (if (not k)
      m
      (let [k (try (read-string k)
                   (catch Throwable t))
            v (try (read-string v)
                   (catch Throwable t))]
        (cond (not (keyword? k))
              (recur m (rest args))


              (or (keyword? v)
                  (= 1 (count args)))
              (recur (assoc m k true)
                     (rest args))
              
              :else
              (recur (case k
                       :only    (assoc m :ns v)
                       :with    (assoc m :ns [v])
                       (assoc m k v))
                     (rest (rest args))))))))
