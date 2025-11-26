(ns std.lang.base.util
  (:require [std.lib :as h]
            [std.string :as str]))

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
  (str/replace (h/strn sym) "-" "_"))

(defn sym-default-inverse-str
  "inverses the symbol string"
  {:added "4.0"}
  [sym]
  (str/replace (h/strn sym) "_" "-"))

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
     (h/error "No Lang Input" {:input lang}))))

(defn lang-rt-list
  "lists rt in a namespace"
  {:added "4.0"}
  ([]
   (lang-rt-list (h/ns-sym)))
  ([ns]
   (let [space (h/p:space ns)]
     (keep (fn [k]
             
             (if (= 'lang (sym-module k))
               (keyword (name k))))
           (h/p:space-context-list space)))))

(defn lang-rt
  "getn the runtime contexts in a map"
  {:added "4.0"}
  ([]
   (h/map-juxt [identity
                lang-rt]
               (lang-rt-list)))
  ([lang]
   (h/p:space-rt-current (lang-context lang)))
  ([ns lang]
   (h/p:space-rt-current ns (lang-context lang))))

(defn rt-current
  "gets the current runtime"
  {:added "4.0"}
  ([lang]
   (rt-current lang (h/ns-sym)))
  ([lang ns]
   (lang-rt ns lang)))

(defn lang-rt-default
  "gets the default runtime function"
  {:added "4.0"}
  [ptr]
  (let [ns (h/ns-sym)
        active (set (lang-rt-list ns))
        {:keys [module lang]} ptr]
    (or (if (active lang) (lang-rt lang))
        (let [rts (map (fn [lang] (lang-rt ns lang)) active)]
          (or (first (filter (fn [rt] (get-in rt [:module/primary module]))
                             rts))
              (first (filter (fn [rt] (get-in rt [:module/internal module]))
                             rts))))
        (h/p:space-rt-current ns (:context ptr)))))

(defn lang-pointer
  "creates a lang pointer"
  {:added "4.0"}
  ([lang]
   (lang-pointer lang {}))
  ([lang {:keys [module id] :as m}]
   (let [ctx (lang-context lang)]
     (h/pointer (assoc m
                       :module module :lang lang
                       :context ctx
                       :context/fn #'lang-rt-default)))))
