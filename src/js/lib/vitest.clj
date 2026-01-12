(ns js.lib.vitest
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:import [["vitest" :as vitest]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "vitest"
                                   :tag "js"}]
  [suite
   test
   describe
   it
   expect
   assert
   vi
   beforeAll
   afterAll
   beforeEach
   afterEach
   onTestFailed
   onTestFinished])
