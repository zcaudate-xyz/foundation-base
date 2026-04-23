(ns std.lang.seedgen.form-parse
  (:require [code.framework :as base]
             [code.framework.common :as framework.common]
             [code.project :as project]
             [std.block.navigate :as nav]
             [std.lang.seedgen.common-util :as common]
             [std.lang.seedgen.form-common :as form-common]
             [std.lib.result :as res]
             [std.task :as task]))

(defn class-empty
  []
  {:root []
   :derived []
   :scaffold []})

(defn class-explicit
  [form]
  (let [m (meta form)]
    (cond
      (:seedgen/root m) :root
      (:seedgen/derived m) :derived
      (:seedgen/scaffold m) :scaffold)))

(defn form-script?
  [script-heads form]
  (and (seq? form)
       (contains? script-heads (first form))))

(defn class-form
  [root-lang script-heads form]
  (or (class-explicit form)
      (cond
        (form-script? script-heads form)
        (if (= root-lang (common/seedgen-normalize-runtime-lang (second form)))
          :root
          :derived)

        :else
        (let [langs (common/seedgen-runtime-dispatch-langs form)]
          (cond
             (empty? langs) :scaffold
             (and root-lang
                  (= #{root-lang} (set langs))) :root
             :else :derived)))))

(defn class-navs
  [root-lang script-heads navs]
  (reduce (fn [out zloc]
            (update out
                    (class-form root-lang script-heads (nav/value zloc))
                    conj
                    (form-common/nav-entry zloc)))
          (class-empty)
          navs))

(defn class-merge
  [a b]
  (merge-with into a b))

(defn check-arrow?
  [form]
  (and (symbol? form)
       (boolean (re-find #"=>" (name form)))))

(defn check-classify
  [root-lang script-heads fact-nav]
  (let [fact-nav (form-common/nav-body fact-nav)]
    (loop [current (some-> fact-nav nav/down nav/right)
           out     (class-empty)]
      (cond
        (nil? current)
        out

        (string? (nav/value current))
        (recur (nav/right current) out)

        :else
        (let [arrow      (nav/right current)
              expected   (some-> arrow nav/right)
              expr-value (nav/value current)
              entry      (cond-> (form-common/nav-entry current)
                           (and arrow expected (check-arrow? (nav/value arrow)))
                           (assoc :check (form-common/nav-entry arrow)
                                  :expected (form-common/nav-entry expected)
                                  :langs (common/seedgen-runtime-dispatch-langs expr-value)))
              next-nav   (if (and arrow expected (check-arrow? (nav/value arrow)))
                           (nav/right expected)
                            (nav/right current))]
          (recur next-nav
                 (update out
                         (class-form root-lang script-heads expr-value)
                         conj
                         entry)))))))

(defn fact-classify-meta
  [root-lang script-heads fact-nav]
  (let [mnav      (form-common/nav-meta fact-nav)
        setup-nav (some-> mnav (form-common/nav-map-value :setup))
        tear-nav  (some-> mnav (form-common/nav-map-value :teardown))]
    {:fact-setup (class-navs root-lang script-heads (form-common/nav-vector-items setup-nav))
     :fact-teardown (class-navs root-lang script-heads (form-common/nav-vector-items tear-nav))}))

(defn fact-config-nav
  [fact-nav]
  (some-> fact-nav form-common/nav-body nav/down nav/right))

(defn global-context
  [forms top-navs]
  (let [ns-form          (first forms)
         script-heads     (common/seedgen-script-heads ns-form)
         root-script-nav  (some (fn [zloc]
                                  (let [form (nav/value zloc)]
                                    (when (and (form-script? script-heads form)
                                               (= :root (class-explicit form)))
                                      zloc)))
                                top-navs)
        root-script-form (some-> root-script-nav nav/value)
        root-lang        (some-> root-script-form second common/seedgen-normalize-runtime-lang)
        derived-script-navs
        (->> top-navs
              (filter (fn [zloc]
                        (let [form (nav/value zloc)]
                          (and (form-script? script-heads form)
                               (not= (nav/line-info zloc)
                                     (some-> root-script-nav nav/line-info))))))
              vec)
        global-fact-navs (->> top-navs
                              (filter (fn [zloc]
                                        (= 'fact:global (first (nav/value (form-common/nav-body zloc))))))
                              vec)
        global-top-navs  (->> top-navs
                              (remove (fn [zloc]
                                        (let [form (nav/value (form-common/nav-body zloc))]
                                          (or (some #{zloc} (concat [root-script-nav] derived-script-navs))
                                              (some #{zloc} global-fact-navs)
                                              (and (seq? form)
                                                   (#{'ns 'fact 'comment} (first form)))))))
                              vec)
        global-facts     (reduce (fn [out zloc]
                                   (let [mnav (fact-config-nav zloc)
                                         setup (class-navs root-lang
                                                           script-heads
                                                           (form-common/nav-vector-items
                                                            (some-> mnav (form-common/nav-map-value :setup))))
                                         teardown (class-navs root-lang
                                                              script-heads
                                                              (form-common/nav-vector-items
                                                               (some-> mnav (form-common/nav-map-value :teardown))))]
                                     (-> out
                                         (update :global-fact-setup class-merge setup)
                                         (update :global-fact-teardown class-merge teardown))))
                                 {:global-fact-setup (class-empty)
                                  :global-fact-teardown (class-empty)}
                                 global-fact-navs)]
    {:lang {:root root-lang
            :derived (mapv #(-> % nav/value second common/seedgen-normalize-runtime-lang)
                           derived-script-navs)}
     :global-script {:root (some-> root-script-nav form-common/nav-entry)
                     :derived (mapv form-common/nav-entry derived-script-navs)}
     :global-fact-setup (:global-fact-setup global-facts)
     :global-fact-teardown (:global-fact-teardown global-facts)
     :global-top (class-navs root-lang script-heads global-top-navs)}))

(defn entry-enrich
  [root-lang script-heads fact-nav entry]
  (merge entry
         (fact-classify-meta root-lang script-heads fact-nav)
         {:checks (check-classify root-lang script-heads fact-nav)}))

(defn seedgen-readforms
  "returns parsed seedgen metadata and analyse output under :entries"
  ([ns params lookup project]
   (let [test-ns   (project/test-ns ns)
         test-file (lookup test-ns)
         params    (task/single-function-print params)]
     (cond
       (nil? test-file)
       (res/result {:status :error
                    :data :no-test-file})

       :else
        (let [analysis (base/analyse-file [:test test-file])]
          (if (res/result? analysis)
            analysis
            (let [text      (slurp test-file)
                  root      (nav/parse-root text)
                  top-navs  (form-common/nav-top-levels root)
                  forms     (mapv nav/value top-navs)
                  globals   (global-context forms top-navs)
                  root-lang (get-in globals [:lang :root])
                  script-heads (common/seedgen-script-heads (first forms))
                  fact-navs (->> top-navs
                                 (keep (fn [zloc]
                                         (let [form (nav/value zloc)
                                               refer (:refer (meta form))]
                                           (when (and refer
                                                      (= 'fact (first (nav/value (form-common/nav-body zloc)))))
                                             [refer zloc]))))
                                 (into {}))
                  entries   (framework.common/entry
                            (reduce-kv (fn [out nsp vars]
                                         (assoc out
                                                nsp
                                                (reduce-kv (fn [m v entry]
                                                              (let [fact-nav (get fact-navs
                                                                                  (symbol (str nsp) (str v)))]
                                                                (assoc m
                                                                       v
                                                                       (if fact-nav
                                                                         (entry-enrich root-lang
                                                                                       script-heads
                                                                                       fact-nav
                                                                                       entry)
                                                                         entry))))
                                                            {}
                                                            vars)))
                                       {}
                                       analysis))]
             {:globals globals
              :entries entries})))))))

(comment
  (code.project/in-context
   (std.lang.seedgen.form-parse/seedgen-readforms 'xt.sample.train-002-test
                                                  {}))
  => '{:globals {}
       :entries {xt.lang.spec-base/example.B {:status ... }}})
