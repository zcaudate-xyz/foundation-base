(ns std.lang.seedgen.form-bench
  (:require [clojure.string :as str]
             [code.framework :as base]
             [code.project :as project]
             [std.fs :as fs]
             [std.block.base :as block]
             [std.block.navigate :as nav]
             [std.lang.seedgen.form-common :as form-common]
             [std.lang.seedgen.form-parse :as readforms]
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
  [project target-ns]
  (str (fs/path (:root project)
                (or (first (:test-paths project))
                    "test")
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
          (let [target-langs    (form-common/target-normalize-langs (:lang params) available-langs)
                unsupported     (->> target-langs
                                     (remove (set available-langs))
                                     distinct
                                     vec)
                target-entries  (mapv (fn [lang]
                                        (let [target-ns (bench-target-ns test-ns
                                                                         lang
                                                                         (:rename params))]
                                          {:lang lang
                                           :ns target-ns
                                            :path (bench-target-path proj target-ns)}))
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
                :output output
                :target-langs target-langs
                :targets target-entries})))))))

(defn- unwrap-meta-string
  [s]
  (let [root    (nav/parse-root s)
        current (nav/down root)]
    (if (form-common/nav-meta-block? current)
      (-> current nav/down nav/right nav/block block/block-string)
      s)))

(defn- replace-ns-name-string
  [ns-str new-ns]
  (let [root   (nav/parse-root ns-str)
        ns-nav (some-> root nav/down)
        name-nav (some-> ns-nav nav/down nav/right)]
    (if name-nav
      (-> name-nav
          (nav/replace new-ns)
          nav/root-string)
      ns-str)))

(defn- replace-script-lang-string
  [script-str lang]
  (let [root       (nav/parse-root script-str)
        script-nav (nav/down root)
        lang-nav   (some-> script-nav nav/down nav/right)]
    (if lang-nav
      (-> lang-nav
          (nav/replace lang)
          nav/root-string)
      script-str)))

(defn- indent-lines
  [prefix s]
  (str prefix
       (str/replace s "\n" (str "\n" prefix))))

