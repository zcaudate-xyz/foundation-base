(ns xt.db.node
  (:refer-clojure :exclude [remove sync])
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.main :as main]
             [xt.db.node.spec :as spec]
             [xt.db.node.state :as state]]})

(def.xt META_KEY spec/META_KEY)
(def.xt STATE_TAG spec/STATE_TAG)

(def.xt ACTION_QUERY spec/ACTION_QUERY)
(def.xt ACTION_QUERY_REFRESH spec/ACTION_QUERY_REFRESH)
(def.xt ACTION_SYNC spec/ACTION_SYNC)
(def.xt ACTION_REMOVE spec/ACTION_REMOVE)
(def.xt ACTION_CLEAR spec/ACTION_CLEAR)
(def.xt ACTION_SNAPSHOT spec/ACTION_SNAPSHOT)

(def.xt SIGNAL_CACHE_CHANGED spec/SIGNAL_CACHE_CHANGED)
(def.xt SIGNAL_CACHE_INVALIDATED spec/SIGNAL_CACHE_INVALIDATED)
(def.xt SIGNAL_QUERY_CHANGED spec/SIGNAL_QUERY_CHANGED)
(def.xt SIGNAL_MODEL_CHANGED spec/SIGNAL_MODEL_CHANGED)

(def.xt install main/install)
(def.xt uninstall main/uninstall)
(def.xt ensure-space-state main/ensure-space-state)

(def.xt query main/query)
(def.xt query-refresh main/query-refresh)
(def.xt sync main/sync)
(def.xt remove main/remove)
(def.xt clear main/clear)
(def.xt snapshot main/snapshot)

(def.xt model-put main/model-put)
(def.xt model-get main/model-get)
(def.xt model-refresh main/model-refresh)
(def.xt view-put main/view-put)
(def.xt view-get main/view-get)
(def.xt view-val main/view-val)
(def.xt view-input main/view-input)
(def.xt view-pending main/view-pending)
(def.xt view-error main/view-error)
(def.xt view-refresh main/view-refresh)
(def.xt view-set-input main/view-set-input)

(def.xt node-opts state/node-opts)
