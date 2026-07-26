tahto/runtime/basic/impl/process_elisp_test.clj:1:(ns tahto.runtime.basic.impl.process-elisp-test
  (:use code.test)
  (:require [tahto.core :as l]
tahto/runtime/basic/impl/process_elisp_test.clj:4:            [tahto.runtime.basic.impl.process-elisp :refer [elisp-root]]
            [std.lib.env :as env]))

(l/script- :elisp
  {:runtime :basic
   :test-mode true})

(fact:global
 {:skip (not (env/program-exists? "emacs"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

tahto/runtime/basic/impl/process_elisp_test.clj:16:^{:refer tahto.runtime.basic.impl.process-elisp/+elisp-basic-config+ :added "4.1"}
(fact "elisp basic runtime evaluates simple expressions"
  (!.elisp (+ 1 2))
  => 3)


tahto/runtime/basic/impl/process_elisp_test.clj:22:^{:refer tahto.runtime.basic.impl.process-elisp/elisp-root :added "4.1"}
(fact "returns the project root directory"
  (elisp-root)
  => (or (System/getenv "PWD")
         (System/getProperty "user.dir")))