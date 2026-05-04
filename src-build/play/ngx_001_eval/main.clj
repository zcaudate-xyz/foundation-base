(ns play.ngx-001-eval.main
  (:require [hara.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [net.http :as http]
            [net.http.websocket :as client]))

(l/script :lua
  {:runtime :nginx
   :config  {:no-server true
             :port 18091
             :dev {:print false}}
   :require [[lua.core :as u]
             [lua.nginx :as n]
             [lua.nginx.websocket :as ws]]})

(defn lua-block
  "constructs a lua block"
  {:added "4.0"}
  ([form]
   [:content-by-lua-block 
    [[:- (l/emit-script form {:lang :lua
                              :layout :full})]]]))

(def +nginx-conf+
  [[:worker-processes 1]
   [:error-log ["logs/error.log" :warn]]
   [:events {:worker-connections 1024}]
   [:http   [[:lua_package_path "\"$prefix/src/?.lua;;\""]
             [:log-format ["'$remote_addr - $remote_user [$time_local] '"
                           "'$request - $status $body_bytes_sent '"
                           "'$http_referer - $http_user_agent - $gzip_ratio'"]]
             [:lua-shared-dict [:GLOBAL  "20k"]]
             [:lua-shared-dict [:WS_DEBUG  "20k"]]
             [:server
              (vec
               (concat
                [[:listen [18091 :reuseport]]
                 [:access-log ["logs/access.log"]]
                 [:charset "utf-8"]
                 [:charset-types ["application/json"]]
                 [:default-type  ["application/json"]]]
                [[:location ["=" "/eval"
                             [(lua-block
                               '(do (:= cjson (require "cjson"))
                                    (lua.nginx/http-debug-api)))]]]]
                [[:location ["=" "/eval/ws"
                             [(lua-block
                               '(lua.nginx/http-debug-ws))]]]
                 [:location ["=" "/echo/ws"
                             [(lua-block
                               '(lua.nginx/http-echo-ws))]]]]))]]]])

(comment
  (hara.runtime.nginx/error-logs))


(comment

  (!.lua (+ 1 2 3))
  (!.lua
   (u/mapt DEBUG type))
  
  ^:debug
  (!.lua
   (ws/service-register "WS_DEBUG" {})
   (local f ws/ws-main)
   (:= DEBUG
       {:ws-handler (fn []
                      (f "WS_DEBUG"
                         {}
                         {:setup (fn [_ uid]
                                   (n/-set (n/shared :GLOBAL)
                                           (cat "__COUNTER__:" uid)
                                           0)
                                   (return [uid]))
                          :main (fn [conn vars]
                                  (local uid (u/first vars))
                                  (local num (n/-incr (n/shared :GLOBAL)
                                                      (cat "__COUNTER__:" uid)
                                                      1))
                                  (ws/send-text conn (u/tostring num))
                                  (n/sleep 0.1))
                          :teardown (fn []
                                      (n/-delete (n/shared :GLOBAL)))}
                         {}
                         {}))})
   true)
  
  
  

  [skey
   listener
   stream
   conndata
   opts
   dict]
  (def -ws- @(client/websocket "ws://localhost:18091/eval/ws"
                               {:on-open  (fn [& args]
                                            (h/prn args))
                                :on-message  (fn [& args]
                                               (h/prn args))
                                :on-close (fn [& args]
                                            (h/prn args))}))

  (ws/connection-count "WS_DEBUG")
  (client/close! -ws-)

  (client/send! -ws- "hello")
  
  (def -ws- @(client/websocket (str "ws://localhost:18091/echo/ws")
                               {:on-open     (fn [& args]
                                               (h/prn args))
                                :on-message  (fn [& args]
                                               (h/prn args))
                                :on-close    (fn [& args]
                                               (h/prn args))}))
  
  )



