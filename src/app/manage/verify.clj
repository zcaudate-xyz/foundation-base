(ns app.manage.verify
  "Declarative page verification shared by UI and CLI; no executable assertions."
  (:require [app.manage :as manage] [app.manage.slices :as slices]
            [clojure.edn :as edn] [clojure.java.io :as io]))
(defn- read-file [p path]
  (let [f (manage/project-file p path)] (if (.isFile f) (edn/read-string (slurp f)) {:revision 0 :cases {}})))
(defn- write-file! [p path value]
  (let [f (manage/project-file p path)] (io/make-parents f)
    (let [tmp (java.io.File/createTempFile "verify-" ".edn" (.getParentFile f))]
      (try (spit tmp (pr-str value))
        (java.nio.file.Files/move (.toPath tmp) (.toPath f)
          (into-array java.nio.file.CopyOption [java.nio.file.StandardCopyOption/ATOMIC_MOVE java.nio.file.StandardCopyOption/REPLACE_EXISTING]))
        (finally (.delete tmp))))))
(defn cases [p] (read-file p ".app-manage/page-tests.edn"))
(defn- sha256 [value]
  (format "%064x" (java.math.BigInteger. 1 (.digest (java.security.MessageDigest/getInstance "SHA-256") (.getBytes (str value) "UTF-8")))))
(defn fingerprint [p]
  (sha256 (str (slices/fingerprint p (:verification-targets p)) (pr-str (cases p)))))
(defn with-receipt
  "Wraps a generated runtime target with source and artifact hashes for verification."
  [target path]
  (update target :render
    (fn [render]
      (fn [{:keys [project target] :as context}]
        (let [artifacts (render context)]
          (assoc artifacts path (pr-str {:source (slices/fingerprint project [target] false)
                                        :artifacts (into (sorted-map) (map (fn [[k v]] [k (sha256 v)]) artifacts))})))))))
(defn ready? [p]
  (if-let [path (:verification-receipt p)]
    (let [receipt (read-file p path)]
      (and (= (:source receipt) (slices/fingerprint p (:verification-targets p) false))
           (seq (:artifacts receipt))
           (every? (fn [[path hash]] (let [file (manage/project-file p path)]
                                     (and (.isFile file) (= hash (sha256 (slurp file)))))) (:artifacts receipt))))
    true))
(defn subset? [expected actual]
  (if (map? expected) (and (map? actual) (every? (fn [[k v]] (and (contains? actual k) (subset? v (get actual k)))) expected)) (= expected actual)))
