(ns kmi.redis.compile-test
  (:require [kmi.redis.compile :refer :all]
            [kmi.redis.sample-type :as type])
  (:use code.test)
  (:refer-clojure :exclude [compile]))

^{:refer kmi.redis.compile/parse-map :added "3.0"}
(fact "parses the :map type from a spec and key"
  ^:hidden

  (parse-map type/<SPEC> :__meta__)
  => {:type :hash}

  (parse-map type/<SPEC> :active)
  => {:type :key}

  (parse-map (get-in type/<SPEC> [:fields :active])
             "id")
  => {:type :zset})

^{:refer kmi.redis.compile/parse-coll :added "3.0"}
(fact "parses a collection (:list, :set, :stream) type")

^{:refer kmi.redis.compile/parse-zset :added "3.0"}
(fact "parses a :zset type")

^{:refer kmi.redis.compile/compile-path :added "3.0"}
(fact "creates a typed path"
  ^:hidden

  (compile-path type/<SPEC> [:active "<ID>"])
  => [{:type :key, :path :active} {:type :zset, :path "<ID>"}])

^{:refer kmi.redis.compile/to:str :added "3.0"}
(fact "conserve symbols, other types to string")

^{:refer kmi.redis.compile/to:array :added "3.0"}
(fact "creates a form for arrays"
  ^:hidden

  (to:array ['<RETURN>])
  => '[(unpack (xt.lang.common-data/to-flat <RETURN>))]

  (to:array [[:hello :world]])
  => '[(unpack ["hello" "world"])])

^{:refer kmi.redis.compile/build-path :added "3.0"}
(fact "build path command"
  ^:hidden

  (build-path [{:path 'hello}]
              ["test"])
  => '(cat "test" ":" hello))

^{:refer kmi.redis.compile/build-command :added "3.0"}
(fact "builds a redis command"
  ^:hidden

  (build-command {:prefix ["GET"] :suffix []}
                 [{:path 'hello}]
                 ["test"])
  => '(redis.call "GET" (cat "test" ":" hello)))

^{:refer kmi.redis.compile/run-command :added "3.0"}
(fact "command for run"
  ^:hidden

  (compile type/<SPEC>
           '[:RUN "ZRANGE" ["test"] [:active]])
  => '(redis.call "ZRANGE" (cat "test" ":" "active")))

^{:refer kmi.redis.compile/path-command :added "3.0"}
(fact "command for path"
  ^:hidden

  (compile type/<SPEC>
           '[:PATH ["test"] [:active id]])
  => '(cat "test" ":" "active" ":" id))

^{:refer kmi.redis.compile/get-all-command :added "3.0"}
(fact "creates a get all command"
  ^:hidden

  (get-all-command {:type :zset :path 'hello}
                   []
                   [])
  => '(redis.call "ZRANGEBYSCORE" hello "-inf" "+inf" "WITHSCORES")

  (get-all-command {:type :string :path 'hello}
                   []
                   [])
  => (throws))

^{:refer kmi.redis.compile/get-vals-command :added "3.0"}
(fact "command for set vals"
  ^:hidden

  (compile type/<SPEC>
           '[:GET ["test"] [:unfilled <id>] ["volume"]])
  => '(redis.call "HMGET" (cat "test" ":" "unfilled" ":" <id>) "volume")

  (compile type/<SPEC>
           '[:GET ["test"] [:active :ask] [<id>]])
  => '(redis.call "ZMSCORE" (cat "test" ":" "active" ":" "ask") <id>))

^{:refer kmi.redis.compile/get-entry-command :added "3.0"}
(fact "command for get entry"
  ^:hidden

  (compile type/<SPEC>
           '[:GET ["test"] [:active :ask <id>]])
  => '(redis.call "ZSCORE" (cat "test" ":" "active" ":" "ask") <id>)

  (compile type/<SPEC>
           '[:GET ["test"] [:unfilled <id> "volume"]])
  => '(redis.call "HGET" (cat "test" ":" "unfilled" ":" <id>) "volume"))

^{:refer kmi.redis.compile/get-all-key :added "3.0"}
(fact "export all sub keys"
  ^:hidden

  (compile type/<SPEC>
           '[:GET [] [:active]])
  => '(xt.lang.common-data/arr-map (kmi.redis/scan-level "active") kmi.redis/key-export))

^{:refer kmi.redis.compile/get-vals-key :added "3.0"}
(fact "export input keys"
  ^:hidden

  (compile type/<SPEC>
           '[:GET [] [:active] ["ask" "bid"]])
  => '(xt.lang.common-data/arr-map ["ask" "bid"] (fn [_k] (return (kmi.redis/key-export (cat "active" ":" _k))))))

^{:refer kmi.redis.compile/get-command :added "3.0"}
(fact "command for get")

^{:refer kmi.redis.compile/len-command :added "3.0"}
(fact "command for length"
  ^:hidden

  (compile type/<SPEC>
           '[:LEN ["test"] [:unfilled <id>]])
  => '(redis.call "HLEN" (cat "test" ":" "unfilled" ":" <id>))

  (compile type/<SPEC>
           '[:LEN ["test"] [:events :_:tx]])
  => '(redis.call "XLEN" (cat "test" ":" "events" ":" "_:tx")))

^{:refer kmi.redis.compile/keys-command :added "3.0"}
(fact "command for keys"
  ^:hidden

  (compile type/<SPEC>
           '[:KEYS ["test"] [:unfilled]])
  => '(kmi.redis/scan-sub (cat "test" ":" "unfilled"))

  (compile type/<SPEC>
           '[:KEYS ["test"] [:unfilled <id>]])
  => '(redis.call "HKEYS" (cat "test" ":" "unfilled" ":" <id>)))

^{:refer kmi.redis.compile/has-command :added "3.0"}
(fact "command for has"
  ^:hidden

  (compile type/<SPEC>
           '[:HAS ["test"] [:unfilled <id>]])
  => '(== 1 (redis.call "EXISTS" (cat "test" ":" "unfilled" ":" <id>)))

  (compile type/<SPEC>
           '[:HAS ["test"] [:_:order <id> :frame-completed]])
  => '(== 1 (redis.call "HEXISTS" (cat "test" ":" "_:order" ":" <id>) "frame_completed")))

^{:refer kmi.redis.compile/del-all-key :added "3.0"}
(fact "delete all for :key"
  ^:hidden

  (compile type/<SPEC>
           '[:DEL ["test"] [:unfilled]])
  => '(redis.call "DEL" (unpack (kmi.redis/scan-level (cat "test" ":" "unfilled")))))

^{:refer kmi.redis.compile/del-vals-key :added "3.0"}
(fact "delete for :key inputs"
  ^:hidden

  (compile type/<SPEC>
           '[:DEL ["test"] [:unfilled] ["a" "b"]])
  => '(redis.call "DEL"
                  (unpack (xt.lang.common-data/arr-map
                           ["a" "b"]
                           (fn [_k]
                             (return (kmi.redis/key-export (cat "test" ":" "unfilled" ":" _k))))))))

^{:refer kmi.redis.compile/del-vals-command :added "3.0"}
(fact "delete for data structures"
  ^:hidden

  (compile type/<SPEC>
           '[:DEL ["test"] [:completed] ["a" "b"]])
  => '(redis.call "ZREM" (cat "test" ":" "completed") "a" "b")

  (compile type/<SPEC>
           '[:DEL ["test"] [:unfilled :ask] ["a" "b"]])
  => '(redis.call "HDEL" (cat "test" ":" "unfilled" ":" "ask") "a" "b"))

^{:refer kmi.redis.compile/del-entry-command :added "3.0"}
(fact "deletes for single field values"
  ^:hidden

  (compile type/<SPEC>
           '[:DEL ["test"] [:unfilled :ask "a"]])
  => '(redis.call "HDEL" (cat "test" ":" "unfilled" ":" "ask") "a"))

^{:refer kmi.redis.compile/del-command :added "3.0"}
(fact "command for del")

^{:refer kmi.redis.compile/set-vals-command :added "3.0"}
(fact "command for set vals"
  ^:hidden

  (compile type/<SPEC>
           '[:SET ["test"] [:unfilled :ask] {10 100}])
  => '(redis.call "HMSET" (cat "test" ":" "unfilled" ":" "ask")
                  (unpack (xt.lang.common-data/to-flat {10 100}))))

^{:refer kmi.redis.compile/set-entry-command :added "3.0"}
(fact "command for set entry"
  ^:hidden

  (compile type/<SPEC>
           '[:SET ["test"] [:unfilled :ask 10] 100])
  => '(redis.call "HSET" (cat "test" ":" "unfilled" ":" "ask") "10" 100))

^{:refer kmi.redis.compile/set-command :added "3.0"}
(fact "command for set"
  ^:hidden

  (compile type/<SPEC>
           '[:SET ["test"] [:frame] 100])
  => '(redis.call "SET" (cat "test" ":" "frame") 100))

^{:refer kmi.redis.compile/incr-command :added "3.0"}
(fact "command for incr"
  ^:hidden

  (compile type/<SPEC>
           '[:INCR ["test"] [:frame]])
  => '(tonumber (redis.call "INCR" (cat "test" ":" "frame")))

  (compile type/<SPEC>
           '[:INCR ["test"] [:unfilled :ask 10] 100])
  => '(tonumber (redis.call "HINCRBY" (cat "test" ":" "unfilled" ":" "ask") "10" 100)))

^{:refer kmi.redis.compile/decr-command :added "3.0"}
(fact "command for decr"
  ^:hidden

  (compile type/<SPEC>
           '[:DECR ["test"] [:frame]])
  => '(tonumber (redis.call "DECR" (cat "test" ":" "frame")))

  (compile type/<SPEC>
           '[:DECR ["test"] [:unfilled :ask 10] 100])
  => '(tonumber (redis.call "HINCRBY" (cat "test" ":" "unfilled" ":" "ask") "10" (- 100))))

^{:refer kmi.redis.compile/add-command :added "4.0"}
(fact "command for add"

  (compile type/<SPEC>
           '[:ADD ["test"] [:events :_:error] {:id "A"}])
  => '(redis.call "XADD" (cat "test" ":" "events" ":" "_:error") "*" "id" "A"))

^{:refer kmi.redis.compile/compile-cmd :added "3.0"}
(fact "compiles the command")

^{:refer kmi.redis.compile/compile :added "3.0"}
(fact "compiles a body with spec")
