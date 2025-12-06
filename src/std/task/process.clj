(ns std.task.process
  (:require [std.task.bulk :as bulk]
            [std.lib :as h]
            [std.lib.result :as res]))

(def ^:dynamic *interrupt* false)

(defn main-function
  "creates a main function to be used for execution
   (main-function ns-aliases 1)
   => (contains [h/vargs? false])"
  {:added "4.0"}
  ([func count]
   (let [fcounts (h/arg-count func)
         fcount  (if-not (empty? fcounts)
                   (apply min fcounts)
                   4)
         args?   (> fcount count)
         main    (cond (= count 4) (fn [input params lookup env & args]
                                     (try
                                       (apply func input params lookup env args)
                                       (catch clojure.lang.ArityException e
                                         ;; Fallback for functions detected as 4-arity but actually 3-arity
                                         (if (= (.-actual e) 4)
                                           (apply func input params env args)
                                           (throw e)))))
                       (= count 3) (fn [input params _ env & args]
                                     (apply func input params env args))
                       (= count 2) (fn [input params _ _ & args]
                                     (apply func input params args))
                       (= count 1) (fn [input _ _ _ & args]
                                     (apply func input args))
                       :else (throw (ex-info "`count` is a value between 1 to 4" {:count count})))]
     [main args?])))

(defn select-filter
  "matches given a range of filters"
  {:added "4.0"}
  [selector id]
  (cond (or (fn?  selector)
            (var? selector))
        (h/suppress (selector id))

        (or (string? selector)
            (symbol? selector)
            (keyword? selector))
        (.startsWith (str id) (str selector))

        (h/regexp? selector)
        (boolean (re-find selector (str id)))
        
        (set? selector) (selector id)

        (h/form? selector)  (every? #(select-filter % id)
                                    selector)
        
        
        (vector? selector) (some #(select-filter % id)
                                 selector)

        :else
        (throw (ex-info "Selector not valid" {:selector selector}))))

(defn select-inputs
  "selects inputs based on matches"
  {:added "4.0"}
  ([task lookup env selector]
   (let [list-fn    (or (-> task :item :list)
                        (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
     (cond (= selector :all)
           (list-fn lookup env)
           
           :else
           (->> (list-fn lookup env)
                (filter #(select-filter selector %)))))))

(defn wrap-execute
  "enables execution of task with transformations
   
   ((wrap-execute process-test-fn +task+)
    1 {} {} {})
   => 3"
  {:added "3.0"}
  ([f task]
   (fn [input params lookup env & args]
     (let [pre-fn    (or (-> task :item :pre) identity)
           post-fn   (or (-> task :item :post) identity)
           output-fn (or (-> task :item :output) identity)
           input  (pre-fn input)
           result (apply f input params lookup env args)
           result (post-fn result)]
       (if (:bulk params)
         [input (res/->result input result)]
         (output-fn result))))))

(defn wrap-input
  "enables execution of task with single or multiple inputs
   ((wrap-input process-test-fn +task+)
    1 {} {} {})
   => 2
 
   (let [task (assoc-in +task+ [:item :list] (constantly [1 2 3]))
         f    (wrap-execute process-test-fn task)
         res  ((wrap-input f task) :all {} {} {})]
     (get res 2) => 3
     (get res 3) => 5
     (get res 4) => 7)"
  {:added "3.0"}
  ([f task]
   (fn [input params lookup env & args]
     (cond (= :list input)
           (let [list-fn  (or (-> task :item :list)
                              (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
             (list-fn lookup env))

           (or (keyword? input)
               (vector? input)
               (set? input)
               (h/form? input))
           (let [inputs (select-inputs task lookup env input)]
             (apply bulk/bulk task f inputs params lookup env args))

           :else
           (apply f input params lookup env args)))))

(defn task-inputs
  "constructs inputs to the task given a set of parameters"
  {:added "4.0"}
  ([task]
   (let [input-fn (or (-> task :construct :input) (constantly nil))]
     (task-inputs task (input-fn task) task)))
  ([task input]
   (let [input-fn (or (-> task :construct :input) (constantly nil))
         [input params] (cond (map? input)
                              [(input-fn task) input]

                              :else [input {}])]
     (task-inputs task input params)))
  ([task input params]
   (let [env-fn (or (-> task :construct :env) (constantly {}))]
     (task-inputs task input params (env-fn (merge task params)))))
  ([task input params env]
   (let [lookup-fn (or (-> task :construct :lookup) (constantly {}))]
     (task-inputs task input params (lookup-fn task (merge env params)) env)))
  ([task input params lookup env]
   [input params lookup env]))

(defn invoke
  "executes the task, given functions and parameters"
  {:added "4.0"}
  ([task & args]
   (let [idx (h/index-at #{:args} args)
         _    (if (and (neg? idx) (-> task :main :args?))
                (throw (ex-info "Require `:args` keyword to specify additional arguments"
                                {:input args})))
         [task-args func-args] (if (neg? idx)
                                 [args []]
                                 [(take idx args) (drop (inc idx) args)])
         [input params lookup env] (apply task-inputs task task-args)
         f  (-> (-> task :main :fn)
                (wrap-execute task)
                (wrap-input task))
         params (h/merge-nested (:params task) params)
         result (apply f input params lookup env func-args)]
     (alter-var-root #'*interrupt*
                     (fn [_] false))
     result)))
