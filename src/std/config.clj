(ns std.config
  (:require [std.config.global :as global]
            [std.config.resolve :as resolve]
            [std.config.secure :as secure]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [resolve load]))

(f/intern-in global/global
             resolve/load
             resolve/resolve
             secure/resolve-key
             secure/encrypt-text
             secure/decrypt-text)

(defn get-session
  "retrieves the global session"
  {:added "3.0"}
  ([]
   @global/+session+))

(defn swap-session
  "updates the global session"
  {:added "3.0"}
  ([f & args]
   (apply swap! global/+session+ f args)))

(defn clear-session
  "clears the global session"
  {:added "3.0"}
  ([]
   (reset! global/+session+ {})))

(def +directives+
  [:format
   :str
   :or
   :case
   :error
   :eval

   :merge
   :root
   :parent

   :global
   :properties
   :env
   :project

   :include
   :file
   :resource])

(def +opt-keys+
  [:type
   :secured
   :key
   :default])

(def +opt-types+
  [;; builtin
   :text
   :edn
   :key

   ;; extensions
   :gpg.public
   :gpg
   :yaml
   :json])

