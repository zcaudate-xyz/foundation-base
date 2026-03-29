(ns std.lang.base.util
  (:require [clojure.string]
            [std.lib.collection :as collection]
            [std.lib.context.pointer :as ptr]
            [std.lib.context.space :as space]
            [std.lib.env :as env]
            [std.lib.foundation :as f]))

;;
;; SYMBOL
;;

(defn sym-id
  "gets the symbol id"
  {:added "3.0"}
  ([id]
   (symbol (name id))))

(defn sym-module
  "gets the symbol namespace"
  {:added "3.0"}
  ([id]
   (if-let [s (namespace id)]
     (symbol s))))

(defn sym-pair
  "gets the symbol pair
 
   (sym-pair 'L.core/identity)
   => '[L.core identity]"
  {:added "3.0"}
  ([id]
   [(sym-module id)
    (symbol (name id))]))

(defn sym-full
  "creates a full symbol"
  {:added "3.0"}
  ([{:keys [module id]}]
   (if (and module id)
     (sym-full module id)))
  ([module id]
   (symbol (name module) (name id))))

(defn sym-default-str
  "default fast symbol conversion"
  {:added "4.0"}
  [sym]
  (clojure.string/replace (f/strn sym) "-" "_"))

(defn sym-default-inverse-str
  "inverses the symbol string"
  {:added "4.0"}
  [sym]
  (clojure.string/replace (f/strn sym) "_" "-"))

(defn hashvec?
  "checks for hash vec"
  {:added "4.0"}
  ([x]
   (and (set? x)
        (= 1 (count x))
        (vector? (first x)))))

(defn doublevec?
  "checks for double vec"
  {:added "4.0"}
  ([x]
   (and (vector? x)
        (= 1 (count x))
        (vector? (first x)))))


;;
;; Context
;;

(defn lang-context
  "creates the lang context"
  {:added "4.0"}
  ([lang]
   (if lang 
     (keyword "lang" (name lang))
     (f/error "No Lang Input" {:input lang}))))

(defn lang-rt-list
  "lists rt in a namespace"
  {:added "4.0"}
  ([]
   (lang-rt-list (env/ns-sym)))
  ([ns]
   (let [space (space/space ns)]
     (keep (fn [k]
             
             (if (= 'lang (sym-module k))
               (keyword (name k))))
           (space/space:context-list space)))))

(defn lang-rt
  "getn the runtime contexts in a map"
  {:added "4.0"}
  ([]
   (collection/map-juxt [identity
                lang-rt]
               (lang-rt-list)))
  ([lang]
   (space/space:rt-current (lang-context lang)))
  ([ns lang]
   (space/space:rt-current ns (lang-context lang))))

(defn lang-rt-default
  "gets the default runtime function"
  {:added "4.0"}
  [ptr]
  (let [ns (env/ns-sym)
        active (set (lang-rt-list ns))
        {:keys [module lang]} ptr]
    (or (if (active lang) (lang-rt lang))
        (let [rts (map (fn [lang] (lang-rt ns lang)) active)]
          (or (first (filter (fn [rt] (get-in rt [:module/primary module]))
                             rts))
              (first (filter (fn [rt] (get-in rt [:module/internal module]))
                             rts))))
        (space/space:rt-current ns (:context ptr)))))

(defn lang-pointer
  "creates a lang pointer"
  {:added "4.0"}
  ([lang]
   (lang-pointer lang {}))
  ([lang {:keys [module id] :as m}]
   (let [ctx (lang-context lang)]
     (ptr/pointer (assoc m
                       :module module :lang lang
                       :context ctx
                       :context/fn #'lang-rt-default)))))

(defn module-id
  "gets the module id from a module symbol or map"
  {:added "4.1"}
  [module]
  (cond (map? module)
        (:id module)

        :else
        module))

(defn entry-summary
  "returns a concise summary for an entry"
  {:added "4.1"}
  [entry]
  (cond-> (select-keys entry [:lang :module :namespace :id :section :line :op :op-key])
    (and (:module entry)
         (:id entry))
    (assoc :symbol (sym-full entry))))

(defn error-with-context
  "wraps an exception with std.lang generation context"
  {:added "4.1"}
  [message data ^Throwable t]
  (let [cause-data (ex-data t)
        wrapped?   (:std.lang/wrapped cause-data)
        data       (cond-> (merge cause-data data)
                     true
                     (assoc :std.lang/wrapped true
                            :std.lang/cause-class (.getName (class t))
                            :std.lang/cause-message (.getMessage t))

                     (and cause-data
                          (not wrapped?))
                     (assoc :std.lang/cause-data cause-data))]
    (ex-info (if-let [cause-message (.getMessage t)]
               (str message ": " cause-message)
               message)
             data
             t)))

(defn throw-with-context
  "throws an exception wrapped with std.lang generation context"
  {:added "4.1"}
  [message data ^Throwable t]
  (throw (error-with-context message data t)))
