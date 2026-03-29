(ns std.lang.base.emit-top-level
  (:require [clojure.string]
            [std.lang.base.emit-assign :as assign]
            [std.lang.base.emit-block :as block]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-fn :as fn]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-special :as special]
            [std.lib.collection :as collection]
            [std.lib.env :as env]
            [std.lib.foundation :as f]))

(defn transform-defclass-inner
  "transforms the body to be fn.inner and var.inner"
  {:added "4.0"}
  [body]
  (mapv (fn [form]
          (if (collection/form? form)
            (cond (= (first form) 'fn)
                  (cons 'fn.inner (cons (with-meta (second form)
                                          (meta form))
                                        (drop 2 form)))

                  (= (first form) 'var)
                  (cons 'var.inner (rest form))

                  :else form)
            form))
        body))

(defn emit-def
  "creates the def string"
  {:added "3.0"}
  ([key [tag sym body :as form] grammar mopts]
   (let [{:keys [raw space assign statement] :as props} (helper/get-options grammar [:define key])
         typestr (fn/emit-def-type sym (or raw
                                           (f/strn tag))
                                   grammar mopts)
         sym-str   (case key
                     :defglobal (common/emit-with-global nil [nil sym] grammar mopts)
                     (common/*emit-fn* sym grammar mopts))]
     (str typestr
          (if (not-empty typestr) space)
          sym-str
          space assign space
          (common/*emit-fn* body grammar mopts)
          statement))))

(defn emit-declare
  "emits declared 
 
   (emit-declare :def
                 '(declare a b c)
                 +grammar+
                 {})
   => \"def a,b,c\""
  {:added "4.0"}
  ([key [tag & syms] grammar mopts]
   (let [{:keys [raw space sep]}  (helper/get-options grammar [:define key])
         pre (str (or raw tag))]
     (str pre space (clojure.string/join sep (common/emit-array (vec syms) grammar mopts))))))

(defn emit-top-level
  "generic define form
 
   (binding [common/*emit-fn* common/emit-common]
     (emit-top-level :defn
                     '(defn abc [a := 0]
                        (+ 1 2 3))
                     +grammar+
                     {}))
   => \"function abc(a = 0){\\n  1 + 2 + 3;\\n}\""
  {:added "3.0"}
  ([key [tag sym & more :as form] grammar mopts]
   (let [module-id (or (-> mopts :entry :module)
                       (-> mopts :module :id))
         sym (if (and
                  (not (:inner (meta sym)))
                  (or (#{:full :host} (:layout mopts))
                      (= key :defglobal)))
               (do (or module-id
                       (f/error "Module not found"
                                (select-keys mopts [:module :entry])))
                   (with-meta (symbol (name module-id)
                                      (name sym))
                     (meta sym)))
               sym)
         form (apply list tag sym more)]
     (case key
       :def         (emit-def key form grammar mopts)
       :defglobal   (emit-def key form grammar mopts)
       :defrun      (block/emit-do  (drop 2 form) grammar mopts)
       :defn        (fn/emit-fn  key form grammar mopts)
       :defgen      (fn/emit-fn  key form grammar mopts)
       :declare     (emit-declare key form grammar mopts)))))

(def +emit-lookup+
  (assoc common/+emit-lookup+
         :data  #'data/emit-data
         :block #'block/emit-block))

(defn emit-form
  "creates a customisable emit and integrating both top-level and statements
   ^:hiddn
   
   (emit-form :custom
              '(custom 1 2 3)
              (assoc-in +grammar+
                        [:reserved 'custom]
                        {:emit  (fn [_ _ _]
                                  'CUSTOM)})
             [])
   => 'CUSTOM"
  {:added "4.0"}
  ([key [sym & args :as form] {:keys [reserved] :as grammar} mopts]
   (let [{:keys [emit type] :as props} (get reserved sym)]
     (if common/*trace*
       (env/prn form))
     (try
       (cond (or (fn? emit)
                 (var? emit))
             (emit form grammar mopts)

             (keyword? emit)
             (common/emit-op key form grammar mopts
                             {:def-assign #'assign/emit-def-assign
                              :quote #'data/emit-quote
                              :table #'data/emit-table
                              :with-module #'special/emit-with-module
                              :with-lang  #'special/emit-with-lang
                              :with-eval  #'special/emit-with-eval
                              :with-deref #'special/emit-with-deref})
             
             :else
             (case type
               :fn         (fn/emit-fn key form grammar mopts)
               :def        (emit-top-level key form grammar mopts)
               (f/error "Missing key" {:key key
                                       :symbol sym
                                       :props props
                                       :entry (get reserved sym)})))
       (catch Throwable t
         (if common/*explode*
           (env/prn :EMIT-ERROR form t))
         (throw t))))))
