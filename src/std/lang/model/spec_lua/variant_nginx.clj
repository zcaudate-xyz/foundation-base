(ns std.lang.model.spec-lua.variant-nginx
  (:require [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lang.model.spec-lua :as lua]))

(def +meta-delta+
  {})

(def +grammar-delta+
  {})

(def +meta+
  (lua/variant-meta +meta-delta+))

(def +grammar+
  (lua/variant-grammar :lua.nginx +grammar-delta+))

(def +book+
  (book/book {:lang :lua.nginx
              :parent :lua
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
