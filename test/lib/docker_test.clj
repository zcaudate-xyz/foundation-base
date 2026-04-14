(ns lib.docker-test
  (:require [lib.docker :as docker]
            [std.lang :as l]
            [std.lib.env :as env]
            [std.lib.os :as os])
  (:use code.test))

^{:refer lib.docker/start-runtime :added "4.0"}
(fact "starts a runtime with attached container"
  (with-redefs [docker/start-ryuk (fn [] :ryuk)
                docker/start-reaped (fn [container]
                                      {:container-ip "127.0.0.1"
                                       :container-id "cid"
                                       :container container})
                docker/start-container (fn [container]
                                         {:container-ip "127.0.0.1"
                                          :container-id "cid"
                                          :container container})]
    (docker/start-runtime {:lang :clj :tag :app :module :core}
                          {:suffix "dev"}))
  => (contains {:host "127.0.0.1"
                :container (contains {:id "app-dev"
                                      :container-id "cid"
                                      :labels {"rt/lang" "clj"
                                               "rt/module" "core"}})}))

^{:refer lib.docker/stop-runtime :added "4.0"}
(fact "stops a runtime with attached container"
  (let [stopped (atom nil)]
    (with-redefs [docker/stop-container (fn [container] (reset! stopped container))]
      [(docker/stop-runtime {:id :rt} {:id "cid"})
       @stopped]))
  => [{:id :rt} {:id "cid"}])

(comment

  (docker/list-containers)
  
  (do
    (future 
      (docker/start-reaped {:group   "redis"
                            :id      "redis-build1"
                            :image   "tahto/kmi.infra:v6.2"
                            :ports   [6379 17000]
                            :cmd ["redis-server" "--protected-mode" "no"]}))
    (future 
      (docker/start-reaped {:group   "redis"
                            :id      "redis-build2"
                            :image   "tahto/kmi.infra:v6.2"
                            :ports   [6379 17001]
                            :cmd ["redis-server" "--protected-mode" "no"]}))
    (future
      (docker/start-reaped {:group   "redis"
                            :id      "redis-build3"
                            :image   "tahto/kmi.infra:v6.2"
                            :ports   [[6379 17003]]
                            :cmd ["redis-server" "--protected-mode" "no"]})))

  (docker/get-ip "redis_redis-build1")

  (docker/start-ryuk)

  (docker/stop-all-reaped)
  (docker/docker ["ps"])


  (std.json/read 
   @(os/sh "docker" "images" "--format" "{{json .}}"))

  (std.json/read 
   @(os/sh "docker" "inspect" "--format" "{{json .}}"))

  (std.json/read 
   @(os/sh "docker" "ps" "--format" "{{json .}}"))


  (std.json/read 
   @(os/sh "docker" "ps" "--format" "{{json .}}"))


  (std.json/read 
   @(os/sh "docker" "inspect" "--help"))


  (docker/start-ryuk)




  )

