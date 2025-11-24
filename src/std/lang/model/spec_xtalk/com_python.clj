(ns std.lang.model.spec-xtalk.com-python
  (:require [std.lib :as h]))

(defn python-tf-x-return-encode
  ([[_ out id key]]
   (h/$ (do (:- :import json)
            (try
              (return (json.dumps {:id  ~id
                                   :key ~key
                                   :type  "data"
                                   :value  ~out}))
              (catch Exception
                  (return (json.dumps {:id ~id
                                       :key ~key
                                       :type  "raw"
                                       :value (str ~out)}))))))))

(defn python-tf-x-return-wrap
  ([[_ f encode-fn]]
   (h/$ (do (:- :import json)
            (try (:= out (~f))
                 (catch [Exception :as e]
                     (return (json.dumps {:type "error"
                                          :value (str e)}))))
            (return (~encode-fn out nil nil))))))

(defn python-tf-x-return-eval
  ([[_ s wrap-fn]]
   (h/$ (do (fn thunk []
              (let [g   (globals)]
                (exec ~s g g)
                (return (g.get "OUT"))))
            (return (~wrap-fn thunk))))))

(def +python-return+
  {:x-return-encode  {:macro #'python-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'python-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'python-tf-x-return-eval     :emit :macro}})

(defn python-tf-x-socket-connect
  ([[_ host port opts]]
   (h/$ (do (:- :import socket)
            (var conn   (socket.socket))
            (conn.connect '(host port))
            (return conn)))))

(defn python-tf-x-socket-send
  ([[_ conn s]]
   (h/$ (. ~conn (sendall (. ~s (encode)))))))

(defn python-tf-x-socket-close
  ([[_ conn]]
   (h/$ (. ~conn (close)))))

(def +python-socket+
  {:x-socket-connect      {:macro #'python-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'python-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'python-tf-x-socket-close        :emit :macro}})

(defn python-tf-x-ws-connect
  ([[_ host port opts]]
   (h/$ (do (:- :import json)
            (:- :import websocket)
            (let [schema (:? (hasattr ~opts "schema") (getattr (unquote opts) "schema") "ws")
                  url    (:? (hasattr ~opts "url") (getattr (unquote opts) "url") "")
                  conn   (websocket.WebSocketApp
                          (+ schema "://" ~host ":" (str ~port) url)
                          :on-message (. ~opts ["cb"]))]
              (return [true conn]))))))

(defn python-tf-x-ws-send
  ([[_ wb s]]
   (h/$ (. ~wb (send ~s)))))

(defn python-tf-x-ws-close
  ([[_ wb]]
   (h/$ (. ~wb (close)))))

(def +python-ws+
  {:x-ws-connect      {:macro #'python-tf-x-ws-connect      :emit :macro}
   :x-ws-send         {:macro #'python-tf-x-ws-send         :emit :macro}
   :x-ws-close        {:macro #'python-tf-x-ws-close        :emit :macro}})

(def +python-notify+
  {})

(defn python-tf-x-client-basic
  ([[_ host port connect-fn eval-fn]]
   (h/$ (do (:= '[ok conn] (~connect-fn ~host ~port {}))
          (while true
            (let [raw ""
                  ch (conn.recv 1)
                  _  (if (== ch (:% b ""))
                       (break))
                  _  (while (not= ch (:% b "\n"))
                       (:+= raw (ch.decode))
                       (:= ch (conn.recv 1)))]
              (~eval-fn conn raw)))))))

(defn python-tf-x-client-ws
  ([[_ host port opts connect-fn eval-fn]]
   (h/$ (do (:= '[ok conn] (~connect-fn ~host ~port {:cb ~eval-fn}))
            (:- :import threading)
            (-> (. threading
                   (Thread
                    :target (fn [] (conn.run-forever)))
                   (start)))))))

(def +python-client+
  {:x-client-basic  {:macro #'python-tf-x-client-basic  :emit :macro}
   :x-client-ws     {:macro #'python-tf-x-client-ws     :emit :macro}})

(defn python-tf-x-print
  ([[_ & args]]
   (apply list 'print args)))

(defn python-tf-x-shell
  ([[_ s cm]]
   (h/$ (do (var res (. (__import__ "os") (system ~s)))
            (var f (. ~cm (get "success")))
            (if f
              (return (f res))
              (return res))))))

(def +python-shell+
  {:x-print    {:macro #'python-tf-x-print         :emit :macro}
   :x-shell    {:macro #'python-tf-x-shell         :emit :macro}})

(def +python-com+
  (merge +python-return+
         +python-socket+
         +python-ws+
         +python-notify+
         +python-client+
         +python-shell+))
