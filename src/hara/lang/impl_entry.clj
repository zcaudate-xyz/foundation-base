(ns hara.lang.impl-entry
  (:require [hara.lang.book :as book]
             [hara.common.emit-template :as impl-template]
             [hara.common.emit :as emit]
             [hara.common.emit-preprocess :as preprocess] [hara.common.preprocess-base :as preprocess-base]
             [hara.common.emit-rewrite :as rewrite]
             [hara.common.grammar-xtalk-system :as xtalk-system]
             [hara.common.provenance :as provenance]
             [hara.common.util :as ut]
             [std.lib.collection :as collection]
             [std.lib.env :as env]
             [std.lib.foundation :as f]
             [std.lib.invoke :as invoke]
            [std.print.ansi :as ansi]))

;;;;
;;
;; CREATE ENTRY
;;

(def ^:dynamic *extra* {})

(defn create-common
  "create entry common keys from metadata"
  {:added "4.0"}
  [{:keys [lang namespace module line time]
    :or {namespace (env/ns-sym)}
    :as meta}]
  (merge
   {:lang lang
    :namespace namespace
    :module (or module namespace)
    :line line
    :time time}
   (collection/qualified-keys meta [:static :rt :api])))

(defn create-code-raw
  "creates a raw entry compatible with submit"
  {:added "4.0"}
  ([[op sym & body :as form-raw] reserved meta]
   (let [{:keys [section priority format format-hook]
          :or {priority 5}} reserved
         modifiers  (:- (clojure.core/meta sym))
         sym        (if (some symbol? modifiers)
                      (with-meta sym
                        (assoc (clojure.core/meta sym)
                               :-
                               (mapv #(if (symbol? %)
                                        (if-let [v (resolve %)]
                                          (f/var-sym v)
                                          %)
                                         %)
                                      modifiers)))
                       sym)
         form-raw     (if (some symbol? modifiers)
                        (apply list op sym body)
                        form-raw)
         [tmeta form]  (if format
                         (format form-raw)
                         [nil form-raw])
         form-input (-> form
                        preprocess/to-input
                        preprocess/eval-template-forms)
         entry (book/book-entry (merge {:op op
                                        :op-key (:op reserved)
                                        :id (cond (vector? sym)
                                                  (first (filter symbol? sym))
                                                  
                                                  :else sym)
                                        :form-input form-input
                                        :section (or section :code)
                                        :priority priority}
                                       tmeta
                                       (create-common meta)))]
     (if format-hook (format-hook entry))
     [tmeta entry])))

(defn create-code-base
  "creates the base code entry"
  {:added "4.0"}
  ([[op sym & body :as form] meta grammar]
   (let [reserved (get-in grammar [:reserved op])]
     (second (create-code-raw form reserved meta)))))

(defn hydrate-form
  "hydrates a raw input form using the reserved hydrate hook"
  {:added "4.1"}
  [form-input reserved grammar & [mopts]]
  (let [{:keys [hydrate]} reserved]
    (if hydrate
      (hydrate form-input grammar mopts)
      [nil form-input])))

(defn create-code-hydrate
  "hydrates the forms"
  {:added "4.0"}
  ([entry reserved grammar modules & [mopts]]
    (let [{:keys [hydrate-hook]} reserved
          {:keys [hmeta
                  form
                  deps
                  deps-fragment
                  deps-native
                  xtalk-ops
                  xtalk-profiles
                  polyfill-modules]}
           (impl-template/create-code-state entry
                                            reserved
                                            grammar
                                           modules
                                           mopts)
         entry (merge (assoc entry
                              :form form
                              :deps deps
                              :deps-fragment deps-fragment
                              :deps-native   deps-native
                              :xtalk-ops xtalk-ops
                              :xtalk-profiles xtalk-profiles
                              :polyfill-modules polyfill-modules)
                        hmeta
                        *extra*)
          entry (cond-> entry
                  (not (:static/code.cache entry))
                  (assoc :static/code.cache (atom {})))
           entry (if hydrate-hook
                   (or (hydrate-hook entry) entry)
                   entry)]
       entry)))

