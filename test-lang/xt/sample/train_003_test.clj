(ns xt.sample.train-003-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root     {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-base/x:return-eval :added "4.1"}
(fact "evaluates code through wrapped return handlers"

  ^{:seedgen/base   {:lua    {:transform {"1 + 1" "return 1 + 1" }}
                     :python {:suppress true}
                     :dart   {:suppress true}}}
   (!.js
     (var encode-fn
          (fn [value id key]
            (return
             (xt/x:return-encode value id key))))
     (var wrap-fn
          (fn [gen-fn wrap-fn]
            (return
             (xt/x:return-wrap gen-fn wrap-fn))))
     (var eval-fn
          (fn [s re-wrap-fn]
            (return
             (xt/x:return-eval s re-wrap-fn))))
     (xt/x:json-decode
      (eval-fn "1 + 1"
               (fn [f]
                 (return
                  (wrap-fn f
                           (fn [out]
                             (return
                              (encode-fn out "id-A" "key-B")))))))))
  => (contains-in {"key" "key-B", "id" "id-A", "value" 2, "type" "data"}))
