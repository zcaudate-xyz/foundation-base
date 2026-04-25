(ns xt.lang.spec-runtime-http-test
  (:use code.test)
  (:require [clojure.set :as set]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-runtime-http :as spec-http]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-runtime-http :as spec-http]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-runtime-http :as spec-http]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-runtime-http/x:b64-encode :added "4.1"}
(fact "encodes base64 strings"

  ^{:seedgen/base       {:all    {:suppress true}
                         :python {:suppress false}}}
  (!.js
    (spec-http/x:b64-encode "hello"))
  => "aGVsbG8="

  (!.py
    (spec-http/x:b64-encode "hello"))
  => "aGVsbG8=")

^{:refer xt.lang.spec-runtime-http/x:b64-decode :added "4.1"}
(fact "decodes base64 strings"

  ^{:seedgen/base       {:all     {:suppress true}
                         :python  {:suppress false}}}
  (!.js
    (spec-http/x:b64-decode "aGVsbG8="))
  => "hello"

  (!.py
    (spec-http/x:b64-decode "aGVsbG8="))
  => "hello")

^{:refer xt.lang.spec-runtime-http/x:uri-encode :added "4.1"}
(fact "encodes uri components"

  ^{:seedgen/base       {:all     {:suppress true}
                         :python  {:suppress false}}}
  (!.js
    (spec-http/x:uri-encode "hello world"))
  => "hello%20world"

  (!.py
    (spec-http/x:uri-encode "hello world"))
  => "hello%20world")

^{:refer xt.lang.spec-runtime-http/x:uri-decode :added "4.1"}
(fact "decodes uri components"

  ^{:seedgen/base       {:all     {:suppress true}
                         :python  {:suppress false}}}
  (!.js
    (spec-http/x:uri-decode "hello%20world"))
  => "hello world"

  (!.py
    (spec-http/x:uri-decode "hello%20world"))
  => "hello world")

(comment

  (s/seedgen-benchadd '[xt.lang.common] {:lang [:r] :write true})
  (s/seedgen-benchadd '[xt.lang.spec-base] {:lang [:r] :write true})
  
  
  (s/seedgen-langadd 'xt.lang.spec-runtime-http {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.spec-runtime-http {:lang [:lua :python] :write true}))
