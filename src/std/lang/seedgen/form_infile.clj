(ns std.lang.seedgen.form-infile
  (:require [clojure.string :as str]
             [code.query :as query]
             [code.framework :as base]
             [code.project :as project]
             [std.block.base :as block]
             [std.block.navigate :as nav]
             [std.lang.seedgen.common-util :as common]
             [std.lang.seedgen.form-common :as form-common]
             [std.lang.seedgen.form-parse :as seed-readforms]
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
    (if (form-common/nav-meta-block? current)
      (-> current nav/down nav/right nav/block block/block-string)
      s)))

(defn- strip-seedgen-control-meta-string
  [s]
  (let [root    (nav/parse-root s)
        current (nav/down root)]
    (if-let [mnav (some-> current form-common/nav-meta)]
      (let [m      (nav/value mnav)
            next-m (dissoc m :seedgen/lang :seedgen/base :seedgen/check)]
        (cond
          (= m next-m)
          s

          (empty? next-m)
          (-> current form-common/nav-body nav/block block/block-string)

          :else
          (-> mnav
              (nav/replace next-m)
              nav/root-string)))
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

(defn- replace-ns-name-string
  [ns-str new-ns]
  (let [root     (nav/parse-root ns-str)
        ns-nav   (some-> root nav/down)
        name-nav (some-> ns-nav nav/down nav/right)]
    (if name-nav
      (-> name-nav
          (nav/replace new-ns)
          nav/root-string)
      ns-str)))

(defn- replace-runtime-lang-content-string
  [expr-str expr-form lang]
  (let [target-lang (common/seedgen-normalize-runtime-lang lang)
        target-tag  (common/seedgen-dispatch-tag target-lang)
        langs       (common/seedgen-runtime-dispatch-langs expr-form)
        replace-lang-token
        (fn [s src]
          (let [src-lang (common/seedgen-normalize-runtime-lang src)
                src-tag  (common/seedgen-dispatch-tag src-lang)
                out      (str/replace s
                                      (re-pattern (str "!" "\\." (java.util.regex.Pattern/quote (name src-tag)) "(?![A-Za-z0-9_\\-])"))
                                      (str "!." (name target-tag)))]
            (-> out
                (str/replace (re-pattern (str ":" (java.util.regex.Pattern/quote (name src-lang)) "(?![A-Za-z0-9_\\-])"))
                             (str ":" (name target-lang)))
                (str/replace (re-pattern (str ":" (java.util.regex.Pattern/quote (name src-tag)) "(?![A-Za-z0-9_\\-])"))
                             (str ":" (name target-lang))))))]
    (reduce replace-lang-token expr-str langs)))

(defn- replace-runtime-lang-string
  [expr-str lang]
  (let [expr-str (strip-seedgen-control-meta-string expr-str)
        root     (nav/parse-root expr-str)
        expr-nav (nav/down root)
        form-nav (some-> expr-nav form-common/nav-body)
        head-nav (some-> form-nav nav/down)
        arg-nav   (some-> head-nav nav/right)
        expr-form (some-> form-nav nav/value)
        arg-form  (some-> arg-nav nav/value)
        tag       (common/seedgen-dispatch-tag lang)]
    (cond
      (and head-nav
           (common/seedgen-dispatch-lang expr-form))
      (-> head-nav
          (nav/replace (symbol (str "!." (name tag))))
          nav/root-string)

      (and arg-nav
           (common/seedgen-runtime-reference-lang expr-form)
           (keyword? arg-form))
      (-> arg-nav
          (nav/replace lang)
          nav/root-string)

      (and arg-nav
           (common/seedgen-runtime-reference-lang expr-form)
           (vector? arg-form)
           (seq arg-form))
      (if-let [first-arg-nav (some-> arg-nav nav/down)]
        (-> first-arg-nav
            (nav/replace lang)
            nav/root-string)
        expr-str)

      :else
      (replace-runtime-lang-content-string expr-str expr-form lang))))

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
            item-string
            (normalise-block-string (max 0 (dec col))))))

(defn- render-clause-snippet
  [indent expr-str expected-str]
  (str (indent-lines indent expr-str)
       (when expected-str
         (str "\n"
              indent
              "=> "
              (str/replace expected-str
                           "\n"
                           (str "\n" indent "   "))))))

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

(defn- item-suppressed?
  [item lang]
  (true? (:suppress (common/seedgen-lang-entry (item-value item) lang))))

(defn- item-base-override
  [item lang key]
  (get (common/seedgen-lang-entry (item-value item) lang)
       key))

(defn- transform-string-match?
  [match]
  (or (instance? Character match)
      (instance? CharSequence match)
      (instance? java.util.regex.Pattern match)))

