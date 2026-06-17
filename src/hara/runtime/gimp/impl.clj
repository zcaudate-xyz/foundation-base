(ns hara.runtime.gimp.impl
  (:require [clojure.string :as str]
            [std.json :as json]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.lib.impl :as std-impl]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.runtime.basic.impl.process-python :as process-python]
            [hara.runtime.basic.type-common :as common]
            [std.lib.network :as network]
            [xt.lang.common-lib :as lib])
  (:import [java.io BufferedReader InputStreamReader]
           [java.net Socket]
           [java.util.concurrent.atomic AtomicInteger]))

;;
;; BOOTSTRAP
;;

(def +server-gimp+
  '[(defn server-gimp
      [port opts]
      (:- :import json)
      (:- :import socket)
      (:- :import threading)
      (:- :import sys)
      (defn client-gimp [conn]
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
                          out   (return-eval body)]
                      (conn.sendall (. (json.dumps {:id id :status "ok" :body out}) (encode "utf-8")))
                      (conn.sendall (:% b "\n"))))))))
      (let [server (socket.socket)]
        (. server (bind '("127.0.0.1" port)))
        (. server (listen 1))
        (print "HARA_GIMP_READY")
        (. sys (stdout.flush))
        (while true
          (let [result (. server (accept))
                conn   (. result [0])
                thd    (threading.Thread
                        :target (fn [] (client-gimp conn)))]
            (. thd (setDaemon true))
            (. thd (start))))))])

(defn gimp-bootstrap
  "Python bootstrap that runs inside GIMP and starts a TCP server on PORT."
  {:added "4.1"}
  [port]
  (let [bootstrap (->> [(impl/emit-entry-deps
                         lib/return-eval
                         {:lang :python
                          :layout :flat})
                        (impl/emit-as
                         :python +server-gimp+)]
                       (clojure.string/join "\n\n"))]
    (str bootstrap
         "\n\n"
         (impl/emit-as
          :python [(list 'server-gimp port {})]))))

;;
;; PROCESS
;;

(defn gimp-exec
  "Resolves the GIMP executable."
  {:added "4.1"}
  []
  (or (System/getenv "GIMP_EXEC")
      (some (fn [cmd]
              (when (common/program-exists? cmd)
                cmd))
            ["gimp" "gimp-console"])
      "gimp"))

(defn- wait-for-gimp-ready
  "Reads GIMP stdout until the ready line appears."
  {:added "4.1"}
  [^Process process timeout-ms]
  (let [reader (BufferedReader. (InputStreamReader. (.getInputStream process)))
        deadline (+ (System/currentTimeMillis) timeout-ms)]
    (loop []
      (when (< (System/currentTimeMillis) deadline)
        (if-let [line (.readLine reader)]
          (if (str/starts-with? line "HARA_GIMP_READY")
            true
            (recur))
          (do (Thread/sleep 50)
              (recur)))))))

(defn start-gimp
  "Starts a headless GIMP process with a Python socket server."
  {:added "4.1"}
  [{:keys [id exec port] :as rt}]
  (let [exec (or exec (gimp-exec))
        port (or port (network/port:check-available 0))
        bootstrap (gimp-bootstrap port)
        proc (.start (ProcessBuilder. ^"[Ljava.lang.String;"
                                      (into-array String [exec
                                                          "-i"
                                                          "--batch-interpreter"
                                                          "python-fu-eval"
                                                          "-b"
                                                          bootstrap])))]
    (wait-for-gimp-ready proc 60000)
    (network/wait-for-port "127.0.0.1" port {:timeout 30000})
    (let [socket (Socket. "127.0.0.1" ^Integer port)
          in  (BufferedReader. (InputStreamReader. (.getInputStream socket)))
          out (.getOutputStream socket)]
      (assoc rt
             :process proc
             :socket socket
             :reader in
             :output out
             :msgid (AtomicInteger. 0)))))

(defn stop-gimp
  "Stops the GIMP process and socket."
  {:added "4.1"}
  [{:keys [^Process process ^Socket socket] :as rt}]
  (when socket
    (try (.close socket)
         (catch Throwable _)))
  (when process
    (try (.destroyForcibly process)
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

(defn raw-eval-gimp
  "Evaluates Python code inside GIMP and returns the JSON-decoded result."
  {:added "4.1"}
  [{:keys [^java.io.BufferedReader reader
           ^java.io.OutputStream output]
    :as rt}
   code]
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
            (throw (ex-info "GIMP Python error" {:code code :error (:value inner)}))
            (:value inner)))
        (throw (ex-info "GIMP Python error" {:code code :error (:body parsed)})))
      (throw (ex-info "GIMP response id mismatch" {:expected id :response parsed})))))

(defn invoke-ptr-gimp
  "Invokes a pointer in the GIMP runtime."
  {:added "4.1"}
  ([rt ptr args]
   (rt/default-invoke-script rt ptr args raw-eval-gimp
                             {:main  {}
                              :emit  {:body  {:transform #'process-python/default-body-transform}}
                              :json  :full
                              :encode :json})))

;;
;; RUNTIME RECORD
;;

(defn- rt-gimp-string
  "String representation of the gimp runtime."
  {:added "4.1"}
  [{:keys [id port]}]
  (str "#rt.gimp" [id port]))

(std-impl/defimpl RuntimeGimp [id]
  :string rt-gimp-string
  :protocols [std.protocol.component/IComponent
              :suffix "-gimp"
              :method {-start start-gimp
                       -stop stop-gimp
                       -kill stop-gimp}
              std.protocol.context/IContext
              :prefix "rt/default-"
              :method {-raw-eval raw-eval-gimp
                       -invoke-ptr invoke-ptr-gimp}])

(defn gimp:create
  "Creates a GIMP runtime."
  {:added "4.1"}
  [{:keys [id exec]
    :as m}]
  (map->RuntimeGimp (merge
                        {:id (or id (f/sid))
                         :tag :gimp
                         :exec exec
                         :lifecycle {:main {}
                                    :emit {}
                                    :json :full}}
                        m)))

(defn gimp
  "Creates and starts a GIMP runtime."
  {:added "4.1"}
  ([]
   (gimp {}))
  ([m]
   (-> (gimp:create m)
       (component/start))))

(def +init+
  [(rt/install-type!
    :python :gimp
    {:type :hara/rt.gimp
     :config {:layout :full}
     :instance {:create gimp:create}})])
