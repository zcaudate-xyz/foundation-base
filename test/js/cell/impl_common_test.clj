(ns js.cell.impl-common-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]
            [std.fs :as fs]
            [js.cell.playground :as browser]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt :with [defvar.js]]
             [js.core :as j]
             [js.cell.link-raw :as link-raw]
             [js.cell.link-fn :as link-fn]
             [js.cell.impl-model :as impl-model]
             [js.cell.impl-common :as impl-common]]
   :import [["tiny-worker" :as Worker]]})

(fact:global
 {:setup     [(do (l/rt:restart :js)
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


^{:refer js.cell.impl-common/new-cell-init :added "4.0" :unchecked true}
(fact "creates a record for asynchronous resolve"
  ^:hidden
  
  (set (!.js
        (k/obj-keys
         (impl-common/new-cell-init))))
  => #{"resolve" "current" "reject"}
  
  ;;
  ;; RESOLVE
  ;;  
  (notify/wait-on :js
   (:= (!:G INIT) (impl-common/new-cell-init))
   ((. INIT ["resolve"]) true)
   (. INIT ["current"]
      (then (repl/>notify))))
  => true

  ;;
  ;; AFTER RESOLVE
  ;;
  (notify/wait-on :js
    (. INIT ["current"]
       (then (repl/>notify))))
  => true)

^{:refer js.cell.impl-common/new-cell :adopt true :added "0.1"
  :setup [(fact:global :setup)]}
(fact "makes the core link"
  ^:hidden
  
  (notify/wait-on :js
    (var cell (impl-common/new-cell
               (fn []
                 (eval ^{:indent 2}
                       (:- (@! (str "`\n"
                                    (browser/play-worker true)
                                    "`")))))))

    (var #{link} cell)
    (link-raw/add-callback link
                           "test"
                           "hello"
                           (repl/>notify))
    (link-raw/post-eval link
      (postMessage {:op "stream"
                    :topic "hello"
                    :status "ok"
                    :body {}})))
  => {"body" {}, "status" "ok", "op" "stream", "topic" "hello"}

  (notify/wait-on :js
    (var cell (impl-common/new-cell
               (fn []
                 (eval ^{:indent 2}
                       (:- (@! (str "`\n"
                                    (browser/play-worker true)
                                    "`")))))))
    (. (link-fn/error (. cell ["link"]))
       (catch (repl/>notify))))
  => (contains-in
      {"body" ["error" integer?],
       "route" "@/error",
       "id" string?
       "status" "error", "input" [],
       "start_time" integer?,
       "end_time" integer?,
       "op" "route"}))

^{:refer js.cell.impl-common/list-models :added "0.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (!.js
           (impl-model/add-model (-/CELL)
                                 "common/hello"
                                 {:echo  {:handler link-fn/echo}})
           (impl-model/add-model (-/CELL)
                                 "common/hello1"
                                 {:echo  {:handler link-fn/echo}}))]}
(fact "lists all models"
  ^:hidden
  
  (set (!.js
        (impl-common/list-models (-/CELL))))
  => #{"common/hello1" "common/hello"})

^{:refer js.cell.impl-common/call :added "4.0" :unchecked true}
(fact "conducts a call, either for a link or cell"
  ^:hidden
  
  (j/<!
   (impl-common/call (-/CELL)
              {:op "route"
               :route "@/echo"
               :body ["hello"]}))
  => (contains ["hello" integer?])

  (j/<!
   (impl-common/call (. (-/CELL) ["link"])
               {:op "route"
                :route "@/echo"
                :body ["hello"]}))
  => (contains ["hello" integer?]))

^{:refer js.cell.impl-common/model-get :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (notify/wait-on :js
           (impl-model/add-model (-/CELL)
                                 "hello"
                                 {:echo  {:handler link-fn/echo
                                          :defaultArgs ["TEST"]}})
           (. (impl-common/model-get (-/CELL) "hello")
              ["init"]
              (then (repl/>notify))))]}
(fact "gets a model"
  ^:hidden
  
  (!.js
   (impl-common/model-get (-/CELL) "hello"))
  => (contains-in
      {"name" "hello",
       "views"
       {"echo"
        {"output"
         {"elapsed" integer?
          "current" ["TEST" integer?],
          "updated" integer?},
         "pipeline" {"remote" {}, "main" {}},
         "input" {"current" {"data" ["TEST"]}, "updated" integer?},
         "::" "event.view",
         "options" {},
         "listeners"
         {"@/cell"
          {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}},
       "deps" {},
       "init" {},
       "throttle" {"queued" {}, "active" {}}})
  
  (!.js
   (impl-common/model-get (-/CELL) "WRONG"))
  => nil)

^{:refer js.cell.impl-common/model-ensure :added "4.0" :unchecked true}
(fact "throws an error if model is not present"
  ^:hidden
  
  (!.js
   (impl-common/model-ensure (-/CELL) "WRONG"))
  => (throws))

^{:refer js.cell.impl-common/list-views :added "0.1"}
(fact "lists views in the model"
  ^:hidden
  
  (!.js
   (impl-common/list-views (-/CELL) "hello"))
  => ["echo"])

^{:refer js.cell.impl-common/view-ensure :added "0.1"
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (notify/wait-on :js
           (impl-model/add-model (-/CELL)
                                 "hello"
                                 {:echo  {:handler link-fn/echo
                                          :defaultArgs ["TEST"]}})
           (. (impl-common/model-get (-/CELL) "hello")
              ["init"]
              (then (repl/>notify))))]}
(fact "gets the view"
  ^:hidden
  
  (!.js
   (k/second (impl-common/view-ensure (-/CELL)
                                      "hello"
                                      "echo")))
  => (contains-in
      {"::" "event.view",
         "pipeline"  {"remote" {}, "main" {}},
         "output"    {"elapsed" integer?
                      "current" ["TEST" integer?],
                      "updated" integer?},
         "input"     {"current" {"data" ["TEST"]},
                      "updated" integer?},
         "options"   {},
         "listeners" {"@/cell"
                      {"meta" {"listener/id" "@/cell", "listener/type" "view"}}}}))

^{:refer js.cell.impl-common/view-access :added "4.0" :unchecked true}
(fact "acts as the view access function")

^{:refer js.cell.impl-common/clear-listeners :adopt true :added "4.0" :unchecked true}
(fact "clears all listeners")

^{:refer js.cell.impl-common/add-listener :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (j/<! (. (impl-model/add-model (-/CELL)
                                         "hello"
                                         {:echo {:handler link-fn/echo
                                                 :defaultArgs ["TEST"]}})
                   ["init"]))]}
(fact "add listener to cell"
  ^:hidden

  (notify/wait-on :js
    (impl-common/add-listener (-/CELL)
                              ["hello" "echo"]
                              "@react/1234"
                               (fn [event]
                                 (var #{type} event)
                                 (when (== type "view.output")
                                   (repl/notify event))))
    
    (impl-model/refresh-view (-/CELL)
                             "hello"
                             "echo"))
  => (contains-in
      {"path" ["hello" "echo"],
       "type" "view.output",
       "meta" {"listener/id" "@react/1234", "listener/type" "cell"},
       "data"
       {"current" ["TEST" integer?],
        "updated" integer?
        "pending" true}}))

^{:refer js.cell.impl-common/remove-listener :added "4.0" :unchecked true}
(fact "remove listeners from cell"
  ^:hidden
  
  (!.js
   (impl-common/remove-listener (-/CELL)
                                ["hello" "echo"]
                                "@react/1234"))
  => map?)

^{:refer js.cell.impl-common/list-listeners :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))
          (!.js
           (impl-common/add-listener (-/CELL)
                                     ["hello" "echo"]
                                     "@react/1234"
                                     (fn:>))
           (impl-common/add-listener (-/CELL)
                                     ["hello" "echo"]
                                     "@react/5678"
                                     (fn:>)))]}
(fact "lists listeners in a cell path"
  ^:hidden
  
  (!.js
   (impl-common/list-listeners (-/CELL)
                               ["hello" "echo"]))
  => ["@react/1234" "@react/5678"])

^{:refer js.cell.impl-common/list-all-listeners :added "4.0" :unchecked true}
(fact "lists all listeners in cell"
  ^:hidden
  
  (!.js
   (impl-common/list-all-listeners (-/CELL)))
  => {"hello" {"echo" ["@react/1234" "@react/5678"]}})

^{:refer js.cell.impl-common/trigger-listeners :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (notify/wait-on :js
            (. (-/get-cell (fn []
                             (eval (@! +core+))))
               ["init"]
               (then (repl/>notify))))]}
(fact "triggers listeners"
  ^:hidden
  
  (!.js
   (impl-common/add-listener (-/CELL)
                             ["hello" "echo"]
                             "@react/1234"
                             (fn:>))
   [(impl-common/trigger-listeners (-/CELL)
                                   ["hello" "echo"]
                                   {})
    (impl-common/trigger-listeners (-/CELL)
                                   ["hello" "WRONG"]
                                   {})])
  => [["@react/1234"] []]
  
  
  (notify/wait-on :js
    (impl-common/add-listener (-/CELL)
                              ["hello" "echo"]
                              "@react/1234"
                              (repl/>notify))
    (impl-common/trigger-listeners (-/CELL)
                                   ["hello" "echo"]
                                   {:data [1 2 3]
                                    :meta {:custom "hello"}}))
  => {"data" [1 2 3]
      "path" ["hello" "echo"],
      "meta"
      {"custom" "hello",
       "listener/id" "@react/1234",
       "listener/type" "cell"}})
