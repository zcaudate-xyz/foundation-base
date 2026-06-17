(ns hara.runtime.unreal.impl-test
  (:require [hara.lang.type-shared :as shared]
            [hara.runtime.unreal.impl :as impl]
            [std.json :as json]
            [std.lib.env :as env])
  (:use code.test)
  (:import [java.io ByteArrayOutputStream InputStream]
           [java.net DatagramPacket DatagramSocket InetAddress InetSocketAddress MulticastSocket ServerSocket Socket]
           [java.util UUID]))

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

^{:refer hara.runtime.unreal.impl/unreal-shared :added "4.1"}
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
