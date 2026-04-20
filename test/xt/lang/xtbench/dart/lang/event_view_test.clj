(ns
 xtbench.dart.lang.event-view-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-spec :as xt]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-repl :as repl]
   [xt.lang.event-view :as view]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-view/list-listeners, :adopt true, :added "4.0"}
(fact
 "lists all listeners"
 (!.dt
  (var
   v
   (view/create-view (fn:> [x] {:value x}) {} [3] {:value 0} nil nil))
  (view/add-listener v "a1" (fn:>) nil nil)
  (view/add-listener v "b2" (fn:>) nil nil)
  (view/add-listener v "c3" (fn:>) nil nil)
  (view/list-listeners v))
 =>
 ["a1" "b2" "c3"])

^{:refer xt.lang.event-view/pipeline-run-remote.errored,
  :adopt true,
  :added "4.0"}
(fact
 "runs the pipeline"
 ^{:hidden true}
 (!.dt
  (notify/wait-on
   :dart
   (var
    v
    (view/create-view
     nil
     {:remote {:handler (fn:> [x] (throw "ERRORED"))}}
     [3]
     ["BLAH"]
     xtd/first
     nil))
   (view/init-view v)
   (var [context disabled] (view/pipeline-prep v nil))
   (var
    async-fn
    (fn
     [handler-fn context cb]
     (var out (handler-fn context))
     (if
      (== out "ERRORED")
      (return ((xt/x:get-key cb "error") out))
      (return ((xt/x:get-key cb "success") out)))))
   (view/pipeline-run-remote context true async-fn nil nil)
   (xt/x:get-key context "acc")))
 =>
 {"error" true,
  "remote" [true "ERRORED" true],
  "post" [false],
  "pre" [false],
  "::" "view.run"})

^{:refer xt.lang.event-view/wrap-args, :added "4.0"}
(fact
 "wraps handler for context args"
 ^{:hidden true}
 (!.dt ((view/wrap-args k/identity) {:args [1]}))
 =>
 1)

^{:refer xt.lang.event-view/check-disabled, :added "4.0"}
(fact
 "checks that view is disabled"
 ^{:hidden true}
 (!.dt
  [(view/check-disabled {})
   (view/check-disabled {:input {:data [3]}})
   (view/check-disabled {:input {:data [3], :disabled true}})])
 =>
 [true false true])

^{:refer xt.lang.event-view/parse-args, :added "4.0"}
(fact
 "parses args from context"
 ^{:hidden true}
 (!.dt (view/parse-args {:input {:data [1 2 3]}}))
 =>
 [1 2 3])

^{:refer xt.lang.event-view/create-view,
  :added "4.0",
  :setup
  [(def
    +out+
    (contains-in
     {"output" {"default" "<function>"},
      "pipeline"
      {"remote" {"wrapper" "<function>"},
       "check_disabled" "<function>",
       "check_args" "<function>",
       "main" {"handler" "<function>", "wrapper" "<function>"}},
      "input" {"default" "<function>"},
      "::" "event.view",
      "options" {},
      "listeners" {}}))]}
(fact
 "creates a view"
 (!.dt
  (xtd/tree-get-data
   (view/create-view (fn:> [x] {:value x}) {} [3] {:value 0} nil nil)))
 =>
 {"output"
  {"elapsed" nil,
   "process" "<function>",
   "current" nil,
   "type" "output",
   "updated" nil,
   "default" "<function>"},
  "::" "event.view",
  "pipeline"
  {"remote" {"wrapper" "<function>"},
   "check_disabled" "<function>",
   "check_args" "<function>",
   "main" {"handler" "<function>", "wrapper" "<function>"},
   "sync" {"wrapper" "<function>"}},
  "input" {"current" nil, "updated" nil, "default" "<function>"},
  "options" {},
  "listeners" {}})

