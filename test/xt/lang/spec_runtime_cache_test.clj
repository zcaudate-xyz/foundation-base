(ns xt.lang.spec-runtime-cache-test
  (:use code.test)
  (:require [clojure.set :as set]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-runtime-cache :as spec-cache]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-runtime-cache/x:cache :added "4.1"
  :setup [^{:seedgen/base   {:lua     {:suppress true}
                             :python  {:suppress true}}}
          (!.js
            (:= (!:G TEST_CACHE)
                {"_keys" ["a" "b"]}))]}
(fact "selects the global cache store"

  (!.js
    (xt/x:cache "GLOBAL"))
  => identity)

^{:refer xt.lang.spec-runtime-cache/x:cache-list :added "4.1"
  :setup [^{:seedgen/base   {:lua     {:suppress true}
                             :python  {:suppress true}}}
          (!.js
            (:= (!:G TEST_CACHE)
                {"_keys" ["a" "b"]}))]}
(fact "lists cache keys"

  (!.js
    (xt/x:cache-list (!:G TEST_CACHE)))
  => ["a" "b"])

^{:refer xt.lang.spec-runtime-cache/x:cache-flush :added "4.1"
  :setup [^{:seedgen/base   {:lua     {:suppress true}
                             :python  {:suppress true}}}
          (!.js
            (:= (!:G TEST_CACHE)
                {:clear (fn [])}))]}
(fact "flushes cache stores"
  
  (!.js
    (xt/x:cache-flush (!:G TEST_CACHE)))
  => anything)

^{:refer xt.lang.spec-runtime-cache/x:cache-get :added "4.1"}
(fact "reads cache values"

  (!.js
    (var cache {:getItem (fn [k]
                           (return (+ "value:" k)))})
    (xt/x:cache-get cache "key"))
  => "value:key")

^{:refer xt.lang.spec-runtime-cache/x:cache-set :added "4.1"}
(fact "writes cache values"

  (!.js
    (var out nil)
    (var cache {:setItem (fn [k v]
                           (:= out [k v])
                           (return v))})
    [(xt/x:cache-set cache "key" "value")
     out])
  => ["value" ["key" "value"]])

^{:refer xt.lang.spec-runtime-cache/x:cache-del :added "4.1"}
(fact "deletes cache values"

  (!.js
    (var out nil)
    (var cache {:removeItem (fn [k]
                              (:= out k))})
    (xt/x:cache-del cache "key")
    out)
  => "key")

^{:refer xt.lang.spec-runtime-cache/x:cache-incr :added "4.1"}
(fact "increments cached numeric values"

  (!.js
    (var state {"count" "2"})
    (var cache {:getItem (fn [k]
                           (return (. state [k])))
                :setItem (fn [k v]
                           (:= (. state [k]) v)
                           (return v))})
    (xt/x:cache-incr cache "count" 3))
  => 5)
