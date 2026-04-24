(ns xt.lang.spec-runtime-http
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defspec.xt x:b64-encode [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true} 
  x:b64-encode
  "encodes base64 strings"
  {:added "4.1"}
  ([value] (list (quote x:b64-encode) value)))

(defspec.xt x:b64-decode [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true} 
  x:b64-decode
  "decodes base64 strings"
  {:added "4.1"}
  ([value] (list (quote x:b64-decode) value)))


(defspec.xt x:uri-encode [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true} 
  x:uri-encode
  "encodes uri components"
  {:added "4.1"}
  ([value] (list (quote x:uri-encode) value)))

(defspec.xt x:uri-decode [:fn [:xt/str] :xt/str])

(defmacro.xt ^{:standalone true} 
  x:uri-decode
  "decodes uri components"
  {:added "4.1"}
  ([value] (list (quote x:uri-decode) value)))
