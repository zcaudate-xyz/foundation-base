(ns rt.basic.impl.process-lua
  (:require [clojure.string]
             [rt.basic.type-basic :as basic]
             [rt.basic.type-common :as common]
             [rt.basic.type-oneshot :as oneshot]
             [rt.basic.type-websocket :as websocket]
             [std.lang.base.impl :as impl]
             [std.lang.base.runtime :as rt]
             [std.lang.model.spec-lua :as spec]
             [std.lib.collection :as collection]
             [std.lib.env :as env]
             [std.lib.os :as os]
             [xt.lang.common-lib :as lib]))

;;
;; PROGRAM
;;

(defn- lua-local-rocks-env
  "adds the user-local luarocks tree to Lua module lookup when present."
  {:added "4.1"}
  []
  (let [home  (System/getenv "HOME")
        share (some-> home (str "/.luarocks/share/lua/5.1"))
        lib   (some-> home (str "/.luarocks/lib/lua/5.1"))
        share-paths (cond-> []
                      (and share (.exists (java.io.File. share)))
                      (into [(str share "/?.lua")
                             (str share "/?/init.lua")]))
        lib-paths   (cond-> []
                      (and lib (.exists (java.io.File. lib)))
                      (conj (str lib "/?.so")))]
    (cond-> {}
      (seq share-paths)
      (assoc "LUA_PATH" (str (clojure.string/join ";" share-paths)
                             ";"
                             (or (System/getenv "LUA_PATH")
                                 ";;")))
      (seq lib-paths)
      (assoc "LUA_CPATH" (str (clojure.string/join ";" lib-paths)
                              ";"
                              (or (System/getenv "LUA_CPATH")
                                  ";;"))))))

(def +lua-local-rocks-shell+
  (when-let [env (not-empty (lua-local-rocks-env))]
    {:env env}))

(def +program-init+
  (common/put-program-options
   :lua  {:default  {:oneshot        :luajit
 	             :basic          :luajit
	             :websocket      :resty}
          :env      {:lua       {:exec   "lua"
                                 :shell  +lua-local-rocks-shell+
	                         :flags  {:oneshot ["-e"]
                                          :basic   ["-e"]
                                          :interactive ["-i"]
	                                  :json ["cjson" :install]
                                          :bench {:basic     ["luasocket" :install]}}}
	             :luajit    {:exec   "luajit"
                                 :shell  +lua-local-rocks-shell+
	                         :flags   {:oneshot ["-e"]
                                           :basic   ["-e"]
                                           :interactive  ["-i"]
	                                   :json ["cjson" :install]
                                           :bench {:basic     ["luasocket" :install]}}}
                     :torch     {:exec   "th"
                                 :shell  +lua-local-rocks-shell+
	                         :flags   {:oneshot ["-e"]
                                           :basic   ["-e"]
                                           :interactive  ["-i"]
	                                   :json ["cjson" :install]
                                           :bench {:basic     ["luasocket" :install]}}}
	             :resty     {:exec   "resty"
                                 :shell  +lua-local-rocks-shell+
	                         :flags   {:oneshot   ["-e"]
                                           :basic     ["-e"]
                                           :websocket ["-e"]
                                           :interactive false
 	                                   :json ["cjson" :builtin]
                                            :bench {:basic     ["resty.socket" :builtin]
                                                    :websocket ["resty.websocket.client" :builtin]}}}}}))

(def +program-init-nginx+
  (common/put-program-options
   :lua.nginx
   (common/get-program-options :lua)))

;;
;; ONESHOT
;; 

(defn default-body-wrap
  "wraps body forms in a local helper so inline defs remain callable within
   the same Lua eval scope."
  {:added "4.1"}
  [forms]
  (let [forms (rt/return-format forms '#{:- := var local def defn break throw})]
    (list 'do
          (apply list 'defn (with-meta 'OUT-FN
                              {:inner true})
                 []
                 forms)
          (list 'return (list 'OUT-FN)))))

(defn normalize-forms
  "normalizes runtime input into a flat sequence of Lua statements."
  {:added "4.1"}
  [input {:keys [bulk]}]
  (let [forms (if bulk input [input])]
    (if (and (= 1 (count forms))
             (collection/form? (first forms))
             (= 'do (ffirst forms)))
      (rest (first forms))
      forms)))

(defn mark-inline-defs
  "marks inline `defn` forms as inner so Lua emits local helper definitions."
  {:added "4.1"}
  [forms]
  (map (fn [form]
         (if (and (collection/form? form)
                  (= 'defn (first form))
                  (symbol? (second form)))
           (apply list 'defn
                  (with-meta (second form)
                    (assoc (meta (second form)) :inner true))
                  (drop 2 form))
           form))
       forms))

(defn default-body-transform
  "transform code for return
 
   (default-body-transform [1 2 3] {})
   => '(do (return [1 2 3]))
 
   (default-body-transform [1 2 3] {:bulk true})
   => '(do 1 2 (return 3))"
  {:added "4.0"}
  [input mopts]
  (-> (normalize-forms input mopts)
      (mark-inline-defs)
      (default-body-wrap)))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap (impl/emit-entry-deps
                   lib/return-eval
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

(def +lua-nginx-oneshot-config+
  (common/set-context-options
   [:lua.nginx :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +lua-oneshot+
  [(rt/install-type!
    :lua :oneshot
    {:type :hara/rt.oneshot
     :instance {:create #'oneshot/rt-oneshot:create}
     :config {:layout :full}})
   (rt/install-type!
    :lua.nginx :oneshot
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
                         lib/return-eval
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

(def +lua-nginx-basic-config+
  (common/set-context-options
   [:lua.nginx :basic :default]
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
     :config {:layout :full}})
   (rt/install-type!
    :lua.nginx :basic
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
                         lib/return-eval
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

(def +lua-nginx-websocket-config+
  (common/set-context-options
   [:lua.nginx :websocket :default]
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
     :config {:layout :full}})
   (rt/install-type!
    :lua.nginx :websocket
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
