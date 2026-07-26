tahto/runtime/basic/impl/process_scheme_test.clj:1:(ns tahto.runtime.basic.impl.process-scheme-test
  (:use code.test)
  (:require [tahto.core :as l]
tahto/runtime/basic/impl/process_scheme_test.clj:4:            [tahto.runtime.basic.impl.process-scheme :refer [scheme-root]]
            [std.lib.env :as env]))

(l/script- :scheme
  {:runtime :basic
   :test-mode true})

(fact:global
 {:skip (not (env/program-exists? "racket"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

tahto/runtime/basic/impl/process_scheme_test.clj:16:^{:refer tahto.runtime.basic.impl.process-scheme/+scheme-basic-config+ :added "4.1"}
(fact "scheme basic runtime evaluates simple expressions"
  (!.scheme (+ 1 2))
  => 3)


tahto/runtime/basic/impl/process_scheme_test.clj:22:^{:refer tahto.runtime.basic.impl.process-scheme/scheme-root :added "4.1"}
(fact "returns the project root directory"
  (scheme-root)
  => (or (System/getenv "PWD")
         (System/getProperty "user.dir")))