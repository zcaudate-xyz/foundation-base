(ns xt.lang.base-repl
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]
            [std.lib.foundation :as f]
            [std.lib.template :as template]
            [xt.lang.base-notify :as notify])
  (:refer-clojure :exclude [print]))

(l/script :xtalk
 {})

(defspec.xt ReturnCallback
  [:fn [:xt/any] :xt/any])

(defspec.xt CallbackMap
  [:xt/dict :xt/str ReturnCallback])

(defspec.xt SocketCallback
  [:fn [:xt/any :xt/any] :xt/any])

(defspec.xt SocketOptions
  [:xt/dict :xt/str :xt/any])

(defspec.xt socket-send
  [:fn [:xt/any :xt/any] :xt/any])

(defspec.xt socket-close
  [:fn [:xt/any] :xt/any])

(defspec.xt return-encode
  [:fn [:xt/any :xt/any :xt/any] :xt/str])

(defspec.xt return-wrap
  [:fn [ReturnCallback] ReturnCallback])

(defspec.xt return-eval
  [:fn [:xt/str] :xt/str])

(defspec.xt return-callbacks
  [:fn [CallbackMap :xt/str] ReturnCallback])

(defspec.xt socket-connect-base
  [:fn [:xt/str :xt/num SocketOptions SocketCallback] :xt/any])

(defspec.xt socket-connect
  [:fn [:xt/str :xt/num SocketOptions] :xt/any])

(defspec.xt notify-socket-handler
  [:fn [:xt/any :xt/str] :xt/any])

(defspec.xt notify-socket
  [:fn [:xt/str :xt/num :xt/any :xt/any :xt/any SocketOptions] :xt/any])

(defspec.xt notify-socket-http-handler
  [:fn [:xt/any :xt/str :xt/num SocketOptions :xt/str] :xt/any])

(defspec.xt notify-socket-http
  [:fn [:xt/str :xt/num :xt/any :xt/any :xt/any SocketOptions] :xt/any])

(defspec.xt notify-http
  [:fn [:xt/str :xt/num :xt/any :xt/any :xt/any SocketOptions] :xt/any])

(defspec.xt print
  [:fn [:xt/any [:xt/maybe :xt/any]] :xt/any])

(defspec.xt capture
  [:fn [:xt/any [:xt/maybe :xt/any]] :xt/any])

(defspec.xt notify
  [:fn [:xt/any [:xt/maybe :xt/any] [:xt/maybe :xt/any]] :xt/any])

(defspec.xt >notify
  [:fn [[:xt/maybe ReturnCallback]] ReturnCallback])

(defspec.xt <!
  [:fn [] CallbackMap])

;;
;; RETURN
;;

(defn.xt return-encode
  "returns the encoded"
  {:added "4.0"}
  [out id key]
  (x:return-encode out id key))

(defn.xt return-wrap
  "returns a wrapped call"
  {:added "4.0"}
  [f]
  (x:return-wrap f -/return-encode))

(defn.xt return-eval
  "evaluates a returns a string"
  {:added "4.0"}
  [s]
  (x:return-eval s -/return-wrap))

(defn.xt return-callbacks
  "constructs return callbacks"
  {:added "4.0"}
  [callbacks key]
  (var result-fn
       (fn [result]
         (if (x:has-key? callbacks key)
           (return ((x:get-key callbacks key) result))
           (return result))))
  (return result-fn))

;;
;; SOCKET
;;

(defmacro.xt socket-send
  "sends a message via the socket"
  {:added "4.0"}
  [conn input]
  (list 'x:socket-send conn input))

(defmacro.xt socket-close
  "closes the socket"
  {:added "4.0"}
  [conn]
  (list 'x:socket-close conn))

(defn.xt socket-connect-base
  "base connect call"
  {:added "4.0"}
  [host port opts cb]
  (x:socket-connect host port opts cb))

(defn.xt socket-connect
  "connects a a socket to port"
  {:added "4.0"}
  [host port opts]
  (var success-fn (-/return-callbacks opts "success"))
  (var error-fn   (-/return-callbacks opts "error"))
  (for:return [[conn err] (-/socket-connect-base host port
                                                 opts
                                                 (x:callback))]
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
  (-/socket-send conn (x:cat out"\n"))
  (-/socket-close conn))

(defn.xt notify-socket
  "notifies the socket of a value"
  {:added "4.0"}
  [host port value id key opts]
  (var out (-/return-encode value id key))
  (return (-/socket-connect
           host port
           {:success (fn [conn]
                       (-/notify-socket-handler conn out))})))

(defn.xt notify-socket-http-handler
  "helper function for `notify-socket-http`"
  {:added "4.0"}
  [conn host port opts output]
  (var #{path} (or opts {}))
  (var envelope (x:cat "POST " (or path "/") " HTTP/1.0\r\n"
                       "Host: " host ":"  (x:to-string port) "\r\n"
                       "Content-Length: " (x:to-string (x:len output)) "\r\n"
                       "\r\n"
                       output))
  (-/socket-send conn envelope)
  (-/socket-close conn))

(defn.xt notify-socket-http
  "using the base socket implementation to notify on http protocol"
  {:added "4.0"}
  [host port value id key opts]
  (var output  (-/return-encode value id key))
  (return (-/socket-connect
           host port
           {:success (fn [conn]
                       (-/notify-socket-http-handler conn host port opts output))})))

(defn.xt ^{:static/template true}
  notify-http
  "call a http notify function."
  {:added "4.0"}
  [host port value id key opts]
  (x:notify-http host port value id key opts))

(defn notify-form
  "creates the notify form"
  {:added "4.0"}
  [notify-id value meta]
  (let [{:keys [column line]}  (clojure.core/meta (l/macro-form))
        {:keys [namespace id]} (:entry (l/macro-opts))
        [rt-id port lang protocol host opts] (notify/notify-ceremony
                                              (-> (l/macro-opts)
                                                  :emit
                                                  :runtime))]
    (list (case protocol
            :socket `-/notify-socket
            :http   `-/notify-http)
          host
          port
          value
          notify-id
          [(f/strn rt-id) (merge {:column column
                                  :line line
                                  :namespace (or namespace
                                                 (str (.getName *ns*)))
                                  :id id}
                                 meta)]
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
