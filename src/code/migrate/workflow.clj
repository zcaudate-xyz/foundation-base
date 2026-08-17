(ns code.migrate.workflow
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [code.migrate :as migrate]
            [code.migrate.catalog :as catalog]
            [code.migrate.engine :as engine]
            [code.migrate.stage :as stage]
            [code.migrate.verify :as verify]))

(defn ordered-targets
  [migration-catalog]
  (vec (sort-by :target/order (:migration/targets migration-catalog))))

(defn file-checksum
  [path]
  (when path
    (let [file (io/file path)]
      (when (.isFile file)
        (engine/sha256 (slurp file))))))

(defn contained-path
  [root relative-path]
  (stage/resolve-stage-path root relative-path))

(defn target-manifest-path
  [stage-root target]
  (format "manifests/%02d-%s.edn"
          (:target/order target)
          (name (:target/id target))))

(defn write-edn!
  [root path value]
  (stage/write-generated! root path (with-out-str (pprint/pprint value))))

(defn read-manifest
  [path]
  (edn/read-string (slurp path)))

(defn staged-manifests
  [stage-root]
  (->> (file-seq (io/file stage-root "manifests"))
       (filter #(.isFile ^java.io.File %))
       (map #(read-manifest (.getPath ^java.io.File %)))
       (sort-by :target/order)
       vec))

(defn project-content
  []
  (with-out-str
    (pprint/pprint
     {:hara/type :project
      :hara/version "1.0.0"
      :project/id 'migration.std-block
      :project/version "0.0.0"
      :project/source-paths ["lib/src" "lib/src-lang"]
      :project/test-paths ["lib/test"]
      :project/extension-paths []
      :project/capabilities #{}})))

(defn copy-tree!
  [source-root target-root]
  (let [source-root (.getCanonicalFile (io/file source-root))]
    (doseq [source (file-seq source-root)
            :when (.isFile ^java.io.File source)]
      (let [relative (str (.relativize (.toPath source-root)
                                      (.toPath ^java.io.File source)))
            target (stage/resolve-stage-path target-root relative)]
        (io/make-parents target)
        (io/copy source target)))
    target-root))

(defn source-unit
  [target]
  {:unit/kind :source
   :source/path (:target/source-path target)
   :source/string (slurp (:target/source-path target))})

(defn test-unit
  [target]
  (when-let [path (:target/test-path target)]
    {:unit/kind :test
     :source/path path
     :source/string (slurp path)}))

(defn prepare-target!
  [migration-catalog target {:keys [stage-root hara-root]}]
  (let [source (migrate/migrate-unit (source-unit target) migration-catalog)
        test (when-let [unit (test-unit target)]
               (migrate/migrate-unit unit migration-catalog))
        expected-assertions (:target/expected-assertions target)
        actual-assertions (:assertions test)]
    (when (and expected-assertions
               (not= expected-assertions actual-assertions))
      (throw (ex-info "Historical assertion count changed"
                      {:target/id (:target/id target)
                       :expected expected-assertions
                       :actual actual-assertions})))
    (let [source-relative (:target/target-source-path target)
          test-relative (:target/target-test-path target)
          source-path (stage/write-generated! stage-root source-relative (:output source))
          test-path (when test
                      (stage/write-generated! stage-root test-relative (:output test)))
          ^java.io.File installed-source (contained-path hara-root source-relative)
          ^java.io.File installed-test (when test-relative
                                         (contained-path hara-root test-relative))
          manifest {:target/id (:target/id target)
                    :target/order (:target/order target)
                    :stage/root (.getCanonicalPath (io/file stage-root))
                    :hara/root (.getCanonicalPath (io/file hara-root))
                    :source/path source-path
                    :test/path test-path
                    :target/source-path (.getPath installed-source)
                    :target/test-path (some-> installed-test .getPath)
                    :source/checksum (:output/checksum source)
                    :test/checksum (:output/checksum test)
                    :target/source-checksum (file-checksum installed-source)
                    :target/test-checksum (file-checksum installed-test)
                    :source/applied (:applied source)
                    :test/applied (:applied test)
                    :test/operations (:operations test)
                    :test/assertions actual-assertions
                    :diagnostics (vec (concat (:diagnostics source)
                                              (:diagnostics test)))
                    :manual-fixups 0
                    :verification/status :pending}
          manifest-path (write-edn! stage-root
                                    (target-manifest-path stage-root target)
                                    manifest)]
      (assoc manifest :manifest/path manifest-path))))

(defn prepare!
  [{:keys [stage-root hara-root catalog-path]}]
  (let [target-root (.getCanonicalFile (io/file hara-root "target"))
        stage-file (.getCanonicalFile (io/file stage-root))
        _ (when-not (.startsWith (.getPath stage-file)
                                 (str (.getPath target-root)
                                      java.io.File/separator))
            (throw (ex-info "Migration stage must be beneath Hara core/target"
                            {:stage/root (.getPath stage-file)
                             :required/root (.getPath target-root)})))
        migration-catalog (if catalog-path
                            (migrate/load-catalog catalog-path)
                            (migrate/load-catalog))]
    (copy-tree! (io/file hara-root "lib/src")
                (io/file stage-root "lib/src"))
    (copy-tree! (io/file hara-root "lib/src-lang")
                (io/file stage-root "lib/src-lang"))
    (stage/write-generated! stage-root "project.edn" (project-content))
    (mapv #(prepare-target! migration-catalog %
                            {:stage-root stage-root :hara-root hara-root})
          (ordered-targets migration-catalog))))

(defn verify-target!
  [manifest {:keys [hara]}]
  (when (seq (:diagnostics manifest))
    (throw (ex-info "Cannot verify migration diagnostics" manifest)))
  (when-not (= (:source/checksum manifest)
               (file-checksum (:source/path manifest)))
    (throw (ex-info "Staged source checksum changed" manifest)))
  (when-not (= (:test/checksum manifest)
               (file-checksum (:test/path manifest)))
    (when (:test/path manifest)
      (throw (ex-info "Staged test checksum changed" manifest))))
  (let [stage-root (:stage/root manifest)
        source-result (verify/run-process
                       [hara "--project" stage-root "--offline" "--allow-file"
                        "run" (:source/path manifest)]
                       {:dir stage-root})
        test-result (when (:test/path manifest)
                      (verify/run-process
                       [hara "--project" stage-root "--offline"
                        "project" "test" (:test/path manifest)]
                       {:dir stage-root}))
        passed (and (:passed source-result)
                    (or (nil? test-result) (:passed test-result)))
        verified (assoc manifest
                        :verification/status (if passed :passed :failed)
                        :verification/source source-result
                        :verification/test test-result)]
    (write-edn! stage-root
                (target-manifest-path stage-root manifest)
                (dissoc verified :manifest/path))
    verified))

(defn current-target-checksums
  [manifest]
  {:source (file-checksum (:target/source-path manifest))
   :test (file-checksum (:target/test-path manifest))})

(defn install-target!
  [manifest]
  (when-not (= :passed (:verification/status manifest))
    (throw (ex-info "Only verified migrations may be installed" manifest)))
  (when-not (zero? (:manual-fixups manifest))
    (throw (ex-info "Migration contains manual fixups" manifest)))
  (let [current (current-target-checksums manifest)
        expected {:source (:target/source-checksum manifest)
                  :test (:target/test-checksum manifest)}]
    (when-not (= expected current)
      (throw (ex-info "Hara target changed after migration preparation"
                      {:target/id (:target/id manifest)
                       :expected expected
                       :actual current}))))
  (stage/write-generated! (:hara/root manifest)
                          (str (.relativize (.toPath (io/file (:hara/root manifest)))
                                           (.toPath (io/file (:target/source-path manifest)))))
                          (slurp (:source/path manifest)))
  (when (:test/path manifest)
    (stage/write-generated! (:hara/root manifest)
                            (str (.relativize (.toPath (io/file (:hara/root manifest)))
                                             (.toPath (io/file (:target/test-path manifest)))))
                            (slurp (:test/path manifest))))
  (let [audit {:source (= (:source/checksum manifest)
                          (file-checksum (:target/source-path manifest)))
               :test (or (nil? (:test/path manifest))
                         (= (:test/checksum manifest)
                            (file-checksum (:target/test-path manifest))))}]
    (when-not (every? true? (vals audit))
      (throw (ex-info "Installed migration checksum mismatch" audit)))
    (assoc manifest :installation/status :installed :audit audit)))

(defn audit
  [manifest]
  {:target/id (:target/id manifest)
   :source (= (:source/checksum manifest)
              (file-checksum (:target/source-path manifest)))
   :test (or (nil? (:test/path manifest))
             (= (:test/checksum manifest)
                (file-checksum (:target/test-path manifest))))})
