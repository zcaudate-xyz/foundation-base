(ns kmi.redis
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str])
  (:refer-clojure :exclude [eval sort sync keys get set type time]))

(l/script :lua
  kmi.redis
  {:require [[xt.lang.base-lib :as k]]
   :static {:lang/lint-globals #{redis}}})

(defmacro.lua flushdb
  "clears the redis db"
  {:added "4.0"}
  ([]
   (list 'redis.call "FLUSHDB")))

(comment
  => ("LOG_DEBUG" "LOG_NOTICE" "LOG_VERBOSE" "LOG_WARNING"
      "REPL_ALL" "REPL_AOF" "REPL_NONE" "REPL_REPLICA" "REPL_SLAVE"
      "breakpoint" "call" "debug" "error_reply" "log" "pcall" "replicate_commands"
      "set_repl" "setresp" "sha1hex" "status_reply"))

(def$.lua call redis.call)

(def$.lua pcall redis.pcall)

(def$.lua sha1hex redis.sha1hex)

(defmacro.lua log
  "outputs to std out"
  {:added "4.0"}
  [msg & [level]]
  (list 'redis.log msg (or level 2)))

(defn.lua ^{:rt/redis {:nargs -1}}
  call-fn
  "calles with redis"
  {:added "4.0"}
  ([...]
   (return (redis.call ...))))

(defn.lua as-num
  "converts to a number"
  {:added "3.0"}
  ([x]
   (if (not x)
     (return 0)
     (return (tonumber x)))))

;;
;; ZSET
;;

(defn.lua ^{:rt/redis {:nkeys 1}}
  zscoremin
  "finds the minimum score"
  {:added "3.0"}
  ([key]
   (return (. (-/call "ZRANGE" key 0 0 "WITHSCORES") [2]))))

(defn.lua ^{:rt/redis {:nkeys 1}}
  zscoremax
  "retrieves the maximum score"
  {:added "3.0"}
  ([key]
   (return (. (-/call "ZREVRANGE" key 0 0 "WITHSCORES") [2]))))

(defn.lua ^{:rt/redis {:nkeys 1}}
  zscorerange
  "retrieves the maximum score"
  {:added "3.0"}
  ([key]
   (local min (-/zscoremin key))
   (if (== nil min)
     (return nil)
     (return [(tonumber min) (tonumber (-/zscoremax key))]))))

(defn- z-tmpl
  ([{:keys [name check param] :as m}]
   (h/$ (defn.lua ~(with-meta name {:rt/redis {:nkeys 1}})
          {:added "3.0"}
          ([...]
           (local arg (tab ...))
           (local key (. arg [1]))
           (x:arr-pop-first arg)
           (local '[elems reply] '[(-/call "ZRANGE" key 0 -1) []])
           (if (and (< 0 (len arg))
                    (< 0 (len elems)))
             (k/for:array [[i e] elems]
               (local ~param true)
               (k/for:array [[j k] arg]
                 (local score (-/call "ZSCORE" k e))
                 (if ~check
                   (do (:= ~param false)
                       (break))))
               (if ~param
                 (table.insert reply e))))
           (return reply))))))

(h/template-entries [z-tmpl]
  [{:name zintersect :check (not score) :param exists}
   {:name zdiff :check score  :param not-exists}])

;;
;; CAS
;;

(defn- cas-tmpl
  "compares and swap for keys and hashs"
  {:added "3.0"}
  [{:keys [name args prefix]}]
  (h/$ (defn.lua ~(with-meta name {:rt/redis {:nkeys 1}})
         (~(vec (concat args '[old new]))
          (local val (-/call ~(str prefix "GET") ~@args))
          (if (or (== val old)
                  (and (== val false)
                       (or (== old ""))))
            (if (== new "")
              (do (-/call ~(str prefix "DEL") ~@args)
                  (return ["OK"]))
              (do (local ret (-/call ~(str prefix "SET") ~@args new))
                  (return ["OK"])))
            (return ["NEW" val]))))))

(h/template-entries [cas-tmpl]
  [{:name cas-set  :args [key] :prefix ""}
   {:name cas-hset :args [key field] :prefix "H"}])

(defn.lua ^{:rt/redis {:nkeys 2}}
  key-copy
  "copies a key"
  {:added "3.0"}
  ([src dst]
   (if (== 1 (-/call "EXISTS" dst))
     (-/call "DEL" dst))
   (-/call "RESTORE" dst 0 (-/call "DUMP" src))
   (return "OK")))

(defn.lua ^{:rt/redis {:nkeys 1}}
  ttl-time
  "gets the absolute time for expiry"
  {:added "3.0"}
  ([key]
   (local t (tonumber (-/call "TTL" key)))
   (if (> t 0)
     (:= t (+ t (tonumber (. (-/call "TIME") [1])))))
   (return t)))

(def.lua key-getters
  (tab :string [["GET"] []]
       :list   [["LRANGE"] [0 -1]]
       :hash   [["HGETALL"] []]
       :set    [["MEMBERS"] []]
       :zset   [["ZRANGE"] [0 -1 "WITHSCORES"]]
       :stream [["XRANGE"] ["-" "+"]]))

(defn.lua ^{:rt/redis {:nkeys 1}}
  key-export
  "exports a key"
  {:added "3.0"}
  ([key]
   (local t (. (-/call "TYPE" key) ["ok"]))
   (return [t  (-/call (unpack (. -/key-getters [t] [1]))
                       key
                       (unpack (. -/key-getters [t] [2])))])))

(defn.lua dump-db
  "dumps all keys in db"
  {:added "4.0"}
  []
  (local ks (-/call "KEYS" "*"))
  (return (k/arr-juxt ks k/identity -/key-export)))


(defn.lua time-ms
  "gets time in ms"
  {:added "4.0"}
  ([]
   (local t (-/call "TIME"))
   (return (math.floor
            (+ (* (. t [1]) 1000)
               (* (. t [2]) 0.001))))))

(defn.lua time-us
  "gets time in us"
  {:added "4.0"}
  ([]
   (local t (-/call "TIME"))
   (return (+ (* (. t [1]) 1000000)
              (. t [2])))))

;;
;;
;; TIME
;;

(defn.lua bench
  "benchmarkes a function in `us`"
  {:added "3.0"}
  ([f ...]
   (local t-0 (-/call  "TIME"))
   (local res (f ...))
   (local t-1 (-/call "TIME"))
   (local t (- (+ (* 1000000 (. t-1 [1])) (. t-1 [2]))
               (+ (* 1000000 (. t-0 [1])) (. t-0 [2]))))
   (return [t res])))


(defn.lua bench-offset
  "provides measurement of additional `us` time for bench"
  {:added "4.0"}
  ([]
   (local t-0 (-/call "TIME"))
   (local t-1 (-/call "TIME"))
   (return (- (. t-1 [2])
              (. t-0 [2])))))

(defn.lua bench-wrap
  "wraps a function so that it is benched"
  {:added "4.0"}
  ([f]
   (return (fn [...]
             (return (-/bench f ...))))))

;;
;; SCAN
;;

(defn.lua do-regex
  "helper function for key actions"
  {:added "4.0"}
  ([re match f]
   (local '[cur tmp out] '[0 nil nil])
   (if (not match) (:= match "*"))
   (while true
     (:= tmp (-/call "SCAN" cur "MATCH" match))
     (:= '[cur out] '[(tonumber (. tmp [1])) (. tmp [2])])
     (if out
       (k/for:object [[k v] out]
         (if (. v (find re)) (f v))))
     (if (== 0 cur)
       (return true)))))

(defn.lua ^{:rt/redis {}}
  scan-regex
  "provides a regex extension to scan"
  {:added "3.0"}
  ([re match]
   (local rep [])
   (-/do-regex re match (fn [v] (table.insert rep v)))
   (return rep)))

(defn.lua ^{:rt/redis {:nkeys 1}}
  scan-level
  "scan keys only at a given level"
  {:added "3.0"}
  ([key]
   (return (-/scan-regex (cat key ":[^\\:]+$")
                         (cat key ":*")))))

(defn.lua ^{:rt/redis {:nkeys 1}}
  scan-sub
  "scan keys but return only subkey part"
  {:added "3.0"}
  ([key]
   (return (k/arr-map (-/scan-regex (cat key ":[^\\:]+$")
                                    (cat key ":*"))
                      (fn [k] (return (. k (sub (+ 2 (len key))))))))))

(defn.lua flat-pairs-to-object
  "flat pairs to object"
  {:added "4.0"}
  ([arr]
   (return (k/from-flat arr
                        k/step-set-key
                        {}))))

(defn.lua flat-pairs-to-array
  "flat pairs to array"
  {:added "4.0"}
  ([arr]
   (return (k/from-flat arr
                        (fn [arr k v]
                          (table.insert arr [k v])
                          (return arr))
                        []))))

(defn.lua call-batched
  "applies command to keys in batches"
  {:added "3.0"}
  ([command bargs opts]
   (local '[n prefix suffix] '[(or (. opts ["batch"]) 1000)
                               (or (. opts ["prefix"]) [])
                               (or (. opts ["suffix"]) [])])
   (local call-fn (fn [args]
                    (return (-/call command
                                    (unpack (k/arr-mapcat
                                             [prefix
                                              args
                                              suffix]
                                             k/identity))))))
   (local total (len bargs))
   (local '[tnum trem] '[(math.floor (/ total n))
                         (mod total n)])
   (local out [])
   (k/for:index [i [0 (- tnum 1)]]
     (local chunk [])
     (k/for:index [j [1 n]]
       (table.insert chunk (. bargs [(+ (* i n) j)])))
     (table.insert out (call-fn chunk)))
   
   (when (< 0 trem)
     (local chunk [])
     (k/for:index [j [1 trem]]
       (table.insert chunk (. bargs [(+ (* tnum n) j)])))
     (table.insert out (call-fn chunk)))

   (return out)))

