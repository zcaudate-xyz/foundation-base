(ns xt.lang.common-repl
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]
            [std.lib.foundation :as f]
            [std.lib.template :as template]
            [xt.lang.common-notify :as notify])
  (:refer-clojure :exclude [print]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-link :as xt-link]
             [xt.lang.common-lib :as xt-lib]]})

;;
;; SOCKET
;;

(defn.xt socket-connect
  "connects a a socket to port"
  {:added "4.0"}
  [host port opts]
  (var success-fn (xt-lib/wrap-callback opts "success"))
  (var error-fn   (xt-lib/wrap-callback opts "error"))
  (for:return [[conn err] (xt-link/x:socket-connect  host port
                                                     opts
                                                     (xt/x:callback))]
              {:success (return (success-fn conn))
     :error   (return (error-fn err))
     :final   true}))

;;
;; NOTIFY
;;

(defn.xt notify-socket-handler
  "helper function for `notify-socket`"
  {:added "4.0"}
  [conn out]
  (xt-link/x:socket-send conn (xt/x:cat out"\n"))
  (xt-link/x:socket-close conn))

(defn.xt notify-socket
  "notifies the socket of a value"
  {:added "4.0"}
  [host port value id key opts]
  (var out (xt-lib/return-encode value id key))
  (return (-/socket-connect
           host port
           {:success (fn [conn]
                       (return (-/notify-socket-handler conn out)))})))

(defn.xt notify-socket-http-handler
  "helper function for `notify-socket-http`"
  {:added "4.0"}
  [conn host port opts output]
  (var resolved-opts (:? (xt/x:nil? opts) {} opts))
  (var #{path} resolved-opts)
  (var endpoint (:? (xt/x:nil? path) "/" path))
  (var envelope (xt/x:cat "POST " endpoint " HTTP/1.0\r\n"
                       "Host: " host ":"  (xt/x:to-string port) "\r\n"
                       "Content-Length: " (xt/x:to-string (xt/x:len output)) "\r\n"
                       "\r\n"
                       output))
  (xt-link/x:socket-send conn envelope)
  (xt-link/x:socket-close conn))

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
  (xt-link/x:notify-http host port value id key opts))

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
            :socket `-/notify-socket
            :http   `-/notify-http)
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
  (notify-form "print" value {:data data}))

(defmacro.xt ^{:standalone true}
  capture
  "creats the capture op"
  {:added "4.0"}
  [value & [tag]]
  (notify-form "capture" value {:tag tag}))

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
  [& [f]]
  (template/$ (fn [val]
                (return (xt.lang.base-repl/notify ~(if f
                                                     (list f 'val)
                                                     'val))))))

(defmacro.xt ^{:standalone true}
  <!
  "creates a callback map"
  {:added "4.0"}
  []
  ''({:success (fn [val]
                 (return (xt.lang.base-repl/notify val)))
      :error   (fn [err]
                 (return (xt.lang.base-repl/notify err)))}))
