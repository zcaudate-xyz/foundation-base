(ns std.lang.base.impl-entry
  (:require [std.lang.base.util :as ut]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.book :as book]
            [std.lib :as h :refer [definvoke]]
            [std.string :as str]
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
    :or {namespace (h/ns-sym)}
    :as meta}]
  (merge
   {:lang lang
    :namespace namespace
    :module (or module namespace)
    :line line
    :time time}
   (h/qualified-keys meta [:static :rt :api])))

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
                                        (h/var-sym (resolve %))
                                        %)
                                     modifiers)))
                      sym)
         form-raw     (if (some symbol? modifiers)
                        (apply list op sym body)
                        form-raw)
         [tmeta form]  (if format
                         (format form-raw)
                         [nil form-raw])
         form-input (preprocess/to-input form)
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

(defn create-code-hydrate
  "hydrates the forms"
  {:added "4.0"}
  ([entry reserved grammar modules & [mopts]]
   (let [{:keys [hydrate hydrate-hook]} reserved
         {:keys [form-input]} entry
         [hmeta form-hydrate] (if hydrate
                                (hydrate form-input grammar mopts)
                                [nil form-input])
         module (assoc (get modules (:module entry))
                       :display :brief)
         [form deps] (preprocess/to-staging form-hydrate
                                            grammar
                                            modules
                                            (merge {:module module
                                                    :entry (assoc entry :display :brief)}
                                                   mopts))
         
         entry (merge (assoc entry
                             :form form
                             :deps deps)
                      hmeta
                      *extra*)
         entry (cond-> entry
                 (:static/template entry) (assoc :static/template.cache (atom {})))
         _     (if hydrate-hook (hydrate-hook entry))]
     entry)))

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
                          (h/qualified-keys meta :rt))))

(defn create-macro
  "creates a macro"
  {:added "4.0"}
  [[_ sym & body] {return :-
                   :keys [standalone] :as meta}]
  (let [form (apply list 'fn body)
        template (eval (apply list 'fn body))]
    (book/book-entry (merge {:op 'defmacro
                             :id sym
                             :form form
                             :template template
                             :section :fragment
                             :standalone standalone
                             :static/return return}
                            (create-common meta)
                            (h/qualified-keys meta :rt)))))


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
   (let [{:keys [form]}  entry
         form (if (:transform emit)
                ((:transform emit) form mopts)
                form)
         mopts (assoc mopts :entry (assoc entry :display :brief))]
     (binding [preprocess/*macro-opts* mopts
               preprocess/*macro-grammar* grammar]
       (try
         (emit/emit form grammar
                    (:namespace entry)
                    mopts)
         (catch Throwable t
           (h/p   (str (:module entry) " - [" (:line entry) "] - "
                       (:id entry) "\n" (ansi/red (h/date))))
           (h/pp  form)
           (throw t)))))))

(def +cached-emit-keys+
  [:transform
   :label
   :trim])

(def +cached-keys+
  [:layout :emit])

(definvoke emit-entry-cached
  "emits using a potentially cached entry"
  {:added "4.0"}
  [:recent {:key (fn [{:keys [entry mopts]}]
                   [(ut/sym-full entry) (select-keys mopts +cached-keys+)])
            :compare (fn [{:keys [grammar entry mopts]}]
                       [(:lang mopts)
                        (h/hash-id grammar)
                        (h/hash-id entry)
                        (select-keys mopts +cached-keys+)
                        (h/qualified-keys mopts :lang)])}]
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
                           (let [{:keys [lang module]} entry]
                             (assoc mopts :module (get-in snapshot [lang :book :modules module]))))
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
           body (cond->> body
                  label (str (emit-entry-label grammar entry) "\n")
                  trim  (trim))]
       body))))