(defn- transform-literal
  [form]
  (if (and (seq? form)
           (= 'quote (first form))
           (= 2 (count form)))
    (second form)
    form))

(defn- apply-item-transform-form
  [s from to]
  (let [root     (nav/parse-root s)
        expr-nav (nav/down root)
        body-nav (some-> expr-nav form-common/nav-body)]
    (if body-nav
      (-> (query/modify body-nav
                        [{:is from}]
                        (fn [zloc]
                          (nav/replace zloc to)))
          nav/root-string)
      s)))

(defn- apply-item-transform-string
  [s item lang]
  (let [transform-override (item-base-override item lang :transform)]
    (if (and s (map? transform-override))
      (reduce-kv (fn [out from to]
                   (let [from (transform-literal from)
                         to   (transform-literal to)]
                     (if (transform-string-match? from)
                       (str/replace out from to)
                       (apply-item-transform-form out from to))))
                  s
                  transform-override)
      s)))

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

(defn root-script-meta-langs
  [output]
  (some-> output
          (get-in [:globals :global-script :root])
          item-value
          meta
          :seedgen/root
          :langs
          normalize-target-langs))

(defn- keep-target-items
  [classification lang]
  (->> (form-common/item-classify-langs classification)
       (filter (fn [item]
                 (let [item-lang (item-lang item)]
                   (or (nil? item-lang)
                       (= lang item-lang)))))
       vec))

(defn- render-check-clause
  [item]
  (render-clause-snippet "  "
                         (render-item-string item)
                         (some-> item :expected render-item-string)))

(defn- render-generated-item-string
  [root-item lang]
  (let [input-override (item-base-override root-item lang :input)
        generated-str  (if input-override
                         (let [base-str  (replace-runtime-lang-string
                                          (render-item-string root-item)
                                          lang)
                               root      (nav/parse-root base-str)
                               expr-nav  (some-> root nav/down form-common/nav-body)
                               expr-form (some-> expr-nav nav/value)]
                           (cond
                             (common/seedgen-dispatch-lang input-override)
                             (strip-seedgen-control-meta-string
                              (pr-str input-override))

                             (common/seedgen-dispatch-lang expr-form)
                             (if-let [body-nav (some-> expr-nav nav/down nav/right)]
                               (-> body-nav
                                   (nav/replace input-override)
                                   nav/root-string)
                               base-str)

                             :else
                             (strip-seedgen-control-meta-string
                              (pr-str input-override))))
                         (replace-runtime-lang-string
                          (render-item-string root-item)
                          lang))]
    (apply-item-transform-string generated-str root-item lang)))

(defn- render-generated-check-clause
  [root-item lang]
  (let [expr-str        (render-generated-item-string root-item lang)
        expect-override (item-base-override root-item lang :expect)
        expected-str    (if (nil? expect-override)
                          (some-> root-item :expected render-item-string)
                          (pr-str expect-override))]
    (render-clause-snippet "  " expr-str expected-str)))

(declare render-target-runtime-item)

(defn- render-target-runtime-items
  "Renders setup/teardown items for the requested languages while preserving
   scaffold-only forms in their original position."
  [classification root-lang langs]
  (:rendered-items
   (reduce (fn [{:keys [rendered-items processed-langs]} item]
             (if-let [current-lang (item-lang item)]
               (cond
                 (contains? processed-langs current-lang)
                 {:rendered-items rendered-items
                  :processed-langs processed-langs}

                 (= current-lang root-lang)
                 (let [rendered (->> langs
                                     (remove processed-langs)
                                     (keep (fn [lang]
                                             (when-let [snippet (render-target-runtime-item classification
                                                                                            root-lang
                                                                                            lang)]
                                               [lang snippet])))
                                     vec)]
                   {:rendered-items (into rendered-items (map second rendered))
                    :processed-langs (into processed-langs (map first rendered))})

                 (some #{current-lang} langs)
                 {:rendered-items (conj rendered-items (render-item-string item))
                  :processed-langs (conj processed-langs current-lang)}

                 :else
                 {:rendered-items rendered-items
                  :processed-langs processed-langs})
               ;; Preserve scaffold-only forms that aren't language-specific.
               {:rendered-items (conj rendered-items (render-item-string item))
                :processed-langs processed-langs}))
           {:rendered-items []
            :processed-langs #{}}
           (classify-lang-items classification))))

(defn- render-check-snippets-add
  [entry ordered-extra-langs target-set]
  (let [root-checks     (vec (sort-items (get-in entry [:checks :root])))
        derived-by-lang (->> (get-in entry [:checks :derived])
                             sort-items
                             (group-by item-lang))
        root-snippets   (mapv render-check-clause root-checks)
        extra-snippets
        (mapcat (fn [lang]
                  (let [existing       (vec (get derived-by-lang lang))
                        eligible-roots (vec (remove #(item-suppressed? % lang) root-checks))
                        generated      (map-indexed
                                        (fn [idx root-item]
                                          (if-let [item (nth existing idx nil)]
                                            (render-check-clause item)
                                            (when (contains? target-set lang)
                                              (render-generated-check-clause
                                               root-item
                                               lang))))
                                        eligible-roots)
                        trailing       (map render-check-clause
                                            (drop (count eligible-roots) existing))]
                    (concat (remove nil? generated)
                            trailing)))
                ordered-extra-langs)]
    (vec (concat root-snippets extra-snippets))))

(defn- render-fact-string-add
  [entry root-lang ordered-extra-langs target-set original-string]
  (let [root-checks (vec (sort-items (get-in entry [:checks :root])))]
    (if (empty? root-checks)
        original-string
       (let [final-langs     (vec (cons root-lang ordered-extra-langs))
               root-check      (first root-checks)
               check-snippets  (render-check-snippets-add entry ordered-extra-langs target-set)
               setup-render    (let [items (render-target-runtime-items (:fact-setup entry)
                                                                       root-lang
                                                                       final-langs)]
                                 (when (seq items)
                                   (render-vector-string :setup items)))
              teardown-render (let [items (render-target-runtime-items (:fact-teardown entry)
                                                                      root-lang
                                                                      final-langs)]
                                (when (seq items)
                                  (render-vector-string :teardown items)))
             meta-string     (render-meta-string (cond-> (entry-meta entry)
                                                  setup-render (assoc :setup [])
                                                  teardown-render (assoc :teardown []))
                                                 {:setup setup-render
                                                  :teardown teardown-render})
              fact-body       (str "(fact " (pr-str (:intro entry))
                                   "\n\n"
                                   (str/join "\n\n" check-snippets)
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
        setup-render   (render-vector-string :setup (mapv render-item-string keep-setup))
        teardown-render (render-vector-string :teardown (mapv render-item-string keep-teardown))
        meta-string    (render-meta-string (cond-> (entry-meta entry)
                                             setup-render (assoc :setup [])
                                             teardown-render (assoc :teardown []))
                                            {:setup setup-render
                                             :teardown teardown-render})
        fact-body      (if (seq keep-checks)
                         (str "(fact " (pr-str (:intro entry))
                               "\n\n"
                               (str/join "\n\n"
                                         (map (fn [item]
                                                (render-clause-snippet "  "
                                                                       (render-item-string item)
                                                                       (some-> item :expected render-item-string)))
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
        setup-render   (render-vector-string :setup (mapv render-item-string keep-setup))
        teardown-render (render-vector-string :teardown (mapv render-item-string keep-teardown))]
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
        top-navs        (form-common/nav-top-levels root)]
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
                             (= 'fact (first (nav/value (form-common/nav-body zloc))))
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
        top-navs        (form-common/nav-top-levels root)]
    (str (str/join
          "\n\n"
          (mapcat (fn [zloc]
                    (let [line    (line-key (nav/line-info zloc))
                          current (block/block-string (nav/block zloc))
                          body    (form-common/nav-body zloc)
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

(defn- render-check-snippets-target
  [entry root-lang lang]
  (let [root-checks     (vec (sort-items (get-in entry [:checks :root])))
        derived-by-lang (->> (get-in entry [:checks :derived])
                             sort-items
                             (group-by item-lang))]
    (if (= lang root-lang)
      (mapv render-check-clause root-checks)
      (let [existing       (vec (get derived-by-lang lang))
            eligible-roots (vec (remove #(item-suppressed? % lang) root-checks))
            generated      (map-indexed
                            (fn [idx root-item]
                              (if-let [item (nth existing idx nil)]
                                (render-check-clause item)
                                (render-generated-check-clause root-item lang)))
                            eligible-roots)
            trailing       (map render-check-clause
                                (drop (count eligible-roots) existing))]
        (vec (concat (remove nil? generated)
                     trailing))))))

(defn- render-target-runtime-item
  [classification root-lang lang]
  (let [runtime-items (runtime-item-map classification)
        target-item   (get runtime-items lang)
        root-item     (get runtime-items root-lang)]
    (or (some-> target-item render-item-string)
        (when (and root-item
                   (not= lang root-lang))
          (render-generated-item-string root-item lang)))))

(defn- render-fact-string-target
  [entry root-lang lang]
  (let [check-snippets   (render-check-snippets-target entry root-lang lang)
        setup-items      (render-target-runtime-items (:fact-setup entry) root-lang [lang])
        teardown-items   (render-target-runtime-items (:fact-teardown entry) root-lang [lang])
        setup-render     (when (seq setup-items)
                           (render-vector-string :setup setup-items))
        teardown-render  (when (seq teardown-items)
                           (render-vector-string :teardown teardown-items))]
    (when (seq check-snippets)
      (let [meta-string (render-meta-string (cond-> (entry-meta entry)
                                              setup-render (assoc :setup [])
                                              teardown-render (assoc :teardown []))
                                            {:setup setup-render
                                             :teardown teardown-render})
            fact-body   (str "(fact " (pr-str (:intro entry))
                             "\n\n"
                             (str/join "\n\n" check-snippets)
                             ")")]
        (if meta-string
          (str meta-string "\n" fact-body)
          fact-body)))))

(defn- render-global-fact-target
  [output root-lang lang]
  (let [setup-items     (render-target-runtime-items (get-in output [:globals :global-fact-setup])
                                                     root-lang
                                                     [lang])
        teardown-items  (render-target-runtime-items (get-in output [:globals :global-fact-teardown])
                                                     root-lang
                                                     [lang])
        setup-render    (when (seq setup-items)
                          (render-vector-string :setup setup-items))
        teardown-render (when (seq teardown-items)
                          (render-vector-string :teardown teardown-items))]
    (when (or setup-render teardown-render)
      (str "(fact:global\n "
           (render-map-string {:setup setup-render
                               :teardown teardown-render})
           ")"))))

(defn- render-global-top-target
  [output lang]
  (let [all-items (form-common/item-classify-langs (get-in output [:globals :global-top]))
        keep-map  (->> (keep-target-items (get-in output [:globals :global-top]) lang)
                       (map (fn [item]
                              [(line-key (item-line item))
                               (render-item-string item)]))
                       (into {}))]
    {:all-lines (->> all-items
                     (map item-line)
                     (map line-key)
                     set)
     :keep-map keep-map}))

(defn- render-target-script-string
  [output lang]
  (let [root-lang   (get-in output [:globals :lang :root])
        derived-map (->> (get-in output [:globals :global-script :derived])
                         (keep (fn [item]
                                 (let [item-lang (some-> item
                                                         item-value
                                                         second
                                                         common/seedgen-normalize-runtime-lang)]
                                   (when item-lang
                                     [item-lang item]))))
                         (into {}))]
    (cond
      (= lang root-lang)
      (root-script-body-string output)

      (get derived-map lang)
      (some-> (get derived-map lang)
              item-string
              unwrap-meta-string)

      :else
      (some-> (root-script-body-string output)
              (replace-script-lang-string lang)))))

(defn render-top-level-target
  [output text lang target-ns]
  (let [root             (nav/parse-root text)
        top-navs         (form-common/nav-top-levels root)
        root-entry       (get-in output [:globals :global-script :root])
        root-lang        (get-in output [:globals :lang :root])
        root-script-line (some-> root-entry item-line line-key)
        derived-lines    (->> (get-in output [:globals :global-script :derived])
                              (map item-line)
                              (map line-key)
                              set)
        fact-entries     (->> (get output :entries)
                              vals
                              (mapcat vals))
        fact-by-refer    (into {}
                               (map (fn [entry]
                                      [(symbol (str (:ns entry)) (str (:var entry))) entry]))
                               fact-entries)
        {:keys [all-lines keep-map]} (render-global-top-target output lang)
        script-string    (render-target-script-string output lang)]
    (str (str/join
          "\n\n"
          (keep (fn [zloc]
                  (let [line    (line-key (nav/line-info zloc))
                        current (block/block-string (nav/block zloc))
                        body    (form-common/nav-body zloc)
                        form    (nav/value zloc)
                        head    (when (seq? (nav/value body))
                                  (first (nav/value body)))
                        refer   (:refer (meta form))]
                    (cond
                      (and (seq? form)
                           (= 'ns (first form)))
                      (replace-ns-name-string current target-ns)

                      (= line root-script-line)
                      script-string

                      (contains? derived-lines line)
                      nil

                      (= 'fact:global head)
                      (render-global-fact-target output root-lang lang)

                      (and (= 'fact head)
                           refer
                           (contains? fact-by-refer refer))
                      (render-fact-string-target (get fact-by-refer refer)
                                                 root-lang
                                                 lang)

                      (contains? all-lines line)
                      (get keep-map line)

                      :else
                      current)))
                top-navs))
         "\n")))

(defn seedgen-langadd
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
       (let [output      (seed-readforms/seedgen-readforms ns {} lookup project)
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

(defn seedgen-langremove
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
       (let [output      (seed-readforms/seedgen-readforms ns {} lookup project)
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
