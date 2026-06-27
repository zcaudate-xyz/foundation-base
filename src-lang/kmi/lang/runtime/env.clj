(ns kmi.lang.runtime.env
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [empty]))

(l/script :xtalk
  {:require [[kmi.lang.common-util :as util]
             [xt.lang.spec-base :as xt]]})

;;
;; SYMBOL HELPERS
;;

(defn.xt sym-name
  "returns the name string of a kmi symbol"
  {:added "4.1"}
  [sym]
  (return (util/get-name sym)))

(defn.xt sym-ns
  "returns the namespace string of a kmi symbol"
  {:added "4.1"}
  [sym]
  (return (util/get-namespace sym)))

;;
;; LEXICAL ENVIRONMENT
;;

(defn.xt env-create
  "creates a new lexical frame with an optional parent"
  {:added "4.1"}
  [parent]
  (return {"bindings" {}
           "parent" parent}))

(defn.xt empty-env
  "returns an empty top-level environment"
  {:added "4.1"}
  []
  (return (-/env-create nil)))

(defn.xt env-lookup
  "looks up a symbol in the lexical environment chain"
  {:added "4.1"}
  [env sym]
  (var name (-/sym-name sym))
  (while (xt/x:not-nil? env)
    (var bindings (xt/x:get-key env "bindings"))
    (when (xt/x:has-key? bindings name)
      (return (xt/x:get-key bindings name)))
    (:= env (xt/x:get-key env "parent")))
  (return nil))

(defn.xt env-has?
  "checks if a symbol is bound in the lexical environment chain"
  {:added "4.1"}
  [env sym]
  (var name (-/sym-name sym))
  (while (xt/x:not-nil? env)
    (var bindings (xt/x:get-key env "bindings"))
    (when (xt/x:has-key? bindings name)
      (return true))
    (:= env (xt/x:get-key env "parent")))
  (return false))

(defn.xt env-extend
  "creates a child frame with the given bindings map"
  {:added "4.1"}
  [env bindings]
  (return {"bindings" bindings
           "parent" env}))

;;
;; NAMESPACE / RUNTIME STATE
;;

(defn.xt runtime-create
  "creates an empty runtime snapshot"
  {:added "4.1"}
  []
  (return {"ns" "user"
           "namespaces" {"user"     {"vars" {} "macros" {} "aliases" {} "refs" {}}
                         "kmi.core" {"vars" {} "macros" {} "aliases" {} "refs" {}}}}))

(defn.xt current-ns-name
  "returns the current namespace name"
  {:added "4.1"}
  [runtime]
  (return (xt/x:get-key runtime "ns")))

(defn.xt current-ns
  "returns the current namespace map"
  {:added "4.1"}
  [runtime]
  (return (xt/x:get-key (xt/x:get-key runtime "namespaces")
                        (xt/x:get-key runtime "ns"))))

(defn.xt ns-lookup
  "looks up a symbol in the current namespace vars"
  {:added "4.1"}
  [runtime sym]
  (var ns (-/current-ns runtime))
  (return (xt/x:get-key (xt/x:get-key ns "vars")
                        (-/sym-name sym))))

(defn.xt ns-lookup-in
  "looks up a symbol in a specific namespace"
  {:added "4.1"}
  [runtime ns-name sym]
  (var ns (xt/x:get-key (xt/x:get-key runtime "namespaces") ns-name))
  (return (xt/x:get-key (xt/x:get-key ns "vars")
                        (-/sym-name sym))))

(defn.xt ns-alias
  "returns the namespace name for an alias, or nil"
  {:added "4.1"}
  [runtime alias-name]
  (var ns (-/current-ns runtime))
  (return (xt/x:get-key (xt/x:get-key ns "aliases") alias-name)))

(defn.xt var-lookup
  "looks up a symbol: env -> current ns -> refs -> kmi.core"
  {:added "4.1"}
  [runtime env sym]
  (var local (-/env-lookup env sym))
  (when (xt/x:not-nil? local)
    (return local))
  (var ns-name (-/sym-ns sym))
  (var name (-/sym-name sym))
  (when (xt/x:not-nil? ns-name)
    (var aliased (-/ns-alias runtime ns-name))
    (when (xt/x:not-nil? aliased)
      (:= ns-name aliased))
    (return (-/ns-lookup-in runtime ns-name sym)))
  (var cur-ns (-/current-ns runtime))
  (var cur-vars (xt/x:get-key cur-ns "vars"))
  (when (xt/x:has-key? cur-vars name)
    (return (xt/x:get-key cur-vars name)))
  (var refs (xt/x:get-key cur-ns "refs"))
  (when (xt/x:has-key? refs name)
    (return (xt/x:get-key refs name)))
  (return (-/ns-lookup-in runtime "kmi.core" sym)))

(defn.xt ns-assoc
  "returns a new runtime with sym bound to value in the current namespace"
  {:added "4.1"}
  [runtime sym value]
  (var ns-name (xt/x:get-key runtime "ns"))
  (var ns (xt/x:get-key (xt/x:get-key runtime "namespaces") ns-name))
  (var vars (xt/x:obj-clone (xt/x:get-key ns "vars")))
  (xt/x:set-key vars (-/sym-name sym) value)
  (var ns-new (xt/x:obj-clone ns))
  (xt/x:set-key ns-new "vars" vars)
  (var rt-new (xt/x:obj-clone runtime))
  (xt/x:set-key (xt/x:get-key rt-new "namespaces") ns-name ns-new)
  (return rt-new))

