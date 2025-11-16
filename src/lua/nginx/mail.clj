(ns lua.nginx.mail
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [send]))

(l/script :lua
  {:import [["resty.mail" :as ngxmail]
            ["resty.mail" :as ngxmail]]})

(h/template-entries [l/tmpl-macro {:base "mail"
                                   :inst "mailer"
                                   :tag "lua"}]
  [[send   [m]]])

