(ns js.net.http-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-tree :as tree]
             [xt.net.http-fetch :as fetch]
             [js.net.http-fetch :as js-fetch]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.net.fetch/request-http-raw :added "4.1"}
(fact "performs a http request"

  (notify/wait-on :js
    (-> (js-fetch/request-http-raw {:url "http://www.google.com"} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            (tree/tree-get-spec out))))))
  => {"body" "string", "status" "number", "headers" {}})

^{:refer js.net.fetch/request-http-client :added "4.1"}
(fact "TODO"

  (!.js
    (-> (js-fetch/create)
        (fetch/prepare-input {:url "http://www.google.com"})))
  => {"url" "http://www.google.com", "method" "GET", "headers" {}}

  (notify/wait-on :js
    (-> (fetch "http://www.google.com"
               {"method" "GET", "headers" {}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            (. out status))))
        (promise/x:promise-catch
         (fn [out]
           (repl/notify
            (. out status))))))
  
  (notify/wait-on :js
    (-> (js-fetch/create)
        (js-fetch/request-http-client {:url "http://www.google.com"} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            (tree/tree-get-spec out)))))))

^{:refer js.net.fetch/create-methods :added "4.1"}
(fact "creates the wrapper methods for fetch"

  (!.js
    (tree/tree-get-spec
     (js-fetch/create-methods)))
  => {"request_http" "function"})

^{:refer js.net.fetch/create :added "4.1"}
(fact "creates the wrapper for fetching"
  
  (notify/wait-on :js
    (-> (js-fetch/create)
        (fetch/request-http {:url "http://www.google.com"} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            (tree/tree-get-spec out))))))
  => {"body" "string", "status" "number", "headers" {}}
  
  (notify/wait-on :js
    (. (fetch  "http://www.google.com")
       (then (fn [res]
               (return
                (. res (text)))))
       (then (fn [out]
               (repl/notify out)))))
  => string?)
