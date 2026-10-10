(ns js.react.ext-model-test
  (:require [lang.core :as l]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-notify :as notify]
             [xt.event.base-model :as event-model]
             [js.react :as r]
             [js.react.helper-jsdom :as helper]
             [js.react.ext-model :as ext-model]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defn.js make-test-view
  [handler pipeline args output]
  (var view (event-model/create-model handler
                                      (or pipeline {})
                                      args
                                      output
                                      nil
                                      nil))
  (event-model/init-model view)
  (return view))

^{:refer js.react.ext-model/throttled-setter :added "4.0"}
(fact "updates immediately and coalesces delayed values"

  (notify/wait-on :js
    (var results [])
    (var [setThrottled throttle]
         (ext-model/throttled-setter
          (fn [value] (. results (push value)))
          20))
    (setThrottled 1)
    (setThrottled 2)
    (setTimeout (fn [] (repl/notify {"results" results
                                     "mounted" (. throttle ["mounted"])})) 50)
    nil)
  => {"results" [1 2] "mounted" true})

^{:refer js.react.ext-model/refresh-view :added "4.0"}
(fact "refreshes the main pipeline"

  (notify/wait-on :js
    (var view (-/make-test-view
               (fn [x] (return {"value" x}))
               {}
               [3]
               {"value" 0}))
    (. (ext-model/refresh-view view {})
       (then (fn [acc]
               (repl/notify {"main" (. acc ["main"])
                             "output" (event-model/get-current view nil)})))))
  => {"main" [true {"value" 3}] "output" {"value" 3}})

^{:refer js.react.ext-model/refresh-args :added "4.0"}
(fact "sets new args before refreshing"
  
  (notify/wait-on :js
    (var view (-/make-test-view
               (fn [x] (return {"value" x}))
               {}
               [3]
               {"value" 0}))
    (. (ext-model/refresh-args view [10] {})
       (then (fn [_]
               (repl/notify {"current" (. (event-model/get-input view) ["current"])})))))
  => {"current" {"data" [10]}})

^{:refer js.react.ext-model/refresh-view-remote :added "4.0"}
(fact "runs the remote pipeline"
  (notify/wait-on :js
    (var view (-/make-test-view
               (fn [x] (return {"main" x}))
               {"remote" {"handler" (fn [x] (return {"remote" true}))}}
               []
               nil))
    (. (ext-model/refresh-view-remote view true {})
       (then (fn [_]
               (repl/notify (event-model/get-current view "remote"))))))
  => {"remote" true})
^{:refer js.react.ext-model/refresh-args-remote :added "4.0"}
(fact "sets args and runs the remote pipeline"
  (notify/wait-on :js
    (var view (-/make-test-view
               (fn [x] (return {"main" x}))
               {"remote" {"handler" (fn [x] (return {"remote" x}))}}
               []
               nil))
    (. (ext-model/refresh-args-remote view [7] true {})
       (then (fn [_]
               (repl/notify {"args" (. (event-model/get-input view) ["current"])
                             "remote" (event-model/get-current view "remote")})))))
  => {"args" {"data" [7]} "remote" {"remote" 7}})
^{:refer js.react.ext-model/refresh-view-sync :added "4.0"}
(fact "runs the sync pipeline"
  (notify/wait-on :js
    (var view (-/make-test-view
               (fn [x] (return {"main" x}))
               {"sync" {"handler" (fn [x] (return {"sync" true}))}}
               []
               nil))
    (. (ext-model/refresh-view-sync view true {})
       (then (fn [_]
               (repl/notify (event-model/get-current view "sync"))))))
  => {"sync" true})
^{:refer js.react.ext-model/refresh-args-sync :added "4.0"}
(fact "sets args and runs the sync pipeline"
  (notify/wait-on :js
    (var view (-/make-test-view
               (fn [x] (return {"main" x}))
               {"sync" {"handler" (fn [x] (return {"sync" x}))}}
               []
               nil))
    (. (ext-model/refresh-args-sync view [8] true {})
       (then (fn [_]
               (repl/notify {"args" (. (event-model/get-input view) ["current"])
                             "sync" (event-model/get-current view "sync")})))))
  => {"args" {"data" [8]} "sync" {"sync" 8}})
^{:refer js.react.ext-model/make-view :added "4.0"}
(fact "creates an initialised view with an init refresh"
  (notify/wait-on :js
    (var view (ext-model/make-view
               (fn [x] (return {"value" x}))
               {}
               [3]
               {"value" 0}))
    (. (. view ["init"])
       (then (fn [_]
               (repl/notify {"type" (. view ["::"])
                             "input" (. (event-model/get-input view) ["current"])})))))
  => {"type" "event.model" "input" {"data" [3]}})
