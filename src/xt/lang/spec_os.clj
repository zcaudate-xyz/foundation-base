(ns xt.lang.spec-os
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defspec.xt x:slurp-file [:fn [:xt/str :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true}
  x:slurp-file
  "reads file content through a callback-based runtime contract"
  {:added "4.1"}
  ([path opts cb] (list (quote x:slurp-file) path opts cb)))

(defspec.xt x:spit-file [:fn [:xt/str :xt/any :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true}
  x:spit-file
  "writes file content through a callback-based runtime contract"
  {:added "4.1"}
  ([path value opts cb] (list (quote x:spit-file) path value opts cb)))

(defspec.xt x:shell [:fn [:xt/str :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true} 
  x:shell
  "executes shell commands through the canonical for:return contract"
  {:added "4.1"}
  ([command opts cb] (list (quote x:shell) command opts cb)))

(defspec.xt x:with-delay [:fn [:xt/int :xt/any] :xt/any])

(defmacro.xt ^{:standalone true} 
  x:with-delay
  "delays asynchronous js computations"
  {:added "4.1"}
  ([ms value cb] (list (quote x:with-delay) ms value cb)))
