(ns example.build.cache-brain
  (:require [hara.lang :as l]
            [std.make :as make :refer [def.make]]))

(def +brain-specializations+
  [{:id :brain-custom
    :entry 'example.brain-custom
    :source 'example.xt.feature.memory-brain
    :target 'example.brain-custom.brain
    :backend 'example.xt.cache.custom-cache
    :bindings {'example.xt.protocol.cache
               'example.xt.cache.custom-cache}}
   {:id :brain-local
    :entry 'example.brain-local
    :source 'example.xt.feature.memory-brain
    :backend 'example.js.cache.localstore
    :bindings {'example.xt.protocol.cache
               'example.js.cache.localstore}}
   {:id :brain-redis
    :entry 'example.brain-redis
    :source 'example.xt.feature.memory-brain
    :backend 'example.js.cache.redis
    :bindings {'example.xt.protocol.cache
               'example.js.cache.redis}}])

(defn emit-specialization-manifest
  [_]
  (pr-str +brain-specializations+))

(def.make PROJECT
  {:tag "example.cache-brain"
   :build ".build/example-cache-brain"
   :triggers '#{example.xt.protocol.cache
                example.xt.feature.memory-brain
                example.xt.cache.custom-cache
                example.js.cache.localstore
                example.js.cache.redis
                example.python.cache.localstore
                example.python.cache.redis
                example.brain-custom
                example.brain-local
                example.brain-redis}
   :sections {:manifests [{:type :custom
                           :file "specializations.edn"
                           :fn #'emit-specialization-manifest}]}
   :default [{:type :module.graph
              :lang :xtalk
              :main 'example.xt
              :search ["src-extra/example/xt"]
              :target "xt"}
             {:type :module.graph
              :lang :js
              :main 'example.js.cache
              :search ["src-extra/example/js"]
              :target "js"}
             {:type :module.graph
              :lang :python
              :main 'example.python.cache
              :search ["src-extra/example/python"]
              :target "python"}]})

(comment
  (make/build-all PROJECT))
