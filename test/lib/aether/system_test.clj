(ns lib.aether.system-test
  (:require [lib.aether.system :refer :all])
  (:use code.test))

^{:refer lib.aether.system/repository-system :added "3.0"}
(fact "creates a repository system for interfacting with maven"

  (repository-system)
  => org.eclipse.aether.RepositorySystem)