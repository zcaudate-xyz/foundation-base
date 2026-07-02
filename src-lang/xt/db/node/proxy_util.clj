(ns xt.db.node.proxy-util
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]]})

;;
;; Client-side proxy handlers for the server-side supabase adaptor handlers in
;; xt.db.node.kernel-supabase.
;;
;; These mirror the server substrate handler ids (e.g. @xt.supabase/sign-in)
;; so that the same function ids can be invoked on a client node and be
;; forwarded to the server over the configured transport.
;;
;; Substrate routing will always prefer an attached transport when no
;; transport_id is given, so proxy handlers must supply transport_id
;; explicitly from the request meta.
;;

(defn.xt set-default-transport
  "sets the default server transport id for proxy requests"
  {:added "4.1"}
  [node transport-id]
  (xtd/set-in node ["state" "adaptor_proxy" "default_transport_id"] transport-id)
  (return transport-id))

(defn.xt get-default-transport
  "gets the default server transport id for proxy requests"
  {:added "4.1"}
  [node]
  (return (xtd/get-in node ["state" "adaptor_proxy" "default_transport_id"])))

(defn.xt get-transport-id
  "resolves the transport id from opts or the node default"
  {:added "4.1"}
  [node opts]
  (return (or (xtd/get-in opts ["transport_id"])
              (-/get-default-transport node))))

(defn.xt request-meta
  "builds request meta with an explicit transport_id"
  {:added "4.1"}
  [node request]
  (var transport-id (-/get-transport-id node request))
  (return {"transport_id" transport-id}))

(defn.xt request-proxy
  "generic client proxy for @xt.supabase/* actions"
  {:added "4.1"}
  [space args request node]
  (return
   (substrate/request node
                      nil
                      (xt/x:get-key request "action")
                      args
                      (-/request-meta node request))))

(defn.xt request-client
  "calls a supabase action through substrate/request"
  {:added "4.1"}
  [node action args opts]
  (return
   (substrate/request node
                      nil
                      action
                      args
                      (-/request-meta node opts))))
