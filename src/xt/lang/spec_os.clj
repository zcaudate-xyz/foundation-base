(ns xt.lang.spec-os
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defspec.xt x:pwd [:fn [] :xt/str])

(defmacro.xt ^{:standalone true}
  x:pwd
  "reads file content through a callback-based runtime contract"
  {:added "4.1"}
  ([] (list (quote x:pwd))))

(defspec.xt x:file-resolve [:fn [] :xt/str])

(defmacro.xt ^{:standalone true}
  x:pwd
  "reads file content through a callback-based runtime contract"
  {:added "4.1"}
  ([] (list (quote x:pwd))))

(defspec.xt x:shell [:fn [:xt/str :xt/obj [:xt/fn [:xt/any :xt/any] :xt/any]] :xt/any])

(defmacro.xt ^{:standalone true} 
  x:shell
  "executes shell commands through the canonical for:return contract"
  {:added "4.1"}
  ([command opts cb] (list (quote x:shell) command opts cb)))

(defspec.xt x:file-resolve [:fn [:xt/str :xt/obj [:xt/fn [:xt/any :xt/any] :xt/any]]])

(defmacro.xt ^{:standalone true}
  x:file-resolve
  "reads file content through a callback-based runtime contract"
  {:added "4.1"}
  ([root path] (list (quote x:file-resolve) root path)))

(defspec.xt x:file-slurp [:fn [:xt/str :xt/obj [:xt/fn [:xt/any :xt/any] :xt/any]]])

(defmacro.xt ^{:standalone true}
  x:file-slurp
  "reads file content through a callback-based runtime contract"
  {:added "4.1"}
  ([path opts cb] (list (quote x:file-slurp) path opts cb)))


(defspec.xt x:file-spit [:fn [:xt/str :xt/str :xt/obj [:xt/fn [:xt/any :xt/any] :xt/any]]])

(defmacro.xt ^{:standalone true}
  x:file-spit
  "writes file content through a callback-based runtime contract"
  {:added "4.1"}
  ([path value opts cb] (list (quote x:file-spit) path value opts cb)))
