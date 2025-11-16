(ns std.lang.base.impl
  (:require [std.string :as str]
            [std.lib :as h]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit-common :as emit-common]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.impl-deps :as deps]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.library :as lib]
            [std.lang.base.util :as ut]))

(defonce ^:dynamic *library* nil)

(defonce ^:dynamic *print-form* nil)

(defmacro ^{:style/indent 1}
  with:library
  "injects a library as the default"
  {:added "4.0"}
  ([[lib] & body]
   `(binding [*library* ~lib]
      ~@body)))

(h/res:spec-add
 {:type :hara/lang.library
  :mode {:allow #{:global}
         :default :global}
  :instance {:create #'lib/library:create
             :start h/start
             :stop h/stop}})

(defn default-library
  "gets the default library"
  {:added "4.0"}
  ([& [override]]
   (or override
       *library*
       (h/res :hara/lang.library))))

(defn default-library:reset
  "clears the default library, including all grammars"
  {:added "4.0"}
  ([& [override]]
   (h/res:stop :hara/lang.library)))

(defn runtime-library
  "gets the current runtime (annex or default)
 
   (runtime-library)
   => lib/library?"
  {:added "4.0"}
  []
  (or  *library*
      (:library (h/p:space-rt-get :lang.annex))
      (default-library)))

(defn grammar
  "gets the grammar"
  {:added "4.0"}
  [lang]
  (:grammar (lib/get-book (runtime-library) lang)))

;;
;;
;;

(defn- emit-options-raw
  [library snapshot lang]
  (let [snapshot (or snapshot
                     (let [library (or library (runtime-library))]
                       (lib/get-snapshot library)))
        book   (snap/get-book snapshot lang)]
    [snapshot book]))

(defn emit-options
  "create emit options
 
   (emit-options {:lang :lua})
   => vector?"
  {:added "4.0"}
  [{:keys [lang library snapshot emit] :as meta}]
  (let [_ (assert (identity lang) "Lang Required")
        [snapshot book] (emit-options-raw library snapshot lang)]
    (emit/prep-options (assoc meta
                              :book book
                              :snapshot snapshot))))

(defn to-form
  "input to form"
  {:added "4.0"}
  [form meta]
  (let [[stage grammar book namespace mopts] (emit-options meta)]
    (first (emit/prep-form stage form grammar book mopts))))

(defmacro %.form
  "converts to a form"
  {:added "4.0"}
  [form]
  (list 'quote (to-form form (merge {:lang :xtalk} (meta &form)))))

;;
;;
;;

(defn- emit-bulk?
  [form]
  (boolean (and (vector? form)
                (:bulk (meta form)))))

(defn emit-direct
  "adds additional controls to transform form"
  {:added "4.0"}
  [grammar form namespace {:keys [lang
                                  emit
                                  bulk]
                           :as mopts}]
  (or lang (h/error "Lang required." {:input (keys mopts)}))
  (binding [preprocess/*macro-grammar* grammar
            preprocess/*macro-opts* mopts]
    (if (not (:suppress emit))
      (let [{:keys [trim transform]} emit
            form (cond-> form transform (transform mopts))
            _    (if *print-form* (h/p :FORM form))
            body (emit/emit form
                            grammar
                            (the-ns namespace)
                            mopts)
            body (cond-> body trim (trim))]
        body))))

(defn emit-str
  "converts to an output string"
  {:added "4.0"}
  [form meta]
  (let [[stage grammar book namespace mopts] (emit-options meta)
        bulk (emit-bulk? form)
        [form]  (emit/prep-form stage form grammar book mopts)]
    (emit-direct grammar
                 form
                 namespace
                 (assoc mopts :bulk bulk))))

(defmacro %.str
  "converts to an output string"
  {:added "4.0"}
  [form]
  (emit-str form (merge {:lang :xtalk} (meta &form))))

(defn emit-as
  "helper function for emitting multiple forms"
  {:added "4.0"}
  [lang forms & [meta]]
  (->> forms
       (map (fn [form] (emit-str form (merge {:lang lang}
                                             meta))))
       (str/join "\n\n")))

(defn emit-symbol
  "emits string given symbol and grammar"
  {:added "4.0"}
  [lang sym & [mopts]]
  (let [[_ book] (emit-options-raw nil nil lang)
        {:keys [grammar]} book]
    (emit-common/emit-symbol sym grammar (merge {:layout :flat}
                                                mopts))))

(defn get-entry
  "gets an entry"
  {:added "4.0"}
  [library lang module id]
  (lib/get-entry library {:lang lang
                          :module module
                          :section :code
                          :id id}))

(defn emit-entry
  "emits an entry given parameters and options"
  {:added "4.0"}
  [{:keys [lang
           module
           id
           section]
    :or {section :code}}
   {:keys [library] :as meta}]
  (let [[stage grammar book namespace mopts] (emit-options meta)
        {:keys [snapshot]} mopts  
        module  (get-in snapshot [lang :book :modules module])
        entry   (get-in module [section id])]
    (entry/emit-entry grammar
                      entry
                      (assoc mopts :module module))))

(defn emit-entry-deps-collect
  "only collects the dependencies"
  {:added "4.0"}
  [{:keys [lang
           module
           id] :as ptr}
   {:keys [library] :as meta}]
  (let [[stage grammar book namespace mopts] (emit-options meta)]
    (first (deps/collect-script-entries book [(ut/sym-full ptr)]))))

(defn emit-entry-deps
  "emits only the entry deps"
  {:added "4.0"}
  [{:keys [lang
           module
           id] :as ptr}
   {:keys [library] :as meta}]
  (let [[stage grammar book namespace mopts] (emit-options meta)
        [deps] (deps/collect-script-entries book [(ut/sym-full ptr)])]
    (str/join "\n\n"
              (map #(emit-entry % mopts) deps))))

(defn emit-script-imports
  "emit imports"
  {:added "4.0"}
  [natives emit [stage grammar book namespace mopts :as input]]
  (if (not (-> emit :native :suppress))
    (let [imports-opts (update mopts :emit merge (:native emit))]
      
      (mapcat (fn [[name module]]
                (let [{:keys [bundle]} module 
                      form-bundled (if (not-empty bundle)
                                     (emit-script-imports bundle emit input))
                      form (deps/module-import-form book name module imports-opts)]
                  (if form
                    (concat form-bundled
                            [(emit-direct grammar
                                          (list 'do form)
                                          namespace
                                          imports-opts)]))))
              natives))))

(defn emit-script-deps
  "emits the script deps"
  {:added "4.0"}
  [entries emit [stage grammar book namespace mopts]]
  (let [deps-opts    (-> mopts
                         (update :emit merge (:code emit))
                         (dissoc :module))
        deps-arr     (keep (fn [entry]
                             (entry/emit-entry grammar entry deps-opts))
                           entries)]
    deps-arr))

(defn emit-script-join
  "joins the necessary parts of the script"
  {:added "4.0"}
  [imports-arr deps-arr body]
  (->> [(if (not-empty imports-arr)
          (str/join "\n" imports-arr))
        (if (not-empty deps-arr)
          (str/join "\n\n" deps-arr))
        body]
       (filter not-empty)
       (str/join "\n\n")))

(defn emit-script
  "emits a script with all dependencies"
  {:added "4.0"}
  [form
   {:keys [library
           lang
           emit] :as meta}]
  (let [[stage grammar book namespace mopts] (emit-options
                                              (update meta :emit merge {:type :script}))
        bulk (emit-bulk? form)
        [form deps natives]  (deps/collect-script book form mopts)
        imports-arr  (emit-script-imports natives emit [stage grammar book namespace mopts])
        deps-arr     (emit-script-deps deps emit [stage grammar book namespace mopts])
        body         (emit-direct grammar
                                  form
                                  namespace
                                  (-> mopts
                                      (update :emit
                                              merge
                                              (:body emit))
                                      (assoc :bulk bulk)))]
    (emit-script-join imports-arr deps-arr body)))

(defn emit-scaffold-raw-imports
  "gets only the scaffold imports"
  {:added "4.0"}
  [modules emit [stage grammar book namespace mopts] ]
  (let [natives      (deps/collect-script-natives modules {})]
    [natives (emit-script-imports natives emit [stage grammar book namespace mopts])]))

(defn emit-scaffold-raw
  "creates entries only for defglobal and defrun entries"
  {:added "4.0"}
  [transform
   module-id
   {:keys [library
           lang
           emit] :as meta}]
  (let [[stage grammar book namespace mopts] (emit-options meta)
        module-ids   (transform (h/deps:ordered book [module-id]))
        modules      (map (:modules book) module-ids)
        [_ imports-arr]  (emit-scaffold-raw-imports
                          modules emit
                          [stage grammar book namespace mopts] )
        globals      (vec (mapcat (fn [module]
                                   (keep (fn [{:keys [op] :as entry}]
                                           (if ('#{defrun defglobal} op)
                                             (ut/sym-full entry)))
                                         (vals (:code module))))
                                  modules))
        [deps]       (deps/collect-script-entries book globals)
        deps-arr     (emit-script-deps deps emit [stage grammar book namespace mopts])
        body         (emit-direct grammar
                                  (mapv (juxt h/strn identity)
                                        globals)
                                  namespace
                                  (-> mopts
                                      (update :emit merge (:body emit))))]
    (emit-script-join imports-arr deps-arr body)))

(defn emit-scaffold-for
  "creates scaffold for module and its deps"
  {:added "4.0"}
  [module-id meta]
  (emit-scaffold-raw identity module-id meta))

(defn emit-scaffold-to
  "creates scaffold up to module"
  {:added "4.0"}
  [module-id meta]
  (emit-scaffold-raw butlast module-id meta))

(defn emit-scaffold-imports
  "create scaggold to expose native imports"
  {:added "4.0"}
  [module-id {:keys [emit] :as meta}]
  (let [[stage grammar book namespace mopts] (emit-options meta)
        module-ids   (h/deps:ordered book [module-id])
        modules      (map (:modules book) module-ids)
        [natives imports-arr] (emit-scaffold-raw-imports
                               modules emit
                               [stage grammar book namespace mopts])]
    (str/join "\n" (conj (vec imports-arr)
                         (emit-direct grammar
                                      (list 'do:>
                                            (list 'return (h/postwalk (fn [x]
                                                                        (if (or (symbol? x)
                                                                                (keyword? x))
                                                                          (name x)
                                                                          x))
                                                                      natives)))
                                      namespace
                                      mopts)))))
