(ns xt.db.node.runtime
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-supabase :as proxy-supabase]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.kernel-supabase :as kernel-supabase]]})

(defn.xt init-server
  [node]
  (page-proxy/install node)
  (kernel-base/init-handlers node)
  (kernel-supabase/init-handlers node))

(defn.xt init-server-proxy
  [node]
  (page-proxy/install node)
  (proxy-base/init-proxy-handlers node)
  (proxy-supabase/init-proxy-handlers node))
