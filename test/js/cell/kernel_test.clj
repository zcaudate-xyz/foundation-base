(ns js.cell.kernel-test
  (:use code.test)
  (:require [js.cell.playground :as playground]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
              [xt.lang.common-repl :as repl]
              [xt.lang.common-runtime :as rt]
              [xt.lang.event-view :as base-view]
             [js.cell.kernel.worker-impl :as internal]
             [js.cell.kernel.worker-fn :as base-fn]
             [js.cell.kernel.base-link :as link-raw]
             [js.cell.kernel.base-link-local :as link-fn]
             [js.cell.kernel.base-impl :as impl-common]
             [js.cell.kernel.base-model :as impl-model]
             [js.cell.kernel :as cl]
             [js.core :as j]]
   :import [["tiny-worker" :as Worker]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel/make-cell :added "4.0" :unchecked true}
(fact "makes a current cell")

^{:refer js.cell.kernel/GD :added "4.0" :unchecked true
  :setup [(!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))]}
(fact "gets the current cell"
  ^:hidden
  
  (cl/GD)
  => map?)

^{:refer js.cell.kernel/GX :added "4.0" :unchecked true
  :setup [(!.js
           (cl/GX-set
            "p0"
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))]}
(fact "gets the current annex"
  ^:hidden
  
  (!.js
   (xt/x:obj-keys (cl/GX)))
  => ["p0"])

^{:refer js.cell.kernel/GX-val :added "4.0" :unchecked true}
(fact "gets the current annex key"
  ^:hidden
  
  (cl/GX-val "p0")
  => map?)

^{:refer js.cell.kernel/GX-set :added "4.0" :unchecked true}
(fact "set the current annex key"
  ^:hidden
  
  (!.js
   (cl/GX-set
    "p0"
    (cl/make-cell
     (fn []
       (eval (@! (playground/play-worker true)))))))
  => map?)

^{:refer js.cell.kernel/get-cell :added "4.0" :unchecked true}
(fact "gets the current cell"
  ^:hidden
  
  (cl/get-cell)
  => map?)

^{:refer js.cell.kernel/fn-call-cell :added "4.0" :unchecked true}
(fact "calls the cell in context"
  ^:hidden
  
  (cl/fn-call-cell j/identity [])
  => map?

  (cl/fn-call-cell j/identity [] "p0")
  => map?)

^{:refer js.cell.kernel/fn-call-model :added "4.0" :unchecked true
  :setup [(!.js
           (cl/add-model-attach "hello"
                                {:echo  {:handler link-fn/echo
                                         :defaultArgs ["HELLO"]}}))]}
(fact "calls the model in context"
  ^:hidden
  
  (cl/fn-call-model impl-common/model-get "hello" [])
  => map?)

^{:refer js.cell.kernel/fn-call-view :added "4.0" :unchecked true}
(fact "calls the view in context"
  ^:hidden
  
  (!.js
   (xt/x:second (cl/fn-call-view impl-common/view-ensure ["hello" "echo"] [])))
  => map?)

^{:refer js.cell.kernel/fn-access-cell :added "4.0" :unchecked true
  :setup [(!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))
          (j/<!
           (. (cl/add-model "hello"
                            {:echo  {:handler link-fn/echo
                                     :trigger {"@worker/::INIT" false}
                                     :defaultArgs ["HELLO"]}})
              ["init"]))]}
(fact "calls access function on the current cell"
  ^:hidden
  
  (!.js (cl/fn-access-cell base-view/get-current))
  => (contains-in
      {"hello" {"echo" ["HELLO" integer?]}}))

^{:refer js.cell.kernel/fn-access-model :added "4.0" :unchecked true}
(fact "calls access function on the current model"
  ^:hidden
  
  (!.js (cl/fn-access-model base-view/get-current "hello"))
  => (contains-in
      {"echo" ["HELLO" integer?]}))

^{:refer js.cell.kernel/fn-access-view :added "4.0" :unchecked true
  :setup [(!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))
          (j/<!
           (. (cl/add-model "hello"
                            {:echo  {:handler link-fn/echo
                                     :trigger {"@worker/::INIT" false}
                                     :defaultArgs ["HELLO"]}})
              ["init"]))]}
