(ns hara.runtime.unreal.impl-test
  (:require [hara.lang :as h]
            [hara.lang.type-shared :as shared]
            [hara.runtime.unreal.impl :as impl]
            [std.json :as json]
            [std.lib.env :as env])
  (:use code.test)
  (:import [java.io ByteArrayOutputStream InputStream]
           [java.net DatagramPacket DatagramSocket InetAddress InetSocketAddress MulticastSocket ServerSocket Socket]
           [java.util UUID]))

(fact:global
 {:skip (not (or (env/program-exists? "UE4Editor")
                  (env/program-exists? "UE5Editor")
                  (env/program-exists? "UnrealEditor")))})

;;
;; Helpers
;;

(defn- read-json-object
  "Reads a complete JSON object from an InputStream."
  [^InputStream input]
  (let [baos (ByteArrayOutputStream.)
        buf (byte-array 1)]
    (loop []
      (let [n (.read input buf)]
        (when (neg? n)
          (throw (ex-info "stream closed" {})))
        (.write baos buf 0 1)
        (let [s (.toString baos "UTF-8")]
          (or (try (json/read s json/+keyword-mapper+) (catch Throwable _ nil))
              (recur)))))))

(defn- mock-unreal-command-server
  "Starts a background thread that pretends to be Unreal's remote execution
   command channel. It listens for the UDP open_connection message, connects
   back to the client's TCP server, and replies to commands with a fixed
   command_result."
  [expected-command result & [success?]]
  (let [success? (if (nil? success?) true success?)
        group (InetAddress/getByName "239.0.0.1")
        udp (doto (MulticastSocket. 6766)
              (.joinGroup group))
        running (atom true)
        thread (doto (Thread.
                      (fn []
                        (try
                          (let [buf (byte-array 8192)
                                packet (DatagramPacket. buf (alength buf))]
                            (.receive udp packet)
                            (let [msg (impl/parse-message (String. buf 0 (.getLength packet) "UTF-8"))
                                  data (:data msg)
                                  host (get data :command_ip)
                                  port (get data :command_port)]
                              (when (and host port)
                                (let [socket (Socket. ^String host ^Integer port)
                                      input (.getInputStream socket)
                                      output (.getOutputStream socket)]
                                  (let [cmd (read-json-object input)
                                        cmd-data (:data cmd)]
                                    (.write output
                                            (.getBytes
                                             (json/write
                                              {:version 1
                                               :magic "ue_py"
                                               :type "command_result"
                                               :source "mock-node"
                                               :dest (:source cmd)
                                               :data {:success success?
                                                      :command (get cmd-data :command)
                                                      :result result
                                                      :output []}})
                                             "UTF-8"))
                                    (.flush output))
                                  (.close socket)))))
                          (catch Throwable t
                            (println "mock-unreal error:" (.getMessage t)))
                          (finally
                            (try (.leaveGroup udp group) (catch Throwable _))
                            (.close udp)))))
                   (.start))]
    {:thread thread
     :udp udp
     :running running}))

(defn- stop-mock [mock]
  (reset! (:running mock) false)
  (try (.close (:udp mock)) (catch Throwable _))
  (try (.join ^Thread (:thread mock) 1000) (catch Throwable _)))

;;
;; Tests
;;

^{:refer hara.runtime.unreal.impl/make-message :added "4.1"}
(fact "builds a remote execution message"
  (impl/make-message "ping" "client-id")
  => {:version 1 :magic "ue_py" :type "ping" :source "client-id"})

^{:refer hara.runtime.unreal.impl/parse-message :added "4.1"}
(fact "parses a remote execution message"
  (impl/parse-message (json/write {:version 1 :magic "ue_py"
                                   :type "pong" :source "node-1"
                                   :dest "client-1"
                                   :data {:user "user"}}))
  => {:version 1 :magic "ue_py" :type "pong"
      :source "node-1" :dest "client-1"
      :data {:user "user"}})

