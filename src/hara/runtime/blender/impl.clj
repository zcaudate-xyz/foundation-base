(ns hara.runtime.blender.impl
  (:require [clojure.string :as str]
            [std.json :as json]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.lib.impl :as std-impl]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.lang.type-shared :as shared]
            [hara.runtime.basic.impl.process-python :as process-python]
            [hara.runtime.basic.type-common :as common]
            [std.lib.network :as network]
            [lib.docker.common :as docker]
            [xt.lang.common-lib :as lib])
  (:import [java.io BufferedReader InputStreamReader]
           [java.net Socket]
           [java.util.concurrent.atomic AtomicInteger]))

;;
;; BOOTSTRAP
;;

(def +server-blender+
  '[(defn server-blender
      [port opts]
      (:- :import bpy)
      (:- :import json)
      (:- :import socket)
      (:- :import threading)
      (:- :import sys)
      (defn client-blender [conn]
        (while true
          (let [buf (bytearray)
                ch  (conn.recv 1)]
            (if (== ch (:% b ""))
              (break))
            (while (not= ch (:% b "\n"))
              (. buf (extend ch))
              (:= ch (conn.recv 1)))
            (let [line (buf.decode "utf-8")]
              (cond (== line "<PING>")
                    (pass)

                    :else
                    (let [input (json.loads line)
                          body  (. input ["body"])
                          id    (. input (get "id"))
                          out   (return-eval (+ "import bpy\n" body))]
                      (conn.sendall (. (json.dumps {:id id :status "ok" :body out}) (encode "utf-8")))
                      (conn.sendall (:% b "\n"))))))))
      (let [server (socket.socket)
            host   (or (. opts (get "host")) "127.0.0.1")]
        (. server (bind '(host port)))
        (. server (listen 1))
        (print "HARA_BLENDER_READY")
        (. sys (stdout.flush))
        (while true
          (let [result (. server (accept))
                conn   (. result [0])
                thd    (threading.Thread
                        :target (fn [] (client-blender conn)))]
            (. thd (setDaemon true))
            (. thd (start))))))])

