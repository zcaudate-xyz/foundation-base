(ns xt.lang.common-repl
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]
            [xt.lang.common-notify :as notify])
  (:refer-clojure :exclude [print]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-link :as xt-link]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-lib :as xt-lib]]})

(defn.xt notify-with-promise
  "promise-compatible socket notification"
  {:added "4.1"}
  [notify-fn host port value id key opts]
  (if (not (promise/x:promise-native? value))
    (return
     (notify-fn host port value id key opts))
    (-> value
        (promise/x:promise-then
         (fn [out]
           (return
            (-/notify-with-promise notify-fn host port out id key opts))))
        (promise/x:promise-catch
         (fn [err]
           (return
            (notify-fn host port err id key opts)))))))

;;
;; SOCKET
;;



(defn.xt socket-connect-base
  [host port opts cb]
  (return
   (xt-link/x:socket-connect host port opts cb)))

(defn.xt socket-connect
  "connects a a socket to port"
  {:added "4.0"}
  [host port opts]
  (var success-fn (xt-lib/wrap-callback opts "success"))
  (var error-fn   (xt-lib/wrap-callback opts "error"))
  (var callback-fn
       (fn [err out]
         (if (xt/x:not-nil? err)
           (return (xt/x:apply error-fn [err]))
           (return (xt/x:apply success-fn [out])))))
  (return
   (-/socket-connect-base  host
                           port
                           opts
                           callback-fn)))

;;
;; NOTIFY
;;

(defn.xt notify-socket-handler
  "helper function for `notify-socket`"
  {:added "4.0"}
  [conn out]
  (xt-link/x:socket-send conn (xt/x:cat out"\n"))
  (return
   (xt-link/x:socket-close conn)))

(defn.xt notify-socket
  "notifies the socket of a value"
  {:added "4.0"}
  [host port value id key opts]
  (var out (xt-lib/return-encode value id key))
  (return (-/socket-connect
           host port
           {:success (fn [conn]
                       (return (-/notify-socket-handler conn out)))})))

(defn.xt notify-socket-full
  "promise-compatible socket notification"
  {:added "4.1"}
  [host port value id key opts]
  (return
   (-/notify-with-promise -/notify-socket host port value id key opts)))

(defn.xt notify-socket-http-handler
  "helper function for `notify-socket-http`"
  {:added "4.0"}
  [conn host port opts output]
  (xt-link/x:socket-send
   conn
   (xt/x:cat "POST "
             (:? (xt/x:nil? (:? (xt/x:nil? opts)
                                nil
                                (xt/x:get-key opts "path" nil)))
                 "/"
                 (:? (xt/x:nil? opts)
                     nil
                     (xt/x:get-key opts "path" nil)))
             " HTTP/1.0\r\n"
             "Host: " host ":"  (xt/x:to-string port) "\r\n"
             "Content-Length: " (xt/x:to-string (xt/x:str-len output)) "\r\n"
             "\r\n"
             output))
  (return
   (xt-link/x:socket-close conn)))

(defn.xt notify-socket-http
  "using the base socket implementation to notify on http protocol"
  {:added "4.0"}
  [host port value id key opts]
  (var output  (xt-lib/return-encode value id key))
  (return (-/socket-connect
           host port
           {:success (fn [conn]
                       (return (-/notify-socket-http-handler conn host port opts output)))})))

(defn.xt notify-http
  "call a http notify function."
  {:added "4.0"}
  [host port value id key opts]
  (return
   (xt-link/x:notify-http host port value id key opts)))

(defn.xt notify-http-full
  "promise-compatible http notification"
  {:added "4.1"}
  [host port value id key opts]
  (return
   (-/notify-with-promise -/notify-http host port value id key opts)))

(defn notify-form
  "creates the notify form"
  {:added "4.0"}
  [notify-id value meta]
  (let [{:keys [column line]}  (clojure.core/meta (l/macro-form))
        {:keys [namespace id]} (:entry (l/macro-opts))
        [rt-id port lang protocol host opts] (notify/notify-ceremony
                                              (-> (l/macro-opts)
                                                  :emit
                                                  :runtime))
        key  [(f/strn rt-id) (merge {:column column
                                     :line line
                                     :namespace (or namespace
                                                    (str (.getName *ns*)))
                                     :id id}
                                    meta)]]
    (list (case protocol
            :socket `-/notify-socket-full
            :http   `-/notify-http-full)
          host
          port
          value
          notify-id
          key
          opts)))

(defmacro.xt ^{:standalone true}
  print
  "creates the print op"
  {:added "4.0"}
  [value & [data]]
  (xt.lang.common-repl/notify-form "print" value {:data data}))

(defmacro.xt ^{:standalone true}
  capture
  "creats the capture op"
  {:added "4.0"}
  [value & [tag]]
  (xt.lang.common-repl/notify-form "capture" value {:tag tag}))


(defmacro.xt ^{:standalone true}
  notify
  "sends a message to the notify server"
  {:added "4.0"}
  [value & [id tag]]
  (notify-form (or id
                   notify/*override-id*
                   (f/error "No ID for Notify"))
               value {:tag tag}))


(defmacro.xt ^{:standalone true}
  >notify
  "creates a callback function"
  {:added "4.0"}
  ([] (list 'fn '[val]
            (list 'return
                  (list 'xt.lang.common-repl/notify 'val))))
  ([f]
   (list 'fn '[val]
         (list 'return
               (list 'xt.lang.common-repl/notify 'val f)))))

(defmacro.xt ^{:standalone true}
  <!
  "creates a callback map"
  {:added "4.0"}
  []
  ''({:success (fn [val]
                 (return (xt.lang.common-repl/notify val)))
      :error   (fn [err]
                 (return (xt.lang.common-repl/notify err)))}))
