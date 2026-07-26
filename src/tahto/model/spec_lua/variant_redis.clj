(ns tahto.model.spec-lua.variant-redis
  (:require [tahto.base.book :as book]
            [tahto.core.script :as script]
            [tahto.model.spec-lua :as lua]))

(def +meta-delta+
  "Redis-specific metadata overrides layered onto base Lua."
  {})

(def +grammar-delta+
  "Redis-specific grammar overrides layered onto base Lua."
  {})

(def +meta+
  (lua/variant-meta +meta-delta+))

(def +grammar+
  (lua/variant-grammar +grammar-delta+))

(def +book+
  (book/book {:lang :lua.redis
              :parent :lua
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
