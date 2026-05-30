(ns postgres.gen.template-code
  (:require [clojure.string :as str]
            [code.project :as project]
            [postgres.gen.bind-macro :as bind]
            [std.fs :as fs]
            [std.block.template :as template]
            [std.string.prose :as prose]))

(def ^:private ROUTE_ENTRY_TEMPLATE
  (prose/join-lines
   ["(def.xt ~route-sym"
    "  {:input ~input"
    "   :return ~return"
    "   :schema ~schema"
    "   :id ~id"
    "   :flags ~flags})"]))

(defn namespace-url-root
  "Creates a default API root from a postgres module namespace."
  {:added "4.1"}
  [ns]
  (str "api/"
       (-> ns
           name
           (str/split #"\.")
           last
           (str/replace "_" "-"))))

(defn route-entry-input
  "Creates template input for a generated `def.xt` route entry."
  {:added "4.1"}
  [{:keys [src route-sym root]}]
  (let [{:keys [input return schema id flags]} (bind/bind-function @(resolve src))]
    {'route-sym route-sym
     'input     input
     'return    return
     'schema    schema
     'id        id
     'flags     flags}))

(def ^:private +route-entry+
  (template/get-template ROUTE_ENTRY_TEMPLATE
                         route-entry-input))

(defn emit-route-entry
  "Emits a single `def.xt` route entry for a postgres function symbol."
  {:added "4.1"}
  ([src]
   (emit-route-entry src nil))
  ([src root]
   (let [ns-sym    (symbol (namespace src))
         _         (require ns-sym)
         route-sym (symbol (name src))
         root      (or root
                       (namespace-url-root ns-sym))]
     (template/fill-template +route-entry+
                             {:src src
                              :route-sym route-sym
                              :root root}))))

(defn emit-route-entries
  "Emits all `def.xt` route entries for a postgres module namespace."
  {:added "4.1"}
  ([ns]
   (emit-route-entries ns nil))
  ([ns root]
   (require ns)
   (let [root (or root (namespace-url-root ns))]
     (->> (bind/list-api ns)
          (map (comp #(emit-route-entry % root) second))
          (str/join "\n\n")))))

(defn target-path
  "Resolves the output path for a generated namespace using the source namespace root."
  {:added "4.1"}
  [target-ns src-ns]
  (let [src-path    (or (project/get-path src-ns)
                        (throw (ex-info "Cannot resolve source namespace path."
                                        {:src-ns src-ns})))
        project-map (project/project)
        match       (some (fn [source-root]
                            (let [prefix (str source-root "/")
                                  infix  (str "/" source-root "/")]
                              (cond
                                (.startsWith ^String src-path prefix)
                                {:root source-root
                                 :base nil}

                                (.contains ^String src-path infix)
                                (let [idx (.indexOf ^String src-path infix)]
                                  {:root source-root
                                   :base (subs src-path 0 idx)}))))
                          (:source-paths project-map))]
    (when-not match
      (throw (ex-info "Cannot resolve source root for namespace."
                      {:src-ns src-ns
                       :src-path src-path})))
    (let [{:keys [root base]} match]
      (str (when base (str base "/"))
           root "/"
           (fs/ns->file target-ns)
           ".clj"))))

(defn namespace-body
  "Creates the source body for a generated route namespace."
  {:added "4.1"}
  [target-ns src-ns]
  (str "(ns " target-ns "\n"
       "  (:require [hara.lang :as l]))\n\n"
       "(l/script :xtalk)\n\n"
       (emit-route-entries src-ns)
       "\n"))

(defn generate-ns
  "Generates a route namespace file from a postgres module namespace."
  {:added "4.1"}
  [target-ns src-ns]
  (let [path (target-path target-ns src-ns)
        body (namespace-body target-ns src-ns)]
    (fs/create-directory (fs/parent path))
    (spit path body)
    path))
