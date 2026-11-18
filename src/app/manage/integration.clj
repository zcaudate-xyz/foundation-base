(ns app.manage.integration
  "Clojure-owned multi-runtime fixtures and strict application integration checks."
  (:refer-clojure :exclude [run!])
  (:require [code.test :as test]
            [xt.lang.common-notify]
            [code.test.base.executive :as executive]))

(defn fixture
  "Declares ordered resources. start receives prior resources; stop receives context and value.
   Declaring a fixture performs no IO. Failed starts unwind, including their partial resource."
  [steps]
  (when-not (and (vector? steps)
                 (every? #(and (keyword? (:id %)) (ifn? (:start %)) (ifn? (:stop %))) steps)
                 (= (count steps) (count (distinct (map :id steps)))))
    (throw (ex-info "Integration resources need unique IDs and start/stop functions" {})))
  {:steps steps :state (atom {:status :new :context {} :started []})})

(defn context [fixture] (:context @(:state fixture)))

(defn stop!
  "Stops every resource in reverse order, even after a cleanup failure. Idempotent."
  [fixture]
  (let [{:keys [context started]} @(:state fixture)
        failures (atom [])]
    (reset! (:state fixture) {:status :stopped :context {} :started []})
    (doseq [step (reverse started)]
      (try ((:stop step) context (get context (:id step)))
           (catch Throwable error (swap! failures conj error))))
    (when-let [failure (first @failures)]
      (doseq [other (rest @failures)] (.addSuppressed failure other))
      (throw failure))
    true))

(defn start!
  [fixture]
  (when (= :running (:status @(:state fixture)))
    (throw (ex-info "Integration fixture is already running" {})))
  (reset! (:state fixture) {:status :starting :context {} :started []})
  (try (doseq [step (:steps fixture)]
         ;; Register cleanup first so a start that partially succeeds is still unwound.
         (swap! (:state fixture) update :started conj step)
         (let [value ((:start step) (context fixture))]
           (swap! (:state fixture) assoc-in [:context (:id step)] value)))
       (swap! (:state fixture) assoc :status :running)
       (context fixture)
       (catch Throwable failure
         (try (stop! fixture) (catch Throwable cleanup (.addSuppressed failure cleanup)))
         (throw failure))))

(defn failure-info [error]
  (let [causes (vec (take 16 (take-while some? (iterate #(.getCause ^Throwable %) error))))]
    {:message (or (.getMessage ^Throwable (last causes)) (.getMessage ^Throwable error))
     :exception (executive/report-edn (Throwable->map error))}))

(defn run!
  "Runs each code.test namespace separately; rejects load/setup errors, skipped and empty suites."
  [namespaces]
  (when-not (and (seq namespaces) (every? symbol? namespaces))
    (throw (ex-info "Integration checks need explicit code.test namespaces" {})))
  (let [results (mapv (fn [module]
                        (try (let [result (test/run module)
                                   failed? (or (seq (:errored @executive/+latest+))
                                               (not (pos? (or (:passed result) 0)))
                                               (not= (:facts result) (:queued result))
                                               (some pos?
                                                     (map #(or (get result %) 0)
                                                          [:failed :throw :thrown :timeout
                                                           :skipped])))]
                               {:namespace module :status (if failed? :error :ok) :result result})
                             (catch Throwable error
                               (merge {:namespace module :status :error} (failure-info error)))))
                      namespaces)]
    {:status (if (every? #(= :ok (:status %)) results) :ok :error) :namespaces results}))

(defn check
  "An app.manage :checks entry. Assertions and resource lifecycle stay in .clj facts."
  [{:keys [targets namespaces]}]
  (when-not (and (seq namespaces) (every? symbol? namespaces))
    (throw (ex-info "Integration check must declare its test namespaces" {})))
  {:targets (vec targets) :run (fn [_] (run! namespaces))})

(defmacro await-js
  "Waits for a JS promise through the runtime notification channel; rejection fails the fact."
  [expression]
  (let
    [code
     (list
      '.
      (list 'Promise.resolve expression)
      '(then (fn [value] (return (xt.lang.common-repl/notify {"ok" true "value" value}))))
      '(catch
        (fn
         [error]
         (return
          (xt.lang.common-repl/notify
           {"ok" false "message" error.message "code" error.code "stack" error.stack})))))]
    `(let [result# ~(list 'xt.lang.common-notify/wait-on [:js 15000] code)]
       (if (= true (get result# "ok"))
         (get result# "value")
         (throw (ex-info "JavaScript integration promise failed" {:result result#}))))))
