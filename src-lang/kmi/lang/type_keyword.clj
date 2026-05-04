(ns kmi.lang.type-keyword
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [keyword]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]
             [kmi.protocol.ieq :as p-eq]
             [kmi.protocol.ihash :as p-hash]
             [kmi.protocol.inamespaced :as p-namespaced]
             [kmi.protocol.ishow :as p-show]
             [kmi.lang.interface-common :as interface-common]
             [kmi.lang.interface-spec :as spec]
             [kmi.lang.common-hash :as common-hash]]})

(def.xt KEYWORD_LOOKUP
  {})

(defn.xt keyword-hash
  "gets the keyword hash"
  {:added "4.0"}
  [sym]
  (var #{_ns _name} sym)
  (var sname (:? (xt/x:nil? _ns)
                 _name
                 (xt/x:cat _ns "/" _name)))
  (return
   (-> (xt/x:get-key common-hash/SEED "keyword")
       (xt/x:bit-xor (common-hash/hash-string
                     sname)))))

(defn.xt keyword-show
  "shows the keyword"
  {:added "4.0"}
  [sym]
  (var #{_ns _name} sym)
  (var sname (:? (xt/x:nil? _ns)
                 _name
                 (xt/x:cat _ns "/" _name)))
  (return
   (xt/x:cat ":" sname)))

(defn.xt keyword-eq
  "gets keyword equality"
  {:added "4.0"}
  [sym o]
  (var oclass (common-hash/native-class o))
  (return (and (== "keyword" oclass)
               (== (. sym _ns)   (. o _ns))
               (== (. sym _name) (. o _name)))))

(def.xt KEYWORD_SPEC
  [[p-eq/IEq         {:eq        -/keyword-eq}]
   [p-hash/IHash       {:hash      (interface-common/wrap-with-cache
                                    -/keyword-hash)}]
   [p-namespaced/INamespaced {:name      interface-common/get-name
                        :namespace interface-common/get-namespace} ]
   [p-show/IShow       {:show      -/keyword-show}]])

(def.xt KEYWORD_PROTOTYPE
  (-> -/KEYWORD_SPEC
      (proto/proto-spec)
      (spec/proto-create)))

(defn.xt keyword-create
  "creates a keyword"
  {:added "4.0"}
  [ns name]
  (var sym {"::" "keyword"
            :_ns   ns
            :_name name})
  (return (spec/runtime-attach sym -/KEYWORD_PROTOTYPE)))

(defn.xt keyword
  "creates the keyword or pulls it from cache"
  {:added "4.0"}
  [ns name]
  (var lu -/KEYWORD_LOOKUP)
  (var key (:? (xt/x:nil? ns)
               name
               (xt/x:cat ns "/" name)))
  (var out (xt/x:get-key lu key))
  (when (xt/x:nil? out)
    (var sym (-/keyword-create ns name))
    (xt/x:set-key lu key sym)
    (return sym))
  (return out))
