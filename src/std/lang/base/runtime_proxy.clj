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
  "TODO"
  {:added "4.0"}
  [redirect lang]
  (space/space:rt-current redirect (ut/lang-context lang)))

(defn proxy-raw-eval
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]} string]
  (protocol.context/-raw-eval
   (proxy-get-rt redirect lang)
   string))

(defn proxy-init-ptr
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr]
  (protocol.context/-init-ptr
   (proxy-get-rt redirect lang)
   ptr))

(defn proxy-tags-ptr
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr]
  (protocol.context/-tags-ptr
   (proxy-get-rt redirect lang)
   ptr))

(defn proxy-deref-ptr
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr]
  (protocol.context/-deref-ptr
   (proxy-get-rt redirect lang)
   ptr))

(defn proxy-display-ptr
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr]
  (protocol.context/-display-ptr
   (proxy-get-rt redirect lang)
   ptr))

(defn proxy-invoke-ptr
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr args]
  (protocol.context/-invoke-ptr
   (proxy-get-rt redirect lang)
   ptr
   args))

(defn proxy-transform-in-ptr
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr args]
  (protocol.context/-transform-in-ptr
   (proxy-get-rt redirect lang)
   ptr
   args))

(defn proxy-transform-out-ptr
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]} ptr return]
  (protocol.context/-transform-out-ptr
   (proxy-get-rt redirect lang)
   ptr
   return))

(defn proxy-started?
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]}]
  (protocol.component/-started?
   (proxy-get-rt redirect lang)))

(defn proxy-stopped?
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]}]
  (protocol.component/-stopped?
   (proxy-get-rt redirect lang)))

(defn proxy-remote?
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]}]
  (protocol.component/-remote?
   (proxy-get-rt redirect lang)))

(defn proxy-info
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]} level]
  (protocol.component/-info
   (proxy-get-rt redirect lang)
   level))

(defn proxy-health
  "TODO"
  {:added "4.0"}
  [{:keys [redirect lang]}]
  (protocol.component/-health
   (proxy-get-rt redirect lang)))
