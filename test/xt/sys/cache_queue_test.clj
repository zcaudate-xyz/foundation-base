(ns xt.sys.cache-queue-test
  (:require [rt.nginx.config :as config]
            [xt.lang.common-notify :as notify]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.sys.cache-queue :as queue]
             [xt.sys.cache-common :as cache]]})

(defn create-resty-params
  "creates default resty params"
  {:added "4.0"}
  ([& [{:keys [blocks]}]]
   (rt.nginx.script/write [[:client-body-buffer-size "1m"]
                           [:variables-hash-max-size 2048]
                           [:variables-hash-bucket-size 128]
                           [:lua-shared-dict [:GLOBAL    "20k"]]
                           #_#_
                           [:lua-shared-dict [:WS_DEBUG  "20k"]]
                           [:lua-shared-dict [:ES_DEBUG  "20k"]]])))

(l/script- :lua
  {:runtime :basic
   :config  {:exec ["resty" "--http-conf" "client_body_buffer_size 1m;\nvariables_hash_max_size 2048;\nvariables_hash_bucket_size 128;\nlua_shared_dict GLOBAL 20k;\nlua_shared_dict WS_DEBUG 20k;\nlua_shared_dict ES_DEBUG 20k;" "-e"]
               #_#_
               :container {:group "test"
                           :image "python"
                         :runtime :basic
                         :exec ["python" "-c"]
                         #_#_:bootstrap (fn [port opts]
                                          "1+1")}}
   :require [[xt.sys.cache-queue :as queue]
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

^{:refer xt.sys.cache-queue/INDEX-KEY :added "4.0"}
(fact "gets the index key"
  ^:hidden

  (!.js
   (queue/INDEX-KEY "main"))
  => "main:__index__"

  (!.lua
   (queue/INDEX-KEY "main"))
  => "main:__index__")

^{:refer xt.sys.cache-queue/GROUPCOUNT-KEY :added "4.0"}
(fact "gets the groupcount key"
  ^:hidden
  
  (!.js (queue/GROUPCOUNT-KEY "main"))
  => "main:__groupcount__"

  (!.lua (queue/GROUPCOUNT-KEY "main"))
  => "main:__groupcount__")

^{:refer xt.sys.cache-queue/GROUP-KEY :added "4.0"}
(fact "gets the group key"
  ^:hidden
  
  (!.js (queue/GROUP-KEY "main" "g0"))
  => "main:__group__:g0"

  (!.lua (queue/GROUP-KEY "main" "g0"))
  => "main:__group__:g0")

^{:refer xt.sys.cache-queue/BUFFER-KEY :added "4.0"}
(fact "gets the buffer key"
  ^:hidden
  
  (!.js (queue/BUFFER-KEY "main" 0))
  => "main:__buffer__:0"

  (!.lua (queue/BUFFER-KEY "main" 0))
  => "main:__buffer__:0")

^{:refer xt.sys.cache-queue/queue-meta :added "4.0"}
(fact "gets the meta "
  ^:hidden

  (!.js  (var cache (cache/cache :GLOBAL))
         (cache/flush cache)
         (queue/create-queue cache
                             "main" 5)
         (queue/group-setup cache
                            "main"
                            "g0")
         (queue/group-setup cache
                      "main"
                      "g1")
         
         [(queue/queue-meta cache)
          (queue/queue-groupcount cache "main")])
  => [{"main" {"size" 5}} 2]
  
  (!.lua (cache/cache :GLOBAL))
  => (comp not nil?)
  
  (!.lua (var cache (cache/cache :GLOBAL))
         (cache/flush cache)
         (queue/create-queue cache
                             "main" 5)
         (queue/group-setup cache
                            "main"
                            "g0")
         (queue/group-setup cache
                            "main"
                            "g1")
         
         [(queue/queue-meta cache)
          (queue/queue-groupcount cache "main")])
  => [{"main" {"size" 5}} 2])

^{:refer xt.sys.cache-queue/queue-meta :added "4.0"}
(fact "gets the queue meta")

^{:refer xt.sys.cache-queue/buffer-item :added "4.0"}
(fact "gets the buffer item")

^{:refer xt.sys.cache-queue/buffer-get-index :added "4.0"}
(fact "gets the index in the buffer"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   [(queue/buffer-get-index cache "main")
    (queue/push-item cache "main" "hello" 5)
    (queue/push-item cache "main" "hello" 5)
    (queue/push-item cache "main" "hello" 5)
    (queue/buffer-get-index cache "main")])
  => [4 0 1 2 2]
  
  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   [(queue/buffer-get-index cache "main")
    (queue/push-item cache "main" "hello" 5)
    (queue/push-item cache "main" "hello" 5)
    (queue/push-item cache "main" "hello" 5)
    (queue/buffer-get-index cache "main")])
  => [4 0 1 2 2])

