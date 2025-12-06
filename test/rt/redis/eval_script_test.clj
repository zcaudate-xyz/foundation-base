(ns rt.redis.eval-script-test
  (:use code.test)
  (:require [rt.redis.eval-script :refer :all]
            [rt.redis.client :as r]
            [lib.redis.bench :as bench]
            [kmi.redis :as redis]
            [std.concurrent :as cc]
            [std.lib :as h]
            [std.lang :as l]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.impl :as impl]
            [lib.redis.script :as script]))

(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

^{:refer rt.redis.eval-script/raw-compile-form :added "4.0"}
(fact "converts a ptr into a form"
  ^:hidden
  
  (raw-compile-form redis/scan-sub)
  => '(return (kmi.redis/scan-sub (. KEYS [1]))))

^{:refer rt.redis.eval-script/raw-compile :added "4.0"}
(fact "compiles a function as body and sha"
  ^:hidden
  
  (raw-compile redis/scan-sub)
  => {:body
      (std.string/join-lines
       ["local function arr_map(arr,f)"
        "  local out = {}"
        "  for _, e in  ipairs(arr) do"
        "    table.insert(out,f(e))"
        "  end"
        "  return out"
        "end"
        ""
        "local function do_regex(re,match,f)"
        "  local cur,tmp,out = 0,nil,nil"
        "  if not match then"
        "    match = '*'"
        "  end"
        "  while true do"
        "    tmp = redis.call('SCAN',cur,'MATCH',match)"
        "    cur,out = tonumber(tmp[1]),tmp[2]"
        "    if out then"
        "      for k, v in  pairs(out) do"
        "        if v:find(re) then"
        "          f(v)"
        "        end"
        "      end"
        "    end"
        "    if 0 == cur then"
        "      return true"
        "    end"
        "  end"
        "end"
        ""
        "local function scan_regex(re,match)"
        "  local rep = {}"
        "  do_regex(re,match,function (v)"
        "    table.insert(rep,v)"
        "  end)"
        "  return rep"
        "end"
        ""
        "local function scan_sub(key)"
        "  return arr_map(scan_regex(key .. ':[^\\\\:]+$',key .. ':*'),function (k)"
        "    return k:sub(2 + #key)"
        "  end)"
        "end"
        ""
        "return scan_sub(KEYS[1])"]),
      :sha "a6878daff456b17437fc0192cce60dd4a7449e11"})

^{:refer rt.redis.eval-script/raw-prep-in-fn :added "4.0"}
(fact "prepares the arguments for entry"

  (raw-prep-in-fn {:rt/redis {:nkeys 1}} [:key :arg])
  => [[:key] [:arg]])

^{:refer rt.redis.eval-script/raw-prep-out-fn :added "4.0"}
(fact "prepares arguments out"

  (raw-prep-out-fn {:rt/redis {:encode {:out true}}} "{\"a\":1}")
  => {:a 1})

^{:refer rt.redis.eval-script/rt-install-fn :added "4.0"}
(fact "retries the function if not installed"
  ^:hidden
  
  (with-redefs [script/script:load (fn [& _] :load)
                script/script:evalsha (fn [& _] :eval)]
    ((rt-install-fn {} "sha" "body" [] []) (Exception. "NOSCRIPT")))
  => :eval)

^{:refer rt.redis.eval-script/redis-invoke-sha :added "4.0"
  :setup    [(def -client- (r/client {:port 17001}))
             (cc/req -client- ["FLUSHDB"])
             (cc/req -client- ["SCRIPT" "FLUSH"])]
  :teardown [(h/stop -client-)]}
(fact "creates a sha call"
  ^:hidden
  
  (with-redefs [ptr/get-entry (fn [_] {:rt/redis {:nkeys 0}})
                raw-compile (fn [_] {:body "return 1" :sha "sha"})
                raw-prep-in-fn (fn [_ args] [[] args])
                script/script:evalsha (fn [& _] 1)]
    (redis-invoke-sha -client- 'ptr ["PING"]))
  => 1)
