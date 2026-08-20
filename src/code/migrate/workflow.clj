(ns code.migrate.workflow
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string]
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

(defn namespace-source-path
  [namespace]
  (str (clojure.string/replace (str namespace) "." "/") ".hal"))

(defn bootstrap-source-paths
  "returns source-relative paths already embedded by the native runtime"
  [hara-root]
  (->> (slurp (io/file hara-root "rust/bootstrap.namespaces"))
       clojure.string/split-lines
       (remove clojure.string/blank?)
       (map namespace-source-path)
       set))

(defn remove-staged-bootstrap-shadows!
  [stage-root paths]
  (doseq [path paths]
    (io/delete-file (contained-path stage-root (str "lib/src/" path)) true)))

(defn copy-tree!
  ([source-root target-root]
   (copy-tree! source-root target-root #{}))
  ([source-root target-root excluded]
   (let [source-root (.getCanonicalFile (io/file source-root))]
     (doseq [source (file-seq source-root)
             :when (.isFile ^java.io.File source)
             :let [relative (str (.relativize (.toPath source-root)
                                              (.toPath ^java.io.File source)))]
             :when (not (contains? excluded relative))]
       (let [target (stage/resolve-stage-path target-root relative)]
         (io/make-parents target)
         (io/copy source target)))
     target-root)))

(defn source-unit
  [target]
  (when-let [path (:target/source-path target)]
    {:unit/kind :source
     :source/path path
     :source/string (slurp path)}))

(defn test-unit
  [target]
  (when-let [path (:target/test-path target)]
    {:unit/kind :test
     :source/path path
     :source/string (slurp path)}))

(defn prepare-test-data!
  "stages catalog-owned historical test data and records guarded targets"
  [stage-root hara-root paths]
  (mapv (fn [path]
          (let [content (slurp path)
                staged (stage/write-generated! stage-root path content)
                ^java.io.File target (contained-path hara-root path)]
            {:relative/path path
             :stage/path staged
             :target/path (.getPath target)
             :checksum (engine/sha256 content)
             :target/checksum (file-checksum target)}))
        paths))

(defn prepare-target!
  [migration-catalog target {:keys [stage-root hara-root]}]
  (let [source (when-let [unit (source-unit target)]
                 (migrate/migrate-unit unit migration-catalog))
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
          test-data (prepare-test-data! stage-root
                                        hara-root
                                        (:target/test-data-paths target))
          source-path (when source
                        (stage/write-generated! stage-root source-relative (:output source)))
          test-path (when test
                      (stage/write-generated! stage-root test-relative (:output test)))
          ^java.io.File installed-source (when source-relative
                                           (contained-path hara-root source-relative))
          ^java.io.File installed-test (when test-relative
                                         (contained-path hara-root test-relative))
          manifest {:target/id (:target/id target)
                    :target/order (:target/order target)
                    :stage/root (.getCanonicalPath (io/file stage-root))
                    :hara/root (.getCanonicalPath (io/file hara-root))
                    :source/path source-path
                    :test/path test-path
                    :target/source-path (some-> installed-source .getPath)
                    :target/test-path (some-> installed-test .getPath)
                    :source/checksum (:output/checksum source)
                    :test/checksum (:output/checksum test)
                    :verification/source-paths
                    (mapv #(.getPath ^java.io.File
                                    (contained-path stage-root %))
                          (:target/source-dependency-paths target))
                    :target/source-checksum (file-checksum installed-source)
                    :target/test-checksum (file-checksum installed-test)
                    :source/applied (:applied source)
                    :test/applied (:applied test)
                    :test/operations (:operations test)
                    :test/assertions actual-assertions
                    :test-data test-data
                    :verification/allow-process
                    (boolean (:target/allow-process target))
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
    (let [bootstrap-paths (bootstrap-source-paths hara-root)]
      (remove-staged-bootstrap-shadows! stage-root bootstrap-paths)
      (copy-tree! (io/file hara-root "lib/src")
                  (io/file stage-root "lib/src")
                  bootstrap-paths))
    (copy-tree! (io/file hara-root "lib/src-lang")
                (io/file stage-root "lib/src-lang"))
    (stage/write-generated! stage-root "project.edn" (project-content))
    (mapv #(prepare-target! migration-catalog %
                            {:stage-root stage-root :hara-root hara-root})
          (ordered-targets migration-catalog))))

(defn verification-test-expression
  "loads a staged source/test pair together and fails on any historical check"
  [source-paths test-path]
  (format
   (str "(do %s (load-file %s) "
        "(let [results (Test/run []) "
        "failed (vec (filter (fn [result] (not (:pass result))) results))] "
        "(if (empty? failed) {:passed (count results)} "
        "(throw (ex-info \"Migrated historical tests failed\" "
        "{:failed failed})))))")
   (clojure.string/join
    " "
   (map #(format "(load-file %s)" (pr-str %)) source-paths))
   (pr-str test-path)))

(defn standalone-source-verification?
  "runs a source directly only when no paired test loads it in project context"
  [manifest]
  (and (:source/path manifest)
       (nil? (:test/path manifest))))

(defn verification-source-expression
  "loads declared dependencies before a standalone staged source"
  [source-paths source-path]
  (format "(do %s (load-file %s))"
          (clojure.string/join
           " "
           (map #(format "(load-file %s)" (pr-str %)) source-paths))
          (pr-str source-path)))

(defn verification-prefix
  "builds the least-capability native command prefix declared by a target"
  [hara stage-root manifest]
  (cond-> [hara "--project" stage-root "--offline" "--allow-file"]
    (:verification/allow-process manifest)
    (conj "--allow-process")))

(defn verification-project-root
  "uses the staged project when it owns source, otherwise the Hara project"
  [manifest]
  (if (:source/path manifest)
    (:stage/root manifest)
    (:hara/root manifest)))

(defn verify-target!
  [manifest {:keys [hara]}]
  (when (seq (:diagnostics manifest))
    (throw (ex-info "Cannot verify migration diagnostics" manifest)))
  (when (and (:source/path manifest)
             (not= (:source/checksum manifest)
                   (file-checksum (:source/path manifest))))
    (throw (ex-info "Staged source checksum changed" manifest)))
  (when-not (= (:test/checksum manifest)
               (file-checksum (:test/path manifest)))
    (when (:test/path manifest)
      (throw (ex-info "Staged test checksum changed" manifest))))
  (doseq [asset (:test-data manifest)]
    (when-not (= (:checksum asset)
                 (file-checksum (:stage/path asset)))
      (throw (ex-info "Staged test data checksum changed" asset))))
  (let [stage-root (:stage/root manifest)
        project-root (verification-project-root manifest)
        source-result (when (standalone-source-verification? manifest)
                        (verify/run-process
                         (if (seq (:verification/source-paths manifest))
                           (conj (verification-prefix hara project-root manifest)
                                 "eval"
                                 (verification-source-expression
                                  (:verification/source-paths manifest)
                                  (:source/path manifest)))
                           (conj (verification-prefix hara project-root manifest)
                                 "run" (:source/path manifest)))
                         {:dir project-root}))
        test-result (when (:test/path manifest)
                      (verify/run-process
                       (conj (verification-prefix hara project-root manifest)
                             "eval" (verification-test-expression
                                     (cond-> (vec (:verification/source-paths manifest))
                                       (:source/path manifest)
                                       (conj (:source/path manifest)))
                                     (:test/path manifest)))
                       {:dir project-root}))
        passed (and (or (nil? source-result) (:passed source-result))
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
  (doseq [asset (:test-data manifest)]
    (when-not (= (:target/checksum asset)
                 (file-checksum (:target/path asset)))
      (throw (ex-info "Hara test data changed after migration preparation"
                      asset))))
  (when (:source/path manifest)
    (stage/write-generated! (:hara/root manifest)
                            (str (.relativize (.toPath (io/file (:hara/root manifest)))
                                             (.toPath (io/file (:target/source-path manifest)))))
                            (slurp (:source/path manifest))))
  (when (:test/path manifest)
    (stage/write-generated! (:hara/root manifest)
                            (str (.relativize (.toPath (io/file (:hara/root manifest)))
                                             (.toPath (io/file (:target/test-path manifest)))))
                            (slurp (:test/path manifest))))
  (doseq [asset (:test-data manifest)]
    (stage/write-generated! (:hara/root manifest)
                            (:relative/path asset)
                            (slurp (:stage/path asset))))
  (let [audit {:source (or (nil? (:source/path manifest))
                           (= (:source/checksum manifest)
                              (file-checksum (:target/source-path manifest))))
               :test (or (nil? (:test/path manifest))
                         (= (:test/checksum manifest)
                            (file-checksum (:target/test-path manifest))))}
        audit (if (seq (:test-data manifest))
                (assoc audit
                       :test-data
                       (every? true?
                               (map #(= (:checksum %)
                                        (file-checksum (:target/path %)))
                                    (:test-data manifest))))
                audit)]
    (when-not (every? true? (vals audit))
      (throw (ex-info "Installed migration checksum mismatch" audit)))
    (assoc manifest :installation/status :installed :audit audit)))

(defn audit
  [manifest]
  {:target/id (:target/id manifest)
   :source (or (nil? (:source/path manifest))
               (= (:source/checksum manifest)
                  (file-checksum (:target/source-path manifest))))
   :test (or (nil? (:test/path manifest))
             (= (:test/checksum manifest)
                (file-checksum (:target/test-path manifest))))
   :test-data (every? true?
                      (map #(= (:checksum %)
                               (file-checksum (:target/path %)))
                           (:test-data manifest)))})