(fact "starts a command channel and evaluates Python via the mock Unreal server"
  (let [mock (mock-unreal-command-server "print('hello')" "'hello'")
        rt (try
             (impl/start-unreal {:node-id "mock-node"})
             (catch Throwable t
               (stop-mock mock)
               (throw t)))]
    (try
      [(= "mock-node" (:node-id rt))
       (boolean (:socket rt))
       (= "'hello'" (impl/raw-eval-unreal rt "print('hello')"))]
      (finally
        (impl/stop-unreal rt)
        (stop-mock mock))))
  => [true true true])

(fact "propagates command failures from the mock Unreal server"
  (let [mock (mock-unreal-command-server "1/0" "error" false)
        rt (try
             (impl/start-unreal {:node-id "mock-node"})
             (catch Throwable t
               (stop-mock mock)
               (throw t)))]
    (try
      (try
        (impl/raw-eval-unreal rt "1/0")
        false
        (catch clojure.lang.ExceptionInfo e
          (= "Unreal Python command failed" (ex-message e))))
      (finally
        (impl/stop-unreal rt)
        (stop-mock mock))))
  => true)

^{:refer hara.runtime.unreal.impl/unreal-shared:create :added "4.1"}
(fact "two shared unreal runtimes with the same id share the underlying instance"
  (let [mock (mock-unreal-command-server "1 + 2" "3")
        rt1 (impl/unreal-shared:create {:id :shared-unreal-test :node-id "mock-node"})
        rt2 (impl/unreal-shared:create {:id :shared-unreal-test :node-id "mock-node"})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      [(= (shared/rt-get-inner rt1) (shared/rt-get-inner rt2))
       (= "3" (impl/raw-eval-unreal (shared/rt-get-inner rt1) "1 + 2"))]
      (finally
        (std.lib.component/stop rt1)
        (std.lib.component/stop rt2)
        (stop-mock mock))))
  => [true true])


