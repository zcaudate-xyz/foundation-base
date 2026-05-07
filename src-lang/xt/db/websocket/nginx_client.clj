(ns xt.db.websocket.nginx-client
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt join-url
  "joins a base url and relative path"
  {:added "4.1.3"}
  [base-url path]
  (var base-trailing-slash
       (and (xt/x:is-string? base-url)
            (< 0 (xt/x:len base-url))
            (== "/"
                (xt/x:str-substring base-url
                                    (- (xt/x:len base-url) 1)))))
  (var path-leading-slash
       (and (xt/x:is-string? path)
            (< 0 (xt/x:len path))
            (== "/"
                (xt/x:str-substring path 0 1))))
  (cond (or (xt/x:nil? base-url)
            (not (xt/x:is-string? base-url)))
        (return path)

        (and base-trailing-slash
             path-leading-slash)
        (return (xt/x:cat base-url
                          (xt/x:str-substring path 1)))

        (and (not base-trailing-slash)
             (not path-leading-slash))
        (return (xt/x:cat base-url "/" path))

        :else
        (return (xt/x:cat base-url path))))

(defn.xt host-url
  "builds a websocket host url from an nginx-style options map"
  {:added "4.1.3"}
  [opts]
  (:= opts (or opts {}))
  (return
   (xt/x:cat (:? (xt/x:get-key opts "secured") "wss" "ws")
             "://"
             (or (xt/x:get-key opts "host") "localhost")
             ":"
             (xt/x:to-string (or (xt/x:get-key opts "port") 80)))))

(defn.xt query-string
  "encodes a websocket query string using an optional encoder"
  {:added "4.1.3"}
  [params encode-fn]
  (var out [])
  (xt/for:object [[k v] (or params {})]
    (when (xt/x:not-nil? v)
      (var ek (:? (xt/x:is-function? encode-fn)
                  (encode-fn (xt/x:to-string k))
                  (xt/x:to-string k)))
      (var ev (:? (xt/x:is-function? encode-fn)
                  (encode-fn (xt/x:to-string v))
                  (xt/x:to-string v)))
      (xt/x:arr-push out (xt/x:cat ek "=" ev))))
  (return (xt/x:str-join "&" out)))

(defn.xt endpoint-url
  "builds a websocket endpoint url using statslink-style host and params"
  {:added "4.1.3"}
  [path params opts]
  (:= opts (or opts {}))
  (var base (or (xt/x:get-key opts "base-url")
                (-/host-url opts)))
  (var url (-/join-url base path))
  (var query (-/query-string params (xt/x:get-key opts "encode-param")))
  (if (== query "")
    (return url)
    (return (xt/x:cat url "?" query))))

(l/script :lua.nginx
  {:require [[xt.lang.spec-base :as xt]
             [lua.nginx :as ngx]
             [lua.nginx.ws-client :as ngxws]
             [xt.protocol.impl.client-websocket :as ws]]})

(defn.lua default-connect-raw
  "opens an nginx websocket client using resty.websocket.client"
  {:added "4.1.3"}
  [url connect-opts]
  (var client (ngxws/new))
  (local '[ok err] (. client (connect url (or connect-opts {}))))
  (when (not ok)
    (xt/x:err (xt/x:cat "Failed to connect nginx websocket client - "
                        (xt/x:to-string err))))
  (return client))

(defn.lua create-driver
  "creates a websocket protocol driver backed by lua.nginx.ws-client"
  {:added "4.1.3"}
  [opts]
  (:= opts (xt/x:obj-clone (or opts {})))
  (var connect-opts (xt/x:obj-clone (or (xt/x:get-key opts "connect-options")
                                        {})))
  (return
   (ws/driver-create
    {"connect" (fn [url]
                 (return (-/default-connect-raw url connect-opts)))})))

(defn.lua connect
  "connects to an nginx websocket endpoint using the websocket protocol"
  {:added "4.1.3"}
  [path params opts]
  (:= opts (xt/x:obj-clone (or opts {})))
  (when (xt/x:nil? (xt/x:get-key opts "encode-param"))
    (xt/x:set-key opts "encode-param" ngx/escape-uri))
  (var driver (or (xt/x:get-key opts "driver")
                  (-/create-driver opts)))
  (return (ws/connect driver (-/endpoint-url path params opts))))
