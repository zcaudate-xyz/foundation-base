(ns lua.nginx.crypt
  (:require [std.lang :as l])
  (:refer-clojure :exclude [print flush time re-find]))

(l/script :lua
  {:import [["crypt.core" :as ngxcryptcore]] :require [[xt.lang.common-lib :suppress true :as k] [lua.core :as u] [xt.lang.common-spec :as xt]]})

(def.lua CHARS "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")

(def.lua METHODS
  {:md5 {:prefix "$1$"
         :min-salt 8
         :max-salt 8}
   :bf     {:prefix "$2a$06$"
            :min-salt 22
            :max-salt 22}})

(defn.lua crypt
  "same functionality as postgres crypt"
  {:added "4.0"}
  [key salt]
  (return (ngxcryptcore.crypt key salt)))

(defn.lua gen-salt
  "generates salt compatible with pgcrypto libraries"
  {:added "4.0"}
  [method]
  (var #{prefix
         max-salt} (. -/METHODS [method]))
  (var output "")
  (while (< (xt/x:len output) max-salt)
    (var i (u/random 1 64))
    (:= output (xt/x:cat output (u/substring -/CHARS i i))))
  (return (xt/x:cat prefix output)))

