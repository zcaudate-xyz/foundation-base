(ns hara.rt.basic.type-basic-php-test
  (:use code.test)
  (:require [hara.rt.basic.impl-annex.process-php]
            [hara.rt.basic.type-basic :as p]
            [hara.rt.basic.type-common :as common]
            [hara.lang :as l]
            [hara.lang.base.pointer :as ptr]
            [hara.lang.base.util :as ut]
            [std.lib.component :as component]))

(l/script+ [:php.0 :php]
  {:runtime :basic})

(fact:global
 {:setup    [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

(def CANARY-PHP
  (common/program-exists? "php"))

^{:refer hara.rt.basic.type-basic-php-test/CANARY-PHP :adopt true :added "4.1"}
(fact "php basic can return values"
  (if CANARY-PHP
    [(l/! [:php.0]
       (+ 1 2 3))

     (l/! [:php.0]
       (+ 10 6))

     (l/! [:php.0]
       (+ 20 (+ 10 6)))

     (l/! [:php.0]
       (+ 20 10))]
    :php-unavailable)
  => (any [6 16 36 30]
           :php-unavailable))

^{:refer hara.rt.basic.type-basic/invoke-ptr-basic :added "4.1"}
(fact "php basic emits executable input and decodes output"

  (if CANARY-PHP
    (let [rt (p/rt-basic {:lang :php
                          :id "test-php-basic"
                          :program :php})]
      (try
        (let [[input raw value]
              [(-> rt
                   (p/invoke-ptr-basic (ut/lang-pointer :php)
                                       ['(+ 1 2 3 4)])
                   (ptr/with:input))
               (-> rt
                   (p/invoke-ptr-basic (ut/lang-pointer :php)
                                       ['(+ 1 2 3 4)])
                   (ptr/with:raw))
                (-> rt
                    (p/invoke-ptr-basic (ut/lang-pointer :php)
                                        ['(+ 1 2 3 4)]))]]
          input => "return (function () { return 1 + 2 + 3 + 4; })();"
          raw => #"\"value\":10"
          value => 10
          nil)
        (finally
          (component/stop rt))))
     :php-unavailable)
  => (any nil? :php-unavailable))

^{:refer hara.rt.basic.type-basic/invoke-ptr-basic :added "4.1"}
(fact "php basic survives eval errors and continues serving requests"

  (if CANARY-PHP
    (let [rt (p/rt-basic {:lang :php
                          :id "test-php-basic-error"
                          :program :php})]
      (try
        (let [error-status (try
                             (-> rt
                                 (p/invoke-ptr-basic (ut/lang-pointer :php)
                                                     ['(not_a_real_php_fn)]))
                             :no-error
                             (catch Throwable _
                               :thrown))
              value (-> rt
                        (p/invoke-ptr-basic (ut/lang-pointer :php)
                                            ['(+ 1 2 3)]))]
          [error-status value])
        (finally
          (component/stop rt))))
    :php-unavailable)
  => (any [:thrown 6] :php-unavailable))
