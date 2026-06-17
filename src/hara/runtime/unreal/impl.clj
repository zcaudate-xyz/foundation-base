(ns hara.runtime.unreal.impl
    (:require [std.json :as json]
              [std.lib.component :as component]
              [std.lib.foundation :as f]
              [std.lib.impl :as std-impl]
              [hara.lang.runtime :as rt]
              [hara.lang.type-shared :as shared]
              [hara.runtime.basic.impl.process-python :as process-python])
    (:import [java.io ByteArrayOutputStream InputStream]
             [java.net DatagramPacket DatagramSocket InetAddress InetSocketAddress MulticastSocket ServerSocket Socket]
             [java.util UUID]))

(def +protocol+
     "Unreal Python Remote Execution protocol constants."
     {:version 1
      :magic "ue_py"
      :multicast-group "239.0.0.1"
      :multicast-port 6766
      :bind-address "0.0.0.0"
      :command-host "127.0.0.1"})

;;
;; MESSAGES
;;

(defn make-message
      "Builds a remote execution protocol message."
      {:added "4.1"}
      [type source & [dest data]]
      (cond-> {:version (:version +protocol+)
               :magic (:magic +protocol+)
               :type type
               :source source}
              dest (assoc :dest dest)
              data (assoc :data data)))

(defn parse-message
      "Parses a remote execution JSON message."
      {:added "4.1"}
      [s]
      (json/read s json/+keyword-mapper+))

(defn make-node-id
      "Generates a new node id."
      {:added "4.1"}
      []
      (.toString (UUID/randomUUID)))

;;
;; UDP
;;

(defn send-udp
      "Sends a JSON message over a UDP socket."
      {:added "4.1"}
      [^DatagramSocket socket message ^String host ^Integer port]
      (let [bytes (.getBytes ^String (json/write message) "UTF-8")
            packet (DatagramPacket. bytes (alength bytes)
                                    (InetAddress/getByName host) port)]
           (.send socket packet)))

(defn recv-udp
      "Receives a single UDP packet as a UTF-8 string, blocking up to timeout-ms."
      {:added "4.1"}
      [^DatagramSocket socket timeout-ms]
      (.setSoTimeout socket timeout-ms)
      (let [buf (byte-array 8192)
            packet (DatagramPacket. buf (alength buf))]
           (.receive socket packet)
           (String. buf 0 (.getLength packet) "UTF-8")))

(defn discover-node
      "Discovers a running Unreal Editor instance via UDP multicast ping/pong."
      {:added "4.1"}
      [{:keys [multicast-group multicast-port bind-address discovery-timeout]
        :or {multicast-group (:multicast-group +protocol+)
             multicast-port (:multicast-port +protocol+)
             bind-address (:bind-address +protocol+)
             discovery-timeout 5000}}]
      (let [source (make-node-id)
            socket (doto (MulticastSocket. multicast-port))
            group (InetAddress/getByName multicast-group)]
           (.joinGroup socket group)
           (send-udp socket (make-message "ping" source) multicast-group multicast-port)
           (try
            (loop [deadline (+ (System/currentTimeMillis) discovery-timeout)]
                  (let [remaining (- deadline (System/currentTimeMillis))]
                       (if (pos? remaining)
                           (let [raw (recv-udp socket remaining)
                                 msg (parse-message raw)]
                                (if (and (= "pong" (:type msg))
                                         (not= source (:source msg))
                                         (or (nil? (:dest msg)) (= source (:dest msg))))
                                    {:node-id (:source msg)
                                     :data (:data msg)
                                     :source source}
                                    (recur deadline)))
                           (f/error "No Unreal node discovered"
                                    {:multicast multicast-group
                                     :port multicast-port
                                     :timeout discovery-timeout}))))
            (finally
             (.leaveGroup socket group)
             (.close socket)))))

;;
;; TCP COMMAND CONNECTION
;;

(defn- read-json-object
       "Reads bytes from an InputStream until a complete JSON object is parsed."
       {:added "4.1"}
       [^InputStream input]
       (let [baos (ByteArrayOutputStream.)
             buf (byte-array 1)]
            (loop []
                  (let [n (.read input buf)]
                       (when (neg? n)
                             (throw (ex-info "Unreal command stream closed" {})))
                       (.write baos buf 0 1)
                       (let [s (.toString baos "UTF-8")]
                            (or (try (json/read s json/+keyword-mapper+) (catch Throwable _ nil))
                                (recur)))))))

(defn start-command-server
      "Starts a local TCP server and asks Unreal to connect to it."
      {:added "4.1"}
      [{:keys [node-id source-id multicast-group multicast-port
               command-host command-port open-retries open-retry-delay]
        :or {open-retries 6
             open-retry-delay 500}}]
      (let [multicast-group (or multicast-group (:multicast-group +protocol+))
            multicast-port  (or multicast-port  (:multicast-port +protocol+))
            command-host    (or command-host    (:command-host +protocol+))
            ^ServerSocket server (ServerSocket. (or command-port 0))
            port (.getLocalPort server)]
           (try
            (let [^Socket socket (loop [attempt 0]
                                       (when (>= attempt open-retries)
                                             (throw (ex-info "Unreal did not open command connection"
                                                             {:node-id node-id :port port})))
                                       (.setSoTimeout server open-retry-delay)
                                       (send-udp (DatagramSocket.)
                                                 (make-message "open_connection" source-id node-id
                                                               {:command_ip command-host
                                                                :command_port port})
                                                 multicast-group multicast-port)
                                       (let [conn (try
                                                    (.accept ^ServerSocket server)
                                                    (catch java.net.SocketTimeoutException _
                                                           ::timeout))]
                                            (if (= conn ::timeout)
                                              (recur (inc attempt))
                                              (do (.setSoTimeout ^Socket conn 0)
                                                  conn))))]
                 {:server server
                  :socket socket
                  :input (.getInputStream socket)
                  :output (.getOutputStream socket)})
            (catch Throwable t
                   (.close server)
                   (throw t)))))

