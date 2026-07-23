(ns xt.lang.spec-os
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defspec.xt x:pwd [:fn [] :xt/str])

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
  ([command root cb] (list (quote x:shell) command root cb)))

(defspec.xt x:file-resolve [:fn [:xt/str :xt/obj [:xt/fn [:xt/any :xt/any] :xt/any]]])

(defmacro.xt ^{:standalone true}
  x:file-resolve
  "reads file content through a callback-based runtime contract"
  {:added "4.1"}
  ([root path] (list (quote x:file-resolve) root path)))

(defspec.xt x:file-read [:fn [:xt/str] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:file-read
  "reads bytes and returns a native promise"
  {:added "4.1"}
  ([path] (list (quote x:file-read) path)))

(defspec.xt x:file-write [:fn [:xt/str :xt/any] :xt/promise])

(defmacro.xt ^{:standalone true}
  x:file-write
  "writes bytes and returns a native promise"
  {:added "4.1"}
  ([path bytes] (list (quote x:file-write) path bytes)))
