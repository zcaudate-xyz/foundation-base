(ns app.manage.http-api
  "Versioned HTTP API attached to the existing workbench controller and queue."
  (:require [app.manage :as manage]
            [app.manage.protocol :as protocol]
            [app.manage.slices :as slices]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.nio.file Files]))

(defn json-value [value]
  (cond
    (map? value) (into {} (map (fn [[k v]] [(if (keyword? k) (subs (str k) 1) (str k)) (json-value v)]) value))
    (or (sequential? value) (set? value)) (mapv json-value value)
    (or (keyword? value) (symbol? value)) (str (if (keyword? value) (subs (str value) 1) value))
    (or (nil? value) (string? value) (number? value) (boolean? value)) value
    :else (str value)))

(defn respond! [exchange status content-type body]
  (let [bytes (if (string? body) (.getBytes ^String body "UTF-8") body)]
    (doto (.getResponseHeaders exchange)
      (.set "Content-Type" content-type)
      (.set "Cache-Control" "no-store")
      (.set "X-Content-Type-Options" "nosniff"))
    (.sendResponseHeaders exchange status (count bytes))
    (with-open [out (.getResponseBody exchange)] (.write out ^bytes bytes))))

(defn reply! [exchange status value]
  (respond! exchange status "application/json; charset=utf-8" (json/write-str (json-value value))))

(defn authorize! [workbench exchange]
  (when-not (= (:token workbench) (.getFirst (.getRequestHeaders exchange) "X-App-Manage-Token"))
    (throw (ex-info "Missing or invalid workbench session token" {:http-status 403}))))

(defn read-action [exchange]
  (with-open [body (.getRequestBody exchange)]
    (let [bytes (.readNBytes body 65537)]
      (when (> (count bytes) 65536) (throw (ex-info "Request too large" {:http-status 413})))
      (let [action (json/read-str (String. bytes "UTF-8") :key-fn keyword)]
        (when-not (map? action) (throw (ex-info "Expected a JSON object" {:http-status 400})))
        action))))

