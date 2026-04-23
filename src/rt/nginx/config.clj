(ns rt.nginx.config
  (:require [lua.nginx]
            [rt.nginx.script :as script]
            [std.lang :as l]
            [std.lib.os :as os]))

;;;;
;;
;; TEST SERVER
;;

(def +eval-block+
  (str "cjson = require('cjson')\n\n"
       (std.lang.base.pointer/ptr-invoke-script
        lua.nginx/http-debug-api []
        {:lang :lua.nginx})))

(def +eval-ws+
  (l/emit-as
   :lua.nginx '[(lua.nginx/http-debug-ws)]))

(def +eval-es+
  (l/emit-as
   :lua.nginx '[(lua.nginx/http-debug-es)]))

(def +echo-ws+
  (l/emit-as
   :lua.nginx '[(lua.nginx/http-echo-ws)]))

(def +init-block+
  (l/emit-as
   :lua.nginx '[(lua.nginx/http-setup-global)]))

(defn create-resty-params
  "creates default resty params"
  {:added "4.0"}
  ([& [{:keys [blocks]}]]
   (script/write [[:client-body-buffer-size "1m"]
                  [:variables-hash-max-size 2048]
                  [:variables-hash-bucket-size 128]
                  [:lua-shared-dict [:GLOBAL    "20k"]]
                  [:lua-shared-dict [:WS_DEBUG  "20k"]]
                  [:lua-shared-dict [:ES_DEBUG  "20k"]]])))

(defn create-conf
  "cerates default conf"
  {:added "4.0"}
  ([{:keys [port
            blocks]}]
   [[:worker-processes 1]
    (if (= (os/os) "Linux")
     [:user "root" "root"])
    [:error-log ["error.log" :warn]]
    [:events {:worker-connections 1024}]
    [:http   [[:log-format ["'$remote_addr - $remote_user [$time_local] '"
                            "'$request - $status $body_bytes_sent '"
                            "'$http_referer - $http_user_agent - $gzip_ratio'"]]
              [:client-body-buffer-size "1m"]
              [:variables-hash-max-size 2048]
              [:variables-hash-bucket-size 128]
              [:access-log ["access.log"]]
              [:lua-shared-dict [:GLOBAL    "20k"]]
              [:lua-shared-dict [:WS_DEBUG  "20k"]]
              [:lua-shared-dict [:ES_DEBUG  "20k"]]
              (or (:init blocks)
                  [:init-by-lua-block
                   [[:- +init-block+]]])
              [:server
               (vec
                (concat
                 [[:listen [port :reuseport]]
                  [:charset "utf-8"]
                  [:charset-types ["application/json"]]
                  [:default-type  ["application/json"]]]
                 [[:location ["=" "/eval"
                              [[:resolver "8.8.8.8"]
                               #_[:allow "127.0.0.1"]
                               #_[:deny "all"]
                               [:content-by-lua-block 
                                [[:- +eval-block+]]]]]]
                  [:location ["=" "/eval/ws"
                              [[:content-by-lua-block 
                                [[:- +eval-ws+]]]]]]
                  [:location ["=" "/eval/es"
                              [[:default-type "application/event-stream"]
                               [:content-by-lua-block 
                                [[:- +eval-es+]]]]]]]))]]]]))
