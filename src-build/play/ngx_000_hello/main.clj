(ns play.ngx-000-hello.main
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :lua
  {:require [[xt.lang.common-lib :as k]
             [lua.nginx :as n]]})

(defn.lua main
  []
  (n/say "\n---THREAD-TEST-")
  (n/thread-spawn
   (fn []
     (k/for:array [i (k/arr-range 10)]
       (n/sleep (/ (math.random) 5))
       (n/say "Thread 1: " i))))
  (n/thread-spawn
   (fn []
     (for:array [i (k/arr-range 10)]
       (n/sleep (/ (math.random) 5))
       (n/say "Thread 2: " i)))))

(defrun.lua __init__
  (-/main))
