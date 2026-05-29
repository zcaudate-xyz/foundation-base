(ns demo-xtdb-backbone.build-test
  (:require [demo-xtdb-backbone.build :as build]
            [demo-xtdb-backbone.app.worker-base])
  (:use code.test))

(fact "the demo build declares the expected emitted files"
  (every? (set build/+expected-files+)
          ["webapp/app.js"
           "sharedworker/src/main.js"
           "webapp/index.html"])
  => true)

(fact "the generic worker-base namespace exposes sharedworker helpers"
  (every? (set (keys (ns-publics 'demo-xtdb-backbone.app.worker-base)))
          '[custom-config
            merge-config
            prepare-supabase-source
            prepare-sqlite-source
            worker-config
            ensure-shared-state
            boot-port
            runtime-init])
  => true)
