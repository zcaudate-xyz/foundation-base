(ns code.manage.unit.scaffold-test
  (:use code.test)
  (:require [code.manage.unit.scaffold :refer :all]
            [code.project :as project]
            [std.fs :as fs]))

^{:refer code.manage.unit.scaffold/test-fact-form :added "1.2"}
(fact "creates a fact form for the namespace"

  (test-fact-form 'lucid 'hello "1.1")
  => "^{:refer lucid/hello :added \"1.1\"}\n(fact \"TODO\")")

^{:refer code.manage.unit.scaffold/new-filename :added "3.0"}
(fact "creates a new file based on test namespace"

  (new-filename 'lucid.hello-test (project/project) false)
  => (str (fs/path "test/lucid/hello_test.clj")))

^{:refer code.manage.unit.scaffold/scaffold-new :added "1.2"}
(fact "creates a completely new scaffold")

^{:refer code.manage.unit.scaffold/scaffold-append :added "1.2"}
(fact "creates a scaffold for an already existing file")

^{:refer code.manage.unit.scaffold/scaffold-arrange :added "3.0"}
(fact "arranges tests to match the order of functions in source file")

(comment
  (code.manage/import {:write true}))
