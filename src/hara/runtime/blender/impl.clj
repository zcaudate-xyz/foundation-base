(ns hara.runtime.blender.impl
  (:require [clojure.string :as str]
            [std.json :as json]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.lib.impl :as std-impl]
            [hara.lang.runtime :as rt]
            [hara.runtime.basic.impl.process-python :as process-python]
            [hara.runtime.basic.type-common :as common])
  (:import [java.io BufferedReader InputStreamReader]
           [java.net Socket]
           [java.util.concurrent.atomic AtomicInteger]))

;;
;; BOOTSTRAP
;;

(def +blender-bootstrap+
  "Python bootstrap that runs inside Blender and starts a TCP server."
  (str "import socket\n"
       "import threading\n"
       "import json\n"
       "import sys\n"
       "\n"
       "def _hara_return_eval(body):\n"
       "    g = globals()\n"
       "    try:\n"
       "        exec('import bpy\\n' + body, g, g)\n"
       "        out = g.get('OUT')\n"
       "        try:\n"
       "            json.dumps(out)\n"
       "            return {'status': 'ok', 'body': out}\n"
       "        except Exception:\n"
       "            return {'status': 'ok', 'body': str(out)}\n"
       "    except Exception as e:\n"
       "        return {'status': 'error', 'body': str(e)}\n"
       "\n"
       "def _hara_client(conn):\n"
       "    while True:\n"
       "        buf = bytearray()\n"
       "        ch = conn.recv(1)\n"
       "        if ch == b'':\n"
       "            break\n"
       "        while ch != b'\\n':\n"
       "            buf.extend(ch)\n"
       "            ch = conn.recv(1)\n"
       "        line = buf.decode('utf-8')\n"
       "        if line == '<PING>':\n"
       "            continue\n"
       "        try:\n"
       "            req = json.loads(line)\n"
       "            out = _hara_return_eval(req['body'])\n"
       "            out['id'] = req.get('id')\n"
       "        except Exception as e:\n"
       "            out = {'status': 'error', 'body': str(e)}\n"
       "        conn.sendall((json.dumps(out) + '\\n').encode('utf-8'))\n"
       "\n"
       "_hara_server = socket.socket()\n"
       "_hara_server.bind(('127.0.0.1', 0))\n"
       "_hara_server.listen(1)\n"
       "_hara_port = _hara_server.getsockname()[1]\n"
       "print('HARA_BLENDER_READY ' + str(_hara_port))\n"
       "sys.stdout.flush()\n"
       "\n"
       "while True:\n"
       "    _hara_conn, _ = _hara_server.accept()\n"
       "    _hara_thread = threading.Thread(target=_hara_client, args=(_hara_conn,))\n"
       "    _hara_thread.daemon = True\n"
       "    _hara_thread.start()\n"))

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

(defn- wait-for-blender-port
  "Reads Blender stdout until the ready line appears, returning the port."
  {:added "4.1"}
  [^Process process timeout-ms]
  (let [reader (BufferedReader. (InputStreamReader. (.getInputStream process)))
        deadline (+ (System/currentTimeMillis) timeout-ms)]
    (loop []
      (when (< (System/currentTimeMillis) deadline)
        (if-let [line (.readLine reader)]
          (if (str/starts-with? line "HARA_BLENDER_READY ")
            (Long/parseLong (subs line (count "HARA_BLENDER_READY ")))
            (recur))
          (do (Thread/sleep 50)
              (recur))))))))

(defn start-blender
  "Starts a headless blender process with a Python socket server."
  {:added "4.1"}
  [{:keys [id exec] :as rt}]
  (let [exec (or exec (blender-exec))
        proc (.start (ProcessBuilder. ^"[Ljava.lang.String;"
                                      (into-array String [exec
                                                          "--background"
                                                          "--python-expr"
                                                          +blender-bootstrap+])))]
    (let [port (wait-for-blender-port proc 60000)
          socket (Socket. "127.0.0.1" port)
          in  (BufferedReader. (InputStreamReader. (.getInputStream socket)))
          out (.getOutputStream socket)]
      (assoc rt
             :process proc
             :socket socket
             :reader in
             :output out
             :msgid (AtomicInteger. 0)))))

(defn stop-blender
  "Stops the blender process and socket."
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

(defn raw-eval-blender
  "Evaluates Python code inside Blender and returns the JSON-decoded result."
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
        (:body parsed)
        (throw (ex-info "Blender Python error" {:code code :error (:body parsed)})))
      (throw (ex-info "Blender response id mismatch" {:expected id :response parsed})))))

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

(defn blender:create
  "Creates a Blender runtime."
  {:added "4.1"}
  [{:keys [id exec]
    :as m}]
  (map->RuntimeBlender (merge
                        {:id (or id (f/sid))
                         :tag :blender
                         :exec exec
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

(def +init+
  [(rt/install-type!
    :python :blender
    {:type :hara/rt.blender
     :config {:layout :full}
     :instance {:create blender:create}})])
