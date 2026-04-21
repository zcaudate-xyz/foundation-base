(ns std.lang.seedgen.seed-removelang
  (:require [clojure.set :as set]
            [clojure.string :as str]
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

(defn- update-root-meta-string
  [text known-langs]
  (if (empty? known-langs)
    text
    (let [root         (nav/parse-root text)
          current0     (nav/down root)
          script-heads (common/seedgen-script-heads
                        (nav/value current0))]
      (loop [current current0]
        (if (nil? current)
          text
          (let [form (nav/value current)]
            (if (and (seq? form)
                     (contains? script-heads (first form))
                     (:seedgen/root (meta form)))
              (let [expr-str (-> current nav/block block/block-string)
                    body-str (if (= :meta (block/block-tag (nav/block current)))
                               (-> current nav/down nav/right nav/block block/block-string)
                               expr-str)
                    new-meta (assoc (:seedgen/root (meta form))
                               :langs known-langs)
                    revised  (str "^{:seedgen/root "
                                  (pr-str new-meta)
                                  "}\n"
                                  body-str)]
                (str/replace-first text
                                   (re-pattern (java.util.regex.Pattern/quote expr-str))
                                   (java.util.regex.Matcher/quoteReplacement revised)))
              (recur (nav/right current)))))))))

(defn seedgen-removelang
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
           (let [root-form   (seedgen-root-form test-file)
                 root-lang   (some-> root-form second)
                 purge-langs (common/seedgen-root-langs test-file false)
                 known-langs (->> (concat (or (seedgen-root-meta-langs test-file) [])
                                          purge-langs)
                                  distinct
                                  vec)
                 target-lang (or (normalize-target-langs (:lang params))
                                 :all)
                 target-lang (if (= :all target-lang)
                               purge-langs
                               target-lang)]
             (cond (nil? root-lang)
                   (res/result {:status :error
                                :data :no-seedgen-root})

                   (some #{root-lang} target-lang)
                   (res/result {:status :error
                                :data :cannot-purge-root
                                :lang root-lang})

                   :else
                   (letfn [(purge-target?
                             [form]
                             (let [langs (common/seedgen-runtime-dispatch-langs form)]
                               (and (seq langs)
                                    (set/subset? (set langs)
                                                 (set target-lang)))))
                           (meta-block?
                             [zloc]
                             (= :meta (block/block-tag (nav/block zloc))))
                           (meta-nav
                             [zloc]
                             (when (meta-block? zloc)
                               (nav/down zloc)))
                           (body-nav
                             [zloc]
                             (if (meta-block? zloc)
                               (-> zloc nav/down nav/right)
                               zloc))
                           (check-arrow?
                             [form]
                             (and (symbol? form)
                                  (boolean (re-find #"=>" (name form)))))
                           (normalize-container-nav
                             [zloc pred]
                             (cond (nil? zloc)
                                   zloc

                                   (pred (nav/value zloc))
                                   zloc

                                   :else
                                   (nav/up zloc)))
                           (purge-vector-nav
                             [vnav]
                             (loop [state-nav vnav
                                    current   (nav/down vnav)]
                               (cond (nil? current)
                                     (normalize-container-nav state-nav vector?)

                                     (purge-target? (nav/value current))
                                     (let [updated (nav/delete current)
                                           updated (if (nil? (nav/value updated))
                                                     (nav/tighten updated)
                                                     updated)]
                                       (recur updated updated))

                                     :else
                                     (recur state-nav (nav/right current)))))
                           (purge-meta-nav
                             [mnav]
                             (loop [state-nav mnav
                                    current   (nav/down mnav)]
                               (cond (nil? current)
                                     (normalize-container-nav state-nav map?)

                                     :else
                                     (let [key  (nav/value current)
                                           vnav (nav/right current)]
                                       (cond (not (#{:setup :teardown} key))
                                             (recur state-nav
                                                    (some-> vnav nav/right))

                                             (nil? vnav)
                                             (recur state-nav nil)

                                             :else
                                             (let [updated-vnav (purge-vector-nav vnav)]
                                               (if (empty? (nav/value updated-vnav))
                                                 (let [updated (-> updated-vnav
                                                                   nav/delete-left
                                                                   nav/delete)
                                                       updated (if (nil? (nav/value updated))
                                                                 (nav/tighten updated)
                                                                 updated)]
                                                   (recur updated updated))
                                                 (recur updated-vnav
                                                        (some-> updated-vnav
                                                                nav/right)))))))))
                           (purge-body-nav
                             [bnav]
                             (loop [state-nav bnav
                                    current   (nav/down bnav)]
                               (cond (nil? current)
                                     (normalize-container-nav state-nav
                                                              #(and (seq? %)
                                                                    (= 'fact (first %))))

                                     (and (purge-target? (nav/value current))
                                          (some-> current nav/right nav/value check-arrow?))
                                     (let [updated (-> current
                                                       nav/delete-right
                                                       nav/delete-right
                                                       nav/delete)
                                           updated (if (nil? (nav/value updated))
                                                     (nav/tighten updated)
                                                     updated)]
                                       (recur updated updated))

                                     :else
                                     (recur state-nav (nav/right current)))))
                           (script-form?
                             [form script-heads]
                             (and (seq? form)
                                  (contains? script-heads (first form))))
                           (purge-string
                             [text]
                             (let [root         (nav/parse-root text)
                                   current0     (nav/down root)
                                   script-heads (common/seedgen-script-heads
                                                 (nav/value current0))]
                               (loop [state-nav root
                                      current   current0]
                                 (cond (nil? current)
                                       (nav/root-string state-nav)

                                       :else
                                       (let [form (nav/value current)]
                                         (cond (and (script-form? form script-heads)
                                                    (contains? (set target-lang)
                                                               (common/seedgen-normalize-runtime-lang
                                                                (second form))))
                                               (let [updated (nav/delete current)]
                                                 (recur updated updated))

                                               (and (seq? form)
                                                    (= 'fact (first form)))
                                               (let [current (if-let [mnav (meta-nav current)]
                                                               (-> mnav
                                                                   purge-meta-nav
                                                                   nav/up)
                                                               current)
                                                     bnav    (body-nav current)
                                                     updated (if (meta-block? current)
                                                               (-> bnav
                                                                   purge-body-nav
                                                                   nav/up)
                                                               (purge-body-nav bnav))]
                                                 (recur updated (nav/right updated)))

                                               :else
                                               (recur current (nav/right current))))))))]
                     (base/transform-code test-ns
                                          (-> params
                                              (assoc :transform (comp #(update-root-meta-string % known-langs)
                                                                      purge-string)
                                                     :no-analysis true)
                                              (dissoc :lang))
                                          lookup
                                          project))))))))

(comment
  (code.project/in-context
   (std.lang.seedgen.seed-removelang/seedgen-removelang '<sample>
                                                        {:lang :all})))
