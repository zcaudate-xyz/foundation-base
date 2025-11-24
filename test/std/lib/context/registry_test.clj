(ns std.lib.context.registry-test
  (:use code.test)
  (:require [std.lib.context.registry :refer :all]))

^{:refer std.lib.context.registry/rt-null? :added "3.0"}
(fact "checks that object is of type NullRuntime"
  (rt-null? +rt-null+) => true)

^{:refer std.lib.context.registry/registry-list :added "3.0"}
(fact "lists all contexts"

  (registry-list)
  => (contains [:null] :in-any-order :gaps-ok))

^{:refer std.lib.context.registry/registry-install :added "3.0"}
(fact "installs a new context type"

  (registry-install :play.test)
  => vector?)

^{:refer std.lib.context.registry/registry-uninstall :added "3.0"}
(fact "uninstalls a new context type"

  (registry-uninstall :play.test)
  => map?)

^{:refer std.lib.context.registry/registry-get :added "3.0"}
(fact "gets the context type"

  (registry-get :null)
  => (contains-in
      {:context :null,
       :rt {:default {:key :default,
                      :resource :hara/context.rt.null,
                      :config {}}}}))

^{:refer std.lib.context.registry/registry-rt-list :added "3.0"}
(fact "return all runtime types"

  (registry-rt-list)
  => map?

  (registry-rt-list :null)
  => (contains [:default]))

^{:refer std.lib.context.registry/registry-rt-add :added "3.0"}
(fact "installs a context runtime type"
  (registry-rt-add :null {:key :test})
  => vector?)

^{:refer std.lib.context.registry/registry-rt-remove :added "3.0"}
(fact "uninstalls a context runtime type"
  (registry-rt-remove :null :test)
  => map?)

^{:refer std.lib.context.registry/registry-rt :added "3.0"}
(fact "gets the runtime type information"
  (registry-rt :null)
  => map?)

^{:refer std.lib.context.registry/registry-scratch :added "4.0"}
(fact "gets the scratch runtime for a registered context"
  (registry-scratch :null)
  => rt-null?)
