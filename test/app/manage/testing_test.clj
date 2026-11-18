(ns app.manage.testing-test
  (:require [app.manage.testing :as testing] [app.manage.units-test :as fixture]
            [app.manage.server :as server] [app.manage.server-test :as http])
  (:use code.test))
^{:refer app.manage.testing/select-profile :id registered-selection}
(fact "only registered namespaces can be requested"
  (let [p {:test-profiles {:db {:namespaces '[sample.test]}}}]
    [(mapv :namespaces (testing/profiles p))
     (try (testing/select-profile p "db" ["other.test"]) :accepted (catch Exception e (:http-status (ex-data e))))])
  => [[["sample.test"]] 400])
^{:refer app.manage.server/session! :id owned-source}
(fact "whole source is available only from the indexed inventory"
  (fixture/with-fixture
    (fn [p _]
      (let [s (server/session! (constantly (assoc p :id 'source :targets {:app {:outputs [] :sources ["src"]}})))]
        (try
          [(first (http/http s "GET" "/api/source?path=src/logic.clj" nil nil))
           (first (http/http s "GET" "/api/source?path=project.clj" nil nil))
           (get-in (http/http s "GET" "/api/source?path=src/logic.clj" nil nil) [1 :path])]
          (finally (server/stop! s))))))
  => [200 403 "src/logic.clj"])

^{:refer app.manage.testing/run-request! :id assertion-evidence}
(fact "the execution report captures actual assertion values from code.test"
  (let [file (java.io.File/createTempFile "manager-test-report-" ".edn")]
    (try
      (let [r (testing/run-request! {:namespaces '[app.manage.units-test] :result (str file)})]
        [(:status r) (count (get-in r [:namespaces 0 :assertions]))])
      (finally (.delete file))))
  => [:ok 7])

^{:refer app.manage.server/snapshot :id stale-results}
(fact "source changes mark prior test evidence stale without requiring a UI rescan"
  (fixture/with-fixture
    (fn [p root]
      (let [s (server/session! (constantly (assoc p :id 'source :targets {:app {:outputs [] :sources ["src"]}})))
            fingerprint ((requiring-resolve 'app.manage.units/fingerprint) p)]
        (try
          (swap! (:state s) assoc :order ["test"] :runs {"test" {:operation "run-test-namespaces" :result {:fingerprint fingerprint}}})
          (let [before (get-in (server/snapshot s) ["runs" 0 "result" "stale"])]
            (spit (clojure.java.io/file root "src/logic.clj") "\n; source edited\n" :append true)
            [before (get-in (server/snapshot s) ["runs" 0 "result" "stale"])])
          (finally (server/stop! s))))))
  => [false true])

^{:refer app.manage.process/run :id streamed-output}
(fact "streamed command output preserves the final stdout bytes"
  (let [chunks (atom [])
        result ((requiring-resolve 'app.manage.process/run)
                {:root "." :command ["sh" "-c" "printf first; sleep 0.3; printf second"]
                 :on-output (fn [stream text] (when (= stream :stdout) (swap! chunks conj text)))})]
    [(:status result) (:stdout result) (apply str @chunks)])
  => [:ok "firstsecond" "firstsecond"])

^{:refer app.manage.integration/failure-info :id wrapped-load-errors}
(fact "namespace load errors expose their root cause and preserve diagnostic context"
  (let [root (ex-info "missing runtime" {:runtime :postgres})
        wrapped (ex-info "namespace failed to load" {} root)
        result ((requiring-resolve 'app.manage.integration/failure-info) wrapped)]
    [(:message result) (get-in result [:exception :cause])])
  => ["missing runtime" "missing runtime"])

(defn omit-run [_] :not-executed)
^{:refer app.manage.testing/run-request! :id missing-fixture-callback}
(fact "a fixture that never executes tests cannot report success"
  (let [file (java.io.File/createTempFile "empty-test-report-" ".edn")]
    (try
      (select-keys (testing/run-request! {:namespaces '[app.manage.units-test]
                                         :fixture 'app.manage.testing-test/omit-run :result (str file)})
                   [:status :namespaces :message])
      (finally (.delete file))))
  => {:status :error :namespaces [] :message "Fixture did not execute the requested test namespaces"})

^{:refer app.manage.testing/execute! :id missing-process-report}
(fact "a zero-exit child without a report is not a successful test run"
  (fixture/with-fixture
    (fn [p _]
      (with-redefs [app.manage.process/run (fn [_] {:status :ok :exit 0})]
        (let [r (testing/execute! (assoc p :test-profiles {:sample {:namespaces '[sample.test]}})
                                  {:profile "sample" :namespaces ["sample.test"]})]
          [(:status r) (get-in r [:report :message])]))))
  => [:error "Test runner exited without a structured report"])

^{:refer app.manage.testing/run-request! :id empty-namespace-selection}
(fact "an empty namespace selection cannot produce green execution evidence"
  (let [file (java.io.File/createTempFile "empty-namespace-report-" ".edn")]
    (try (:status (testing/run-request! {:namespaces [] :result (str file)}))
         (finally (.delete file))))
  => :error)

^{:refer app.manage.units/fingerprint :id dependency-version-evidence}
(fact "changing execution dependency versions invalidates prior evidence"
  (fixture/with-fixture
    (fn [p root]
      (let [file (clojure.java.io/file root "project.version")
            p (assoc p :project-sources ["project.version"])
            fingerprint (requiring-resolve 'app.manage.units/fingerprint)]
        (spit file "4.1.8")
        (let [before (fingerprint p)]
          (spit file "4.1.9")
          (not= before (fingerprint p))))))
  => true)
