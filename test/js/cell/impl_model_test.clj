(ns js.cell.impl-model-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]
            [std.fs :as fs]
            [js.cell.playground :as browser]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.event-view :as base-view]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt :with [defvar.js]]
             [js.core :as j]
             [js.cell.link-raw :as link-raw]
             [js.cell.link-fn :as link-fn]
             [js.cell.impl-model :as impl-model]
             [js.cell.impl-common :as impl-common]]
   :import [["tiny-worker" :as Worker]]})

(fact:global
 {:setup    [(do (l/rt:restart :js)
                 (l/rt:scaffold-imports :js))]
  :teardown  [(l/rt:stop)]})

(def +core+ (browser/play-worker true))

(defvar.js CELL
  []
  (return nil))

(defn.js get-cell
  "initialises the current link"
  {:added "0.1"}
  [worker-url]
  (var cell (-/CELL))
  (when cell (return cell))
  (:= cell (impl-common/new-cell worker-url))
  (-/CELL-reset cell)
  (return cell))

^{:refer js.cell.impl-model/async-fn :adopt true :added "4.0" :unchecked true}
(fact "async function"
  ^:hidden
  
  (j/<! (impl-model/async-fn k/identity
                             {:a 1}
                             {:success k/identity}))
  => {"a" 1})

^{:refer js.cell.impl-model/wrap-cell-args :added "4.0" :unchecked true}
(fact "puts the cell as first argument"
  ^:hidden
  
  (j/<!
   ((impl-model/wrap-cell-args
     link-fn/echo)
    {:cell (-/get-cell (fn:> (eval (@! +core+))))
     :args ["hello"]}))
  => (contains-in ["hello" integer?]))

^{:refer js.cell.impl-model/prep-view :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<!
           (. (impl-model/add-model (-/CELL)
                                    "hello"
                                    {:ping   {:handler link-fn/ping
                                              :defaultArgs []}})
              ["init"]))]}
(fact "prepares params of views"
  ^:hidden
  
  (!.js
   (impl-model/prep-view (-/CELL) "hello" "ping" {}))
  => vector?)

^{:refer js.cell.impl-model/get-view-dependents :added "4.0" :unchecked true}
(fact "gets all dependents for a view"
  ^:hidden
  
  (!.js
   (impl-model/get-view-dependents
    {:models {"test/common" {:deps {"test/common" {"b2" {"a1" true}}}}
              "test/util"   {:deps {"test/common" {"b2" {"c3" true}}}}}}
    "test/common" "b2"))
  => {"test/common" ["a1"],
      "test/util" ["c3"]})

^{:refer js.cell.impl-model/get-model-dependents :added "4.0" :unchecked true}
(fact "gets all dependents for a model"
  ^:hidden
  
  (!.js
   (impl-model/get-model-dependents
    {:models {"test/common" {:deps {"test/common" {"b2" {"a1" true}}}}
              "test/util"   {:deps {"test/common" {"b2" {"c3" true}}}}}}
    "test/common"))
  => {"test/common" true,
      "test/util" true})

^{:refer js.cell.impl-model/run-tail-call :added "4.0" :unchecked true}
(fact "helper function for tail calls on `run` commands")

^{:refer js.cell.impl-model/run-remote :added "4.0" :unchecked true}
(fact "runs the remote function")

^{:refer js.cell.impl-model/remote-call :added "4.0" :unchecked true}
(fact "runs tthe remote call")

^{:refer js.cell.impl-model/run-refresh :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<!
           (. (impl-model/add-model (-/CELL)
                                    "hello"
                                    {:ping   {:handler link-fn/ping
                                              :defaultArgs []}})
              ["init"]))]}
(fact "helper function for refresh"
  ^:hidden
  
  (j/<!
   (do:>
    (var [path context disabled]
         (impl-model/prep-view (-/CELL) "hello" "ping" {:event {}}))
    (return (impl-model/run-refresh context disabled path nil))))
  => (contains-in
      {"::" "view.run"
       "path" ["hello" "ping"],
       "pre" [false],
       "main" [true ["pong" integer?]],
       "post" [false]}))

^{:refer js.cell.impl-model/refresh-view-dependents :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<!
           (. (impl-model/add-model (-/CELL)
                                    "hello"
                                    {:ping   {:handler link-fn/ping
                                              :defaultArgs []}
                                     :ping1  {:handler link-fn/ping
                                              :defaultArgs []
                                              :deps ["ping"]}})
              ["init"]))]}
