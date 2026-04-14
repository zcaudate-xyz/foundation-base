(ns js.cell.kernel.base-link-eval-test
  (:require [std.lang :as l]
            [js.cell.kernel.base-link-eval :as base-link-eval])
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
(fact "posts code to worker"
  ^:hidden
  
  (let [out (macroexpand-1 '(base-link-eval/wait-post worker (repl/notify true)))]
    (first out)
    => 'xt.lang.common-notify/wait-on
    
    (-> out last first)
    => '.)
  
  (let [out (macroexpand-1 '(base-link-eval/wait-post worker
                               (repl/notify true)
                               "eval"
                               {}
                               "id-action"))]
    (str out)
    => #".*postMessage.*id-action.*"))

^{:refer js.cell.kernel.base-link-eval/async-post :added "4.0"}
(fact "helper for async post"
  ^:hidden
  
  (binding [base-link-eval/*temp-id* "temp-1"]
    (macroexpand-1 '(base-link-eval/async-post [1 2 3 4])))
  => '(postMessage
       {:op "eval",
        :id "temp-1",
        :status "ok",
        :body (JSON.stringify {:type "data", :value [1 2 3 4]})}))

^{:refer js.cell.kernel.base-link-eval/post-eval :added "4.0"}
(fact "posts to worker, works in conjunction with async-post"
  ^:hidden
  
  (resolve 'js.cell.kernel.base-link-eval/post-eval)
  => var?)

^{:refer js.cell.kernel.base-link-eval/wait-eval :added "4.0"}
(fact "posts code to worker with eval"
  ^:hidden
  
  (let [out (macroexpand-1 '(base-link-eval/wait-eval link
                               [1 2 3 4]
                               true
                               "eval-2"))]
    (first out)
    => 'xt.lang.common-notify/wait-on
    
    (-> out last first)
    => '.)
  
  (let [out (macroexpand '(base-link-eval/wait-eval [link 100]
                              (base-link-eval/async-post [1 2 3 4])
                              true
                              "eval-3"))]
    (str out)
    => #".*wait-on-fn.*eval-3.*:async true.*"))
