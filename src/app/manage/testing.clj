(ns app.manage.testing
  "Registered namespace execution in isolated child JVMs."
  (:require [app.manage :as manage]
            [app.manage.units :as units]
            [app.manage.process :as process]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [app.manage.integration :as integration]
            [code.test.base.executive :as executive]
            [code.test.base.context :as context]
            [code.test.base.listener :as listener]
            [std.lib.signal :as signal]))

(defn profiles [project]
  (mapv (fn [[id profile]]
          {:id (name id) :label (or (:label profile) (name id))
           :namespaces (mapv str (let [n (:namespaces profile)]
                                  (if (symbol? n) ((requiring-resolve n)) n)))})
        (sort-by key (:test-profiles project))))

(defn select-profile [project profile namespaces]
  (let [spec (get (:test-profiles project) (keyword profile))
        allowed (set (:namespaces (some #(when (= profile (:id %)) %) (profiles project))))]
    (when-not (and spec (seq namespaces) (every? allowed namespaces))
      (throw (ex-info "Namespaces must belong to the selected test profile" {:http-status 400})))
    spec))

(defn execute! [project {:keys [profile namespaces]}]
  (let [fingerprint (units/fingerprint project)
        spec (select-profile project profile namespaces)
        directory (manage/project-file project (str ".cache/app.manage/tests/" (java.util.UUID/randomUUID)))
        request (io/file directory "request.edn") result (io/file directory "result.edn")]
    (.mkdirs directory)
    (spit request (pr-str {:namespaces (mapv symbol namespaces) :fixture (:fixture spec)
                          :result (str result)}))
    (let [out (process/run {:root (str (manage/project-root project))
                           :timeout-ms (or (:timeout-ms spec) 300000)
                           :on-output (fn [stream text] (manage/emit! {:type :test/output :stream stream :text text}))
                           :command ["lein" "run" "-m" "app.manage.testing" (str request)]})
          report (if (.isFile result) (edn/read-string (slurp result))
                     {:status :error :namespaces []
                      :message (if (:timed-out out) "Test namespace timed out; cleanup could not be confirmed"
                                   "Test runner exited without a structured report")})]
      (assoc out :status (if (and (= :ok (:status out)) (= :ok (:status report))) :ok :error)
             :profile profile :namespaces namespaces :fingerprint fingerprint
             :stale (not= fingerprint (units/fingerprint project))
             :report report
             :cleanup (if (:timed-out out) :unconfirmed :reported)))))

(defn run-request! [{:keys [namespaces fixture result]}]
  (let [recorded (atom [])
        invocations (atom 0)
        run (fn []
              (when-not (= 1 (swap! invocations inc))
                (throw (ex-info "A test fixture must invoke its runner exactly once" {})))
              (reset! recorded (mapv (fn [module]
                      (let [checks (atom [])
                            report (binding [signal/*manager* (atom @signal/*manager*)]
                                     (signal/signal:install ::assertions :test (fn [event]
                                     (when (= :check (:test event))
                                       (swap! checks conj (executive/report-edn (assoc (listener/summarise-verify (:result event)) :fact-id (str (get-in event [:result :meta :id]))))))))
                                     (integration/run! [module]))]
                        (assoc report :namespace module :message (get-in report [:namespaces 0 :message]) :exception (get-in report [:namespaces 0 :exception])
                               :assertions @checks
                               :evidence (executive/report-edn {:results (-> report :namespaces first :result meta :data) :errors @executive/+latest+})))) namespaces)))
        report (try
                 (if fixture ((requiring-resolve fixture) run) (run))
                 (let [items @recorded
                       executed? (and (seq items) (= 1 @invocations) (= (mapv :namespace items) namespaces))]
                   (cond-> {:status (if (and executed? (every? #(= :ok (:status %)) items)) :ok :error)
                            :namespaces items}
                     (not executed?) (assoc :message "Fixture did not execute the requested test namespaces")))
                 (catch Throwable e (merge {:status :error :namespaces @recorded} (integration/failure-info e))))]
    (spit result (pr-str report))
    report))

(defn -main [request]
  (let [report (run-request! (edn/read-string (slurp request)))]
    (shutdown-agents)
    (System/exit (if (= :ok (:status report)) 0 1))))
