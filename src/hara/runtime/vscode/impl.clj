(ns hara.runtime.vscode.impl
  (:require [clojure.java.io :as io]
            [clojure.string]
            [std.json :as json]
            [std.lib.component :as component]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.impl :as std-impl]
            [std.lib.network :as network]
            [std.lib.os :as os]
            [std.fs :as fs]
            [hara.lang.runtime :as rt]
            [hara.lang.type-shared :as shared]
            [hara.runtime.basic.type-common :as common]
            [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context])
  (:import [java.io BufferedReader InputStreamReader PrintWriter]
           [java.net Socket]))

;;
;; EXECUTABLE
;;

(defn vscode-exec
  "Resolves the VS Code executable.

   In a headless environment (no DISPLAY) with xvfb-run available, wraps the
   launch with xvfb-run so the Electron UI can start."
  {:added "4.1"}
  []
  (or (System/getenv "VSCODE_EXEC")
      (let [cmd (some (fn [cmd]
                        (when (common/program-exists? cmd)
                          cmd))
                      ["code"
                       "code-oss"
                       "code-insiders"
                       "codium"
                       "cursor"])]
        (if cmd
          (if (and (nil? (System/getenv "DISPLAY"))
                   (common/program-exists? "xvfb-run"))
            ["xvfb-run" "-a" cmd]
            cmd)
          "code"))))

(defn- extension-path
  "Returns the absolute path to the companion VS Code extension."
  {:added "4.1"}
  []
  (str (io/file (System/getProperty "user.dir")
                "src-js"
                "vscode-hara-runtime")))

;;
;; SOCKET IO
;;

(defn- write-request
  "Sends a JSON request to the VS Code extension socket."
  {:added "4.1"}
  [{:keys [^PrintWriter output]} id code]
  (.println output (json/write {:id id :code code}))
  (.flush output))

(defn- read-response
  "Reads and parses a single JSON line from the socket."
  {:added "4.1"}
  [{:keys [^BufferedReader input]}]
  (when-let [line (.readLine input)]
    (json/read line json/+keyword-mapper+)))

;;
;; PROCESS
;;

(defn start-vscode
  "Starts a VS Code instance with the Hara runtime extension and connects a client socket."
  {:added "4.1"}
  [{:keys [id exec port] :as rt}]
  (let [exec         (or exec (vscode-exec))
        ^String host "127.0.0.1"
        ^Integer port (or port (network/port:check-available 0))
        address      (str host ":" port)
        user-data    (str (java.io.File/createTempFile "vscode-" "-userdata"))
        _            (.delete (java.io.File. user-data))
        _            (.mkdirs (java.io.File. user-data))
        base         (if (vector? exec) exec [exec])
        args         (vec (concat base
                                  ["--user-data-dir" user-data
                                   "--extensionDevelopmentPath" (extension-path)
                                   "--disable-extensions"
                                   "--disable-workspace-trust"
                                   "--new-window"
                                   "--wait"
                                   "--disable-gpu"
                                   "--no-sandbox"
                                   "--disable-dev-shm-usage"]))
        proc         (os/sh {:args args
                             :wait false
                             :env {"HARA_VSCODE_PORT" (str port)}})]
    (network/wait-for-port host port {:timeout 60000})
    (let [socket (Socket. host port)
          in     (BufferedReader. (InputStreamReader. (.getInputStream socket)))
          out    (PrintWriter. (.getOutputStream socket) true)]
      (assoc rt
             :process proc
             :host host
             :port port
             :user-data-dir user-data
             :socket socket
             :input in
             :output out
             :msgid (atom 0)))))

(defn stop-vscode
  "Stops the VS Code process and closes the client socket."
  {:added "4.1"}
  [{:keys [^Process process ^Socket socket ^PrintWriter output ^BufferedReader input user-data-dir] :as rt}]
  (when input
    (try (.close input)
         (catch Throwable _)))
  (when output
    (try (.close output)
         (catch Throwable _)))
  (when socket
    (try (.close socket)
         (catch Throwable _)))
  (when process
    (try (.destroyForcibly process)
         (catch Throwable _)))
  (when user-data-dir
    (try (std.fs/delete user-data-dir)
         (catch Throwable _)))
  rt)