^{:refer xt.sys.cache-queue/buffer-set-index :added "4.0"}
(fact "sets the index for a buffer")

^{:refer xt.sys.cache-queue/queue-groupcount :added "4.0"}
(fact "gets the queue group count")

^{:refer xt.sys.cache-queue/queue-meta-assoc :added "4.0"}
(fact "sets a key in the queue meta")

^{:refer xt.sys.cache-queue/queue-meta-dissoc :added "4.0"}
(fact "removes a key in the queue meta")

^{:refer xt.sys.cache-queue/create-queue :added "4.0"}
(fact "creates a queue "
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/create-queue cache "world" 10)
   (queue/queue-meta cache))
  => {"world" {"size" 10}, "main" {"size" 5}}

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/create-queue cache "world" 10)
   (queue/queue-meta cache))
  => {"world" {"size" 10}, "main" {"size" 5}})

^{:refer xt.sys.cache-queue/list-queues :added "4.0"}
(fact "lists all queues"
  ^:hidden
  
  (set (!.js
        (var cache (cache/cache :GLOBAL))
        (cache/flush cache)
        (queue/create-queue cache "main" 5)
        (queue/create-queue cache "world" 10)
        (queue/list-queues cache)))
  => #{"main" "world"}

  (set (!.lua
        (var cache (cache/cache :GLOBAL))
        (cache/flush cache)
        (queue/create-queue cache "main" 5)
        (queue/create-queue cache "world" 10)
        (queue/list-queues cache)))
  => #{"main" "world"})

^{:refer xt.sys.cache-queue/list-queue-groups :added "4.0"}
(fact "lists all queue groups"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   (queue/group-setup cache "main" "g1")
   (queue/group-setup cache "main" "g2")
   (queue/list-queue-groups cache "main"))
  => ["main:__group__:g0"
      "main:__group__:g1"
      "main:__group__:g2"]

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   (queue/group-setup cache "main" "g1")
   (queue/group-setup cache "main" "g2")
   (queue/list-queue-groups cache "main"))
  => ["main:__group__:g0"
      "main:__group__:g1"
      "main:__group__:g2"])

^{:refer xt.sys.cache-queue/purge-queue :added "4.0"}
(fact "removes all groups and items in a queue"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   (queue/push-item cache "main" "hello" 5)
   (queue/push-item cache "main" "hello" 5)
   (queue/push-item cache "main" "hello" 5)
   (queue/purge-queue cache "main")
   (queue/queue-meta cache))
  => {}

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   (queue/push-item cache "main" "hello" 5)
   (queue/push-item cache "main" "hello" 5)
   (queue/push-item cache "main" "hello" 5)
   (queue/purge-queue cache "main")
   (queue/queue-meta cache))
  => {})

^{:refer xt.sys.cache-queue/reset-queue :added "4.0"}
(fact "resets a group's index"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   (queue/push-item cache "main" "hello" 5)
   (queue/push-item cache "main" "hello" 5)
   (queue/push-item cache "main" "hello" 5)
   (queue/group-read cache "main" "g0" 5)
   [(queue/group-get-index cache "main" "g0")
    (queue/reset-queue cache "main")
    (queue/queue-meta cache)
    (queue/group-get-index cache "main" "g0")])
  => [2 nil {"main" {"size" 5}} 4]

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   (queue/push-item cache "main" "hello" 5)
   (queue/push-item cache "main" "hello" 5)
   (queue/push-item cache "main" "hello" 5)
   (queue/group-read cache "main" "g0" 5)
   [(queue/group-get-index cache "main" "g0")
    (queue/reset-queue cache "main")
    (queue/queue-meta cache)
    (queue/group-get-index cache "main" "g0")])
  => [2 nil {"main" {"size" 5}} 4])