^{:refer hara.runtime.unreal.impl/make-node-id :added "4.1"}
(fact "generates a string uuid node id"
  (let [id1 (impl/make-node-id)
        id2 (impl/make-node-id)]
    [(string? id1)
     (not= id1 id2)
     (boolean (re-find #"^[0-9a-f-]{36}$" id1))])
  => [true true true])

^{:refer hara.runtime.unreal.impl/send-udp :added "4.1"}
(fact "sends a udp message"
  (let [server (DatagramSocket. 0 (InetAddress/getByName "127.0.0.1"))
        client (DatagramSocket.)
        port (.getLocalPort server)
        msg (impl/make-message "ping" "client")]
    (try
      (future
        (Thread/sleep 50)
        (impl/send-udp client msg "127.0.0.1" port))
      (let [received (impl/recv-udp server 1000)
            parsed (impl/parse-message received)]
        [(:type parsed) (:source parsed)])
      (finally
        (.close server)
        (.close client))))
  => ["ping" "client"])

^{:refer hara.runtime.unreal.impl/recv-udp :added "4.1"}
(fact "receives a udp message"
  (let [server (DatagramSocket. 0 (InetAddress/getByName "127.0.0.1"))
        client (DatagramSocket.)
        port (.getLocalPort server)
        msg (impl/make-message "pong" "node")]
    (try
      (future
        (Thread/sleep 50)
        (impl/send-udp client msg "127.0.0.1" port))
      (= msg (impl/parse-message (impl/recv-udp server 1000)))
      (finally
        (.close server)
        (.close client))))
  => true)

^{:refer hara.runtime.unreal.impl/discover-node :added "4.1"}
(fact "discovers a node from a mock udp pong"
  (let [captured-source (atom nil)]
    (with-redefs [impl/send-udp (fn [_ message _ _] (reset! captured-source (:source message)))
                  impl/recv-udp (fn [_ _]
                                  (json/write
                                   {:version 1
                                    :magic "ue_py"
                                    :type "pong"
                                    :source "mock-node"
                                    :dest @captured-source
                                    :data {:foo 1}}))]
      (let [result (impl/discover-node {:multicast-group "239.0.0.2"
                                        :multicast-port 0
                                        :discovery-timeout 1000})]
        [(:node-id result) (:data result) (string? (:source result)) (= (:source result) @captured-source)])))
  => ["mock-node" {:foo 1} true true])

^{:refer hara.runtime.unreal.impl/start-command-server :added "4.1"}
(fact "starts a command channel and returns server socket and streams"
  (let [mock (mock-unreal-command-server "1 + 2" "3")
        conn (try
               (impl/start-command-server {:node-id "mock-node"
                                           :source-id "client"
                                           :open-retries 5
                                           :open-retry-delay 500})
               (catch Throwable t
                 (stop-mock mock)
                 (throw t)))]
    (try
      [(boolean (:server conn))
       (boolean (:socket conn))
       (boolean (:input conn))
       (boolean (:output conn))
       (number? (.getLocalPort ^ServerSocket (:server conn)))
       (not (.isClosed ^Socket (:socket conn)))]
      (finally
        (try (.close ^Socket (:socket conn)) (catch Throwable _))
        (try (.close ^ServerSocket (:server conn)) (catch Throwable _))
        (stop-mock mock))))
  => [true true true true true true])

^{:refer hara.runtime.unreal.impl/start-unreal :added "4.1"}
(fact "starts an unreal runtime using a provided node id"
  (let [mock (mock-unreal-command-server "1 + 2" "3")
        rt (try
             (impl/start-unreal {:node-id "mock-node"})
             (catch Throwable t
               (stop-mock mock)
               (throw t)))]
    (try
      [(= "mock-node" (:node-id rt))
       (string? (:source rt))
       (boolean (:socket rt))
       (boolean (:lock rt))]
      (finally
        (impl/stop-unreal rt)
        (stop-mock mock))))
  => [true true true true])

^{:refer hara.runtime.unreal.impl/stop-unreal :added "4.1"}
(fact "closes the command channel and returns the runtime"
  (let [mock (mock-unreal-command-server "1 + 2" "3")
        rt (try
             (impl/start-unreal {:node-id "mock-node"})
             (catch Throwable t
               (stop-mock mock)
               (throw t)))]
    (try
      (let [stopped (impl/stop-unreal rt)]
        [(identical? stopped rt)
         (.isClosed ^Socket (:socket rt))
         (.isClosed ^ServerSocket (:server rt))])
      (finally
        (stop-mock mock))))
  => [true true true])

^{:refer hara.runtime.unreal.impl/raw-eval-unreal :added "4.1"}
(fact "evaluates python through the mock command channel"
  (let [mock (mock-unreal-command-server "1 + 2" "3")
        rt (try
             (impl/start-unreal {:node-id "mock-node"})
             (catch Throwable t
               (stop-mock mock)
               (throw t)))]
    (try
      (= "3" (impl/raw-eval-unreal rt "1 + 2"))
      (finally
        (impl/stop-unreal rt)
        (stop-mock mock))))
  => true)

^{:refer hara.runtime.unreal.impl/invoke-ptr-unreal :added "4.1"}
(fact "invokes a pointer through the unreal runtime"
  (let [mock (mock-unreal-command-server nil "3")
        rt (try
             (impl/start-unreal {:node-id "mock-node"})
             (catch Throwable t
               (stop-mock mock)
               (throw t)))]
    (try
      (boolean (impl/invoke-ptr-unreal rt (h/ptr :python {:module (ns-name *ns*)}) ['(+ 1 2)]))
      (finally
        (impl/stop-unreal rt)
        (stop-mock mock))))
  => true)

^{:refer hara.runtime.unreal.impl/unreal:create :added "4.1"}
(fact "creates an unreal runtime record"
  (let [rt (impl/unreal:create {})]
    [(boolean rt)
     (= :unreal (:tag rt))
     (boolean (:id rt))])
  => [true true true])

^{:refer hara.runtime.unreal.impl/unreal :added "4.1"}
(fact "creates and starts an unreal runtime"
  (let [mock (mock-unreal-command-server "1 + 2" "3")
        rt (try
             (impl/unreal {:node-id "mock-node"})
             (catch Throwable t
               (stop-mock mock)
               (throw t)))]
    (try
      [(boolean rt)
       (= "mock-node" (:node-id rt))
       (boolean (:socket rt))]
      (finally
        (impl/stop-unreal rt)
        (stop-mock mock))))
  => [true true true])