(fact "refreshes view dependents"
  ^:hidden
  
  (def +res+
    (!.js
     (var [model view] (impl-common/view-ensure (-/CELL) "hello" "ping1"))
     (. view ["output"] ["current"])))
  
  (!.js
   (impl-model/refresh-view-dependents (-/CELL)
                                       "hello" "ping"))
  => {"hello" ["ping1"]}
  
  (def +res2+
    (!.js
     (var [model view] (impl-common/view-ensure (-/CELL) "hello" "ping1"))
     (. view ["output"] ["current"])))
  
  (not= +res+ +res2+)
  => true)

^{:refer js.cell.impl-model/refresh-view :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<!
           (. (impl-model/add-model (-/CELL)
                                    "hello"
                                    {:echo-async  {:handler link-fn/echo-async
                                                   :defaultArgs ["hello" 100]}})
              ["init"]))]}
(fact "calls update on the view"
  ^:hidden

  (j/<! (impl-model/refresh-view
         (-/CELL) "hello" "echo_async" {}))
  => (contains-in
      {"::" "view.run"
       "path" ["hello" "echo_async"],
       "post"   [false],
       "main"   [true ["hello" integer?]],
       "pre"    [false],}))

^{:refer js.cell.impl-model/refresh-view-remote :added "4.0" :unchecked true}
(fact "calls update on remote function")

^{:refer js.cell.impl-model/refresh-view-dependents-unthrottled :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<!
           (. (impl-model/add-model (-/CELL)
                                    "hello"
                                    {:ping   {:handler link-fn/ping
                                              :defaultArgs []}
                                     :ping1  {:handler link-fn/ping
                                              :defaultArgs []
                                              :deps ["ping"]}})
              ["init"]))]}