^{:refer xt.sys.cache-queue/has-queue :added "4.0"}
(fact "checks that queue exists"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   [(queue/has-queue cache "main")
    (queue/has-queue cache "WRONG")])
  => [true false]

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   [(queue/has-queue cache "main")
    (queue/has-queue cache "WRONG")])
  => [true false])

^{:refer xt.sys.cache-queue/has-group :added "4.0"}
(fact "checks that group exists"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   [(queue/has-group cache "main" "g0")
    (queue/has-group cache "main" "WRONG")])
  => [true false]

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   [(queue/has-group cache "main" "g0")
    (queue/has-group cache "main" "WRONG")])
  => [true false])

^{:refer xt.sys.cache-queue/push-item :added "4.0"}
(fact "pushes an item into the queue"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 3)
   [(queue/push-item cache "main" "A" 3)
    (queue/push-item cache "main" "B" 3)
    (queue/push-item cache "main" "C" 3)
    (queue/push-item cache "main" "D" 3)
    (cache/get-all cache)])
  => [0 1 2 0 {"main:__buffer__:0" "D",
               "main:__groupcount__" "0",
               "main:__buffer__:1" "B",
               "main:__index__" "0",
               "main:__buffer__:2" "C",
               "__meta__:queue" "{\"main\":{\"size\":3}}"}]

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 3)
   [(queue/push-item cache "main" "A" 3)
    (queue/push-item cache "main" "B" 3)
    (queue/push-item cache "main" "C" 3)
    (queue/push-item cache "main" "D" 3)
    (cache/get-all cache)])
  => [0 1 2 0 {"main:__buffer__:0" "D",
               "main:__groupcount__" 0
               "main:__buffer__:1" "B",
               "main:__index__" "0",
               "main:__buffer__:2" "C",
               "__meta__:queue" "{\"main\":{\"size\":3}}"}])

^{:refer xt.sys.cache-queue/push-items :added "4.0"}
(fact "pushes items into the queue"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 3)
   [(queue/push-items cache "main" ["A" "B" "C" "D"] 3)
    (cache/get-all cache)])
  => [0 {"main:__buffer__:0" "D",
         "main:__groupcount__" "0"
         "main:__buffer__:1" "B",
         "main:__index__" "0",
         "main:__buffer__:2" "C",
         "__meta__:queue" "{\"main\":{\"size\":3}}"}]

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 3)
   [(queue/push-items cache "main" ["A" "B" "C" "D"] 3)
    (cache/get-all cache)])
  => [0 {"main:__buffer__:0" "D",
         "main:__groupcount__" 0
         "main:__buffer__:1" "B",
         "main:__index__" "0",
         "main:__buffer__:2" "C",
         "__meta__:queue" "{\"main\":{\"size\":3}}"}])

^{:refer xt.sys.cache-queue/group-set-index :added "4.0"}
(fact "sets an index for a group")

^{:refer xt.sys.cache-queue/group-get-index :added "4.0"}
(fact "gets an index for a group")

^{:refer xt.sys.cache-queue/group-setup :added "4.0"}
(fact "setup a group"
  ^:hidden

  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   (queue/push-items cache "main" ["A" "B" "C" "D"] 5)
   (queue/group-read cache "main" "g0" 5))
  => [4 ["A" "B" "C" "D"] 3]

  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/create-queue cache "main" 5)
   (queue/group-setup cache "main" "g0")
   (queue/push-items cache "main" ["A" "B" "C" "D"] 5)
   (queue/group-read cache "main" "g0" 5))
  => [4 ["A" "B" "C" "D"] 3])

^{:refer xt.sys.cache-queue/group-read :added "4.0"}
(fact "reads all items in the group")

^{:refer xt.sys.cache-queue/group-teardown :added "4.0"}
(fact "tears down the group")

^{:refer xt.sys.cache-queue/queue-default :added "4.0"}
(fact "pushes a queue, if not there creates a queue and default group"
  ^:hidden
  
  (!.js
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/queue-default cache "main" "a" 10)
   (queue/group-read cache "main" "default" 5))
  => [1 ["a"] 0]
  
  (!.lua
   (var cache (cache/cache :GLOBAL))
   (cache/flush cache)
   (queue/queue-default cache "main" "a" 10)
   (queue/group-read cache "main" "default" 5))
  => [1 ["a"] 0])

(comment

  )
