(ns xt.lang.spec-runtime-os
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defspec.xt x:slurp-file [:fn [:xt/str :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template true}
  x:slurp-file
  "reads file content through a callback-based runtime contract"
  {:added "4.1"}
  ([path opts cb] (list (quote x:slurp-file) path opts cb)))

(defspec.xt x:spit-file [:fn [:xt/str :xt/any :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template true}
  x:spit-file
  "writes file content through a callback-based runtime contract"
  {:added "4.1"}
  ([path value opts cb] (list (quote x:spit-file) path value opts cb)))

(defspec.xt x:shell [:fn [:xt/str :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:shell
  "executes shell commands through the canonical for:return contract"
  {:added "4.1"}
  ([command opts cb] (list (quote x:shell) command opts cb)))
