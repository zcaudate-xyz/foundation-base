(ns std.pipe.util
  (:require [std.lib :as h]
            [std.lib.result :as res]))

(defn main-function
  "creates a main function to be used for execution"
  {:added "4.0"}
  ([func count]
   (let [fcounts (h/arg-count func)
         fcount  (if-not (empty? fcounts)
                   (apply min fcounts)
                   3)
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
  "matches given a range of filters"
  {:added "4.0"}
  [selector id]
  (cond (or (fn?  selector)
            (var? selector))
        (h/suppress (selector id))

        (or (string? selector)
            (symbol? selector)
            (keyword? selector))
        (.startsWith ^String (str id) (str selector))

        (h/regexp? selector)
        (boolean (re-find selector (str id)))

        (set? selector) (selector id)

        (h/form? selector)  (every? #(select-filter % id)
                                    selector)

        (vector? selector) (some #(select-filter % id)
                                 selector)

        (number? selector) (= selector id)

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
