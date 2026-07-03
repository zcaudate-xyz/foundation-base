(ns hara.runtime.basic.impl.process-scheme-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.basic.impl.process-scheme :refer [scheme-root]]
            [std.lib.env :as env]))

(l/script- :scheme
  {:runtime :basic
   :test-mode true})

(fact:global
 {:skip (not (env/program-exists? "racket"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.basic.impl.process-scheme/+scheme-basic-config+ :added "4.1"}
(fact "scheme basic runtime evaluates simple expressions"
  (!.scheme (+ 1 2))
  => 3)


^{:refer hara.runtime.basic.impl.process-scheme/scheme-root :added "4.1"}
(fact "returns the project root directory"
  (scheme-root)
  => (or (System/getenv "PWD")
         (System/getProperty "user.dir")))