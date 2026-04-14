(ns xt.runtime.type-keyword
  (:require [std.lang :as l])
  (:refer-clojure :exclude [keyword]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.runtime.interface-common :as interface-common]
             [xt.runtime.interface-spec :as spec]
             [xt.runtime.common-hash :as common-hash]]})

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
  (var otype (xt/x:type-native o))
  (var oclass (:? (xt/x:is-object? o)
                  (xt/x:get-key o "::" otype)
                  otype))
  (return (and (== "keyword" oclass)
               (== (. sym _ns)   (. o _ns))
               (== (. sym _name) (. o _name)))))

(def.xt KEYWORD_SPEC
  [[spec/IEq         {:eq        -/keyword-eq}]
   [spec/IHash       {:hash      (interface-common/wrap-with-cache
                                  -/keyword-hash)}]
   [spec/INamespaced {:name      interface-common/get-name
                      :namespace interface-common/get-namespace} ]
   [spec/IShow       {:show      -/keyword-show}]])

(def.xt KEYWORD_PROTOTYPE
  (-> -/KEYWORD_SPEC
      (spec/proto-spec)
      (xt/x:proto-create)))

(defn.xt keyword-create
  "creates a keyword"
  {:added "4.0"}
  [ns name]
  (var sym {"::" "keyword"
            :_ns   ns
            :_name name})
  (xt/x:proto-set sym -/KEYWORD_PROTOTYPE nil)
  (return sym))

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
