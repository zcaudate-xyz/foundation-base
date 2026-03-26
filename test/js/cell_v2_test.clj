(ns js.cell-v2-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [js.cell-v2 :as cell-v2]
             [js.cell-v2.event :as event]
             [js.cell-v2.protocol :as protocol]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2/make-system :added "4.0" :unchecked true}
(fact "creates a composed v2 system"
  (!.js
   (var system (cell-v2/make-system {:defaultStore "cache"}))
   (var seen [])
   (cell-v2/add-signal-listener
    system
    "all-events"
    true
    (fn [input topic bus]
      (x:arr-push seen [topic
                        (k/get-key input "status")
                        (k/get-key input "body")])
      (k/set-key (. bus ["owner"] ["state"]) "last-signal" topic)
      (k/set-key (. bus ["owner"] ["state"]) "last-group" (. input ["group"]))))
    (cell-v2/register-route
     system
     "local/echo"
    (fn [ctx arg]
      (return [(. ctx ["tag"]) arg]))
    {:kind "query"})
    (cell-v2/register-store
     system
     "cache"
     {:read (fn [input]
              (return ["cache" (. input ["id"])]))
      :write (fn [input]
               (return ["cache-write" (. input ["id"])]))
      :sync (fn [input]
              (return (. input ["payload"])))
      :remove (fn [input]
                (return (. input ["ids"])))
      :clear (fn []
               (return "cache-cleared"))})
    (cell-v2/register-remote
     system
     "http"
     {:call (fn [input]
              (return {:status "ok"
                       :body {:url (. input ["url"])
                              :method (. input ["method"])}
                       :events [{:signal event/EV_LOCAL
                                 :status "ok"
                                 :body {:action "after-remote"}}]
                       :store {:sync {:payload {"User" ["u1"]}}
                               :remove {:ids ["s1"]}}}))})
   (cell-v2/emit-signal system
                        event/EV_REMOTE
                        {:request-id "r1"}
                        nil
                        "ok")
   {:listeners (cell-v2/list-event-listeners system)
    :event (. system ["state"])
    :route (cell-v2/dispatch-route system "local/echo" ["hello"] {:tag "ctx"})
    :store (cell-v2/store-read system "cache" {:id "A"})
    :action-route (cell-v2/call-action system "local/echo" {:input ["world"]} {:tag "ctx"})
    :action-store (cell-v2/call-action system "store/query" {:store "cache"
                                                             :input {:id "A"}} nil)
    :remote (cell-v2/remote-call system "http" {:url "/ping"
                                                :method "POST"})
    :protocol (protocol/call "c1" "remote/http" {:input {:url "/ping"}} nil)
    :seen seen})
  => {"listeners" ["all-events"]
      "event" {"last-signal" "db/::REMOVE"
               "last-group" "store"}
      "route" ["ctx" "hello"]
      "store" ["cache" "A"]
      "action_route" ["ctx" "world"]
      "action_store" ["cache" "A"]
      "remote" {"status" "ok"
                "body" {"url" "/ping"
                        "method" "POST"}
                "events" [{"signal" "cell/::LOCAL"
                           "status" "ok"
                           "body" {"action" "after-remote"}}]
                "meta" {}
                "store" {"sync" {"payload" {"User" ["u1"]}}
                         "remove" {"ids" ["s1"]}}}
      "protocol" {"op" "call"
                  "id" "c1"
                  "action" "remote/http"
                  "body" {"input" {"url" "/ping"}}
                  "meta" {}}
      "seen" [["cell/::REMOTE" "ok" {"request_id" "r1"}]
              ["cell/::REMOTE" "ok" {"url" "/ping"
                                     "method" "POST"}]
              ["cell/::LOCAL" "ok" {"action" "after-remote"}]
              ["db/::SYNC" "ok" {"User" ["u1"]}]
              ["db/::REMOVE" "ok" ["s1"]]]})
