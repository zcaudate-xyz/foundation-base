(ns std.lang.seedgen.seed-readforms
  (:require [code.framework :as base]
            [code.framework.common :as framework.common]
            [code.project :as project]
            [std.block.base :as block]
            [std.block.navigate :as nav]
            [std.lang.seedgen.seed-common :as common]
            [std.lib.result :as res]
            [std.task :as task]))

(defn empty-classification
  []
  {:root []
   :derived []
   :scaffold []})

(defn explicit-class
  [form]
  (let [m (meta form)]
    (cond
      (:seedgen/root m) :root
      (:seedgen/derived m) :derived
      (:seedgen/scaffold m) :scaffold)))

(defn script-form?
  [script-heads form]
  (and (seq? form)
       (contains? script-heads (first form))))

(defn form-class
  [root-lang script-heads form]
  (or (explicit-class form)
      (cond
        (script-form? script-heads form)
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

(defn meta-block?
  [zloc]
  (= :meta (block/block-tag (nav/block zloc))))

(defn meta-nav
  [zloc]
  (when (meta-block? zloc)
    (nav/down zloc)))

(defn body-nav
  [zloc]
  (if (meta-block? zloc)
    (-> zloc nav/down nav/right)
    zloc))

(defn top-level-navs
  [root]
  (loop [current (nav/down root)
         out     []]
    (if (nil? current)
      out
      (recur (nav/right current) (conj out current)))))

(defn nav-entry
  [zloc]
  {:form (nav/block zloc)
   :line (nav/line-info zloc)})

(defn map-value-nav
  [map-nav target-key]
  (loop [current (some-> map-nav nav/down)]
    (cond
      (nil? current)
      nil

      (= target-key (nav/value current))
      (nav/right current)

      :else
      (recur (some-> current nav/right nav/right)))))

(defn vector-item-navs
  [vector-nav]
  (loop [current (some-> vector-nav nav/down)
         out     []]
    (if (nil? current)
      out
      (recur (nav/right current) (conj out current)))))

(defn classify-navs
  [root-lang script-heads navs]
  (reduce (fn [out zloc]
            (update out
                    (form-class root-lang script-heads (nav/value zloc))
                    conj
                    (nav-entry zloc)))
          (empty-classification)
          navs))

(defn merge-classifications
  [a b]
  (merge-with into a b))

(defn check-arrow?
  [form]
  (and (symbol? form)
       (boolean (re-find #"=>" (name form)))))

(defn classify-checks
  [root-lang script-heads fact-nav]
  (let [fact-nav (body-nav fact-nav)]
    (loop [current (some-> fact-nav nav/down nav/right)
           out     (empty-classification)]
      (cond
        (nil? current)
        out

        (string? (nav/value current))
        (recur (nav/right current) out)

        :else
        (let [arrow      (nav/right current)
              expected   (some-> arrow nav/right)
              expr-value (nav/value current)
              entry      (cond-> (nav-entry current)
                           (and arrow expected (check-arrow? (nav/value arrow)))
                           (assoc :check (nav-entry arrow)
                                  :expected (nav-entry expected)
                                  :langs (common/seedgen-runtime-dispatch-langs expr-value)))
              next-nav   (if (and arrow expected (check-arrow? (nav/value arrow)))
                           (nav/right expected)
                           (nav/right current))]
          (recur next-nav
                 (update out
                         (form-class root-lang script-heads expr-value)
                         conj
                         entry)))))))

(defn fact-meta-classification
  [root-lang script-heads fact-nav]
  (let [mnav      (meta-nav fact-nav)
        setup-nav (some-> mnav (map-value-nav :setup))
        tear-nav  (some-> mnav (map-value-nav :teardown))]
    {:fact-setup (classify-navs root-lang script-heads (vector-item-navs setup-nav))
     :fact-teardown (classify-navs root-lang script-heads (vector-item-navs tear-nav))}))

(defn fact-config-map-nav
  [fact-nav]
  (some-> fact-nav body-nav nav/down nav/right))

(defn globals-context
  [forms top-navs]
  (let [ns-form          (first forms)
        script-heads     (common/seedgen-script-heads ns-form)
        root-script-nav  (some (fn [zloc]
                                 (let [form (nav/value zloc)]
                                   (when (and (script-form? script-heads form)
                                              (= :root (explicit-class form)))
                                     zloc)))
                               top-navs)
        root-script-form (some-> root-script-nav nav/value)
        root-lang        (some-> root-script-form second common/seedgen-normalize-runtime-lang)
        derived-script-navs
        (->> top-navs
             (filter (fn [zloc]
                       (let [form (nav/value zloc)]
                         (and (script-form? script-heads form)
                              (not= (nav/line-info zloc)
                                    (some-> root-script-nav nav/line-info))))))
             vec)
        global-fact-navs (->> top-navs
                              (filter (fn [zloc]
                                        (= 'fact:global (first (nav/value (body-nav zloc))))))
                              vec)
        global-top-navs  (->> top-navs
                              (remove (fn [zloc]
                                        (let [form (nav/value (body-nav zloc))]
                                          (or (some #{zloc} (concat [root-script-nav] derived-script-navs))
                                              (some #{zloc} global-fact-navs)
                                              (and (seq? form)
                                                   (#{'ns 'fact 'comment} (first form)))))))
                              vec)
        global-facts     (reduce (fn [out zloc]
                                   (let [mnav (fact-config-map-nav zloc)
                                         setup (classify-navs root-lang
                                                              script-heads
                                                              (vector-item-navs
                                                               (some-> mnav (map-value-nav :setup))))
                                         teardown (classify-navs root-lang
                                                                 script-heads
                                                                 (vector-item-navs
                                                                  (some-> mnav (map-value-nav :teardown))))]
                                     (-> out
                                         (update :global-fact-setup merge-classifications setup)
                                         (update :global-fact-teardown merge-classifications teardown))))
                                 {:global-fact-setup (empty-classification)
                                  :global-fact-teardown (empty-classification)}
                                 global-fact-navs)]
    {:lang {:root root-lang
            :derived (mapv #(-> % nav/value second common/seedgen-normalize-runtime-lang)
                           derived-script-navs)}
     :global-script {:root (some-> root-script-nav nav-entry)
                     :derived (mapv nav-entry derived-script-navs)}
     :global-fact-setup (:global-fact-setup global-facts)
     :global-fact-teardown (:global-fact-teardown global-facts)
     :global-top (classify-navs root-lang script-heads global-top-navs)}))

(defn enrich-entry
  [root-lang script-heads fact-nav entry]
  (merge entry
         (fact-meta-classification root-lang script-heads fact-nav)
         {:checks (classify-checks root-lang script-heads fact-nav)}))

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
                 forms     (mapv nav/value (top-level-navs root))
                 top-navs  (top-level-navs root)
                 globals   (globals-context forms top-navs)
                 root-lang (get-in globals [:lang :root])
                 script-heads (common/seedgen-script-heads (first forms))
                 fact-navs (->> top-navs
                                (keep (fn [zloc]
                                        (let [form (nav/value zloc)
                                              refer (:refer (meta form))]
                                          (when (and refer
                                                     (= 'fact (first (nav/value (body-nav zloc)))))
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
                                                                        (enrich-entry root-lang
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
   (std.lang.seedgen.seed-readforms/seedgen-readforms <sample>
                                                      {}))
  => '{:globals {}
       :entries {xt.lang.common-spec/example.B {:status ... }}})
