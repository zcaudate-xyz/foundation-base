(ns schema-downloader.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [net.http.client :as http]
            [std.lib.env])
  (:import [java.io File])
  (:gen-class))

(def ^:const github-api-url
  "The GitHub API URL for the extensions API directory."
  "https://api.github.com/repos/chromium/chromium/contents/extensions/api")

(def ^:const output-dir
  "The local directory to save schemas into."
  "schemas")

(defn get-schema-files
  "Fetches the directory listing from the GitHub API and filters for schema files."
  []
  (try
    (std.lib.env/p "Fetching file list from" github-api-url "...")
    (let [response (http/get github-api-url
                               {:headers {"Accept" "application/vnd.github.v3+json"}})

          _ (when-not (= 200 (:status response))
              (throw (Exception. (str "GitHub API request failed: " (:status response) " " (:body response)))))

          files (->> (json/read-str (:body response) :key-fn keyword)
                     (filter #(and (= "file" (:type %))
                                   (re-find #"\\.(json|idl)$" (:name %))))
                     (map #(select-keys % [:name :download_url])))]
      (std.lib.env/p (count files) "schema files found.")
      files)
    (catch Exception e
      (std.lib.env/p "Failed to fetch or parse file list:")
      (std.lib.env/p (.getMessage e))
      (std.lib.env/p "This can happen if you are rate-limited by the GitHub API.")
      [])))

(defn download-and-save-schemas
  "Downloads each schema file from its raw URL and saves it."
  [files]
  (std.lib.env/p "Creating output directory:" output-dir)
  (.mkdirs (File. output-dir))

  (doseq [file-info files]
    (let [filename (:name file-info)
          download-url (:download_url file-info)
          output-file  (io/file output-dir filename)]
      (try
        (print "Downloading" filename "...")
        ;; The download_url points to raw content, no decoding needed.
        (let [response (http/get download-url)]
          (spit output-file (:body response))
          (std.lib.env/p " done."))
        (catch Exception e
          (std.lib.env/p " failed:" (.getMessage e)))))))

(defn -main
  "Main application entry point."
  [& args]
  (let [files (get-schema-files)]
    (if (empty? files)
      (std.lib.env/p "No files found to download. Exiting.")
      (download-and-save-schemas files)))
  (std.lib.env/p "Schema download process complete.")
  (System/exit 0))
