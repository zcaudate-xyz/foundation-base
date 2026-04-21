(ns rt.basic.type-common-test
  (:require [rt.basic.impl.process-lua :as lua]
            [rt.basic.type-common :as common :refer :all])
  (:use code.test))

^{:refer rt.basic.type-common/get-context-options :added "4.0"}
(fact "gets all or a section of the `*context-options*` structure"

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

  (common/put-context-options
   [:lua :test.raw]  {:default  {}})
  => '[nil {:default {}}])

^{:refer rt.basic.type-common/set-context-options :added "4.0"
  :setup [(common/clear-context-options :lua :test.raw)]}
(fact "sets a entry into context options"

  (common/set-context-options
   [:lua :test.raw :default] {})
  => '([[:lua :test.raw :default] nil {}]))

^{:refer rt.basic.type-common/program-exists? :added "4.0"}
(fact  "checks if an executable exists"

  (program-exists? "ls")
  => true)

^{:refer rt.basic.type-common/get-program-options :added "4.0"}
(fact "gets all program options"

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

  (get-program-default :lua :oneshot nil)
  => :luajit)

^{:refer rt.basic.type-common/get-program-flags :added "4.0"}
(fact "gets program flags"

  (get-program-flags :lua :luajit)
  => map?)

^{:refer rt.basic.type-common/get-program-exec :added "4.0"}
(fact "gets running parameters for program"

  (get-program-exec :lua :oneshot :luajit)
  => ["luajit" "-e"])

^{:refer rt.basic.type-common/get-options :added "4.0"}
(fact "gets merged options for context"

  (get-options :lua :oneshot :luajit)
  => map?)

^{:refer rt.basic.type-common/require-runtime! :added "4.0"}
(fact "raises a clear error when a runtime has not been installed"
  (try (get-options :missing.lang :twostep :default)
       (catch clojure.lang.ExceptionInfo e
         [(.getMessage e)
          (select-keys (ex-data e) [:lang :runtime :available])]))
  => ["Runtime not installed"
      {:lang :missing.lang
       :runtime :twostep
       :available []}])


^{:refer rt.basic.type-common/available-runtimes :added "4.1"}
(fact "lists installed runtimes for a language"
  (with-redefs [std.lang.base.registry/+registry+
                (atom {[:lua :oneshot] 'rt.basic.impl.process-lua
                       [:lua :basic] 'rt.basic.impl.process-lua
                       [:python :oneshot] 'rt.basic.impl.process-python})]
    (available-runtimes :lua))
  => [:basic :oneshot])

^{:refer rt.basic.type-common/valid-context! :added "4.1"}
(fact "asserts runtime context is valid"
  (valid-context! :oneshot)
  => nil?

  (try (valid-context! :not-a-context)
       (catch AssertionError e
         true))
  => true)