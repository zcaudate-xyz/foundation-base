(ns rt.basic.impl.process-lua
  (:require [clojure.string]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-websocket :as websocket]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang.model.spec-lua :as spec]
            [std.lib.env :as env]
            [std.lib.os :as os]
            [xt.lang.common-repl :as k]))

;;
;; PROGRAM
;;

(def +program-init+
  (common/put-program-options
   :lua  {:default  {:oneshot        :luajit
	             :basic          :luajit
	             :websocket      :resty}
          :env      {:lua       {:exec   "lua"
	                         :flags  {:oneshot ["-e"]
                                          :basic   ["-e"]
                                          :interactive ["-i"]
	                                  :json ["cjson" :install]
                                          :bench {:basic     ["luasocket" :install]}}}
	             :luajit    {:exec   "luajit"
	                         :flags   {:oneshot ["-e"]
                                           :basic   ["-e"]
                                           :interactive  ["-i"]
	                                   :json ["cjson" :install]
                                           :bench {:basic     ["luasocket" :install]}}}
                     :torch     {:exec   "th"
	                         :flags   {:oneshot ["-e"]
                                           :basic   ["-e"]
                                           :interactive  ["-i"]
	                                   :json ["cjson" :install]
                                           :bench {:basic     ["luasocket" :install]}}}
	             :resty     {:exec   "resty"
	                         :flags   {:oneshot   ["-e"]
                                           :basic     ["-e"]
                                           :websocket ["-e"]
                                           :interactive false
	                                   :json ["cjson" :builtin]
                                           :bench {:basic     ["resty.socket" :builtin]
                                                   :websocket ["resty.websocket.client" :builtin]}}}}}))

;;
;; ONESHOT
;; 

(defn default-body-transform
  "transform code for return
 
   (default-body-transform [1 2 3] {})
   => '(do (return [1 2 3]))
 
   (default-body-transform [1 2 3] {:bulk true})
   => '(do 1 2 (return 3))"
  {:added "4.0"}
  [input mopts]
  (rt/return-transform
   input mopts
   {:wrap-fn (fn [forms]
               (apply list 'do forms))}))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap (impl/emit-entry-deps
                   k/return-eval
                   {:lang :lua
                     :layout :flat})]
    (fn [body]
      (str "cjson = require(\"cjson\")\n\n"
           bootstrap
           "\n\n"
           (impl/emit-as
            :lua [(list 'print (list 'return-eval body))])))))

(def +lua-oneshot-config+
  (common/set-context-options
   [:lua :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +lua-oneshot+
  [(rt/install-type!
    :lua :oneshot
    {:type :hara/rt.oneshot
     :instance {:create #'oneshot/rt-oneshot:create}
     :config {:layout :full}})])

;;
;; BASIC
;; 

(def +client-basic+
  '[(defn client-basic
      [host port opts]
      (local '[conn ok err])
      (if ngx
        (do (:= conn (ngx.socket.tcp))
            (. conn  (settimeout 1000000))
            (:= '[ok err]  (conn:connect host port)))
        (do (local socket (require "socket"))
            (:= '[conn err] (socket.connect host port))))
      (pcall (fn []
               (while true
                 (local '[raw err] (conn:receive "*l"))
                 (cond err (break)
                       
                       (== raw "<PING>") (:-)
                       
                       :else
                       (do (local input (cjson.decode raw))
                           (conn:send (cat (return-eval input) "\n"))))))))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [bootstrap (->> ["cjson = require(\"cjson\")"
                        (impl/emit-entry-deps
                         k/return-eval
                         {:lang :lua
                          :layout :flat})
                        (impl/emit-as
                         :lua +client-basic+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :lua [(list 'client-basic
                        (or host "127.0.0.1")
                        port)])))))

(def +lua-basic-config+
  (common/set-context-options
   [:lua :basic :default]
   {:bootstrap #'default-basic-client
    :main  {}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full
    :encode :json
    :timeout 2000}))

(def +lua-basic+
  [(rt/install-type!
    :lua :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])

;;
;; WEBSOCKET
;; 

(def +client-ws+
  '[(defn client-ws
      [host port opts]
      (var client := (require "resty.websocket.client"))
      (var '[conn err] (client:new))
      (var uri (cat "ws://" host ":" port "/"))
      (var '[ok err] (conn:connect uri))
      (if (not ok)
        (ngx.say (cat "failed to connect: " err)))
      (while true
        (var '[data type err] (conn:recv-frame))
        (when err
          (ngx.say (cat "failed to read frame: " err))
          (break))
        (when data
          (var msg (cjson.decode data))
          (var #{id body} msg)
          (conn:send-text (cjson.encode  {:id id
                                          :status "ok"
                                          :body (return-eval body)})))))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-websocket-client
  (let [bootstrap (->> ["cjson = require(\"cjson\")"
                        (impl/emit-entry-deps
                         k/return-eval
                         {:lang :lua
                          :layout :flat})
                        (impl/emit-as
                         :lua +client-ws+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :lua [(list 'client-ws
                        (or host "127.0.0.1")
                        port
                        {})])))))

(def +lua-websocket-config+
  (common/set-context-options
   [:lua :websocket :default]
   {:bootstrap #'default-websocket-client
    :main  {}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full
    :encode :json
    :timeout 2000}))

(def +lua-websocket+
  [(rt/install-type!
    :lua :websocket
    {:type :hara/rt.websocket
     :instance {:create #'websocket/rt-websocket:create}
     :config {:layout :full}})])

(comment
  (def +sh+ (os/sh {:args ["resty" "-e" (default-basic-client 51270)]
                   :wait false}))

  (def +sh+ (os/sh {:args ["resty" "-e" (default-websocket-client 60714)]
                   :wait false}))
  
  (env/pl (default-websocket-client 60714))
  
  (os/sh-output +sh+)
  )
