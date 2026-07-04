(ns xt.lang.common-module-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-module :refer :all]
            js.core.impl))

^{:refer xt.lang.common-module/current-module :added "4.1"}
(fact "gets a module from the library by id"

  (l/with:macro-opts [(l/rt:macro-opts :js)]
    (current-module 'xt.lang.common-module))
  => (contains {:id 'xt.lang.common-module})

  (l/with:macro-opts [(l/rt:macro-opts :js)]
    (:id (current-module 'js.core.impl)))
  => 'js.core.impl)

^{:refer xt.lang.common-module/linked-natives :added "4.1"}
(fact "collects native imports across module dependencies"

  (sort (keys (linked-natives :js 'js.core.impl)))
  => ())

^{:refer xt.lang.common-module/current-natives :added "4.1"}
(fact "gets the native imports of a single module"

  (sort (keys (current-natives :js 'js.core.impl)))
  => ())

^{:refer xt.lang.common-module/expose-module :added "4.1"}
(fact "returns a module key as stringified data"

  (l/with:macro-opts [(l/rt:macro-opts :js)]
    (expose-module :native 'xt.lang.common-module))
  => {}

  (l/with:macro-opts [(l/rt:macro-opts :js)]
    (expose-module :link 'xt.lang.common-module))
  => {"-" "xt.lang.common-module"}

  (l/with:macro-opts [(l/rt:macro-opts :js)]
    (expose-module :internal 'xt.lang.common-module))
  => {"xt.lang.common-module" "-"})

^{:refer xt.lang.common-module/module-native :added "4.1"}
(fact "returns the native map as a string"

  (module-native 'xt.lang.common-module)
  => "{}")

^{:refer xt.lang.common-module/module-link :added "4.1"}
(fact "returns the link map as a string"

  (module-link 'xt.lang.common-module)
  => "{\"-\":\"xt.lang.common-module\"}")

^{:refer xt.lang.common-module/module-internal :added "4.1"}
(fact "returns the internal map as a string"

  (module-internal 'xt.lang.common-module)
  => "{\"xt.lang.common-module\":\"-\"}")

^{:refer xt.lang.common-module/module-save :added "4.1"}
(fact "saves the module and returns its id"

  (module-save 'xt.lang.common-module)
  => "\"xt.lang.common-module\""

  (:id *saved-module*)
  => 'xt.lang.common-module

  (alter-var-root #'*saved-module* (fn [_] nil)))
