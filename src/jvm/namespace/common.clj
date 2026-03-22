(ns jvm.namespace.common
  (:require [clojure.string]
            [std.lib.foundation :as f]
            [std.object.query :as query])
  (:import (clojure.lang DynamicClassLoader Namespace))
  (:refer-clojure :exclude [ns-unalias ns-unmap]))

(defonce ^java.util.concurrent.ConcurrentHashMap +class-cache+
  (query/apply-element DynamicClassLoader "classCache" []))

(defonce ^java.lang.ref.ReferenceQueue +rq+
  (query/apply-element DynamicClassLoader "rq" []))

(defonce ^java.util.concurrent.ConcurrentHashMap +namespaces+
  (query/apply-element Namespace "namespaces" []))

(defn ns-unalias
  "removes given aliases in namespaces
 
   (ns-unalias *ns* '[hello world])
   => '[hello world]"
  {:added "3.0"}
  ([ns refer]
   (cond (symbol? refer)
         (do (clojure.core/ns-unalias ns refer)
             refer)

         (coll? refer)
         (mapv #(ns-unalias ns %) refer))))

(defn ns-unmap
  "removes given mapped elements in namespaces
 
   (ns-unmap *ns* '[hello world])
   => '[hello world]"
  {:added "3.0"}
  ([ns refer]
   (cond (symbol? refer)
         (do (clojure.core/ns-unmap ns refer)
             refer)

         (coll? refer)
         (mapv #(ns-unmap ns %) refer))))

(defn ns-clear-aliases
  "removes all namespace aliases
 
   ;; require std.string
   (require '[std.string :as str])
   => nil
 
   ;; error if a new namespace is set to the same alias
   (require '[clojure.set :as string])
   => (throws) ;;  Alias string already exists in namespace
 
   ;; clearing all aliases
   (ns-clear-aliases)
 
   (ns-aliases *ns*)
   => {}
 
   ;; okay to require
   (require '[clojure.set :as string])
   => nil"
  {:added "3.0"}
  ([ns]
   (for [alias (keys (ns-aliases ns))]
     (do (clojure.core/ns-unalias ns alias)
         alias))))

(def +ns-clojure-imports+
  #{java.util.concurrent.Callable
    clojure.lang.DynamicClassLoader
    java.math.BigInteger
    clojure.lang.Namespace
    java.math.BigDecimal
    clojure.lang.Compiler})

(defn ns-list-external-imports
  "lists all external imports
 
   (import java.io.File)
   (ns-list-external-imports *ns*)
   => '(File)"
  {:added "3.0"}
  ([ns]
   (->> (ns-imports *ns*)
        (remove (fn [[k ^Class v]]
                  (or (.startsWith (.getName v) "java.lang")
                      (+ns-clojure-imports+ v))))
        (map first))))

(defn ns-clear-external-imports
  "clears all external imports
 
   (ns-clear-external-imports *ns*)
   (ns-list-external-imports *ns*)
   => ()"
  {:added "3.0"}
  ([ns]
   (mapv (fn [k]
           (clojure.core/ns-unmap ns k)
           k)
         (ns-list-external-imports ns))))

(defn ns-clear-mappings
  "removes all mapped vars in the namespace
 
   ;; require `join`
   (require '[std.string.base :refer [join]])
 
   ;; check that it runs
   (join [\"a\" \"b\" \"c\"])
   => \"abc\"
 
   ;; clear mappings
   (ns-clear-mappings)
 
   ;; the mapped symbol is gone
   (join [\"a\" \"b\" \"c\"])
   => (throws) ;; \"Unable to resolve symbol: join in this context\""
  {:added "3.0"}
  ([ns]
   (mapv (fn [k]
           (clojure.core/ns-unmap ns k)
           k)
         (keys (ns-map ns)))))

(defn ns-clear-interns
  "clears all interns in a given namespace
 
   (ns-clear-interns *ns*)"
  {:added "3.0"}
  ([ns]
   (mapv (fn [k]
           (clojure.core/ns-unmap ns k)
           k)
         (keys (ns-interns ns)))))

(defn ns-clear-refers
  "clears all refers in a given namespace"
  {:added "3.0"}
  ([ns]
   (mapv (fn [k]
           (clojure.core/ns-unmap ns k)
           k)
         (keys (ns-refers ns)))))

(defn ns-clear
  "clears all mappings and aliases in a given namespace
 
   (ns-clear)"
  {:added "3.0"}
  ([ns]
   (concat (ns-clear-aliases ns)
           (ns-clear-refers ns)
           (ns-clear-interns ns)
           (ns-clear-external-imports ns))))

(defn group-in-memory
  "creates human readable results from the class list
 
   (group-in-memory [\"code.manage$add$1\" \"code.manage$add$2\"
                     \"code.manage$sub$1\" \"code.manage$sub$2\"])
   => '[[add 2]
        [sub 2]]"
  {:added "3.0"}
  ([results]
   (let [results (->> results
                      (map #(clojure.string/split % (re-pattern "\\$")))
                      (map rest))
         anon?  (fn [[call _]]
                  (try (re-find #"^fn__*" call)
                       (catch Exception ex
                         true)))
         anons  (->> results (filter anon?) count)
         eval?  (fn [[call _]]
                  (try (re-find #"^eval\d+" call)
                       (catch Exception ex
                         true)))
         evals   (->> results (filter eval?) count)
         funcs   (->> results
                      (remove eval?)
                      (remove anon?)
                      (group-by first))
         fks     (sort (keys funcs))]
     (cond->> (keep (fn [fk]
                      (try
                        [(symbol (f/demunge fk)) (count (get funcs fk))]
                        (catch Throwable t)))
                    fks)
       (pos? anons) (cons ['ANON anons])
       (pos? evals) (cons ['EVAL evals])
       :then (vec)))))

(defn raw-in-memory
  "returns a list of keys representing objects
 
   (raw-in-memory 'code.manage)
   ;;(\"code.manage$eval6411\" \"code.manage$eval6411$loading__5569__auto____6412\")
   => coll?"
  {:added "3.0"}
  ([ns]
   (->> +class-cache+
        (keep (fn [[k v]]
                (if (or (= ns k)
                        (.startsWith ^String k (str ns "$")))
                  k))))))

(defn ns-in-memory
  "retrieves all the clojure namespaces currently in memory
 
   (ns-in-memory 'code.manage)
   ;;[[EVAL 2]]
   => coll?"
  {:added "3.0"}
  ([ns]
   (->> (raw-in-memory ns)
        (group-in-memory))))

(defn ns-loaded?
  "checks if the namespaces is currently active
 
   (ns-loaded? 'jvm.namespace.common)
   => true"
  {:added "3.0"}
  ([ns]
   (boolean (get @@#'clojure.core/*loaded-libs* ns))))

(defn ns-delete
  "clears all namespace mappings and remove namespace from clojure environment
 
   (ns-delete 'jvm.namespace)
 
   (ns-delete 'jvm.namespace-test)"
  {:added "3.0"}
  ([ns]
   (let [;; unload from libs
         _    (dosync (alter @#'clojure.core/*loaded-libs*
                             disj ns))

         ;; clear classloader cache
         results (->> (raw-in-memory ns)
                      (mapv (fn [k] (.remove +class-cache+ k)
                              k)))

         ;; unmap from global *namespaces
         nsp (.get +namespaces+ ns)
         _   (ns-clear nsp)
         _   (.remove +namespaces+ ns)]

     results)))

(defn ns-list
  "returns all existing clojure namespaces
 
   (ns-list)
   => coll?"
  {:added "3.0"}
  ([]
   (keys +namespaces+)))

(defn ns-reload
  "reloads aliases in current namespace"
  {:added "3.0"}
  ([ns]
   (require ns :reload)))

(defn ns-reload-all
  "reloads aliases and dependents in current namespace"
  {:added "3.0"}
  ([ns]
   (require ns :reload-all)))
