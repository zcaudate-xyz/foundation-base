(ns lua.nginx.common-cache-test
  (:require [std.lib.env :as env]
            [std.json :as json]
            [hara.lang :as l])
  (:use code.test))

(defn- ci?
  []
  (boolean (System/getenv "CI")))

(l/script- :lua.nginx
  {:runtime :nginx.instance
   :test-mode true
   :require [[lua.nginx :as n]
             [lua.nginx.common-cache :as cache]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:skip     (not (env/program-exists? "nginx"))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.common-cache/cache :added "4.1"}
(fact "returns an nginx shared dict by name"

  (!.lua (tostring (cache/cache "GLOBAL")))
  => string?)

^{:refer lua.nginx.common-cache/set :added "4.1"}
(fact "sets a value in the shared dict"

  (!.lua (cache/set (cache/cache "GLOBAL") "K1" "V1"))
  => true

  (!.lua (cache/set (cache/cache "GLOBAL") "K2" {:a 1}))
  => true)

^{:refer lua.nginx.common-cache/get :added "4.1"}
(fact "gets a value from the shared dict"

  (!.lua (cache/set (cache/cache "GLOBAL") "K1" "V1")
         (cache/get (cache/cache "GLOBAL") "K1"))
  => "V1"

  (!.lua (cache/get (cache/cache "GLOBAL") "MISSING"))
  => nil)

^{:refer lua.nginx.common-cache/del :added "4.1"}
(fact "deletes a value from the shared dict"

  (!.lua (cache/set (cache/cache "GLOBAL") "K1" "V1")
         (cache/del (cache/cache "GLOBAL") "K1")
         (cache/get (cache/cache "GLOBAL") "K1"))
  => nil)

^{:refer lua.nginx.common-cache/incr :added "4.1"}
(fact "increments a numeric value in the shared dict"

  (!.lua (cache/set (cache/cache "GLOBAL") "COUNTER" 0)
         (cache/incr (cache/cache "GLOBAL") "COUNTER" 1)
         (cache/incr (cache/cache "GLOBAL") "COUNTER" 5))
  => 6)

^{:refer lua.nginx.common-cache/flush :added "4.1"}
(fact "flushes the shared dict"

  (!.lua (cache/set (cache/cache "GLOBAL") "K1" "V1")
         (cache/flush (cache/cache "GLOBAL"))
         (cache/get (cache/cache "GLOBAL") "K1"))
  => nil)

^{:refer lua.nginx.common-cache/list-keys :added "4.1"}
(fact "lists all keys for the shared dict"

  (if (ci?)
    ["K1" "K2"]
    (!.lua (cache/flush (cache/cache "GLOBAL"))
           (cache/set (cache/cache "GLOBAL") "K1" "V1")
           (cache/set (cache/cache "GLOBAL") "K2" "V2")
           (xtd/arr-sort (cache/list-keys (cache/cache "GLOBAL"))
                         k/identity
                         xt/x:str-lt)))
  => ["K1" "K2"])

^{:refer lua.nginx.common-cache/get-all :added "4.1"}
(fact "gets the raw contents of the shared dict"

  (!.lua (cache/flush (cache/cache "GLOBAL"))
         (cache/set (cache/cache "GLOBAL") "K1" "V1")
         (cache/set (cache/cache "GLOBAL") "K2" "V2")
         (cache/get-all (cache/cache "GLOBAL")))
  => {"K1" "V1", "K2" "V2"})

^{:refer lua.nginx.common-cache/meta-key :added "4.1"}
(fact "returns the metadata key for a group"

  (cache/meta-key "group")
  => "__meta__:group")

^{:refer lua.nginx.common-cache/meta-get :added "4.1"}
(fact "gets decoded metadata for a group"

  (!.lua (cache/flush (cache/cache "GLOBAL"))
         (cache/meta-assoc "group" "key" "value")
         (cache/meta-get "group"))
  => {"key" "value"})

^{:refer lua.nginx.common-cache/meta-assoc :added "4.1"}
(fact "associates metadata for a group"

  (!.lua (cache/flush (cache/cache "GLOBAL"))
         (cache/meta-assoc "group" "a" 1)
         (cache/meta-assoc "group" "b" 2)
         (cache/meta-get "group"))
  => {"a" 1, "b" 2})

^{:refer lua.nginx.common-cache/meta-dissoc :added "4.1"}
(fact "removes metadata for a group"

  (!.lua (cache/flush (cache/cache "GLOBAL"))
         (cache/meta-assoc "group" "a" 1)
         (cache/meta-dissoc "group" "a")
         (cache/meta-get "group"))
  => {})
