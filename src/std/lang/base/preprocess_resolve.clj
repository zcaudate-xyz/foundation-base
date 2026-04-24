(ns std.lang.base.preprocess-resolve
  (:require [std.lang.base.preprocess-base :as preprocess]
            [std.lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]))

(defn get-fragment
  "gets the fragment given a symbol and modules"
  {:added "4.0"}
  ([sym modules mopts]
   (if (and (symbol? sym)
            (namespace sym))
     (let [[sym-ns sym-id] (ut/sym-pair sym)
           {:keys [id link]} (:module mopts)
           sym-module (or (if (= sym-ns id) id)
                          (get link sym-ns)
                          (first (filter #(= % sym-ns)
                                         (vals link)))
                          sym-ns)]
       (or (get-in modules [sym-module :fragment sym-id])
           (if-let [et (and (get-in modules [sym-module :code sym-id]))]
             (if (= :defrun (:op-key et))
               (apply list 'do (drop 2 (:form et))))))))))

(defn process-namespaced-resolve
  "resolves symbol in current namespace"
  {:added "4.0"}
  [sym modules {:keys [module] :as mopts}]
  (let [[sym-ns sym-id] (ut/sym-pair sym)
        sym-module (or (if (= '- sym-ns) (:id module))
                       (get (:link module) sym-ns)
                       (if (get modules sym-ns) sym-ns))]
    (cond (not sym-module)
          (f/error "Cannot resolve Module." {:input sym
                                             :current module
                                             :modules (keys modules)})

          :else
          [sym-module sym-id
           (ut/sym-full sym-module sym-id)])))

(defn- resolve-module-entry
  [sym-module sym-id modules mopts]
  (or (if-let [e (get-in modules [sym-module :code sym-id])]
        [:code e])
      (if-let [e (get-in modules [sym-module :fragment sym-id])]
        [:fragment e])
      (if-let [e (get-in modules [sym-module :header sym-id])]
        [:header e])
      (f/error (str "Upstream not found: "
                    (ut/sym-full {:module sym-module
                                  :id sym-id}))
               {:entry (ut/sym-full {:module sym-module
                                     :id sym-id})
                :opts  (select-keys mopts [:lang :module])})))

(defn- process-code-entry
  [entry sym-full sym-module module deps]
  (let [{:keys [op]} entry]
    (if (and (get (:suppress module) sym-module)
             (not= 'defglobal op))
      (f/error "Suppressed module - macros only"
               {:sym [sym-module (:id entry)]
                :module (dissoc module :code :fragment)}))
    (if (not (or preprocess/*macro-skip-deps*
                 (not deps)
                 (= 'defglobal op)
                 (= 'defrun op)))
      (vswap! deps conj sym-full))
    sym-full))

(defn- process-fragment-entry
  [entry sym-full sym-module sym deps-fragment walk-fn]
  (let [{:keys [template standalone form]} entry]
    (if (not (or preprocess/*macro-skip-deps*
                 (not deps-fragment)))
      (vswap! deps-fragment conj sym-full))
    (cond (not template) form

          (not standalone)
          (f/error "Pure templates are not allowed in body"
                   {:module sym-module
                    :id (:id entry)
                    :form sym})

          (or (collection/form? standalone)
              (symbol? standalone))
          (walk-fn standalone)

          :else
          (let [args (second form)]
            (list 'fn args
                  (list 'return
                        (apply template args)))))))

(defn process-namespaced-symbol
  "process namespaced symbols"
  {:added "4.0"}
  [sym modules {:keys [module entry] :as mopts} deps deps-fragment walk-fn]
  (let [walk-fn (or walk-fn identity)
        [sym-module sym-id sym-full] (process-namespaced-resolve sym modules mopts)
        module-id (:id module)]
    (if (and (= sym-module module-id)
             (= sym-id (:id entry)))
      sym-full
      (let [[type entry] (resolve-module-entry sym-module sym-id modules mopts)]
        (or (if preprocess/*macro-skip-deps*
              sym-full)
            (case type
              (:header :code) (process-code-entry entry sym-full sym-module module deps)
              :fragment (process-fragment-entry entry sym-full sym-module sym deps-fragment walk-fn)))))))

(defn process-standard-symbol
  [sym mopts deps-native]
  (let [symstr (name sym)
        idx    (.indexOf (name sym) ".")
        _      (if (<= 0 idx)
                 (let [symlead (symbol (subs symstr 0 idx))
                       import  (get-in mopts
                                       [:module
                                        :native-lu
                                        symlead])]
                   (if (and import deps-native)
                     (vswap! deps-native
                             update
                             import
                             (fnil #(conj % symlead) #{}))))
                 (let [import (get-in mopts
                                      [:module
                                       :native-lu
                                       sym])]
                   (if (and import deps-native)
                     (vswap! deps-native
                             update
                             import
                             (fnil #(conj % sym) #{})))))]
    sym))

(defn find-natives
  [entry mopts]
  (let [deps-quoted (volatile! [])
        deps-native (volatile! {})
        _           (walk/postwalk
                     (fn [form]
                       (if (and (list? form)
                                (= (first form) 'quote))
                         (vswap! deps-quoted conj (second form)))
                       form)
                     (:form entry))
        _           (walk/postwalk
                     (fn [form]
                       (cond (symbol? form)
                             (process-standard-symbol form mopts deps-native)

                             :else form))
                     @deps-quoted)]
    @deps-native))
