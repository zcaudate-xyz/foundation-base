(ns js.lib.vitest
  (:require [std.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [test assert]))

(l/script :js
  {:import [["vitest" :as [* vitest]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
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
