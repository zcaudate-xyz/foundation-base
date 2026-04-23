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

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-base/x:b64-encode :added "4.1"}
(fact "encodes base64 strings"

  (!.js
    (xt/x:b64-encode "hello"))
  => "aGVsbG8="

  (!.py
    (xt/x:b64-encode "hello"))
  => "aGVsbG8="

  (!.lua
    (xt/x:b64-encode "hello"))
  => "aGVsbG8=")

^{:refer xt.lang.spec-base/x:b64-decode :added "4.1"}
(fact "decodes base64 strings"

  (!.js
    (xt/x:b64-decode "aGVsbG8="))
  => "hello"

  (!.py
    (xt/x:b64-decode "aGVsbG8="))
  => "hello"

  (!.lua
    (xt/x:b64-decode "aGVsbG8="))
  => "hello")


^{:refer xt.lang.spec-base/x:uri-encode :added "4.1"}
(fact "encodes uri components"

  (!.js
    (xt/x:uri-encode "hello world"))
  => "hello%20world"

  (!.py
    (xt/x:uri-encode "hello world"))
  => "hello%20world"

  (!.lua
    (xt/x:uri-encode "hello world"))
  => "hello%20world")

^{:refer xt.lang.spec-base/x:uri-decode :added "4.1"}
(fact "decodes uri components"

  (!.js
    (xt/x:uri-decode "hello%20world"))
  => "hello world"

  (!.py
    (xt/x:uri-decode "hello%20world"))
  => "hello world"

  (!.lua
    (xt/x:uri-decode "hello%20world"))
  => "hello world")