(fact "calls access function on the current view"
  ^:hidden
  
  (!.js (cl/fn-access-view base-view/get-current
                           ["hello" "echo"] []))
  => (contains ["HELLO" integer?]))

^{:refer js.cell.kernel/list-models :added "4.0" :unchecked true}
(fact "lists all models"
  ^:hidden
  
  (!.js
   (cl/list-models))
  => ["hello"])

^{:refer js.cell.kernel/list-views :added "4.0" :unchecked true}
(fact "lists all views"
  ^:hidden
  
  (!.js
   (cl/list-views "hello"))
  => ["echo"])

^{:refer js.cell.kernel/get-model :added "4.0" :unchecked true}
(fact "gets the model in context"
  ^:hidden
  
  (cl/get-model "hello")
  => map?)

^{:refer js.cell.kernel/get-view :added "4.0" :unchecked true}
(fact "gets the view in context"
  ^:hidden
  
  (cl/get-view ["hello" "echo"])
  => map?)

^{:refer js.cell.kernel/cell-vals :added "4.0" :unchecked true}
(fact "gets all vals in the context"
  ^:hidden
  
  (cl/cell-vals)
  => (contains-in {"hello" {"echo" ["HELLO" integer?]}})

  (cl/cell-inputs "p0")
  => {})

^{:refer js.cell.kernel/cell-outputs :added "4.0" :unchecked true}
(fact "gets all output data in the context"
  ^:hidden
  
  (cl/cell-outputs)
  => (contains-in
      {"hello" {"echo" {"current" ["HELLO" integer?],
                        "updated" integer?}}})
  
  (cl/cell-inputs "p0")
  => {})

^{:refer js.cell.kernel/cell-inputs :added "4.0" :unchecked true}
(fact "gets all output data in the context"
  ^:hidden
  
  (cl/cell-inputs)
  => (contains-in {"hello" {"echo" {"current" {"data" ["HELLO"]},
                                    "updated" integer?}}})

  (cl/cell-inputs "p0")
  => {})

^{:refer js.cell.kernel/cell-trigger :added "4.0" :unchecked true
  :setup [(!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))
          (j/<!
           (. (cl/GD)
              ["init"]
              (then (fn []
                      (cl/add-model-attach
                       "hello"
                       {:echo  {:handler link-fn/echo
                                :defaultArgs ["HELLO"]}})))))]}
(fact "triggers a view given event"
  ^:hidden
  
  (cl/view-val ["hello" "echo"])
  => nil

  (do (j/<! (xt/x:first (cl/view-trigger ["hello" "echo"]
                                        "@/::EVENT"
                                        {})))
      (cl/view-val ["hello" "echo"]))
  => (contains-in ["HELLO" integer?]))

^{:refer js.cell.kernel/model-outputs :added "4.0" :unchecked true}
(fact "gets the model outputs"
  ^:hidden
  
  (cl/model-outputs "hello")
  => (contains-in {"echo" {"current" ["HELLO" integer?],
                           "updated" integer?}}))

^{:refer js.cell.kernel/model-vals :added "4.0" :unchecked true}
(fact "gets model vals"
  ^:hidden
  
  (cl/model-vals "hello")
  => (contains-in {"echo" ["HELLO" integer?]}))

^{:refer js.cell.kernel/model-is-errored :added "4.0" :unchecked true}
(fact "checks if model has errored")

^{:refer js.cell.kernel/model-is-pending :added "4.0" :unchecked true}
(fact "checks if model is pending")

^{:refer js.cell.kernel/add-model-attach :added "4.0" :unchecked true
  :setup [(!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))]}
(fact "adds a model"
  ^:hidden
  
  (!.js 
   (cl/add-model-attach "hello0"
                        {:echo0  {:handler link-fn/echo
                                  :defaultArgs ["HELLO"]}})
   
   (cl/add-model-attach "hello1"
                        {:echo1  {:handler link-fn/echo
                                  :defaultArgs ["HELLO"]}}))
  => map?
  
  (!.js
   (cl/list-models))
  => ["hello0" "hello1"]

  (!.js
   (cl/remove-model "hello1")
   (cl/list-models))
  => ["hello0"])