(defn validate-action! [project {:keys [operation target slice stage tests] :as action}]
  (when-not (some #{operation} protocol/operations)
    (throw (ex-info "Unknown workbench operation" {:http-status 400})))
  (when (and target slice) (throw (ex-info "Choose target or slice" {:http-status 400})))
  (when (and tests (not (boolean? tests))) (throw (ex-info "tests must be boolean" {:http-status 400})))
  (when (and stage (not slice)) (throw (ex-info "stage requires slice" {:http-status 400})))
  (when target
    (when-not (and (string? target) (get-in project [:targets (keyword target)]))
      (throw (ex-info "Unknown target" {:http-status 400}))))
  (when slice (slices/targets project slice stage))
  action)


(defn openapi [project]
  (let [result {"description" "JSON response" "content" {"application/json" {"schema" {"type" "object"}}}}
        get-route (fn [summary] {"get" {"summary" summary "responses" {"200" result "404" result}}})
        post-route (fn [summary] {"post" {"summary" summary "security" [{"WorkbenchToken" []}]
                                         "responses" {"202" result "400" result "403" result}}})
        config (:http-api project)
        run-post (assoc (get (post-route "Queue an allowed operation") "post")
                        "requestBody" {"required" true "content" {"application/json" {"schema"
                          {"type" "object" "required" ["operation"] "properties"
                            {"operation" {"type" "string" "enum" (:operations config)}
                             "target" {"type" "string"} "slice" {"type" "string"}
                             "stage" {"type" "string"} "tests" {"type" "boolean"}
                             "request" {"type" "object"}}}}}})
        paths {"/api/session" (get-route "Local session token")
               "/api/v1" (get-route "API discovery")
               "/api/v1/openapi.json" (get-route "Configuration-derived OpenAPI document")
               "/api/v1/checks" (get-route "Declared checks, configured actions and slice evidence")
               "/api/v1/runs" (cond-> (get-route "Recent runs") (seq (:operations config)) (assoc "post" run-post))
               "/api/v1/runs/{id}" (assoc-in (get-route "Poll one run; terminal status is ok or error")
                 ["get" "parameters"] [{"name" "id" "in" "path" "required" true "schema" {"type" "string"}}])}
        actions (into {} (for [[id _] (:actions config)]
                          [(str "/api/v1/actions/" (name id) "/runs") (post-route (str "Run configured action " (name id)))]))
        artifacts (into {} (for [[id artifact] (:artifacts config)]
                            [(str "/api/v1/artifacts/" (name id) (when (:directory artifact) "/{filename}"))
                             {"get" (cond-> {"summary" (str "Read artifact " (name id))
                                              "responses" {"200" {"description" "Configured artifact"
                                                "content" {(or (:content-type artifact) "application/octet-stream")
                                                  {"schema" {"type" "string" "format" "binary"}}}} "404" result}}
                                      (:directory artifact) (assoc "parameters" [{"name" "filename" "in" "path" "required" true
                                        "schema" {"type" "string" "pattern" (:filename-pattern artifact)}}]))}]))]
    {"openapi" "3.0.3" "info" {"title" (str (:id project) " workbench API") "version" "1.0.0"}
     "components" {"securitySchemes" {"WorkbenchToken" {"type" "apiKey" "in" "header" "name" "X-App-Manage-Token"}}}
     "paths" (merge paths actions artifacts)}))

(defn handle! [workbench exchange {:keys [submit! project-index]}]
  (try
    (let [path (.getPath (.getRequestURI exchange)) method (.getRequestMethod exchange)
          project ((:supplier workbench)) config (:http-api project)]
      (when-not (:enabled config) (throw (ex-info "HTTP API is disabled" {:http-status 404})))
      (cond
        (and (= method "GET") (= path "/api/v1"))
        (reply! exchange 200 {:version 1 :project (str (:id project)) :openapi "/api/v1/openapi.json"
                             :session "/api/session" :checks "/api/v1/checks" :runs "/api/v1/runs"})

        (and (= method "GET") (= path "/api/v1/openapi.json"))
        (respond! exchange 200 "application/json; charset=utf-8" (json/write-str (openapi project)))

        (and (= method "GET") (= path "/api/v1/checks"))
        (reply! exchange 200 {:checks (:checks (project-index project))
                             :actions (:actions config)
                             :status (:check-status @(:state workbench))
                             :slices (slices/status project)})

        (and (= method "GET") (= path "/api/v1/runs"))
        (reply! exchange 200 {:runs (mapv #(get-in @(:state workbench) [:runs %]) (reverse (:order @(:state workbench))))})

        (and (= method "GET") (re-matches #"/api/v1/runs/[^/]+" path))
        (if-let [run (get-in @(:state workbench) [:runs (last (str/split path #"/"))])]
          (reply! exchange 200 run)
          (reply! exchange 404 {:error "Unknown or expired run"}))

        (and (= method "POST") (= path "/api/v1/runs"))
        (do (authorize! workbench exchange)
            (reply! exchange 202 (submit! workbench (let [action (validate-action! project (read-action exchange))]
                               (when-not (some #{(:operation action)} (:operations config))
                                 (throw (ex-info "Operation is not exposed by this project" {:http-status 403})))
                               action))))

        (and (= method "POST") (re-matches #"/api/v1/actions/[^/]+/runs" path))
        (do (authorize! workbench exchange)
            (let [id (keyword (nth (str/split path #"/") 4))
                  action (get-in config [:actions id])]
              (when-not action (throw (ex-info "Unknown configured action" {:http-status 404})))
              (reply! exchange 202 (submit! workbench (validate-action! project action)))))

        (and (= method "GET") (re-matches #"/api/v1/artifacts/[^/]+(?:/[^/]+)?" path))
        (let [[_ _ _ _ id filename] (str/split path #"/")
              {:keys [path directory filename-pattern content-type] :as artifact} (get-in config [:artifacts (keyword id)])]
          (when-not artifact (throw (ex-info "Unknown configured artifact" {:http-status 404})))
          (when (or (and path filename) (and directory (not (and filename filename-pattern (re-matches (re-pattern filename-pattern) filename)))))
            (throw (ex-info "Artifact filename is not allowed" {:http-status 404})))
          (let [file (manage/project-file project (or path (str directory "/" filename)))]
            (if (.isFile file)
              (respond! exchange 200 (or content-type "application/octet-stream") (Files/readAllBytes (.toPath file)))
              (reply! exchange 404 {:error "Artifact not found; run its check first"}))))

        :else (reply! exchange 404 {:error "Unknown API route"})))
    (catch Throwable error
      (reply! exchange (or (:http-status (ex-data error)) 400) {:error (.getMessage error)}))))
