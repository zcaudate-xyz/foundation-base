(ns hara.runtime.haxe.impl-test
  (:require [hara.runtime.haxe.impl :as impl]
            [std.lib.component :as component]
            [std.lib.env :as env])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "haxe"))})

^{:refer hara.runtime.haxe.impl/haxe-exec :added "4.1"}
(fact "resolves the haxe executable"
  (impl/haxe-exec)
  => string?)

^{:refer hara.runtime.haxe.impl/raw-eval-haxe :added "4.1"}
(fact "evaluates haxe code"
  (let [rt (impl/haxe {})]
    (try
      [(impl/raw-eval-haxe rt "var OUT = 1 + 2 + 3;")
       (impl/raw-eval-haxe rt "var OUT = 'hello' + ' ' + 'world';")]
      (finally
        (component/stop rt))))
  => [6 "hello world"])

^{:refer hara.runtime.haxe.impl/start-haxe :added "4.1"}
(fact "starts a haxe runtime"
  (let [rt (-> (impl/haxe:create {})
               (impl/start-haxe))]
    [(boolean rt)
     (= :haxe (:tag rt))
     (number? (.get ^java.util.concurrent.atomic.AtomicInteger (:msgid rt)))])
  => [true true true])

^{:refer hara.runtime.haxe.impl/stop-haxe :added "4.1"}
(fact "stops a haxe runtime without error"
  (let [rt (impl/haxe:create {})]
    (= rt (impl/stop-haxe rt)))
  => true)

^{:refer hara.runtime.haxe.impl/haxe:create :added "4.1"}
(fact "creates a haxe runtime record"
  (let [rt (impl/haxe:create {})]
    [(boolean rt)
     (= :haxe (:tag rt))])
  => [true true])

^{:refer hara.runtime.haxe.impl/haxe :added "4.1"}
(fact "creates and starts a haxe runtime"
  (let [rt (impl/haxe {})]
    (try
      (boolean rt)
      (finally
        (component/stop rt))))
  => true)
