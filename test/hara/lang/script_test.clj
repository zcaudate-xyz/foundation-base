(ns hara.lang.script-test
  (:require [lua.core]
             [hara.lang :as l]
             [hara.lang.book :as book]
             [hara.common.emit-prep-lua-test :as prep-lua]
             [hara.lang.impl :as impl]
             [hara.lang.library :as lib]
             [hara.lang.library-snapshot :as snap]
             [hara.lang.runtime :as rt]
             [hara.lang.script :as script]
             [hara.model.spec-js :as js]
             [hara.model.spec-lua :as lua]
             [hara.model.spec-xtalk :as xtalk]
             [std.lib.env :as env])
  (:use code.test))

(def +library+
  (impl/clone-default-library))

(def +runtime-config-key+
  :port)

(def +runtime-config-value+
  17001)

(def +runtime-config-form+
  "ready")

(def +runtime-config+
  {:port +runtime-config-value+
   :startup {:args ["/bin/sh"
                    "-lc"
                    +runtime-config-form+]}})

(rt/install-lang! :lua)

(l/script+ [:LUA.0 :lua]
  {:runtime :oneshot
   :require [[xt.lang.common-data :as xtd]]})

(fact "reloads required modules into the active library when they are missing"

  (let [xlib (lib/library:create {})]
    (impl/with:library [xlib]
      (script/install xtalk/+book+)
      (script/install js/+book+)
      (script/script-ns-import :js {:require '[[xt.lang.spec-base :as xt]]})
      (-> (lib/get-module xlib :js 'xt.lang.spec-base)
          :fragment
          not-empty
          boolean)))
  => true)

(fact "allows books without any exported macros"

  (script/script-macro-import {:macros []
                               :highlights []})
  => '[#{} #{}])

^{:refer hara.lang.script/install :added "4.0"}
(fact "installs a language"

  (impl/with:library [+library+]
    (binding [*ns* (the-ns 'hara.model.spec-lua)]
      (script/install lua/+book+)))
  => vector?)

^{:refer hara.lang.script/script-ns-import :added "4.0"}
(fact "imports the namespace and sets a primary flag"

  (impl/with:library [+library+]
    (script/script-ns-import {:require '[[xt.lang.common-data :as xtd :primary true]]}))
  => '#{xt.lang.common-data})

^{:refer hara.lang.script/script-macro-import :added "4.0"}
(fact "import macros into the namespace"

  (impl/with:library [+library+]
    (script/script-macro-import (l/get-book (l/runtime-library)
                                            :lua)))
  => vector?)

^{:refer hara.lang.script/script-require-target-id :added "4.1"}
(fact "constructs a target module id from module, source, and alias"

  (script/script-require-target-id 'my.module 'source.core nil)
  => 'my.module.source.core

  (script/script-require-target-id 'my.module 'source.core 'src)
  => 'my.module.src

  (script/script-require-target-id 'my.module 'source.core '[prefix src])
  => 'my.module.src)

^{:refer hara.lang.script/script-specialize-merge-contracts :added "4.1"}
(fact "merges specialization bindings into a contract map"

  (script/script-specialize-merge-contracts :demo 'current {}
                                            {'source.core {:backend 'backend.core
                                                           :bindings {'contract.core 'backend.core}}})
  => {'contract.core {:backend 'backend.core
                      :source 'source.core
                      :declared-backend 'backend.core}}

  (script/script-specialize-merge-contracts :demo 'current
                                            {'contract.core {:backend 'backend.core
                                                             :source 'source.core
                                                             :declared-backend 'backend.core}}
                                            {'other.core {:backend 'alt.core
                                                          :bindings {'contract.core 'alt.core}}})
  => (throws))

^{:refer hara.lang.script/script-specialize-require :added "4.1"}
(fact "resolves a specialization require spec"

  (let [lib (lib/library:create {})]
    (lib/install-book! lib xtalk/+book+)
    (lib/install-book! lib lua/+book+)
    (lib/install-module! lib :lua 'demo.contract {})
    (lib/install-module! lib :lua 'demo.source {:require '[[demo.contract :as cache]]})
    (lib/install-module! lib :lua 'demo.backend {:implements '[demo.contract]})
    (script/script-specialize-require :lua 'demo.current lib '[demo.source :as src :with demo.backend]))
  => '{:require-spec [demo.current.src :as src]
       :specialize {demo.source {:backend demo.backend
                                 :bindings {demo.contract demo.backend}
                                 :contracts [demo.contract]
                                 :source-lang :lua
                                 :backend-lang :lua
                                 :target demo.current.src}}})

^{:refer hara.lang.script/script-specialize-config :added "4.1"}
(fact "processes config require specs for specialization"

  (let [lib (lib/library:create {})]
    (lib/install-book! lib xtalk/+book+)
    (lib/install-book! lib lua/+book+)
    (lib/install-module! lib :lua 'demo.contract {})
    (lib/install-module! lib :lua 'demo.source {:require '[[demo.contract :as cache]]})
    (lib/install-module! lib :lua 'demo.backend {:implements '[demo.contract]})
    (script/script-specialize-config :lua 'demo.current
                                     {:require '[[demo.source :as src :with demo.backend]]}
                                     lib))
  => '{:require [[demo.current.src :as src]]
       :specialize {demo.source {:backend demo.backend
                                 :bindings {demo.contract demo.backend}
                                 :contracts [demo.contract]
                                 :source-lang :lua
                                 :backend-lang :lua
                                 :target demo.current.src}}})

^{:refer hara.lang.script/script-fn-base :added "4.0"}
(fact "setup for the runtime"

  (impl/with:library [+library+]
    (binding [book/*skip-check* true]
      (keys (script/script-fn-base :lua 'hara.lang.script-test
                                   {:require '[[xt.lang.common-data :as xtd]]}
                                   (l/runtime-library)))))
  => (contains [:module :module/internal :module/primary]))

^{:refer hara.lang.script/script-fn :added "4.0"}
(fact "calls the regular setup script for the namespace"

  (script/script-fn :lua)
  => map?)

^{:refer hara.lang.script/script :added "4.0"}
(fact "script macro"

  (script/script :lua)
  => map?)

^{:refer hara.lang.script/script-test-prep :added "4.0"}
(fact "preps the current namespace"

  (script/script-test-prep :js {})
  => (contains {:module 'hara.lang.script-test}))

^{:refer hara.lang.script/resolve-runtime-config :added "4.1"}
(fact "resolves quoted vars, symbols, forms, and config keys"

  (script/resolve-runtime-config '+runtime-config+)
  => {:port 17001
      :startup {:args ["/bin/sh" "-lc" "ready"]}}

  (script/script-test-prep
   :js
   {:runtime :basic
    :config {'+runtime-config-key+ '+runtime-config-value+
             :startup {:args ["/bin/sh"
                              "-lc"
                              '(str "rea" "dy")]}}})
  => (contains {:port 17001
                :startup {:args ["/bin/sh" "-lc" "ready"]}}))

^{:refer hara.lang.script/script-test :added "4.0"}
(fact "the `script-` function call"

  (script/script-test :js {})
  => map?)

^{:refer hara.lang.script/script- :added "4.0"}
(fact "macro for test setup"

  (script/script- :lua)
  => map?)

^{:refer hara.lang.script/script-test-mode? :added "4.0"}
(fact "detects test mode from :test-mode and eval-mode"

  (script/script-test-mode? {})
  => false

  (script/script-test-mode? {:test-mode true})
  => false

  (binding [code.test.base.context/*eval-mode* false]
    (script/script-test-mode? {:test-mode true}))
  => true

  (binding [code.test.base.context/*eval-mode* false]
    (script/script-test-mode? {}))
  => false)

^{:refer hara.lang.script/script-ext :added "4.0"}
(fact "the `script+` function call"

  (script/script-ext [:LUA.1 :lua] {:runtime :oneshot})
  => vector?)

^{:refer hara.lang.script/script+ :added "4.0"}
(fact "macro for test extension setup"

  (script/script+ [:LUA.2 :lua] {:runtime :oneshot})
  => vector?)

^{:refer hara.lang.script/script-ext-run :added "4.0"}
(fact "function to call with the `!` macro"
  (script/script-ext-run (env/ns-sym) :LUA.0 '(return 1) {})
  => 1)

^{:refer hara.lang.script/! :added "4.0"}
(fact "switch between defined annex envs"

  (l/! [:LUA.0] (xtd/arr-map [1 2 3 4]
                             (fn:> [x] (+ x 1))))
  => [2 3 4 5]

  (l/! [:NOT-FOUND] (xtd/arr-map [1 2 3 4]
                                 (fn:> [x] (+ x 1))))
  => (throws))

^{:refer hara.lang.script/annex:start :added "4.0"}
(fact "starts an annex tag"

  (script/annex:start :LUA.0)
  => vector?)

^{:refer hara.lang.script/annex:get :added "4.0"}
(fact "gets the runtime associated with an annex"

  (script/annex:get :LUA.0)
  => map?

  (-> (script/annex:get :LUA.0)
      :library)
  => some?)

^{:refer hara.lang.script/annex:stop :added "4.0"
  :setup [(script/annex:start :LUA.0)]}
(fact "stops an annex tag"

  (script/annex:stop :LUA.0)
  => map?)

^{:refer hara.lang.script/annex:start-all :added "4.0"}
(fact "starts all the annex tags"

  (script/annex:start-all)
  => map?)

^{:refer hara.lang.script/annex:stop-all :added "4.0"}
(fact "stops all annexs"

  (script/annex:stop-all)
  => map?)

^{:refer hara.lang.script/annex:restart-all :added "4.0"}
(fact "stops and starts all annex runtimes"

  (script/annex:restart-all)
  => map?)

^{:refer hara.lang.script/annex:list :added "4.0"
  :setup [(script/annex:stop-all)]}
(fact "lists all annexs"

  (script/annex:list)
  => {:registered #{:LUA.0 :LUA.1 :LUA.2}, :active #{}}

  (do (script/annex:start-all)
      (script/annex:list))
  => {:registered #{:LUA.0 :LUA.1 :LUA.2}
      :active #{:LUA.0 :LUA.1 :LUA.2}})

(comment
  (./import)
  )