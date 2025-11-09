(ns code.ai.common
  (:require [std.fs :as fs]
            [std.lib :as h :use [defimpl]]
            [std.json :as json]
            [std.lib.resource :as res]
            [std.protocol.component :as protocol.component]))

(defn chat-string
  [chat])

(defn chat-start
  "starts the context runtime"
  {:added "3.0"}
  ([{:keys [state] :as sp} ctx]
   ))

(defn chat-stop
  "stops the context runtime"
  {:added "3.0"}
  ([{:keys [state] :as sp} ctx]
   ))

(defimpl Chat [id config state history]
  :prefix "chat-"
  :string chat-string
  :protocols [protocol.component/IComponent
              :exclude [-kill]
              :body {-start component}])

(defn chat?
  "checks that an object is of type chat"
  {:added "3.0"}
  ([obj]
   (instance? Chat obj)))

(defn chat-create
  "creates a chat"
  {:added "3.0"}
  ([{:keys [namechat] :as m}]
   (map->Chat (merge m {:namechat (or namechat (env/ns-sym))
                         :state (atom {})}))))

(res/res:spec-add
 {:type :hara/ai.chat
  :mode {:key :namechat
         :allow #{:namechat}
         :default :namechat}
  :instance {:create chat-create}})


(defn make-plan
  [label])

(comment
  
  (chat {:id "oeoue"
         :name "this is about stuff"
         :plans [:clojure
                 :dsl
                 :tailwind]})
  
  (chat/chat:rt-get
   (chat/chat-create {:namechat :local})
   :hello)

  (chat/chat :local)
  
  (def +resource+
    (h/res:spec-add
     {:type :hara/concurrent.bus
      :instance {:create bus:create}}))

  (h/res:spec-get :hara/concurrent.bus)
  
  (h/res:start :hara/concurrent.bus)
  (h/res:stop :hara/concurrent.bus)
  
  (h/res:spec- :hara/concurrent.bus)
  
  )
