(ns std.lang.base.script-annex-test
  (:use code.test)
  (:require [std.lang.base.script-annex :as annex]
            [std.lang.base.library :as lib]
            [std.lang.base.book :as book]
            [xt.lang]
            [std.lib :as h]))

^{:refer std.lang.base.script-annex/rt-annex? :added "4.0"}
(fact "checks that object is an annex"
  ^:hidden
  
  (annex/rt-annex? (annex/rt-annex:create {}))
  => true)

^{:refer std.lang.base.script-annex/rt-annex:create :added "4.0"}
(fact "creates an annex object"
  
  
  (annex/rt-annex:create {})
  => annex/rt-annex?)

^{:refer std.lang.base.script-annex/annex-current :added "4.0"}
(fact "gets the current annex. May not exist"

  (annex/annex-current)
  => any?)

^{:refer std.lang.base.script-annex/annex-reset :added "4.0"
  :setup [(annex/get-annex)]}
(fact "resets the current annex"
  ^:hidden
  
  (annex/annex-reset)
  => map?)

^{:refer std.lang.base.script-annex/get-annex :added "4.0"}
(fact "gets the current annex in the namespace"
  ^:hidden
  
  (annex/get-annex)
  => map?)

^{:refer std.lang.base.script-annex/clear-annex :added "4.0"}
(fact "clears all runtimes in the annex"
  ^:hidden
  
  (annex/clear-annex)
  => map?)

^{:refer std.lang.base.script-annex/get-annex-library :added "4.0"}
(fact "gets the current annex library"

  (annex/get-annex-library (h/ns-sym))
  => lib/library?)

^{:refer std.lang.base.script-annex/get-annex-book :added "4.0"}
(fact "gets the current book in the annex"

  (annex/get-annex-book (h/ns-sym) :lua)
  => book/book?)

^{:refer std.lang.base.script-annex/add-annex-runtime :added "4.0"
  :setup [(annex/clear-annex)]}
(fact "adds a runtime to the annex"
  ^:hidden
  
  (annex/add-annex-runtime (h/ns-sym)
                           :hello
                           :hello.rt)
  => [nil :hello.rt]

  (annex/get-annex-runtime (h/ns-sym)
                           :hello)
  => :hello.rt

  (annex/remove-annex-runtime (h/ns-sym)
                              :hello)
  => :hello.rt)

^{:refer std.lang.base.script-annex/get-annex-runtime :added "4.0"}
(fact "gets the annex rutime"
  (annex/get-annex-runtime (h/ns-sym) :hello) => nil)

^{:refer std.lang.base.script-annex/remove-annex-runtime :added "4.0"}
(fact "removes the annex runtime"
  (annex/remove-annex-runtime (h/ns-sym) :hello) => nil)

^{:refer std.lang.base.script-annex/register-annex-tag :added "4.0"
  :setup [(annex/deregister-annex-tag (h/ns-sym) :redis.0)]}
(fact "registers a config for the tag"
  ^:hidden
  
  (annex/register-annex-tag (h/ns-sym)
                            :redis.0
                            :lua
                            :redis
                            {})
  => [nil :lua]

  (annex/deregister-annex-tag (h/ns-sym) :redis.0)
  => {:lang :lua, :runtime :redis, :config {}})

^{:refer std.lang.base.script-annex/deregister-annex-tag :added "4.0"}
(fact "removes the config for the tag"
  (annex/deregister-annex-tag (h/ns-sym) :redis.0) => nil)

^{:refer std.lang.base.script-annex/start-runtime :added "4.0"}
(fact "starts the runtime in the annex"
  ^:hidden

  (annex/start-runtime :lua :default {}) => map?)

^{:refer std.lang.base.script-annex/same-runtime? :added "4.0"}
(fact "checks that one runtime is the same as another"
  ^:hidden
  
  (annex/same-runtime? (annex/start-runtime :lua :default {})
                       :lua
                       :default
                       {})
  => true)

(comment
  (./create-tests)
  (./import)
  (h/p:registry-list)

  

  (h/p:registry-get :lang.annex)

  (h/p:space-context-list)
  (h/p:space-context-get (h/p:space) :lang.annex)

  (h/p:space-context-get (h/p:space) :lang/c)

  (h/p:space-rt-start (h/p:space) :lang.annex)
  (h/p:space-rt-start (h/p:space) :lang/lua)


  )
