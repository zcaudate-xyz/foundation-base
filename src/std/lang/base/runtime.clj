(ns std.lang.base.runtime
  (:require [std.protocol.context :as protocol.context]
            [std.protocol.component :as protocol.component]
            [std.lang.base.runtime-proxy :as proxy]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.impl :as impl]
            [std.lang.base.impl-deps :as deps]
            [std.lang.base.impl-lifecycle :as lifecycle]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.util :as ut]
            [std.json :as json]
            [std.lib :as h :refer [defimpl]]
            [std.lib.context.registry :as reg]
            [std.string :as str]))

;;
;; Default Runtime
;;

(defn default-tags-ptr
  "runtime default args"
  {:added "4.0"}
  ([rt ptr]
   (ptr/ptr-tag ptr (:runtime rt))))

(defn default-deref-ptr
  "runtime default deref"
  {:added "4.0"}
  ([{:keys [library] :as rt} ptr]
   (ptr/ptr-deref (assoc ptr :library library))))

(defn default-invoke-ptr
  "runtime default invoke"
  {:added "4.0"}
  ([{:keys [id library layout emit] :as rt} ptr args]
   (ptr/ptr-invoke-string ptr args
                          {:library library ;; override default library
                           :layout  layout  
                           :emit    (merge emit
                                           {:transform (fn [args {:keys [bulk] :as mopts}]
                                                         (if bulk
                                                           (apply list 'do args)
                                                           args))
                                            :runtime
                                            (assoc rt
                                                   :type (:runtime rt)
                                                   :namespace (h/ns-sym))})})))

(defn default-init-ptr
  "will init pointer if there is a :rt/init key"
  {:added "4.0"}
  [{:keys [id lang library layout module emit] :as rt} entry]
  (when (and (#{:defglobal :defrun} (:op-key entry))
             (:rt/init entry))
    (h/p:rt-invoke-ptr rt
                       (ut/lang-pointer lang
                                        {:module module})
                       [(:form-input entry)])))

(defn default-display-ptr
  "runtime default display"
  {:added "4.0"}
  ([{:keys [lang library module emit] :as rt} ptr]
   (cond (#{:defglobal} (:op-key @ptr))
         (let [body (impl/emit-as lang [(ut/sym-full ptr)] {:layout :full
                                                            :emit emit})]
           (ptr/ptr-invoke rt
                           h/p:rt-raw-eval
                           body
                           (:main emit)
                           (or (:json emit) :full)))
         
         :else
         (ptr/ptr-display ptr {:library library
                               :emit emit}))))

(def default-raw-eval
  (fn [_ string] string))

(def default-transform-in-ptr
  (fn [_ _ args] args))

(def default-transform-out-ptr
  (fn [_ _ return] return))

(defn- rt-default-string [{:keys [lang]}]
  (str "#rt:lang" [lang]))

(defimpl RuntimeDefault [lang redirect]
  :string rt-default-string
  :protocols
  [protocol.context/IContext
   :body
   {-raw-eval      (if redirect
                     (proxy/proxy-raw-eval rt string)
                     (default-raw-eval rt string))
    -init-ptr      (if redirect
                     (proxy/proxy-init-ptr rt ptr)
                     (default-init-ptr rt ptr))
    -tags-ptr      (if redirect
                     (proxy/proxy-tags-ptr rt ptr)
                     (default-tags-ptr rt ptr))
    -deref-ptr     (if redirect
                     (proxy/proxy-deref-ptr rt ptr)
                     (default-deref-ptr rt ptr))
    -display-ptr   (if redirect
                     (proxy/proxy-display-ptr rt ptr)
                     (default-display-ptr rt ptr))
    -invoke-ptr    (if redirect
                     (proxy/proxy-invoke-ptr rt ptr args)
                     (default-invoke-ptr rt ptr args))
    -transform-in-ptr   (if redirect
                          (proxy/proxy-transform-in-ptr rt ptr args)
                          (default-transform-in-ptr rt ptr args))
    -transform-out-ptr  (if redirect
                          (proxy/proxy-transform-out-ptr rt ptr return)
                          (default-transform-out-ptr rt ptr return))}
   protocol.component/IComponent
   :body
   {-start  (if redirect
              (let [rt (proxy/proxy-get-rt redirect lang)
                    _   (doseq [[module shortcut] (:module/internal rt)]
                          (when (not= module redirect)
                            (h/suppress (require [module :as shortcut]))))]
                component)
              component)
    -stop    component
    -kill    component}
   
   protocol.component/IComponentQuery
   :body
   {-started?      (if redirect
                     (proxy/proxy-started? component)
                     true)
    -stopped?      (if redirect
                     (proxy/proxy-stopped? component)
                     true)
    -info          (if redirect
                     (proxy/proxy-info component level)
                     {})
    -remote?       (if redirect
                     (proxy/proxy-remote? component)
                     false)
    -health        (if redirect
                     (proxy/proxy-health component)
                     true)}])

(defn rt-default
  "creates a lang runtime"
  {:added "4.0"}
  ([{:keys [lang runtime] :as m}]
   (map->RuntimeDefault (merge  m {:runtime :default}))))

(defn rt-default?
  "checks if object is default runtime"
  {:added "4.0"}
  ([obj]
   (instance? RuntimeDefault obj)))
  
(h/res:spec-add
 {:type :hara/lang.rt
  :config {:bootstrap false}
  :instance {:create rt-default}})

(defn rt-null
  "creates a default null runtime"
  {:added "4.0"}
  ([m]
   (rt-default (assoc m :lang :null))))

(h/res:spec-add
 {:type :hara/context.rt.null
  :instance {:create rt-null}})

(alter-var-root #'reg/+rt-null+
                (fn [_]
                  (rt-null reg/+null+)))

;;
;;
;;

(defn install-lang!
  "installs a language within `std.lib.context`"
  {:added "4.0"}
  [lang & [options]]
  (let [ctx   (ut/lang-context lang)]
    (h/p:registry-install
     ctx
     {:config {:context ctx :lang lang}
      :scratch (rt-default {:context ctx :lang lang
                            :options (or options
                                         {})})
      :rt  {:default {:resource :hara/lang.rt}}})))

(defn install-type!
  "installs a specific runtime type given `:lang`"
  {:added "4.0"}
  ([lang runtime {:keys [type config] :as spec}]
   (let [ctx    (ut/lang-context lang)
         r-spec (h/res:spec-add (dissoc spec :config))
         r-ctx  (h/p:registry-rt-add ctx {:key runtime
                                          :config config
                                          :resource type})]
     {:spec r-spec
      :context r-ctx})))

;;
;;
;;

(defn return-format-simple
  "format forms for return"
  {:added "4.0"}
  [forms]
  (concat (butlast forms)
          [(list 'return (last forms))]))

(defn return-format
  "standard format for return"
  {:added "4.0"}
  [forms & [opts]]
  (let [v (last forms)
        v (if (and (h/form? v)
                   ((h/union '#{:- := var return break throw}
                             opts)
                    (first v)))
            v
            (list 'return v))]
    (concat (butlast forms) [v])))

(defn return-wrap-invoke
  "wraps forms to be invoked"
  {:added "4.0"}
  [forms]
  (h/$ ('((fn [] ~@forms)))))

(defn return-transform
  "standard return transform"
  {:added "4.0"}
  [input {:keys [bulk] :as mopts} & [{:keys [wrap-fn
                                             format-fn]}]]
  (let [forms (if bulk input [input])
        wrap-fn   (or wrap-fn
                      return-wrap-invoke)
        format-fn (or format-fn
                      return-format)]
    (wrap-fn (format-fn forms))))

(defn default-invoke-script
  "default invoke script call used by most runtimes"
  {:added "4.0"}
  ([{:keys [id lang library layout] :as rt} ptr args f {:keys [json main emit]
                                                        :or {json :full}
                                                        :as params}]
   (let [emit   (h/merge-nested emit (:emit rt))
         emit   (assoc emit
                       :input   {:pointer ptr
                                 :args args}
                       :runtime (assoc rt
                                       :type (:runtime rt)
                                       :namespace (h/ns-sym)))
         _      (if common/*trace* (h/prn emit params))
         body   (ptr/ptr-invoke-script ptr args {:emit emit
                                                 :lang lang
                                                 :layout (or (:layout params)
                                                             layout)})]
     (ptr/ptr-invoke rt
                     f
                     body
                     main
                     json))))

;;
;; LIFECYCLE
;;

(defn default-lifecycle-prep
  "prepares mopts for lifecycle"
  {:added "4.0"}
  [{:keys [id layout lang module library lifecycle emit] :as rt}]
  (let [library  (or library (impl/runtime-library))
        snapshot (lib/get-snapshot library)
        book     (snap/get-book snapshot lang)
        module   (get-in book [:modules module])]
    (merge {:layout layout
            :book book
            :lang lang
            :snapshot snapshot
            :module module
            :emit (assoc emit
                         :runtime
                         {:id id
                          :lang   (:lang rt)
                          :type   (:runtime rt)
                          :module (:module rt)
                          :namespace (h/ns-sym)})}
           lifecycle)))


;;
;; SCAFFOLD
;;

(defn- default-scaffold-array
  [rt arr]
  (reduce  (fn [acc [k body]]
             (try
               (conj acc [k (ptr/ptr-invoke rt
                                            h/p:rt-raw-eval
                                            body
                                            (:main meta)
                                            (:json meta))])
               (catch Throwable t
                 (reduced (conj acc [k body (.getMessage t)])))))
           []
           arr))

(defn default-scaffold-setup-for
  "setup native modules, defglobals and defruns in the runtime"
  {:added "4.0"}
  [rt module-id]
  (let [meta (default-lifecycle-prep rt)
        body (impl/emit-scaffold-for module-id meta)]
    (ptr/ptr-invoke rt
                    h/p:rt-raw-eval
                    body
                    (:main meta)
                    (:json meta))))

(defn default-scaffold-setup-to
  "setup scaffold up to but not including the current module in the runtime"
  {:added "4.0"}
  [rt module-id]
  (let [meta (default-lifecycle-prep rt)
        body (impl/emit-scaffold-to module-id meta)]
    (ptr/ptr-invoke rt
                    h/p:rt-raw-eval
                    body
                    (:main meta)
                    (:json meta))))

(defn default-scaffold-imports
  "embed native imports to be globally accessible"
  {:added "4.0"}
  [rt module-id]
  (let [meta (default-lifecycle-prep rt)
        body (impl/emit-scaffold-imports module-id meta)]
    (ptr/ptr-invoke rt
                    h/p:rt-raw-eval
                    body
                    (:main meta)
                    (:json meta))))

(defn default-lifecycle-fn
  "constructs a lifecycle fn"
  {:added "4.0"}
  [f]
  (fn [rt & args]
    (let [{:keys [book] :as meta} (default-lifecycle-prep rt)
          form (apply f book args)]
      
      (if form
        (let [body (impl/emit-str form meta)]
          (ptr/ptr-invoke rt
                          h/p:rt-raw-eval
                          body
                          (:main meta)
                          (:json meta)))))))


(def ^{:arglists '([rt module-id])}
  default-has-module?
  (default-lifecycle-fn deps/has-module-form))

(def ^{:arglists '([rt ptr])}
  default-has-ptr?
  (default-lifecycle-fn deps/has-ptr-form))

(def ^{:arglists '([rt ptr])}
  default-setup-ptr
  (default-lifecycle-fn deps/setup-ptr-form))

(def ^{:arglists '([rt ptr])}
  default-teardown-ptr
  (default-lifecycle-fn deps/teardown-ptr-form))

(defn default-setup-module-emit
  "emits the string for the module"
  {:added "4.0"}
  [rt module-id]
  (let [{:keys [book] :as meta
         :rt/keys [setup]} (default-lifecycle-prep rt)]
    (lifecycle/emit-module-setup module-id (update meta :emit h/merge-nested-new setup))))

(defn default-setup-module-basic
  "basic setup module action"
  {:added "4.0"}
  [rt module-id]
  (let [{:keys [book] :as meta
         :rt/keys [setup]} (default-lifecycle-prep rt)
        body (lifecycle/emit-module-setup module-id (update meta :emit h/merge-nested-new setup))]
    (ptr/ptr-invoke rt
                    h/p:rt-raw-eval
                    body
                    (:main meta)
                    (:json meta))))

(defn default-teardown-module-basic
  "basic teardown module action"
  {:added "4.0"}
  [rt module-id]
  (let [{:keys [teardown] :as meta
         :rt/keys [teardown]} (default-lifecycle-prep rt)
        body (lifecycle/emit-module-teardown module-id (update meta :emit h/merge-nested-new teardown))]
    (ptr/ptr-invoke rt
                    h/p:rt-raw-eval
                    body
                    (:main meta)
                    (:json meta))))

(defn default-setup-module
  "default setup module (with error isolation)"
  {:added "4.0"}
  [rt module-id & [meta]]
  (let [{:keys [book] :as meta
         :rt/keys [setup]} (or meta (default-lifecycle-prep rt))
        raw  (lifecycle/emit-module-setup-raw module-id (update meta :emit h/merge-nested-new setup))
        body (lifecycle/emit-module-setup-join raw)]
    (if (not-empty body)
      (try (ptr/ptr-invoke rt h/p:rt-raw-eval
                           body
                           (:main meta)
                           (:json meta))
           (catch Throwable t
             (let [arr (lifecycle/emit-module-setup-concat raw)]
               (mapv (fn [part]
                       (try
                         (ptr/ptr-invoke rt h/p:rt-raw-eval
                                         part
                                         (:main meta)
                                         (:json meta))
                         (catch Throwable t
                           (throw (ex-info "Error at:" {:part (vec (str/split-lines part))
                                                        :message (loop [t t
                                                                        c (.getCause ^Throwable t)]
                                                                   (if c
                                                                     (recur c (.getCause ^Throwable c))
                                                                     (.getMessage ^Throwable t)))})))))
                     arr)))))))

(defn default-teardown-module
  "default teardown module (with error isolation)"
  {:added "4.0"}
  [rt module-id & [meta]]
  (let [{:keys [book] :as meta
         :rt/keys [teardown]} (or meta (default-lifecycle-prep rt))
        raw  (lifecycle/emit-module-teardown-raw module-id (update meta :emit h/merge-nested-new teardown))
        body (lifecycle/emit-module-teardown-join raw)]
    (if (not-empty body)
      (try (ptr/ptr-invoke rt h/p:rt-raw-eval
                           body
                           (:main meta)
                           (:json meta))
           (catch Throwable t
             (let [arr (lifecycle/emit-module-teardown-concat raw)]
               (mapv (fn [part]
                       (try
                         (ptr/ptr-invoke rt h/p:rt-raw-eval
                                         part
                                         (:main meta)
                                         (:json meta))
                         (catch Throwable t
                           (throw (ex-info "Error at:" {:part (vec (str/split-lines part))
                                                        :message (loop [t t
                                                                        c (.getCause ^Throwable t)]
                                                                   (if c
                                                                     (recur c (.getCause ^Throwable c))
                                                                     (.getMessage ^Throwable t)))})))))
                     arr)))))))

(defn multistage-invoke
  "invokes a multistage pipeline given deps function"
  {:added "4.0"}
  [rt module-id module-fn deps-fn]
  (let [{:keys [book] :as meta} (default-lifecycle-prep rt)
        module-ids (deps-fn book module-id)]
    (mapv (fn [module-id]
            [module-id (module-fn rt module-id meta)])
          module-ids)))

(defn multistage-setup-for
  "setup for a given namespace"
  {:added "4.0"}
  [rt module-id]
  (multistage-invoke rt module-id default-setup-module
                     (fn [book module-id]
                       (h/deps:ordered book [module-id]))))

(defn multistage-setup-to
  "setup to a given namespcase"
  {:added "4.0"}
  [rt module-id]
  (multistage-invoke rt module-id default-setup-module
                     (fn [book module-id]
                       (butlast (h/deps:ordered book [module-id])))))

(defn multistage-teardown-for
  "teardown for a given namespace"
  {:added "4.0"}
  [rt module-id]
  (multistage-invoke rt module-id default-teardown-module
                     (fn [book module-id]
                       (reverse (h/deps:ordered book [module-id])))))

(defn multistage-teardown-at
  "teardown all dependents including this"
  {:added "4.0"}
  [rt module-id]
  (multistage-invoke rt module-id default-teardown-module
                     (fn [book module-id]
                       (binding [std.lang.base.book/*dep-types* :module]
                         (h/dependents:ordered book module-id)))))

(defn multistage-teardown-to
  "teardown all dependents upto this"
  {:added "4.0"}
  [rt module-id]
  (multistage-invoke rt module-id default-teardown-module
                     (fn [book module-id]
                       (binding [std.lang.base.book/*dep-types* :module]
                         (butlast (h/dependents:ordered book module-id))))))


(comment
  (./import)
  (./create-tests))
