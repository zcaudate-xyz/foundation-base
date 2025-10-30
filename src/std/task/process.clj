(ns std.task.process
  (:require [std.task.bulk :as bulk]
            [std.lib :as h]
            [std.lib.result :as res]))

(def ^:dynamic *interrupt* false)

(defn main-function
  "creates a main function to be used for execution
 
   (main-function ns-aliases 1)
   => (contains [h/vargs? false])
 
   (main-function ns-unmap 1)
   => (contains [h/vargs? true])"
  {:added "3.0"}
  ([func count]
   (let [fcounts (h/arg-count func)
         fcount  (if-not (empty? fcounts)
                   (apply min fcounts)
                   4)
         args?   (> fcount count)
         main    (cond (= count 4) (fn [input params lookup env & args]
                                     (apply func input params lookup env args))
                       (= count 3) (fn [input params _ env & args]
                                     (apply func input params env args))
                       (= count 2) (fn [input params _ _ & args]
                                     (apply func input params args))
                       (= count 1) (fn [input _ _ _ & args]
                                     (apply func input args))
                       :else (throw (ex-info "`count` is a value between 1 to 4" {:count count})))]
     [main args?])))

(defn select-filter
  "matches given a range of filters
 
   (select-filter #\"ello\" 'hello)
   => true
 
   (select-filter #\"^ello\" 'hello)
   => false
 
   (select-filter 'code 'code.test) => true
 
   (select-filter 'hara 'spirit.common) => false"
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
  "selects inputs based on matches
 
   (select-inputs {:item {:list (fn [_ _] ['code.test 'spirit.common])}}
                  {}
                  {}
                  ['code])
   => ['code.test]"
  {:added "3.0"}
  ([task lookup env selector]
   (let [list-fn    (or (-> task :item :list)
                        (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
     (cond (= selector :all)
           (list-fn lookup env)
           
           :else
           (->> (list-fn lookup env)
                (filter #(select-filter selector %)))))))

(defn wrap-execute
  "enables execution of task with transformations"
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
  "enables execution of task with single or multiple inputs"
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
  "constructs inputs to the task given a set of parameters
 
   (task-inputs (task/task :namespace \"ns-interns\" ns-interns)
                'std.task)
   => '[std.task {} {} {}]
 
   (task-inputs (task/task :namespace \"ns-interns\" ns-interns)
                {:bulk true})
   => '[std.task.process-test {:bulk true} {} {}]"
  {:added "3.0"}
  ([task]
   (let [input-fn (-> task :construct :input)]
     (task-inputs task (input-fn task) task)))
  ([task input]
   (let [input-fn (-> task :construct :input)
         [input params] (cond (map? input)
                              [(input-fn task) input]

                              :else [input {}])]
     (task-inputs task input params)))
  ([task input params]
   (let [env-fn (-> task :construct :env)]
     (task-inputs task input params (env-fn (merge task params)))))
  ([task input params env]
   (let [lookup-fn (-> task :construct :lookup)]
     (task-inputs task input params (lookup-fn task (merge env params)) env)))
  ([task input params lookup env]
   [input params lookup env]))

(defn invoke
  "executes the task, given functions and parameters
 
   (def world nil)
 
   (invoke (task/task :namespace \"ns-interns\" ns-interns))
   => {'world #'std.task.process-test/world,
       '*last* #'std.task.process-test/*last*}"
  {:added "3.0"}
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
