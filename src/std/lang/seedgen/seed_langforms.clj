(ns std.lang.seedgen.seed-langforms
  (:require [clojure.string :as str]
            [code.framework :as base]
            [code.project :as project]
            [std.block.base :as block]
            [std.block.navigate :as nav]
            [std.lang.seedgen.seed-common :as common]
            [std.lang.seedgen.seed-readforms :as readforms]
            [std.lib.result :as res]
            [std.task :as task]))

(defn- normalize-target-langs
  [lang]
  (let [target-lang (cond (= :all lang)
                          :all

                          (keyword? lang)
                          [lang]

                          (vector? lang)
                          lang

                          (seq? lang)
                          (vec lang)

                          (nil? lang)
                          nil

                          :else
                          [lang])]
    (cond (= :all target-lang)
          :all

          (nil? target-lang)
          nil

          :else
          (->> target-lang
               (map common/seedgen-normalize-runtime-lang)
               distinct
               vec))))

(defn- line-key
  [{:keys [row col end-row end-col]}]
  [row col end-row end-col])

(defn- item-form
  [item]
  (:form item))

(defn- item-line
  [item]
  (:line item))

(defn- item-string
  [item]
  (some-> item item-form block/block-string))

(defn- item-value
  [item]
  (some-> item item-form block/block-value))

(defn- item-lang
  [item]
  (some-> item item-value common/seedgen-dispatch-lang))

(defn- sort-items
  [items]
  (sort-by (comp line-key item-line) items))

(defn- parse-first-block
  [s]
  (some-> s nav/parse-root nav/down nav/block))

(defn- unwrap-meta-string
  [s]
  (let [root    (nav/parse-root s)
        current (nav/down root)]
    (if (readforms/meta-block? current)
      (-> current nav/down nav/right nav/block block/block-string)
      s)))

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

(defn- replace-dispatch-lang-string
  [expr-str lang]
  (let [root     (nav/parse-root expr-str)
        expr-nav (nav/down root)
        head-nav (some-> expr-nav nav/down)]
    (if head-nav
      (-> head-nav
          (nav/replace (symbol (str "!." (name lang))))
          nav/root-string)
      expr-str)))

(defn- indent-lines
  [prefix s]
  (str prefix
       (str/replace s "\n" (str "\n" prefix))))

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

(defn- classify-lang-items
  [classification]
  (->> classification
       vals
       (mapcat identity)
       sort-items))

(defn- runtime-item-map
  [classification]
  (->> (classify-lang-items classification)
       (keep (fn [item]
               (when-let [lang (item-lang item)]
                 [lang item])))
       (into {})))

(defn- fact-original-body-string
  [original-string]
  (unwrap-meta-string original-string))

(defn- entry-check-langs
  [entry]
  (->> (:checks entry)
       classify-lang-items
       (keep item-lang)
       distinct
       vec))

(defn- root-script-meta-langs
  [output]
  (some-> output
          (get-in [:globals :global-script :root])
          item-value
          meta
          :seedgen/root
          :langs
          normalize-target-langs))

(defn- render-fact-string-add
  [entry root-lang ordered-extra-langs target-set original-string]
  (let [root-check   (first (get-in entry [:checks :root]))]
    (if (nil? root-check)
      original-string
      (let [final-langs     (vec (cons root-lang ordered-extra-langs))
            check-items     (runtime-item-map (:checks entry))
            setup-items     (runtime-item-map (:fact-setup entry))
            teardown-items  (runtime-item-map (:fact-teardown entry))
            root-check-expr (item-string root-check)
            root-check-exp  (some-> root-check :expected item-string)
            root-setup      (some-> (get setup-items root-lang) item-string)
            root-teardown   (some-> (get teardown-items root-lang) item-string)
            indent          "  "
            clause-snippets (mapv (fn [lang]
                                    (if-let [item (get check-items lang)]
                                      (render-clause-snippet indent
                                                             (item-string item)
                                                             (some-> item :expected item-string))
                                      (render-clause-snippet indent
                                                             (replace-dispatch-lang-string
                                                              root-check-expr
                                                              lang)
                                                             root-check-exp)))
                                  final-langs)
            setup-render    (when root-setup
                              (render-vector-string
                               :setup
                               (mapv (fn [lang]
                                       (or (some-> (get setup-items lang) item-string)
                                           (when (contains? target-set lang)
                                             (replace-dispatch-lang-string root-setup lang))))
                                     final-langs)))
            teardown-render (when root-teardown
                              (render-vector-string
                               :teardown
                               (mapv (fn [lang]
                                       (or (some-> (get teardown-items lang) item-string)
                                           (when (contains? target-set lang)
                                             (replace-dispatch-lang-string root-teardown lang))))
                                     final-langs)))
            meta-string     (render-meta-string (cond-> (entry-meta entry)
                                                  setup-render (assoc :setup [])
                                                  teardown-render (assoc :teardown []))
                                               {:setup setup-render
                                                :teardown teardown-render})
            fact-body       (str "(fact " (pr-str (:intro entry))
                                 "\n"
                                 (str/join "\n\n" clause-snippets)
                                 ")")]
        (if meta-string
          (str meta-string "\n" fact-body)
          fact-body)))))

