(ns std.lang.base.impl-deps
  (:require [std.lang.base.util :as ut]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.book :as b]
            [std.lang.base.impl-deps-imports :as imports]
            [std.string :as str]
            [std.lib :as h]
            [clojure.set :as set]))

;;
;;
;;

(defn module-import-form
  "import form"
  {:added "4.0"}
  [{:keys [meta] :as book} name module opts]
  (if-let [f (:module-import meta)] (f name module opts)))

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
                                 (h/error "Imports not of the same name"
                                          {:ns   id
                                           :key  k
                                           :curr ov
                                           :new  v}))))
                       out
                       native))
          (or initial {})
          modules))

(defn collect-script-entries
  "collects all entries"
  {:added "4.0"}
  ([{:keys [modules] :as book} sym-ids]
   (let [ids  (h/seqify sym-ids)
         ids  (:all (h/deps:resolve book ids))
         module-ids (set (map ut/sym-module ids)) 
         module-lu  (->> (h/deps:ordered book module-ids)
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
        [entries module-lu] (collect-script-entries book sym-ids)
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

(defn collect-module-check-options
  "does the checks for the inputs"
  {:added "4.0"}
  [{:keys [type] :as options}]
  (let [ks-input (set (keys options))
        [ks-required
         ks-optional] (case type
                        nil        [#{} #{}]
                        :custom    [#{:type
                                      :fn-link-form
                                      :root-ns
                                      :params} #{:base}]
                        :graph     [#{:type :root-ns}
                                    #{:path-suffix :base}]
                        :directory [#{:type
                                      :root-ns}
                                    #{:root-libs
                                      :root-prefix
                                      :path-separator
                                      :path-suffix
                                      :path-replace
                                      :base}])
        ks-errored {:input options
                    :required ks-required
                    :optional ks-optional}]
    (if-not (= ks-required (set/intersection ks-required ks-input))
      (h/error "Required keys missing" ks-errored))
    (if-not (empty? (set/difference ks-input
                                    (set/union ks-required
                                               ks-optional) ))
      (h/error "Extra keys in map" ks-errored))))

(defn collect-module-ns-select
  "TODO"
  {:added "4.0"}
  [ns selection]
  (h/prn ns selection)
  (let [{:keys [default]} selection
        selected (first (keep (fn [[kns value]]
                                (cond (and (symbol? kns)
                                           (.startsWith (str ns)
                                                        (str kns)))
                                      [kns value]

                                      (and (h/regexp? kns)
                                           (re-find kns (str ns)))
                                      [nil value]))
                              (dissoc selection :default)))]
    (or selected
        [nil default])))

(defn collect-module-directory-form
  "collects forms for"
  {:added "4.0"}
  [_ ns {:keys [root-ns
                root-libs
                root-prefix
                path-separator
                path-suffix
                path-replace]
         :or {root-libs   "libs"
              root-prefix "."
              path-suffix ""
              path-separator "/"
              path-replace {}}
         :as options}]
  (let [[root-local root-prefix] (if (string? root-prefix)
                                   [nil root-prefix]
                                   (collect-module-ns-select ns root-prefix))
        [_ path-suffix] (if (string? path-suffix)
                          [nil path-suffix]
                          (collect-module-ns-select ns path-suffix))
        root-ns   (or root-local root-ns)

        [ns-str
         root-str] [(str ns)
                    (str root-ns)]        
        is-sub? (.startsWith ^String ns-str
                             root-str)
        ns-new  (if is-sub?
                  (subs ns-str
                        (inc (count root-str)))
                  ns-str)
        ns-paths (str/split ns-new #"\.")
        ns-paths (mapv (fn [path]
                         (reduce (fn [s [pat sub]]
                                   (str/replace s pat sub))
                                 path
                                 path-replace))
                       ns-paths)]
    (str root-prefix
         (if is-sub?
           path-separator
           (str path-separator root-libs path-separator))
         (str/join path-separator ns-paths)
         (if (not-empty path-suffix)
           (str path-suffix)))))

(defn collect-module
  "collects information for the entire module"
  {:added "4.0"}
  [book {:keys [native]
         :as module}
   & [{:keys [type]
       :as options}]]
  (let [_        (collect-module-check-options options)
        setup    (setup-module-form book module)
        teardown (teardown-module-form book module)
        {:keys [direct
                native]} (imports/module-imports book (:id module))
        code    (->> (vals (:code module))
                     (sort-by (juxt :priority :line :time)))
        
        form-fn  (cond (= type :custom)
                       (or (:fn-link-form options)
                           (h/error (str "Missing key " :fn-link-form)
                                    options))
                       
                       (= type :graph)
                       module-link-form
                       
                       (= type :directory)
                       collect-module-directory-form) 
        
        link    (case type
                  (:graph :directory :custom)
                  (keep (fn [[sym ns]]
                          (cond (= ns (:id module)) nil
                                
                                :else
                                (let [link (form-fn book ns options)]
                                  [link (if (vector? sym)
                                          {:refer sym
                                           :ns ns}
                                          {:as sym
                                           :ns ns})])))
                        (h/transpose
                         (select-keys (:internal module)
                                      direct)))
                  ;; an empty map differs from the array
                  ^:meta/empty {})]
    {:setup    setup
     :teardown teardown
     :code     code
     :native   native
     :link     link}))

