(ns rt.basic.type-basic-php-test
  (:use code.test)
  (:require [rt.basic.impl-annex.process-php]
            [rt.basic.type-basic :as p]
            [rt.basic.type-common :as common]
            [std.lang :as l]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.util :as ut]
            [std.lib.component :as component]))

(l/script+ [:php.0 :php]
  {:runtime :basic})

(fact:global
 {:setup    [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

(def CANARY-PHP
  (common/program-exists? "php"))

^{:refer rt.basic.type-basic-php-test/CANARY-PHP :adopt true :added "4.1"}
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

^{:refer rt.basic.type-basic/invoke-ptr-basic :added "4.1"}
(fact "php basic emits executable input and decodes output"
  ^:hidden

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
