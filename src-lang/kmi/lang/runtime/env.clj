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
           "namespaces" {"user"     {"vars" {} "macros" {}}
                         "kmi.core" {"vars" {} "macros" {}}}}))

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

(defn.xt var-lookup
  "looks up a symbol: env -> current ns -> kmi.core"
  {:added "4.1"}
  [runtime env sym]
  (var local (-/env-lookup env sym))
  (when (xt/x:not-nil? local)
    (return local))
  (var cur (-/ns-lookup runtime sym))
  (when (xt/x:not-nil? cur)
    (return cur))
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
