(ns xt.lang.common-protocol
  (:require [hara.lang :as l]
            [std.string.case :as case]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]]})

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
  "TODO"
  {:added "4.1"}
  [value]
  (if (promise/x:promise-native? value)
    (return value)
    (return (promise/x:promise-run value))))


;;
;; design of protocol and class
;;

(defn.xt create-protocol-fn
  "creates a runtime protocol descriptor"
  {:added "4.1"}
  [on sig-map]
  (return
   {"::" "type/protocol"
    "on"    on    
    "sigs"  sig-map
    "impls" {}}))

(defn format-defprotocol-xt
  "formats a defprotocol.xt form into a runtime protocol descriptor"
  {:added "4.1"}
  [sym opts+sigs]
  (let [curr-ns   (case/snake-case (name (:module (l/rt :xtalk))))
        curr-sym  (name sym)
        name-fn   (fn [s] (case/snake-case (name s)))
        on-str    (str curr-ns "/" curr-sym)        
        sig-map   (cons 'tab
                        (mapcat (fn [[sig-sym arglist]]
                                  (let [sig-name (name-fn sig-sym)]
                                    [sig-name {"name" sig-name
                                               "arglist" (mapv name-fn arglist)}
                                     {}]))
                                opts+sigs))]
    (list `create-protocol-fn on-str sig-map)))

(defmacro defprotocol.xt
  "defines a protocol descriptor"
  {:added "4.1"}
  [sym & opts+sigs]
  (list 'def.xt sym
        (format-defprotocol-xt sym opts+sigs)))


;;
;;
;;
