(ns lua.nginx.http-client
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :lua
  {:import [["resty.http" :as ngxhttp]
            ["multipart" :as ngxmultipart]
            ["resty.http" :as ngxhttp]
            ["multipart" :as ngxmultipart]]})

(defmacro.lua new
  "creates a new lua client"
  {:added "4.0"}
  []
  (list 'ngxhttp.new))

(h/template-entries [l/tmpl-macro {:base "http"
                                   :inst "client"
                                   :tag "lua"}]
  [[connect       [options]]
   [set-proxy-options [opts]]
   [set-timeout   [time]]
   [set-timeouts [connect-timeout, send-timeout, read-timeout]]
   [set-keepalive  [max-idle-timeout, pool-size]]
   [get-reused-times  []]
   [close    []]
   
   [request  [params]]
   [request-uri [uri params]]
   [request-pipeline [params]]
   [parse-uri [uri] {:optional [query-in-path]}]
   [get-client-body-reader [] {:optional [chunksize sock]}]])

