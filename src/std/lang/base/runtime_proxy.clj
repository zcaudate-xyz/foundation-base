(ns std.lang.base.runtime-proxy
  (:require [std.protocol.context :as protocol.context]
            [std.protocol.component :as protocol.component]
            [std.lib.context.space :as space]
            [std.lang.base.util :as ut]
            [std.string :as str]))

(defn- rt-proxy-string
  ([{:keys [namespace redirect lang]}]
   (str "#rt.proxy" [lang redirect namespace])))

(defn proxy-get-rt
  "gets the redirected runtime"
  {:added "4.0"}
  [redirect lang]
  (space/space:rt-current redirect (ut/lang-context lang)))

(defn proxy-raw-eval
  "evaluates the raw string"
  {:added "4.0"}
  [{:keys [redirect lang]} string]
  (protocol.context/-raw-eval
   (proxy-get-rt redirect lang)
   string))

(defn proxy-init-ptr
  "initialises ptr"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr]
  (protocol.context/-init-ptr
   (proxy-get-rt redirect lang)
   ptr))

(defn proxy-tags-ptr
  "gets the ptr tags"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr]
  (protocol.context/-tags-ptr
   (proxy-get-rt redirect lang)
   ptr))

(defn proxy-deref-ptr
  "dereefs the pointer"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr]
  (protocol.context/-deref-ptr
   (proxy-get-rt redirect lang)
   ptr))

(defn proxy-display-ptr
  "displays the pointer"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr]
  (protocol.context/-display-ptr
   (proxy-get-rt redirect lang)
   ptr))

(defn proxy-invoke-ptr
  "invokes the pointer"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr args]
  (protocol.context/-invoke-ptr
   (proxy-get-rt redirect lang)
   ptr
   args))

(defn proxy-transform-in-ptr
  "transforms the pointer on in"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr args]
  (protocol.context/-transform-in-ptr
   (proxy-get-rt redirect lang)
   ptr
   args))

(defn proxy-transform-out-ptr
  "transforms the pointer on out"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr return]
  (protocol.context/-transform-out-ptr
   (proxy-get-rt redirect lang)
   ptr
   return))

(defn proxy-started?
  "checks if proxied has started"
  {:added "4.0"}
  [{:keys [redirect lang]}]
  (protocol.component/-started?
   (proxy-get-rt redirect lang)))

(defn proxy-stopped?
  "checks if proxied has stopped"
  {:added "4.0"}
  [{:keys [redirect lang]}]
  (protocol.component/-stopped?
   (proxy-get-rt redirect lang)))

(defn proxy-remote?
  "checks if proxied is remote"
  {:added "4.0"}
  [{:keys [redirect lang]}]
  (protocol.component/-remote?
   (proxy-get-rt redirect lang)))

(defn proxy-info
  "gets the proxied info"
  {:added "4.0"}
  [{:keys [redirect lang]} level]
  (protocol.component/-info
   (proxy-get-rt redirect lang)
   level))

(defn proxy-health
  "checks the proxied health"
  {:added "4.0"}
  [{:keys [redirect lang]}]
  (protocol.component/-health
   (proxy-get-rt redirect lang)))
