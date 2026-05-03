(ns xtbench.lua.sample.sql-util-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(fact "encodes a value to sql"

  (!.lua (string.format "%0.f" 100000000000000000))
  => "100000000000000000")
