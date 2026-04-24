(ns xt.lang.spec-runtime-os
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

(defspec.xt x:thread-spawn [:fn [:xt/fn] :xt/any])

(defmacro.xt ^{:standalone true} 
  x:thread-spawn
  "spawns js promise-backed threads"
  {:added "4.1"}
  ([f] (list (quote x:thread-spawn) f)))

(defspec.xt x:thread-join [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true} 
  x:thread-join
  "throws for unsupported js thread joins"
  {:added "4.1"}
  ([thread] (list (quote x:thread-join) thread)))

(defspec.xt x:with-delay [:fn [:xt/int :xt/any] :xt/any])

(defmacro.xt ^{:standalone true} 
  x:with-delay
  "delays asynchronous js computations"
  {:added "4.1"}
  ([ms value] (list (quote x:with-delay) ms value)))

(defspec.xt x:start-interval [:fn [:xt/int :xt/fn] :xt/any])

(defmacro.xt ^{:standalone true} 
  x:start-interval
  "keeps the start-interval wrapper intact"
  {:added "4.1"}
  ([ms f] (list (quote x:start-interval) ms f)))

(defspec.xt x:stop-interval [:fn [:xt/str] :xt/any])

(defmacro.xt ^{:standalone true} 
  x:stop-interval
  "keeps the stop-interval wrapper intact"
  {:added "4.1"}
  ([id] (list (quote x:stop-interval) id)))