(defn- render-fact-string-remove
  [entry target-set original-string]
  (let [keep-setup     (->> (:fact-setup entry)
                            classify-lang-items
                            (remove #(contains? target-set (item-lang %)))
                            vec)
        keep-teardown  (->> (:fact-teardown entry)
                            classify-lang-items
                            (remove #(contains? target-set (item-lang %)))
                            vec)
        keep-checks    (->> (:checks entry)
                            classify-lang-items
                            (remove #(contains? target-set (item-lang %)))
                            vec)
        setup-render   (render-vector-string :setup (mapv item-string keep-setup))
        teardown-render (render-vector-string :teardown (mapv item-string keep-teardown))
        meta-string    (render-meta-string (cond-> (entry-meta entry)
                                             setup-render (assoc :setup [])
                                             teardown-render (assoc :teardown []))
                                           {:setup setup-render
                                            :teardown teardown-render})
        fact-body      (if (seq keep-checks)
                         (str "(fact " (pr-str (:intro entry))
                              "\n"
                              (str/join "\n\n"
                                        (map (fn [item]
                                               (render-clause-snippet "  "
                                                                      (item-string item)
                                                                      (some-> item :expected item-string)))
                                             keep-checks))
                              ")")
                         (fact-original-body-string original-string))]
    (if meta-string
      (str meta-string "\n" fact-body)
      fact-body)))

(defn- render-global-fact-remove
  [output target-set original-string]
  (let [keep-setup     (->> (get-in output [:globals :global-fact-setup])
                            classify-lang-items
                            (remove #(contains? target-set (item-lang %)))
                            vec)
        keep-teardown  (->> (get-in output [:globals :global-fact-teardown])
                            classify-lang-items
                            (remove #(contains? target-set (item-lang %)))
                            vec)
        setup-render   (render-vector-string :setup (mapv item-string keep-setup))
        teardown-render (render-vector-string :teardown (mapv item-string keep-teardown))]
    (str "(fact:global\n "
         (render-map-string {:setup setup-render
                             :teardown teardown-render})
         ")")))

(defn- update-root-script-string
  [output]
  (let [root-entry  (get-in output [:globals :global-script :root])
        root-form   (some-> root-entry item-value)
        current-str (item-string root-entry)
        known-langs (->> (concat (or (root-script-meta-langs output) [])
                                 (get-in output [:globals :lang :derived]))
                         distinct
                         vec)]
    (if (empty? known-langs)
      current-str
      (str "^{:seedgen/root "
           (pr-str (assoc (:seedgen/root (meta root-form))
                          :langs known-langs))
           "}\n"
           (unwrap-meta-string current-str)))))

(defn- script-string-map
  [output]
  (->> (get-in output [:globals :global-script :derived])
       (keep (fn [item]
               (let [lang (some-> item item-value second common/seedgen-normalize-runtime-lang)]
                 (when lang
                   [lang (item-string item)]))))
       (into {})))

(defn- root-script-body-string
  [output]
  (some-> output
          (get-in [:globals :global-script :root])
          item-string
          unwrap-meta-string))

(defn- render-top-level-add
  [output text target-lang]
  (let [root-entry       (get-in output [:globals :global-script :root])
        root-lang        (get-in output [:globals :lang :root])
        stored-langs     (or (root-script-meta-langs output) target-lang [])
        current-langs    (set (get-in output [:globals :lang :derived]))
        target-set       (set (or target-lang stored-langs []))
        existing-scripts (script-string-map output)
        fact-entries     (->> (get output :entries)
                              vals
                              (mapcat vals))
        fact-by-refer    (into {}
                               (map (fn [entry]
                                      [(symbol (str (:ns entry)) (str (:var entry))) entry]))
                               fact-entries)
        root-script-line (some-> root-entry item-line line-key)
        derived-lines    (->> (get-in output [:globals :global-script :derived])
                              (map item-line)
                              (map line-key)
                              set)
        ordered-scripts  (->> stored-langs
                              (filter #(or (contains? current-langs %)
                                           (contains? target-set %)))
                              vec)
        add-script-strs  (mapv #(or (get existing-scripts %)
                                    (replace-script-lang-string
                                     (root-script-body-string output)
                                     %))
                               ordered-scripts)
        root            (nav/parse-root text)
        top-navs        (readforms/top-level-navs root)]
    (str (str/join
          "\n\n"
          (mapcat (fn [zloc]
                    (let [line     (line-key (nav/line-info zloc))
                          current  (block/block-string (nav/block zloc))
                          form     (nav/value zloc)
                          refer    (:refer (meta form))]
                      (cond
                        (= line root-script-line)
                        (into [current] add-script-strs)

                        (contains? derived-lines line)
                        []

                        (and (seq? form)
                             (= 'fact (first (nav/value (readforms/body-nav zloc))))
                             refer
                             (contains? fact-by-refer refer))
                        [(render-fact-string-add (get fact-by-refer refer)
                                                 root-lang
                                                 ordered-scripts
                                                 target-set
                                                 current)]

                        :else
                        [current])))
                  top-navs))
         "\n")))

(defn- render-top-level-remove
  [output text target-lang]
  (let [root-entry       (get-in output [:globals :global-script :root])
        root-lang        (get-in output [:globals :lang :root])
        purge-langs      (get-in output [:globals :lang :derived])
        target-lang      (if (= :all target-lang)
                           purge-langs
                           target-lang)
        target-set       (set target-lang)
        fact-entries     (->> (get output :entries)
                              vals
                              (mapcat vals))
        fact-by-refer    (into {}
                               (map (fn [entry]
                                      [(symbol (str (:ns entry)) (str (:var entry))) entry]))
                               fact-entries)
        root-script-line (some-> root-entry item-line line-key)
        derived-line->lang
        (->> (get-in output [:globals :global-script :derived])
             (keep (fn [item]
                     (let [lang (some-> item item-value second common/seedgen-normalize-runtime-lang)]
                       (when lang
                         [(line-key (item-line item)) lang]))))
             (into {}))
        root            (nav/parse-root text)
        top-navs        (readforms/top-level-navs root)]
    (str (str/join
          "\n\n"
          (mapcat (fn [zloc]
                    (let [line    (line-key (nav/line-info zloc))
                          current (block/block-string (nav/block zloc))
                          body    (readforms/body-nav zloc)
                          form    (nav/value zloc)
                          refer   (:refer (meta form))
                          head    (when (seq? (nav/value body))
                                    (first (nav/value body)))]
                      (cond
                        (= line root-script-line)
                        [(update-root-script-string output)]

                        (contains? derived-line->lang line)
                        (if (contains? target-set (get derived-line->lang line))
                          []
                          [current])

                        (= 'fact:global head)
                        [(render-global-fact-remove output target-set current)]

                        (and (= 'fact head)
                             refer
                             (contains? fact-by-refer refer))
                        [(render-fact-string-remove (get fact-by-refer refer)
                                                    target-set
                                                    current)]

                        :else
                        [current])))
                  top-navs))
         "\n")))

(defn seedgen-addlang
  {:added "4.1"}
  ([ns params lookup project]
   (let [test-ns   (project/test-ns ns)
         test-file (lookup test-ns)
         params    (task/single-function-print params)]
     (cond
       (nil? test-file)
       (res/result {:status :error
                    :data :no-test-file})

       :else
       (let [output      (readforms/seedgen-readforms ns {} lookup project)
             root-lang   (get-in output [:globals :lang :root])
             stored-langs (or (root-script-meta-langs output)
                              [])
             target-lang (or (normalize-target-langs (:lang params))
                             stored-langs
                             [])]
         (cond
           (res/result? output)
           output

           (nil? root-lang)
           (res/result {:status :error
                        :data :no-seedgen-root})

           (some #{root-lang} target-lang)
           (res/result {:status :error
                        :data :cannot-add-root
                        :lang root-lang})

           :else
           (base/transform-code test-ns
                                (-> params
                                    (assoc :transform #(render-top-level-add output % target-lang)
                                           :no-analysis true)
                                    (dissoc :lang))
                                lookup
                                project)))))))

(defn seedgen-removelang
  {:added "4.1"}
  ([ns params lookup project]
   (let [test-ns   (project/test-ns ns)
         test-file (lookup test-ns)
         params    (task/single-function-print params)]
     (cond
       (nil? test-file)
       (res/result {:status :error
                    :data :no-test-file})

       :else
       (let [output      (readforms/seedgen-readforms ns {} lookup project)
             root-lang   (get-in output [:globals :lang :root])
             purge-langs (get-in output [:globals :lang :derived])
             target-lang (or (normalize-target-langs (:lang params))
                             :all)
             target-lang (if (= :all target-lang)
                           purge-langs
                           target-lang)]
         (cond
           (res/result? output)
           output

           (nil? root-lang)
           (res/result {:status :error
                        :data :no-seedgen-root})

           (some #{root-lang} target-lang)
           (res/result {:status :error
                        :data :cannot-purge-root
                        :lang root-lang})

           :else
           (base/transform-code test-ns
                                (-> params
                                    (assoc :transform #(render-top-level-remove output % target-lang)
                                           :no-analysis true)
                                    (dissoc :lang))
                                lookup
                                project)))))))
