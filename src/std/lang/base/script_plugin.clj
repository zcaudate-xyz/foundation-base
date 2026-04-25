(ns std.lang.base.script-plugin
  (:require [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.function :as fn]
            [std.lib.template :as template]))

(defonce +registry+
  (atom {}))

(defn intern-in
  "interns a macro"
  {:added "4.0"}
  ([prefix tag val & [namespace]]
   (intern (or namespace (env/ns-sym))
           (with-meta (symbol (str prefix (name tag)))
             {:macro true
              :arglists '([& body])})
           val)))

(defn register-plugin!
  "registers a script support plugin"
  {:added "4.0"}
  [id plugin]
  (swap! +registry+ assoc id plugin))

(defn get-plugin
  "gets a script support plugin"
  {:added "4.0"}
  [id]
  (or (get @+registry+ id)
      (f/error "Support plugin not found" {:plugin id})))

(defn plugin-symbol
  "returns the symbol that a support plugin will intern"
  {:added "4.0"}
  [id grammar]
  (let [{:keys [symbol-fn]} (get-plugin id)]
    (symbol-fn {:id id
                :grammar grammar})))

(defn intern-plugin
  "interns a support plugin for the active namespace"
  {:added "4.0"}
  ([lang id]
   (let [library (impl/runtime-library)
         grammar (:grammar (lib/get-book library lang))]
     (intern-plugin lang grammar id)))
  ([lang grammar id]
   (let [{:keys [intern-fn]} (get-plugin id)]
     (intern-fn {:id id
                 :lang lang
                 :grammar grammar}))))

(defn intern-plugins
  "interns declared support plugins, skipping existing imports.

   Returns `[added ids]` where `added` contains newly interned symbols and
   `ids` contains all requested support symbols."
  {:added "4.0"}
  [lang grammar ids]
  (let [curr     (env/ns-sym)
        existing (set (concat (keys (ns-refers curr))
                              (keys (ns-interns curr))))
        [added syms _]
        (reduce (fn [[added syms seen] id]
                  (let [sym (plugin-symbol id grammar)]
                    (if (contains? seen sym)
                      [added (conj syms sym) seen]
                      (do (intern-plugin lang grammar id)
                          [(conj added sym)
                           (conj syms sym)
                           (conj seen sym)]))))
                [#{} #{} existing]
                ids)]
    [added syms]))

(defn defvar-fn
  "helper function for defvar support macros"
  {:added "4.0"}
  [&form tag sym-id doc? attrs? more]
  (let [sym-ns  (or (get (meta sym-id) :ns)
                    (str (env/ns-sym)))
        sym-id  (if (vector? sym-id)
                  (first sym-id)
                  sym-id)
        sym-key (f/strn sym-id)
        [_doc _attr more] (fn/fn:init-args doc? attrs? more)
        more   (if (vector? (first more))
                 more
                 (first more))
        def-sym (symbol (str "defn." tag))
        item-sym 'xt.lang.common-runtime/xt-item-get
        set-sym  'xt.lang.common-runtime/xt-var-set]
    (template/$ [(~def-sym ~(with-meta sym-id (merge (meta &form)
                                                     (meta sym-id)))
                   []
                   (return (~item-sym
                            ~sym-ns
                            ~sym-key
                            (fn ~@more))))
                 (~def-sym ~(with-meta (symbol (str sym-id "-reset"))
                              (merge (meta &form)
                                     (meta sym-id)))
                   [val]
                   (return (~set-sym
                            ~(str sym-ns "/" sym-key)
                            val)))])))

(defn register-defvar-plugin!
  "registers the `defvar.<tag>` support plugin"
  {:added "4.0"}
  []
  (register-plugin!
   :defvar
   {:symbol-fn (fn [{:keys [grammar]}]
                 (symbol (str "defvar." (name (:tag grammar)))))
    :intern-fn (fn [{:keys [grammar]}]
                 (let [tag (name (:tag grammar))]
                   (intern-in
                    "defvar." tag
                    (fn [&form _ sym-id & [doc? attrs? & more]]
                      (defvar-fn &form tag sym-id doc? attrs? more)))))}))

(def +init+
  (register-defvar-plugin!))
