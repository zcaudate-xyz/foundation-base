(ns std.lang.base.workspace
  (:require [std.protocol.context :as protocol.context]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.impl :as impl]
            [std.lang.base.impl-entry :as impl-entry]
            [std.lang.base.impl-lifecycle :as lifecycle]
            [std.lang.base.runtime :as rt]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.util :as ut]
            [std.lang.interface.type-shared :as shared]
            [std.lang.base.script-control :as script-control]
            [std.task.process :as process]
            [std.lib :as h]))

(defn rt-resolve
  "resolves an rt given keyword"
  {:added "4.0"}
  [lang-or-rt]
  (if (keyword? lang-or-rt)
    (ut/lang-rt lang-or-rt)
    lang-or-rt))

(defn sym-entry
  "gets the entry using a symbol"
  {:added "4.0"}
  [lang sym]
  (let [[sym-module sym-id] (ut/sym-pair sym)]
    (lib/get-entry (impl/runtime-library)
                   {:lang lang
                    :module sym-module
                    :section :code
                    :id sym-id})))

(defn sym-pointer
  "converts to a pointer map"
  {:added "4.0"}
  [lang sym]
  (let [[sym-module sym-id] (ut/sym-pair sym)]
    {:lang lang
     :module sym-module
     :section :code
     :id sym-id}))

