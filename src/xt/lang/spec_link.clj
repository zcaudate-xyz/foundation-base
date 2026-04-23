(ns xt.lang.spec-link
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)

(defspec.xt x:socket-connect [:fn [:xt/str :xt/int :xt/any :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template true}
  x:socket-connect
  {:added "4.1"}
  ([host port opts cb] (list (quote x:socket-connect) host port opts cb)))

(defspec.xt x:socket-send [:fn [:xt/any :xt/str] :xt/any])

(defmacro.xt ^{:standalone true :is-template false}
  x:socket-send
  {:added "4.1"}
  ([conn message] (list (quote x:socket-send) conn message)))

(defspec.xt x:socket-close [:fn [:xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template false}
  x:socket-close
  {:added "4.1"}
  ([conn] (list (quote x:socket-close) conn)))

(defspec.xt x:notify-http [:fn [:xt/str :xt/num :xt/any :xt/str :xt/str :xt/any] :xt/any])

(defmacro.xt ^{:standalone true :is-template true} 
  x:notify-http
  "posts encoded values through fetch"
  {:added "4.1"}
  ([host port value id key opts] (list (quote x:notify-http) host port value id key opts)))
