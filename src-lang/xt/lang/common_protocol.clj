(ns xt.lang.common-protocol
  (:require [hara.lang :as l]
            [std.string.case :as case]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]]})

(def.xt REGISTRY {})

(defn.xt iface-combine
  "combines interface vectors without duplicates"
  {:added "4.1"}
  [interfaces]
  (var seen {})
  (var out [])
  (xt/for:array [iface interfaces]
    (xt/for:array [key iface]
      (when (xt/x:nil? (xt/x:get-key seen key))
        (xt/x:set-key seen key true)
        (xt/x:arr-push out key))))
  (return out))

(defn.xt proto-group
  "pairs the combined protocol surface with the implementation map"
  {:added "4.1"}
  [interfaces spec-map]
  (return [(-/iface-combine interfaces) spec-map]))

(defn.xt proto-spec
  "merges protocol groups and rejects missing required methods"
  {:added "4.1"}
  [spec-arr]
  (var acc {})
  (xt/for:array [entry spec-arr]
    (var required (xt/x:first entry))
    (var spec-map (xt/x:second entry))
    (xt/for:array [key required]
      (when (xt/x:nil? (xt/x:get-key spec-map key))
        (throw (xt/x:ex "Invalid Key"
                        {:required key
                         :actual (xtd/obj-keys spec-map)}))))
    (:= acc (xt/x:obj-assign acc spec-map)))
  (return acc))

(defn.xt ensure-promise
  "wraps raw values in a promise and preserves native promises"
  {:added "4.1"}
  [value]
  (if (promise/x:promise-native? value)
    (return value)
    (return (promise/x:promise-run value))))


;;
;; design of protocol and class
;;

(defn.xt protocol-method
  "looks up the registered method by protocol and implementation type"
  {:added "4.1"}
  [obj on method]
  (var type           (xt/x:get-key obj "::"))
  (var override-map   (xt/x:get-key obj "::/override"))
  (var override-proto (xt/x:get-key override-map on))
  (var override-fn    (xt/x:get-key override-proto method))
  (when (not (xt/x:nil? override-fn))
    (return override-fn))

  (var protocol (xt/x:get-key -/REGISTRY on))
  (when (not (xt/x:nil? protocol))
    (var protocol-impls (xt/x:get-key protocol "impls"))
    (var protocol-impl-map (xt/x:get-key protocol-impls type))
    (when (not (xt/x:nil? protocol-impl-map))
      (var method-fn (xt/x:get-key protocol-impl-map method))
      (when (not (xt/x:nil? method-fn))
        (return method-fn)))

  (var direct-fn (xt/x:get-key obj method))
  (when (not (xt/x:nil? direct-fn))
    (return direct-fn))

  (var local-impls (xt/x:get-key obj "::/impls"))
  (var local-proto (xt/x:get-key local-impls on))
  (var local-fn (xt/x:get-key local-proto method))
  (when (not (xt/x:nil? local-fn))
    (return local-fn))

  (xt/x:err (xt/x:cat "Missing protocol method " on "/" method)))

(defn.xt register-protocol-impl
  "registers protocol implementations in the registry"
  {:added "4.1"}
  [protocol-or-on type impl-map]
  (var on (if (xt/x:is-object? protocol-or-on)
            (xt/x:get-key protocol-or-on "on")
            protocol-or-on))
  (var protocol (xt/x:get-key -/REGISTRY on))
  (when (xt/x:nil? protocol)
    (xt/x:err (xt/x:cat "Missing protocol " on)))
  (var impls (xt/x:get-key protocol "impls"))
  (xt/x:set-key impls type impl-map)
  (return impl-map))

(defn.xt create-protocol-fn
  "creates a runtime protocol descriptor"
  {:added "4.1"}
  [on sig-map]
  (var protocol
       {"::" "type/protocol"
        "on"    on
        "sigs"  sig-map
        "impls" {}})
  (xt/x:set-key -/REGISTRY on protocol)
  (return protocol))


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
                      opts+sigs)]
    (vec (cons (list 'def.xt sym (format-defprotocol-xt sym opts+sigs))
               methods))))

;;
;; defimpl.xt
;;

(defn- normalize-protocol-impl-map
  [impl-map]
  (into {}
        (map (fn [[k v]]
               [(case/snake-case (name k)) v])
             impl-map)))

(defn format-defimpl-xt
  "formats a defimpl.xt constructor and protocol registration"
  {:added "4.1"}
  [type-sym impl-fields protocols]
  (let [curr-ns     (case/snake-case (name (ns-name *ns*)))
        type-name   (str curr-ns "/" (name type-sym))
        impl-fields (mapv name impl-fields)
        ctor-map    (into {"::" type-name}
                          (concat (map (fn [f] [f (symbol f)]) impl-fields)
                                  [["::/override" {}]]))
        proto-forms  (mapv (fn [[proto-sym impl-map]]
                             (list `register-protocol-impl
                                   proto-sym
                                   type-name
                                   (normalize-protocol-impl-map impl-map)))
                           protocols)]
    [(list 'def.xt (symbol (str (name type-sym) "-init"))
           proto-forms)
     (list 'defn.xt type-sym impl-fields
           (list 'return ctor-map))]))

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
