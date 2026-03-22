(ns lib.aether.local-repo-test
  (:require [lib.aether.local-repo :refer :all]
            [std.object :as object])
  (:use code.test)
  (:import [org.eclipse.aether.repository LocalRepository]))

^{:refer lib.aether.local-repo/local-repo :added "3.0"}
(fact "creates a `LocalRepository` from a string"

  (local-repo)
  => LocalRepository ;; #local "<.m2/repository>"

  ;; hooks into std.object
  (-> (local-repo "/tmp")
      (object/to-data))
  => "/tmp")

(fact "creates a `LocalRepository` from a string"

  (object/from-data "/tmp" LocalRepository)
  ;;=> #local "/tmp"
  )