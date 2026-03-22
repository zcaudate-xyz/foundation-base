(ns lib.aether.dependency-test
  (:require [lib.aether.dependency :refer :all])
  (:use code.test)
  (:import (org.eclipse.aether.graph Exclusion)))

^{:refer lib.aether.dependency/rep-exclusion :added "3.0"}
(fact "creates a rep from an exclusion"

  (str (rep-exclusion (artifact-exclusion "hara:hara")))
  => "hara:hara:jar:")

^{:refer lib.aether.dependency/artifact-exclusion :added "3.0"}
(fact "creates an artifact exclusion"

  (artifact-exclusion "hara:hara:jar:2.8.4")
  => Exclusion)
