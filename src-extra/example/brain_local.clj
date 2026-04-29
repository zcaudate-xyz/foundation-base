(ns example.brain-local
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[example.xt.feature.memory-brain :as brain
              :with example.js.cache.localstore]]})