(defn- leading-indent
  [^String line]
  (loop [i 0]
    (if (and (< i (count line))
             (#{\space \tab} (.charAt line i)))
      (recur (inc i))
      i)))

(defn- trim-indent
  [^String line n]
  (let [limit (min n (leading-indent line))]
    (subs line limit)))

(defn- normalise-block-string
  [^String s indent]
  (let [[head & rest] (str/split-lines s)]
    (if (empty? rest)
      s
      (str/join "\n"
                (cons head
                      (map (fn [line]
                             (if (str/blank? line)
                               line
                               (trim-indent line indent)))
                           rest))))))

(defn- render-item-string
  [item]
  (let [col (or (get-in item [:line :col]) 1)]
    (some-> item
            form-common/item-string
            (normalise-block-string (max 0 (dec col))))))

(defn- render-clause-snippet
  [indent expr-str expected-str]
  (str (indent-lines indent expr-str)
       "\n"
       indent
       "=> "
       (str/replace expected-str "\n" (str "\n" indent "   "))))

(defn- render-vector-string
  [key snippets]
  (let [items        (vec (remove nil? snippets))
        prefix-count (+ 2 (count (str key)) 2)
        prefix       (apply str (repeat prefix-count \space))]
    (cond
      (empty? items)
      nil

      (= 1 (count items))
      (str "[" (first items) "]")

      :else
      (str "[" (first items)
           "\n"
           (str/join "\n" (map #(indent-lines prefix %) (rest items)))
           "]"))))

(defn- render-meta-string
  [m vector-values]
  (let [preferred-order [:refer :added :setup :teardown]
        ordered-keys    (concat (filter #(contains? m %) preferred-order)
                                (remove (set preferred-order) (keys m)))
        entries         (reduce (fn [out k]
                                  (let [v        (get m k)
                                        rendered (if (contains? vector-values k)
                                                   (get vector-values k)
                                                   (pr-str v))]
                                    (cond-> out
                                      rendered
                                      (conj [k rendered]))))
                                []
                                ordered-keys)]
    (when (seq entries)
      (str "^{"
           (loop [[[k rendered] & more] entries
                  out nil]
             (let [piece (str k " " rendered)
                   out   (if out
                           (str out
                                (if (str/starts-with? rendered "[")
                                  "\n  "
                                  " ")
                                piece)
                           piece)]
               (if more
                 (recur more out)
                 out)))
           "}"))))

(defn- render-map-string
  [vector-values]
  (let [ordered-keys [:setup :teardown]
        entries      (keep (fn [k]
                             (when-let [rendered (get vector-values k)]
                               [k rendered]))
                           ordered-keys)]
    (if (empty? entries)
      "{}"
      (str "{"
           (loop [[[k rendered] & more] entries
                  out nil]
             (let [piece (str k " " rendered)
                   out   (if out
                           (str out
                                (if (str/starts-with? rendered "[")
                                  "\n "
                                  " ")
                                piece)
                           piece)]
               (if more
                 (recur more out)
                 out)))
           "}"))))

(defn- entry-meta
  [entry]
  (assoc (:meta entry)
         :refer (symbol (str (:ns entry)) (str (:var entry)))))

(defn- root-script-body-string
  [output]
  (some-> output
          (get-in [:globals :global-script :root])
          form-common/item-string
          unwrap-meta-string))

(defn- target-script-string
  [output lang]
  (let [root-lang  (get-in output [:globals :lang :root])
        root-entry (get-in output [:globals :global-script :root])
        derived    (->> (get-in output [:globals :global-script :derived])
                        (keep (fn [item]
                                (let [item-lang (some-> item
                                                        form-common/item-value
                                                        second
                                                        form-common/target-normalize-langs
                                                        first)]
                                  (when item-lang
                                    [item-lang item]))))
                        (into {}))]
    (cond
      (= lang root-lang)
      (some-> root-entry form-common/item-string)

      (get derived lang)
      (some-> (get derived lang) form-common/item-string)

      :else
      (some-> (root-script-body-string output)
              (replace-script-lang-string lang)))))

(defn- keep-target-items
  [classification lang]
  (->> (form-common/item-classify-langs classification)
       (filter (fn [item]
                 (let [item-lang (form-common/item-lang item)]
                   (or (nil? item-lang)
                       (= lang item-lang)))))
       vec))

(defn- render-fact-string-bench
  [entry lang]
  (let [check-items      (keep-target-items (:checks entry) lang)
        setup-items      (keep-target-items (:fact-setup entry) lang)
        teardown-items   (keep-target-items (:fact-teardown entry) lang)
        setup-render     (render-vector-string :setup (mapv render-item-string setup-items))
        teardown-render  (render-vector-string :teardown (mapv render-item-string teardown-items))]
    (when (seq check-items)
      (let [meta-string (render-meta-string (cond-> (entry-meta entry)
                                              setup-render (assoc :setup [])
                                              teardown-render (assoc :teardown []))
                                            {:setup setup-render
                                             :teardown teardown-render})
            fact-body   (str "(fact " (pr-str (:intro entry))
                             "\n\n"
                             (str/join "\n\n"
                                       (map (fn [item]
                                              (render-clause-snippet "  "
                                                                     (render-item-string item)
                                                                     (some-> item :expected render-item-string)))
                                            check-items))
                             ")")]
        (if meta-string
          (str meta-string "\n" fact-body)
          fact-body)))))

(defn- render-global-fact-bench
  [output lang]
  (let [setup-items      (keep-target-items (get-in output [:globals :global-fact-setup]) lang)
        teardown-items   (keep-target-items (get-in output [:globals :global-fact-teardown]) lang)
        setup-render     (render-vector-string :setup (mapv render-item-string setup-items))
        teardown-render  (render-vector-string :teardown (mapv render-item-string teardown-items))]
    (when (or setup-render teardown-render)
      (str "(fact:global\n "
           (render-map-string {:setup setup-render
                               :teardown teardown-render})
           ")"))))

(defn- render-global-top-bench
  [output lang]
  (->> (keep-target-items (get-in output [:globals :global-top]) lang)
       (map (fn [item]
              [(form-common/item-line-key (form-common/item-line item))
               (form-common/item-string item)]))
       (into {})))

(defn- bench-render-target
  [output text {:keys [lang ns] :as target}]
  (let [root             (nav/parse-root text)
        top-navs         (form-common/nav-top-levels root)
        root-entry       (get-in output [:globals :global-script :root])
        root-script-line (some-> root-entry form-common/item-line form-common/item-line-key)
        derived-lines    (->> (get-in output [:globals :global-script :derived])
                              (map form-common/item-line)
                              (map form-common/item-line-key)
                              set)
        fact-entries     (->> (get output :entries)
                              vals
                              (mapcat vals))
        fact-by-refer    (into {}
                               (map (fn [entry]
                                      [(symbol (str (:ns entry)) (str (:var entry))) entry]))
                               fact-entries)
        global-top-map   (render-global-top-bench output lang)
        script-string    (target-script-string output lang)
        content          (str
                          (str/join
                           "\n\n"
                           (keep (fn [zloc]
                                   (let [line    (form-common/item-line-key (nav/line-info zloc))
                                         current (block/block-string (nav/block zloc))
                                         body    (form-common/nav-body zloc)
                                         form    (nav/value zloc)
                                         head    (when (seq? (nav/value body))
                                                   (first (nav/value body)))
                                         refer   (:refer (meta form))]
                                     (cond
                                       (and (seq? form) (= 'ns (first form)))
                                       (replace-ns-name-string current ns)

                                       (= line root-script-line)
                                       script-string

                                       (contains? derived-lines line)
                                       nil

                                       (= 'fact:global head)
                                       (render-global-fact-bench output lang)

                                       (and (= 'fact head)
                                            refer
                                            (contains? fact-by-refer refer))
                                       (render-fact-string-bench (get fact-by-refer refer) lang)

                                       (contains? global-top-map line)
                                       (get global-top-map line)

                                       :else
                                       current)))
                                 top-navs))
                          "\n")]
    (assoc target :content content)))

(defn- bench-render-targets
  [output test-file targets]
  (let [text (slurp test-file)]
    (mapv #(bench-render-target output text %)
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
   (let [resolved (bench-resolve-targets ns params lookup project)]
     (if (res/result? resolved)
       resolved
       (->> resolved :targets (mapv :ns))))))

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
