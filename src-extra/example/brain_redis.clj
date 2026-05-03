(ns example.brain-redis
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[example.xt.feature.memory-brain :as brain
              :with example.js.cache.redis]]})
