(ns rt.basic.impl.process-python
  (:require [clojure.string]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [rt.basic.type-remote-port :as remote-port]
            [rt.basic.type-websocket :as websocket]
            [std.json :as json]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang.model.spec-python :as spec]
            [std.lib.os :as os]
            [xt.lang.common-repl :as repl]))

(def +python-init+
  (common/put-program-options
   :python  {:default  {:oneshot     :cpython
                        :basic       :cpython
                        :interactive :cpython
                        :websocket   :cpython}
             :env      {:conda     {:exec    "conda"
                                    :flags   {:oneshot     ["run" "-n" :venv "python" "-c"]
                                              :basic       ["run" "-n" :venv "python" "-c"]
                                              :websocket   ["run" "-n" :venv "python" "-c"]
                                              :interactive ["run" "-n" :venv "python" "-i"]
                                              :json ["json" :builtin]
                                              :ws-client ["websocket" :installed]}}
                        :cpython   {:exec    "python3"
                                    :flags   {:oneshot   ["-c"]
                                              :basic     ["-c"]
                                              :websocket ["-c"]
                                              :interactive ["-i"]
                                              :json ["json" :builtin]
                                              :ws-client ["websocket" :installed]}}
                        :pypy      {:exec    "pypy"
                                    :flags   {:oneshot   ["-c"]
                                              :basic     ["-c"]
                                              :websocket ["-c"]
                                              :interactive ["-i"]
                                              :json ["json" :builtin]
                                              :ws-client ["websocket" :installed]}}}}))

;;
;; EVAL
;;


(defn default-body-wrap
  "creates the scaffolding for the runtime eval to work"
  {:added "4.0"}
  [forms]
  (list 'do
        (list 'defn (with-meta 'OUT-FN
                      {:inner true})
              []
              '(:- :import traceback)
              '(var err)
              (concat
               '[try]
               (butlast forms)
               [(list 'return (last forms))
                '(catch Exception
                     (:= err (. traceback (format-exc))))])
              '(throw (Exception err)))
        '(:= (. (globals) ["OUT"])
             (OUT-FN))))

(defn default-body-transform
  "standard python transforms"
  {:added "4.0"}
  [input mopts]
  (rt/return-transform
   input mopts
   {:format-fn identity
    :wrap-fn default-body-wrap}))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap (impl/emit-entry-deps
                   repl/return-eval
                   {:lang :python
                    :layout :flat})]
    (fn [body]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :python [(list 'print (list 'return-eval body))])))))

(def +python-oneshot-config+
  (common/set-context-options
   [:python :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +python-oneshot+
  [(rt/install-type!
    :python :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;; BASIC
;;

(def +client-basic+
  '[(defn client-basic
      [host port opts]
      (:- :import json)
      (:- :import socket)
      (let [conn   (socket.socket)
            _      (conn.connect '(host port))]
        (while true
          (let [l ""
                ch (conn.recv 1)
                _  (if (== ch (:% b ""))
                     (break))
                _  (while (not= ch (:% b "\n"))
                     (:+= l (ch.decode))
                     (:= ch (conn.recv 1)))]
            (cond (== l "<PING>")
                  (pass)
                  
                  :else
                  (let [input (json.loads l)
                        out   (return-eval input)]
                    (conn.sendall (. out (encode)))
                    (conn.sendall (:% b "\n"))))))))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [bootstrap (->> [(impl/emit-entry-deps
                         repl/return-eval
                         {:lang :python
                          :layout :flat})
                        (impl/emit-as
                         :python +client-basic+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :python [(list 'client-basic
                           (or host "127.0.0.1")
                           port
                           {})])))))

(def +python-basic-config+
  (common/set-context-options
   [:python :basic :default]
   {:bootstrap #'default-basic-client
    :main  {}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full
    :encode :json
    :timeout 2000}))

(def +python-basic+
  [(rt/install-type!
    :python :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])

;;
;; WEBSOCKET
;;

(def +client-ws+
  '[(defn client-ws
      [host port opts]
      (:- :import json)
      (:- :import websocket)
      (:- :import threading)
      (let [_      (defn on-message [conn msg]
                     (let [data  (json.loads msg)
                           id    (. data ["id"])
                           input (. data ["body"])
                           out   (return-eval input)]
                       (conn.send (json.dumps {:id id 
                                               :status "ok"
                                               :body out}))))
            conn   (websocket.WebSocketApp
                    (+ "ws://" (or host "127.0.0.1") ":" (str port) "/")
                    :on-message on-message)
            thd    (threading.Thread
                    :target (fn [] (conn.run-forever)))
            _      (thd.start)]
        (return conn)))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-websocket-client
  (let [bootstrap  (->> [(impl/emit-entry-deps
                          repl/return-eval
                         {:lang :python
                          :layout :flat})
                        (impl/emit-as
                         :python +client-ws+)]
                       (clojure.string/join "\n\n"))]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :python [(list 'client-ws
                           host
                           port
                           {})])))))

(def +python-websocket-config+
  (common/set-context-options
   [:python :websocket :default]
   {:bootstrap #'default-websocket-client
    :main  {}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full
    :encode :json
    :timeout 2000}))

(def +python-websocket+
  [(rt/install-type!
    :python :websocket
    {:type :hara/rt.websocket
     :instance {:create #'websocket/rt-websocket:create}
     :config {:layout :full}})])

;;
;; REMOTE SOCKET
;;

(def +python-remote-port-config+
  (common/set-context-options
   [:python :remote-port :default]
   {:main  {}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full
    :encode :json
    :timeout 2000}))

(def +python-remote-port+
  [(rt/install-type!
    :python :remote-port
    {:type :hara/rt.remote-port
     :instance {:create remote-port/rt-remote-port:create}
     :config {:layout :full}})])



(comment
  (os/clip:nil (default-basic-client 62691))
  (def +sh+ (os/sh {:args ["python3" "-c"
                          (default-websocket-client 53644 )]
                   :wait false}))

  (def +sh+ (os/sh {:args ["python3" "-c"
                          (default-basic-client 62535)]
                   :wait false}))
  

  (spit
   "hello.py"
   (->> [(impl/emit-entry-deps
          repl/return-eval
          {:lang :python
           :layout :flat})
         (impl/emit-as
          :python +client-basic+)]
        (clojure.string/join "\n\n")))
  
  (os/sh-output +sh+))
