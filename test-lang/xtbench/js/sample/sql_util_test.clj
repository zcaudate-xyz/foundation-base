(ns xtbench.js.sample.sql-util-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(fact "encodes a value to sql"

  (!.js (xt/x:json-encode 100000000000000000))
  => "100000000000000000")
