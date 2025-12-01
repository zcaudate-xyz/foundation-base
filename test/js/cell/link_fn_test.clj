(ns js.cell.link-fn-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [js.cell.playground :as browser]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt]
             [js.cell.base-internal :as internal]
             [js.cell.base-fn :as base-fn]
             [js.cell.link-raw :as link-raw]
             [js.cell.link-fn :as link-fn]
             [js.core :as j]]
   :import [["tiny-worker" :as Worker]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})


^{:refer js.cell.link-fn/tmpl-link-route :added "4.0" :unchecked true}
(fact "performs a template"
  ^:hidden
  
  (link-fn/tmpl-link-route
   '[trigger base-fn/fn-trigger])
  => '(defn.js trigger
        [link op topic status body]
        (return
         (js.cell.link-raw/call
          link
          {:op "route",
           :route "@/trigger",
           :body [op topic status body]}))))

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
      "topic" "hello"})

^{:refer js.cell.link-fn/trigger-async :adopt true :added "4.0" :unchecked true}
(fact  "triggers an event after delay"
  ^:hidden

  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (link-raw/add-callback link "test" "hello" (repl/>notify))
    (link-fn/trigger-async link "stream" "hello" "ok" "hello" 100))
  => {"body" "hello", "status" "ok", "op" "stream", "topic" "hello"})

^{:refer js.cell.link-fn/error :adopt true :added "4.0" :unchecked true}
(fact "throws an error"
  ^:hidden
  
  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/error link)
       (catch (repl/>notify))))
  => (contains-in {"body" ["error" integer?], "route" "@/error",
                   "status" "error", "op" "route"}))

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
                   "route" "@/error-async"
                   "status" "error", "op" "route"}))

^{:refer js.cell.link-fn/route-list :adopt true :added "4.0" :unchecked true}
(fact "gets the route list"
  ^:hidden
  
  (set (notify/wait-on :js
        (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
        (. (link-fn/route-list link)
           (then (repl/>notify)))))
  => #{"@/route-list"
       "@/route-entry"
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

^{:refer js.cell.link-fn/route-entry :adopt true :added "4.0" :unchecked true}
(fact "gets the route doc"
  ^:hidden

  (notify/wait-on :js
    (var link (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-fn/route-entry link "@/echo")
       (then (repl/>notify))))
  => {"args" ["arg"], "async" false})



