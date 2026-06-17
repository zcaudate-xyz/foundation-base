(ns hara.runtime.basic.type-basic-php-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-php]
            [hara.runtime.basic.type-basic :as p]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.pointer :as ptr]
            [hara.common.util :as ut]
            [std.lib.component :as component]))

(l/script+ [:php.0 :php]
  {:runtime :basic})

(fact:global
 {:skip (not (env/program-exists? "php"))
  :setup    [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer hara.runtime.basic.type-basic-php-test/CANARY-PHP :adopt true :added "4.1"}
(fact "php basic can return values"
  [(l/! [:php.0]
     (+ 1 2 3))

   (l/! [:php.0]
     (+ 10 6))

   (l/! [:php.0]
     (+ 20 (+ 10 6)))

   (l/! [:php.0]
     (+ 20 10))]
  => [6 16 36 30])

^{:refer hara.runtime.basic.type-basic/invoke-ptr-basic :added "4.1"}
(fact "php basic emits executable input and decodes output"

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
  => nil?)

^{:refer hara.runtime.basic.type-basic/invoke-ptr-basic :added "4.1"}
(fact "php basic survives eval errors and continues serving requests"

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
  => [:thrown 6])
