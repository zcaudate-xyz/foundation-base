(ns code.tool.maven.package
  (:require [std.fs :as fs]
            [std.html :as html]
            [std.string :as str]
            [jvm.artifact :as artifact]
            [code.framework.link :as linkage]
            [std.fs.archive :as archive]
            [std.fs :as fs]
            [code.project :as project]))

(def +interim+ "target/interim")

(def +header+
  {"xmlns"
   "http://maven.apache.org/POM/4.0.0"

   "xmlns:xsi"
   "http://www.w3.org/2001/XMLSchema-instance"

   "xsi:schemaLocation"
   (str "http://maven.apache.org/POM/4.0.0"
        " "
        "http://maven.apache.org/xsd/maven-4.0.0.xsd")})

(defn pom-properties
  "creates a pom.properties file
 
   (pom-properties (project/project))
   ;; \"# foundation.deploy\\n# Sun Oct 14 22:57:11 CST 2018\\nversion=3.0.1\\ngroupId=foundation\\nartifactId=base\"
   => string?"
  {:added "3.0"}
  ([entry]
   (str "# hara.deploy\n"
        "# " (java.util.Date.) "\n"
        "version=" (:version entry) "\n"
        "groupId=" (:group entry) "\n"
        "artifactId=" (:artifact entry))))

