(ns code.ai.heal.form
  (:require [code.manage :as manage]
            [code.edit :as edit]
            [code.query :as query]
            [std.block.heal.core :as heal]
            [code.ai.heal.form-edits :as form-edits]
            [std.block :as b]
            [std.fs :as fs]
            [std.lib :as h]))

(defn get-dsl-deps-fn
  [content]
  (let [nav      (edit/parse-root content)
        ns-name  (edit/value
                  (first (query/select nav
                                       '[(ns | _ & _)])))
        ns-deps  (set (map first
                           (:require (edit/value
                                      (first
                                       (query/select nav
                                                     ['(l/script & _)
                                                      '|
                                                      map?]))))))]
    {:ns ns-name
     :deps ns-deps}))

(defn load-file-fn
  [content]
  (let [ns  (second (read-string content))
        res (load-string content)]
    [ns ]))

(defn get-load-order
  [input-deps]
  (let [all-deps   (apply clojure.set/union
                          (map :deps (vals input-deps)))
        lu-ns      (h/map-vals :ns input-deps)
        lu-path    (h/transpose lu-ns)
        load-deps  (set (keys lu-path))
        sys-deps   (clojure.set/difference
                    all-deps load-deps)
        mdeps      (h/map-entries
                    (fn [[path {:keys [ns deps]}]]
                      [ns (clojure.set/difference
                           deps sys-deps)])
                    input-deps)
        ordered    (h/topological-sort-order-by-deps
                    mdeps
                    (h/topological-sort mdeps))]
    (mapv lu-path ordered)))

(defn heal-directory
  [{:keys [root source-paths]
    :as env}
   & [{:keys [write]
       :as params}]]
  (manage/transform-code
   :all
   (h/merge-nested
    {:title (fn [params env]
              (str "HEAL DIRECTORY" " - " (fs/path (:root env))))
     :transform heal/heal-content
     :print {:function true}
     :verify {:read b/parse-root}
     :no-analysis true}
    params)
   {:root root
    :source-paths source-paths}))

(defn refactor-directory
  [{:keys [root source-paths]
    :as env}
   edits
   & [{:keys [write]
       :as params}]]
  (manage/refactor-code
   :all
   (h/merge-nested
    {:title (fn [params env]
              (str "REFACTOR DIRECTORY" " - " (fs/path (:root env))))
     :edits  edits
     :print  {:function true}
     :verify {:read b/parse-root}
     :no-analysis true}
    params)
   {:root root
    :source-paths source-paths}))

(defn get-dsl-deps
  [{:keys [root source-paths]
    :as env}
   & [{:keys [write]
       :as params}]]
  (manage/extract
   :all
   (h/merge-nested
    {:title (fn [params env]
              (str "GET DSL DEPS" " - " (fs/path (:root env))))
     :process get-dsl-deps-fn}
    params)
   {:root root
    :source-paths source-paths}))

(defn load-directory
  [{:keys [root source-paths]
    :as env}
   deps
   & [{:keys [write ]
       :as params}]]
  (let [task-fn (assoc-in manage/extract
                       [:item :list]
                       (fn [lookup env]
                         (get-load-order deps)))]
    (task-fn
     :all
     (h/merge-nested
      {:parallel false
       :print {:item true :result true :summary true}
       :title (fn [params env]
                (str "LOAD DSL" " - " (fs/path (:root env))))
       :process load-file-fn}
      params)
     {:root root
      :source-paths source-paths})))


(comment
  (heal-directory
   {:root "../Szncampaigncenter/"
    :source-paths ["src-translated"]}
   {:write true})
  
  (refactor-directory
   {:root "../Szncampaigncenter/"
    :source-paths ["src-translated"]}
   [form-edits/fix:remove-fg-extra-references
    form-edits/fix:replace-fg-extra-namepspaces
    form-edits/fix:namespaced-symbol-no-dot
    form-edits/fix:set-arg-destructuring
    form-edits/fix:dash-indexing
    form-edits/fix:remove-mistranslated-syms]
   {:write true})
  

  (refactor-directory
   {:root "../Szncampaigncenter/"
    :source-paths ["src-translated"]}
   [form-edits/fix:remove-mistranslated-syms])
  
  
  (def +deps+
    (get-dsl-deps
     {:root "../Szncampaigncenter/"
      :source-paths ["src-translated"]}))
  

  (load-directory
   {:root "../Szncampaigncenter/"
    :source-paths ["src-translated"]}
   +deps+))