(defn prepare-code-entry
  "prepares a raw code entry for storage without eager hydration"
  {:added "4.1"}
  [entry]
  (let [entry (merge entry *extra*)]
    (cond-> entry
      (not (:static/code.cache entry))
      (assoc :static/code.cache (atom {})))))

(defn create-code
  "creates the code entry"
  {:added "4.0"}
  ([form meta book]
   (create-code form meta book {}))
  ([form meta book mopts]
   (let [{:keys [modules]} book]
     (create-code form meta
                  (:grammar book)
                  (:modules book)
                  (merge {:module (-> modules
                                      (get (:module meta))
                                      (assoc :display :brief))}
                         mopts))))
  ([[op sym & body :as form] meta grammar modules mopts]
   (let [reserved (get-in grammar [:reserved op])]
     (-> form
         (create-code-raw reserved meta)
         second
         (create-code-hydrate reserved grammar modules mopts)))))

(defn create-fragment
  "creates a fragment"
  {:added "4.0"}
  [[_ sym value] {return :-
                  :as meta}]
  (book/book-entry (merge {:op 'def$
                           :id sym
                           :form value
                           :section :fragment
                           :static/return return}
                          (create-common meta)
                          (collection/qualified-keys meta :rt))))

(defn create-fragment-hydrate
  "hydrates the forms"
  {:added "4.0"}
  ([entry grammar modules mopts]
   (if (:template entry)
     (let [deps-native  (preprocess/find-natives entry
                                                 mopts)]
       (assoc entry
              :deps #{}
              :deps-fragment #{}
              :deps-native   deps-native))
     (let [{:keys [form]} entry
           [_
            deps
            deps-fragment
            deps-native]   (preprocess/to-staging form
                                                  grammar
                                                  modules
                                                  mopts)
           {:keys [ops profiles polyfill-modules]} (xtalk-system/scan-xtalk form grammar)]
       (assoc entry
              :deps #{}
              :deps-fragment deps-fragment
              :deps-native   deps-native
              :xtalk-ops ops
              :xtalk-profiles profiles
              :polyfill-modules polyfill-modules)))))