^{:refer js.cell.kernel/add-model :added "4.0" :unchecked true
  :setup [(!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))]}
(fact "attaches a model"
  ^:hidden
  
  (j/<!
   (. (cl/add-model "hello"
                    {:echo  {:handler link-fn/echo
                             :defaultArgs ["HELLO"]}})
      ["init"]))
  => (contains-in
      [{"path" ["hello" "echo"],
        "post" [false],
        "main" [true ["HELLO" integer?]],
        "pre" [false],
        "::" "view.run"}]))

^{:refer js.cell.kernel/remove-model :added "4.0" :unchecked true}
(fact "removes a model from cell")

^{:refer js.cell.kernel/model-update :added "4.0" :unchecked true
  :setup [(!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))
          (j/<!
           (. (cl/GD)
              ["init"]
              (then (fn []
                      (cl/add-model-attach "hello"
                                   {:echo   {:handler link-fn/echo
                                             :defaultArgs ["HELLO"]}
                                    :echo1  {:handler link-fn/echo
                                             :defaultArgs ["HELLO1"]}})))))]}
(fact "calls update on a model"
  ^:hidden
  
  (cl/model-vals "hello")
  => {"echo1" nil, "echo" nil}

  (j/<! (cl/model-update "hello"))
  => (contains-in
      {"echo1"
       {"path" ["hello" "echo1"],
        
        "post" [false],
        "main" [true ["HELLO1" integer?]],
        "pre" [false],
        "::" "view.run"},
       "echo"
       {"path" ["hello" "echo"],
        
        "post" [false],
        "main" [true ["HELLO" integer?]],
        "pre" [false],
        "::" "view.run"}}))

^{:refer js.cell.kernel/model-trigger :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))
          (!.js
           (cl/add-model-attach "hello"
                                {:echo  {:handler  link-fn/echo
                                         :trigger  {"@/::HELLO" true}
                                         :defaultArgs ["HELLO1"]}}))]}
(fact "triggers an event on the model"
  ^:hidden

  (cl/model-trigger "hello" "@/::OTHER" {})
  => []
  
  (cl/model-trigger "hello" "@/::HELLO" {})
  => ["echo"])

^{:refer js.cell.kernel/view-success :added "4.0" :unchecked true}
(fact "gets the success value")

^{:refer js.cell.kernel/view-val :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))
          (!.js
           (cl/add-model-attach "hello"
                        {:echo  {:handler link-fn/echo
                                 :trigger {"@/::HELLO" true}
                                 :defaultArgs ["HELLO"]}}))]}
(fact "gets the view val"
  ^:hidden
  
  (cl/view-val ["hello" "echo"])
  => nil

  (do (cl/view-set-val ["hello" "echo"] 1)
      (cl/view-val ["hello" "echo"]))
  => 1)

^{:refer js.cell.kernel/view-get-input :added "4.0" :unchecked true}
(fact "gets the view input"
  ^:hidden

  (cl/view-get-input ["hello" "echo"])
  => map?)

^{:refer js.cell.kernel/view-get-output :added "4.0" :unchecked true}
(fact "gets the view output"
  ^:hidden
  
  (set (!.js
        (xt/x:obj-keys (cl/view-get-output ["hello" "echo"]))))
  =>  #{"current" "default" "elapsed" "process" "tag" "type" "updated"}

  (cl/view-get-output ["hello" "WRONG"])
  => nil)

^{:refer js.cell.kernel/view-set-val :added "4.0" :unchecked true}
(fact "sets the view val"
  ^:hidden
  
  (notify/wait-on :js
    (cl/add-listener ["hello" "echo"]
                     "oneshot"
                     (fn [event]
                       (repl/notify event)))
    (cl/view-set-val ["hello" "echo"] 1))
  => (contains-in
      {"path" ["hello" "echo"],
       "type" "view.output",
       "meta" {"listener/id" "oneshot", "listener/type" "cell"},
       "data" {"elapsed" nil, "current" 1, "updated" integer?}}))

