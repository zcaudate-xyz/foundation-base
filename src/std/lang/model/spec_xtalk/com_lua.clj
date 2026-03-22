^{:no-test true}
(ns std.lang.model.spec-xtalk.com-lua
  (:require [std.lib.template]))

;;
;; COM
;;

(defn lua-tf-x-return-encode
  ([[_ out id key]]
   (std.lib.template/$ (do (do (local ret nil)
                (local [r-ok r-err]
                       (pcall (fn []
                                (cond (== nil ~out)
                                      (:= ret (cjson.encode {:id  ~id
                                                             :key ~key
                                                             :type "data"
                                                             :value (. cjson ["null"])}))
                                      
                                      :else
                                      (:= ret (cjson.encode {:id  ~id
                                                             :key ~key
                                                             :type "data"
                                                             :value ~out}))))))
                (cond r-err
                      (return (cjson.encode {:id  ~id
                                             :key ~key
                                             :type "raw"
                                             :error (tostring r-err)
                                             :value (tostring ~out)}))
                      
                      :else
                      (return ret)))))))

(defn lua-tf-x-return-wrap
  ([[_ f encode-fn]]
   (std.lib.template/$ (do (local out)
            (local [o-ok o-err] (pcall (fn [] (:= out (~f)))))
            (cond o-err
                  (return (cjson.encode {:type "error"
                                         :value o-err}))
                  
                  :else
                  (return (~encode-fn out)))))))

(defn lua-tf-x-return-eval
  ([[_ s wrap-fn]]
   (std.lib.template/$ (return (~wrap-fn
                 (fn []
                   (local load-fn (or loadstring load))
                   (local [f err] (load-fn ~s))
                   (if err
                     (error err)
                     (return (f)))))))))

(def +lua-return+
  {:x-return-encode  {:macro #'lua-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'lua-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'lua-tf-x-return-eval     :emit :macro}})

(defn lua-tf-x-socket-connect
  ([[_ host port opts]]
   (std.lib.template/$ (do* (local [conn err])
             (if ngx
               (do (:= conn (ngx.socket.tcp))
                   (:= '[_ err]  (conn:connect ~host ~port)))
               (do (local socket (require "socket"))
                   (:= '[conn err] (socket.connect ~host ~port))))
             (return conn err)))))

(defn lua-tf-x-socket-send
  ([[_ conn s]]
   (std.lib.template/$ (. ~conn (send ~s)))))

(defn lua-tf-x-socket-close
  ([[_ conn]]
   (std.lib.template/$ (. ~conn (close)))))

(def +lua-socket+
  {:x-socket-connect      {:macro #'lua-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'lua-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'lua-tf-x-socket-close        :emit :macro}})

(defn lua-tf-x-ws-connect
  ([[_ host port opts]]
   (std.lib.template/$ (do* (var client := (require "resty.websocket.client"))
             (var [wb err] (client:new))
             (var uri (cat (or (. ~opts ["schema"])
                               "ws")
                           "://"
                           ~host
                           ":"
                           ~port
                           (or (. ~opts ["url"]) "/")))
             (var [ok err] (wb:connect uri))
             (if (not err)
               (return [true  wb])
               (return [false err]))))))

(defn lua-tf-x-ws-send
  ([[_ wb s]]
   (std.lib.template/$ (. ~wb (send-text ~s)))))

(defn lua-tf-x-ws-close
  ([[_ wb]]
   (std.lib.template/$ (. ~wb (close)))))

(def +lua-ws+
  {:x-ws-connect      {:macro #'lua-tf-x-ws-connect      :emit :macro}
   :x-ws-send         {:macro #'lua-tf-x-ws-send         :emit :macro}
   :x-ws-close        {:macro #'lua-tf-x-ws-close        :emit :macro}})

(def +lua-notify+
  {})

(defn lua-tf-x-client-basic
  ([[_ host port connect-fn eval-fn]]
   (std.lib.template/$ (do* (while true
               (var '[ok conn] := (unpack (~connect-fn ~host ~port {})))
               (when (not ok)
                 (return))
               (pcall (fn []
                        (while true
                          (local [raw err] (conn:receive "*l"))
                          (if err (break))
                          (~eval-fn conn raw)))))))))

(defn lua-tf-x-client-ws
  ([[_ host port opts connect-fn eval-fn]]
   (std.lib.template/$ (do* (var '[ok conn] := (unpack (~connect-fn ~host ~port ~opts)))
             (while true
               (local [raw type err] (conn:recv-frame))
               (when err
                 (ngx.say (cat "failed to read frame: " err))
                 (break))
               (when raw
                 (~eval-fn conn raw)))))))

(def +lua-client+
  {:x-client-basic  {:macro #'lua-tf-x-client-basic  :emit :macro}
   :x-client-ws     {:macro #'lua-tf-x-client-ws     :emit :macro}})


(defn lua-tf-x-print
  ([[_ & args]]
   (apply list 'print args)))

(defn lua-tf-x-shell
  ([[_ s cm]]
   (std.lib.template/$ (do* (var handle (io.popen ~s))
             (var res (handle:read "*a"))
             (var f (. ~cm ["success"]))
             (if f
               (return (f res))
               (return res))))))

(def +lua-shell+
  {:x-print    {:macro #'lua-tf-x-print         :emit :macro}
   :x-shell    {:macro #'lua-tf-x-shell         :emit :macro}})


(def +lua-com+
  (merge +lua-return+
         +lua-socket+
         +lua-ws+
         +lua-notify+
         +lua-client+
         +lua-shell+))

(comment

  (defn lua-tf-x-socket-server
    ([[_ port handle-line]]
     (std.lib.template/$ (do* (var socket (require "socket"))
               (var server (assert (socket.bind "*" ~port)))
               (. server (settimeout 0))
               (var connections {})

               (var handler (fn []
                              (var [raw err] (conn:receive "*l"))
                              (if err
                                (return true)
                                (do (handle-line raw)
                                    (return false)))))
               (while true
                 (var new-conn (. server (accept server)))
                 
                 (when new-conn
                   (. new-conn (settimeout 0))
                   (table.insert connections new-conn))
                 
                 (var readList (socket.select [server (unpack connections)]))
                 (for [i conn :in (ipairs readList)]
                   (if (and (not= conn server)
                            (handle-line conn))
                     ;; CLEANUP
                     (for [i v :in (ipairs connections)]
                       (if (== v conn)
                         (table.remove connections i)))))))))))
