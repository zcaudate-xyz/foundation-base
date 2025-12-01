(ns js.cell.link-raw-test
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

(def +core+ (browser/play-worker))

^{:refer js.cell.link-raw/link-listener-call :added "4.0" :unchecked true}
(fact "resolves a call to the link"
  ^:hidden
  
  (link-raw/link-listener-call {:op "eval"
                                :id "hello"
                                :status "ok"
                                :body "1"}
                               {:hello {:resolve k/identity}})
  => 1

  (link-raw/link-listener-call {:op "eval"
                                :id "hello"
                                :status "ok"}
                               {:hello {:resolve k/identity
                                        :reject  k/identity}})
  => (contains {"message" "Format Invalid",
                "id" "hello",
                "status" "error",
                "op" "eval"})
  
  
  (link-raw/link-listener-call {:op "eval"
                                :id "hello"
                                :status "error"
                                :message "Server Error"}
                               {:hello {:resolve k/identity
                                        :reject  k/identity}})
  => (contains {"message" "Server Error",
                "id" "hello",
                "status" "error",
                "op" "eval"}))

^{:refer js.cell.link-raw/link-listener-event :added "4.0" :unchecked true}
(fact "notifies all registered callbacks"
  ^:hidden

  (!.js
   (link-raw/link-listener-event {:op    "stream"
                                  :topic "hello"}
                                 {:hello {:pred true
                                          :handler (fn:> true)}
                                  :world {:handler (fn:> true)}
                                  :again {:pred (fn:> [topic event] (not= topic "hello"))
                                          :handler (fn:> true)}}))
  => ["hello" "world"])

^{:refer js.cell.link-raw/link-listener :added "4.0" :unchecked true}
(fact "constructs a link listener"
  ^:hidden
  
  (link-raw/link-listener {:data {:op "eval"
                                  :id "hello"
                                  :status "ok"
                                  :body "1"}}
                          {:hello {:resolve k/identity}}
                          {})
  => 1)

^{:refer js.cell.link-raw/link-create-worker :added "4.0" :unchecked true}
(fact "helper function to create a worker")

^{:refer js.cell.link-raw/link-create :added "4.0" :unchecked true}
(fact "creates a link from url"
  ^:hidden

  ;;
  ;; Worker EVAL
  ;;
  
  (notify/wait-on :js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. link
       ["worker"]
       (postMessage (@! (l/with:input
                          (!.js (repl/notify true)))))))
  => true  
  
  
  ;;
  ;; Link EVAL
  ;;
  
  (notify/wait-on :js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-raw/call link {:op "eval"
                            :body "1+1"})
       (then (repl/>notify))))
  => 2
  
  ;;
  ;; Link EVAL ASYNC
  ;;
  
  (notify/wait-on :js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-raw/call link {:op "eval"
                            :id "id-async"
                            :async true
                            :body (@! (l/with:input
                                        (!.js (j/future-delayed [100]
                                                (postMessage {:op "eval"
                                                              :id "id-async"
                                                              :status "ok"
                                                              :body "1"})))))})
       (then  (repl/>notify))))
  => 1

  
  ;;
  ;; Link ROUTE
  ;;
  
  (notify/wait-on :js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-raw/call link {:op "route"
                            :route "@/ping-async"
                            :body [100]})
       (then (repl/>notify))))
  => (contains ["pong" integer?]))

^{:refer js.cell.link-raw/link-active :added "4.0" :unchecked true}
(fact "gets the calls that are active"
  ^:hidden
  
  (vals (!.js
        (var link (link-raw/link-create
                   (fn []
                     (eval (@! (browser/play-worker true))))))
        (link-raw/call link {:op "route"
                             :route "@/ping-async"
                             :body [100]})
        (link-raw/link-active link)))
  => (contains-in
      [{"input" {"body" [100], "route" "@/ping-async", "op" "route"}}]))

^{:refer js.cell.link-raw/add-callback :added "4.0" :unchecked true}
(fact "adds a callback to the link"
  ^:hidden
  
  (!.js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (link-raw/call link {:op "route"
                            :route "@/ping-async"
                         :body [100]})
    (. link ["active"]))
  
  => map?
  
  (notify/wait-on :js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-raw/call link {:op "route"
                            :route "@/ping-async"
                            :body [100]})
       (then (repl/>notify))))
  => (contains ["pong" integer?])

  (notify/wait-on :js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-raw/call link {:op "route"
                            :route "@/error"})
       (then (repl/>notify))
       (catch (repl/>notify))))
  => (contains-in
      {"body" ["error" integer?], "id" string?,
       "status" "error", "op" "route"
       "route" "@/error"})

  (notify/wait-on :js
    (var link (link-raw/link-create
               (fn []
                 (eval (@! (browser/play-worker true))))))
    (. (link-raw/call link {:op "route"
                            :route "@/error-async"
                            :body [100]})
       (catch (repl/>notify))))
  => (contains-in
      {"body" ["error" integer?], "id" string?,
       "status" "error", "op" "route"
       "route" "@/error-async"}))

