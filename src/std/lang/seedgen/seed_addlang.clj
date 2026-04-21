(ns std.lang.seedgen.seed-addlang
  (:require [clojure.string :as str]
            [code.framework :as base]
            [code.project :as project]
            [std.block.base :as block]
            [std.block.navigate :as nav]
            [std.fs :as fs]
            [std.lang.seedgen.seed-common :as common]
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

(defn- seedgen-root-form
  [test-file]
  (let [forms (fs/read-code test-file)
        heads (common/seedgen-script-heads (first forms))]
    (some (fn [form]
            (let [root? (:seedgen/root (meta form))]
              (when (and root?
                         (instance? clojure.lang.IObj form)
                         (seq? form)
                         (contains? heads (first form)))
                form)))
          (rest forms))))

(defn- seedgen-root-meta-langs
  [test-file]
  (some->> (seedgen-root-form test-file)
           meta
           :seedgen/root
           :langs
           normalize-target-langs))

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

(defn- fact-desc-string
  [bnav]
  (some-> bnav nav/down nav/right nav/block block/block-string))

(defn- fact-clause-data
  [bnav]
  (loop [current (some-> bnav nav/down nav/right nav/right)
         out     []]
    (if (nil? current)
      out
      (let [arrow    (nav/right current)
            expected (some-> arrow nav/right)
            lang     (common/seedgen-dispatch-lang (nav/value current))]
        (if (and arrow
                 expected
                 lang
                 (symbol? (nav/value arrow))
                 (re-find #"=>" (name (nav/value arrow))))
          (recur (nav/right expected)
                 (conj out {:lang            lang
                            :expr-string     (-> current nav/block block/block-string)
                            :expected-string (-> expected nav/block block/block-string)}))
          (recur (nav/right current) out))))))

(defn- render-vector-string
  [key ordered-langs snippets]
  (let [items        (keep snippets ordered-langs)
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

(defn- rewrite-fact-addlang
  [fact-str root-lang ordered-extra-langs target-set]
  (let [root       (nav/parse-root fact-str)
        current    (nav/down root)
        meta?      (= :meta (block/block-tag (nav/block current)))
        mnav       (when meta?
                     (nav/down current))
        bnav       (if meta?
                     (-> current nav/down nav/right)
                     current)
        m          (meta (nav/value current))
        desc-str   (fact-desc-string bnav)
        clauses    (fact-clause-data bnav)
        by-lang    (into {} (map (juxt :lang identity) clauses))
        root-clause (get by-lang root-lang)]
    (if (or (nil? desc-str)
            (nil? root-clause))
      fact-str
      (let [available-langs (->> ordered-extra-langs
                                 (filter #(or (contains? by-lang %)
                                              (contains? target-set %)))
                                 vec)
            final-langs     (vec (cons root-lang available-langs))
            indent          "  "
            clause-snippets (reduce (fn [out lang]
                                      (assoc out lang
                                             (if-let [{:keys [expr-string expected-string]} (get by-lang lang)]
                                               (render-clause-snippet indent expr-string expected-string)
                                               (render-clause-snippet indent
                                                                      (replace-dispatch-lang-string
                                                                       (:expr-string root-clause)
                                                                       lang)
                                                                      (:expected-string root-clause)))))
                                    {}
                                    final-langs)
            meta-string     (when m
                              (let [setup-root    (some #(when (= root-lang (common/seedgen-dispatch-lang %))
                                                           (pr-str %))
                                                       (:setup m))
                                    teardown-root (some #(when (= root-lang (common/seedgen-dispatch-lang %))
                                                           (pr-str %))
                                                       (:teardown m))
                                    setup-items   (when setup-root
                                                    (let [current-items (->> (:setup m)
                                                                             (keep (fn [form]
                                                                                     (when-let [lang (common/seedgen-dispatch-lang form)]
                                                                                       [lang (pr-str form)])))
                                                                             (into {}))]
                                                      (render-vector-string :setup
                                                                            final-langs
                                                                            (reduce (fn [out lang]
                                                                                      (assoc out lang
                                                                                             (or (get current-items lang)
                                                                                                 (when (contains? target-set lang)
                                                                                                   (replace-dispatch-lang-string
                                                                                                    setup-root
                                                                                                    lang)))))
                                                                                    {root-lang setup-root}
                                                                                    final-langs))))
                                    teardown-items (when teardown-root
                                                     (let [current-items (->> (:teardown m)
                                                                              (keep (fn [form]
                                                                                      (when-let [lang (common/seedgen-dispatch-lang form)]
                                                                                        [lang (pr-str form)])))
                                                                              (into {}))]
                                                       (render-vector-string :teardown
                                                                             final-langs
                                                                             (reduce (fn [out lang]
                                                                                       (assoc out lang
                                                                                              (or (get current-items lang)
                                                                                                  (when (contains? target-set lang)
                                                                                                    (replace-dispatch-lang-string
                                                                                                     teardown-root
                                                                                                     lang)))))
                                                                                     {root-lang teardown-root}
                                                                                     final-langs))))]
                                (render-meta-string m {:setup setup-items
                                                       :teardown teardown-items})))
            fact-body       (str "(fact " desc-str
                                 "\n"
                                 (str/join "\n\n" (map clause-snippets final-langs))
                                 ")")]
        (if meta-string
          (str meta-string "\n" fact-body)
          fact-body)))))

(defn seedgen-addlang
  "imports unit tests as docstrings
  
   (project/in-context (import {:print {:function true}}))
   => map?"
  {:added "3.0"}
  ([ns params lookup project]
   (let [test-ns   (project/test-ns ns)
         test-file (lookup test-ns)
         params    (task/single-function-print params)]
     (cond (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})

           :else
           (let [root-form    (seedgen-root-form test-file)
                 root-lang    (some-> root-form second)
                 stored-langs (seedgen-root-meta-langs test-file)
                 target-lang  (or (normalize-target-langs (:lang params))
                                  stored-langs
                                  [])
                 target-set   (set target-lang)]
             (cond (nil? root-lang)
                   (res/result {:status :error
                                :data :no-seedgen-root})

                   (some #{root-lang} target-lang)
                   (res/result {:status :error
                                :data :cannot-add-root
                                :lang root-lang})

                   :else
                   (let [top-level-form-strings
                         (fn [text]
                           (let [root          (nav/parse-root text)
                                 current0      (nav/down root)
                                 script-heads  (common/seedgen-script-heads
                                                (nav/value current0))
                                 current-langs (set (common/seedgen-root-langs test-file false))
                                 existing-script-strings
                                 (loop [current current0
                                        out     {}]
                                   (if (nil? current)
                                     out
                                     (let [form (nav/value current)]
                                       (recur (nav/right current)
                                              (if (and (seq? form)
                                                       (contains? script-heads (first form))
                                                       (not (:seedgen/root (meta form))))
                                                (assoc out
                                                       (common/seedgen-normalize-runtime-lang (second form))
                                                       (-> current nav/block block/block-string))
                                                out)))))]
                             (loop [current current0
                                    out     []]
                               (if (nil? current)
                                 (str (str/join "\n\n" out) "\n")
                                 (let [blk      (nav/block current)
                                       form     (nav/value current)
                                       expr-str (block/block-string blk)]
                                   (cond
                                     (and (seq? form)
                                          (contains? script-heads (first form))
                                          (not (:seedgen/root (meta form))))
                                     (recur (nav/right current) out)

                                     (and (seq? form)
                                          (contains? script-heads (first form))
                                          (:seedgen/root (meta form)))
                                     (let [body-string    (if (= :meta (block/block-tag (nav/block current)))
                                                            (-> current nav/down nav/right nav/block block/block-string)
                                                            expr-str)
                                           ordered-scripts (->> (or stored-langs target-lang)
                                                                (filter #(or (contains? current-langs %)
                                                                             (contains? target-set %)))
                                                                vec)
                                           add-scripts     (->> ordered-scripts
                                                                (map #(or (get existing-script-strings %)
                                                                          (replace-script-lang-string body-string %)))
                                                                vec)]
                                       (recur (nav/right current)
                                              (into out (cons expr-str add-scripts))))

                                     (and (seq? form)
                                          (= 'fact (first form)))
                                     (recur (nav/right current)
                                            (conj out (rewrite-fact-addlang expr-str
                                                                            root-lang
                                                                            stored-langs
                                                                            target-set)))

                                     :else
                                     (recur (nav/right current)
                                            (conj out expr-str))))))))]
                     (base/transform-code test-ns
                                          (-> params
                                              (assoc :transform top-level-form-strings
                                                     :no-analysis true)
                                              (dissoc :lang))
                                          lookup
                                          project))))))))

(comment
  (code.project/in-context
   (std.lang.seedgen.seed-addlang/seedgen-addlang '<sample>)))
