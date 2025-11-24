(ns rt.basic.type-common-test
  (:use code.test)
  (:require [rt.basic.type-common :as common :refer :all]
            [rt.basic.impl.process-lua :as lua]))

^{:refer rt.basic.type-common/get-context-options :added "4.0"}
(fact "gets all or a section of the `*context-options*` structure"
  ^:hidden
  
  (common/get-context-options)
  => map?

  (common/get-context-options :lua)
  => map?)

^{:refer rt.basic.type-common/clear-context-options :added "4.0"}
(fact "clear entries from the `*context-options*` structure"
  (with-redefs [common/*context-options* (atom {:a {:b {:c 1}}})]
    (clear-context-options :a :b :c)
    @common/*context-options* => {:a {:b {}}}))

^{:refer rt.basic.type-common/put-context-options :added "4.0"
  :setup [(common/clear-context-options :lua :test.raw)]}
(fact "puts entries into context options"
  ^:hidden
  
  (common/put-context-options
   [:lua :test.raw]  {:default  {}})
  => '[nil {:default {}}])

^{:refer rt.basic.type-common/set-context-options :added "4.0"
  :setup [(common/clear-context-options :lua :test.raw)]}
(fact "sets a entry into context options"
  ^:hidden
  
  (common/set-context-options
   [:lua :test.raw :default] {})
  => '([[:lua :test.raw :default] nil {}]))

^{:refer rt.basic.type-common/program-exists? :added "4.0"}
(fact  "checks if an executable exists"
  
  (program-exists? "ls")
  => true)

^{:refer rt.basic.type-common/get-program-options :added "4.0"}
(fact "gets all program options"
  ^:hidden
  
  (common/get-program-options)
  => map?

  (common/get-program-options :lua)
  => anything)

^{:refer rt.basic.type-common/put-program-options :added "4.0"}
(fact "puts configuration into program options"
  (with-redefs [common/*program-options* (atom {:lua {}})]
    (put-program-options :lua {:a 1})
    @common/*program-options* => {:lua {:a 1}}))

^{:refer rt.basic.type-common/swap-program-options :added "4.0"}
(fact "swaps out the program options using a funciotn"
  (with-redefs [common/*program-options* (atom {:lua {:a 1}})]
    (swap-program-options :lua (fn [m k v] (assoc m k v)) :b 2)
    (:lua @common/*program-options*) => {:a 1 :b 2}))

^{:refer rt.basic.type-common/get-program-default :added "4.0"}
(fact "gets the default program"
  ^:hidden
  
  (get-program-default :lua :oneshot nil)
  => :luajit)

^{:refer rt.basic.type-common/get-program-flags :added "4.0"}
(fact "gets program flags"
  ^:hidden
  
  (get-program-flags :lua :luajit)
  => map?)

^{:refer rt.basic.type-common/get-program-exec :added "4.0"}
(fact "gets running parameters for program"
  ^:hidden
  
  (get-program-exec :lua :oneshot :luajit)
  => ["luajit" "-e"])

^{:refer rt.basic.type-common/get-options :added "4.0"}
(fact "gets merged options for context"
  ^:hidden
  
  (get-options :lua :oneshot :luajit)
  => map?)
