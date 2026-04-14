(ns xt.sys.cache-common-test
  (:require [rt.nginx.config :as config]
            [xt.lang.common-notify :as notify]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.sys.cache-common :as cache]]})

(l/script- :lua
  {:runtime :basic
   :config  {:exec ["resty" "--http-conf" (config/create-resty-params) "-e"]}
   :require [[xt.lang.common-spec :as xt]
             [xt.sys.cache-common :as cache]]})

(fact:global
 {:setup    [(l/rt:restart)
             (notify/wait-on [:js 5000]
               (:= (!:G window)  (require "window"))
               (:= (!:G LocalStorage)  (. (require "node-localstorage")
                                          LocalStorage))
               (:= window.localStorage (new LocalStorage "./test-scratch/localstorage"))
               (repl/notify true))]
  :teardown [(l/rt:stop)]})

^{:refer xt.sys.cache-common/cache :added "4.0"}
(fact "gets a cache"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush     cache)
   (cache/list-keys cache))
  => []
  
  (!.lua
   (cache/flush     (cache/cache :GLOBAL))
   (cache/list-keys (cache/cache :GLOBAL)))
  => {})

^{:refer xt.sys.cache-common/list-keys :added "4.0"}
(fact "lists keys in the cache"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/set cache "A" 1)
   (cache/set cache "B" 2)
   (cache/list-keys cache))
  => ["A" "B"]

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/set cache "A" 1)
   (cache/set cache "B" 2)
   (cache/list-keys cache))
  => ["A" "B"])

^{:refer xt.sys.cache-common/flush :added "4.0"}
(fact "clears all keys in the cache"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (xt/for:array [k ["A" "B" "C" "D" "E"]]
     (cache/set cache k k))
   (cache/flush cache)
   (cache/get-all cache))
  => {}

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (xt/for:array [k ["A" "B" "C" "D" "E"]]
     (cache/set cache k k))
   (cache/flush cache)
   (cache/get-all cache))
  => {})

^{:refer xt.sys.cache-common/get :added "4.0"}
(fact "gets a cache entry")

^{:refer xt.sys.cache-common/set :added "4.0"}
(fact "sets a cache entry")

^{:refer xt.sys.cache-common/del :added "4.0"}
(fact "removes a cache entry")

^{:refer xt.sys.cache-common/incr :added "4.0"}
(fact "increments the cache key"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/set cache "A" 1)
   (cache/incr cache "A" 10))
  => 11

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/set cache "A" 1)
   (cache/incr cache "A" 10))
  => 11)

^{:refer xt.sys.cache-common/get-all :added "4.0"}
(fact "gets the cache map"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (xt/for:array [k ["A" "B" "C" "D" "E"]]
     (cache/set cache k k))
   (cache/get-all cache))
  => {"E" "E", "C" "C", "B" "B", "A" "A", "D" "D"}

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (xt/for:array [k ["A" "B" "C" "D" "E"]]
     (cache/set cache k k))
   (cache/get-all cache))
  => {"E" "E", "C" "C", "B" "B", "A" "A", "D" "D"})

^{:refer xt.sys.cache-common/meta-key :added "4.0"}
(fact "constructs a meta key"
  ^:hidden
  
  (!.js
   (cache/meta-key "hello"))
  => "__meta__:hello"

  (!.lua
   (cache/meta-key "hello"))
  => "__meta__:hello")

^{:refer xt.sys.cache-common/meta-get :added "4.0"}
(fact  "gets the meta map"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   [(cache/meta-get "task")
    (cache/meta-update "task"
                       (fn:> [m]
                             (xt/x:set-key m "A" 1)
                             (return m)))
    (cache/meta-assoc "task" "B" 2)
    (cache/meta-dissoc "task" "A")])
  => [{}
      {"A" 1}
      {"B" 2, "A" 1}
      {"B" 2}]

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   [(cache/meta-get "task")
    (cache/meta-update "task"
                       (fn:> [m]
                             (xt/x:set-key m "A" 1)
                             (return m)))
    (cache/meta-assoc "task" "B" 2)
    (cache/meta-dissoc "task" "A")])
  => [{}
      {"A" 1}
      {"B" 2, "A" 1}
      {"B" 2}])

^{:refer xt.sys.cache-common/meta-update :added "4.0"}
(fact "updates the meta map")

^{:refer xt.sys.cache-common/meta-assoc :added "4.0"}
(fact "adds a key to the meta")

^{:refer xt.sys.cache-common/meta-dissoc :added "4.0"}
(fact "dissocs a key from the meta")
