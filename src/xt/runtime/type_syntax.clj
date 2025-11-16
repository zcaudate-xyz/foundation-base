(ns xt.runtime.type-syntax
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.base-runtime :as rt :with [defvar.xt]]
             [xt.runtime.interface-spec :as spec]
             [xt.runtime.interface-common :as interface-common]
             [xt.runtime.interface-collection :as interface-collection]
             [xt.runtime.common-hash :as common-hash]]})

(defn.xt syntax-wrap
  "wraps a function to use syntax"
  {:added "4.0"}
  [f]
  (return (fn [syntax ...]
            (var value (. syntax _value))
            (return (f value ...)))))  

(def.xt SYNTAX_SPEC
  [[spec/IAssoc {:assoc interface-common/assoc}]
   [spec/IAssocMutable {:assoc-mutable interface-common/assoc-mutable}]
   [spec/IColl {:to-iter  interface-common/to-iter
                :to-array interface-common/to-array}]
   [spec/IDissoc {:dissoc interface-common/dissoc}]
   [spec/IDissocMutable {:dissoc-mutable interface-common/dissoc-mutable}]
   [spec/IEmpty {:empty interface-common/empty}]
   [spec/IEq    {:eq  interface-common/eq}]
   [spec/IFind {:find interface-common/find}]
   [spec/IHash {:hash interface-common/hash}]
   [spec/INth  {:nth  interface-common/nth}]
   [spec/IPush {:push interface-common/push}]
   [spec/IPushMutable {:push-mutable interface-common/push-mutable}]
   [spec/IPop {:pop interface-common/pop}]
   [spec/IPopMutable {:pop-mutable interface-common/pop-mutable}]
   [spec/INamespaced {:name interface-common/get-name
                      :namespace interface-common/get-namespace}]
   [spec/ISize {:size interface-common/count}]
   [spec/IShow {:show interface-common/show}]])

(def.xt SYNTAX_PROTOTYPE
  (-> -/SYNTAX_SPEC
      (k/proto-spec)
      (k/obj-map -/syntax-wrap)
      (k/proto-create)))

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
  (k/set-proto syntax -/SYNTAX_PROTOTYPE)
  (return syntax))

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
  (return (:? (k/nil? metadata)
              v
              (-/syntax-create v metadata))))

(comment
  (comment
  #_[spec/IEdit
      spec/IIndexed
      spec/IIndexedKV
      spec/ILookup
     ]
  (k/proto-spec))
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
