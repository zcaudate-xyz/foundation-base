(ns code.test.task
  (:require [std.string :as str]
            [code.project :as project]
            [std.task :as task]
            [code.test.checker.common]
            [code.test.checker.collection]
            [code.test.checker.logic]
            [code.test.base.runtime :as rt]
            [code.test.base.listener :as listener]
            [code.test.base.print :as print]
            [code.test.compile]
            [code.test.base.executive :as executive]
            [std.lib :as h :refer [definvoke]]
            [std.lib.result :as res]))

(defn- display-errors
  [data]
  (let [errors (concat (executive/retrieve-line :failed data)
                       (executive/retrieve-line :thrown data))
        cnt  (count (:passed data))]
    (if (empty? errors)
      (res/result {:status :highlight
                   :data  (format "passed (%s)" cnt)})
      (res/result {:status :error
                   :data (format "passed (%s), errors: #{%s}"
                                 cnt
                                 (str/joinl ", " (mapv #(str (first %) ":" (second %))
                                                       errors)))}))))

(defn- retrieve-fn [kw]
  (fn [data]
    (->> (executive/retrieve-line kw data)
         (mapv #(str (first %) ":" (second %))))))

(defn- test-lookup [project]
  (project/all-files (:test-paths project)
                     {}
                     project))

(defmethod task/task-defaults :test
  ([_]
   {:construct {:input    (fn [_] *ns*)
                :lookup   (fn [_ project]
                            (test-lookup project))
                :env      (fn [_] (project/project))}
    :params    {:print {:item true
                        :result true
                        :summary true}
                :return :summary}
    :arglists '([] [ns] [ns params] [ns params project] [ns params lookup project])
    :main      {:count 4}
    :item      {:list     (fn [lookup _] (sort (keys lookup)))
                :pre      project/sym-name
                :output   executive/summarise
                :display  display-errors}
    :result    {:ignore  (fn [data]
                           (and (empty? (:failed data))
                                (empty? (:thrown data))))
                :keys    {:failed  (retrieve-fn :failed)
                          :thrown  (retrieve-fn :thrown)}
                :columns [{:key    :key
                           :align  :left}
                          {:key    :failed
                           :align  :left
                           :length 40
                           :color  #{:red}}
                          {:key    :thrown
                           :align  :left
                           :length 40
                           :color  #{:yellow}}]}
    :summary  {:finalise  executive/summarise-bulk}}))

(defn run:interrupt
  []
  (alter-var-root #'std.task.process/*interrupt*
                  (fn [_] true)))

(definvoke run
  "runs all tests
 
   (task/run :list)
 
   (task/run 'std.lib.foundation)
   ;; {:files 1, :thrown 0, :facts 8, :checks 18, :passed 18, :failed 0}
   => map?"
  {:added "3.0"}
  [:task {:template :test
          :main   {:fn executive/run-namespace}
          :params {:title "TEST PROJECT"}}])

(definvoke run:current
  "runs the current namespace"
  {:added "4.0"}
  [:task {:template :test
          :main   {:fn executive/run-current}
          :params {:title "TEST PROJECT"}}])

(definvoke run:test
  "runs loaded tests"
  {:added "3.0"}
  [:task {:template :test
          :main   {:fn executive/test-namespace}
          :params {:title "TEST EVAL"}}])

(definvoke run:unload
  "unloads the test namespace"
  {:added "3.0"}
  [:task {:template :test
          :main   {:fn executive/unload-namespace}
          :item   {:output identity}
          :params {:title "TEST UNLOADED"}}])

(definvoke run:load
  "load test namespace"
  {:added "3.0"}
  [:task {:template :test
          :main   {:fn executive/load-namespace}
          :item   {:output identity}
          :params {:title "TEST LOADED"}}])

(defn print-options
  "output options for test results
 
   (task/print-options)
   => #{:disable :default :all :current :help}
 
   (task/print-options :default)
   => #{:print-bulk :print-failure :print-thrown}"
  {:added "3.0"}
  ([] (print-options :help))
  ([opts]
   (cond (set? opts)
         (alter-var-root #'print/*options*
                         (constantly opts))

         (= :help opts)
         #{:help :current :default :disable :all}

         (= :current opts) print/*options*

         (= :default opts)
         (alter-var-root #'print/*options*
                         (constantly #{:print-thrown :print-failure :print-bulk}))

         (= :disable opts)
         (alter-var-root #'print/*options* (constantly #{}))

         (= :all opts)
         #{:print-thrown :print-success :print-facts
           :print-facts-success :print-failure :print-bulk})))

;;(print-options (print-options :default))

(defn process-args
  "processes input arguments
 
   (task/process-args [\"hello\"])
   => {:hello true}

   (task/process-args [\":only\" \"hello\"])
   => {:only \"hello\"}"
  {:added "3.0"}
  ([args]
   (loop [out {}
          [curr & more :as args] args]
     (cond (empty? args) out

           (= ":only" curr)
           (recur (assoc out :only (first more))
                  (rest more))

           (= "exit" curr)
           (recur (assoc out :exit true)
                  more)

           :else
           (let [k (try (read-string curr) (catch Exception _ (keyword curr)))
                 k (cond (symbol? k) (keyword (name k))
                         (keyword? k) k
                         (string? k) (keyword k)
                         :else (keyword (str k)))]
             (recur (assoc out k true)
                    more))))))

(defn -main
  "main entry point for leiningen
 
   (task/-main)"
  {:added "3.0"}
  ([& args]
   (let [args (process-args args)
         target (or (when-let [ns-arg (:only args)]
                      (symbol ns-arg))
                    :all)
         {:keys [thrown failed] :as stats} (run target)
         res (+ thrown failed)]
     (if (get args :exit)
       (System/exit res)
       res))))

(defn run-errored
  "runs only the tests that have errored
 
   (task/run-errored)"
  {:added "3.0"}
  ([]
   (let [latest @executive/+latest+]
     (-> (h/union (set (:errored latest))
                  (set (map (comp :ns :meta) (:failed latest)))
                  (set (map (comp :ns :meta) (:thrown latest))))
         (run)))))

(comment
  (run '[platform])
  (run-errored)
  (./ns:reset '[hara])
  (./ns:reset '[std.task])

  (fact "hello"
    2))
