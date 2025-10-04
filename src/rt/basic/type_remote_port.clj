(ns rt.basic.type-remote-port
  (:require [std.protocol.context :as protocol.context]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lib :as h :refer [defimpl]]
            [std.json :as json]
            [std.concurrent :as cc]
            [std.string :as str]
            [rt.basic.type-common :as common]))

(defn start-remote-port
  "starts the connection to the remote port"
  {:added "4.0"}
  ([{:keys [host port] :as rt}]
   (let [relay (cc/relay
                 {:type :socket
                  :host (or host "localhost")
                  :port (or port (h/error "Missing Port"
                                          {:host host
                                           :port port}))})]
     (assoc rt :relay relay))))

(defn stop-remote-port
  "stops the connection to the remote port"
  {:added "4.0"}
  [{:keys [id lang bench container] :as rt}]
  (let [{:keys [relay]} rt
        _ (when relay
            (h/stop relay))]
    (dissoc rt :relay)))

(defn raw-eval-remote-port-relay
  "evaluates over the remote port"
  {:added "4.0"}
  [rt body & [timeout]]
  (h/prn body)
  (let [{:keys [relay
                encode]} rt
        {:keys [^java.net.Socket socket]} relay]
    (cond relay
          (try (let [{:keys [output]}  @(cc/send relay
                                                 {:op :line
                                                  :line ((or (:write encode)
                                                             std.json/write)
                                                         body)
                                                  :timeout (or timeout 1000)})
                     ret  (if output
                            ((or (:read encode)
                                 std.json/read) output)
                            {:status "timeout"
                             :connected (try (-> ^java.net.Socket (:attached relay)
                                                 (.getOutputStream)
                                                 (.write (.getBytes "<PING>\n")))
                                             true
                                             (catch Throwable t
                                               (h/stop relay)
                                               false))})]
                 (cond (= (get ret "type")
                          "data")
                       (get ret "value")
                       
                       (= (get ret "type")
                          "error")
                       (throw (ex-info (get ret "message")
                                       {:data (get ret "value")}))
                       
                       :else
                       ret))
               (catch com.fasterxml.jackson.databind.exc.MismatchedInputException e
                 (h/stop relay)
                 (if socket (.close socket)))
               (catch java.net.SocketException e
                 (h/stop relay)
                 (if socket (.close socket))))
          
          :else
          {:status "not-connected"})))

(defn raw-eval-remote-port
  "evaluates over the remote port"
  {:added "4.0"}
  ([{:keys [id lang process raw-eval] :as rt} body]
   ((or raw-eval raw-eval-remote-port-relay)
    rt body (:timeout process))))

(defn invoke-ptr-remote-port
  "invokes over the remote port"
  {:added "4.0"}
  ([{:keys [process lang layout] :as rt} ptr args]
   (default/default-invoke-script rt ptr args raw-eval-remote-port process)))

(defn rt-remote-port-string
  "gets the remote port string"
  {:added "4.0"}
  [{:keys [id lang host port]}]
  (str "#rt.remote-port"
       [lang :remote-port
        (or host "localhost")
        port]))

(defimpl RuntimeRemote [id]
  :string rt-remote-port-string
  :protocols [std.protocol.component/IComponent
              :suffix "-remote-port"
              :method {-kill stop-remote-port}
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    raw-eval-remote-port
                       -invoke-ptr  invoke-ptr-remote-port}])

(defn rt-remote-port:create
  "creates the service"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           process] :as m
    :or {runtime :remote-port}}]
  (let [process (h/merge-nested {:encode :json}
                                (common/get-options lang :remote-port :default)
                                process)]
    (h/prn {:lang lang
            :process process})
    (map->RuntimeRemote (merge  m
                                {:id (or id (h/sid))
                                 :tag runtime
                                 :runtime runtime
                                 :process process
                                 :lifecycle process}))))

(defn rt-remote-port
  "create and starts the service"
  {:added "4.0"}
  [{:keys [id
           lang
           runtime
           program
           process] :as m}]
  (-> (rt-remote-port:create m)
      (h/start)))

(comment
  (./import)
  )