(defn create-macro
  "creates a macro"
  {:added "4.0"}
  [[_ sym & body] {return :-
                    :keys [standalone] :as meta}]
  (let [form (apply list 'fn body)
        template (eval form)]
    (book/book-entry (merge {:op 'defmacro
                             :id sym
                             :form form
                             :template template
                             :section :fragment
                             :standalone standalone
                             :static/return return}
                            (create-common meta)
                            (collection/qualified-keys meta :rt)))))


;;;;
;;
;; EMIT ENTRY
;;
;; This is the major workhorse function for
;; most of the library and optimisations should
;; start from here because it's used everywhere
;;
;; Methods for generation of code from entries
;; - single entry (for use in modules)
;; - entry and all dependencies (with native imports and declarations at the top)
;;    - layout can be specified
;;

(def ^:dynamic *cache-none* false)

(def ^:dynamic *cache-force* false)

(defmacro with:cache-none
  "skips the cache"
  {:added "4.0"}
  [& body]
  `(binding [*cache-none* true]
     ~@body))

(defmacro with:cache-force
  "forces the cache to update"
  {:added "4.0"}
  [& body]
  `(binding [*cache-force* true]
     ~@body))

(defn emit-entry-raw
  "emits using the raw entry"
  {:added "4.0"}
  ([grammar entry {:keys [emit]
                    :as mopts}]
    (assert entry "Entry cannot be null")
    (let [lang    (or (:lang mopts) (:lang entry))
          book    (or (:book mopts)
                      (when-let [snapshot (:snapshot mopts)]
                        (book/book-from snapshot lang)))
          modules (or (:modules mopts)
                      (:modules book)
                      (get-in mopts [:snapshot lang :book :modules]))
          reserved (get-in grammar [:reserved (:op entry)])
          entry   (if (and reserved
                           modules)
                    (impl-template/materialize-code-entry {:grammar grammar
                                                           :modules modules
                                                           :lang lang}
                                                          entry
                                                          (assoc mopts :lang lang))
                    entry)
          {:keys [form]}  entry
          form (if (:transform emit)
                 ((:transform emit) form mopts)
                 form)
          form (if (= :python lang)
                 (rewrite/rewrite-stage :staging
                                        form
                                        grammar
                                        (assoc mopts :entry entry))
                 form)
          mopts (-> mopts
                    (assoc :entry (assoc entry :display :brief))
                    (provenance/with-provenance
                      {:hara/phase :emit/entry
                       :hara/subsystem :hara.lang.impl-entry/emit-entry-raw
                       :hara/form form
                       :hara/lang lang
                       :hara/module (:module entry)
                       :hara/namespace (:namespace entry)
                       :hara/entry (ut/entry-summary entry)}))]
      (binding [preprocess-base/*macro-opts* mopts
                preprocess-base/*macro-grammar* grammar]
        (try
         (emit/emit form grammar
                    (:namespace entry)
                    mopts)
          (catch Throwable t
            (env/p   (str (:module entry) " - [" (:line entry) "] - "
                        (:id entry) "\n" (ansi/red (f/date))))
            (env/pp  form)
            (ut/throw-with-context
             "hara.lang entry emit failed"
             (:hara/provenance mopts)
             t)))))))

(def +cached-emit-keys+
  [:transform
   :label
   :trim])

(def +cached-keys+
  [:layout :emit])

(invoke/definvoke emit-entry-cached
  "emits using a potentially cached entry"
  {:added "4.0"}
  [:recent {:key (fn [{:keys [entry mopts]}]
                   [(ut/sym-full entry) (select-keys mopts +cached-keys+)])
            :compare (fn [{:keys [grammar entry mopts]}]
                       [(:lang mopts)
                        (f/hash-id grammar)
                        (f/hash-id entry)
                        (select-keys mopts +cached-keys+)
                        (collection/qualified-keys mopts :lang)])}]
  ([{:keys [grammar entry mopts]}]
   (emit-entry-raw grammar entry mopts)))

(defn emit-entry-label
  "emits the entry label"
  {:added "4.0"}
  [grammar entry]
  (let [{:keys [prefix suffix]} (get-in grammar [:default :comment])]
    (str prefix " " (ut/sym-full entry) " [" (:line entry) "] " suffix)))

(defn emit-entry
  "emits a given entry"
  {:added "4.0"}
  ([grammar entry {:keys [snapshot module emit]
                   :as mopts}]
   (if (not (:suppress emit))
     (let [mopts  (or (and (not module)
                           snapshot
                           (let [{:keys [lang module]} entry
                                 book (book/book-from snapshot lang)]
                             (assoc mopts
                                    :book book
                                    :modules (:modules book)
                                    :module (get-in book [:modules module]))))
                      mopts)
            {:keys [label trim cache]} emit
             body (cond (or *cache-none*
                            (:static/no-cache entry)
                            (= cache :none))
                        (emit-entry-raw grammar entry mopts)
                      
                      :else
                      (binding [std.lib.invoke/*force* (or *cache-force* (= cache :force))]
                        (emit-entry-cached {:grammar grammar
                                            :entry   entry
                                            :mopts   mopts})))
           body (reduce (fn [body transform]
                          (transform body {:grammar grammar
                                           :entry   entry
                                           :mopts   mopts}))
                        body
                        (-> emit :code :transforms :entry))
           body (reduce (fn [body transform]
                          (transform body {:grammar grammar
                                           :entry   entry
                                           :mopts   mopts}))
                        body
                        (get-in snapshot [(:lang mopts) :book :meta :transforms :entry]))
           body (cond->> body
                  label (str (emit-entry-label grammar entry) "\n")
                  trim  (trim))]
       body))))

(comment
  (-> (hara.lang/get-snapshot
       (hara.lang/default-library))
      :js
      :book
      :meta)
  
  (:meta (hara.lang/grammar :js))
  
  )
