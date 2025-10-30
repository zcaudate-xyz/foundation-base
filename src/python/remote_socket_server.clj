(ns python.remote-socket-server
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :python
  {:require [[xt.lang.base-repl :as repl]]
   :import  [["asyncio" :as asyncio]
             ["socket" :as socket]
             ["json" :as json]
             ["threading" :as threading]]})

(defn.py ^{:- [:async]}
  handle-connection
  "handles the connection"
  {:added "4.0"}
  [client]
  (var loop (. asyncio (get-event-loop)))
  (while true
    (let [l ""
          ch (:- :await (. loop (sock-recv client 1)))
          _  (if (== ch (:% b ""))
               (break))
          _  (while (not= ch (:% b "\n"))
               (:+= l (ch.decode))
               (:= ch (:- :await (. loop (sock-recv client 1)))))]
      (cond (== l "<PING>")
            (pass)
            
            :else
            (let [input (json.loads l)
                  out   (repl/return-eval input)]
              (:- :await (. loop (sock-sendall client (. out (encode)))))
              (:- :await (. loop (sock-sendall client (:% b "\n"))))))))
  (. client (close)))

(defn.py ^{:- [:async]}
  run-server
  "runs the server"
  {:added "4.0"}
  [host port]
  (var loop (. asyncio (get-event-loop)))
  (var server (. socket (socket (. socket AF_INET)
                                (. socket SOCK_STREAM))))
  (. server (bind '(host port)))
  (. server (listen 8))
  (. server (setblocking false))

  (while true
    (var ret (:- :await (. loop (sock-accept server))))
    (. loop (create-task (-/handle-connection (. ret [0]))))))

(defn.py start-async-loop
  "starts the async loop"
  {:added "4.0"}
  [port]
  (var loop (. asyncio (new-event-loop)))
  (var coroutine
       (-/run-server "localhost" (or port
                                     12366)))
  (. loop
     (run-until-complete coroutine))
  (. loop (run-forever)))

(defn.py start-async
  "starts the async server"
  {:added "4.0"}
  [port]
  (var thread 
       (. threading
          (Thread :target -/start-async-loop
                  :args [port])))
  (. thread (start))
  (return thread))


(comment
  (l/rt:restart)
  (l/rt:scaffold :python)
  
  (def +s+
    (h/socket "localhost" 12368))
  
  (def +r+
    (std.concurrent/relay
     {:type :socket
      :host "localhost"
      :port 12368}))

  
  @(std.concurrent/send +r+
                        (std.json/write
                         (std.lang/emit-script
                          (rt.basic.impl.process-python/default-body-wrap
                           '[(:- :import bpy)
                             (bpy.ops.object.select_all :action "SELECT")
                             ])
                          {:lang :python})))

  @(std.concurrent/send +r+
                        (std.json/write
                         (std.lang/emit-script
                          (rt.basic.impl.process-python/default-body-wrap
                           '[(:- :import bpy)
                             (bpy.msgbus.publish_rna :key "hello")
                             ])
                          {:lang :python})))
  
  (dir bpy.ops.object)
  
  (spit
   "return"
   )
  
  

  (spit "hello.py"
        (l/emit-script
         `(-/start-async 12366)
         {:lang :python}))
  
  ^*(!.py
     (:= (!:G _th) (-/start-async 12366))
     _th)
  
  (!.py
   (globals))
  
  
  (!.py
   (. _loop (close))
   _loop)
  
  
  (!.py
   (. _loop
      (run_until_complete (-/run-server "localhost" 12366))))
  
  (!.py
   ((-/run-server "localhost" 12366)))

  (!.py
   (-/start-async 12368))
  
  
  
  (h/port:check-available 12366)
  
  (h/port:get-available [12366])
  
  (h/wait-for-port "localhost" 12366 {:timeout 1000})
  
  (!.py
   asyncio))
