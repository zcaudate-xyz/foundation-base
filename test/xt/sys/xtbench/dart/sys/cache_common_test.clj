(ns
 xtbench.dart.sys.cache-common-test
 (:require
  [rt.nginx.script :as script]
  [xt.lang.common-notify :as notify]
  [std.lang :as l])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.lang.spec-base :as xt]
   [xt.lang.common-lib :as k]
   [xt.lang.common-repl :as repl]
   [xt.sys.cache-common :as cache]]})

(defn
 create-resty-params
 "creates default resty params"
 {:added "4.0"}
 ([]
  (script/write
   [[:client-body-buffer-size "1m"]
    [:variables-hash-max-size 2048]
    [:variables-hash-bucket-size 128]
    [:lua-shared-dict [:GLOBAL "20k"]]
    [:lua-shared-dict [:WS_DEBUG "20k"]]
    [:lua-shared-dict [:ES_DEBUG "20k"]]])))

(fact:global
 {:setup
  [(l/rt:restart)
   (notify/wait-on
    [:dart 5000]
    (:= (!:G window) {})
    (:=
     (!:G LocalStorage)
     (. (require "node-localstorage") LocalStorage))
    (:=
     window.localStorage
     (new LocalStorage "./test-scratch/localstorage"))
    (repl/notify true))],
  :teardown [(l/rt:stop)]})

^{:refer xt.sys.cache-common/cache, :added "4.0"}
(fact
 "gets a cache"
 ^{:hidden true}
 (!.dt
  (var cache (cache/cache :GLOBAL))
  (cache/flush cache)
  (cache/list-keys cache))
 =>
 [])

^{:refer xt.sys.cache-common/list-keys, :added "4.0"}
(fact
 "lists keys in the cache"
 ^{:hidden true}
 (!.dt
  (var cache (cache/cache :GLOBAL))
  (cache/set cache "A" 1)
  (cache/set cache "B" 2)
  (cache/list-keys cache))
 =>
 ["A" "B"])

^{:refer xt.sys.cache-common/flush, :added "4.0"}
(fact
 "clears all keys in the cache"
 ^{:hidden true}
 (!.dt
  (var cache (cache/cache :GLOBAL))
  (xt/for:array [k ["A" "B" "C" "D" "E"]] (cache/set cache k k))
  (cache/flush cache)
  (cache/get-all cache))
 =>
 {})

^{:refer xt.sys.cache-common/incr, :added "4.0"}
(fact
 "increments the cache key"
 ^{:hidden true}
 (!.dt
  (var cache (cache/cache :GLOBAL))
  (cache/set cache "A" 1)
  (cache/incr cache "A" 10))
 =>
 11)

^{:refer xt.sys.cache-common/get-all, :added "4.0"}
(fact
 "gets the cache map"
 ^{:hidden true}
 (!.dt
  (var cache (cache/cache :GLOBAL))
  (cache/flush cache)
  (xt/for:array [k ["A" "B" "C" "D" "E"]] (cache/set cache k k))
  (cache/get-all cache))
 =>
 {"E" "E", "C" "C", "B" "B", "A" "A", "D" "D"})

^{:refer xt.sys.cache-common/meta-key, :added "4.0"}
(fact
 "constructs a meta key"
 ^{:hidden true}
 (!.dt (cache/meta-key "hello"))
 =>
 "__meta__:hello")

^{:refer xt.sys.cache-common/meta-get, :added "4.0"}
(fact
 "gets the meta map"
 ^{:hidden true}
 (!.dt
  (var cache (cache/cache :GLOBAL))
  (cache/flush cache)
  [(cache/meta-get "task")
   (cache/meta-update
    "task"
    (fn:> [m] (xt/x:set-key m "A" 1) (return m)))
   (cache/meta-assoc "task" "B" 2)
   (cache/meta-dissoc "task" "A")])
 =>
 [{} {"A" 1} {"B" 2, "A" 1} {"B" 2}])
