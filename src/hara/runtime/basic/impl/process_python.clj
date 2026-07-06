(ns hara.runtime.basic.impl.process-python
  (:require [clojure.string]
            [xt.lang.common-promise]
            [hara.runtime.basic.type-basic :as basic]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.runtime.basic.type-remote-port :as remote-port]
            [hara.runtime.basic.type-verify :as type-verify]
            [hara.runtime.basic.type-websocket :as websocket]
            [std.json :as json]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.model.spec-python :as spec]
            [std.lib.os :as os]
            [xt.lang.common-lib :as lib]))

(def +python-init+
  (common/put-program-options
   :python  {:default  {:oneshot     :cpython
                        :verify      :cpython
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
                                    :extension "py"
                                    :flags   {:oneshot   ["-c"]
                                              :verify    ["-m" "py_compile"]
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
  (let [last-form (last forms)
        return-form (cond (and (seq? last-form)
                               (= ':= (first last-form)))
                          (let [[_ lhs] last-form]
                            (list 'do last-form (list 'return lhs)))

                          (and (seq? last-form)
                               (= 'var (first last-form)))
                          (let [[_ lhs] last-form]
                            (list 'do last-form (list 'return lhs)))

                          :else
                          (last (rt/return-format [last-form] '#{:- return break throw})))
        forms (concat (butlast forms) [return-form])]
    (list 'do
          (list 'defn (with-meta 'OUT-FN
                        {:inner true})
                []
                '(:- :import traceback)
                '(var err)
                (concat
                 '[try]
                 forms
                 ['(catch Exception
                      (:= err (. traceback (format-exc))))])
                '(throw (Exception err)))
          '(:= (. (globals) ["OUT"])
               (OUT-FN)))))

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
                   lib/return-eval
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

(def +python-verify-config+
  (common/set-context-options
   [:python :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +python-oneshot+
  [(rt/install-type!
    :python :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +python-verify+
  [(rt/install-type!
    :python :verify
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
           (let [buf (bytearray)
                 ch (conn.recv 1)
                 _  (if (== ch (:% b ""))
                      (break))
                 _  (while (not= ch (:% b "\n"))
                      (. buf (extend ch))
                      (:= ch (conn.recv 1)))
                 l  (. buf (decode "utf-8"))]
             (cond (== l "<PING>")
                   (pass)
                   
                   :else
                   (let [input (json.loads l)
                         out   (return-eval input)]
                     (conn.sendall (. out (encode "utf-8")))
                     (conn.sendall (:% b "\n"))))))))])

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [bootstrap (->> [(impl/emit-entry-deps
                         lib/return-eval
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
                          lib/return-eval
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
          lib/return-eval
          {:lang :python
           :layout :flat})
         (impl/emit-as
          :python +client-basic+)]
        (clojure.string/join "\n\n")))
  
  (os/sh-output +sh+))
