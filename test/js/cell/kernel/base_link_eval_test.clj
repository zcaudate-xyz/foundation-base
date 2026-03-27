(ns js.cell.kernel.base-link-eval-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.cell.kernel.base-link-eval :as base-link-eval]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.base-link-eval/wait-post :added "4.0"}
(fact "posts code to worker")

^{:refer js.cell.kernel.base-link-eval/async-post :added "4.0"}
(fact "helper for async post")

^{:refer js.cell.kernel.base-link-eval/post-eval :added "4.0"}
(fact "posts to worker, works in conjunction with async-post")

^{:refer js.cell.kernel.base-link-eval/wait-eval :added "4.0"}
(fact "posts code to worker with eval")
