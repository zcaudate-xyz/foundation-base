(ns xt.feature.brain-memory-test
  (:require [std.lang :as l]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.workspace :as workspace])
  (:use code.test))

^{:refer std.lang.base.library/install-module-specialized! :added "4.1"}
(fact "supports multiple specialized brain-memory modules in the same library"
  (impl/with:library [(impl/clone-default-library)]
    (require '[xt.lang.spec-base] :reload)
    (require '[xt.lang.common-data] :reload)
    (require '[xt.protocol.cache] :reload)
    (require '[xt.feature.brain-memory] :reload)
    (require '[js.cache.localstore] :reload)
    (require '[js.cache.cache-custom] :reload)
    (let [library (impl/runtime-library)]
      (lib/install-module-specialized! library
                                       :xtalk
                                       'xt.feature.brain-memory
                                       'web-app
                                       {:bindings {'xt.protocol.cache 'js.cache.localstore}})
      (lib/install-module-specialized! library
                                       :xtalk
                                       'xt.feature.brain-memory
                                       'web-app-cache
                                       {:bindings {'xt.protocol.cache 'js.cache.cache-custom}})
      [(get-in (lib/get-module library :xtalk 'web-app) [:link 'cache])
       (get-in (lib/get-module library :xtalk 'web-app-cache) [:link 'cache])]))
  => ['js.cache.localstore 'js.cache.cache-custom])

^{:refer std.lang.base.workspace/emit-module :added "4.1"}
(fact "emits specialized brain-memory copies independently"
  (impl/with:library [(impl/clone-default-library)]
    (require '[xt.lang.spec-base] :reload)
    (require '[xt.lang.common-data] :reload)
    (require '[xt.protocol.cache] :reload)
    (require '[xt.feature.brain-memory] :reload)
    (require '[js.cache.localstore] :reload)
    (require '[js.cache.cache-custom] :reload)
    (let [library (impl/runtime-library)]
      (lib/install-module-specialized! library
                                       :xtalk
                                       'xt.feature.brain-memory
                                       'web-app
                                       {:bindings {'xt.protocol.cache 'js.cache.localstore}})
      (lib/install-module-specialized! library
                                       :xtalk
                                       'xt.feature.brain-memory
                                       'web-app-cache
                                       {:bindings {'xt.protocol.cache 'js.cache.cache-custom}})
      (every? string?
              [(workspace/emit-module :xtalk 'web-app)
               (workspace/emit-module :xtalk 'web-app-cache)])))
  => true)
