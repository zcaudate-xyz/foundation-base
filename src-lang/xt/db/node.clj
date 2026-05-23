(ns xt.db.node
  (:refer-clojure :exclude [remove sync])
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.view-model :as model]
             [xt.db.node.view-util :as util]
             [xt.db.node.schema-spec :as spec]]})

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

(def.xt install model/install)
(def.xt uninstall model/uninstall)
(def.xt ensure-space-state model/ensure-space-state)

(def.xt query model/query)
(def.xt query-refresh model/query-refresh)
(def.xt sync model/sync)
(def.xt remove model/remove)
(def.xt clear model/clear)
(def.xt snapshot model/snapshot)

(def.xt model-put model/model-put)
(def.xt model-get model/model-get)
(def.xt model-dependents model/model-dependents)
(def.xt model-refresh model/model-refresh)
(def.xt view-put model/view-put)
(def.xt view-get model/view-get)
(def.xt view-dependents model/view-dependents)
(def.xt view-val model/view-val)
(def.xt view-input model/view-input)
(def.xt view-pending model/view-pending)
(def.xt view-error model/view-error)
(def.xt view-refresh model/view-refresh)
(def.xt view-set-input model/view-set-input)

(def.xt node-opts util/node-opts)
