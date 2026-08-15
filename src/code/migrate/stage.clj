(ns code.migrate.stage
  (:require [clojure.java.io :as io]))

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

(defn stage-pair
  "writes a generated source/test pair and returns its evidence manifest"
  {:added "4.1"}
  [pair {:keys [stage-root source-path test-path project-content]}]
  (let [source-file (write-generated! stage-root
                                      source-path
                                      (:output (:source pair)))
        test-file   (write-generated! stage-root
                                      test-path
                                      (:output (:test pair)))
        project-file (when project-content
                       (write-generated! stage-root
                                         "project.edn"
                                         project-content))]
    {:stage/root (.getCanonicalPath (io/file stage-root))
     :source/path source-file
     :test/path test-file
     :project/path project-file
     :source/checksum (:output/checksum (:source pair))
     :test/checksum (:output/checksum (:test pair))
     :manual-fixups 0}))