(defn.xt ns-assoc-macro
  "returns a new runtime with sym bound to a macro in the current namespace"
  {:added "4.1"}
  [runtime sym macro-fn]
  (var ns-name (xt/x:get-key runtime "ns"))
  (var ns (xt/x:get-key (xt/x:get-key runtime "namespaces") ns-name))
  (var macros (xt/x:obj-clone (xt/x:get-key ns "macros")))
  (xt/x:set-key macros (-/sym-name sym) macro-fn)
  (var ns-new (xt/x:obj-clone ns))
  (xt/x:set-key ns-new "macros" macros)
  (var rt-new (xt/x:obj-clone runtime))
  (xt/x:set-key (xt/x:get-key rt-new "namespaces") ns-name ns-new)
  (return rt-new))

(defn.xt macro-lookup
  "looks up a macro in the current namespace or kmi.core"
  {:added "4.1"}
  [runtime sym]
  (var ns (-/current-ns runtime))
  (var cur (xt/x:get-key (xt/x:get-key ns "macros")
                         (-/sym-name sym)))
  (when (xt/x:not-nil? cur)
    (return cur))
  (var core (xt/x:get-key (xt/x:get-key (xt/x:get-key runtime "namespaces") "kmi.core") "macros"))
  (return (xt/x:get-key core (-/sym-name sym))))

(defn.xt macro?
  "checks if a symbol names a macro"
  {:added "4.1"}
  [runtime sym]
  (return (xt/x:not-nil? (-/macro-lookup runtime sym))))

;;
;; NAMESPACE MANAGEMENT
;;

(defn.xt runtime-set-ns
  "returns a new runtime with the current namespace changed"
  {:added "4.1"}
  [runtime ns-name]
  (var rt-new (xt/x:obj-clone runtime))
  (xt/x:set-key rt-new "ns" ns-name)
  (return rt-new))

(defn.xt ns-ensure
  "creates a namespace if it does not already exist"
  {:added "4.1"}
  [runtime ns-name]
  (var namespaces (xt/x:get-key runtime "namespaces"))
  (when (xt/x:has-key? namespaces ns-name)
    (return runtime))
  (var rt-new (xt/x:obj-clone runtime))
  (:= namespaces (xt/x:obj-clone (xt/x:get-key rt-new "namespaces")))
  (xt/x:set-key namespaces ns-name {"vars" {} "macros" {} "aliases" {} "refs" {}})
  (xt/x:set-key rt-new "namespaces" namespaces)
  (return rt-new))

(defn.xt ns-set-alias
  "returns a new runtime with an alias set in the current namespace"
  {:added "4.1"}
  [runtime alias-name ns-name]
  (var cur-name (xt/x:get-key runtime "ns"))
  (var ns (xt/x:get-key (xt/x:get-key runtime "namespaces") cur-name))
  (var aliases (xt/x:obj-clone (xt/x:get-key ns "aliases")))
  (xt/x:set-key aliases alias-name ns-name)
  (var ns-new (xt/x:obj-clone ns))
  (xt/x:set-key ns-new "aliases" aliases)
  (var rt-new (xt/x:obj-clone runtime))
  (xt/x:set-key (xt/x:get-key rt-new "namespaces") cur-name ns-new)
  (return rt-new))

(defn.xt ns-refer
  "returns a new runtime with a referred var in the current namespace"
  {:added "4.1"}
  [runtime source-ns sym]
  (var cur-name (xt/x:get-key runtime "ns"))
  (var ns (xt/x:get-key (xt/x:get-key runtime "namespaces") cur-name))
  (var refs (xt/x:obj-clone (xt/x:get-key ns "refs")))
  (var val (-/ns-lookup-in runtime source-ns sym))
  (xt/x:set-key refs (-/sym-name sym) val)
  (var ns-new (xt/x:obj-clone ns))
  (xt/x:set-key ns-new "refs" refs)
  (var rt-new (xt/x:obj-clone runtime))
  (xt/x:set-key (xt/x:get-key rt-new "namespaces") cur-name ns-new)
  (return rt-new))

(defn.xt ns-refer-all
  "refers all vars from a namespace into the current namespace"
  {:added "4.1"}
  [runtime source-ns-name]
  (var cur-name (xt/x:get-key runtime "ns"))
  (var ns (xt/x:get-key (xt/x:get-key runtime "namespaces") cur-name))
  (var refs (xt/x:obj-clone (xt/x:get-key ns "refs")))
  (var source-ns (xt/x:get-key (xt/x:get-key runtime "namespaces") source-ns-name))
  (var source-vars (xt/x:get-key source-ns "vars"))
  (var keys (xt/x:obj-keys source-vars))
  (xt/for:array [k keys]
    (xt/x:set-key refs k (xt/x:get-key source-vars k)))
  (var ns-new (xt/x:obj-clone ns))
  (xt/x:set-key ns-new "refs" refs)
  (var rt-new (xt/x:obj-clone runtime))
  (xt/x:set-key (xt/x:get-key rt-new "namespaces") cur-name ns-new)
  (return rt-new))