^{:refer xt.lang.event-view/view-context, :added "4.0"}
(fact
 "gets the view-context"
 (set
  (!.dt
   (var
    v
    (view/create-view (fn:> [x] {:value x}) {} [3] {:value 0} nil nil))
   (view/init-view v)
   (xtd/obj-keys (view/view-context v))))
 =>
 #{"input" "view"})

^{:refer xt.lang.event-view/add-listener, :added "4.0"}
(fact
 "adds a listener to the view"
 (!.dt
  (var out nil)
  (var
   v
   (view/create-view (fn:> [x] {:value x}) {} [3] {:value 0} nil nil))
  (view/add-listener
   v
   "a1"
   (fn [res] (:= out (xtd/tree-get-data res)) (return nil))
   nil
   nil)
  (view/trigger-listeners v "output" {:value 0})
  out)
 =>
 {"type" "output",
  "meta" {"listener/id" "a1", "listener/type" "view"},
  "data" {"value" 0}})

^{:refer xt.lang.event-view/get-input, :added "4.0"}
(fact
 "gets the view input record"
 (!.dt
  (do
   (var
    v
    (view/create-view (fn:> [x] {:value x}) {} [3] {:value 0} nil nil))
   (view/init-view v)
   (xtd/tree-get-data (view/get-input v))))
 =>
 (contains-in {"current" {"data" [3]}, "updated" integer?}))

^{:refer xt.lang.event-view/get-output, :added "4.0"}
(fact
 "gets the view output record"
 (!.dt
  (do
   (var
    v
    (view/create-view (fn:> [x] {:value x}) {} [3] {:value 0} nil nil))
   (view/init-view v)
   (xtd/tree-get-data (view/get-output v nil))))
 =>
 {"type" "output", "elapsed" nil, "current" nil, "updated" nil})

^{:refer xt.lang.event-view/set-input, :added "4.0"}
(fact
 "sets the input"
 (notify/wait-on-call
  2000
  (fn
   []
   (!.dt
    (var out nil)
    (var
     v
     (view/create-view
      (fn:> [x] {:value x})
      {}
      [3]
      {:value 0}
      nil
      nil))
    (view/add-listener
     v
     "a1"
     (fn [res] (:= out (xtd/tree-get-data res)) (return nil))
     nil
     nil)
    (view/set-input v 1)
    out)))
 =>
 (contains-in
  {"type" "view.input",
   "meta" {"listener/id" "a1", "listener/type" "view"},
   "data" map?}))

^{:refer xt.lang.event-view/set-output, :added "4.0"}
(fact
 "sets the output"
 (notify/wait-on-call
  2000
  (fn
   []
   (!.dt
    (var out nil)
    (var
     v
     (view/create-view
      (fn:> [x] {:value x})
      {}
      [3]
      {:value 0}
      nil
      nil))
    (view/add-listener
     v
     "a1"
     (fn [res] (:= out (xtd/tree-get-data res)) (return nil))
     nil
     nil)
    (view/set-output v 1 nil nil nil nil)
    out)))
 =>
 (contains-in
  {"type" "view.output",
   "meta" {"listener/id" "a1", "listener/type" "view"},
   "data" map?}))

^{:refer xt.lang.event-view/pipeline-run, :added "4.0"}
(fact
 "runs the pipeline"
 (!.dt
  (var
   v
   (view/create-view (fn:> [x] (return {:value x})) {} [3] {} nil nil))
  (view/init-view v)
  (var [context disabled] (view/pipeline-prep v nil))
  (var
   async-fn
   (fn
    [handler-fn context cb]
    (try
     (return ((xt/x:get-key cb "success") (handler-fn context)))
     (catch err (return ((xt/x:get-key cb "error") err))))))
  (view/pipeline-run context disabled async-fn nil nil nil)
  (xt/x:get-key context "acc"))
 =>
 {"::" "view.run",
  "pre" [false],
  "main" [true {"value" 3}],
  "post" [false]})

^{:refer xt.lang.event-view/pipeline-run-remote, :added "4.0"}
(fact
 "runs the remote pipeline"
 (!.dt
  (var
   v
   (view/create-view
    nil
    {:remote {:handler (fn:> [x] (return {:value x}))}}
    [3]
    nil
    nil
    nil))
  (view/init-view v)
  (var [context disabled] (view/pipeline-prep v nil))
  (var
   async-fn
   (fn
    [handler-fn context cb]
    (try
     (return ((xt/x:get-key cb "success") (handler-fn context)))
     (catch err (return ((xt/x:get-key cb "error") err))))))
  (view/pipeline-run-remote context true async-fn nil nil)
  (xt/x:get-key context "acc"))
 =>
 {"::" "view.run",
  "pre" [false],
  "remote" [true {"value" 3}],
  "post" [false]})

^{:refer xt.lang.event-view/pipeline-run-sync, :added "4.0"}
(fact
 "runs the sync pipeline"
 (!.dt
  (var
   v
   (view/create-view
    nil
    {:sync {:handler (fn:> [x] (return {:value x}))}}
    [3]
    nil
    nil
    nil))
  (view/init-view v)
  (var [context disabled] (view/pipeline-prep v nil))
  (var
   async-fn
   (fn
    [handler-fn context cb]
    (try
     (return ((xt/x:get-key cb "success") (handler-fn context)))
     (catch err (return ((xt/x:get-key cb "error") err))))))
  (view/pipeline-run-sync context true async-fn nil nil)
  (xt/x:get-key context "acc"))
 =>
 {"::" "view.run",
  "pre" [false],
  "sync" [true {"value" 3}],
  "post" [false]})

^{:refer xt.lang.event-view/get-with-lookup, :added "0.1"}
(fact
 "creates a results vector and a lookup table"
 ^{:hidden true}
 (!.dt (view/get-with-lookup [{:id "A"} {:id "B"} {:id "C"}] nil))
 =>
 {"results" [{"id" "A"} {"id" "B"} {"id" "C"}],
  "lookup" {"C" {"id" "C"}, "B" {"id" "B"}, "A" {"id" "A"}}})

^{:refer xt.lang.event-view/sorted-lookup, :added "0.1"}
(fact
 "sorted lookup for region data"
 ^{:hidden true}
 (!.dt
  ((view/sorted-lookup "name")
   [{:id "A", :name "a"}
    {:id "B", :name "b"}
    {:id "C", :name "c"}
    {:id "D", :name "d"}]))
 =>
 {"results"
  [{"id" "A", "name" "a"}
   {"id" "B", "name" "b"}
   {"id" "C", "name" "c"}
   {"id" "D", "name" "d"}],
  "lookup"
  {"C" {"id" "C", "name" "c"},
   "B" {"id" "B", "name" "b"},
   "A" {"id" "A", "name" "a"},
   "D" {"id" "D", "name" "d"}}})

^{:refer xt.lang.event-view/group-by-lookup, :added "0.1"}
(fact
 "creates group-by lookup"
 ^{:hidden true}
 (!.dt
  ((view/group-by-lookup "name")
   [{:id "A", :name "a"}
    {:id "B", :name "a"}
    {:id "C", :name "b"}
    {:id "D", :name "b"}]))
 =>
 {"results"
  [{"id" "A", "name" "a"}
   {"id" "B", "name" "a"}
   {"id" "C", "name" "b"}
   {"id" "D", "name" "b"}],
  "lookup"
  {"a" [{"id" "A", "name" "a"} {"id" "B", "name" "a"}],
   "b" [{"id" "C", "name" "b"} {"id" "D", "name" "b"}]}})
