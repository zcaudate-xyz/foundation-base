(ns app.manage.services-test
  (:require [app.manage.services :as services]
            [app.manage.server :as server]
            [app.manage-test :as fixture]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.net ServerSocket])
  (:use code.test))

(defn free-port [] (with-open [socket (ServerSocket. 0)] (.getLocalPort socket)))
(defn wait-for [f]
  (loop [n 0]
    (cond (f) true (>= n 100) (throw (ex-info "Process did not reach expected state" {}))
          :else (do (Thread/sleep 50) (recur (inc n))))))
(defn project [p]
  (let [port (free-port)]
    (assoc p :dev-servers [{:id :web :label "Fixture HTTP" :url (str "http://127.0.0.1:" port)
                           :command ["python3" "-u" "-m" "http.server" (str port) "--bind" "127.0.0.1"]}])))
(defn run-action [s operation]
  (server/await-run s (:id (server/submit! s {:operation operation :request {:server "web"}})) 15000))
(defn entry [s] (first (get (server/snapshot s) "dev-servers")))

^{:refer app.manage.services/execute! :id real-http-lifecycle}
(fact "actions start real HTTP content, replace the process on restart and release its port on stop"
  (fixture/with-project
    (fn [p]
      (let [p (project p) s (server/session! (constantly p)) url (get-in p [:dev-servers 0 :url])]
        (spit (io/file (:root p) "index.html") "owned development content")
        (try
          (let [start (run-action s "server-start")
                _ (wait-for #(get (entry s) "ready"))
                first-pid (get (entry s) "pid")
                first-content (slurp url)
                duplicate (run-action s "server-start")
                restart (run-action s "server-restart")
                _ (wait-for #(get (entry s) "ready"))
                replaced (not= first-pid (get (entry s) "pid"))
                second-content (slurp url)
                _ (wait-for #(some (fn [line] (str/includes? line "GET /")) (get (entry s) "logs")))
                stop (run-action s "server-stop")]
            [(:status start) first-content (:already-running duplicate) (:status restart)
             replaced second-content (:status stop) (select-keys (entry s) ["status" "managed" "ready"])])
          (finally (server/stop! s))))))
  => [:ok "owned development content" true :ok true "owned development content" :ok
      {"status" "stopped" "managed" false "ready" false}])

^{:refer app.manage.services/start! :id external-port-and-allowlist}
(fact "an external server is detected, cannot be stopped or replaced, and unknown ids cannot launch commands"
  (fixture/with-project
    (fn [p]
      (let [p (project p) owner (services/controller) other (services/controller)]
        (try
          (services/start! owner p "web")
          (wait-for #(:ready (first (services/snapshot owner p))))
          [(select-keys (first (services/snapshot other p)) [:status :managed :ready])
           (mapv (fn [[op id]] (try (services/execute! other p op id) :accepted
                                   (catch clojure.lang.ExceptionInfo _ :rejected)))
                 [["server-start" "web"] ["server-stop" "web"] ["server-restart" "web"] ["server-start" "undeclared"]])
           (:managed (first (services/snapshot owner p)))]
          (finally (services/close! other p) (services/close! owner p))))))
  => [{:status :external :managed false :ready true} [:rejected :rejected :rejected :rejected] true])

^{:refer app.manage.server/stop! :id graceful-tree-cleanup}
(fact "closing the workbench terminates children and lets the wrapper perform graceful cleanup"
  (fixture/with-project
    (fn [p]
      (let [p (project p) port (.getPort (java.net.URI. (get-in p [:dev-servers 0 :url])))
            code (str "import subprocess,sys,signal,pathlib,time\n"
                      "p=subprocess.Popen([sys.executable,'-u','-m','http.server','" port "','--bind','127.0.0.1'])\n"
                      "def stop(*args):\n p.terminate()\n p.wait(timeout=5)\n pathlib.Path('cleaned').write_text('released')\n sys.exit(0)\n"
                      "signal.signal(signal.SIGTERM,stop)\nwhile True: time.sleep(0.1)\n")
            p (-> p (assoc-in [:dev-servers 0 :command] ["python3" "-u" "-c" code])
                    (assoc-in [:dev-servers 0 :stop-argument] "-c"))
            s (server/session! (constantly p))]
        (try
          (run-action s "server-start")
          (wait-for #(get (entry s) "ready"))
          (server/stop! s)
          [(slurp (io/file (:root p) "cleaned"))
           (select-keys (entry s) ["status" "managed" "ready"])]
          (finally (server/stop! s))))))
  => ["released" {"status" "stopped" "managed" false "ready" false}])

^{:refer app.manage.services/snapshot :id failed-process-output}
(fact "a crashing development process reports its actual exit code and output"
  (fixture/with-project
    (fn [p]
      (let [p (assoc-in (project p) [:dev-servers 0 :command]
                        ["python3" "-u" "-c" "print('deliberate startup failure'); raise SystemExit(7)"])
            c (services/controller)]
        (try
          (services/start! c p "web")
          (wait-for #(let [v (first (services/snapshot c p))] (and (= :error (:status v)) (seq (:logs v)))))
          (select-keys (first (services/snapshot c p)) [:status :exit :logs :managed :ready])
          (finally (services/close! c p))))))
  => {:status :error :exit 7 :logs ["deliberate startup failure"] :managed false :ready false})
