(ns js.cell.link-fn-test
  (:require [js.cell.playground :as browser]
            [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt]
             [js.cell.kernel.worker-impl :as internal]
             [js.cell.kernel.worker-fn :as base-fn]
             [js.cell.link-raw :as link-raw]
             [js.cell.link-fn :as link-fn]
             [js.core :as j]]
   :import [["tiny-worker" :as Worker]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})


^{:refer js.cell.link-fn/tmpl-link-action :added "4.0" :unchecked true}
(fact "performs a template"
  ^:hidden
  
  (link-fn/tmpl-link-action
   '[trigger base-fn/fn-trigger])
  => '(defn.js trigger
        [link op signal status body]
        (return
         (js.cell.link-raw/call
          link
          {:op "action",
           :action "@/trigger",
           :body [op signal status body]}))))

^{:refer js.cell.link-fn/ping :adopt true :added "4.0" :unchecked true}
(fact "performs link ping"
  ^:hidden
  
  (notify/wait-on :js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/ping link)
       (then (repl/>notify))))
  => (contains ["pong" integer?]))

^{:refer js.cell.link-fn/ping-async :adopt true :added "4.0" :unchecked true}
(fact "performs link async ping"
  ^:hidden

  (notify/wait-on :js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/ping-async link 100)
       (then (repl/>notify))))
  => (contains ["pong" integer?]))

^{:refer js.cell.link-fn/echo :adopt true :added "4.0" :unchecked true}
(fact "performs link echo"
  ^:hidden
  
  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/echo link ["hello"])
       (then (repl/>notify))))
  => (contains [["hello"] integer?])

  ;;
  ;; PASS FN
  ;;
  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/echo link k/identity)
       (then (fn [e]
               (repl/notify ((k/first e) "hello"))))))
  => "hello")

^{:refer js.cell.link-fn/echo-async :adopt true :added "4.0" :unchecked true}
(fact  "performs link async echo"
  ^:hidden

  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/echo-async link ["hello"] 100)
       (then (repl/>notify))))
  => (contains [["hello"] integer?])

  ;;
  ;; PASS FN
  ;;
  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/echo-async link k/identity 100)
       (then (fn [e]
               (repl/notify ((k/first e) "hello"))))))
  => "hello")

^{:refer js.cell.link-fn/trigger :adopt true :added "4.0" :unchecked true}
(fact "triggers an event"
  ^:hidden
  
  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (link-raw/add-callback link "test" "hello" (repl/>notify))
    (link-fn/trigger link "stream" "hello" "ok" "hello"))
  => {"body" "hello",
      "status" "ok",
      "op" "stream",
      "signal" "hello"})

^{:refer js.cell.link-fn/trigger-async :adopt true :added "4.0" :unchecked true}
(fact  "triggers an event after delay"
  ^:hidden

  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (link-raw/add-callback link "test" "hello" (repl/>notify))
    (link-fn/trigger-async link "stream" "hello" "ok" "hello" 100))
  => {"body" "hello", "status" "ok", "op" "stream", "signal" "hello"})

^{:refer js.cell.link-fn/error :adopt true :added "4.0" :unchecked true}
(fact "throws an error"
  ^:hidden
  
  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/error link)
       (catch (repl/>notify))))
  => (contains-in {"body" ["error" integer?], "action" "@/error",
                   "status" "error", "op" "action"}))

^{:refer js.cell.link-fn/error-async :adopt true :added "4.0" :unchecked true}
(fact "throws a error on delay"
  ^:hidden
  
  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/error-async link 100)
       (catch (repl/>notify))))
  => (contains-in {"body" ["error" integer?],
                   "action" "@/error-async"
                   "status" "error", "op" "action"}))

^{:refer js.cell.link-fn/action-list :adopt true :added "4.0" :unchecked true}
(fact "gets the action list"
  ^:hidden
  
  (set (notify/wait-on :js
        (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
        (. (link-fn/action-list link)
           (then (repl/>notify)))))
  => #{"@/action-list"
       "@/action-entry"
       "@/error-async"
       "@/echo"
       "@/ping"
       "@/eval-enable"
       "@/error"
       "@/final-set"
       "@/echo-async"
       "@/final-status"
       "@/trigger"
       "@/ping-async"
       "@/trigger-async"
       "@/eval-disable"
       "@/eval-status"})

^{:refer js.cell.link-fn/action-entry :adopt true :added "4.0" :unchecked true}
(fact "gets the action doc"
  ^:hidden

  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/action-entry link "@/echo")
       (then (repl/>notify))))
  => {"args" ["arg"], "async" false})