^{:refer js.cell.kernel/view-get-time-updated :added "4.0" :unchecked true}
(fact "gets updated"
  ^:hidden
  
  (cl/view-get-time-updated ["hello" "echo"])
  => integer?)

^{:refer js.cell.kernel/view-is-errored :added "4.0" :unchecked true}
(fact "gets the errored flag for view")

^{:refer js.cell.kernel/view-is-pending :added "4.0" :unchecked true}
(fact "gets pending"
  ^:hidden
  
  (!.js
   (cl/view-is-pending ["hello" "echo"]))
  => false)

^{:refer js.cell.kernel/view-get-time-elapsed :added "4.0" :unchecked true
  :setup [(j/<!
           (cl/view-refresh ["hello" "echo"]))]}
(fact "gets the elapsed time"
  ^:hidden

  (!.js
   (cl/view-get-time-elapsed ["hello" "echo"]))
  => integer?)

^{:refer js.cell.kernel/view-set-input :added "4.0" :unchecked true}
(fact "sets the view input"
  ^:hidden
  
  (j/<! (xt/x:first (cl/view-set-input ["hello" "echo"]
                                     {:data ["WORLD"]})))
  => (contains-in
      {"::" "view.run"
       "path" ["hello" "echo"],
       
       "post" [false],
       "main" [true ["WORLD" integer?]],
       "pre" [false]}))

^{:refer js.cell.kernel/view-refresh :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))
          (!.js
           (cl/add-model-attach "hello"
                                {:echo  {:handler link-fn/echo
                                         :trigger {"@/::HELLO" true}
                                         :defaultArgs ["HELLO"]}}))]}
(fact "refreshes the view"
  ^:hidden
  
  (j/<! (cl/view-refresh ["hello" "echo"]))
  => (contains-in
      {"::" "view.run"
       "path" ["hello" "echo"],
       
       "post" [false],
       "main" [true ["HELLO" integer?]],
       "pre" [false]}))

^{:refer js.cell.kernel/view-update :added "4.0" :unchecked true}
(fact "updates the view"
  ^:hidden
  
  (j/<! (xt/x:first (cl/view-update ["hello" "echo"])))
  => (contains-in
      {"::" "view.run"
       "path" ["hello" "echo"],
       "post" [false],
       "main" [true ["HELLO" integer?]],
       "pre" [false]}))

^{:refer js.cell.kernel/view-ensure :added "4.0" :unchecked true}
(fact "ensures view"
  ^:hidden

  (!.js (cl/view-ensure ["hello" "echo"]))
  => (contains [map? map?]))

^{:refer js.cell.kernel/view-call-remote :added "4.0" :unchecked true}
(fact "calls the remote function"
  ^:hidden
  
  (j/<! (cl/view-call-remote ["hello" "echo"]
                              1
                              false))
  =>  {"::" "view.run",
       "path" ["hello" "echo"],
       "post" [false],
       "pre" [false],
       "remote" [false]})

^{:refer js.cell.kernel/view-refresh-remote :added "4.0" :unchecked true}
(fact "refreshes the remote function"
  ^:hidden
  
  (j/<! (cl/view-refresh-remote ["hello" "echo"]))
  => {"::" "view.run",
      "path" ["hello" "echo"],
      "post" [false],
      "pre" [false],
      "remote" [false]})

^{:refer js.cell.kernel/view-trigger :added "4.0" :unchecked true}
(fact "triggers the view with an event"
  ^:hidden
  
  (j/<!
   (xt/x:first (cl/view-trigger ["hello" "echo"]
                              "@/::HELLO"
                              {})))
  => (contains-in
      {"path" ["hello" "echo"],
       "post" [false],
       "main" [true ["HELLO" integer?]],
       "pre" [false],
       "::" "view.run"}))

^{:refer js.cell.kernel/view-for :added "4.0" :unchecked true}
(fact "gets the view after update"
  ^:hidden
  
  (j/<! (cl/view-for ["hello" "echo"]))
  => (contains-in ["HELLO" integer?]))

