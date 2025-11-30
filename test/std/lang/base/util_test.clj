(ns std.lang.base.util-test
  (:use code.test)
  (:require [std.lang.base.util :refer :all]
            [std.string :as str]
            [std.lib :as h]))

^{:refer std.lang.base.util/sym-id :added "3.0"}
(fact "gets the symbol id"
  ^:hidden

  (sym-id 'L.core/identity)
  => 'identity)

^{:refer std.lang.base.util/sym-module :added "3.0"}
(fact "gets the symbol namespace"
  ^:hidden

  (sym-module 'L.core/identity)
  => 'L.core)

^{:refer std.lang.base.util/sym-pair :added "3.0"}
(fact "gets the symbol pair"

  (sym-pair 'L.core/identity)
  => '[L.core identity])

^{:refer std.lang.base.util/sym-full :added "3.0"}
(fact "creates a full symbol"
  ^:hidden

  (sym-full 'L.core 'identity)
  => 'L.core/identity)

^{:refer std.lang.base.util/sym-default-str :added "4.0"}
(fact "default fast symbol conversion"
  ^:hidden
  
  (sym-default-str :helloWorld)
  => "helloWorld"

  (sym-default-str :hello-World)
  => "hello_World")

^{:refer std.lang.base.util/sym-default-inverse-str :added "4.0"}
(fact "inverses the symbol string"
  ^:hidden
  
  (sym-default-inverse-str "hello_world")
  => "hello-world")

^{:refer std.lang.base.util/hashvec? :added "4.0"}
(fact "checks for hash vec"
  ^:hidden

  (hashvec? #{[1 2 3]})
  => true)

^{:refer std.lang.base.util/doublevec? :added "4.0"}
(fact "checks for double vec"
  ^:hidden

  (doublevec? [[1 2 3]])
  => true)

^{:refer std.lang.base.util/lang-context :added "4.0"}
(fact "creates the lang context"
  ^:hidden

  (lang-context :lua)
  => :lang/lua)

^{:refer std.lang.base.util/lang-rt-list :added "4.0"}
(fact "lists rt in a namespace"
  ^:hidden
  
  (lang-rt-list)
  => coll?)

^{:refer std.lang.base.util/lang-rt :added "4.0"}
(fact "getn the runtime contexts in a map"
  ^:hidden
  
  (lang-rt)
  => map?)

^{:refer std.lang.base.util/lang-rt-default :added "4.0"}
(fact "gets the default runtime function"
  (lang-rt-default (lang-pointer :lua {:module 'L.core}))
  => any?)

^{:refer std.lang.base.util/lang-pointer :added "4.0"}
(fact "creates a lang pointer"
  ^:hidden
  
  (into {} (lang-pointer :lua {:module 'L.core}))
  => {:context :lang/lua, :module 'L.core, :lang :lua,
      :context/fn #'std.lang.base.util/lang-rt-default})
