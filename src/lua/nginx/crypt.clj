(ns lua.nginx.crypt
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str])
  (:refer-clojure :exclude [print flush time re-find]))

(l/script :lua
  {:require [[xt.lang.base-lib :as k :suppress true]
             [lua.core :as u]]
   :import [["crypt.core" :as ngxcryptcore]]})

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
  (while (< (k/len output) max-salt)
    (var i (u/random 1 64))
    (:= output (k/cat output (u/substring -/CHARS i i))))
  (return (k/cat prefix output)))

