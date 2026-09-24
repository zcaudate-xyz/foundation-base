(ns lang-demos.ngx-000-hello.main
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :lua.nginx
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as k]
             [lua.nginx :as n]]})

(defn.lua main
  []
  (n/say "\n---THREAD-TEST-")
  (n/thread-spawn
   (fn []
     (xt/for:array [i (k/arr-range 10)]
       (n/sleep (/ (math.random) 5))
       (n/say "Thread 1: " i))))
  (n/thread-spawn
   (fn []
     (xt/for:array [i (k/arr-range 10)]
       (n/sleep (/ (math.random) 5))
       (n/say "Thread 2: " i)))))

(defrun.lua __init__
  (-/main))
