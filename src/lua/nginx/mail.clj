(ns lua.nginx.mail
  (:require [std.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [send]))

(l/script :lua.nginx
  {:import [["resty.mail" :as ngxmail]
            ["resty.mail" :as ngxmail]]})

(f/template-entries [l/tmpl-macro {:base "mail"
                                   :inst "mailer"
                                   :tag "lua"}]
  [[send   [m]]])

