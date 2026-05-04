(ns kmi.lang.type-syntax
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as proto]
             [kmi.protocol.iassoc :as p-assoc]
             [kmi.protocol.iassoc-mutable :as p-assoc-mutable]
             [kmi.protocol.icoll :as p-coll]
             [kmi.protocol.idissoc :as p-dissoc]
             [kmi.protocol.idissoc-mutable :as p-dissoc-mutable]
             [kmi.protocol.iempty :as p-empty]
             [kmi.protocol.ieq :as p-eq]
             [kmi.protocol.ifind :as p-find]
             [kmi.protocol.ihash :as p-hash]
             [kmi.protocol.inth :as p-nth]
             [kmi.protocol.ipush :as p-push]
             [kmi.protocol.ipush-mutable :as p-push-mutable]
             [kmi.protocol.ipop :as p-pop]
             [kmi.protocol.ipop-mutable :as p-pop-mutable]
             [kmi.protocol.inamespaced :as p-namespaced]
             [kmi.protocol.isize :as p-size]
             [kmi.protocol.ishow :as p-show]
             [kmi.lang.interface-spec :as spec]
             [kmi.lang.interface-common :as interface-common]
             [kmi.lang.interface-collection :as interface-collection]
             [kmi.lang.common-hash :as common-hash]]})

(defn.xt syntax-wrap
  "wraps a function to use syntax"
  {:added "4.0"}
  [f]
  (return (fn [syntax ...]
            (var value (. syntax _value))
            (return (f value ...)))))  

(def.xt SYNTAX_SPEC
   [[p-assoc/IAssoc {:assoc interface-common/assoc}]
    [p-assoc-mutable/IAssocMutable {:assoc-mutable interface-common/assoc-mutable}]
    [p-coll/IColl {:to-iter  interface-common/to-iter
                   :to-array interface-common/to-array}]
    [p-dissoc/IDissoc {:dissoc interface-common/dissoc}]
    [p-dissoc-mutable/IDissocMutable {:dissoc-mutable interface-common/dissoc-mutable}]
    [p-empty/IEmpty {:empty interface-common/empty}]
    [p-eq/IEq    {:eq  interface-common/eq}]
    [p-find/IFind {:find interface-common/find}]
    [p-hash/IHash {:hash interface-common/hash}]
    [p-nth/INth  {:nth  interface-common/nth}]
    [p-push/IPush {:push interface-common/push}]
    [p-push-mutable/IPushMutable {:push-mutable interface-common/push-mutable}]
    [p-pop/IPop {:pop interface-common/pop}]
    [p-pop-mutable/IPopMutable {:pop-mutable interface-common/pop-mutable}]
    [p-namespaced/INamespaced {:name interface-common/get-name
                         :namespace interface-common/get-namespace}]
    [p-size/ISize {:size interface-common/count}]
    [p-show/IShow {:show interface-common/show}]])

(def.xt SYNTAX_PROTOTYPE
  (-> -/SYNTAX_SPEC
      (proto/proto-spec)
      (xtd/obj-map -/syntax-wrap)
      (spec/proto-create)))

(defn.xt syntax-create
  "creates a syntax
 
   (!.js
    (tc/count
    (syn/syntax-create [1 2 3] \"hello\")))"
  {:added "4.0"}
  [value metadata]
  (var syntax {"::" "syntax"
               :_value value
               :_metadata metadata})
  (return (spec/runtime-attach syntax -/SYNTAX_PROTOTYPE)))

(defn.xt get-metadata
  "gets metadata"
  {:added "4.0"}
  [x]
  (return (:? (interface-common/is-syntax? x)
              (. x _metadata)
              nil)))

(defn.xt syntax
  "creates a syntax"
  {:added "4.0"}
  [x metadata]
  (var v (:? (interface-common/is-syntax? x)
             (. x _value)
             x))
  (return (:? (xt/x:nil? metadata)
              v
              (-/syntax-create v metadata))))

(comment
  (comment
  #_[spec/IEdit
      spec/IIndexed
      spec/IIndexedKV
      spec/ILookup
     ]
  (xt/x:proto-spec))
  (comment
  [(def.xt IAssoc  ["assoc"])
   (def.xt IDissoc ["dissoc"])
   (def.xt IColl   ["%/start_string"
                    "%/end_string"
                    "%/sep_string"
                    "to_iter"
                    "to_array"])
   (def.xt IPush   ["push"])
   (def.xt IPop    ["pop"])
   (def.xt ISize   ["size"])
   (def.xt IHash    ["hash"])
   (def.xt IEmpty  ["empty"])
   (def.xt IEq     ["eq"])
   (def.xt IIndexed ["index_of"])
   (def.xt IIndexedKV ["index_of_key"
                       "index_of_val"])
   (def.xt ILookup  ["keys"
                     "vals"
                     "lookup"])
   (def.xt INamespaced  ["name"
                         "namespace"])
   (def.xt IPair        ["key"
                         "val"])
   (def.xt IShow    ["show"])]))
