(ns std.lang.model.spec-lua.variant-nginx
  (:require [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lang.model.spec-lua :as lua]))

(def +meta-delta+
  "Nginx-specific metadata overrides layered onto base Lua."
  {})

(def +grammar-delta+
  "Nginx-specific grammar overrides layered onto base Lua."
  {})

(def +meta+
  (lua/variant-meta +meta-delta+))

(def +grammar+
  (lua/variant-grammar +grammar-delta+))

(def +book+
  (book/book {:lang :lua.nginx
              :parent :lua
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
