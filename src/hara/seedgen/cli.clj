(ns hara.seedgen.cli
  "CLI entry point for seedgen tasks.

   lein seedgen test :all
   lein seedgen test :all :with \"[dart ruby]\"
   lein seedgen test '[xt.lang] :with [dart ruby]

   lein seedgen root '[xt.sample]
   lein seedgen list '[xt.sample]
   lein seedgen readforms '[xt.sample]
   lein seedgen benchlist '[xt.sample]
   lein seedgen incomplete '[xt.sample]
   lein seedgen langadd '[xt.sample] :lang lua :write true
   lein seedgen langremove '[xt.sample] :lang lua :write true
   lein seedgen benchadd '[xt.sample] :lang python :write true
   lein seedgen benchremove '[xt.sample] :lang python :write true
   lein seedgen compatible
   lein seedgen compatible '[xt.lang]"
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [code.project :as project]
            [code.test.base.executive :as executive]
            [code.test.task :as test-task]
            [hara.seedgen :as seedgen]
            [std.fs :as fs]
            [std.lib.result :as res]))

(def +compatibility-path+
  "config/xtalk/xtalk_compatibility.edn")

(defn- read-arg
  "reads a CLI arg, stripping a leading quote if present"
  {:added "4.1"}
  [s]
  (let [v (read-string s)]
    (if (and (seq? v) (= 'quote (first v)))
      (second v)
      v)))

(defn- parse-test-args
  "parses args after the 'test' subcommand"
  {:added "4.1"}
  [args]
  (let [[selector & more] args
        selector (read-arg selector)
        [langs more] (cond
                       (and (= ":with" (first more))
                            (second more))
                       [(read-arg (second more)) (drop 2 more)]

                       (seq more)
                       [(read-arg (first more)) (rest more)]

                       :else
                       [nil more])]
    {:selector selector
     :langs    (if (keyword? langs) [langs] langs)
     :extra    more}))

(defn- load-compatibility
  "loads the xtalk compatibility config"
  {:added "4.1"}
  []
  (edn/read-string (slurp +compatibility-path+)))

(defn- source-namespaces
  "returns all xt.* source test namespaces"
  {:added "4.1"}
  ([] (source-namespaces (project/project)))
  ([project]
   (->> (project/all-files ["test-lang/xt"] {} project)
        keys
        sort
        vec)))

(defn- prefix-matches?
  "checks if ns-sym starts with any of the given prefixes"
  {:added "4.1"}
  [ns-sym prefixes]
  (let [s (str ns-sym)]
    (boolean (some #(str/starts-with? s %) prefixes))))

(defn- language-rules
  "computes the include/exclude namespace prefix rules for a language from the config"
  {:added "4.1"}
  [lang config]
  (if-let [rules (get config lang)]
    (let [include (:include rules :all)
          exclude (mapv str (:exclude rules []))]
      {:include (if (= :all include)
                  ["xt."]
                  (mapv str include))
       :exclude exclude})
    (throw (ex-info "Language not in compatibility config"
                    {:lang lang :path +compatibility-path+}))))

(defn- compatible-namespaces
  "returns source xt.* namespaces that match both the selector and language rules"
  {:added "4.1"}
  [lang selector config project]
  (let [all (source-namespaces project)
        {:keys [include exclude]} (language-rules lang config)
        selector-prefs (cond
                         (= :all selector) ["xt."]
                         (symbol? selector) [(str selector)]
                         (sequential? selector) (mapv str selector)
                         (keyword? selector) [(name selector)]
                         :else [(str selector)])]
    (->> all
         (filter #(prefix-matches? % selector-prefs))
         (filter #(prefix-matches? % include))
         (remove #(prefix-matches? % exclude))
         vec)))

(defn- selector-prefixes
  "normalises a feature selector into a list of namespace prefixes"
  {:added "4.1"}
  [selector]
  (cond
    (= :all selector) ["xt."]
    (symbol? selector) [(str selector)]
    (sequential? selector) (mapv str selector)
    (keyword? selector) [(name selector)]
    :else [(str selector)]))

(defn- compatible-report
  "returns a map of source namespace -> compatible languages for the selector"
  {:added "4.1"}
  ([selector]
   (compatible-report selector (load-compatibility) (project/project)))
  ([selector config project]
   (let [prefs (selector-prefixes selector)
         all   (source-namespaces project)
         langs (keys config)]
     (->> all
          (filter #(prefix-matches? % prefs))
          (map (fn [ns]
                 [ns (vec (sort (filter #(seq (compatible-namespaces % ns config project))
                                        langs)))]))
          (filter (fn [[_ ls]] (seq ls)))
          (into (sorted-map))))))

(defn- generated-namespace
  "computes the xtbench.<lang>.* namespace for a source namespace"
  {:added "4.1"}
  [source-ns lang]
  (let [s (str source-ns)]
    (symbol (str/replace s #"^xt\." (str "xtbench." (name lang) ".")))))

(defn- extract-lang
  "extracts the language keyword from an xtbench.<lang>.* namespace"
  {:added "4.1"}
  [ns-sym]
  (let [parts (str/split (str ns-sym) #"\.")]
    (when (= "xtbench" (first parts))
      (keyword (second parts)))))

(defn- failing-functions
  "builds a per-language map of failing functions from the latest test run"
  {:added "4.1"}
  []
  (let [latest @executive/+latest+
        by-lang (atom {})]
    (doseq [type [:failed :throw :timeout]
            entry (get latest type)]
      (let [meta (:meta entry)
            ns   (:ns meta)
            lang (extract-lang ns)
            fn-sym (:function meta)]
        (when (and lang fn-sym)
          (swap! by-lang update-in [lang type] (fnil conj #{}) fn-sym))))
    (doseq [ns (:errored latest)]
      (when-let [lang (extract-lang ns)]
        (swap! by-lang update-in [lang :errored] (fnil conj #{}) ns)))
    (into (sorted-map)
          (for [[lang data] @by-lang]
            [lang (reduce-kv (fn [out k v]
                               (assoc out k (vec (sort v))))
                             (sorted-map)
                             data)]))))

(defn- failing-summary
  "computes a total failure count from the test summary"
  {:added "4.1"}
  [summary errored]
  (+ (or (:failed summary) 0)
     (or (:throw summary) 0)
     (or (:timeout summary) 0)
     (count errored)))

(defn- save-failing-list
  "saves the per-language failing function list and returns the path"
  {:added "4.1"}
  [failing]
  (let [dir (fs/path ".hara" "runs")
        _   (fs/create-directory dir)
        path (fs/path dir (str "seedgen-failing-" (System/currentTimeMillis) ".edn"))]
    (spit (str path) (with-out-str (pprint/pprint failing)))
    (println "\n[seedgen] failing list saved to" (str path))
    (str path)))

(defn seedgen-test
  "generates and runs xtbench tests for the given selector and languages"
  {:added "4.1"}
  ([selector langs]
   (seedgen-test selector langs (project/project)))
  ([selector langs project]
   (let [config (load-compatibility)
         langs  (->> (or langs (keys config))
                    (map keyword)
                    vec)
         _      (when (empty? langs)
                  (throw (ex-info "No languages specified" {})))
         generated (reduce (fn [out lang]
                             (let [nss (compatible-namespaces lang selector config project)]
                               (if (seq nss)
                                 (do (println "\n[seedgen]" (count nss) "namespaces for" lang)
                                     (let [result (seedgen/seedgen-benchadd nss {:lang [lang]
                                                                                 :write true})]
                                       (when (res/result? result)
                                         (throw (ex-info "Bench generation failed"
                                                         {:lang lang :result result})))
                                       (assoc out lang (mapv #(generated-namespace % lang) nss))))
                                 (do (println "\n[seedgen] no compatible namespaces for" lang)
                                     (assoc out lang [])))))
                           {}
                           langs)
         all-nss (vec (sort (mapcat val generated)))
         _       (println "\n[seedgen] running" (count all-nss) "generated namespaces")
         summary (test-task/run all-nss {} project)
         failing (failing-functions)
         _       (save-failing-list failing)
         errors  (:errored @executive/+latest+)]
     (println "\n[seedgen] per-language failing functions:")
     (pprint/pprint failing)
     {:generated generated
      :summary   summary
      :failing   failing
      :exit      (failing-summary summary errors)})))

(def ^{:added "4.1"} ^:private commands
  "dispatch table for non-test seedgen commands"
  {"root"        {:fn seedgen/seedgen-root    :lang? false}
   "list"        {:fn seedgen/seedgen-list    :lang? false}
   "readforms"   {:fn seedgen/seedgen-readforms :lang? false}
   "benchlist"   {:fn seedgen/seedgen-benchlist :lang? false}
   "incomplete"  {:fn seedgen/seedgen-incomplete :lang? false}
   "langremove"  {:fn seedgen/seedgen-langremove :lang? true}
   "langadd"     {:fn seedgen/seedgen-langadd    :lang? true}
   "benchadd"    {:fn seedgen/seedgen-benchadd   :lang? true}
   "benchremove" {:fn seedgen/seedgen-benchremove :lang? true}})

(defn- parse-command-params
  "parses keyword/value option pairs after the namespace selector"
  {:added "4.1"}
  [args]
  (loop [opts {} args args]
    (if (seq args)
      (let [k (read-string (first args))
            k (if (= :with k) :lang k)
            [v args] (if (seq (rest args))
                       [(read-arg (second args)) (drop 2 args)]
                       (throw (ex-info "Missing value for option"
                                       {:key k :args args})))]
        (recur (assoc opts k v) args))
      opts)))

(defn- parse-command-args
  "parses namespace selector and options for a generic seedgen command"
  {:added "4.1"}
  [args]
  (let [[selector & more] args
        selector (read-arg selector)
        params (parse-command-params more)]
    {:selector selector :params params}))

(defn- run-seedgen-command
  "runs a single seedgen command and returns its result"
  {:added "4.1"}
  [command args]
  (case command
    "test"
    (let [{:keys [selector langs]} (parse-test-args args)]
      (seedgen-test selector langs))

    "compatible"
    (let [{:keys [selector]} (parse-command-args args)
          selector (or selector :all)
          report (compatible-report selector)]
      (println (str "\n[seedgen] compatible namespaces" (if (= :all selector) "" (str " for " selector)) ":"))
      (pprint/pprint report)
      report)

    (if-let [{:keys [fn lang?]} (get commands command)]
      (let [{:keys [selector params]} (parse-command-args args)
            params (-> params
                       (cond-> lang? (update :lang keyword))
                       (assoc :print {:item true :result true :summary true}))]
        (when (and lang? (not (:lang params)))
          (throw (ex-info (str command " requires a :lang option")
                          {:command command :params params})))
        (fn selector params))
      (throw (ex-info "Unknown seedgen command"
                      {:command command :available (conj (sort (keys commands)) "compatible" "test")})))))

(defn -main
  "main entry point for lein seedgen"
  {:added "4.1"}
  [& args]
  (let [command (first args)]
    (if command
      (try
        (run-seedgen-command command (rest args))
        (System/exit 0)
        (catch Throwable t
          (println "Error:" (ex-message t))
          (when-let [data (ex-data t)]
            (pprint/pprint data))
          (System/exit 1)))
      (do (println "Usage: lein seedgen <command> <selector> [options]")
          (println "Commands:" (str/join ", " (conj (sort (keys commands)) "compatible" "test")))
          (System/exit 1)))))
