(ns hara.runtime.basic.impl.process-elisp-test
  (:use code.test)
  (:require [hara.lang :as l]
            [std.lib.env :as env]))

(l/script- :elisp
  {:runtime :basic
   :test-mode true})

(fact:global
 {:skip (not (env/program-exists? "emacs"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.basic.impl.process-elisp/+elisp-basic-config+ :added "4.1"}
(fact "elisp basic runtime evaluates simple expressions"
  (!.elisp (+ 1 2))
  => 3)


^{:refer hara.runtime.basic.impl.process-elisp/elisp-root :added "4.1"}
(fact "TODO")