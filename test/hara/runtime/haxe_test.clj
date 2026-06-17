(ns hara.runtime.haxe-test
  (:require [hara.runtime.haxe.impl :as impl]
            [std.lib.component :as component]
            [std.lib.env :as env])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "haxe"))})

^{:refer hara.runtime.haxe.impl/haxe :added "4.1"}
(fact "creates and uses a haxe runtime"
  (let [rt (impl/haxe {})]
    [(boolean rt)
     (impl/raw-eval-haxe rt "var OUT = 1 + 2 + 3;")
     (do (component/stop rt)
         true)])
  => [true 6 true])

^{:refer hara.runtime.haxe.impl/raw-eval-haxe :added "4.1"}
(fact "evaluates string concatenation in haxe"
  (let [rt (impl/haxe {})]
    (try
      (impl/raw-eval-haxe rt "var OUT = 'hello' + ' ' + 'world';")
      (finally
        (component/stop rt))))
  => "hello world")
