(ns kmi.lang.type-keyword
  (:require [hara.lang :as l])
  (:refer-clojure :exclude [keyword]))

(l/script :xtalk
  {:require [[kmi.lang.protocol-base :as p]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]
             [kmi.lang.common-util :as util]
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
               (== (xt/x:get-key sym "_ns")   (xt/x:get-key o "_ns"))
               (== (xt/x:get-key sym "_name") (xt/x:get-key o "_name")))))

(proto/defimpl.xt ^{:rt/tag "keyword"} Keyword
  [_ns _name]
  p/IEq
  {eq -/keyword-eq}
  p/IHash
  {hash (util/wrap-with-cache -/keyword-hash [nil])}
  p/INamespaced
  {name      util/get-name
   namespace util/get-namespace}
  p/IShow
  {show -/keyword-show})

(defn.xt keyword-create
  "creates a keyword"
  {:added "4.0"}
  [ns name]
  (return (-/Keyword ns name)))

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
