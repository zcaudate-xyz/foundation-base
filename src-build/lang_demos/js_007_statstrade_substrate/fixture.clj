(ns lang-demos.js-007-statstrade-substrate.fixture
  (:require [lang.core :as l]))

(l/script :js
  {:require [[lang-demos.js-007-statstrade-substrate.link :as link]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as memory]
             [xt.lang.spec-promise :as promise]]})

(def.js SPACE "demo/topics")

(defn.js seedTopics
  []
  (return [{:id "topic-1" :title "Will turnout increase?" :revision 1}
           {:id "topic-2" :title "Will the bill pass?" :revision 1}]))

(defn.js install
  [server]
  (substrate/register-handler
   server "topic/list"
   (fn [space args request node] (return (. space state topics))) {})
  (substrate/register-handler
   server "topic/detail"
   (fn [space args request node]
     (return (. (. space state topics)
                (find (fn [row] (return (== (. row id) (. args [0])))))))) {})
  (substrate/register-handler
   server "topic/modify"
   (fn [space args request node]
     (var [id title] args)
     (var row (. (. space state topics)
                 (find (fn [entry] (return (== (. entry id) id))))))
     (when (not row) (throw (new Error "Unknown topic")))
     (when (or (not= "string" (typeof title)) (== "" (. title (trim))))
       (throw (new Error "Title is required")))
     (:= (. row title) (. title (trim)))
     (:= (. row revision) (+ (. row revision) 1))
     (return (. (substrate/publish node (. space id) "topic/changed" {:id id} {})
                (then (fn [] (return row)))))) {})
  (return server))

(defn.js close
  [state]
  (var #{client server context} state)
  (return
   (. (substrate/unsubscribe client (. context space) "topic/changed" "topic-editor"
                            {:transport_id "server"})
      (then (fn []
              (substrate/unregister-trigger client "topic/changed")
              (return (promise/x:promise-all
                       [(substrate/detach-transport client "server")
                        (substrate/detach-transport server "client")])))))))

(defn.js bootstrap
  []
  (var server (-/install
               (substrate/node-create
                {:id "topic-server"
                 :spaces {"demo/topics" {:state {:topics (-/seedTopics)}}}})))
  (var client (substrate/node-create {:id "topic-client"}))
  (link/install-event-bridge client "topic/changed")
  (var wire (memory/memory-pair {:left_id "client" :right_id "server"}))
  (var context {:node client :space -/SPACE :transport_id "server"})
  (return
   (. (promise/x:promise-all
       [(substrate/attach-transport client "server" (memory/text-endpoint (. wire left)))
        (substrate/attach-transport server "client" (memory/text-endpoint (. wire right)))])
      (then (fn []
              (return (substrate/subscribe client -/SPACE "topic/changed" "topic-editor"
                                          {:transport_id "server"}))))
      (then (fn [] (return #{client server context}))))))
