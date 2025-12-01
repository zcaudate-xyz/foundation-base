(ns js.lib.valtio-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:refer-clojure :exclude [use val proxy]))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.valtio :as v]
             [js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})


^{:refer js.lib.valtio/make :added "4.0" :unchecked true}
(fact "makes a proxy with reset"
  ^:hidden
  
  (!.js
   (v/make (fn:> {:a 1 :b 2})))
  => {"a" 1, "b" 2})

^{:refer js.lib.valtio/reset :added "4.0" :unchecked true}
(fact "resets proxy to original"
  ^:hidden

  (!.js
   (v/reset (j/assign (v/make (fn:> {:a 1 :b 2}))
                      {:a 3 :b 4})))
  => {"a" 1, "b" 2})

^{:refer js.lib.valtio/useVal :added "4.0" :unchecked true}
(fact "uses only the getter")

^{:refer js.lib.valtio/val :added "4.0" :unchecked true}
(fact "macro for `useVal`")

^{:refer js.lib.valtio/listen :added "4.0" :unchecked true}
(fact "listens for store values")

^{:refer js.lib.valtio/getAccessors :added "4.0" :unchecked true}
(fact "creates accessors on the proxy")

^{:refer js.lib.valtio/getFieldAccessors :added "4.0" :unchecked true}
(fact "creates field accessors on the proxy")

^{:refer js.lib.valtio/useProxy :added "4.0" :unchecked true}
(fact "uses the proxy object directly or via id lookup")

^{:refer js.lib.valtio/useProxyField :added "4.0" :unchecked true}
(fact "uses the proxy object field directly or via id lookup")

^{:refer js.lib.valtio/wrapProxyField :added "4.0" :unchecked true}
(fact "wraps a component with `record` and `field`")

^{:refer js.lib.valtio/use :added "4.0" :unchecked true}
(fact "uses the proxy")

^{:refer js.lib.valtio/useData :added "4.0" :unchecked true}
(fact "data function helper")
