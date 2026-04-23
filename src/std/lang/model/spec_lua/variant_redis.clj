(ns std.lang.model.spec-lua.variant-redis
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
  (lua/variant-grammar :lua.redis +grammar-delta+))

(def +book+
  (book/book {:lang :lua.redis
              :parent :lua
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