^{:refer js.cell.link-raw/list-callbacks :added "4.0" :unchecked true}
(fact "lists all callbacks on the link")

^{:refer js.cell.link-raw/remove-callback :added "4.0" :unchecked true}
(fact "removes a callback on the link")

^{:refer js.cell.link-raw/call-id :added "4.0" :unchecked true}
(fact "gets the call id"
  ^:hidden
  
  (!.js
   (link-raw/call-id
    (link-raw/link-create
     (fn []
       (eval (@! (browser/play-worker true)))))))
  => string?)

^{:refer js.cell.link-raw/call :added "4.0" :unchecked true}
(fact "calls the link with an event"
  ^:hidden
  
  (j/<! (do:>
         (var link (link-raw/link-create
                    (fn []
                      (eval (@! (browser/play-worker true))))))
         (return (link-raw/call
                  link
                  {:op "eval",
                   :id "id-call"
                   :async true
                   :body (@!
                          (l/with:input
                            (!.js
                             (postMessage {:op "eval"
                                           :id "id-call"
                                           :status "ok"
                                           :body (JSON.stringify {:type "data"
                                                                  :value [1 2 3 4]})})
                             true)))}))))
  => [1 2 3 4])

^{:refer js.cell.link-raw/wait-post :added "4.0" :unchecked true}
(fact "posts code to worker"
  ^:hidden
  
  (link-raw/wait-post
   (. (link-raw/link-create 
       (fn []
         (eval (@! (browser/play-worker true)))))
      ["worker"])
   (repl/notify true))
  => true
  
  (link-raw/wait-post
      (. (link-raw/link-create 
          (fn []
            (eval (@! (browser/play-worker true)))))
         ["worker"])
    (@! (l/with:input
          (!.js
           (repl/notify true))))
    "eval"
    {}
    "id-route")
  => true)

^{:refer js.cell.link-raw/async-post :added "4.0" :unchecked true
  :setup [(!.js (:= (!:G LK) (link-raw/link-create 
               (fn []
                 (eval (@! (browser/play-worker true)))))))]}
(fact "helper for async post"
  ^:hidden

  (link-raw/wait-eval LK
    (link-raw/async-post [1 2 3 4])
    true)
  => [1 2 3 4]

  (link-raw/wait-eval LK
    (j/future-delayed [100]
      (link-raw/async-post [1 2 3 4]))
    true)
  => [1 2 3 4]
  
  (link-raw/wait-eval LK
    (j/future-delayed [100]
      (var f link-raw/async-post)
      (f [1 2 3 4]))
    true)
  => [1 2 3 4])

^{:refer js.cell.link-raw/post-eval :added "4.0" :unchecked true
  :setup [(!.js (:= (!:G LK)
                    (link-raw/link-create 
                     (fn []
                       (eval (@! (browser/play-worker true)))))))]}
(fact "posts to worker, works in conjuction with async-post"
  ^:hidden

  (j/<! (link-raw/post-eval LK
          (link-raw/async-post [1 2 3 4])))
  => [1 2 3 4]
  
  (notify/wait-on :js
    (var link (link-raw/link-create 
                 (fn []
                   (eval (@! (browser/play-worker true))))))
    (link-raw/add-callback link
                         "test"
                         "hello"
                         (repl/>notify))
    (link-raw/post-eval link
      (postMessage {:op "stream"
                    :topic "hello"
                    :status "ok"
                    :body {}})))
  => {"body" {}, "status" "ok", "op" "stream", "topic" "hello"})

^{:refer js.cell.link-raw/wait-eval :added "4.0" :unchecked true
  :setup [(!.js (:= (!:G LK) (link-raw/link-create 
                              (fn []
                                (eval (@! (browser/play-worker true)))))))]}
(fact "posts code to worker with eval"
  ^:hidden

  (!.js LK)
  (link-raw/wait-eval LK
    [1 2 3 4])
  => [1 2 3 4]

  (link-raw/wait-eval [LK 100]
    (link-raw/async-post [1 2 3 4])
    true)
  => [1 2 3 4]

  (link-raw/wait-eval [LK 100]
    [1 2 3 4]
    true)
  => :timeout)