(fact "refreshes dependents without throttle"
  ^:hidden
  
  (def +res+
    (!.js
     (var [model view] (impl-common/view-ensure (-/CELL) "hello" "ping1"))
     (. view ["output"] ["current"])))
  
  (j/<!
   (impl-model/refresh-view-dependents-unthrottled
    (-/CELL)
    "hello" "ping"))
  => (contains-in
      [{"path" ["hello" "ping1"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"}])
  
  (def +res2+
    (!.js
     (var [model view] (impl-common/view-ensure (-/CELL) "hello" "ping1"))
     (. view ["output"] ["current"])))
  
  (not= +res+ +res2+)
  => true)

^{:refer js.cell.impl-model/refresh-model :added "4.0" :unchecked true}
(fact "refreshes the model"
  ^:hidden
  
  (j/<!
   (impl-model/refresh-model
    (-/CELL)
    "hello"))
  => (contains-in
      [{"path" ["hello" "ping"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"}
       {"path" ["hello" "ping1"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"}]))

^{:refer js.cell.impl-model/get-model-deps :added "4.0" :unchecked true}
(fact "gets model deps"
  ^:hidden
  
  (impl-model/get-model-deps
   "hello"
   {:ping   {}
    :ping1  {:deps ["ping"]}})
  => {"hello" {"ping" {"ping1" true}}})

^{:refer js.cell.impl-model/get-unknown-deps :added "4.0" :unchecked true}
(fact "gets unknown deps"
  ^:hidden

  (!.js
   (impl-model/get-unknown-deps
    "hello"
    {:ping   {}
     :ping1  {:deps ["ping"]}}
    (impl-model/get-model-deps
     "hello"
     {:ping   {}
      :ping1  {:deps ["ping"]}})
    (-/CELL)))
  => []

  (!.js
   (impl-model/get-unknown-deps
    "hello"
    {:ping   {}
     :ping1  {:deps ["ping"]}}
    (impl-model/get-model-deps
     "hello"
     {:ping   {}
      :ping1  {:deps ["ping2"]}})
    (-/CELL)))
  => [["hello" "ping2"]])

^{:refer js.cell.impl-model/create-throttle :added "4.0" :unchecked true}
(fact "creates the throttle"
  ^:hidden

  (!.js
   (impl-model/create-throttle
    (-/CELL)
    "hello"
    nil))
  => {"queued" {}, "active" {}})

^{:refer js.cell.impl-model/create-view :added "4.0" :unchecked true}
(fact "creates a view"
  ^:hidden
  
  (!.js
   (impl-model/create-view
    (-/CELL)
    "hello"
    "ping"
    {:handler link-fn/ping
     :defaultArgs []}))
  => (contains-in
      {"::" "event.view",
       "input" {"current" {"data" []}, "updated" integer?},
       "output" {"elapsed" nil, "current" nil, "updated" nil},
       "pipeline" {"remote" {}, "main" {}},
       "options" {},
       "listeners"
       {"@/cell"
        {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}))

^{:refer js.cell.impl-model/add-model-attach :added "4.0" :unchecked true}
(fact "adds model statically"
  ^:hidden
  
  (!.js
   (impl-model/add-model-attach
    (-/CELL)
    "hello"
    {:echo-async  {:handler link-fn/echo-async
                   :defaultArgs ["hello" 100]}}))
  => (contains-in
      {"name" "hello",
       "views"
       {"echo_async"
        {"::" "event.view",
         "pipeline" {"remote" {}, "main" {}},
         "options" {},
         "input"  {"current" {"data" ["hello" 100]},
                   "updated" integer?},
         
         "output" {"elapsed" nil, "current" nil, "updated" nil},
         "listeners" {"@/cell"
                      {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}},
       "deps" {},
       "throttle" {"queued" {}, "active" {}}}))

^{:refer js.cell.impl-model/add-model :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))]}
(fact "calls update on the view"
  ^:hidden

  ;;
  ;; Update Async
  ;;
  (j/<!
   (. (impl-model/add-model
       (-/CELL)
       "hello"
       {:echo-async  {:handler link-fn/echo-async
                      :defaultArgs ["hello" 100]}})
      ["init"]))
  => (contains-in
      [{"::" "view.run"
        "path" ["hello" "echo_async"],
        
        "post"   [false],
        "main"   [true ["hello" integer?]],
        "pre"    [false],}])

  ;;
  ;; Error Async
  ;;  
  (j/<!
   (. (impl-model/add-model (-/CELL)
                            "hello"
                            {:error-async {:handler link-fn/error-async
                                           :defaultArgs [100]
                                           }})
      ["init"]))
  => (contains-in
      [{"::" "view.run"
        "path" ["hello" "error_async"],
        "error" true,
        "pre" [false],
        "main" [true
                {"body" ["error" integer?],
                 "route" "@/error-async",
                 "id" string?
                 "status" "error",
                 "input" [100],
                 "end_time" integer?
                 "op" "route",
                 "start_time" integer?}
                true],
        
        "post" [false],}])  
    
  ;;
  ;; Update Error
  ;;
  (j/<!
   (. (impl-model/add-model (-/CELL)
                            "hello"
                            {:error {:handler link-fn/error
                                     :defaultArgs []}})
      ["init"]))
  (contains-in [{"path" ["hello" "error"],
                 "error" true,
                 "post" [false],
                 "main"
                 [true
                  {"body" ["error" integer?],
                   "route" "@/error",
                   "id" string?
                   "status" "error",
                   "input" [],
                   "end_time" integer?
                   "op" "route",
                   "start_time" integer?}
                  true],
                 "pre" [false],
                 "::" "view.run"}]))

^{:refer js.cell.impl-model/remove-model :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<!
           (. (impl-model/add-model
               (-/CELL)
               "hello"
               {:echo-async  {:handler link-fn/echo-async
                              :defaultArgs ["hello" 100]}})
              ["init"]))]}
(fact "removes the model"
  ^:hidden
  
  (impl-model/remove-model
   (-/CELL)
   "hello")
  => (contains-in
      {"name" "hello",
       "views"
       {"echo_async"
        {"::" "event.view",
         "output"
         {"elapsed" integer?
          "current" ["hello" integer?],
          "updated" integer?},
         "pipeline" {"remote" {}, "main" {}},
         "input"
         {"current" {"data" ["hello" 100]}, "updated" integer?},
         
         "options" {},
         "listeners"
         {"@/cell"
          {"meta"
           {"listener/id" "@/cell", "listener/type" "view"}}}}},
       "deps" {},
       "init" {},
       "throttle" {"queued" {}, "active" {}}}))

^{:refer js.cell.impl-model/remove-view :added "4.0" :unchecked true
  :setup [(j/<!
           (. (impl-model/add-model
               (-/CELL)
               "hello"
               {:echo-async  {:handler link-fn/echo-async
                              :defaultArgs ["hello" 100]}})
              ["init"]))]}
(fact "removes the view"
  ^:hidden

  (!.js
   (impl-model/remove-view
    (-/CELL) "hello" "echo_async"))
  => (contains-in
      {"::" "event.view",
       "pipeline" {"remote" {}, "main" {}},
       "options" {},
       "input" {"current" {"data" ["hello" 100]}, "updated" integer?},
       "output" {"elapsed" integer?
                 "current" ["hello" integer?],
                 "updated" integer?},
       "listeners"
       {"@/cell"
        {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}))

^{:refer js.cell.impl-model/model-update :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<!
           (. (impl-model/add-model (-/CELL)
                                    "hello"
                                    {:ping   {:handler link-fn/ping
                                              :defaultArgs []}
                                     :ping1  {:handler link-fn/ping
                                              :defaultArgs []
                                              :deps ["ping"]}})
              ["init"]))]}
(fact "updates a model"
  ^:hidden
  
  (j/<!
   (impl-model/model-update
    (-/CELL)
    "hello"
    {}))
  => (contains-in
      {"ping1"
       {"path" ["hello" "ping1"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"},
       "ping"
       {"path" ["hello" "ping"],
        "post" [false],
        "main" [true ["pong" integer?]],
        "pre" [false],
        "::" "view.run"}}))

^{:refer js.cell.impl-model/view-update :added "4.0" :unchecked true}
(fact "updates a view"
  ^:hidden
  
  (j/<!
   (k/first
    (impl-model/view-update
     (-/CELL)
     "hello"
     "ping")))
  => (contains-in
      {"path" ["hello" "ping"],
       "post" [false],
       "main" [true ["pong" integer?]],
       "pre" [false],
       "::" "view.run"}))

^{:refer js.cell.impl-model/view-set-input :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<!
           (. (impl-model/add-model (-/CELL)
                                    "hello"
                                    {:ping   {:handler link-fn/echo
                                              :defaultArgs ["foo"]}})
              ["init"]))]}
(fact "sets the view input"
  ^:hidden
  
  (j/<!
   (k/first
    (impl-model/view-set-input
     (-/CELL) "hello" "ping" {:data ["bar"]})))
  => (contains-in
      {"path" ["hello" "ping"],
       "post" [false],
       "main" [true ["bar" integer?]],
       "pre" [false],
       "::" "view.run"}))

^{:refer js.cell.impl-model/trigger-model-raw :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<!
           (. (impl-model/add-model (-/CELL)
                                    "hello"
                                    {:ping   {:handler link-fn/echo
                                              :defaultArgs ["foo"]}})
              ["init"]))]}
(fact "triggers a model"
  ^:hidden
  
  (!.js
   (impl-model/trigger-model-raw (-/CELL)
                                 (. (-/CELL)
                                    ["models"]
                                    ["hello"])
                                 "hello"
                                 {}))
  => ["ping"])

^{:refer js.cell.impl-model/trigger-model :added "4.0" :unchecked true}
(fact "triggers a model"
  ^:hidden
  
  (!.js
   (impl-model/trigger-model (-/CELL)
                        "hello"
                        "hello"
                        {}))
  => ["ping"])

^{:refer js.cell.impl-model/trigger-view :added "4.0" :unchecked true}
(fact "triggers a view"
  ^:hidden
  
  (notify/wait-on :js
    (. (impl-model/trigger-view  (-/CELL)
                                 "hello"
                                 "ping"
                                 "hello"
                                 {})
       [0]
       (then (repl/>notify))))
  => (contains-in
      {"path" ["hello" "ping"],
       "post" [false],
       "main" [true ["foo" integer?]],
       "pre" [false],
       "::" "view.run"}))

^{:refer js.cell.impl-model/trigger-all :added "0.1"
  :setup [(j/<!
           (. (impl-model/add-model (-/CELL)
                                    "hello"
                                    {:ping   {:handler link-fn/echo
                                              :defaultArgs ["foo"]}})
              ["init"]))]}
(fact "triggers all models in cell"
  ^:hidden
  
  (!.js (impl-model/trigger-all (-/CELL)
                             "hello"
                             {:a 1}))
  => {"hello" ["ping"]})

^{:refer js.cell.impl-model/add-raw-callback :added "4.0" :unchecked true}
(fact "adds the callback on events"
  ^:hidden
  
  (!.js (impl-model/add-raw-callback (-/CELL)))
  => vector?)

^{:refer js.cell.impl-model/remove-raw-callback :added "4.0" :unchecked true}
(fact "removes the cell callback"
  ^:hidden
  
  (!.js (impl-model/remove-raw-callback (-/CELL)))
  => vector?)

(comment
  
  (-/get-model-deps
   "test/common"
   {:echo  {:deps []}
    :echo1 {:deps [["test/common" "echo"]]}
    :echo2 {:deps ["echo1"]}
    :ping  {:deps [["test/util"  "hello"]]}})
  {"test/common" {"echo1" {"echo2" true},
                  "echo"  {"echo1" true}},
   "test/util" {"hello"   {"ping" true}}}
  
  {"test/common"  {"echo1" {"echo2" true}},
   "test/common " {"echo"  {"echo1" true}}}
  


  {"dependents" {"echo1" [["test/common" "echo2"]],
                 "echo"  [["test/common" "echo1"]],
                 "echo2" []},
   "deps" {}})

(comment
  (require '[js.cell.playground :as browser])
  (def +core+ (browser/play-worker true))
  (do (l/rt:restart :js)
      (l/rt:scaffold-imports :js))

  (!.js
   (:= CELL
       (impl-common/new-cell
        (fn []
          (eval (@! +core+))))))
  
  (j/<! (impl-common/call
         (. CELL
            ["link"]) "HELLO"))

  (j/<! (link-fn/route-list
         (. CELL
            ["link"]))))
