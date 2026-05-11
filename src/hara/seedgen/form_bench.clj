(ns hara.seedgen.form-bench
  (:require [clojure.string :as str]
             [code.framework :as base]
             [code.project :as project]
             [std.fs :as fs]
             [hara.seedgen.common-meta :as runtime]
             [hara.seedgen.common-util :as common]
             [hara.seedgen.form-common :as form-common]
             [hara.seedgen.form-infile :as form-infile]
             [hara.seedgen.form-parse :as readforms]
             [std.lib.result :as res]
             [std.task :as task]))

(def ^:private +seedgen-bench-default-rename+
  '{xt [xtbench :lang]})

(defn- bench-rename-part->segments
  [part lang]
  (cond
    (= :lang part)
    [(name lang)]

    (keyword? part)
    [(name part)]

    (symbol? part)
    [(name part)]

    (string? part)
    [part]

    (vector? part)
    (mapcat #(bench-rename-part->segments % lang) part)

     :else
     [(str part)]))

(defn- bench-requested-runtime-lang
  [lang]
  (-> lang
      runtime/normalize-runtime-lang
      common/seedgen-normalize-runtime-lang))

(defn- bench-display-lang
  [lang]
  (common/seedgen-display-lang (bench-requested-runtime-lang lang)))

(defn- bench-resolve-runtime-lang
  [requested-lang available-langs]
  (let [requested-runtime (bench-requested-runtime-lang requested-lang)
        requested-display (bench-display-lang requested-lang)
        candidate-score   (fn [candidate]
                            (let [candidate-display (bench-display-lang candidate)
                                  candidate-tag     (common/seedgen-dispatch-tag candidate)]
                              (cond
                                (= candidate requested-runtime)
                                [0]

                                (or (= candidate-display requested-runtime)
                                    (= candidate-display requested-display))
                                [1]

                                (or (= candidate-tag requested-runtime)
                                    (= candidate-tag requested-display))
                                [2]

                                :else
                                nil)))]
    (or (->> available-langs
             (keep (fn [candidate]
                     (when-let [score (candidate-score candidate)]
                       [score candidate])))
             (sort-by first)
             first
             second)
        requested-runtime)))

(defn- bench-target-ns
  [test-ns lang rename]
  (let [rename (merge +seedgen-bench-default-rename+
                      (or rename {}))
        segments (str/split (str test-ns) #"\.")]
    (->> segments
         (mapcat (fn [segment]
                   (if-let [replacement (get rename (symbol segment))]
                     (bench-rename-part->segments replacement lang)
                     [segment])))
         (str/join ".")
         symbol)))

(defn- bench-target-path
  [project test-file target-ns]
  (str (fs/path (:root project)
                (project/test-root test-file project)
                (str (fs/ns->file target-ns) ".clj"))))

(defn- bench-resolve-targets
  [ns params lookup project]
  (let [test-ns   (project/test-ns ns)
        test-file (lookup test-ns)
        params    (task/single-function-print params)
        proj      (or project (project/project))]
    (cond
      (nil? test-file)
      (res/result {:status :error
                   :data :no-test-file})

       :else
        (let [output        (readforms/seedgen-readforms ns {} lookup proj)
              root-lang     (get-in output [:globals :lang :root])
              stored-langs  (or (form-infile/root-script-meta-langs output) [])
              derived-langs (get-in output [:globals :lang :derived])]
         (cond
           (res/result? output)
           output

           (nil? root-lang)
          (res/result {:status :error
                       :data :no-seedgen-root})

            :else
            (let [default-langs   (->> (concat [root-lang]
                                               stored-langs
                                               derived-langs)
                                       distinct
                                       vec)
                  default-targets (->> default-langs
                                       (map bench-display-lang)
                                       distinct
                                       vec)
                  target-langs    (if (nil? (:lang params))
                                    default-targets
                                    (form-common/target-normalize-langs (:lang params)
                                                                        default-targets))
                  target-entries  (mapv (fn [lang]
                                           (let [runtime-lang (bench-resolve-runtime-lang lang
                                                                                           default-langs)
                                                 display-lang (bench-display-lang lang)
                                                 target-ns (bench-target-ns test-ns
                                                                            display-lang
                                                                            (:rename params))]
                                             {:lang display-lang
                                              :runtime-lang runtime-lang
                                              :ns target-ns
                                              :path (bench-target-path proj
                                                                       test-file
                                                                       target-ns)}))
                                         target-langs)]
            {:project proj
             :params params
             :test-ns test-ns
             :test-file test-file
             :output output
             :target-langs target-langs
             :targets target-entries}))))))

(defn- bench-render-targets
  [output test-file targets]
  (let [text (slurp test-file)]
    (mapv (fn [{:keys [runtime-lang ns] :as target}]
            (assoc target
                   :content (form-infile/render-top-level-target output
                                                                  text
                                                                  runtime-lang
                                                                  ns)))
          targets)))

(defn- bench-output-functions
  [output]
  (->> output
       :entries
       vals
       (mapcat keys)
       sort
       vec))

(defn seedgen-benchlist
  "returns the bench namespaces that should be created for a seedgen test

   (project/in-context
    (seedgen-benchlist 'xt.sample.train-001-test
                        {:lang [:js :python]}))
   => '[xtbench.js.sample.train-001-test
        xtbench.python.sample.train-001-test]"
  {:added "4.1"}
  ([ns params lookup project]
   (let [resolved (bench-resolve-targets ns params lookup project)]
     (if (res/result? resolved)
       resolved
       (->> resolved :targets (mapv :ns))))))

(comment

  ;; ** seed-bench-list
  ;; should list a namespace for creation using the hara.seedgen.* pipeline

  ;; {:rename '{xt  [xtbench :lang]}}
  ;; means that xt.sample.train-001-test
  ;;    -> xtbench.js.sample.train-001-test for :js
  ;;    -> xtbench.lua.sample.train-001-test for :lua etc

  (seedgen-benchlist )
  
  
  (code.project/in-context
   (hara.seedgen.form-bench/seedgen-benchlist 'xt.sample.train-001-test
                                                   {:lang [:js :python]}))
  => )

(defn seedgen-benchadd
  "creates or updates bench files for the requested seedgen runtimes"
  {:added "4.1"}
  ([ns params lookup project]
   (let [resolved (bench-resolve-targets ns params lookup project)]
     (if (res/result? resolved)
        resolved
          (let [{:keys [output project params test-file targets]} resolved
                rendered (bench-render-targets output test-file targets)
                write?   (boolean (:write params))
                lookup'  (reduce (fn [m {:keys [ns path]}]
                                   (assoc m ns path))
                               lookup
                               rendered)]
          {:functions (bench-output-functions output)
           :outputs
            (mapv (fn [{:keys [lang runtime-lang ns path content]}]
                    (when write?
                      (fs/create-directory (fs/parent path)))
                    (let [result (base/transform-code ns
                                                      (assoc params
                                                           :transform (constantly content)
                                                           :no-analysis true)
                                                    lookup'
                                                    project)]
                     (assoc (select-keys result [:path :updated :verified :inserts :deletes :count :changed])
                            :lang lang
                            :runtime-lang runtime-lang
                            :ns ns)))
                 rendered)})))))


(comment

  
  ;; - look at the namespace form for [hara.lang :as <ns>]
  ;; - look for the <ns>/script- form and the `:seedgen/root` tag
  ;; - return the <lang> value in  (<ns>/script- <lang>) 
  
  ;; ^{:seedgen/root     {:all true}}
  ;; (l/script- :js
  ;;            {:runtime :basic
  ;;             :require [[xt.lang.spec-base :as xt]]})
  
  
  (code.project/in-context
   (hara.seedgen.form-bench/seedgen-root 'xt.sample.train-001-test
                                                {}))
  => :js)


(defn seedgen-benchremove
  "removes bench files for the requested seedgen runtimes"
  {:added "4.1"}
  ([ns params lookup project]
   (let [resolved (bench-resolve-targets ns params lookup project)]
     (if (res/result? resolved)
       resolved
        (let [{:keys [params project targets]} resolved
             root (:root project)
             write? (boolean (:write params))]
         {:outputs
          (mapv (fn [{:keys [lang ns path]}]
                  (let [exists? (fs/exists? path)
                        updated (and write? exists? (do (fs/delete path) true))
                        relpath (if root
                                  (str (fs/relativize root path))
                                  (str path))]
                    {:lang lang
                     :ns ns
                     :path relpath
                     :updated (boolean updated)
                     :exists exists?}))
                targets)})))))

(comment

  ;; seed-bench takes the output of 
  
  ;; - look at the namespace form for [hara.lang :as <ns>]
  ;; - look for the <ns>/script- form and the `:seedgen/root` tag
  ;; - return the <lang> value in  (<ns>/script- <lang>) 
  
  ;; ^{:seedgen/root     {:all true}}
  ;; (l/script- :js
  ;;            {:runtime :basic
  ;;             :require [[xt.lang.spec-base :as xt]]})
  
  
  (code.project/in-context
   (hara.seedgen.form-bench/seedgen-root 'xt.sample.train-001-test
                                                {}))
  => :js)