(defn compare-result [actual expect]
  (when (and expect (not= 1 (count (filter #(contains? expect %) [:equals :contains :error]))))
    (throw (ex-info "Choose exactly one expectation: equals, contains or error" {:http-status 400})))
  (cond
    (nil? expect) (if (:error actual) :failed :executed)
    (contains? expect :error) (if (and (:error actual) (subset? (:error expect) (:error actual))) :passed :failed)
    (:error actual) :failed
    (contains? expect :equals) (if (and (contains? actual :result) (= (:equals expect) (:result actual))) :passed :failed)
    (contains? expect :contains) (if (and (contains? actual :result) (subset? (:contains expect) (:result actual))) :passed :failed)
    :else (throw (ex-info "Expectation needs equals, contains or error" {:http-status 400}))))
(defn save! [p {:keys [id revision case]}]
  (when-not (and (string? id) (re-matches #"[a-z][a-z0-9-]{0,79}" id) (map? case))
    (throw (ex-info "Invalid case" {:http-status 400})))
  (doseq [entry (if (seq (:steps case)) (:steps case) [case])]
    (compare-result {} (:expect entry))
    (when-not (:expect entry) (throw (ex-info "Every saved step needs an expectation" {:http-status 400}))))
  (when (and (:steps case) (or (> (count (:steps case)) 32)
    (not (every? #(and (string? (:id %)) (re-matches #"[a-z][a-z0-9-]{0,79}" (:id %))) (:steps case)))
    (not= (count (:steps case)) (count (distinct (map :id (:steps case)))))))
    (throw (ex-info "Use at most 32 steps with unique named IDs" {:http-status 400})))
  (when-not (or (:expect case) (seq (:steps case))) (throw (ex-info "Saved checks need an expectation" {:http-status 400})))
  (let [state (cases p)]
    (when-not (= revision (:revision state)) (throw (ex-info "Cases changed; refresh before saving" {:http-status 409})))
    (let [next (-> state (update :revision inc) (assoc-in [:cases id] case))]
      (write-file! p ".app-manage/page-tests.edn" next) next)))
(defn evaluate-request [request actual]
  (if (:blocked actual)
    (assoc actual :status :blocked)
    (if (seq (:steps request))
    (let [results (mapv (fn [step result]
                         (assoc result :status (cond (:blocked result) :blocked (not= (:id step) (:id result)) :failed :else (compare-result result (:expect step)))
                                       :expected (:expect step))) (:steps request) (:steps actual))]
      (assoc actual :steps results :status
        (if (and (= (count (:steps request)) (count (:steps actual)) (count results)) (every? #(= :passed (:status %)) results)) :passed :failed)))
    (assoc actual :status (compare-result actual (:expect request))))))
(defn execute! [p request]
  (let [provider (or (:page-verifier p) (throw (ex-info "No verification runtime declared" {:http-status 400})))
        before (fingerprint p)
        actual (if-not (ready? p) {:blocked "Verification runtime is missing or stale; rebuild its target"}
                 (try ((if (symbol? provider) (requiring-resolve provider) provider) request)
                    (catch Exception e {:error {:message (.getMessage e)}})))
        result (assoc (evaluate-request request actual) :expected (:expect request)
                      :request request :time (System/currentTimeMillis) :fingerprint before)]
    (write-file! p ".cache/app.manage/page-verification.edn" result) result))
(defn run-cases! [p]
  (let [entries (:cases (cases p))
        run (fn []
          (when-not (seq entries) (throw (ex-info "No saved checks" {:http-status 400})))
          (let [results (mapv (fn [[id request]] (assoc (execute! p request) :id id)) (sort-by key entries))
                report {:status (if (every? #(= :passed (:status %)) results) :ok :error) :cases results
                        :fingerprint (fingerprint p) :time (System/currentTimeMillis)}]
            (write-file! p ".cache/app.manage/page-verification.edn" report) report))]
    (if-let [fixture (:page-test-fixture p)] ((requiring-resolve fixture) run) (run))))
(defn definitions! [p]
  (let [provider (:pages p) pages ((if (symbol? provider) (requiring-resolve provider) provider))
        findings (vec (for [page pages [kind entries] [[:models (:models page)] [:actions (:actions page)]]
                           [id entry] entries :let [spec (get entry "operation-spec")]
                           :when (or (not spec) (and (= kind :models) (get spec "write")))]
                       {:page (:id page) :member id :kind kind :error "Missing operation or mutation declared as a read"}))
        result {:status (if (seq findings) :error :ok) :kind :contracts :pages (count pages) :findings findings
                :time (System/currentTimeMillis) :fingerprint (fingerprint p)}]
    (write-file! p ".cache/app.manage/page-verification.edn" result) result))
(defn snapshot [p]
  (let [file (manage/project-file p ".cache/app.manage/page-verification.edn")]
    {:saved (cases p) :latest (when (.isFile file)
      (let [r (edn/read-string (slurp file))]
        (cond-> r (not= (:fingerprint r) (fingerprint p)) (assoc :status :stale))))}))
(defn -main [project-var & [operation]]
  (let [p (var-get (requiring-resolve (symbol project-var)))]
    (try (let [result (if (= operation "run") (run-cases! p) (snapshot p))] (prn result) (when (= :error (:status result)) (throw (ex-info "Verification failed" result)))) (finally (shutdown-agents)))))