(defn blender-bootstrap
  "Python bootstrap that runs inside Blender and starts a TCP server on PORT.
   HOST defaults to 127.0.0.1; use 0.0.0.0 when running inside a container."
  {:added "4.1"}
  ([port] (blender-bootstrap port "127.0.0.1"))
  ([port host]
   (let [bootstrap (->> [(impl/emit-entry-deps
                          lib/return-eval
                          {:lang :python
                           :layout :flat})
                         (impl/emit-as
                          :python +server-blender+)]
                        (clojure.string/join "\n\n"))]
     (str bootstrap
          "\n\n"
          (impl/emit-as
           :python [(list 'server-blender port {:host host})])))))

;;
;; PROCESS
;;

(defn blender-exec
  "Resolves the blender executable."
  {:added "4.1"}
  []
  (or (System/getenv "BLENDER_EXEC")
      (some (fn [cmd]
              (when (common/program-exists? cmd)
                cmd))
            ["blender"])
      "blender"))

(defn- wait-for-blender-ready
  "Reads Blender stdout until the ready line appears."
  {:added "4.1"}
  [^Process process timeout-ms]
  (let [reader (BufferedReader. (InputStreamReader. (.getInputStream process)))
        deadline (+ (System/currentTimeMillis) timeout-ms)]
    (loop []
      (when (< (System/currentTimeMillis) deadline)
        (if-let [line (.readLine reader)]
          (if (str/starts-with? line "HARA_BLENDER_READY")
            true
            (recur))
          (do (Thread/sleep 50)
              (recur)))))))

(defn- connect-blender-socket
  "Opens a socket to HOST:PORT and returns the runtime with connection state."
  {:added "4.1"}
  [rt host port]
  (let [socket (Socket. host ^Integer port)
        in  (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (.getOutputStream socket)]
    (assoc rt
           :port port
           :socket socket
           :reader in
           :output out
           :msgid (AtomicInteger. 0))))

(defn- start-blender-local
  "Starts a headless blender process on the host."
  {:added "4.1"}
  [{:keys [exec port] :as rt}]
  (let [exec (or exec (blender-exec))
        ^Integer port (or port (network/port:check-available 0))
        bootstrap (blender-bootstrap port)
        proc (.start (ProcessBuilder. ^"[Ljava.lang.String;"
                                      (into-array String [exec
                                                          "--background"
                                                          "--python-expr"
                                                          bootstrap])))]
    (wait-for-blender-ready proc 60000)
    (network/wait-for-port "127.0.0.1" port {:timeout 30000})
    (assoc (connect-blender-socket rt "127.0.0.1" port)
           :process proc)))

(defn- start-blender-container
  "Starts blender inside a Docker container using host networking.
   Host networking avoids Docker port-mapping races that caused the first
   eval to read EOF/broken pipe."
  {:added "4.1"}
  [{:keys [id container port] :as rt}]
  (let [image     (or (get-in container [:image])
                      (f/error "No container image configured for Blender runtime"))
        port      (or port (network/port:check-available 0))
        bootstrap (blender-bootstrap port "0.0.0.0")
        container-id (str (or id (f/sid)))
        container (docker/start-container
                   {:id      container-id
                    :group   "hara"
                    :image   image
                    :cmd     ["blender" "--background" "--python-expr" bootstrap]
                    :flags   ["--network=host"]
                    :remove  true})]
    (network/wait-for-port "127.0.0.1" port {:timeout 60000})
    (assoc (connect-blender-socket rt "127.0.0.1" port)
           :container container)))

(defn start-blender
  "Starts a headless blender process with a Python socket server.
   Falls back to a Docker container when the local binary is missing
   and a :container image is configured."
  {:added "4.1"}
  [{:keys [exec container] :as rt}]
  (let [exec     (or exec (blender-exec))
        exec-str (cond (vector? exec) (first exec)
                       (string? exec) exec
                       :else nil)
        rt       (assoc rt :exec exec)]
    (cond
      (and exec-str (common/program-exists? exec-str))
      (start-blender-local rt)

      (some? (:image container))
      (start-blender-container rt)

      :else
      (f/error "Blender executable not found and no container image configured"
               {:exec exec
                :container container}))))

(defn stop-blender
  "Stops the blender process/socket or container."
  {:added "4.1"}
  [{:keys [^Process process ^Socket socket container] :as rt}]
  (when socket
    (try (.close socket)
         (catch Throwable _)))
  (when process
    (try (.destroyForcibly process)
         (catch Throwable _)))
  (when container
    (try (docker/stop-container container)
         (catch Throwable _)))
  rt)

;;
;; EVAL
;;

(defn- next-msgid
  "Returns the next request id."
  {:added "4.1"}
  [rt]
  (.incrementAndGet ^AtomicInteger (:msgid rt)))

(defn raw-eval-blender
  "Evaluates Python code inside Blender and returns the JSON-decoded result."
  {:added "4.1"}
  [{:keys [^java.io.BufferedReader reader
           ^java.io.OutputStream output]
    :as rt}
   code]
  (locking (:lock rt)
    (let [id (next-msgid rt)
          req (json/write {:id id :body code})
          _ (doto output
              (.write (.getBytes (str req "\n")))
              (.flush))
          response (.readLine reader)
          parsed (json/read response json/+keyword-mapper+)]
      (if (= id (:id parsed))
        (if (= "ok" (:status parsed))
          (let [inner (json/read (:body parsed) json/+keyword-mapper+)]
            (if (= "error" (:type inner))
              (throw (ex-info "Blender Python error" {:code code :error (:value inner)}))
              (:value inner)))
          (throw (ex-info "Blender Python error" {:code code :error (:body parsed)})))
        (throw (ex-info "Blender response id mismatch" {:expected id :response parsed}))))))

(defn invoke-ptr-blender
  "Invokes a pointer in the Blender runtime."
  {:added "4.1"}
  ([rt ptr args]
   (rt/default-invoke-script rt ptr args raw-eval-blender
                             {:main  {}
                              :emit  {:body  {:transform #'process-python/default-body-transform}}
                              :json  :full
                              :encode :json})))

;;
;; RUNTIME RECORD
;;

(defn- rt-blender-string
  "String representation of the blender runtime."
  {:added "4.1"}
  [{:keys [id port]}]
  (str "#rt.blender" [id port]))

(std-impl/defimpl RuntimeBlender [id]
  :string rt-blender-string
  :protocols [std.protocol.component/IComponent
              :suffix "-blender"
              :method {-start start-blender
                       -stop stop-blender
                       -kill stop-blender}
              std.protocol.context/IContext
              :prefix "rt/default-"
              :method {-raw-eval raw-eval-blender
                       -invoke-ptr invoke-ptr-blender}])

(def +default-blender-container+
  {:image "foundation-base/rt-basic-blender:latest"})

(defn blender:create
  "Creates a Blender runtime."
  {:added "4.1"}
  [{:keys [id exec container]
    :as m}]
  (map->RuntimeBlender (merge
                        {:id (or id (f/sid))
                         :tag :blender
                         :exec exec
                         :container (or container +default-blender-container+)
                         :lock (Object.)
                         :lifecycle {:main {}
                                    :emit {}
                                    :json :full}}
                        m)))

(defn blender
  "Creates and starts a Blender runtime."
  {:added "4.1"}
  ([]
   (blender {}))
  ([m]
   (-> (blender:create m)
       (component/start))))

(defn blender-shared:create
  "Creates a shared Blender runtime client.

   A flat :id is promoted to :rt/id so that `(script :python {:runtime :blender
   :id :shared})` shares the same process across namespaces."
  {:added "4.1"}
  [m]
  (-> {:rt/client {:type :hara/rt.blender
                   :constructor blender:create}
       :rt/temp true}
      (merge m)
      (cond-> (:id m) (assoc :rt/id (:id m)))
      (shared/rt-shared:create)))

(def +init+
  [(rt/install-type!
    :python :blender.instance
    {:type :hara/rt.blender
     :config {:layout :full
              :container {:image "foundation-base/rt-basic-blender:latest"}}
     :instance {:create blender:create}})
   (rt/install-type!
    :python :blender
    {:type :hara/rt.blender.shared
     :config {:layout :full
              :container {:image "foundation-base/rt-basic-blender:latest"}}
     :instance {:create blender-shared:create}})])
