(ns code.project.common-test
  (:require [code.project.common :refer :all])
  (:use code.test))

^{:refer code.project.common/artifact :added "3.0"}
(fact "returns the artifact map given a symbol"

  (artifact 'tahto/tahto)
  => '{:name tahto/tahto, :artifact "tahto", :group "tahto"})