;;
;; PROCESS
;;

(defn start-unreal
      "Starts an Unreal runtime by discovering a node and opening a command channel."
      {:added "4.1"}
      [{:keys [id node-id multicast-group multicast-port bind-address
               discovery-timeout command-port]
        :as rt}]
      (let [source (make-node-id)
            {discovered-node-id :node-id
             node-data :data
             discovered-source :source}
            (when-not node-id
              (discover-node (cond-> {}
                               multicast-group    (assoc :multicast-group multicast-group)
                               multicast-port     (assoc :multicast-port multicast-port)
                               bind-address       (assoc :bind-address bind-address)
                               discovery-timeout  (assoc :discovery-timeout discovery-timeout))))
            source (or discovered-source source)
            discovered-node-id (or discovered-node-id node-id)
            {:keys [server socket input output]}
            (start-command-server (merge (dissoc rt :multicast-group :multicast-port
                                                    :bind-address :discovery-timeout)
                                         {:node-id discovered-node-id
                                          :source-id source
                                          :command-port command-port}))]
           (assoc rt
                  :id (or id (f/sid))
                  :node-id discovered-node-id
                  :node-data node-data
                  :source source
                  :server server
                  :socket socket
                  :input input
                  :output output
                  :lock (Object.))))

(defn stop-unreal
      "Closes the Unreal command channel."
      {:added "4.1"}
      [{:keys [^ServerSocket server ^Socket socket node-id source-id
               multicast-group multicast-port]
        :as rt}]
      (when (and node-id source-id)
            (try
             (send-udp (DatagramSocket.)
                       (make-message "close_connection" source-id node-id)
                       (or multicast-group (:multicast-group +protocol+))
                       (or multicast-port (:multicast-port +protocol+)))
             (catch Throwable _)))
      (when socket
            (try (.close socket) (catch Throwable _)))
      (when server
            (try (.close server) (catch Throwable _)))
      rt)

;;
;; EVAL
;;

(defn raw-eval-unreal
      "Sends Python code to Unreal and returns the command result."
      {:added "4.1"}
      [rt code]
      (locking (:lock rt)
               (let [msg (make-message "command" (:source rt) (:node-id rt)
                                       {:command code
                                        :unattended true
                                        :exec_mode "ExecuteStatement"})
                     bytes (.getBytes ^String (json/write msg) "UTF-8")]
                    (doto ^java.io.OutputStream (:output rt)
                          (.write bytes)
                          (.flush))
                    (let [response (read-json-object ^InputStream (:input rt))
                          data (:data response)]
                         (if (:success data)
                             (:result data)
                             (throw (ex-info "Unreal Python command failed"
                                             {:code code
                                              :result (:result data)
                                              :output (:output data)})))))))

(defn invoke-ptr-unreal
      "Invokes a pointer in the Unreal runtime."
      {:added "4.1"}
      ([rt ptr args]
       (rt/default-invoke-script rt ptr args raw-eval-unreal
                                 {:main  {}
                                  :emit  {:body  {:transform #'process-python/default-body-transform}}
                                  :json  :full
                                  :encode :json})))

;;
;; RUNTIME RECORD
;;

(defn- rt-unreal-string
       "String representation of the Unreal runtime."
       {:added "4.1"}
       [{:keys [id node-id]}]
       (str "#rt.unreal" [id node-id]))

(std-impl/defimpl RuntimeUnreal [id]
                  :string rt-unreal-string
                  :protocols [std.protocol.component/IComponent
                              :suffix "-unreal"
                              :method {-start start-unreal
                                       -stop stop-unreal
                                       -kill stop-unreal}
                              std.protocol.context/IContext
                              :prefix "rt/default-"
                              :method {-raw-eval raw-eval-unreal
                                       -invoke-ptr invoke-ptr-unreal}])

(defn unreal:create
      "Creates an Unreal runtime."
      {:added "4.1"}
      [{:keys [id]
        :as m}]
      (map->RuntimeUnreal (merge
                           {:id (or id (f/sid))
                            :tag :unreal
                            :lifecycle {:main {}
                                        :emit {}
                                        :json :full}}
                           m)))

(defn unreal
      "Creates and starts an Unreal runtime."
      {:added "4.1"}
      ([]
       (unreal {}))
      ([m]
       (-> (unreal:create m)
           (component/start))))

(defn unreal-shared:create
      "Creates a shared Unreal runtime client.

   A flat :id is promoted to :rt/id so that `(script :python {:runtime :unreal
   :id :shared})` shares the same Unreal connection across namespaces."
      {:added "4.1"}
      [m]
      (-> {:rt/client {:type :hara/rt.unreal
                       :constructor unreal:create}
           :rt/temp true}
          (merge m)
          (cond-> (:id m) (assoc :rt/id (:id m)))
          (shared/rt-shared:create)))

(def +init+
     [(rt/install-type!
       :python :unreal.instance
       {:type :hara/rt.unreal
        :config {:layout :full}
        :instance {:create unreal:create}})
      (rt/install-type!
       :python :unreal
       {:type :hara/rt.unreal.shared
        :config {:layout :full}
        :instance {:create unreal-shared:create}})])