;;
;; RPC
;;

(defn next-msgid
  "Returns the next request id and increments the counter."
  {:added "4.1"}
  [rt]
  (swap! (:msgid rt) inc))

(defn send-request
  "Sends a code evaluation request and waits for the matching response."
  {:added "4.1"}
  [rt code]
  (let [id (next-msgid rt)]
    (write-request rt id code)
    (loop []
      (let [response (read-response rt)]
        (cond (nil? response)
              (throw (ex-info "VS Code connection closed while waiting for response"
                              {:id id :code code}))

              (= (:id response) id)
              response

              :else
              (recur))))))

;;
;; EVAL
;;

(defn js-eval-wrap
  "Wraps JavaScript code so that the result is JSON-serializable."
  {:added "4.1"}
  [code]
  (str "(() => {\n"
       "  try {\n"
       "    return eval(" (json/write code) ");\n"
       "  } catch (_hara_err) {\n"
       "    throw _hara_err;\n"
       "  }\n"
       "})()"))

(defn raw-eval-vscode
  "Evaluates JavaScript code inside VS Code and returns the decoded result."
  {:added "4.1"}
  [rt code]
  (let [wrapped  (js-eval-wrap code)
        response (send-request rt wrapped)]
    (case (:status response)
      "ok"    (:value response)
      "error" (throw (ex-info "VS Code eval error" {:code code :error (:error response)}))
      (throw (ex-info "VS Code unexpected response" {:code code :response response})))))

(defn invoke-ptr-vscode
  "Invokes a pointer in the VS Code runtime."
  {:added "4.1"}
  ([rt ptr args]
   (rt/default-invoke-script rt ptr args raw-eval-vscode
                             {:main  {}
                              :emit  {:body  {:transform #'rt/return-transform}
                                      :lang/format :global}
                              :json  :full
                              :encode :json})))

;;
;; RUNTIME RECORD
;;

(defn- rt-vscode-string [{:keys [id]}]
  (str "#rt.vscode" [id]))

(std-impl/defimpl RuntimeVscode [id]
  :string rt-vscode-string
  :protocols [std.protocol.component/IComponent
              :suffix "-vscode"
              :method {-start start-vscode
                       -stop stop-vscode
                       -kill stop-vscode}
              std.protocol.context/IContext
              :prefix "rt/default-"
              :method {-raw-eval raw-eval-vscode
                       -invoke-ptr invoke-ptr-vscode}])

(defn vscode:create
  "Creates a VS Code runtime."
  {:added "4.1"}
  [{:keys [id exec]
    :as m}]
  (map->RuntimeVscode (merge
                       {:id (or id (f/sid))
                        :tag :vscode
                        :exec exec
                        :lifecycle {:main {}
                                    :emit {}
                                    :json :full}}
                       m)))

(defn vscode
  "Creates and starts a VS Code runtime."
  {:added "4.1"}
  ([]
   (vscode {}))
  ([m]
   (-> (vscode:create m)
       (component/start))))

(defn vscode-shared:create
  "Creates a shared VS Code runtime client."
  {:added "4.1"}
  [m]
  (-> {:rt/client {:type :hara/rt.vscode
                   :constructor vscode:create}
       :rt/temp true}
      (merge m)
      (cond-> (:id m) (assoc :rt/id (:id m)))
      (shared/rt-shared:create)))

(def +init+
  [(rt/install-type!
    :js :vscode.instance
    {:type :hara/rt.vscode
     :config {:layout :full}
     :instance {:create vscode:create}})
   (rt/install-type!
    :js :vscode
    {:type :hara/rt.vscode.shared
     :config {:layout :full}
     :instance {:create vscode-shared:create}})])

(comment
  (./import))