(comment
  
  (require '[std.lang :as l])
  (require '[xt.lang.common-notify :as notify])
  (require 'rt.redis)
  (require 'rt.postgres)
  (common/start-container
   {:id "hello"
    :image  "tahto/kmi.all:v6.2.1"
    :cmd    ["nginx"]
    :remove false})
  
  (common/stop-container
   (common/start-container
    ))
  
  
  (std.lang/script :lua
    {:runtime :redis.client
     :config {:port 6379
              :container {:image  "tahto/kmi.all:v6.2.1"
                          :ports  [6379]
                          :cmd    ["redis-server" "--protected-mode" "no"]}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]
               [kmi.redis :as r]]})
  
  (std.lang/script :lua
    {:runtime :basic
     :config {:container {:image  "tahto/kmi.all:v6.2.1"}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]
               [kmi.redis :as r]]})
  
  
  (l/rt:restart :lua)
  
  (def +redis-array+
    {"local.exchange.0"
     (start-container {:group  "stats"
                       :id     "local.exchange.0"
                       :image  "tahto/kmi.all:v6.2.1"
                       :ports  [6379]
                       :cmd    ["redis-server" "--protected-mode" "no"]})
     "local.exchange.1"
     (start-container {:group  "stats"
                       :id     "local.exchange.1"
                       :image  "tahto/kmi.all:v6.2.1"
                       :ports  [6379]
                       :cmd    ["redis-server" "--protected-mode" "no"]})
     "local.mq"
     (start-container {:group  "stats"
                       :id     "local.mq"
                       :image  "tahto/kmi.all:v6.2.1"
                       :ports  [6379]
                       :cmd    ["redis-server" "--protected-mode" "no"]})})
  
  
  (std.lang/script- :postgres
    {:runtime :jdbc.client
     :config {:port 5432
              :dbname "test-scratch"
              :temp :create
              :container {:group  "stats"
                          :image  "tahto/kmi.all:v6.2.1"
                          :ports  [5432]
                          :environment {"POSTGRES_PASSWORD" "postgres"
                                        "POSTGRES_USER" "postgres"}
                          :cmd    ["postgres"]}}
     :import  [["redis"]]
     :require [[rt.postgres :as pg]
               [statsdb.core.account-base :as account-base]
               [statsdb.core.application :as app]
               [statsdb.core.infra-mq :as infra-mq]
               [statsdb.core.infra-exchange :as infra-xch]
               [statsdb.core.system :as sys]
               [statstest.db.data-bench]
               [statstest.db.dev :as dev]]})

  (do  (l/rt:setup :postgres)
       (doseq [label ["local.exchange.0"
                      "local.exchange.1"
                      "local.mq"]]
         (pg/t:update sys/Service
                      {:set   {:host (get-in +redis-array+
                                             [label
                                              :container-ip])
                               :port 6379}
                       :where {:label label}
                       :track :ignore})))
  
  (infra-xch/xch-frame "TEST:STATS:LOW")
  
  
  (comment
    
    (l/rt:restart :postgres)
    [(l/rt:setup :postgres)]
    (pg/t:select app/Book)
    (pg/t:select sys/Service)
    [{:type "redis", :port 17000, :function "exchange", :time-updated nil, :time-created nil,
      :host "127.0.0.1", :label "local.exchange.0", :id "00000000-0000-0000-0000-000000000000"}
     {:type "redis", :port 17001, :function "exchange", :time-updated nil, :time-created nil,
      :host "127.0.0.1", :label "local.exchange.1", :id "00000000-0000-0000-0000-000000000001"}
     {:type "redis", :port 17003, :function "mq", :time-updated nil, :time-created nil,
      :host "127.0.0.1", :label "local.mq", :id "00000000-0000-0000-0000-000000000003"}]
    (pg/t:select app/UserAccount)
    (pg/t:select sys/Op)
    (dev/test-user-create 10))
  

  
  (common/start-container
   {:id "node"
    :image  "tahto/kmi.ui:16"
    :cmd    ["node" "-e" (rt.basic.impl.process-js/default-basic-client
                          45325
                          {:host "host.docker.internal"})]})
  
  (!.lua
   (require "posix"))
  
  (!.lua
   (os.getenv "HOME"))
  
  (!.lua
   [jit.os jit.arch])
  
  (!.lua
   (os.getenv "OS"))

  (std.lang/script :python
    {:runtime :basic
     :config {:container {:image  "tahto/kmi.all:v6.2.1"}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  (std.lang/script :python
    {:runtime :websocket
     :config {:container {:image  "tahto/kmi.all:v6.2.1"}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  
  (notify/wait-on
   :python
   (repl/notify true))
  
  (std.lang/script :lua
    {:runtime :basic
     :config {:container {:image  "tahto/kmi.all:v6.2.1"}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  (std.lang/script :lua
    {:runtime :basic
     :config {:container {:image  "tahto/kmi.all:v6.2.1"}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})

  (std.lang/script :lua
    {:runtime :basic
     :config {:program :resty
              :container {:image  "tahto/kmi.all:v6.2.1"}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})

  
  (std.lang/script :js
    {:runtime :basic
     :config {:container {:image  "tahto/kmi.ui:16"
                          :remove false}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  (std.lang/script :js
    {:runtime :websocket
     :config {:container {:image  "tahto/kmi.ui:16"
                          :remove false}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  (!.js
   (+ 1 2 3))
  
  (!.lua
   (+ 1 2 3))
  
  (std.lang/script :js
    {:config {:container {:image  "tahto/kmi.ui:16.1"
                          :remove false}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  (lib.docker/start-container
   (assoc rt.basic.type-container/*container*
          :remove false))


  rt.basic.type-container/*container*
  
  
  (!.lua
   (+ 1 2 3))
  
  (!.js
   (+ 1 2 3))
  
  (std.lang/rt:restart :lua)
  (std.lang/rt:restart :js)
  (:container (std.lang/rt :lua))
  
  
  (std.lang/script :lua
    {;:runtime :basic
     #_#_:config {:port 6379
              :container {:image  "tahto/kmi.infra:v5.0.1"
                          :ports  [6379]
                          :cmd    ["redis-server" "--protected-mode" "no"]}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  (std.lang/script :js
    {:runtime :basic
     :config {:bench false}
     #_#_:config {:port 6379
                  :container {:image  "tahto/kmi.infra:v5.0.1"
                              :ports  [6379]
                              :cmd    ["redis-server" "--protected-mode" "no"]}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  (!.js (+ 1 2 3))
  
  
  (std.lang/script :lua
    {;:runtime :basic
     #_#_:config {:port 6379
              :container {:image  "tahto/kmi.infra:v5.0.1"
                          :ports  [6379]
                          :cmd    ["redis-server" "--protected-mode" "no"]}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  (require '[xt.lang.common-notify :as notify])
  
  (std.lang/script :lua
    {:runtime :redis.client
     :config {:port 6379
              :container {:image  "tahto/kmi.infra:v5.0.1"
                          :ports  [6379]
                          :cmd    ["redis-server" "--protected-mode" "no"]}}
     :require [[xt.lang.common-repl :as repl]
               [xt.lang.base-lib :as k]]})
  
  (std.lang/script :lua
    {;:runtime :redis.client
     :config {:port 6379
              :container {:image  "tahto/kmi.infra:v6.2,1"
                          :ports  [6379]
                          :cmd    ["redis-server" "--protected-mode" "no"]}}})
  
  
  (std.lang/script :redis
    {:runtime :redis.client
     :config {:host "172.17.0.3"}})
  
  (env/pl (!.lua
         package.path)
        "./?.lua;/usr/local/share/luajit-2.1.0-beta3/?.lua;/usr/local/share/lua/5.1/?.lua;/usr/local/share/lua/5.1/?/init.lua"
        )

  (!.lua
   (:= package.path
       "./?.lua;/opt/openresty/luajit/share/luajit-2.1.0-beta3/?.lua;/usr/local/share/lua/5.1/?.lua;/usr/local/share/lua/5.1/?/init.lua;/opt/openresty/luajit/share/lua/5.1/?.lua;/opt/openresty/luajit/share/lua/5.1/?/init.lua"))
  
  
  
  
  
  (!.lua
   (require "lpeg"))
  
  (!.lua
   (redis.call "INFO"))
  
  (!.lua
   (pcall (fn:> [] ngx)))
  
  (!.lua
   (var socket (require "socket"))
   )

  (notify/wait-on :lua
    (repl/notify true))
  
  (!.lua
   (repl/notify true)
   )

  (!.lua
   (var posix (require "posix"))
   (posix.exec "ls"))

  (!.lua
   (var posix (require "posix"))
   posix.time.gmtime)
  
  (!.lua
   (var time (require "posix.time"))
   (time.gmtime ((. (require "posix")
                    ["time"]))))
  
  
  (!.lua
   (os.getenv "HOME"))

  (!.lua
   (os.getenv "HOME"))
  
  (!.lua
   (os.date))
  
  (std.lang/rt :lua)
  
  
  
  
  
  (start-container nil {:group   "redis"
                        :image   "tahto/kmi.infra:v6.2"
                        :ports   [6379]
                        :cmd     ["redis-server" "--protected-mode" "no"]})
  
  

  (:container-id
   (start-container nil {:group   "redis"
                         :id      "hello"
                         :image   "tahto/kmi.infra:v6.2"
                         :ports   [6379]
                         :cmd     ["redis-server" "--protected-mode" "no"]}))
  
  
  )
