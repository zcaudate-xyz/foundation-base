(ns std.lang.model.spec-xtalk.com-js
  (:require [std.lib :as h]))

;;
;; COM
;;

(defn js-tf-x-return-encode
  ([[_ out id key]]
   (h/$ (do (var type-fn (fn [x]
                           (let [name (typeof x)]
                             (return (:? (== name "object") (:? x x.constructor.name name) name)))))
            (var tb (typeof ~out))
            (cond (== "function" tb)
                  (return (JSON.stringify {:id     ~id
                                           :key    ~key
                                           :type   "raw"
                                           :return "function"
                                           :value  (. ~out (toString))}))
                  
                  
                  (not= "object" tb)
                  (return (JSON.stringify {:id     ~id
                                           :key    ~key
                                           :type "data"
                                           :return tb
                                           :value ~out}))

                  (== nil ~out)
                  (return (JSON.stringify {:id     ~id
                                           :key    ~key
                                           :type "data"
                                           :return "nil"
                                           :value ~out}))
                  
                  :else
                  (do (var ts (type-fn ~out))
                      (try
                        (if (or (== ts "Object")
                                (== ts "Array"))
                          (return (JSON.stringify {:id     ~id
                                                   :key    ~key
                                                   :type "data"
                                                   :value ~out}))
                          (return (JSON.stringify {:id     ~id
                                                   :key    ~key
                                                   :type  "raw"
                                                   :return ts
                                                   :value (. ~out (toString))})))
                        (catch e (return (JSON.stringify {:id     id
                                                          :key    key
                                                          :type   "raw"
                                                          :return ts
                                                          :value (. ~out (toString))}))))))))))

(defn js-tf-x-return-wrap
  ([[_ f encode-fn]]
   (h/$ (try (var out := (~f))
             (return (~encode-fn  out))
             (catch e (let [err (:? (== "string" (typeof e)) e {:message (. e ["message"]) :stack (. e ["stack"])})]
                        (return (JSON.stringify {:type "error"
                                                 :value err}))))))))

(defn js-tf-x-return-eval
  ([[_ s wrap-fn]]
   (h/$ (return (~wrap-fn
                 (fn []
                   (return (eval ~s))))))))

(def +js-return+
  {:x-return-encode  {:macro #'js-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'js-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'js-tf-x-return-eval     :emit :macro}})

(defn js-tf-x-socket-connect
  ([[_ host port opts cb]]
   (h/$ (do* (var net (eval "require('net')"))
             (var rl  (eval  "require('readline')"))
             (var conn (new net.Socket))
             (return (conn.connect
                      port host (fn []
                                  (cb nil conn))))))))

(defn js-tf-x-socket-send
  ([[_ conn s]]
   (h/$ (. ~conn (write ~s)))))

(defn js-tf-x-socket-close
  ([[_ conn]]
   (h/$ (. ~conn (end)))))

(def +js-socket+
  {:x-socket-connect      {:macro #'js-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'js-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'js-tf-x-socket-close        :emit :macro}})

(defn js-tf-x-ws-connect
  ([[_ host port opts]]
   (h/$ (do (var WS := (or (!:G WebSocket)
                           (require "ws")))
            (var schema (or (. ~opts ["schema"]) "ws"))
            (var url    (or (. ~opts ["url"]) "/"))
            (var conn (new WS (+ schema "://" host ":" port url)))
            (return [true conn])))))

(defn js-tf-x-ws-send
  ([[_ wb s]]
   (h/$ (. ~wb (send ~s)))))

(defn js-tf-x-ws-close
  ([[_ wb]]
   (h/$ (. ~wb (close)))))

(def +js-ws+
  {:x-ws-connect      {:macro #'js-tf-x-ws-connect      :emit :macro}
   :x-ws-send         {:macro #'js-tf-x-ws-send         :emit :macro}
   :x-ws-close        {:macro #'js-tf-x-ws-close        :emit :macro}})

(defn js-tf-x-notify-socket
  ([[_ host port value id key
     connect-fn
     encode-fn]]
   (h/$ (try
          (var [ok conn] (~connect-fn host port
                          {:cb  (fn []
                                  (client.end (~encode-fn ~value ~id ~key)
                                              "utf8"))}))
          (return ["async"])
          (catch e (return ["unable to connect"]))))))

(defn js-tf-x-notify-http
  ([[_ host port value id key encode-fn]]
   (h/$ (try
          (fetch (+ "http://" ~host ":" ~port)
                 {:method "POST"
                  :body (~encode-fn ~value ~id ~key)})
         (return ["async"])
         (catch e (return ["unable to connect"]))))))

(def +js-notify+
  {:x-notify-socket  {:macro #'js-tf-x-notify-socket    :emit :macro}
   :x-notify-http    {:macro #'js-tf-x-notify-http    :emit :macro}})

(defn js-tf-x-client-basic
  ([[_ host port connect-fn eval-fn]]
   (h/$ (do (var [ok conn] (~connect-fn host port {}))
            (var rl  (require "readline"))
            (var stream  (rl.createInterface conn
                                             conn))
            (stream.on "line" (fn [line] (~eval-fn conn line)))))))

(defn js-tf-x-client-ws
  [[_ host port opts connect-fn eval-fn]]
  (h/$ (do (var [ok conn] (~connect-fn ~host ~port {}))
           (:=  (. conn onmessage)
                (fn [msg]
                  (~eval-fn conn msg.data))))))

(def +js-client+
  {:x-client-basic  {:macro #'js-tf-x-client-basic  :emit :macro}
   :x-client-ws     {:macro #'js-tf-x-client-ws     :emit :macro}})

(defn js-tf-x-print
  ([[_ & args]]
   (apply list 'console.log args)))

(defn js-tf-x-shell
  ([[_ s cm]]
   (h/$ (do (var p (require "child_process"))
            (p.exec ~s (fn [err res]
                         (if err
                           (if (. ~cm ["error"])
                             (return (. ~cm (error err))))
                           (if (. ~cm ["success"])
                             (return (. ~cm (success res)))))))
            (return ["async"])))))

(def +js-shell+
  {:x-print    {:macro #'js-tf-x-print         :emit :macro}
   :x-shell    {:macro #'js-tf-x-shell         :emit :macro}})

(def +js-com+
  (merge +js-return+
         +js-socket+
         +js-ws+
         +js-notify+
         +js-client+
         +js-shell+))
