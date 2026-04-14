(ns xt.runtime.type-keyword
  (:require [std.lang :as l])
  (:refer-clojure :exclude [keyword]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.base-runtime :as rt :with [defvar.xt]]
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
  (return
   (-> (xt/x:get-key common-hash/SEED "keyword")
       (xt/x:bit-xor (common-hash/hash-string
                   (xt/x:sym-full _ns _name))))))

(defn.xt keyword-show
  "shows the keyword"
  {:added "4.0"}
  [sym]
  (var #{_ns _name} sym)
  (return
   (xt/x:cat ":" (xt/x:sym-full _ns _name))))

(defn.xt keyword-eq
  "gets keyword equality"
  {:added "4.0"}
  [sym o]
  (return (and (== "keyword" (xt/x:type-class o))
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
      (xt/x:proto-spec)
      (xt/x:proto-create)))

(defn.xt keyword-create
  "creates a keyword"
  {:added "4.0"}
  [ns name]
  (var sym {"::" "keyword"
            :_ns   ns
            :_name name})
  (xt/x:set-proto sym -/KEYWORD_PROTOTYPE)
  (return sym))

(defn.xt keyword
  "creates the keyword or pulls it from cache"
  {:added "4.0"}
  [ns name]
  (var lu -/KEYWORD_LOOKUP)
  (var key (xt/x:sym-full ns name))
  (var out (xt/x:get-key lu key))
  (when (xt/x:nil? out)
    (var sym (-/keyword-create ns name))
    (xt/x:set-key lu key sym)
    (return sym))
  (return out))

