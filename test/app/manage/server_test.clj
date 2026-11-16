(ns app.manage.server-test
  (:require [app.manage.server :as server]
            [app.manage-test :as fixture]
            [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:import [java.net URI]
           [com.sun.net.httpserver HttpExchange Headers]
           [java.io ByteArrayInputStream ByteArrayOutputStream])
  (:use code.test))

(defn http [s method path body token]
  (let [output (ByteArrayOutputStream.) status (atom nil)
        headers (Headers.) response-headers (Headers.)
        _ (when token (.set headers "X-App-Manage-Token" token))
        exchange (proxy [HttpExchange] []
                   (getRequestURI [] (URI/create path))
                   (getRequestMethod [] method)
                   (getRequestHeaders [] headers)
                   (getResponseHeaders [] response-headers)
                   (getRequestBody [] (ByteArrayInputStream. (.getBytes (json/write-str body) "UTF-8")))
                   (getResponseBody [] output)
                   (sendResponseHeaders [code length] (reset! status code)))]
    (#'server/handle! s exchange)
    [@status (json/read-str (.toString output "UTF-8") :key-fn keyword)]))

^{:refer app.manage.server/session! :added "4.1"}
(fact "HTTP actions build the actual project, publish progress, and enforce file ownership"
  (fixture/with-project
    (fn [project]
      (let [s (server/session! (constantly project))]
        (try
          (let [[status data] (http s "GET" "/api/state" nil nil)
                denied (first (http s "POST" "/api/actions" {:operation "build"} nil))
                [accepted queued] (http s "POST" "/api/actions" {:operation "build" :tests false} (:token s))
                result (server/await-run s (:id queued) 10000)
                snapshot (server/snapshot s)
                run (first (get snapshot "runs"))
                read-back (http s "GET" "/api/file?target=page&path=generated/page.edn" nil nil)
                foreign (first (http s "GET" "/api/file?target=db&path=generated/page.edn" nil nil))
                unknown (first (http s "POST" "/api/actions" {:operation "shell"} (:token s)))]
            [status (mapv :id (get-in data [:project :targets])) denied accepted (:status result)
             (slurp (io/file (:root project) "generated/page.edn"))
             (count (get snapshot "runs")) (get run "status")
             (mapv #(get % "type") (get run "events"))
             read-back foreign unknown])
          (finally (server/stop! s) (server/stop! s))))))
  => [200 ["db" "page"] 403 202 :ok "uses {:version 1}\n" 1 "ok"
      ["target/started" "target/finished" "target/started" "target/finished"]
      [200 {:path "generated/page.edn" :content "uses {:version 1}\n"}] 403 400])

^{:refer app.manage.server/submit! :added "4.1"}
(fact "failed output checks and unknown targets appear as failed runs"
  (fixture/with-project
    (fn [project]
      (let [s (server/session! (constantly project))]
        (try
          (let [check (server/submit! s {:operation "check"})
                result (server/await-run s (:id check) 10000)
                bad (server/submit! s {:operation "build" :target "absent"})
                failure (server/await-run s (:id bad) 10000)]
            [(:status result) (:missing result) (:status failure) (:message failure)
             (mapv #(get % "status") (get (server/snapshot s) "runs"))])
          (finally (server/stop! s))))))
  => [:error ["generated/db.edn" "generated/page.edn"] :error "Unknown target" ["error" "error"]])

^{:refer app.manage.server/snapshot :added "4.1"}
(fact "renderer failures retain their target error and watcher controls operate on the session"
  (fixture/with-project
    (fn [project]
      (let [_ (.mkdirs (io/file (:root project) "src"))
            project (assoc project :source-paths ["src"])
            project (assoc-in project [:targets :db :render] (fn [_] (throw (ex-info "broken renderer" {}))))
            s (server/session! (constantly project))]
        (try
          (let [run (server/submit! s {:operation "build" :tests false})
                result (server/await-run s (:id run) 10000)
                target (get-in (server/snapshot s) ["target-status" "db" "status"])
                start (server/submit! s {:operation "watch-start"})
                _ (server/await-run s (:id start) 10000)
                watching (get (server/snapshot s) "watching")
                stop (server/submit! s {:operation "watch-stop"})
                _ (server/await-run s (:id stop) 10000)]
            [(:status result) (:message result) target watching (get (server/snapshot s) "watching")])
          (finally (server/stop! s))))))
  => [:error "broken renderer" "error" true false])

^{:refer app.manage.server/preview-index :added "4.1"}
(fact "previews resolve owned static outputs and loopback apps without claiming missing pages are built"
  (fixture/with-project
    (fn [project]
      (let [project (assoc project :previews
                           [{:id :app :target :page :label "Application" :url "http://127.0.0.1:8000/web-main" :command "make serve"}
                            {:id :static :target :page :path "generated/page.edn"}])
            before (:previews (server/project-index project))
            file (io/file (:root project) "generated/page.edn")
            _ (io/make-parents file)
            _ (spit file "page")
            after (:previews (server/project-index project))]
        [(mapv #(select-keys % [:id :url :kind :ready]) before)
         (:command (first before)) (:ready (second after))
         (fixture/error-data #(server/project-index (assoc project :previews [{:id :bad :target :db :path "generated/page.edn"}])))
         (fixture/error-data #(server/project-index (assoc project :previews [{:id :bad :target :page :url "javascript:alert(1)"}])))
         (fixture/error-data #(server/project-index (assoc project :previews [{:id :bad :target :page :url "https://example.com"}])))])))
  => [[{:id "app" :url "http://127.0.0.1:8000/web-main" :kind :server :ready true}
       {:id "static" :url "/preview/generated/page.edn" :kind :static :ready false}]
      "make serve" true {:id :bad :path "generated/page.edn"} {:id :bad} {:id :bad}]
  (fixture/with-project
    (fn [project]
      (let [file (io/file (:root project) "generated/index.html")
            project (assoc-in project [:targets :page :outputs] ["generated/index.html"])]
        (io/make-parents file) (spit file "<h1>Preview</h1>")
        (mapv #(select-keys % [:target :url :ready]) (:previews (server/project-index project))))))
  => [{:target "page" :url "/preview/generated/index.html" :ready true}])
