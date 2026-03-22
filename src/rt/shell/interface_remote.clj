^{:no-test true}
(ns rt.shell.interface-remote
  (:require [rt.shell.interface-basic :as basic]
            [std.concurrent :as cc]
            [std.lang :as l]
            [std.lang.base.impl :as impl]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lang.base.util :as ut]
            [std.lang.interface.type-notify :as notify]
            [std.lib.atom]
            [std.lib.component]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.impl :refer [defimpl]]
            [std.lib.os]
            [std.lib.template]
            [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [std.string.common]))

(def ^:dynamic *remote-id* nil)

(def ^:dynamic *remote-port* nil)

(defn- format-form
  [id port form]
  (let [address (str "/dev/tcp/127.0.0.1/" port)
        fstr  (str "'{id: \"" id  "\", data:.}'")]
    (std.lib.template/$ [(exec (:% 3 <> ~address))
          [~form
           | (jq {:R true
                  :s true}
                     ~fstr)
           (:% >& 3)]
          (exec (:% 3 <&-))])))

(defn remote-body-transform
  "transforms the input"
  {:added "4.0"}
  [input mopts]
  (default/return-transform
   input mopts
   {:format-fn identity
    :wrap-fn (fn [forms]
               (apply list 'do
                      (concat
                       (butlast forms)
                       (format-form *remote-id*
                                    *remote-port*
                                    (last forms)))))}))

(defn raw-eval-remote
  "performs a raw eval on the remote side"
  {:added "4.0"}
  [{:keys [id tx-id last client] :as rt} body]
  (let [port (:socket-port (l/default-notify))
        [_ p] (notify/watch-oneshot (l/default-notify)
                                    2000
                                    (or tx-id id))]
    (reset! last (basic/raw-eval-basic client body))
    (deref p 2000 :timeout)))

(defn invoke-ptr-remote
  "invokes a pointer on the remote side"
  {:added "4.0"}
  [{:keys [id client last] :as rt} ptr args]
  (let [port  (:socket-port (l/default-notify))
        tx-id (str id "--" (std.lib.foundation/sid))
        body  (binding [*remote-id* tx-id
                        *remote-port* port]
                (ptr/ptr-invoke-script ptr args
                                       {:emit {:body  {:transform #'remote-body-transform}}}))
        return (ptr/ptr-invoke (assoc rt :tx-id tx-id)
                               raw-eval-remote
                               body
                               {}
                               false)
        output      (str @last
                         (basic/raw-eval-basic client nil))]
    
    (if (not-empty output)
      (std.lib.env/pl output))
    (or (if-let [s (get return "data")]
          (std.string.common/trim-right s))
        return)))

(defn start-shell-remote-tunnel
  "starts the remote tunnes"
  {:added "4.0"}
  [{:keys [server tunnel]}]
  (or @tunnel
      (reset! tunnel
              (let [port (:socket-port (notify/default-notify))]
                (std.lib.os/sh {:args ["ssh" "-N" server "-R" (str port ":localhost:" port) "-C"]
                       :wait false})))))

(defn start-shell-remote
  "starts the shell"
  {:added "4.0"}
  [{:keys [server client] :as rt}]
  (let [_ (start-shell-remote-tunnel rt)
        _ (std.lib.component/start client)
        s (basic/raw-eval-basic client (str "ssh " server))
        _ (loop [n 10]
            (cond (neg? n)
                  :timeout
                  
                  (not-empty (basic/raw-eval-basic client "echo ${BASH_VERSION}"))
                  (basic/raw-eval-basic client nil)
                  

                  :else
                  (do (Thread/sleep 500)
                      (recur (dec n)))))]
    rt))

(defn stop-shell-remote
  "stops the shell"
  {:added "4.0"}
  [{:keys [tunnel client] :as rt}]
  (let [_ (std.lib.component/stop client)
        _ (std.lib.atom/swap-return! tunnel (fn [sh]
                                   [sh (if sh (std.lib.os/sh-exit sh))]))]
    rt))

(defn- shell-remote-string
  [{:keys [id lang]}]
  (str "#shell.remote" [id]))

(defimpl ShellRemote [relay error]
  :string shell-remote-string
  :protocols [protocol.component/IComponent
              :method {-start start-shell-remote
                       -stop  stop-shell-remote
                       -kill  stop-shell-remote}
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    raw-eval-remote
                       -invoke-ptr  invoke-ptr-remote}])

(defn shell-remote:create
  "creates the shell remote"
  {:added "4.0"}
  ([{:keys [id server client] :as m
     :or {id (std.lib.foundation/sid)}}]
   (map->ShellRemote (assoc m
                            :id id
                            :last   (atom nil)
                            :tunnel (atom nil)
                            :client (basic/shell-basic client)))))

(defn shell-remote
  "create and starts a shell remote"
  {:added "4.0"}
  ([]
   (shell-remote {}))
  ([m]
   (-> (shell-remote:create m)
       (std.lib.component/start))))

(def +bash-basic+
  [(default/install-type!
    :bash :remote
    {:type :hara/rt.shell.remote
     :instance {:create #'shell-remote:create}
     :config {:layout :full}})])
