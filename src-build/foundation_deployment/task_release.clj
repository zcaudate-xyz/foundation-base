(ns foundation-deployment.task-release
  (:require [clojure.string :as string]
            [code.project :as project]
            [code.tool.maven :as maven]
            [std.lib.os :as os]))

(def ^:dynamic *exit*
  (fn [status]
    (System/exit status)))

(def ^:dynamic *env*
  (fn [key]
    (System/getenv key)))

(def ^:dynamic *lein-run*
  (fn [root args]
    (let [lein (if (.exists (java.io.File. (str root "/lein"))) "./lein" "lein")
          process (apply os/sh lein (concat args [{:root root
                                                   :inherit true
                                                   :wait false
                                                   :output false}]))
          result (os/sh-output process)]
      (when-not (zero? (:exit result))
        (throw (ex-info "Leiningen task failed"
                        {:args args
                         :root (str root)
                         :result result})))
      result)))

^{:public true}
(defn project-config
  "Returns the current Leiningen project configuration."
  []
  (project/project))

^{:public true}
(defn preflight
  "Validates the release coordinate, version, and CI release context."
  ([] (preflight {}))
  ([opts]
   (let [config      (project-config)
         expected    (or (:coordinate opts) 'xyz.zcaudate/foundation-base)
         coordinate  (:name config)
         version     (str (:version config))
         tag         (or (*env* "GITHUB_REF_NAME")
                         (*env* "GITHUB_REF"))
         tag         (some-> tag (string/split #"/") last)
         tag-version (some-> tag (string/replace-first #"^v" ""))
         head-repo   (*env* "HEAD_REPO")
         repository  (*env* "GITHUB_REPOSITORY")
         head-ref    (*env* "HEAD_REF")
         base-ref    (*env* "BASE_REF")
         errors      (cond-> []
                       (not= expected coordinate)
                       (conj {:type :coordinate
                              :expected expected
                              :actual coordinate})

                       (not (re-matches #"[0-9]+\.[0-9]+\.[0-9]+(?:[-+][0-9A-Za-z.-]+)?" version))
                       (conj {:type :version
                              :version version})

                       (re-find #"SNAPSHOT" version)
                       (conj {:type :snapshot
                              :version version})

                       (and tag
                            (not= tag "release")
                            (not= tag-version version))
                       (conj {:type :tag
                              :tag tag
                              :version version})

                       (and head-repo repository (not= head-repo repository))
                       (conj {:type :head-repository
                              :expected repository
                              :actual head-repo})

                       (and head-ref (not= head-ref "main"))
                       (conj {:type :head-ref
                              :expected "main"
                              :actual head-ref})

                       (and base-ref (not= base-ref "release"))
                       (conj {:type :base-ref
                              :expected "release"
                              :actual base-ref}))]
     (when (seq errors)
       (throw (ex-info "Release preflight failed"
                       {:errors errors
                        :project config})))
     (assoc config
            :release {:coordinate coordinate
                      :version version
                      :tag tag
                      :head-repo head-repo
                      :head-ref head-ref
                      :base-ref base-ref}))))

^{:public true}
(defn package
  "Builds the POM, root JAR, and split Clojars artifacts."
  ([] (package {}))
  ([opts]
   (let [config   (preflight opts)
         root     (:root config)
         pom      (*lein-run* root ["pom"])
         jar      (*lein-run* root ["jar"])
         packages (maven/package :all (clojure.core/merge {:tag :clojars} opts))]
     (when (and (number? (:errors packages))
                (pos? (:errors packages)))
       (throw (ex-info "Split package task failed"
                       {:result packages})))
     {:project config
      :pom pom
      :jar jar
      :packages packages})))

^{:public true}
(defn publish-split
  "Publishes all split artifacts to Clojars."
  ([] (publish-split {}))
  ([opts]
   (let [config (preflight opts)
         result (maven/deploy :all (clojure.core/merge {:tag :clojars} opts))]
     (when (and (number? (:errors result))
                (pos? (:errors result)))
       (throw (ex-info "Split artifact publication failed"
                       {:result result})))
     {:project config
      :result result})))

^{:public true}
(defn publish-root
  "Publishes the root artifact through Leiningen after split artifacts."
  ([] (publish-root {}))
  ([opts]
   (let [config (preflight opts)
         result (*lein-run* (:root config) ["deploy" "clojars"])]
     {:project config
      :result result})))

^{:public true}
(defn publish
  "Runs preflight, package, split publication, and root publication in order."
  ([] (publish {}))
  ([opts]
   (let [config   (preflight opts)
         username (*env* "CLOJARS_USERNAME")
         password (*env* "CLOJARS_PASSWORD")]
     (when-not (and (seq username) (seq password))
       (throw (ex-info "Clojars credentials are required"
                       {:missing (cond-> []
                                   (not (seq username)) (conj "CLOJARS_USERNAME")
                                   (not (seq password)) (conj "CLOJARS_PASSWORD"))})))
     {:project config
      :package (package opts)
      :publish-split (publish-split opts)
      :publish-root (publish-root opts)})))

^{:public true}
(defn task-run
  "Dispatches a release deployment task."
  [task & args]
  (case task
    "preflight" (apply preflight args)
    "package" (apply package args)
    "publish-split" (apply publish-split args)
    "publish-root" (apply publish-root args)
    "publish" (apply publish args)
    (throw (ex-info "Unknown release task"
                    {:task task
                     :tasks ["preflight" "package" "publish-split" "publish-root" "publish"]}))))

^{:public true}
(defn -main
  "Runs a release task and exits with a shell status."
  [& [task & args]]
  (try
    (apply task-run task args)
    (*exit* 0)
    (catch Throwable _
      (*exit* 1))))
