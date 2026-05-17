(ns xt.event.node
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.event.node-space :as space]
             [xt.event.node-main :as main]]})

(def.xt create-space space/create-space)
(def.xt get-space space/get-space)
(def.xt list-spaces space/list-spaces)
(def.xt get-space-state space/get-space-state)
(def.xt set-space-state space/set-space-state)
(def.xt update-space-state space/update-space-state)

(def.xt node? main/node?)
(def.xt transport? main/transport?)
(def.xt node-create main/node-create)
(def.xt register-handler main/register-handler)
(def.xt unregister-handler main/unregister-handler)
(def.xt get-handler main/get-handler)
(def.xt list-handlers main/list-handlers)
(def.xt register-trigger main/register-trigger)
(def.xt unregister-trigger main/unregister-trigger)
(def.xt get-trigger main/get-trigger)
(def.xt list-triggers main/list-triggers)
(def.xt get-transport main/get-transport)
(def.xt list-transports main/list-transports)
(def.xt attach-transport main/attach-transport)
(def.xt detach-transport main/detach-transport)
(def.xt request main/request)
(def.xt publish main/publish)
