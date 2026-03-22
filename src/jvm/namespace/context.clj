(ns jvm.namespace.context
  (:require [jvm.namespace.common :as common]
            [std.lib.deps :as deps]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]
            [std.protocol.deps :as protocol.deps]))

(defn resolve-ns
  "resolves the namespace or else returns nil if it does not exist
 
   (resolve-ns 'clojure.core) => 'clojure.core
 
   (resolve-ns 'clojure.core/some) => 'clojure.core
 
   (resolve-ns 'clojure.hello) => nil"
  {:added "3.0"}
  ([^clojure.lang.Symbol sym]
   (let [nsp  (.getNamespace sym)
         nsym (or  (and nsp
                        (symbol nsp))
                   sym)]
     (if nsym
       (f/suppress (do (require nsym) nsym))))))

(defn ns-vars
  "lists the vars in a particular namespace
 
   (ns-vars 'jvm.namespace.context)
   => '[*ns-context* ->NamespaceContext map->NamespaceContext
        ns-context ns-vars reeval resolve-ns]"
  {:added "3.0"}
  ([ns]
   (vec (sort (keys (ns-publics ns))))))

(defn- get-entry-ns
  ([_ ns] (the-ns ns)))

(defn- get-deps-ns
  ([_ ns]
   (->> (vals (ns-aliases ns))
        (map #(.getName ^clojure.lang.Namespace %)))))

(defn- list-entries-ns
  ([_]
   (common/ns-list)))

(defn- add-entry-ns
  ([context ns _ _]
   (require ns)
   context))

(defn- remove-entry-ns
  ([context ns]
   (common/ns-delete ns)
   context))

(defn- refresh-entry-ns
  ([context ns]
   (common/ns-reload ns)
   context))

(impl/defimpl NamespaceContext []
  :suffix "-ns"
  :protocols [std.protocol.deps/IDeps
              protocol.deps/IDepsMutate])

(defonce ^:dynamic *ns-context* (NamespaceContext.))

(defn ns-context
  "gets the namespace context"
  {:added "3.0"}
  ([] *ns-context*))

(defn reeval
  "reevals all dependents of a namespace"
  {:added "3.0"}
  ([]
   (reeval (.getName *ns*)))
  ([ns]
   (deps/dependents-refresh (ns-context) ns)))

(comment
  (ns-reeval)
  (require 'jvm.tool)
  (deps/dependents-ordered (ns-context) 'std.concurrent)
  (deps/unload-entry (ns-context) 'std.concurrent)
  (deps/dependents-direct (ns-context) (.getName *ns*))
  (deps/dependents-all (ns-context) (.getName *ns*)))