(defn module-entries
  "gets all module entries
 
   (w/module-entries :xtalk 'xt.lang.base-lib identity)
   => coll?"
  {:added "4.0"}
  [lang ns pred]
  (->> (lib/get-module (impl/runtime-library)
                       lang
                       ns)
       :code
       vals
       (filter pred)
       (sort-by (fn [e]
                  (mapv #(get e %) [:line :time])))))

(defn emit-ptr
  "emits the poiner as a string"
  {:added "4.0"}
  ([ptr & [opts]]
   (impl-entry/with:cache-force
    (ptr/ptr-display ptr (or opts
                             (ut/lang-rt-default ptr))))))

(defn ptr-display-str
  "copies pointer text to clipboard"
  {:added "4.0"}
  [ptr]
  (impl-entry/with:cache-force
   (ptr/ptr-display ptr (ut/lang-rt-default ptr))))

(defn ptr-clip
  "copies pointer text to clipboard"
  {:added "4.0"}
  [ptr]
  (h/clip:nil (ptr-display-str ptr)))

(defn ptr-print
  "copies pointer text to clipboard"
  {:added "4.0"}
  [ptr]
  (impl-entry/with:cache-force
   (h/p)
   (h/p (h/pl-add-lines (ptr-display-str ptr)))))

(defn ptr-setup
  "calls setup on a pointer"
  {:added "4.0"}
  [ptr]
  (h/p:rt-setup-ptr (ut/lang-rt-default ptr)
                    ptr))

(defn ptr-setup
  "calls setup on a pointer"
  {:added "4.0"}
  [ptr]
  (h/p:rt-setup-ptr (ut/lang-rt-default ptr)
                    ptr))

(defn ptr-teardown
  "calls teardown on a pointer"
  {:added "4.0"}
  [ptr]
  (h/p:rt-teardown-ptr (ut/lang-rt-default ptr)
                       ptr))

(defn ptr-setup-deps
  "calls setup on a pointer and all dependencies"
  {:added "4.0"}
  [ptr]
  (let [deps (->> (h/deps:ordered (lib/get-book (impl/runtime-library)
                                                (:lang ptr))
                                  [(ut/sym-full ptr)])
                  (map #(sym-entry (:lang ptr) %))
                  (filter #(-> % :op (= 'defn))))]
    (doseq [p deps]
      (ptr-setup p))))

(defn ptr-teardown-deps
  "calls teardown on pointer all dependencies"
  {:added "4.0"}
  [ptr]
  (let [deps (->> (h/deps:ordered (lib/get-book (impl/runtime-library)
                                                (:lang ptr))
                                  [(ut/sym-full ptr)])
                  (map #(sym-entry (:lang ptr) %))
                  (filter #(-> % :op-key (= 'defn))))]
    (doseq [p deps]
      (ptr-teardown p))))

(defn emit-module
  "emits the entire module"
  {:added "4.0"}
  ([]
   (h/map-juxt [identity emit-module] (ut/lang-rt-list)))
  ([lang-or-rt]
   (emit-module lang-or-rt nil))
  ([lang-or-rt module-id]
   (let [rt (rt-resolve lang-or-rt)]
     (rt/default-setup-module-emit rt
                                   (or module-id (:module rt))))))

(defn print-module
  "emits and prints out the module
 
   (std.print/with-out-str
     (w/print-module (l/rt 'xt.lang.base-lib :xtalk)))
   => string?"
  {:added "4.0"}
  ([]
   (h/map-juxt [identity print-module] (ut/lang-rt-list)))
  ([lang-or-rt]
   (h/pl (emit-module lang-or-rt))))

(defn rt:module
  "gets the book module for a runtime"
  {:added "4.0"}
  ([lang-or-rt]
   (let [rt (rt-resolve lang-or-rt)]
     (lib/get-module (impl/runtime-library)
                     (:lang rt)
                     (:module rt)))))

(defn rt:module-meta
  "gets the book module for a runtime"
  {:added "4.0"}
  ([lang-or-rt]
   (let [rt (rt-resolve lang-or-rt)]
     (dissoc (lib/get-module (impl/runtime-library)
                             (:lang rt)
                             (:module rt))
             :fragment
             :code))))

(defn rt:module-purge
  "purges the current workspace"
  {:added "4.0"}
  ([]
   (h/map-juxt [identity rt:module-purge] (ut/lang-rt-list)))
  ([lang-or-rt]
   (let [{:keys [lang module]
          :as rt} (rt-resolve lang-or-rt)]
     (lib/delete-module! (impl/runtime-library)
                         lang
                         module))))

;;
;;
;;

(defn rt:inner
  "gets the inner client for a shared runtime"
  {:added "4.0"}
  ([]
   (h/map-juxt [identity rt:inner] (ut/lang-rt-list)))
  ([lang & [ns]]
   (shared/rt-get-inner (ut/lang-rt ns lang))))

(defn rt:restart
  "restarts the shared runtime"
  {:added "4.0"}
  ([]
   (h/map-juxt [identity
                rt:restart]
               (ut/lang-rt-list)))
  ([lang & [ns]]
   (let [rt (ut/lang-rt ns lang)]
     (if (shared/rt-is-shared? rt)
       (shared/restart-group-instance (-> rt :client :type)
                                      (-> rt :id))
       (script-control/script-rt-restart lang ns)))))

(defn- multistage-tmpl
  [[sym f]]
  (h/$ (defn ~sym
         ([lang]
          (let [rt (ut/lang-rt lang)]
            (~sym rt (:module rt))))
         ([rt module-id]
          (~f rt module-id)))))

(h/template-entries [multistage-tmpl]
  [[rt:setup-single    h/p:rt-setup-module]
   [rt:teardown-single h/p:rt-teardown-module]
   [rt:setup-to  rt/multistage-setup-to]
   [rt:setup rt/multistage-setup-for]
   [rt:teardown  rt/multistage-teardown-for]
   [rt:teardown-to  rt/multistage-teardown-to]
   [rt:teardown-at  rt/multistage-teardown-at]
   [rt:scaffold-to rt/default-scaffold-setup-to]
   [rt:scaffold rt/default-scaffold-setup-for]
   [rt:scaffold-imports rt/default-scaffold-imports]])

(defn intern-macros
  "interns all macros from one namespace to another"
  {:added "4.0"}
  [lang ns & [module-id library merge-op]]
  (let [library (or library (impl/default-library))
        [to from] (if (vector? ns)
                    ns
                    [(h/ns-sym) ns])
        imports (lib/wait-mutate!
                 library
                 (fn [snap]
                   (let [book (snap/get-book-raw snap lang)
                         imports (->> (get-in book [:modules from :fragment])
                                      (h/map-vals (fn [e]
                                                    (assoc e :module (or module-id to)))))
                         new-book (update-in book [:modules (or module-id to) :fragment]
                                             (fn [m]
                                               ((or merge-op (fn [_ new] new))
                                                m imports)))]
                     [imports (snap/add-book snap new-book)])))]
    (h/map-vals
     (fn [e]
       (let [src-var  (resolve (symbol (str ns) (str (:id e))))]
         (intern to
                 (with-meta (:id e)
                   (merge (meta src-var)
                          (select-keys e [:namespace :line]))
                   #_(assoc 
                      #_#_:arglists (list (second (:form e)))))
                 (ut/lang-pointer lang (select-keys e [:lang :id :module :section])))))
     imports)))
