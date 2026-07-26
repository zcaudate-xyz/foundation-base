(ns tahto.core.script-test
  (:require [lua.core]
             [tahto.core :as l]
             [tahto.base.book :as book]
tahto/lang/script_test.clj:5:             [tahto.common.emit-prep-lua-test :as prep-lua]
             [tahto.core.impl :as impl]
             [tahto.core.library :as lib]
             [tahto.core.library-snapshot :as snap]
             [tahto.core.runtime :as rt]
             [tahto.core.script :as script]
tahto/lang/script_test.clj:11:             [tahto.model.spec-js :as js]
tahto/lang/script_test.clj:12:             [tahto.model.spec-lua :as lua]
tahto/lang/script_test.clj:13:             [tahto.model.spec-xtalk :as xtalk]
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

^{:refer tahto.core.script/install :added "4.0"}
(fact "installs a language"

  (impl/with:library [+library+]
tahto/lang/script_test.clj:64:    (binding [*ns* (the-ns 'tahto.model.spec-lua)]
      (script/install lua/+book+)))
  => vector?)

^{:refer tahto.core.script/script-ns-import :added "4.0"}
(fact "imports the namespace and sets a primary flag"

  (impl/with:library [+library+]
    (script/script-ns-import {:require '[[xt.lang.common-data :as xtd :primary true]]}))
  => '#{xt.lang.common-data})

^{:refer tahto.core.script/script-macro-import :added "4.0"}
(fact "import macros into the namespace"

  (impl/with:library [+library+]
    (script/script-macro-import (l/get-book (l/runtime-library)
                                            :lua)))
  => vector?)

^{:refer tahto.core.script/script-require-target-id :added "4.1"}
(fact "constructs a target module id from module, source, and alias"

  (script/script-require-target-id 'my.module 'source.core nil)
  => 'my.module.source.core

  (script/script-require-target-id 'my.module 'source.core 'src)
  => 'my.module.src

  (script/script-require-target-id 'my.module 'source.core '[prefix src])
  => 'my.module.src)

^{:refer tahto.core.script/script-specialize-merge-contracts :added "4.1"}
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

^{:refer tahto.core.script/script-specialize-require :added "4.1"}
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

^{:refer tahto.core.script/script-specialize-config :added "4.1"}
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

^{:refer tahto.core.script/script-fn-base :added "4.0"}
(fact "setup for the runtime"

  (impl/with:library [+library+]
    (binding [book/*skip-check* true]
      (keys (script/script-fn-base :lua 'tahto.core.script-test
                                   {:require '[[xt.lang.common-data :as xtd]]}
                                   (l/runtime-library)))))
  => (contains [:module :module/internal :module/primary]))

^{:refer tahto.core.script/script-fn :added "4.0"}
(fact "calls the regular setup script for the namespace"

  (script/script-fn :lua)
  => map?)

^{:refer tahto.core.script/script :added "4.0"}
(fact "script macro"

  (script/script :lua)
  => map?)

^{:refer tahto.core.script/script-test-prep :added "4.0"}
(fact "preps the current namespace"

  (script/script-test-prep :js {})
  => (contains {:module 'tahto.core.script-test}))

^{:refer tahto.core.script/resolve-runtime-config :added "4.1"}
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

^{:refer tahto.core.script/script-test :added "4.0"}
(fact "the `script-` function call"

  (script/script-test :js {})
  => map?)

^{:refer tahto.core.script/script- :added "4.0"}
(fact "macro for test setup"

  (script/script- :lua)
  => map?)

^{:refer tahto.core.script/script-test-mode? :added "4.0"}
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

^{:refer tahto.core.script/script-ext :added "4.0"}
(fact "the `script+` function call"

  (script/script-ext [:LUA.1 :lua] {:runtime :oneshot})
  => vector?)

^{:refer tahto.core.script/script+ :added "4.0"}
(fact "macro for test extension setup"

  (script/script+ [:LUA.2 :lua] {:runtime :oneshot})
  => vector?)

^{:refer tahto.core.script/script-ext-run :added "4.0"}
(fact "function to call with the `!` macro"
  (script/script-ext-run (env/ns-sym) :LUA.0 '(return 1) {})
  => 1)

^{:refer tahto.core.script/! :added "4.0"}
(fact "switch between defined annex envs"

  (l/! [:LUA.0] (xtd/arr-map [1 2 3 4]
                             (fn:> [x] (+ x 1))))
  => [2 3 4 5]

  (l/! [:NOT-FOUND] (xtd/arr-map [1 2 3 4]
                                 (fn:> [x] (+ x 1))))
  => (throws))

^{:refer tahto.core.script/annex:start :added "4.0"}
(fact "starts an annex tag"

  (script/annex:start :LUA.0)
  => vector?)

^{:refer tahto.core.script/annex:get :added "4.0"}
(fact "gets the runtime associated with an annex"

  (script/annex:get :LUA.0)
  => map?

  (-> (script/annex:get :LUA.0)
      :library)
  => some?)

^{:refer tahto.core.script/annex:stop :added "4.0"
  :setup [(script/annex:start :LUA.0)]}
(fact "stops an annex tag"

  (script/annex:stop :LUA.0)
  => map?)

^{:refer tahto.core.script/annex:start-all :added "4.0"}
(fact "starts all the annex tags"

  (script/annex:start-all)
  => map?)

^{:refer tahto.core.script/annex:stop-all :added "4.0"}
(fact "stops all annexs"

  (script/annex:stop-all)
  => map?)

^{:refer tahto.core.script/annex:restart-all :added "4.0"}
(fact "stops and starts all annex runtimes"

  (script/annex:restart-all)
  => map?)

^{:refer tahto.core.script/annex:list :added "4.0"
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