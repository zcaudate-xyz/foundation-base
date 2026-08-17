(ns code.migrate.stage
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]))

(defn safe-relative-path?
  "checks that a generated path is relative and does not traverse upward"
  {:added "4.1"}
  [path]
  (let [path (.normalize (.toPath (io/file (str path))))]
    (and (not (.isAbsolute path))
         (not (.startsWith path "..")))))

(defn resolve-stage-path
  "resolves one contained path beneath an explicit staging root"
  {:added "4.1"}
  [stage-root relative-path]
  (when-not (safe-relative-path? relative-path)
    (throw (ex-info "Stage path must be relative and contained"
                    {:stage-root (str stage-root)
                     :path (str relative-path)})))
  (let [^java.io.File root   (.getCanonicalFile (io/file stage-root))
        ^java.io.File target (.getCanonicalFile
                              (io/file root (str relative-path)))]
    (when-not (or (= (.getPath root) (.getPath target))
                  (.startsWith (.getPath target)
                               (str (.getPath root)
                                    java.io.File/separator)))
      (throw (ex-info "Stage path escapes root"
                      {:stage-root (.getPath root)
                       :path (.getPath target)})))
    target))

(defn write-generated!
  "writes one generated artifact beneath the staging root"
  {:added "4.1"}
  [stage-root relative-path content]
  (let [target (resolve-stage-path stage-root relative-path)]
    (io/make-parents target)
    (spit target content)
    (.getPath ^java.io.File target)))

(defn pathway-manifest
  [result installed-path staged-path]
  {:input/path (:source/path result)
   :output/path installed-path
   :staged/path staged-path
   :input/checksum (:source/checksum result)
   :output/checksum (:output/checksum result)
   :applied-rules (:applied result)
   :diagnostics (:diagnostics result)})

(defn stage-pair
  "writes a generated source/test pair and a complete manifest.edn"
  {:added "4.1"}
  [pair {:keys [stage-root source-path test-path project-content]}]
  (let [source-result (:source pair)
        test-result (:test pair)
        source-file (write-generated! stage-root
                                      source-path
                                      (:output source-result))
        test-file   (write-generated! stage-root
                                      test-path
                                      (:output test-result))
        project-file (when project-content
                       (write-generated! stage-root
                                         "project.edn"
                                         project-content))
        manifest {:manifest/type :code-migration
                  :manifest/version 1
                  :stage/root (.getCanonicalPath (io/file stage-root))
                  :project/path project-file
                  :source (pathway-manifest source-result
                                            (:target/path source-result)
                                            source-file)
                  :test (assoc (pathway-manifest test-result
                                                 (:target/path test-result)
                                                 test-file)
                               :operations (:operations test-result)
                               :assertions (:assertions test-result)
                               :operation-correspondence
                               (:operation-correspondence test-result))
                  :manual-fixups 0}
        manifest-file (write-generated! stage-root
                                        "manifest.edn"
                                        (with-out-str (pprint/pprint manifest)))]
    (assoc manifest :manifest/path manifest-file)))