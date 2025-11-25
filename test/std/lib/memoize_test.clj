(ns std.lib.memoize-test
  (:use code.test)
  (:require [std.lib.memoize :refer :all]
            [std.lib :as h])
  (:refer-clojure :exclude [memoize]))

^{:refer std.lib.memoize/map->Memoize :added "3.0"  :adopt true}
(fact "creates an object that holds its own cache"

  (declare -mem-)
  (def -mem-
    (->Memoize + nil (atom {}) #'-mem- *registry* (volatile! true)))
  (resolve 'map->Memoize) => var?)

^{:refer std.lib.memoize/memoize :added "3.0"}
(fact "caches the result of a function"
  (ns-unmap *ns* '+-inc-)
  (ns-unmap *ns* '-inc-)
  (def +-inc- (atom {}))
  (declare -inc-)
  (def -inc-  (memoize inc +-inc- #'-inc-))

  (-inc- 1) => 2
  (-inc- 2) => 3)

^{:refer std.lib.memoize/register-memoize :added "3.0"}
(fact "registers the memoize function"

  (register-memoize -inc-)
  => any?)

^{:refer std.lib.memoize/deregister-memoize :added "3.0"}
(fact "deregisters the memoize function"

  (deregister-memoize -inc-))

^{:refer std.lib.memoize/registered-memoizes :added "3.0"}
(fact "lists all registered memoizes"

  (registered-memoizes)
  => (any nil? coll?))

^{:refer std.lib.memoize/registered-memoize? :added "3.0"}
(fact "checks if a memoize function is registered"

  (registered-memoize? -mem-)
  => false)

^{:refer std.lib.memoize/memoize:status :added "3.0"}
(fact "returns the status of the object"

  (memoize:status -inc-)
  => :enabled)

^{:refer std.lib.memoize/memoize:info :added "3.0"}
(fact "formats the memoize object"

  (def +-plus- (atom {}))
  (declare -plus-)
  (def -plus- (memoize + +-plus- #'-plus-))
  (memoize:info -plus-)
  => (contains {:status :enabled, :registered false, :items number?})
  ;; {:fn +, :cache #atom {(1 1) 2}}
  )

^{:refer std.lib.memoize/memoize:disable :added "3.0"}
(fact "disables the usage of the cache"

  (memoize:disable -inc-)
  => :disabled)

^{:refer std.lib.memoize/memoize:disabled? :added "3.0"}
(fact "checks if the memoized function is disabled"

  (memoize:disabled? -inc-)
  => true)

^{:refer std.lib.memoize/memoize:enable :added "3.0"}
(fact "enables the usage of the cache"

  (memoize:enable -inc-)
  => :enabled)

^{:refer std.lib.memoize/memoize:enabled? :added "3.0"}
(fact "checks if the memoized function is disabled"

  (memoize:enabled? -inc-)
  => true)

^{:refer std.lib.memoize/memoize:invoke :added "3.0"}
(fact "invokes the function with arguments"

  (memoize:invoke -plus- 1 2 3)
  => 6)

^{:refer std.lib.memoize/memoize:remove :added "3.0"}
(fact "removes a cached result"

  (memoize:remove -inc- 1)
  => 2)

^{:refer std.lib.memoize/memoize:clear :added "3.0"}
(fact "clears all results"

  (memoize:clear -inc-)
  => '{(2) 3})

^{:refer std.lib.memoize/invoke-intern-memoize :added "3.0"}
(fact "creates a memoize form template for `definvoke`"

  (invoke-intern-memoize :memoize 'hello {} '([x] x)) ^:hidden
  => '(do (clojure.core/declare hello)
          (def +hello (clojure.core/atom {}))
          (clojure.core/defn hello-raw "helper function for std.lib.memoize-test/hello" [x] x)
          (def hello (std.lib.memoize/memoize hello-raw +hello (var hello)))
          (clojure.core/doto hello (std.lib.memoize/register-memoize)
                             (std.lib.memoize/memoize:clear)) (var hello)))

(comment
  (code.manage/incomplete)
  (./code:scaffold)
  (code.manage/arrange)
  (code.manage/import))

(comment

  (-inc- 4)
  (-inc- 2)
  (-inc- 1)
  +-inc-
  (memoize:deregister -inc-)
  (memoize:disable -inc-)
  (+registry)

  (def a (var oeuoe))
  (code.manage/import))
