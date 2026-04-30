(ns xt.event.util-decorate-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-decorate :as decorate]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-decorate :as decorate]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.event.util-decorate/incr-fn :added "4.1"}
(fact "creates an incrementing id generator"

  (!.js
   (var next-id (decorate/incr-fn))
   [(next-id) (next-id) (next-id)])
  => ["id-0" "id-1" "id-2"]

  (!.py
   (var next-id (decorate/incr-fn))
   [(next-id) (next-id) (next-id)])
  => ["id-0" "id-1" "id-2"])

^{:refer xt.event.util-decorate/plugin-timing :added "4.1"}
(fact "tracks start, end, and elapsed timing"

  (!.js
   (var plugin (decorate/plugin-timing {}))
   ((. plugin ["on_setup"]) [])
   ((. plugin ["on_teardown"]))
   {"name" (. plugin ["name"])
    "output" (. plugin ["output"])})
  => (contains {"name" "timing"
                "output" (contains {"start" integer?
                                    "end" integer?
                                    "elapsed" integer?})})

  (!.py
   (var plugin (decorate/plugin-timing {}))
   ((. plugin ["on_setup"]) [])
   ((. plugin ["on_teardown"]))
   {"name" (. plugin ["name"])
    "output" (. plugin ["output"])})
  => (contains {"name" "timing"
                "output" (contains {"start" integer?
                                    "end" integer?
                                    "elapsed" integer?})}))

^{:refer xt.event.util-decorate/plugin-counts :added "4.1"}
(fact "tracks success and error counts"

  (!.js
   (var plugin (decorate/plugin-counts {}))
   ((. plugin ["on_success"]) 1)
   ((. plugin ["on_error"]) "boom")
   ((. plugin ["on_success"]) 2)
   {"name" (. plugin ["name"])
    "output" (. plugin ["output"])})
  => {"name" "counts"
      "output" {"success" 2
                "error" 1}}

  (!.py
   (var plugin (decorate/plugin-counts {}))
   ((. plugin ["on_success"]) 1)
   ((. plugin ["on_error"]) "boom")
   ((. plugin ["on_success"]) 2)
   {"name" (. plugin ["name"])
    "output" (. plugin ["output"])})
  => {"name" "counts"
      "output" {"success" 2
                "error" 1}})

^{:refer xt.event.util-decorate/to-handle-callback :added "4.1"}
(fact "adapts success error and finally callbacks"

  (!.js
   [(decorate/to-handle-callback {"success" "A"
                                  "error" "B"
                                  "finally" "C"})
    (xt/x:obj-keys (decorate/to-handle-callback nil))])
  => (just-in
      [{"on_error" "B", "on_teardown" "C", "on_success" "A"}
       (just ["on_success" "on_error" "on_teardown"] :in-any-order)])

  (!.py
   [(decorate/to-handle-callback {"success" "A"
                                  "error" "B"
                                  "finally" "C"})
    (xt/x:obj-keys (decorate/to-handle-callback nil))])
  => (just-in
      [{"on_error" "B", "on_teardown" "C", "on_success" "A"}
       (just ["on_success" "on_error" "on_teardown"] :in-any-order)]))

^{:refer xt.lang.spec-promise/x:promise-run :added "4.1"}
(fact "wraps a raw value in the promise interface"

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (spec-promise/x:promise-run 42)
     (repl/>notify)))
  => 42

  (notify/wait-on :python
    (spec-promise/x:promise-then
     (spec-promise/x:promise-run 42)
     (repl/>notify)))
  => 42)

^{:refer xt.event.util-decorate/new-handle :added "4.1"}
(fact "creates a decorated handle with plugins"

  (!.js
   (var handle
        (decorate/new-handle
         (fn [] (return 1))
         [decorate/plugin-counts]
         {:name "demo"
          :delay 25
          :id-fn (fn [] (return "fixed"))}))
   {"name" (. handle ["name"])
    "delay" (. handle ["delay"])
    "plugin_count" (xt/x:len (. handle ["plugins"]))
    "id" ((. handle ["id_fn"]))})
  => {"name" "demo"
      "delay" 25
      "plugin_count" 1
      "id" "fixed"}

  (!.py
   (var handle
        (decorate/new-handle
         (fn [] (return 1))
         [decorate/plugin-counts]
         {:name "demo"
          :delay 25
          :id-fn (fn [] (return "fixed"))}))
   {"name" (. handle ["name"])
    "delay" (. handle ["delay"])
    "plugin_count" (xt/x:len (. handle ["plugins"]))
    "id" ((. handle ["id_fn"]))})
  => {"name" "demo"
      "delay" 25
      "plugin_count" 1
      "id" "fixed"})

^{:refer xt.event.util-decorate/run-handle :added "4.1"}
(fact "runs a handle and resolves a receipt with plugin output"

  (notify/wait-on :js
    (var handle
         (decorate/new-handle
          (fn [] (return 1))
          [decorate/plugin-counts
           decorate/plugin-timing]
          {:delay 10}))
    (spec-promise/x:promise-then
     (decorate/run-handle handle [] nil)
     (repl/>notify)))
  => (contains {"id" "id-0"
                "status" "success"
                "value" 1
                "counts" {"success" 1
                          "error" 0}
                "timing" (contains {"start" integer?
                                    "end" integer?
                                    "elapsed" integer?})})

  (notify/wait-on :python
    (var handle
         (decorate/new-handle
          (fn [] (return 1))
          [decorate/plugin-counts
           decorate/plugin-timing]
          {:delay 10}))
    (spec-promise/x:promise-then
     (decorate/run-handle handle [] nil)
     (repl/>notify)))
  => (contains {"id" "id-0"
                "status" "success"
                "value" 1
                "counts" {"success" 1
                          "error" 0}
                "timing" (contains {"start" integer?
                                    "end" integer?
                                    "elapsed" integer?})}))

(comment
  (s/snapto)
  
  (s/seedgen-benchadd '[xt.event.util-decorate] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.util-decorate]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.util-decorate]  {:lang [:lua :python] :write true}))
