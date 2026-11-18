(ns app.manage.http-api-test
  (:require [app.manage.http-api :as api]
            [app.manage.server :as server]
            [app.manage.server-test :as http]
            [app.manage-test :as fixture]
            [clojure.java.io :as io])
  (:use code.test))

^{:refer app.manage.http-api/handle! :id configured-actions}
(fact "configured actions use the shared queue/history and require a token; API is opt-in"
  (fixture/with-project
    (fn [project]
      (let [config (atom (assoc project :http-api {:enabled true :operations ["analyse"]
                                                  :actions {:render {:operation "build" :tests false}}}))
            s (server/session! #(deref config))]
        (try
          (let [denied (first (http/http s "POST" "/api/v1/actions/render/runs" {} nil))
                [accepted queued] (http/http s "POST" "/api/v1/actions/render/runs" {} (:token s))
                result (server/await-run s (:id queued) 10000)
                [status run] (http/http s "GET" (str "/api/v1/runs/" (:id queued)) nil nil)
                shared (= (:id queued) (get (first (get (server/snapshot s) "runs")) "id"))
                blocked (first (http/http s "POST" "/api/v1/runs" {:operation "build"} (:token s)))
                [allowed next-run] (http/http s "POST" "/api/v1/runs" {:operation "analyse"} (:token s))
                _ (server/await-run s (:id next-run) 10000)
                missing (first (http/http s "GET" "/api/v1/runs/absent" nil nil))
                _ (swap! config dissoc :http-api)
                disabled (first (http/http s "GET" "/api/v1" nil nil))]
            [denied accepted (:status result) status (:status run) shared blocked allowed missing disabled])
          (finally (server/stop! s))))))
  => [403 202 :ok 200 "ok" true 403 202 404 404])

^{:refer app.manage.http-api/openapi :id configured-discovery}
(fact "OpenAPI follows configuration and contains no application-specific routes"
  (let [paths (get (api/openapi {:id 'example :http-api {:enabled true
                    :actions {:sample {:operation "analyse"}}
                    :artifacts {:report {:path ".cache/report.json" :content-type "application/json"}
                                :images {:directory ".cache/images" :filename-pattern "[0-9]+.png"}}}}) "paths")]
    [(contains? paths "/api/v1/actions/sample/runs")
     (contains? paths "/api/v1/artifacts/report")
     (contains? paths "/api/v1/artifacts/images/{filename}")
     (contains? (get paths "/api/v1/runs") "post")
     (some #(re-find #"finfolk|page-health" %) (keys paths))])
  => [true true true false nil])

^{:refer app.manage.http-api/handle! :id configured-artifacts}
(fact "artifact reads allow configured files and reject foreign and traversal paths"
  (fixture/with-project
    (fn [project]
      (let [path (io/file (:root project) ".cache/report.json")
            _ (io/make-parents path)
            _ (spit path "{\"result\":\"failed\"}")
            project (assoc project :http-api {:enabled true :artifacts
              {:report {:path ".cache/report.json" :content-type "application/json"}
               :files {:directory ".cache" :filename-pattern "report[.]json" :content-type "application/json"}
               :escape {:path "../outside.json"}}})
            s (server/session! (constantly project))]
        (try
          [(http/http s "GET" "/api/v1/artifacts/report" nil nil)
           (http/http s "GET" "/api/v1/artifacts/files/report.json" nil nil)
           (first (http/http s "GET" "/api/v1/artifacts/files/secret.json" nil nil))
           (first (http/http s "GET" "/api/v1/artifacts/report/extra" nil nil))
           (first (http/http s "GET" "/api/v1/artifacts/escape" nil nil))
           (first (http/http s "GET" "/api/v1/artifacts/unknown" nil nil))]
          (finally (server/stop! s))))))
  => [[200 {:result "failed"}] [200 {:result "failed"}] 404 404 400 404])

^{:refer app.manage.http-api/handle! :id failing-check}
(fact "check failures stay errors when submitted through a configured API action"
  (fixture/with-project
    (fn [project]
      (let [project (assoc project
                     :checks {:ui {:targets [:page] :run (fn [_] {:status :error :stdout "FAIL /people: error screen"})}}
                     :http-api {:enabled true :actions {:verify {:operation "build"}}})
            s (server/session! (constantly project))]
        (try
          (let [[accepted queued] (http/http s "POST" "/api/v1/actions/verify/runs" {} (:token s))
                result (server/await-run s (:id queued) 10000)
                [_ run] (http/http s "GET" (str "/api/v1/runs/" (:id queued)) nil nil)]
            [accepted (:status result) (:status run) (get-in run [:result :verification :checks 0 :stdout])])
          (finally (server/stop! s))))))
  => [202 :error "error" "FAIL /people: error screen"])

^{:refer app.manage.http-api/json-value :id preserve-event-names}
(fact "API serialization preserves namespaced event types and keys used by the shared controller"
  (api/json-value {:event/type :check/started :status :ok})
  => {"event/type" "check/started" "status" "ok"})
