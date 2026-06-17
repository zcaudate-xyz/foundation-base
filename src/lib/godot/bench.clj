(ns lib.godot.bench
  (:require [clojure.string]
            [std.fs :as fs]
            [std.json :as json]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.future :as future]
            [std.lib.network :as network]
            [std.lib.os :as os]
            [hara.runtime.basic.type-common :as common]))

(def +bench-path+ "test-bench/godot")

(defonce ^:dynamic *active* (atom {}))

(def +godot-server-source+
  "extends SceneTree

var server = TCPServer.new()
var thread = Thread.new()
var port = 0
var running = true

func _init():
    var args = OS.get_cmdline_user_args()
    for i in range(args.size()):
        if args[i] == \"--port\" and i + 1 < args.size():
            port = int(args[i + 1])
    if port == 0:
        push_error(\"HARA_GODOT_ERROR: no port provided\")
        quit()
        return
    if server.listen(port) != OK:
        push_error(\"HARA_GODOT_ERROR: failed to listen on \" + str(port))
        quit()
        return
    print(\"HARA_GODOT_READY\")
    thread.start(server_loop)

func _exit_tree():
    running = false
    thread.wait_to_finish()

func server_loop():
    while running:
        if server.is_connection_available():
            var conn = server.take_connection()
            handle_connection(conn)
        OS.delay_msec(10)

func handle_connection(conn):
    var buffer = \"\"
    while conn.get_status() == StreamPeerTCP.STATUS_CONNECTED:
        var avail = conn.get_available_bytes()
        if avail > 0:
            var arr = conn.get_data(avail)
            if arr[0] != OK:
                break
            var bytes = arr[1]
            buffer += bytes.get_string_from_utf8()
            while true:
                var idx = buffer.find(\"\\n\")
                if idx < 0:
                    break
                var line = buffer.substr(0, idx)
                buffer = buffer.substr(idx + 1)
                if line != \"\":
                    process_line(conn, line)
                    if conn.get_status() != StreamPeerTCP.STATUS_CONNECTED:
                        return
        OS.delay_msec(10)

func process_line(conn, line):
    var parser = JSON.new()
    var err = parser.parse(line)
    if err != OK:
        send_response(conn, {\"id\": null, \"status\": \"error\", \"body\": \"JSON parse error\"})
        return
    var req = parser.get_data()
    var id = req.get(\"id\", 0)
    var body = req.get(\"body\", \"\")
    var result = eval_body(body)
    send_response(conn, {\"id\": id, \"status\": \"ok\", \"body\": result})

func send_response(conn, resp):
    if conn.get_status() != StreamPeerTCP.STATUS_CONNECTED:
        return
    var s = JSON.stringify(resp) + \"\\n\"
    conn.put_data(s.to_utf8_buffer())

func eval_body(body):
    var script = GDScript.new()
    script.source_code = body
    var err = script.reload()
    if err != OK:
        return {\"type\": \"error\", \"value\": \"compile error\"}
    var node = Node.new()
    node.set_script(script)
    if not node.has_method(\"eval\"):
        return {\"type\": \"error\", \"value\": \"missing eval method\"}
    var result = node.eval()
    node.queue_free()
    return {\"type\": \"data\", \"value\": result}
")

(defn- write-godot-project!
  "writes a minimal headless Godot project for the bench"
  [root-dir]
  (fs/create-directory root-dir)
  (let [project-file (str root-dir "/project.godot")
        main-file (str root-dir "/main.gd")]
    (when-not (fs/exists? project-file)
      (spit project-file (str "[application]\n"
                              "config/name=\"hara_godot_bench\"\n"
                              "config/features=PackedStringArray(\"4.2\")\n\n"
                              "[rendering]\n"
                              "renderer/rendering_method=\"mobile\"\n")))
    (when-not (fs/exists? main-file)
      (spit main-file +godot-server-source+))))

(defn godot-exec
  "Resolves the godot executable."
  {:added "4.1"}
  []
  (or (System/getenv "GODOT_EXEC")
      (some (fn [cmd]
              (when (common/program-exists? cmd)
                cmd))
            ["godot-4" "godot"])
      "godot-4"))

(defn- start-godot-process
  "Launches the Godot server process with output redirected to files."
  {:added "4.1"}
  [exec root-dir port]
  (let [pb (doto (ProcessBuilder. ^"[Ljava.lang.String;" (into-array String [exec "--headless" "--script" "main.gd" "--" "--port" (str port)]))
             (.directory (java.io.File. ^String root-dir))
             (.redirectOutput (java.io.File. (str root-dir "/godot.out")))
             (.redirectError (java.io.File. (str root-dir "/godot.err"))))]
    (.start pb)))

(defn- wait-for-ready-file
  "Polls the Godot stdout file until the ready marker appears."
  {:added "4.1"}
  [root-dir timeout-ms]
  (let [file (java.io.File. (str root-dir "/godot.out"))
        deadline (+ (System/currentTimeMillis) timeout-ms)]
    (loop []
      (when (< (System/currentTimeMillis) deadline)
        (if (and (.exists file)
                 (clojure.string/includes? (slurp file) "HARA_GODOT_READY"))
          true
          (do (Thread/sleep 100)
              (recur)))))))

(defn start-godot-server
  "Starts a headless Godot TCP server in a given directory."
  {:added "4.1"}
  ([{:keys [port]} type root-dir]
   (let [port (or port (network/port:check-available 0))]
     (-> (if (not (get @*active* port))
           (swap! *active*
                  (fn [m]
                    (if (get m port)
                      m
                      (do (write-godot-project! root-dir)
                          (let [process (start-godot-process (godot-exec) root-dir port)
                                thread  (doto (Thread. (fn [] (os/sh-wait process) (swap! *active* dissoc port)))
                                          (.setDaemon true)
                                          (.start))]
                            (wait-for-ready-file root-dir 60000)
                            (assoc m port {:type type
                                           :port port
                                           :root-dir root-dir
                                           :process process
                                           :thread thread}))))))
           @*active*)
         (get port)))))

(defn stop-godot-server
  "Stops the Godot server for a given port and type."
  {:added "4.1"}
  [port stop-type]
  (let [{:keys [type process] :as entry} (get @*active* port)]
    (if (= type stop-type)
      (doto process
        (os/sh-exit)
        (os/sh-kill)
        (os/sh-wait)))
    entry))

(defn- scratch-root
  "Returns a scratch directory under the user's home."
  {:added "4.1"}
  []
  (let [dir (str (System/getProperty "user.home") "/hara_godot_bench_" (System/currentTimeMillis))]
    (fs/create-directory dir)
    dir))

(defn bench-start
  "Starts the bench."
  {:added "4.1"}
  [{:keys [port] :as godot} type]
  (let [root-dir (case type
                   :scratch (scratch-root)
                   (str +bench-path+ "/" port))
        entry  (start-godot-server godot type root-dir)]
    (assoc godot :port (:port entry))))

(defn bench-stop
  "Stops the bench."
  {:added "4.1"}
  [{:keys [port bench] :as godot} _]
  (let [{:keys [type process]} (get @*active* port)]
    (stop-godot-server port bench)
    godot))

(defn all-godot-ports
  "Returns all active Godot server ports."
  {:added "4.1"}
  []
  (->> @*active* keys sort vec))
