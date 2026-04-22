(ns xt.sample.train-003-test
  (:use code.test)
  (:require [std.lang :as l]))

^{:seedgen/root     {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-spec/x:return-eval :added "4.1"}
(fact "evaluates code through wrapped return handlers"

  ^{:seedgen/lang     {:python  {:suppress true}}}
  (!.js
    (var encode-fn
         (fn [value id key]
           (xt/x:return-encode value id key)))
    (var wrap-fn
         (fn [gen-fn wrap-fn]
           (xt/x:return-wrap gen-fn wrap-fn)))
    (var eval-fn
         (fn [s re-wrap-fn]
           (xt/x:return-eval s re-wrap-fn)))
    (xt/x:json-decode
     (eval-fn "1 + 1"
              (fn [f]
                (return
                 (wrap-fn f
                          (fn [out]
                            (return
                             (encode-fn out "id-A" "key-B")))))))))
  => {"return" "number", "key" "key-B", "id" "id-A", "value" 2, "type" "data"})
