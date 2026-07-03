(ns hara.runtime.basic.impl-annex.process-perl-test
  (:require [hara.runtime.basic.impl-annex.process-perl :refer :all]
            [std.concurrent :as cc]
            [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(l/script- :perl
  {:runtime :oneshot})

(fact:global
 {:skip (not (env/program-exists? "perl"))
  :setup    [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer hara.runtime.basic.impl-annex.process-perl/CANARY :adopt true :added "4.0"}
(fact "EVALUATE perl code"
  ^:unchecked

  (!.pl (+ 1 2 3 4))
  => 10)


^{:refer hara.runtime.basic.impl-annex.process-perl/default-body-transform :added "4.1"}
(fact "transforms oneshot forms for return-eval"
  (default-body-transform '[1 2 3] {})
  => '[1 2 3]

  (default-body-transform '[1 2 3] {:bulk true})
  => '(do 1 2 3))

^{:refer hara.runtime.basic.impl-annex.process-perl/perl-body-wrap :added "4.0"}
(fact "wraps body forms in a flat do block"
  (perl-body-wrap '[1 2 3])
  => '(do 1 2 3)

  (perl-body-wrap '[(+ 1 2)])
  => '(do (+ 1 2)))

^{:refer hara.runtime.basic.impl-annex.process-perl/default-basic-body-transform :added "4.0"}
(fact "transforms basic forms for Perl without a function wrapper"
  (default-basic-body-transform '[1 2 3] {})
  => '(do [1 2 3])

  (default-basic-body-transform '(do 1 2 3) {})
  => '(do 1 2 3)

  (default-basic-body-transform '[1 2 3] {:bulk true})
  => '(do 1 2 3))

^{:refer hara.runtime.basic.impl-annex.process-perl/default-basic-client :added "4.1"}
(fact "builds perl basic client source from perl forms"
  (let [out (default-basic-client 4567 {:host "127.0.0.1"})]
    [(boolean (re-find #"use IO::Socket::INET;" out))
     (boolean (re-find #"sub client_basic" out))
     (boolean (re-find #"client_basic\(\"127\.0\.0\.1\",\s*4567" out))
     (boolean (re-find #"HOST_PLACEHOLDER" out))])
  => [true true true false])
