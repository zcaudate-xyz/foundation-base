(ns example.brain-custom
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[example.xt.feature.memory-brain :as brain
              :with example.xt.cache.custom-cache]]})
