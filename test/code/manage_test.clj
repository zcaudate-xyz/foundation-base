(ns code.manage-test
  (:use code.test)
  (:require [code.manage :refer :all]
            [code.project :as project])
  (:refer-clojure :exclude [import]))

^{:refer code.manage/analyse :added "3.0"}
(fact "analyse either a source or test file"

  (analyse 'code.manage)
  ;;#code{:test {code.manage [analyse .... vars]}}
  => code.framework.common.Entry

  (analyse '#{code.manage} {:return :summary})
  ;; {:errors 0, :warnings 0, :items 1, :results 1, :total 16}
  => map?)

^{:refer code.manage/vars :added "3.0"}
(fact "returns the list of vars in a namespace"

  (vars 'code.manage)
  
  (vars 'code.manage {:sorted false})
  
  (vars '#{code.manage} {:return #{:errors :summary}})
  => (contains-in {:errors any
                   :summary {:errors 0
                             :warnings 0
                             :items 1
                             :results 1
                             :total number?}}))

^{:refer code.manage/docstrings :added "3.0"}
(fact "returns docstrings"

  (docstrings '#{code.manage.unit} {:return :results})
  ;;{:errors 0, :warnings 0, :items 1, :results 1, :total 14}
  => map?)

^{:refer code.manage/transform-code :added "3.0"}
(fact "helper function for any arbitrary transformation of text"

  (transform-code {:transform #(str % "\n\n\n\nhello world")})
  ;; {:deletes 0, :inserts 5, :changed [arrange], :updated false}
  => map?

  (transform-code '#{code.manage.unit}
                  {:print {:summary true :result true :item true :function true}
                   :transform #(str % "\n\n\n\nhello world")
                   :full true}))

^{:refer code.manage/import :added "3.0"}
(fact "import docstrings from tests"

  (import {:write false})

  (import {:full true
           :write false
           :print {:function false}})

  (import '[code.manage.unit]
          {:print {:summary true :result true :item true}
           :write false}))

^{:refer code.manage/purge :added "3.0"}
(fact "removes docstrings from source code"

  (purge {:write false})

  (purge {:full true :write false})

  (purge '[platform.unit] {:return :summary :write false})
  ;;{:items 38, :results 32, :deletes 1272, :total 169}
  => map?)

^{:refer code.manage/missing :added "3.0"}
(fact "checks for functions with missing tests"

  (missing)

  (missing '[platform] {:print {:result false :summary false}
                        :return :all}))

^{:refer code.manage/todos :added "3.0"}
(fact "checks for tests with `TODO` as docstring"

  (todos)

  (todos '[platform] {:print {:result false :summary false}
                      :return :all}))

^{:refer code.manage/incomplete :added "3.0"}
(fact "both functions missing tests or tests with todos"

  (incomplete)

  (incomplete '[code.manage] {:print {:item true}}))

^{:refer code.manage/orphaned :added "3.0"}
(fact "tests without corresponding source code"

  (orphaned)

  (orphaned '[code.manage] {:print {:item true}}))

^{:refer code.manage/scaffold :added "3.0"}
(fact "creates a scaffold for a new or existing set of tests"

  (scaffold {:write false})

  (scaffold '[code.manage] {:print {:item true}
                            :write false}))

^{:refer code.manage/create-tests :added "3.0"}
(fact "creates and arranges the tests")

^{:refer code.manage/in-order? :added "3.0"}
(fact "checks if tests are in order"

  (in-order?)

  (in-order? '[code.manage] {:print {:item true}}))

^{:refer code.manage/arrange :added "3.0"}
(fact "arranges the test corresponding to function order"

  (arrange {:print {:function false}
            :write false})

  (arrange '[code.manage] {:print {:item true}
                           :write false}))

^{:refer code.manage/locate-code :added "3.0"}
(fact "locates code base upon query"
  ^:hidden
  
  (locate-code '[code.manage]
               {:query '[ns | {:first :import}]}))

^{:refer code.manage/locate-test :added "4.0"}
(fact "locates test based upon query"
  ^:hidden
  
  (locate-test '[code.manage]
               {:query '[ns | {:first :import}]}))

^{:refer code.manage/grep :added "3.0"}
(fact "finds a string or regular expression in files"

  (grep '[code.manage] {:query "hello"}))

^{:refer code.manage/grep-replace :added "3.0"}
(fact "grep and replaces in files"

  (grep-replace '[code.manage] {:query "hello"
                                :replace "HELLO"}))

^{:refer code.manage/unclean :added "3.0"}
(comment "finds source code that has top-level comments"

  (unclean 'code.manage)

  (unclean '[hara]))

^{:refer code.manage/unchecked :added "3.0"}
(comment "returns tests without `=>` checks"

  (unchecked)

  (unchecked '[code.manage]))

^{:refer code.manage/commented :added "3.0"}
(comment "returns tests that are in comment blocks"

  (commented)

  (commented '[code.manage]))

^{:refer code.manage/pedantic :added "3.0"}
(comment "returns tests that may be improved"

  (pedantic)

  (pedantic '[code.manage]))

^{:refer code.manage/refactor-code :added "3.0"}
(comment "refactors code based on given `:edits`"

  (refactor-code '[code.manage]
                 {:edits []}))

^{:refer code.manage/refactor-test :added "4.0"}
(comment "refactors code tests based on given `:edits`")

^{:refer code.manage/refactor-swap :added "4.0"}
(fact "refactors by providing a list of symbols to swap")

^{:refer code.manage/ns-format :added "3.0"}
(fact "formats ns forms")

^{:refer code.manage/find-usages :added "3.0"}
(comment "find usages of a var"

  (find-usages '[code.manage]
               {:var 'code.framework/analyse}))

(comment
  ^{:refer code.manage/replace-usages :added "3.0"}
  (comment "replace usages of a var"

    (replace-usages '[code.manage]
                    {:var 'code.framework/analyse
                     :new 'analyse-changed}))

  ^{:refer code.manage/refactor-ns-forms :added "3.0"}
  (comment "refactors and reorganises ns forms"

    (refactor-ns-forms '[code.manage]))

  ^{:refer code.manage/lint :added "3.0"}
  (fact "lints and reformats code"

    (lint '[code.manage]))

  ^{:refer code.manage/line-limit :added "3.0"}
  (fact "checks for code exceeding line limits"

    (line-limit '[code.manage]
                {:length 100})))

(comment
  (code.manage/import {:write true}))

(comment

  (code.manage/pedantic '[code.framework] {:print {:function true :item true :result true :summary true}})

  (code.manage/unchecked '[code.framework] {:print {:function true :item true :result true :summary true}})

  (code.manage/unchecked 'code.framework.docstring {:print {:function true :item true :result true :summary true}})

  (refactor-code '[hara] {:edits [code.format.ns/expand-shorthand
                                  code.format.ns/reorder-load-form]})

  (refactor-code '[std.lib.security.provider] {:edits [code.format.ns/expand-shorthand
                                                   code.format.ns/reorder-load-form]}))

(comment
  (find-usages ['hara] {:var 'hara.data.base.seq/index-at})

  (find-usages ['jvm.artifact] {:var 'hara.data.base.seq/object-of})

  (find-usages 'jvm.classloader.url-classloader {:var 'hara.data.base.seq/object-of})

  (replace-usages ['jvm.artifact] {:var 'hara.data.base.seq/object-of
                                   :new 'element-at})

  (replace-usages '[jvm.classloader.url-classloader]
                  {:var 'hara.data.base.seq/object-of
                   :new 'element-at}))


^{:refer code.manage/extract :added "4.0"}
(fact "TODO")
