(ns code.manage.automation
  "Machine-readable incomplete-test reporting for CI and repair workers."
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.set :as set]
            [clojure.string :as str]
            [code.manage.unit :as unit]
            [code.manage.unit.template :as template]
            [code.project :as project]
            [std.fs :as fs]
            [std.json :as json]
            [std.lib.result :as res])
  (:import (java.security MessageDigest)
           (java.time Instant)))

(def +schema-version+ 1)
(def +config-path+ "config/manage/automation.edn")

(defn sha256
  "returns a lowercase SHA-256 digest"
  {:added "4.1"}
  [value]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256")
                        (.getBytes (str value) "UTF-8"))]
    (apply str (map #(format "%02x" (bit-and % 0xff)) digest))))

(defn load-edn
  "reads an EDN file, returning the fallback when absent"
  {:added "4.1"}
  [path fallback]
  (if (and path (fs/exists? path))
    (edn/read-string (slurp path))
    fallback))

(defn load-config
  "loads and validates the code.manage automation config"
  {:added "4.1"}
  ([] (load-config +config-path+))
  ([path]
   (let [config (load-edn path nil)]
     (when-not (= +schema-version+ (:schema-version config))
       (throw (ex-info "Unsupported code.manage automation config" {:path path})))
     (when-not (seq (:sections config))
       (throw (ex-info "Automation config has no sections" {:path path})))
     config)))

(defn section-config
  "returns one configured section"
  {:added "4.1"}
  [config section]
  (or (get-in config [:sections (keyword section)])
      (throw (ex-info "Unknown code.manage automation section"
                      {:section section :available (keys (:sections config))}))))

(defn- selector-prefixes
  [selector]
  (let [selector (if (sequential? selector) selector [selector])]
    (mapv #(if (keyword? %) (name %) (str %)) selector)))

(defn section-namespaces
  "selects source namespaces for a configured section"
  {:added "4.1"}
  [selector project]
  (let [prefixes (selector-prefixes selector)]
    (->> (template/source-namespaces {} project)
         (filter (fn [ns]
                   (let [candidate (str ns)]
                     (some (fn [prefix]
                             (if (str/ends-with? prefix ".")
                               (str/starts-with? candidate prefix)
                               (or (= candidate prefix)
                                   (str/starts-with? candidate (str prefix ".")))))
                           prefixes))))
         vec)))

(defn finding-id
  "returns a stable finding id independent of file line numbers"
  {:added "4.1"}
  [kind refer]
  (str (name kind) ":" refer))

(defn- inferred-test-path
  [source-ns project]
  (str (:root project) "/test/"
       (-> (str source-ns)
           (str/replace "-" "_")
           (str/replace "." "/"))
       "_test.clj"))

(defn- finding
  [kind source-ns refer lookup project]
  (let [source-path (lookup source-ns)
        test-ns     (project/test-ns source-ns)
        test-path   (or (lookup test-ns) (inferred-test-path source-ns project))
        root        (fs/path (:root project))
        source-path (when source-path (str (fs/relativize root source-path)))
        test-path   (str (fs/relativize root test-path))
        line        (select-keys (meta refer) [:row :col :end-row :end-col])
        refer       (if (namespace refer)
                      refer
                      (symbol (str source-ns) (name refer)))]
    (cond-> {:id (finding-id kind refer)
             :kind kind
             :namespace (str source-ns)
             :refer (str refer)
             :test-path (str test-path)}
      source-path (assoc :source-path source-path)
      (seq line) (assoc (if (= kind :todo-test) :test-line :source-line) line))))

(defn namespace-findings
  "returns typed missing and TODO findings for one source namespace"
  {:added "4.1"}
  [source-ns lookup project]
  (let [params {:print {}}
        missing (unit/missing source-ns params lookup project)
        todos   (unit/todos source-ns params lookup project)]
    (vec
     (concat
      (when-not (res/result? missing)
        (map #(finding :missing-test source-ns % lookup project) missing))
      (when-not (res/result? todos)
        (map #(finding :todo-test source-ns % lookup project) todos))))))

(defn baseline-diff
  "classifies current finding ids against a baseline"
  {:added "4.1"}
  [baseline-ids findings]
  (let [baseline (set baseline-ids)
        current  (set (map :id findings))]
    {:baseline (vec (sort (set/intersection baseline current)))
     :new (vec (sort (set/difference current baseline)))
     :resolved (vec (sort (set/difference baseline current)))}))

(defn report
  "creates a versioned incomplete-test report for one configured section"
  {:added "4.1"}
  ([section] (report section {}))
  ([section {:keys [config-path baseline-path policy project]
             :or {config-path +config-path+}}]
   (let [project  (or project (project/project))
         config   (load-config config-path)
         section  (keyword section)
         spec     (section-config config section)
         lookup   (project/file-lookup project)
         nss      (section-namespaces (:selector spec) project)
         findings (->> nss
                       (mapcat #(namespace-findings % lookup project))
                       (sort-by :id)
                       vec)
         baseline-path (or baseline-path (:baseline config))
         baseline (load-edn baseline-path {:sections {}})
         diff     (baseline-diff (get-in baseline [:sections section] []) findings)
         fingerprint (sha256 (str/join "\n" (map :id findings)))
         policy   (or policy (:policy config) :observe)
         exit     (if (and (= :new-only policy) (seq (:new diff))) 1 0)]
     {:schema-version +schema-version+
      :kind "code.manage.incomplete"
      :section (name section)
      :label (or (:label spec) (name section))
      :selector (mapv str (:selector spec))
      :test-command (:test-command spec)
      :policy (name policy)
      :repository (or (System/getenv "GITHUB_REPOSITORY") (str (:name project)))
      :sha (System/getenv "GITHUB_SHA")
      :ref (System/getenv "GITHUB_REF")
      :run-id (System/getenv "GITHUB_RUN_ID")
      :generated-at (.toString (Instant/now))
      :fingerprint fingerprint
      :counts {:total (count findings)
               :baseline (count (:baseline diff))
               :unchanged (count (:baseline diff))
               :new (count (:new diff))
               :resolved (count (:resolved diff))}
      :diff diff
      :findings findings
      :exit exit})))

(defn write-json!
  "writes a report as pretty JSON"
  {:added "4.1"}
  [path value]
  (when-let [parent (fs/parent path)]
    (fs/create-directory parent))
  (spit path (str (json/write-pp value) "\n"))
  path)

(defn write-baseline!
  "updates one section in the reviewed baseline"
  {:added "4.1"}
  [path section findings]
  (let [baseline (load-edn path {:schema-version +schema-version+ :sections {}})
        updated  (assoc-in baseline [:sections (keyword section)]
                           (vec (sort (map :id findings))))]
    (when-let [parent (fs/parent path)]
      (fs/create-directory parent))
    (spit path (with-out-str (pprint/pprint updated)))
    updated))

(defn incomplete-report
  "CLI-facing report writer; returns the report including its exit code"
  {:added "4.1"}
  [_target {:keys [section output write-baseline] :as opts}]
  (when-not section
    (throw (ex-info "incomplete-report requires :section" {:options opts})))
  (let [result (report section opts)]
    (when output (write-json! (str output) result))
    (when write-baseline
      (write-baseline! (or (:baseline-path opts)
                           (:baseline (load-config (or (:config-path opts) +config-path+))))
                       section
                       (:findings result)))
    result))

(defn -main
  "writes the configured section report for CI"
  {:added "4.1"}
  [& [section output policy]]
  (when-not (and section output)
    (throw (ex-info "Usage: code.manage.automation SECTION OUTPUT [POLICY]" {})))
  (let [result (incomplete-report :all
                                  (cond-> {:section (keyword section)
                                           :output output}
                                    policy (assoc :policy (keyword policy))))
        new-count (get-in result [:counts :new])]
    (println (format "code.manage incomplete [%s]: %d total, %d new"
                     (:section result) (get-in result [:counts :total]) new-count))
    (when (pos? new-count)
      (println (format "::warning title=Incomplete tests (%s)::%d new incomplete test findings"
                       (:label result) new-count)))
    (shutdown-agents)
    (System/exit (:exit result))))
