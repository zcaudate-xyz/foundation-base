(ns code.test
  (:require [code.test.checker.common :as common]
            [code.test.checker.collection :as coll]
            [code.test.checker.logic :as logic]
            [code.test.base.runtime :as rt]
            [code.test.base.listener :as listener]
            [code.test.base.print :as print]
            [code.test.compile :as compile]
            [code.test.manage :as manage]
            [code.test.task :as task]
            [std.lib :as h :refer [definvoke]]))

(h/intern-in  common/throws
              common/exactly
              common/approx
              common/satisfies
              common/stores
              common/anything
              common/capture

              coll/contains
              coll/just
              coll/contains-in
              coll/just-in
              coll/throws-info

              logic/any
              logic/all
              logic/is-not

              manage/fact:global
              manage/fact:ns
              manage/fact:ns-load

              task/run
              task/run:interrupt
              task/run:current
              task/run:load
              task/run:unload
              task/run:test
              task/run-errored
              task/print-options
              task/-main

              compile/fact
              compile/fact:all
              compile/fact:rerun
              compile/fact:purge
              compile/fact:list
              compile/fact:symbol
              compile/fact:get
              compile/fact:missing
              compile/fact:exec
              compile/fact:setup
              compile/fact:setup?
              compile/fact:teardown
              compile/fact:remove
              compile/fact:template
              compile/fact:compile
              compile/=>)

(def +listeners+
  (listener/install-listeners))

(comment)
