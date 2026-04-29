(ns example.xt.feature.memory-brain-test
  (:require [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib])
  (:use code.test))

^{:refer std.lang.base.library/install-module-specialized! :added "4.1"}
(fact "specializes the xtalk example feature against the xtalk example cache"
  (impl/with:library [(impl/clone-default-library)]
    (require '[xt.lang.spec-base] :reload)
    (require '[example.xt.protocol.cache] :reload)
    (require '[example.xt.feature.memory-brain] :reload)
    (require '[example.xt.cache.custom-cache] :reload)
    (let [library (impl/runtime-library)]
      (lib/install-module-specialized! library
                                       :xtalk
                                       'example.xt.feature.memory-brain
                                       'example.xt.feature.memory-brain-custom
                                       {:bindings {'example.xt.protocol.cache
                                                   'example.xt.cache.custom-cache}})
      (get-in (lib/get-module library :xtalk 'example.xt.feature.memory-brain-custom)
              [:link 'cache])))
  => 'example.xt.cache.custom-cache)
