(ns lib.aether.wagon-test
  (:use code.test)
  (:require [lib.aether.wagon :refer :all])
  (:import (org.apache.maven.wagon AbstractWagon Wagon)))

^{:refer lib.aether.wagon/add-factory :added "3.0"}
(fact "registers a wagon factory for creating transports"
  (add-factory :test String)
  => (contains {:test java.lang.String}))

^{:refer lib.aether.wagon/remove-factory :added "3.0"}
(fact "removes the registered wagon factory"
  (add-factory :test String)
  (remove-factory :test)
  => (fn [m] (not (:test m))))

^{:refer lib.aether.wagon/all-factories :added "3.0"}
(fact "list all registered factories"

  (all-factories)
  => map?
  ;;=> {:https org.apache.maven.wagon.providers.webdav.WebDavWagon}
  )

^{:refer lib.aether.wagon/create :added "3.0"}
(fact "create a wagon given a scheme"
  (create :unknown)
  => nil

  (do (add-factory :test String)
      (try (create :test)
           (catch ClassCastException e :ok)))
  => :ok)
