(ns code.framework.link
  (:require [std.lib.sort :as sort]
            [std.lib :as h]
            [std.fs :as fs]
            [std.config :as config]
            [std.config.ext.gpg :deps true]
            [std.print :as print]
            [code.project :as project]
            [code.framework.link.common :as common]
            [code.framework.link.clj]
            [code.framework.link.cljs]
            [code.framework.link.java]
            [jvm.artifact :as artifact]
            [jvm.deps :as deps]))

;; Linkage takes
;; - read, where an .edn file consisting of 
;; - collect
;;   - entries
;;   - linkages
;;   - internal deps
;;   - external deps
;;   - transfers

(def +default-packages-file+  "config/packages.edn")

(def +default-config-file+ "config/deploy.edn")

(def +suffix-types+ {:clj  ".clj$"
                     :cljs ".cljs$"
                     :cljc ".cljc$"})

(defn file-linkage
  "returns the exports and imports of a given file
 
   (file-linkage \"src/code/link/common.clj\")
   => '{:exports #{[:class code.framework.link.common.FileInfo]
                   [:clj code.framework.link.common]},
        :imports #{[:clj std.fs]
                   [:clj std.lib]}}"
  {:added "3.0"}
  ([path]
   (common/file-linkage-fn path
                           (-> (fs/path path)
                               (fs/attributes)
                               (:last-modified-time)))))

