(ns std.lang.base.compile-specialization-conflict-test
  (:require [std.lang.base.compile :refer :all]
            [std.lang.base.impl :as impl]
            [std.make.compile :as compile])
  (:use code.test))

(fact "rejects module.directory builds that pick conflicting backends for one contract"
  (do (require '[xt.lang.spec-base] :reload)
      (require '[js.core] :reload)
      (impl/with:library [(impl/clone-default-library)]
        (require '[example.xt.protocol.cache] :reload)
        (require '[example.xt.feature.memory-brain] :reload)
        (require '[example.js.cache.localstore] :reload)
        (require '[example.js.cache.redis] :reload)
        (require '[example.brain-local] :reload)
        (require '[example.brain-redis] :reload)
        (compile/with:mock-compile
          (compile-module-directory-selected
           :directory
           ['example.brain-local
            'example.brain-redis]
           {:lang :xtalk
            :main 'example.brain
            :root ".build"
            :target "src"}))))
  => (throws))

(fact "does not reject the same conflict set outside module.directory builds"
  (do (require '[xt.lang.spec-base] :reload)
      (require '[js.core] :reload)
      (impl/with:library [(impl/clone-default-library)]
        (require '[example.xt.protocol.cache] :reload)
        (require '[example.xt.feature.memory-brain] :reload)
        (require '[example.js.cache.localstore] :reload)
        (require '[example.js.cache.redis] :reload)
        (require '[example.brain-local] :reload)
        (require '[example.brain-redis] :reload)
        (with-redefs [compile-module-single (fn [_]
                                              [".build/fake.js" "BODY"])]
          (compile-module-directory-selected
           :graph
           ['example.brain-local
            'example.brain-redis]
           {:lang :xtalk
            :main 'example.brain
            :root ".build"
            :target "src"}))))
  => (contains {:files pos-int?}))
