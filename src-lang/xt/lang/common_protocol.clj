(ns xt.lang.common-protocol
  (:require [hara.lang :as l]
            [std.string.case :as case]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]]})

(def.xt PROTOCOLS {})

(def.xt IMPLEMENTATIONS {})

;;
;; design of protocol and class
;;


(defn.xt raw-method
  [on typename method]
  (return
   (xtd/get-in xt.lang.common-protocol/PROTOCOLS
               [on
                "impls"
                typename
                method])))

(defn.xt protocol-exists
  [typename]
  (return
   (xt/x:has-key? xt.lang.common-protocol/IMPLEMENTATIONS typename)))

(defn.xt protocol-implements
  "returns true if obj implements the given protocol"
  {:added "4.1"}
  [obj protocol]
  (var type (xt/x:get-key obj "::"))
  (when (xt/x:nil? type)
    (return false))
  (var entry (xt/x:get-key xt.lang.common-protocol/PROTOCOLS protocol))
  (when (xt/x:nil? entry)
    (return false))
  (var impls (xt/x:get-key entry "impls"))
  (return (xt/x:has-key? impls type)))

(defn.xt protocol-method
  "looks up the registered method by protocol and implementation type"
  {:added "4.1"}
  [obj on method]
  (var type           (xt/x:get-key obj "::"))  
  (var override-map   (xt/x:get-key obj "::/override"))
  (when (not (xt/x:nil? override-map))
    (var override-fn    (xt/x:get-key override-map method))
    (when (not (xt/x:nil? override-fn))
      (return override-fn)))
  
  (var protocol (xt/x:get-key xt.lang.common-protocol/PROTOCOLS on))
  (when (xt/x:nil? protocol)
    (xt/x:err (xt/x:cat "Missing protocol entry " on)))
  
  (var protocol-impls (xt/x:get-key protocol "impls"))
  (var protocol-impl-map (xt/x:get-key protocol-impls type))

  (when (xt/x:nil? protocol-impl-map)
    (xt/x:err (xt/x:cat "Missing protocol implementation " on " for " type)))
  
  (var method-fn (xt/x:get-key protocol-impl-map method))
  
  (when (xt/x:nil? method-fn)
    (xt/x:err (xt/x:cat "Missing protocol method " on "/" method " for " type)))

  (return method-fn))

(defn.xt protocol-has-method
  "returns true if obj has a concrete method for protocol/method"
  {:added "4.1"}
  [obj protocol method]
  (try
    (-/protocol-method obj protocol method)
    (return true)
    (catch err
        (return false))))

(defn.xt register-protocol-impl
  "registers protocol implementations in the registry"
  {:added "4.1"}
  [protocolname typename impl-map]
  (var protocol (xt/x:get-key xt.lang.common-protocol/PROTOCOLS protocolname))
  (when (xt/x:nil? protocol)
    (xt/x:err (xt/x:cat "Missing protocol " protocolname)))
  (var impls (xt/x:get-key protocol "impls"))
  (xt/x:set-key impls typename impl-map)
  (return impl-map))

(defn.xt register-protocol
  "registers a protocol descriptor in the global protocol registry"
  {:added "4.1"}
  [protocol]
  (xt/x:set-key xt.lang.common-protocol/PROTOCOLS
                (xt/x:get-key protocol "on")
                protocol)
  (return protocol))

(defn.xt create-protocol-fn
  "creates a runtime protocol descriptor"
  {:added "4.1"}
  [on sig-map]
  (var protocol
       {"::" "type/protocol"
        "on"    on
        "sigs"  sig-map
        "impls" {}})
  (return (-/register-protocol protocol)))



;;
;; defprotocol.xt
;;

(defn format-defprotocol-method-xt
  "formats a protocol method wrapper"
  {:added "4.1"}
  [on-str sig-sym arglist]
  (let [sig-name (case/snake-case (name sig-sym))]
    (list 'defn.xt sig-sym arglist
          (list 'var 'method-fn (list `protocol-method (first arglist) on-str sig-name))
          (list 'return (cons 'method-fn arglist)))))

(defn format-defprotocol-xt
  "formats a defprotocol.xt form into a runtime protocol descriptor"
  {:added "4.1"}
  [sym opts+sigs]
  (let [curr-ns   (case/snake-case (name (ns-name *ns*)))
        curr-sym  (name sym)
        name-fn   (fn [s] (case/snake-case (name s)))
        on-str    (str curr-ns "/" curr-sym)
        sig-map   (cons 'tab
                        (map (fn [[sig-sym arglist]]
                               (let [sig-name (name-fn sig-sym)]
                                 [sig-name {"name" sig-name
                                            "arglist" (mapv name-fn arglist)}]))
                             opts+sigs))]
    (list `create-protocol-fn on-str sig-map)))

(defmacro defprotocol.xt
  "expands to a protocol value and method wrappers"
  {:added "4.1"}
  [sym & opts+sigs]
  (let [curr-ns (case/snake-case (name (ns-name *ns*)))
        on-str  (str curr-ns "/" (name sym))
        methods (mapv (fn [[sig-sym arglist]]
                        (format-defprotocol-method-xt on-str sig-sym arglist))
                      opts+sigs)
        proto-sym (symbol (str "-/" sym))]
    (cons 'do
          (cons (list 'def.xt sym (format-defprotocol-xt sym opts+sigs))
                (cons (list `register-protocol proto-sym)
                      methods)))))

;;
;; defimpl.xt
;;

(defn- normalize-protocol-impl-map
  [impl-map]
  (into {}
        (map (fn [[k v]]
               [(case/snake-case (name k)) v])
             impl-map)))

(defn format-defimpl-xt-symbol
  [type-sym & [prefix]]
  (let [lang  (or (:lang (meta type-sym))
                  :xtalk)
        tag   (or (:tag (l/grammar lang))
                  (throw (ex-info "Unknown language" {:lang lang})))]
    (symbol (str (or prefix "def") "."  (name tag)))))

(defn format-defimpl-xt
  "formats a defimpl.xt constructor and protocol registration"
  {:added "4.1"}
  [type-sym impl-fields protocols]
  (let [sym-fn      (fn [ns sym]
                      (str (case/snake-case (name ns))
                           "/"
                           (name sym)))
        typename   (or (:rt/tag (meta type-sym))
                       (sym-fn (ns-name *ns*) type-sym))
        
        ctor-map    (into {"::" typename
                           "::/protocols" (mapv (fn [[proto-sym impl-map]]
                                                  (list `xt/x:get-key proto-sym "on"))
                                                protocols)}
                          (map (fn [x] [(name x) x]) impl-fields))

        proto-forms  (mapv (fn [[proto-sym impl-map]]
                             (list `register-protocol-impl
                                   (list `xt/x:get-key proto-sym "on")
                                   typename
                                   (normalize-protocol-impl-map impl-map)))
                           protocols)]
    
    (list (format-defimpl-xt-symbol type-sym "defn")
          type-sym impl-fields
          (list 'when (list 'not (list `xt/x:get-key `xt.lang.common-protocol/IMPLEMENTATIONS typename))
                (concat ['do (list `xt/x:set-key `xt.lang.common-protocol/IMPLEMENTATIONS typename true)]
                        proto-forms))
          (list 'return ctor-map))))

(defmacro defimpl.xt
  "expands to a constructor and protocol registrations"
  {:added "4.1"}
  [type-sym arglist & impls]
  (format-defimpl-xt type-sym
                     arglist
                     (partition-all 2 impls)))

;;
;;
;;
