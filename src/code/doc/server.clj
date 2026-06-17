(ns code.doc.server
  "A small static-file server for generated code.doc output.

   Serves:
     /           -> public/
     /test-site  -> docs/test-site/"
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (com.sun.net.httpserver HttpServer HttpHandler)
           (java.io File InputStream)
           (java.net InetSocketAddress)
           (java.nio.file Files Path Paths)))

(def ^:dynamic *server* (atom nil))

(def ^:private mime-types
  {".html" "text/html"
   ".htm"  "text/html"
   ".css"  "text/css"
   ".js"   "application/javascript"
   ".json" "application/json"
   ".edn"  "application/edn"
   ".png"  "image/png"
   ".jpg"  "image/jpeg"
   ".jpeg" "image/jpeg"
   ".gif"  "image/gif"
   ".svg"  "image/svg+xml"
   ".ico"  "image/x-icon"
   ".wasm" "application/wasm"
   ".txt"  "text/plain"
   ".md"   "text/markdown"})

(defn- file-extension
  "returns the lower-case extension of a path"
  {:added "4.0"}
  [path]
  (let [s (str path)
        i (str/last-index-of s ".")]
    (when (and i (> i 0))
      (str/lower-case (subs s i)))))

(defn- content-type
  "looks up a content type for a file path"
  {:added "4.0"}
  [path]
  (or (get mime-types (file-extension path))
      "application/octet-stream"))

(defn- safe-path
  "resolves a requested path under a root directory, preventing traversal"
  {:added "4.0"}
  [^String root ^String relative]
  (let [root-path (.toRealPath (Paths/get root (make-array String 0)) (make-array java.nio.file.LinkOption 0))
        target (.resolve root-path relative)
        target-real (try (.toRealPath target (make-array java.nio.file.LinkOption 0))
                         (catch Exception _ target))]
    (when (.startsWith target-real root-path)
      target-real)))

(defn- file-response
  "sends a file as the response, or a 404 if missing/unsafe"
  {:added "4.0"}
  [^Path file exchange]
  (if (let [^java.io.File f (when file (.toFile file))]
        (and f (.exists f) (.isFile f) (.canRead f)))
    (let [bytes (Files/readAllBytes file)
          headers (.getResponseHeaders exchange)]
      (.set headers "Content-Type" (content-type (str file)))
      (.sendResponseHeaders exchange 200 (alength bytes))
      (with-open [out (.getResponseBody exchange)]
        (.write out bytes)))
    (let [msg (.getBytes "Not found" "UTF-8")]
      (.sendResponseHeaders exchange 404 (alength msg))
      (with-open [out (.getResponseBody exchange)]
        (.write out msg)))))

(defn- static-handler
  "creates an HttpHandler for the given root directory.
   If prefix is supplied, it is stripped from the request path first."
  {:added "4.0"}
  ([^String root]
   (static-handler root nil))
  ([^String root prefix]
   (reify HttpHandler
     (handle [_ exchange]
       (try
         (let [raw-path (str (.getRequestURI exchange))
               relative (-> raw-path
                            (cond-> prefix (str/replace-first (re-pattern (str "^" prefix)) "")))
               relative (str/replace-first relative #"^/" "")
               relative (if (str/blank? relative) "index.html" relative)
               file (safe-path root relative)]
           (file-response file exchange))
         (catch Throwable t
           (let [msg (.getBytes (str "Server error: " (.getMessage t)) "UTF-8")]
             (.sendResponseHeaders exchange 500 (alength msg))
             (with-open [out (.getResponseBody exchange)]
               (.write out msg)))))))))

(defn stop!
  "stops the running static-file server"
  {:added "4.0"}
  []
  (when-let [s @*server*]
    (.stop s 0)
    (reset! *server* nil)
    (println "code.doc server stopped")))

(defn start!
  "starts the static-file server on the given port (default 8080)

   (start! 8080)"
  {:added "4.0"}
  ([] (start! 8080))
  ([port]
   (stop!)
   (let [server (HttpServer/create (InetSocketAddress. port) 0)]
     (.createContext server "/" (static-handler "public"))
     (.createContext server "/test-site/" (static-handler "docs/test-site" "/test-site"))
     (.setExecutor server nil)
     (.start server)
     (reset! *server* server)
     (println (str "code.doc server started on http://localhost:" port))
     (println "  /        -> public/")
     (println "  /test-site/ -> docs/test-site/")
     server)))

(defn -main
  "CLI entry point: lein run -m code.doc.server [port]"
  {:added "4.0"}
  [& args]
  (let [port (or (some-> args first parse-long) 8080)]
    (start! port)
    ;; keep the JVM alive
    @(promise)))
