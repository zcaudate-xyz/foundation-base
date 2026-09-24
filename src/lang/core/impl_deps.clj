(ns lang.core.impl-deps
  (:require [clojure.set :as set]
            [lang.base.book :as b]
            [lang.base.emit :as emit]
            [lang.base.emit-preprocess :as preprocess]
            [lang.base.preprocess-base :as preprocess-base]
            [lang.base.emit-rewrite :as rewrite]
            [lang.base.grammar-xtalk-system :as xtalk-system]
            [lang.core.impl-deps-imports :as imports]
            [lang.core.impl-entry :as entry]
            [lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.deps :as deps]
            [std.lib.foundation :as f]))

;;
;;
;;

(defn module-import-form
  "import form"
  {:added "4.0"}
  [{:keys [meta] :as book} name module opts]
  (if-let [f (:module-import meta)] (f name module opts)))

(defn native-name-with-override
  "applies native override to a native import name"
  {:added "4.0"}
  [name emit]
  (if-let [override-map (when-let [override (:override emit)]
                          (into {} (map (fn [[k v]]
                                          [(if (symbol? k) (str k) k) v])
                                        override)))]
    (or (get override-map (if (symbol? name) (str name) name))
        name)
    name))

(defn module-export-form
  "export form"
  {:added "4.0"}
  [{:keys [meta] :as book} module opts]
  (if-let [f (:module-export meta)] (f module opts)))

(defn module-link-form
  "link form for projects"
  {:added "4.0"}
  [{:keys [meta] :as book} ns options]
  (if-let [f (:module-link meta)] (f ns options)))

(defn has-module-form
  "checks if module is available"
  {:added "4.0"}
  [{:keys [meta] :as book} module]
  (if-let [f (:has-module meta)] (f module)))

(defn setup-module-form
  "setup the module"
  {:added "4.0"}
  [{:keys [meta] :as book} module]
  (if-let [f (:setup-module meta)] (f module)))

(defn teardown-module-form
  "teardown the module"
  {:added "4.0"}
  [{:keys [meta] :as book} module]
  (if-let [f (:teardown-module meta)] (f module)))

(defn has-ptr-form
  "form to check if pointer exists"
  {:added "4.0"}
  [{:keys [meta] :as book} ptr]
  (if-let [f (:has-ptr meta)] (f ptr)))

(defn setup-ptr-form
  "form to setup pointer"
  {:added "4.0"}
  [{:keys [meta] :as book} ptr]
  (if-let [f (:setup-ptr meta)] (f ptr)))

(defn teardown-ptr-form
  "form to teardown pointer"
  {:added "4.0"}
  [{:keys [meta] :as book} ptr]
  (if-let [f (:teardown-ptr meta)] (f ptr)))

(defn collect-script-natives
  "gets native imported modules"
  {:added "4.0"}
  [modules initial]
  (reduce (fn [out {:keys [native id]}]
            (reduce-kv (fn [out k v]
                         (let [ov (get out k)]
                           (cond (nil? ov)
                                 (assoc out k v)

                                 (= ov v)
                                 out

                                 :else
                                 (f/error "Imports not of the same name"
                                          {:ns   id
                                           :key  k
                                           :curr ov
                                           :new  v}))))
                       out
                       native))
          (or initial {})
          modules))

(defn is-global-entry?
  [book id]
  (= :defglobal (:op-key (b/get-code-entry book id))))

(defn collect-script-entry-ids
  "resolves script entries, optionally omitting global initializers"
  {:added "4.1"}
  [book sym-ids global-init]
  (if global-init
    (:all (deps/deps-resolve book sym-ids))
    (loop [all #{}
           pending (set sym-ids)]
      (let [pending (set/difference pending all)
            global-entry? (partial is-global-entry? book)]
        (if (empty? pending)
          (set (remove global-entry? all))
          (let [all (into all pending)
                next (->> pending
                          (remove global-entry?)
                          (mapcat (partial b/get-code-deps book))
                          set)]
            (recur all next)))))))

(defn collect-script-entries
  "collects all entries"
  {:added "4.0"}
  ([{:keys [modules] :as book} sym-ids]
   (collect-script-entries book sym-ids {:global-init true}))
  ([{:keys [modules] :as book} sym-ids {:keys [global-init]
                                        :or {global-init true}}]
   (let [ids  (collect-script-entry-ids book (collection/seqify sym-ids) global-init)
         module-ids (set (map ut/sym-module ids)) 
         module-lu  (->> (deps/deps-ordered book module-ids)
                         (map (fn [i id]
                                [id {:index i :module (get modules  id)}])
                              (range))
                         (into {}))
         
         ;; sort by module order and line number
         entries (->> (map (partial b/get-code-entry book) ids)
                      (sort-by (juxt (comp :index module-lu :module)
                                     :priority :line :time)))]
     [entries module-lu])))

(defn collect-script
  "collect dependencies given a form and book"
  {:added "4.0"}
  [book form mopts]
  (let [_ (assert book "Book required.")
        input (preprocess/to-input form)
        [form sym-ids]  (preprocess/to-staging input
                                               (:grammar book)
                                               (:modules book) mopts)
        form (rewrite/rewrite-stage :staging
                                    form
                                    (:grammar book)
                                    (assoc mopts :book book))
        polyfill-sym-ids (-> form
                             (xtalk-system/scan-xtalk (:grammar book))
                             :ops
                             (xtalk-system/xtalk-ops-polyfill-symbols (:grammar book)))
        [entries module-lu] (collect-script-entries book
                                                    (concat sym-ids
                                                            polyfill-sym-ids)
                                                    {:global-init (not= false (:global-init mopts))})
        natives (imports/script-imports book entries)]
    [form entries natives]))

(defn collect-script-summary
  "summaries the output of `collect-script`"
  {:added "4.0"}
  [[form entries natives]]
  [form (map ut/sym-full entries) natives])

;;
;;
;;

(defn collect-module
  "collects information for the entire module"
  {:added "4.0"}
  [book
   {:keys [native]
    :as module}]
  (let [setup    (setup-module-form book module)
        teardown (teardown-module-form book module)
        {:keys [direct
                native]} (imports/module-imports book (:id module))
        code    (->> (vals (:code module))
                     (sort-by (juxt :priority :line :time)))]
    {:setup    setup
     :teardown teardown
     :code     code
     :native   native
     :direct   direct}))