(defn read-packages
  "reads in a list of packages to 
   (-> (read-packages {:file \"config/packages.edn\"})
       (get 'xyz.zcaudate/std.lib))
   => (contains {:description string?
                 :name 'xyz.zcaudate/std.lib})"
  {:added "3.0"}
  ([]
   (read-packages {:root "."
                   :file +default-packages-file+}))
  ([{:keys [root file] :as input}]
   (let [path   (fs/path root file)]
     (if (fs/exists? path)
       (->> (read-string (slurp path))
            (h/map-entries (fn [[k entry]]
                             [k (assoc entry :name k)])))
       (throw (ex-info "Path does not exist" {:path path
                                              :input input}))))))

(defn create-file-lookups
  "creates file-lookups for clj, cljs and cljc files
 
   (-> (create-file-lookups (project/project))
       (get-in [:clj 'std.lib.version]))
   => (str (fs/path \"src/std/lib/version.clj\"))"
  {:added "3.0"}
  ([project]
   (h/pmap-vals (fn [suffix]
                  (project/all-files (:source-paths project)
                                     {:include [suffix]}
                                     project))
                +suffix-types+)))

(defn collect-entries-single
  "collects all namespaces for given lookup and package
 
   (collect-entries-single (get -packages- 'xyz.zcaudate/std.lib)
                           (:clj -lookups-))
   => coll?"
  {:added "3.0"}
  ([package lookup]
   (let [nsps (keys lookup)]
     (mapcat (fn [[ns type select]]
               (case type
                 :base      (filter (fn [sym] (or (= sym ns)
                                                  (.startsWith (str sym)
                                                               (str ns ".base."))))
                                    nsps)
                 :complete  (filter (fn [sym] (or (= sym ns)
                                                  (.startsWith (str sym)
                                                               (str ns "."))))
                                    nsps)
                 :exclude   (filter (fn [sym]
                                      (and (or (= sym ns)
                                               (.startsWith (str sym)
                                                            (str ns ".")))
                                           (not ((set select) ns))))
                                    nsps)
                 (throw (ex-info "Not supported." {:type type
                                                   :options [:base
                                                             :complete
                                                             :exclude]}))))
             (:include package)))))

(defn collect-entries
  "collects all entries given packages and lookups
 
   (-> (collect-entries -packages- -lookups-)
       (get-in '[xyz.zcaudate/std.lib :entries]))
   => coll?"
  {:added "3.0"}
  ([packages lookups]
   (h/pmap-vals (fn [pkg]
                  (->> lookups
                       (mapcat (fn [[suffix lookup]]
                                 (->> (collect-entries-single pkg lookup)
                                      (map (partial vector suffix)))))
                       (set)
                       (assoc pkg :entries)))
                packages)))

(defn overlapped-entries-single
  "finds any overlaps between entries
 
   (overlapped-entries-single '{:name a
                                :entries #{[:clj hara.1]}}
                              '[{:name b
                                 :entries #{[:clj hara.1] [:clj hara.2]}}])
   => '([#{a b} #{[:clj hara.1]}])"
  {:added "3.0"}
  ([x heap]
   (keep (fn [{:keys [name entries]}]
           (let [ol (h/intersection (:entries x)
                                    entries)]
             (if (seq ol)
               [#{name (:name x)} ol])))
         heap)))

(defn overlapped-entries
  "finds any overlapped entries for given map
 
   (overlapped-entries '{a {:name a
                            :entries #{[:clj hara.1]}}
                         b {:name b
                            :entries #{[:clj hara.1] [:clj hara.2]}}})
   => '([#{a b} #{[:clj hara.1]}])"
  {:added "3.0"}
  ([packages]
   (loop [[x & rest] (vals packages)
          heap     []
          overlaps []]
     (cond (nil? x)
           overlaps

           :else
           (let [ols (overlapped-entries-single x heap)]
             (recur rest
                    (conj heap x)
                    (concat overlaps ols)))))))

(defn missing-entries
  "finds missing entries given packages and lookup
 
   (missing-entries '{b {:name b
                         :entries #{[:clj hara.1] [:clj hara.2]}}}
                    '{:clj {hara.1 \"\"
                            hara.2 \"\"
                            hara.3 \"\"}})
   => '{:clj {hara.3 \"\"}}"
  {:added "3.0"}
  ([packages lookups]
   (reduce (fn [lookups {:keys [entries]}]
             (reduce (fn [lookups entry]
                       (h/dissoc-nested lookups entry))
                     lookups
                     entries))
           (reduce-kv (fn [out k v]
                        (if (empty? v) out (assoc out k v)))
                      {}
                      lookups)
           (vals packages))))

(defn collect-external-deps
  "collects dependencies from the local system
 
   (collect-external-deps '{:a {:dependencies [org.clojure/clojure]}})
   => (contains-in {:a {:dependencies [['org.clojure/clojure string?]]}})"
  {:added "3.0"}
  ([packages]
   (h/pmap-vals (fn [{:keys [dependencies] :as package}]
                  (->> dependencies
                       (map (fn [artifact]
                              (let [rep     (artifact/artifact :rep artifact)
                                    version (if (empty? (:version rep))
                                              (deps/current-version rep)
                                              (:version rep))]
                                (artifact/artifact :coord (assoc rep :version version)))))
                       (assoc package :dependencies)))
                packages)))

(defn collect-linkages
  "collects all imports and exports of a package"
  {:added "3.0"}
  ([packages lookups]
   (h/pmap-vals (fn [{:keys [entries] :as package}]
                  (->> entries
                       (map (fn [entry]
                              (file-linkage (get-in lookups entry))))
                       (apply merge-with h/union)
                       (merge package)))
                packages)))

(defn collect-internal-deps
  "collects all internal dependencies"
  {:added "3.0"}
  ([packages]
   (let [packages (h/pmap-vals (fn [{:keys [name imports] :as package}]
                                 (->> (dissoc packages name)
                                      (keep (fn [[k {:keys [exports]}]]
                                              (if-not (empty? (h/intersection imports exports))
                                                k)))
                                      (set)
                                      (assoc package :internal)))
                               packages)
        ;; CHECKS FOR CONSISTENCY
         _  (sort/topological-sort (h/pmap-vals :internal packages))]
     packages)))

(defn collect-transfers
  "collects all files that are packaged"
  {:added "3.0"}
  ([packages lookups project]
   (h/pmap-vals (fn [{:keys [entries bundle] :as package}]
                  (let [efiles (map    (fn [[suffix ns :as entry]]
                                         (let [file (get-in lookups entry)]
                                           [file (str (-> (str ns)
                                                          (.replaceAll  "\\." "/")
                                                          (.replaceAll  "-" "_"))
                                                      "."
                                                      (name suffix))]))
                                       entries)
                        bfiles (mapcat (fn [{:keys [include path]}]
                                         (mapcat (fn [b]
                                                   (let [base (fs/path (:root project) path)]
                                                     (->> (fs/select (fs/path base b)
                                                                     {:include [fs/file?]})
                                                          (map (juxt str #(str (fs/relativize base %)))))))
                                                 include))
                                       bundle)]
                    (assoc package :files (concat efiles bfiles))))
                packages)))

(defn collect
  "cellects all information given lookups and project"
  {:added "3.0"}
  ([]
   (let [project (project/project)
         lookups (create-file-lookups project)]
     (collect (read-packages) lookups project)))
  ([packages lookups project]
   (collect packages lookups project nil))
  ([packages lookups project {:keys [overlapped missing]
                              :or {overlapped :error
                                   missing :none}}]
   (let [packages (collect-entries packages lookups)
         _        (let [entries (missing-entries packages lookups)]
                    (case missing
                      :warn  (if (seq entries)
                               (print/println "Missing entries" entries))
                      :error (if (seq entries)
                               (throw (ex-info "Missing entries" {:entries entries})))
                      true))
         _        (let [entries (overlapped-entries packages)]
                    (case overlapped
                      :warn  (if (seq entries)
                               (print/println "Overlapped entries" entries))
                      :error (if (seq entries)
                               (throw (ex-info "Overlapped entries" {:entries entries})))
                      true))
         packages (-> packages
                      (collect-linkages lookups)
                      (collect-internal-deps)
                      (collect-external-deps)
                      (collect-transfers lookups project))]
     packages)))

(defn make-project
  "makes a maven compatible project
 
   (make-project)
   => map?"
  {:added "3.0"}
  ([]
   (make-project nil))
  ([_]
   (let [project (project/project)]
     (assoc project
            :deploy (or (config/resolve (:deploy project))
                        (config/load +default-config-file+))))))

(defn select-manifest
  "selects all related manifests
 
   (select-manifest {:a {:internal #{:b}}
                     :b {:internal #{:c}}
                     :c {:internal #{}}
                     :d {:internal #{}}}
                    [:a])
   => {:a {:internal #{:b}}
       :b {:internal #{:c}}
       :c {:internal #{}}}"
  {:added "3.0"}
  ([packages manifest]
   (if (= manifest :all)
     packages
     (let [find-deps (fn find-deps
                       [lookup deps entry]
                       (when-not (get @deps entry)
                         (swap! deps conj entry)
                         (doseq [e (get lookup entry)]
                           (find-deps lookup deps e))))
           deps   (atom #{})
           lookup (h/map-vals :internal packages)
           _  (doseq [entry manifest]
                (find-deps lookup deps entry))]
       (select-keys packages @deps)))))

(defn all-linkages
  "gets all linkages
 
   (all-linkages (make-project))"
  {:added "4.0"}
  ([project]
   (all-linkages (:tag project)
                  project
                  (:deploy project)))
  ([tag project {:keys [packages] :as deploy}]
   (let [tag    (or tag :public)
         {:keys [type repository collect]} (get-in deploy [:releases tag])
         packages (h/map-entries (fn [[k entry]]
                                   [k (assoc entry :name k)])
                                 packages)
         lookups  (create-file-lookups project)]
     (code.framework.link/collect packages lookups project collect))))

(defn make-linkages
  "creates linkages
 
   (make-linkages (make-project))
   => map?"
  {:added "3.0"}
  ([project]
   (make-linkages (:tag project)
                  project
                  (:deploy project)))
  ([tag project {:keys [packages] :as deploy}]
   (let [{:keys [manifest]} (get-in deploy [:releases tag])]
     (-> (all-linkages tag project deploy)
         (select-manifest manifest)))))
