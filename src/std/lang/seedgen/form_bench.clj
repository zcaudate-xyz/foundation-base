(ns std.lang.seedgen.form-bench
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [code.framework :as base]
            [code.project :as project]
            [std.fs :as fs]
            [std.block.base :as block]
            [std.block.navigate :as nav]
            [std.lang.seedgen.common-util :as common]
            [std.lang.seedgen.form-parse :as readforms]
            [std.lang.manage.xtalk-scaffold :as scaffold]
            [std.lib.result :as res]
            [std.task :as task]))

(def ^:private +seedgen-bench-default-rename+
  '{xt [xtbench :lang]})

(defn- normalize-target-langs
  [lang default-langs]
  (let [target-langs (cond
                       (= :all lang)
                       default-langs

                       (keyword? lang)
                       [lang]

                       (vector? lang)
                       lang

                       (seq? lang)
                       (vec lang)

                       (nil? lang)
                       default-langs

                       :else
                       [lang])]
    (->> target-langs
         (map common/seedgen-normalize-runtime-lang)
         distinct
         vec)))

(defn- rename-part->segments
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
    (mapcat #(rename-part->segments % lang) part)

    :else
    [(str part)]))

(defn- seedgen-bench-ns
  [test-ns lang rename]
  (let [rename (merge +seedgen-bench-default-rename+
                      (or rename {}))
        segments (str/split (str test-ns) #"\.")]
    (->> segments
         (mapcat (fn [segment]
                   (if-let [replacement (get rename (symbol segment))]
                     (rename-part->segments replacement lang)
                     [segment])))
         (str/join ".")
         symbol)))

(defn- seedgen-bench-path
  [project target-ns]
  (str (fs/path (:root project)
                (or (first (:test-paths project))
                    "test")
                (str (fs/ns->file target-ns) ".clj"))))

(defn- resolve-bench-targets
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
      (let [output         (readforms/seedgen-readforms ns {} lookup proj)
            root-lang      (get-in output [:globals :lang :root])
            derived-langs  (get-in output [:globals :lang :derived])
            available-langs (vec (concat (when root-lang [root-lang]) derived-langs))]
        (cond
          (res/result? output)
          output

          (nil? root-lang)
          (res/result {:status :error
                       :data :no-seedgen-root})

          :else
          (let [target-langs    (normalize-target-langs (:lang params) available-langs)
                unsupported     (->> target-langs
                                     (remove (set available-langs))
                                     distinct
                                     vec)
                target-entries  (mapv (fn [lang]
                                        (let [target-ns (seedgen-bench-ns test-ns
                                                                          lang
                                                                          (:rename params))]
                                          {:lang lang
                                           :ns target-ns
                                           :path (seedgen-bench-path proj target-ns)}))
                                      target-langs)]
            (if (seq unsupported)
              (res/result {:status :error
                           :data :unsupported-bench-langs
                           :langs unsupported
                           :available available-langs})
              {:project proj
               :params params
               :test-ns test-ns
               :test-file test-file
               :target-langs target-langs
               :targets target-entries})))))))

(defn- render-bench-targets
  [test-file targets]
  (let [forms   (scaffold/read-top-level-forms test-file)
        split   (scaffold/separate-runtime-test-forms forms (mapv :lang targets))
        outputs (:by-lang split)]
    (mapv (fn [{:keys [lang ns] :as target}]
            (let [out-forms (get outputs lang)
                  out-forms (if (seq out-forms)
                              (assoc out-forms
                                     0
                                     (scaffold/replace-ns-name (first out-forms) ns))
                              [(list 'ns ns)])]
              (assoc target
                     :content (scaffold/render-top-level-forms out-forms))))
          targets)))

(defn seedgen-benchlist
  "returns the bench namespaces that should be created for a seedgen test

   (project/in-context
    (seedgen-benchlist 'xt.sample.train-001-test
                        {:lang [:js :python]}))
   => '[xtbench.js.sample.train-001-test
        xtbench.python.sample.train-001-test]"
  {:added "4.1"}
  ([ns params lookup project]
   (let [test-ns     (project/test-ns ns)
          test-file   (lookup test-ns)
          params      (task/single-function-print params)]

     (cond
       (nil? test-file)
       (res/result {:status :error
                    :data :no-test-file})

       :else
       (let [root-lang     (first (common/seedgen-root-langs test-file true))
             derived-langs (common/seedgen-root-langs test-file false)
             default-langs (vec (concat (when root-lang [root-lang]) derived-langs))]
         (cond
           (nil? root-lang)
           (res/result {:status :error
                        :data :no-seedgen-root})

           :else
           (->> (normalize-target-langs (:lang params) default-langs)
                (mapv #(seedgen-bench-ns test-ns % (:rename params))))))))))

(comment

  ;; ** seed-bench-list
  ;; should list a namespace for creation, like std.lang.manage/scaffold-runtime-template but going with the std.lang.seedgen.* pipeline

  ;; {:rename '{xt  [xtbench :lang]}}
  ;; means that xt.sample.train-001-test
  ;;    -> xtbench.js.sample.train-001-test for :js
  ;;    -> xtbench.lua.sample.train-001-test for :lua etc

  (seedgen-benchlist )
  
  
  (code.project/in-context
   (std.lang.seedgen.form-bench/seedgen-benchlist 'xt.sample.train-001-test
                                                   {:lang [:js :python]}))
  => )

(defn seedgen-benchadd
  "creates or updates bench files for the requested seedgen runtimes"
  {:added "4.1"}
  ([ns params lookup project]
   (let [resolved (resolve-bench-targets ns params lookup project)]
     (if (res/result? resolved)
       resolved
       (let [{:keys [project params test-file targets]} resolved
             rendered (render-bench-targets test-file targets)
             write?   (boolean (:write params))
             lookup'  (reduce (fn [m {:keys [ns path]}]
                                (assoc m ns path))
                              lookup
                              rendered)]
         {:outputs
          (mapv (fn [{:keys [lang ns path content]}]
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
                           :ns ns)))
                rendered)})))))


(comment

  
  ;; - look at the namespace form for [std.lang :as <ns>]
  ;; - look for the <ns>/script- form and the `:seedgen/root` tag
  ;; - return the <lang> value in  (<ns>/script- <lang>) 
  
  ;; ^{:seedgen/root     {:all true}}
  ;; (l/script- :js
  ;;            {:runtime :basic
  ;;             :require [[xt.lang.common-spec :as xt]]})
  
  
  (code.project/in-context
   (std.lang.seedgen.form-bench/seedgen-root 'xt.sample.train-001-test
                                                {}))
  => :js)


(defn seedgen-benchremove
  "removes bench files for the requested seedgen runtimes"
  {:added "4.1"}
  ([ns params lookup project]
   (let [resolved (resolve-bench-targets ns params lookup project)]
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
  
  ;; - look at the namespace form for [std.lang :as <ns>]
  ;; - look for the <ns>/script- form and the `:seedgen/root` tag
  ;; - return the <lang> value in  (<ns>/script- <lang>) 
  
  ;; ^{:seedgen/root     {:all true}}
  ;; (l/script- :js
  ;;            {:runtime :basic
  ;;             :require [[xt.lang.common-spec :as xt]]})
  
  
  (code.project/in-context
   (std.lang.seedgen.form-bench/seedgen-root 'xt.sample.train-001-test
                                                {}))
  => :js)
