(ns js.blessed.project-test
  (:use code.test)
  (:require [js.blessed.project :refer :all]))

^{:refer js.blessed.project/module-package-json :added "4.0" :unchecked true}
(fact "creates a package json"

  (module-package-json "test-project")
  => map?)

^{:refer js.blessed.project/project :added "4.0" :unchecked true}
(fact "creates a blessed project"

  (project "Hello World")
  => map?)
