(ns lib.aether.session-test
  (:require [lib.aether.session :refer :all]
            [lib.aether.system :as system])
  (:use code.test))

^{:refer lib.aether.session/session :added "3.0"}
(fact "creates a session from a system:"

  (session (system/repository-system)
           {})
  => org.eclipse.aether.RepositorySystemSession)