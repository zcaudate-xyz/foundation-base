(ns example.brain-test
  (:require [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib])
  (:use code.test))

(fact "stores contract metadata on backend modules"
  (impl/with:library [(impl/clone-default-library)]
    (require '[example.xt.protocol.cache])
    (require '[example.js.cache.localstore])
    (require '[example.js.cache.redis])
    [(-> (lib/get-module (impl/runtime-library)
                         :js
                         'example.js.cache.localstore)
         :implements)
     (-> (lib/get-module (impl/runtime-library)
                         :js
                         'example.js.cache.redis)
         :implements)])
  => '[[example.xt.protocol.cache]
       [example.xt.protocol.cache]])

(fact "rejects backend modules that do not satisfy their declared contract"
  (impl/with:library [(impl/clone-default-library)]
    (require '[example.xt.protocol.cache])
    (require '[example.js.cache.localstore])
    (let [library (impl/runtime-library)]
      (lib/install-module! library
                           :js
                           'example.js.cache.invalid
                           {:implements 'example.xt.protocol.cache})
      (lib/validate-module-implements (lib/get-snapshot library)
                                      :js
                                      'example.js.cache.invalid)))
  => (throws))
