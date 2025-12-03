(ns rt.postgres.grammar.common-application-test
  (:use code.test)
  (:require [rt.postgres.grammar.common-application :refer :all]
            [std.lang :as l]))

^{:refer rt.postgres.grammar.common-application/app-modules :added "4.0"}
(fact "checks for modules related to a given application"
  ^:hidden

  (with-redefs [l/get-book (fn [& _] {:modules {:m1 {:static {:application ["app"]}} :m2 {}}})]
    (app-modules "app"))
  => '({:static {:application ["app"]}}))

^{:refer rt.postgres.grammar.common-application/app-create-raw :added "4.0"}
(fact "creates a schema from tables and links"
  ^:hidden

  (app-create-raw {} {})
  => map?)

^{:refer rt.postgres.grammar.common-application/app-create :added "4.0"}
(fact "makes the app graph schema"
  ^:hidden

  (with-redefs [app-modules (fn [_] [{:code {:entry {:op 'deftype :static/schema-seed []}}}])]
    (app-create "test.postgres")
    => map?))

^{:refer rt.postgres.grammar.common-application/app-clear :added "4.0"}
(fact "clears the entry for an app"
  ^:hidden

  (with-redefs [*applications* (atom {"test.postgres" {}})]
    (app-clear "test.postgres")
    => {}
    @*applications* => {}))

^{:refer rt.postgres.grammar.common-application/app-rebuild :added "4.0"}
(fact "rebuilds the app schema"
  ^:hidden

  (with-redefs [*applications* (atom {"test.postgres" {:tables {} :pointers {}}})
                app-create-raw (fn [& _] {:rebuilt true})]
    (app-rebuild "test.postgres")
    => {:rebuilt true}))

^{:refer rt.postgres.grammar.common-application/app-rebuild-tables :added "4.0"}
(fact "initiate rebuild of app schema"
  ^:hidden

  (with-redefs [*applications* (atom {})
                app-create (fn [& _] {:created true})]
    (app-rebuild-tables "test")
    => {:created true}))

^{:refer rt.postgres.grammar.common-application/app-list :added "4.0"}
(fact "rebuilds the app schema"
  ^:hidden

  (with-redefs [*applications* (atom {"test.postgres" {}})]
    (app-list))
  => '("test.postgres"))

^{:refer rt.postgres.grammar.common-application/app :added "4.0"}
(fact "gets an app"
  ^:hidden

  (with-redefs [*applications* (atom {"test.postgres" {:data 1}})]
    (app "test.postgres")
    => {:data 1}))

^{:refer rt.postgres.grammar.common-application/app-schema :added "4.0"}
(fact "gets the app schema"
  ^:hidden

  (with-redefs [*applications* (atom {"test.postgres" {:schema :schema}})]
    (app-schema "test.postgres")
    => :schema))