^{:refer js.cell.kernel/view-for-input :added "4.0" :unchecked true}
(fact "gets the view after setting input"
  ^:hidden
  
  (j/<! (cl/view-for-input ["hello" "echo"] {}))
  => (contains-in ["HELLO" integer?]))

^{:refer js.cell.kernel/get-val :added "4.0" :unchecked true}
(fact "gets the subview"
  ^:hidden
  
  (first (cl/get-val ["hello" "echo"]))
  => "HELLO")

^{:refer js.cell.kernel/get-for :added "4.0" :unchecked true}
(fact "gets the subview after update"
  ^:hidden
  
  (j/<! (cl/get-for ["hello" "echo"]
                    [0]))
  => "HELLO")

^{:refer js.cell.kernel/nil-view :added "4.0" :unchecked true}
(fact "sets view input to nil")

^{:refer js.cell.kernel/nil-model :added "4.0" :unchecked true}
(fact "sets all model inputs to nil")

^{:refer js.cell.kernel/clear-listeners :added "4.0" :unchecked true}
(fact "clears all listeners")

^{:refer js.cell.kernel/add-listener :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))
          (j/<!
           (. (cl/add-model "hello"
                            {:echo  {:handler link-fn/echo
                                     :trigger {"@/::HELLO" true}
                                     :defaultArgs ["HELLO"]}})
              ["init"]))]}
(fact "adds a cell listener"
  ^:hidden
  
  (notify/wait-on :js
    (cl/add-listener ["hello" "echo"]
                     "@react/1234"
                     (fn [event]
                       (var #{type} event)
                       
                       (when (== "view.output" type)
                         (repl/notify event))))
    (cl/view-update ["hello" "echo"]))
  => (contains-in
      {"path" ["hello" "echo"],
       "type" "view.output",
       "meta" {"listener/id" "@react/1234", "listener/type" "cell"},
       "data"
       {"current" ["HELLO" integer?],
        "updated" integer?
        "pending" true}})  

  (!.js
   (cl/list-listeners ["hello" "echo"]))
  => ["@react/1234"]

  (!.js
   (cl/list-all-listeners))
  => {"hello" {"echo" ["@react/1234"]}}

  (!.js
   (cl/remove-listener ["hello" "echo"] "@react/1234"))
  => (contains-in
      {"meta" {"listener/id" "@react/1234", "listener/type" "cell"}}))

^{:refer js.cell.kernel/remove-listener :added "4.0" :unchecked true}
(fact "removes a listener")

^{:refer js.cell.kernel/list-listeners :added "4.0" :unchecked true}
(fact "lists view listeners")

^{:refer js.cell.kernel/list-all-listeners :added "4.0" :unchecked true}
(fact "lists all listeners")

^{:refer js.cell.kernel/add-raw-callback :added "4.0" :unchecked true
  :setup [(fact:global :setup)
          (!.js
           (cl/GD-reset
            (cl/make-cell
             (fn []
               (eval (@! (playground/play-worker true)))))))
          (Thread/sleep 100)]}
(fact "adds a raw callback (for all events)"
  ^:hidden
  
  (notify/wait-on :js
    (cl/add-raw-callback "@/TEST"
                         true
                         (repl/>notify)
                         nil)
    (link-fn/trigger (. (cl/GD) ["link"])
                     "stream"
                     "hello"
                     "ok"
                     {:data 123}))
  => {"body" {"data" 123}, "status" "ok", "op" "stream", "topic" "hello"}

  (set (cl/list-raw-callbacks))
  => #{"@/raw" "@/TEST"}

  (cl/remove-raw-callback "@/TEST")
  => vector?)

^{:refer js.cell.kernel/remove-raw-callback :added "4.0" :unchecked true}
(fact "removes a raw callback")

^{:refer js.cell.kernel/list-raw-callbacks :added "4.0" :unchecked true}
(fact "lists all raw calllbacks")


^{:refer js.cell.kernel/call :added "4.1"}
(fact "TODO")
