(ns xt.db.node.proxy-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
             [xt.db.node.proxy-util :as proxy-util]]})

(def.xt ACTIONS
  [])

(defn.xt init-proxy-handlers
  "Registers client-side supabase proxy handlers so that the same substrate
   function ids used server-side can be invoked on a client node and forwarded
   to the server."
  {:added "4.1"}
  [node]
  (xt/for:array [action -/ACTIONS]
    (substrate/register-handler node action proxy-util/supabase-forward-handler nil))
  (return node))
