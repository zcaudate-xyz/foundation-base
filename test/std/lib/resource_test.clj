(ns std.lib.resource-test
  (:use code.test)
  (:require [std.lib.resource :refer :all]))

^{:refer std.lib.resource/res:spec-list :added "3.0"}
(fact "lists all available specs"

  (res:spec-list)
  => any?)

^{:refer std.lib.resource/res:spec-add :added "3.0"}
(fact "adds a new resource spec"
  (resolve 'res:spec-add) => var?)

^{:refer std.lib.resource/res:spec-remove :added "3.0"}
(fact "removes the resource spec"
  (resolve 'res:spec-remove) => var?)

^{:refer std.lib.resource/res:spec-get :added "3.0"}
(fact "retrieves a resource spec"

  (res:spec-get :hara/concurrent.atom.executor)
  => map?)

^{:refer std.lib.resource/res:variant-list :added "3.0"}
(fact "retrieves a list of variants to the spec"

  (res:variant-list :hara/concurrent.atom.executor)
  => coll?)

^{:refer std.lib.resource/res:variant-add :added "3.0"}
(fact "adds a spec variant")

^{:refer std.lib.resource/res:variant-remove :added "3.0"}
(fact "removes a spec variant")

^{:refer std.lib.resource/res:variant-get :added "3.0"}
(fact "gets a new variant")

^{:refer std.lib.resource/res:mode :added "3.0"}
(fact "gets the default mode of a resource"

  (res:mode :hara/concurrent.atom.executor)
  => :global

  (res:mode :hara/concurrent.atom.executor :std.concurrent.print)
  => :global)

^{:refer std.lib.resource/res:active :added "3.0"}
(fact "gets all active resources"

  (res:active))

^{:refer std.lib.resource/res-setup :added "3.0"}
(fact "creates and starts a resource")

^{:refer std.lib.resource/res-teardown :added "3.0"}
(fact "shutsdown a resource")

^{:refer std.lib.resource/res-key :added "3.0"}
(fact "gets a resource key"

  (res-key :shared :hara/concurrent.atom.executor :default {:id :hello})
  => :hello)

^{:refer std.lib.resource/res-path :added "3.0"}
(fact "gets the resource path"

  (res-path :shared :hara/concurrent.atom.executor :default {:id :hello})
  => '(:shared [:hara/concurrent.atom.executor :default] :hello))

^{:refer std.lib.resource/res-access-get :added "3.0"}
(fact "access function for active resource")

^{:refer std.lib.resource/res-access-set :added "3.0"}
(fact "access set function for active resource")

^{:refer std.lib.resource/res-start :added "3.0"}
(fact "start methods for resource")

^{:refer std.lib.resource/res-stop :added "3.0"}
(fact "stop method for resource")

^{:refer std.lib.resource/res-restart :added "3.0"}
(fact "starts and stops current resource")

^{:refer std.lib.resource/res-base :added "3.0"}
(fact "gets or sets a reosurce")

^{:refer std.lib.resource/res-call-fn :added "3.0"}
(fact "helper function for res-apifn")

^{:refer std.lib.resource/res-api-fn :added "3.0"}
(fact "helper function to create user friendly calls")
