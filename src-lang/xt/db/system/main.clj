(ns xt.db.system.main
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.db.system.main-client :as main-client]
             [xt.db.system.impl-common :as common]
             [xt.db.system.impl-memory :as memory]]})

(defn.xt create-impl
  "Creates memory by default; other backends require an explicit provider map."
  [type defaults schema lookup (:.. optional)]
  (var backends (:? (> (xt/x:len optional) 0) (xt/x:first optional) nil))
  (when (== type "memory") (return (memory/impl-memory schema lookup)))
  (var provider (main-client/get-provider type backends))
  (var create (xt/x:get-key provider "create_impl" nil))
  (when (not (xt/x:is-function? create)) (xt/x:err "Backend provider is missing create_impl"))
  (return (create (main-client/create-client type defaults backends) schema lookup)))

(defn.xt create-impl-init
  "Initializes implementations through their optional initialization protocol."
  [impl]
  (when (protocol/protocol-implements impl (xt/x:get-key common/ISourceInit "on" nil))
    (return (common/init-db impl)))
  (return (promise/x:promise-run impl)))