^{:refer js.react.ext-model/makeViewRaw :added "4.0"}
(fact "creates a raw view inside a component"
  (helper-source/test
   (fn [props]
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"view" (ext-model/makeViewRaw {"handler" (fn [x] (return x))
                                             "defaultArgs" [1]
                                             "defaultOutput" nil})}))
   (fn [props document _]
     (var result (== "event.model" (. (. props ["view"]) ["::"])))
     (return result)))
  => true)
^{:refer js.react.ext-model/makeView :added "4.0"}
(fact "creates a React stable view"
  (helper-source/test
   (fn [props]
     (var view (ext-model/makeView {"handler" (fn [x] (return x))
                                    "defaultArgs" [1]
                                    "defaultOutput" nil}))
     (xt/x:set-key (. props ["state"]) "view" view)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var result (xt/x:is-object? (. (. props ["state"]) ["view"])))
     (return result)))
  => true)
^{:refer js.react.ext-model/initViewBase :added "4.0"}
(fact "registers a view listener and returns teardown"
  (helper-source/test
   (fn [props]
     (var view (. props ["view"]))
     (var [value setValue] (r/local nil))
     (var ref (r/useFollowRef value))
     (var cleanup (ext-model/initViewBase
                   view nil
                   {"setResult" setValue
                    "getResult" (fn [] (return value))
                    "resultRef" ref}))
     (xt/x:set-key (. props ["state"]) "cleanup" cleanup)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"view" (-/make-test-view (fn [x] (return x)) {} [] nil)
              "state" {}}))
   (fn [props document _]
     (var state (. props ["state"]))
     (var result {"listener" (> (xt/x:len (event-model/list-listeners (. props ["view"]))) 0)
                  "cleanup" (xt/x:is-function? (. state ["cleanup"]))})
     (return result)))
  => {"listener" true "cleanup" true})
^{:refer js.react.ext-model/listenView :added "4.0"}
(fact "listens to the current view output"
  (helper-source/test
   (fn [props]
     (var view (. props ["view"]))
     (var value (ext-model/listenView view "output" nil nil nil))
     (return (r/createElement "span" nil
                              (JSON.stringify (or value {})))))
   (fn [_]
     (return {"view" (-/make-test-view (fn [x] (return x)) {} [] nil)}))
   (fn [props document _]
     (var view (. props ["view"]))
     (var task
          (r/act
           (fn []
             (event-model/set-output view {"value" 2} false "output" nil nil)
             (event-model/trigger-listeners view "view.output" {"type" "output"}))))
     (return
      (. (Promise.resolve task)
         (then (fn [_]
                 (return
                  (helper/await-dom
                   (fn []
                     (var result (JSON.parse document.body.textContent))
                     (return result))))))))))
  => {"value" 2})
^{:refer js.react.ext-model/listenViewOutput :added "4.0"}
(fact "listens to selected output events"
  (helper-source/test
   (fn [props]
     (var view (. props ["view"]))
     (var value (ext-model/listenViewOutput view ["output"] nil nil nil))
     (return (r/createElement "span" nil
                              (JSON.stringify (or value {})))))
   (fn [_]
     (return {"view" (-/make-test-view (fn [x] (return x)) {} [] nil)}))
   (fn [props document _]
     (var view (. props ["view"]))
     (var task
          (r/act
           (fn []
             (event-model/trigger-listeners view "view.output" {"type" "output"}))))
     (return
      (. (Promise.resolve task)
         (then (fn [_]
                 (return
                  (helper/await-dom
                   (fn []
                     (var value (JSON.parse document.body.textContent))
                     (var result {"type" (. value ["type"])
                                  "current" (. value ["current"])})
                     (return result))))))))))
  => {"type" "output" "current" nil})
^{:refer js.react.ext-model/listenViewThrottled :added "4.0"}
(fact "returns a throttled successful output listener"
  (helper-source/test
   (fn [props]
     (var view (. props ["view"]))
     (var value (ext-model/listenViewThrottled view 10 nil nil))
     (return (r/createElement "span" nil
                              (JSON.stringify (or value {})))))
   (fn [_]
     (return {"view" (-/make-test-view (fn [x] (return x)) {} [] nil)}))
   (fn [props document _]
     (var view (. props ["view"]))
     (var task
          (r/act
           (fn []
             (event-model/set-output view {"ok" true} false "output" nil nil)
             (event-model/trigger-listeners view "view.output" {"type" "output"}))))
     (return
      (new Promise
           (fn [resolve]
             (setTimeout
              (fn []
                (var result document.body.textContent)
                (resolve result))
              30))))))
  => "{\"ok\":true}")
