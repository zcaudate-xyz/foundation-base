(ns code.manage.unit.scaffold-test
  (:require [code.manage.unit.scaffold :refer :all]
            [clojure.string :as str]
            [code.project :as project]
            [std.fs :as fs])
  (:use code.test))

^{:refer code.manage.unit.scaffold/test-fact-form :added "1.2"}
(fact "creates a fact form for the namespace"

  (test-fact-form 'lucid 'hello "1.1")
  => "^{:refer lucid/hello :added \"1.1\"}\n(fact \"TODO\")")

^{:refer code.manage.unit.scaffold/new-filename :added "3.0"}
(fact "creates a new file based on test namespace"

  (new-filename 'lucid.hello-test (project/project) false)
  => (str (fs/path "test/lucid/hello_test.clj")))

^{:refer code.manage.unit.scaffold/scaffold-new :added "1.2"}
(fact "creates a completely new scaffold"
  (scaffold-new 'lucid 'lucid-test ['hello] "1.1")
  => (satisfies #(clojure.string/includes? % "(ns lucid-test")))

^{:refer code.manage.unit.scaffold/scaffold-append :added "1.2"}
(fact "creates a scaffold for an already existing file"
  (scaffold-append "original" 'lucid ['hello] "1.1")
  => (satisfies #(clojure.string/includes? % "original")))

^{:refer code.manage.unit.scaffold/scaffold-arrange :added "3.0"}
(fact "arranges tests to match the order of functions in source file"
  (scaffold-arrange "^{:refer foo/a} (fact) ^{:refer foo/b} (fact)" ['b 'a])
  => "^{:refer foo/b} (fact)\n\n^{:refer foo/a} (fact)")

^{:refer code.manage.unit.scaffold/scaffold-arrange :added "4.1"}
(fact "ignores metadata forms without `:refer` when arranging"
  (let [output (scaffold-arrange (str "(ns foo-test)\n\n"
                                      "^{:seedgen/root {:all true}}\n"
                                      "(l/script- :js {:require [[foo :as f]]})\n\n"
                                      "^{:refer foo/a} (fact \"a\")\n\n"
                                      "^{:refer foo/b} (fact \"b\")\n\n"
                                      "(fact:global {:setup []})")
                                ['b 'a])]
    (and (< (str/index-of output "seedgen/root")
            (str/index-of output ":refer foo/b"))
         (< (str/index-of output ":refer foo/b")
            (str/index-of output ":refer foo/a"))
         (str/includes? output "(fact:global {:setup []})")))
  => true)

(comment
  (code.manage/import {:write true}))
