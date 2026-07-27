(ns tahto.model.spec-gdscript.rewrite-test
  (:use code.test)
  (:require [tahto.model.spec-gdscript.rewrite :refer :all]))

^{:refer tahto.model.spec-gdscript.rewrite/gdscript-rewrite-stage :added "4.1"}
(fact "rewrites gdscript stages")
