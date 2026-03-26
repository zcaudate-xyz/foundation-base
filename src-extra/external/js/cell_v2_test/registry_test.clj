(ns js.cell-v2.registry-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell-v2.route :as route]
             [js.cell-v2.store :as store]
             [js.cell-v2.remote :as remote]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.route/register-route :added "4.0" :unchecked true}
(fact "supports route, store, and remote registries"
  (!.js
   (var routes (route/make-registry))
   (route/register-route routes
                         "local/ping"
                         (fn [ctx arg]
                           (return [(. ctx ["tag"]) arg]))
                         {:kind "query"})
   {:routes (route/list-routes routes)
    :route-kind (. (route/get-route routes "local/ping") ["kind"])
    :route-value (route/dispatch-route routes "local/ping" ["hello"] {:tag "ctx"})})
  => {"routes" ["local/ping"]
      "route_kind" "query"
      "route_value" ["ctx" "hello"]}

  (!.js
   (var stores (store/make-registry))
   (store/register-store stores
                          "cache"
                          {:read (fn [input]
                                   (return ["read" (. input ["id"])]))
                           :write (fn [input]
                                    (return ["write" (. input ["id"])]))
                           :sync (fn [input]
                                   (return (. input ["payload"])))
                           :remove (fn [input]
                                     (return (. input ["ids"])))
                           :clear (fn []
                                    (return "cleared"))})
   (store/register-store stores
                         "memory"
                         {:read (fn [input]
                                   (return ["mem" (. input ["id"])]))})
    {:stores (store/list-stores stores)
     :read (store/store-read stores "cache" {:id "A"})
     :write (store/store-write stores "cache" {:id "A"})
     :sync (store/store-sync stores "cache" {:payload {"ok" true}})
     :remove (store/store-remove stores "cache" {:ids ["A"]})
     :query (store/store-query stores "cache" {:id "A"})
     :clear (store/store-clear stores "cache" {})})
  => {"stores" ["cache" "memory"]
      "read" ["read" "A"]
      "write" ["write" "A"]
      "sync" {"ok" true}
      "remove" ["A"]
      "query" ["read" "A"]
      "clear" "cleared"}

  (!.js
   (var remotes (remote/make-registry))
   (remote/register-remote remotes
                           "http"
                           {:call (fn [input]
                                    (return {:remote "http"
                                             :path (. input ["path"])}))})
   (remote/register-remote remotes
                           "rpc"
                           {:call (fn [input]
                                    (return {:remote "rpc"
                                              :method (. input ["method"])}))})
   {:remotes (remote/list-remotes remotes)
    :http (remote/remote-call remotes "http" {:path "/ping"})
    :rpc (remote/remote-call remotes "rpc" {:method "ping"})})
  => {"remotes" ["http" "rpc"]
      "http" {"remote" "http" "path" "/ping"}
      "rpc" {"remote" "rpc" "method" "ping"}})