(defn coordinate->tree
  "creates a coordinate tree entry
 
   (coordinate->tree '[xyz.zcaudate/std.lib \"0.1.1\"])
   => [:dependency
       [:groupId \"foundation\"]
       [:artifactId \"std.lib\"]
       [:version \"0.1.1\"]]"
  {:added "3.0"}
  ([coord]
   (let [{:keys [group artifact version exclusions scope]} (artifact/artifact :rep coord)
         scope      (if scope [:scope scope])
         exclusions (if (seq exclusions)
                      (->> exclusions
                           (map (fn [exclusion]
                                  (let [{:keys [group artifact]} (artifact/artifact :rep exclusion)]
                                    [:exclusion
                                     [:groupId group]
                                     [:artifactId artifact]])))
                           (cons :exclusions)
                           (vec)))]
     (->> (concat [:dependency
                   [:groupId group]
                   [:artifactId artifact]
                   [:version version]]
                  [scope exclusions])
          (filterv identity)))))

(defn repository->tree
  "creates a repository tree entry
 
   (repository->tree [\"clojars\" \"https://clojars.org/repo\"])
   => [:repository
       [:id \"clojars\"]
       [:url \"https://clojars.org/repo\"]
       [:snapshots [:enabled \"true\"]]
       [:releases [:enabled \"true\"]]]"
  {:added "3.0"}
  ([[id repository]]
   (let [{:keys [url snapshots releases]}
         (cond (string? repository)
               {:url repository :snapshots true :releases true}

               (map? repository)
               repository

               :else (throw (ex-info "Not supported" {:repository repository})))]
     [:repository
      [:id id]
      [:url url]
      [:snapshots [:enabled (str snapshots)]]
      [:releases [:enabled (str releases)]]])))

(defn license->tree
  "creates a license tree entry
 
   (license->tree {:name \"MIT\" :url \"http/mit.license\"})
   => [:license
       [:name \"MIT\"]
       [:url \"http/mit.license\"]]"
  {:added "3.0"}
  ([{:keys [name url]}]
   [:license
    [:name name]
    [:url url]]))

(def +default-repositories+
  {"central" "https://repo1.maven.org/maven2/"
   "clojars" "https://clojars.org/repo"})

(defn pom-xml
  "creates a pom.properties file"
  {:added "3.0"}
  ([{:keys [dependencies repositories url group artifact version name description license]}]
   (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        "\n"
        (html/html
         [:project +header+
          [:modelVersion "4.0.0"]
          [:packaging "jar"]
          [:groupId group]
          [:artifactId artifact]
          [:version version]
          [:name (str name)]
          [:description description]
          [:url url]
          [:licenses
           (license->tree license)]
          (->> (merge repositories +default-repositories+)
               (map repository->tree)
               (into [:repositories]))
          (->> (sort dependencies)
               (map coordinate->tree)
               (into [:dependencies]))]))))

(defn project-clj-content
  "creates a pom.properties file"
  {:added "3.0"}
  ([{:keys [dependencies repositories url group artifact version name description license]}]
   (str "(defproject " (str name) " " (pr-str version) "\n"
        "  :description "  (pr-str version) "\n"
        "  :url " (pr-str url) "\n"
        "  :license " (prn-str license) "\n"
        "  :dependencies \n"
        "  [" (->> dependencies
                   (map (fn [coord]
                          (let [{:keys [group artifact version exclusions scope]}
                                (artifact/artifact :rep coord)]
                            (str "[" group "/" artifact " " (pr-str version) "]"))))
                   (str/join "\n   ")) "]\n"
        "  :deploy-repositories [[\"clojars\"\n"
        "                         {:url  \"https://clojars.org/repo\"\n"
        "                          :sign-releases false}]])")))

(defn generate-manifest
  "creates a manifest.mf file for the project
 
   (generate-manifest \"test-scratch\" {:simulate true})
   => \"MANIFEST.MF\""
  {:added "3.0"}
  ([root]
   (generate-manifest root {:simulate false}))
  ([root {:keys [simulate]}]
   (if-not simulate
     (spit (str root "/MANIFEST.MF")
           (str "Manifest-Version: 1.0\n"
                "Built-By: hara.deploy\n"
                "Created-By: hara.deploy\n"
                "Build-Jdk: " (get (System/getProperties) "java.runtime.version")  "\n"
                "Main-Class: clojure.main")))
   "MANIFEST.MF"))

(defn generate-pom
  "generates a pom file given an entry
 
   (-> (generate-pom {:artifact \"std.task\"
                      :group \"foundation\"
                      :version \"3.0.1\"}
                    \"test-scratch\")
       (update-in [0] html/tree))"
  {:added "3.0"}
  ([entry root]
   (generate-pom entry root {:simulate false}))
  ([entry root {:keys [simulate]}]
   (let [output (str "META-INF/maven/"
                     (:group entry)
                     "/"
                     (:artifact entry))
         xml    (pom-xml entry)]
     (when-not simulate
       (fs/create-directory (fs/path root output))
       (spit (str (fs/path root output "pom.xml")) xml)
       (spit (str (fs/path root output "pom.properties")) (pom-properties entry)))
     [xml
      (str output "/pom.xml")
      (str output "/pom.properties")])))

(defn generate-project-clj
  "generates a pom file given an entry
 
   (-> (generate-pom {:artifact \"std.task\"
                      :group \"foundation\"
                      :version \"3.0.1\"}
                    \"test-scratch\")
       (update-in [0] html/tree))"
  {:added "3.0"}
  ([entry root]
   (generate-project-clj entry root {:simulate false}))
  ([entry root {:keys [simulate]}]
   (let [content    (project-clj-content entry)]
     (when-not simulate
       (spit (str (fs/path root "project.clj")) content))
     content)))

(defn linkage
  "returns the linkage for a given name
 
   (linkage 'xyz.zcaudate/std.lib nil -collected- nil)
   => (contains {:name 'xyz.zcaudate/std.lib})"
  {:added "3.0"}
  ([name _ linkages _]
   (get linkages name)))

(defn package
  "returns the packaged items for a given name
 
   (package 'xyz.zcaudate/std.image {} -collected- {:root \".\" :version \"3.0.1\"})
 
   => (contains {:package 'xyz.zcaudate/std.image,
                 :jar \"foundation-std.image-3.0.1.jar\",
                 :pom \"foundation-std.image-3.0.1.pom.xml\",
                 :interim (str (fs/file \"./target/interim/xyz.zcaudate/std.image\")),
                 :files (contains [\"MANIFEST.MF\"
                                   \"META-INF/maven/xyz.zcaudate/std.image/pom.xml\"
                                   \"META-INF/maven/xyz.zcaudate/std.image/pom.properties\"])})"
  {:added "3.0"}
  ([name {:keys [simulate interim] :as opts} linkages {:keys [root version url license] :as project}]
   (let [interim (or interim +interim+)
         interim (fs/path root interim (str name))
         {:keys [internal] :as entry}  (get linkages name)
         entry  (-> (assoc entry :version version :license license)
                    (update-in [:dependencies] concat (map #(vector % version) internal)))
         {:keys [artifact group] :as entry}  (merge entry (dissoc (artifact/rep name) :version)
                                                    {:url url})
         output  (fs/path interim "package")
         _       (if-not simulate (fs/create-directory output))
         manifest     (generate-manifest output opts)
         [xml & pom]  (generate-pom entry output opts)
         project-clj  (generate-project-clj entry interim opts)
         pom-name (str group "-" artifact "-" version ".pom.xml")
         jar-name (str group "-" artifact "-" version ".jar")
         results   (if simulate
                     (map second (:files entry))
                     (let [results (mapv (fn [[origin out]]
                                           (fs/copy-single origin
                                                           (fs/path output out)
                                                           {:options #{:replace-existing}})
                                           out)
                                         (:files entry))
                           _        (spit (fs/path interim pom-name) xml)
                           jar-path (fs/path interim jar-name)
                           _        (fs/delete jar-path)
                           jar      (archive/open jar-path)
                           _        (doto jar
                                      (archive/archive output)
                                      (.close))]
                       results))]
     {:artifact artifact
      :group group
      :version version
      :package name
      :jar  jar-name
      :pom  pom-name
      :project project-clj
      :interim (str interim)
      :files   (cons manifest (concat pom results))})))

(defn infer
  "returns the infered items for a given name
 
   (infer 'xyz.zcaudate/std.image nil -collected-  {:root \".\" :version \"3.0.1\"})
   => (contains {:package 'xyz.zcaudate/std.image,
                 :jar \"foundation-std.image-3.0.1.jar\",
                 :pom \"foundation-std.image-3.0.1.pom.xml\",
                 :interim (str (fs/file \"./target/interim/xyz.zcaudate/std.image\"))})"
  {:added "3.0"}
  ([name {:keys [interim] :as params} _ {:keys [root version]}]
   (let [interim (or interim +interim+)
         interim (fs/path root interim (str name))
         {:keys [artifact group]} (artifact/rep name)
         pom-name (str group "-" artifact "-" version ".pom.xml")
         jar-name (str group "-" artifact "-" version ".jar")]
     {:artifact artifact
      :group group
      :version version
      :package name
      :jar jar-name
      :pom pom-name
      :interim (str interim)})))
