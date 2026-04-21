(ns xt.cell.kernel.base-link-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [js.core :as j]
             [xt.cell.kernel.base-link :as base-link]]})

(fact:global
  {:setup    [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
   :teardown [(l/rt:stop)]})

(defn.js make-link
  [handler]
  (return
   (base-link/link-create
    {:create-fn
     (fn:> [listener]
       {"::" "worker.fake"
        "postRequest"
        (fn [input]
          (return (handler listener input)))})})))

^{:refer xt.cell.kernel.base-link/link-listener-call :added "4.1"}
(fact "resolves and rejects active calls from worker responses"

  (!.js
   (var out {})
   (var active
        {"call-1"
         {:time 10
          :input {"action" "@worker/echo"
                  "body" ["hello"]}
          :resolve (fn [x] (xtd/obj-assign out {"ok" x}))
          :reject (fn [x] (xtd/obj-assign out {"err" x}))}})
   (base-link/link-listener-call
    {"op" "call"
     "id" "call-1"
     "status" "ok"
     "body" ["hello" 1]}
    active)
   [(. out ["ok"])
    (xtd/obj-keys active)])
  => [["hello" 1] []]

  (!.js
   (var out {})
   (var active
        {"call-2"
         {:time 10
          :input {"action" "@worker/error"
                  "body" []}
          :resolve (fn [x] (xtd/obj-assign out {"ok" x}))
          :reject (fn [x] (xtd/obj-assign out {"err" x}))}})
   (base-link/link-listener-call
    {"op" "call"
     "id" "call-2"
     "status" "error"
     "body" ["boom" 1]
     "action" "@worker/error"}
    active)
   (xt/x:get-key out "err"))
  => (contains {"status" "error"
                "action" "@worker/error"
                "body" ["boom" 1]})

  (!.js
   (var active
        {"call-2"
         {:time 10
          :input {"action" "@worker/error"
                  "body" []}
          :resolve (fn [x] x)
          :reject (fn [x] x)}})
   (base-link/link-listener-call
    {"op" "call"
     "id" "call-2"
     "status" "error"
     "body" ["boom" 1]
     "action" "@worker/error"}
    active)
   (xtd/obj-keys active))
  => [])

^{:refer xt.cell.kernel.base-link/link-listener-event :added "4.1"}
(fact "dispatches stream events to matching callbacks"

  (!.js
   (var seen [])
   (var callbacks
        {"cb-1" {:pred "hello"
                 :handler (fn [data signal]
                            (seen.push signal))}
         "cb-2" {:pred "ignore"
                 :handler (fn [data signal]
                            (seen.push "wrong"))}})
   [(base-link/link-listener-event
     {"op" "stream"
      "signal" "hello"
      "status" "ok"
      "body" {"id" 1}}
     callbacks)
    seen])
  => [["cb-1"] ["hello"]])

^{:refer xt.cell.kernel.base-link/link-listener :added "4.1"}
(fact "routes call and stream frames to the right handler path"

  (!.js
   (var out {})
   (var active
        {"call-1"
         {:time 10
          :input {"action" "@worker/echo"
                  "body" ["hello"]}
          :resolve (fn [x] (xtd/obj-assign out {"ok" x}))
          :reject (fn [x] x)}})
   (var callbacks
        {"cb-1" {:pred "hello"
                 :handler (fn [data signal]
                            (xtd/obj-assign out {"signal" signal}))}})
   [(base-link/link-listener
     {"data" {"op" "call"
              "id" "call-1"
              "status" "ok"
              "body" ["hello" 1]}}
     active
     callbacks)
    (base-link/link-listener
     {"data" {"op" "stream"
              "signal" "hello"
              "status" "ok"
              "body" {"id" 1}}}
     active
     callbacks)
    out])
  => [nil
      ["cb-1"]
      {"ok" ["hello" 1]
       "signal" "hello"}])

^{:refer xt.cell.kernel.base-link/link-create-worker :added "4.1"}
(fact "creates workers from functions, create_fn maps, or direct worker objects"

  (!.js
   [(xt/x:get-key
     (base-link/link-create-worker
      (fn [listener]
        (return {"postRequest" (fn [input] (return nil))}))
      {}
      {})
     "::")
    (xt/x:get-key
     (base-link/link-create-worker
      {"create_fn"
       (fn [listener]
         (return {"postRequest" (fn [input] (return nil))}))}
      {}
      {})
     "::")
    (xt/x:is-function?
     (xt/x:get-key
      (base-link/link-create-worker
       {"postRequest" (fn [input] input)}
       {}
       {})
      "postRequest"))])
  => [nil nil true])

^{:refer xt.cell.kernel.base-link/link-post :added "4.1"}
(fact "posts requests through supported worker transport methods"

  (!.js
   [(base-link/link-post
     {"postRequest" (fn [input] (return {"kind" "request"
                                         "body" input}))}
     {"id" "req-1"})
    (base-link/link-post
     {"postMessage" (fn [input] (return {"kind" "message"
                                         "body" input}))}
     {"id" "req-2"})])
  => [{"kind" "request"
       "body" {"id" "req-1"}}
      {"kind" "message"
       "body" {"id" "req-2"}}])

^{:refer xt.cell.kernel.base-link/link-create :added "4.1"}
(fact "creates a link record with worker, active, and callback state"

  (!.js
   (var link
        (base-link/link-create
         {"postRequest" (fn [input] input)}))
   [(. link ["::"])
    (xt/x:is-function? (. link ["worker"] ["postRequest"]))
    (. link ["active"])
    (. link ["callbacks"])])
  => ["cell.link" true {} {}])

^{:refer xt.cell.kernel.base-link/link-active :added "4.1"}
(fact "returns the active call map of a link"

  (!.js
   (base-link/link-active
    {"active" {"call-1" {"time" 10}}}))
  => {"call-1" {"time" 10}})

^{:refer xt.cell.kernel.base-link/add-callback :added "4.1"}
(fact "adds callbacks onto a link"

  (!.js
   (var link {"callbacks" {}})
   [(base-link/add-callback link "cb-1" "hello" (fn:>))
    (xt/x:is-function? (. link ["callbacks"] ["cb-1"] ["handler"]))
    (. link ["callbacks"] ["cb-1"] ["key"])
    (. link ["callbacks"] ["cb-1"] ["pred"])])
  => [[nil]
      true
      "cb-1"
      "hello"])

^{:refer xt.cell.kernel.base-link/list-callbacks :added "4.1"}
(fact "lists registered callback ids"

  (!.js
   (base-link/list-callbacks
    {"callbacks" {"cb-1" {}
                  "cb-2" {}}}))
  => ["cb-1" "cb-2"])

^{:refer xt.cell.kernel.base-link/remove-callback :added "4.1"}
(fact "removes callbacks from a link"

  (!.js
   (var link {"callbacks" {"cb-1" {"key" "cb-1"}}})
   [(base-link/remove-callback link "cb-1")
    (base-link/list-callbacks link)])
  => [[{"key" "cb-1"}]
      []])

^{:refer xt.cell.kernel.base-link/call-id :added "4.1"}
(fact "generates unique call ids using the link id as a prefix"

  (!.js
   (xt/x:len
    (base-link/call-id
     {"id" "ln"
      "active" {}})))
  => 6)

^{:refer xt.cell.kernel.base-link/call :added "4.1"}
(fact "performs a round-trip call against the worker transport"

  (notify/wait-on :js
    (var link
         (-/make-link
          (fn [listener input]
            (listener {"op" "call"
                       "id" input.id
                       "status" "ok"
                       "body" input.body}))))
    (. (base-link/call
        link
        {"op" "call"
         "action" "@worker/echo"
         "body" ["hello" 1]})
       (then (repl/>notify))))
  => ["hello" 1])
