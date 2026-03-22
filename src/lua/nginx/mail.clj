(ns lua.nginx.mail
  (:require [std.lang :as l]
            [std.lib.foundation])
  (:refer-clojure :exclude [send]))

(l/script :lua
  {:import [["resty.mail" :as ngxmail]
            ["resty.mail" :as ngxmail]]})

(std.lib.foundation/template-entries [l/tmpl-macro {:base "mail"
                                   :inst "mailer"
                                   :tag "lua"}]
  [[send   [m]]])

