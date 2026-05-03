(ns example.brain-conflict
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[example.xt.feature.memory-brain :as brain-local
              :with example.js.cache.localstore]
             [example.xt.feature.memory-brain :as brain-redis
              :with example.js.cache.redis]]})
