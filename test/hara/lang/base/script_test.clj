(ns hara.lang.base.script-test
  (:require [lua.core]
             [hara.lang :as l]
             [hara.lang.base.book :as book]
             [hara.lang.base.emit-prep-lua-test :as prep-lua]
             [hara.lang.base.impl :as impl]
             [hara.lang.base.library :as lib]
             [hara.lang.base.library-snapshot :as snap]
             [hara.lang.base.runtime :as rt]
             [hara.lang.base.script :as script]
             [hara.lang.model.spec-js :as js]
             [hara.lang.model.spec-lua :as lua]
             [hara.lang.model.spec-xtalk :as xtalk]
             [std.lib.env :as env])
  (:use code.test))

(def +library+
  (impl/clone-default-library))

(rt/install-lang! :lua)

(l/script+ [:LUA.0 :lua]
  {:runtime :oneshot
   :require [[xt.lang.common-data :as xtd]]})

^{:refer hara.lang.base.script/install :added "4.0"}
(fact "installs a language"

  (impl/with:library [+library+]
    (binding [*ns* (the-ns 'hara.lang.model.spec-lua)]
      (script/install lua/+book+)))
  => vector?)

^{:refer hara.lang.base.script/script-ns-import :added "4.0"}
(fact "imports the namespace and sets a primary flag"

  (impl/with:library [+library+]
    (script/script-ns-import {:require '[[xt.lang.common-data :as xtd :primary true]]}))
  => '#{xt.lang.common-data})

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

^{:refer hara.lang.base.script/script-macro-import :added "4.0"}
(fact "import macros into the namespace"

  (impl/with:library [+library+]
    (script/script-macro-import (l/get-book (l/runtime-library)
                                            :lua)))
  => vector?)

(fact "allows books without any exported macros"

  (script/script-macro-import {:macros []
                               :highlights []})
  => '[#{} #{}])


^{:refer hara.lang.base.script/script-fn-base :added "4.0"}
(fact "setup for the runtime"

  (impl/with:library [+library+]
    (binding [book/*skip-check* true]
      (keys (script/script-fn-base :lua 'hara.lang.base.script-test
                                   {:require '[[xt.lang.common-data :as xtd]]}
                                   (l/runtime-library)))))
  => (contains [:module :module/internal :module/primary]))

^{:refer hara.lang.base.script/script-fn :added "4.0"}
(fact "calls the regular setup script for the namespace"

  (script/script-fn :lua)
  => map?)

^{:refer hara.lang.base.script/script :added "4.0"}
(fact "script macro"

  (script/script :lua)
  => map?)

^{:refer hara.lang.base.script/script-test-prep :added "4.0"}
(fact "preps the current namespace"

  (script/script-test-prep :js {})
  => (contains {:module 'hara.lang.base.script-test}))

^{:refer hara.lang.base.script/script-test :added "4.0"}
(fact "the `script-` function call"

  (script/script-test :js {})
  => map?)

^{:refer hara.lang.base.script/script- :added "4.0"}
(fact "macro for test setup"

  (script/script- :lua)
  => map?)

^{:refer hara.lang.base.script/script-ext :added "4.0"}
(fact "the `script+` function call"

  (script/script-ext [:LUA.1 :lua] {:runtime :oneshot})
  => vector?)

^{:refer hara.lang.base.script/script+ :added "4.0"}
(fact "macro for test extension setup"

  (script/script+ [:LUA.2 :lua] {:runtime :oneshot})
  => vector?)

^{:refer hara.lang.base.script/script-ext-run :added "4.0"}
(fact "function to call with the `!` macro"
  (script/script-ext-run (env/ns-sym) :LUA.0 '(return 1) {})
  => 1)

^{:refer hara.lang.base.script/! :added "4.0"}
(fact "switch between defined annex envs"

  (l/! [:LUA.0] (xtd/arr-map [1 2 3 4]
                             (fn:> [x] (+ x 1))))
  => [2 3 4 5]

  (l/! [:NOT-FOUND] (xtd/arr-map [1 2 3 4]
                                 (fn:> [x] (+ x 1))))
  => (throws))

^{:refer hara.lang.base.script/annex:start :added "4.0"}
(fact "starts an annex tag"

  (script/annex:start :LUA.0)
  => vector?)

^{:refer hara.lang.base.script/annex:get :added "4.0"}
(fact "gets the runtime associated with an annex"

  (script/annex:get :LUA.0)
  => map?

  (-> (script/annex:get :LUA.0)
      :library)
  => some?)

^{:refer hara.lang.base.script/annex:stop :added "4.0"
  :setup [(script/annex:start :LUA.0)]}
(fact "stops an annex tag"

  (script/annex:stop :LUA.0)
  => map?)

^{:refer hara.lang.base.script/annex:start-all :added "4.0"}
(fact "starts all the annex tags"

  (script/annex:start-all)
  => map?)

^{:refer hara.lang.base.script/annex:stop-all :added "4.0"}
(fact "stops all annexs"

  (script/annex:stop-all)
  => map?)

^{:refer hara.lang.base.script/annex:restart-all :added "4.0"}
(fact "stops and starts all annex runtimes"

  (script/annex:restart-all)
  => map?)

^{:refer hara.lang.base.script/annex:list :added "4.0"
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