^{:refer js.react.ext-model/wrap-pending :added "4.0"}
(fact "sets pending while a wrapped function is running"
  (notify/wait-on :js
    (var view (-/make-test-view (fn [x] (return x)) {} [] nil))
    (var wrapped (ext-model/wrap-pending
                  (fn [model value]
                    (return (. (Promise.resolve value)
                               (then (fn [v] (return (+ v 1)))))))
                  nil))
    (. (wrapped view 4)
       (then (fn [value]
               (repl/notify {"pending" (event-model/is-pending view nil)
                             "value" value})))))
  => {"pending" false "value" 5})
^{:refer js.react.ext-model/refreshArgsFn :added "4.0"}
(fact "refreshes args through the selected pipeline"
  (notify/wait-on :js
    (var view (-/make-test-view
               (fn [x] (return {"value" x}))
               {}
               [1]
               nil))
    (. (ext-model/refreshArgsFn view [9] {"remote" "none"})
       (then (fn [_]
               (repl/notify (event-model/get-current view nil))))))
  => {"value" 9})
^{:refer js.react.ext-model/useRefreshArgs :added "4.0"}
(fact "watches React args and starts a refresh"
  (helper-source/test
   (fn [props]
     (var view (. props ["view"]))
     (var result (ext-model/useRefreshArgs view [2] {"remote" "none"}))
     (xt/x:set-key (. props ["state"]) "result" result)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"view" (-/make-test-view (fn [x] (return {"value" x})) {} [2] nil)
              "state" {}}))
   (fn [props document _]
     (return
      (helper/await-dom
       (fn []
         (var result {"args" (. (event-model/get-input (. props ["view"])) ["current"])
                      "result" (xt/x:nil? (. (. props ["state"]) ["result"]))})
         (return result))))))
  => {"args" {"data" [2]} "result" true})
^{:refer js.react.ext-model/listenSuccess :added "4.0"}
(fact "combines a success listener with argument refresh"
  (helper-source/test
   (fn [props]
     (var view (. props ["view"]))
     (var result (ext-model/listenSuccess view [3] {"remote" "none"
                                                    "default" {"fallback" true}}
                                          nil nil))
     (return (r/createElement "span" nil
                              (JSON.stringify (or result {})))))
   (fn [_]
     (return {"view" (-/make-test-view (fn [x] (return {"value" x})) {} [3] {"fallback" true})}))
   (fn [props document _]
     (return
      (helper/await-dom
       (fn []
         (var result (JSON.parse document.body.textContent))
         (return result))))))
  => {"fallback" true})
^{:refer js.react.ext-model/handler-base :added "0.1"}
(fact "constructs a handler base"
  (!.js
    (var out (ext-model/handler-base (fn [] 1) {"label" "demo"}))
    (return {"args" (== (xt/x:json-encode (. out ["defaultArgs"])) "[]")
             "init" (== (. out ["defaultInit"] ["disabled"]) true)
             "label" (== (. out ["label"]) "demo")}))
  => {"args" true "init" true "label" true})
^{:refer js.react.ext-model/oneshot-fn :added "0.1"}
(fact "allows only the first call"
  (!.js
    (var f (ext-model/oneshot-fn))
    (return [(f) (f) (f)]))
  => [true false false])
^{:refer js.react.ext-model/input-disabled? :added "0.1"}
(fact "checks disabled or missing input"
  (!.js
    (return [(ext-model/input-disabled? {})
             (ext-model/input-disabled? {"input" {"disabled" true}})
             (ext-model/input-disabled? {"input" {"data" [1]}})]))
  => [true true false])
^{:refer js.react.ext-model/input-data :added "0.1"}
(fact "gets input data"
  (!.js
    (return [(ext-model/input-data {})
             (ext-model/input-data {"input" {"data" 1}})]))

  => [nil 1])
^{:refer js.react.ext-model/input-data-nil? :added "0.1"}
(fact "checks missing or disabled input data"
  (!.js
    (return [(ext-model/input-data-nil? {})
             (ext-model/input-data-nil? {"input" {"data" 1}})
             (ext-model/input-data-nil? {"input" {"data" nil}})]))
  => [true false true])
^{:refer js.react.ext-model/output-empty? :added "0.1"}
(fact "checks empty current output"
  (!.js
    (return [(ext-model/output-empty? {"view" {"output" {"current" nil}}})
             (ext-model/output-empty? {"view" {"output" {"current" []}}})
             (ext-model/output-empty? {"view" {"output" {"current" [1]}}})]))
  => [true true false])
