(ns std.pipe
  (:require [std.protocol.invoke :as protocol.invoke]
            [std.lib :as h :refer [defimpl definvoke]]
            [std.pipe.util :as ut]
            [std.pipe.process :as process]))

(defmulti pipe-defaults
  "creates default settings for pipe task groups"
  {:added "4.0"}
  identity)

(defmethod pipe-defaults :default
  ([_]
   {:main {:arglists '([] [entry])}}))

(declare task task-info task-status)

(defn- task-string
  ([task]
   (str "#pipe" (task-status task) " " (task-info task))))

(defimpl Task [template name main construct arglists item result summary params]
  :invoke process/invoke
  :string task-string
  :final true)

(defn task-status
  "displays the task-status"
  {:added "3.0"}
  ([^Task task]
   (.template task)))

(defn task-info
  "displays the task-body"
  {:added "3.0"}
  ([^Task task]
   {:fn (symbol (.name task))}))

(defn chain
  "chains multiple tasks together"
  {:added "4.0"}
  ([tasks]
   (let [comp-fn (fn [input params lookup env & args]
                   (reduce (fn [val task]
                             (let [task (if (map? task) task (std.pipe/task :default "step" task))]
                               (apply task val (dissoc params :bulk) lookup env args)))
                           input
                           tasks))
         base-task (first tasks)
         base-props (if (map? base-task) base-task {:name "chain" :template :default})]
     (task :default (:name base-props) (assoc base-props :main {:fn comp-fn})))))

(defn task
  "creates a pipe task"
  {:added "4.0"}
  ([m]
   (if (vector? m)
     (task (chain m))
     (map->Task m)))
  ([template name arg]
   (let [[params main] (if (map? arg)
                         [arg (-> arg :main :fn)]
                         [{} arg])
         defaults     (pipe-defaults template)
         params       (h/merge-nested defaults params)
         count        (or (-> params :main :argcount) 4)
         [_ args?] (ut/main-function main count)]
     (task (h/merge-nested defaults
                           params
                           {:main {:fn main
                                   :argcount count
                                   :args? args?}
                            :name name
                            :template template})))))

(defn pipe
  "creates a pipe from tasks and executes it"
  {:added "4.0"}
  ([tasks input]
   (pipe tasks input {}))
  ([tasks input opts]
   (let [p-task (chain tasks)]
     (p-task input opts))))

(definvoke invoke-intern-pipe
  "creates a pipe task"
  {:added "4.0"}
  [:method {:multi protocol.invoke/-invoke-intern
            :val :pipe}]
  ([_ name config body]
   (let [template (:template config)
         ;; Try to get arglists from defaults if possible, but handle failure safely
         defaults (try (pipe-defaults template) (catch Throwable _ {}))

         arglists (or (:arglists config)
                      (:arglists defaults)
                      '([& args]))

         doc      (:doc config)
         
         body     `(std.pipe/task ~template ~(str name) ~config)

         meta-map (merge (meta name)
                         {:doc doc
                          :arglists (list 'quote arglists)})]
      `(def ~name
         (with-meta
           ~body
           ~meta-map)))))
