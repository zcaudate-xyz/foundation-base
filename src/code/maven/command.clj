(ns code.maven.command
  (:require [std.lib.encode :as encode]
            [code.maven.package :as package]
            [std.fs.archive :as archive]
            [std.fs :as fs]
            [std.lib.security :as security]
            [std.lib :as h]
            [lib.aether :as aether]
            [lib.openpgp :as openpgp]))

(defn sign-file
  "signs a file with gpg
 
   (sign-file {:file \"test-data/code.maven/maven.edn\" :extension \"edn\"}
              -key-)
   => {:file \"test-data/code.maven/maven.edn.asc\"
       :extension \"edn.asc\"}"
  {:added "1.2"}
  ([{:keys [file extension]}
    {:keys [suffix public private] :as params
     :or {suffix "asc"}}]
   (let [output (str file "." suffix)
         output-ex (str extension "." suffix)]
     (openpgp/sign file output params)
     {:file output
      :extension output-ex})))

(defn create-digest
  "creates a digest given a file and a digest type
 
   (create-digest \"MD5\"
                  \"md5\"
                  {:file \"test-data/code.maven/maven.edn\"
                   :extension \"edn\"})
   => {:file \"test-data/code.maven/maven.edn.md5\", :extension \"edn.md5\"}"
  {:added "1.2"}
  ([algorithm suffix {:keys [file extension] :as artifact}]
   (let [content (-> (fs/read-all-bytes file)
                     (security/digest "MD5")
                     (encode/to-hex))
         file (str file "." suffix)
         extension (str extension "." suffix)
         _ (spit file content)]
     {:file file :extension extension})))

(defn add-digest
  "adds MD5 and SHA1 digests to all artifacts
 
   (add-digest [{:file \"test-data/code.maven/maven.edn\"
                 :extension \"edn\"}])
   => [{:file \"test-data/code.maven/maven.edn.md5\",
        :extension \"edn.md5\"}
       {:file \"test-data/code.maven/maven.edn\",
        :extension \"edn\"}]"
  {:added "1.2"}
  ([artifacts]
   (concat (mapv (partial create-digest "MD5" "md5") artifacts)
           artifacts)))

(defn clean
  "cleans the interim directory
 
   (def -args- ['xyz.zcaudate/std.object nil -collected- (project/project)])
 
   (apply package/package -args-)
 
   (apply clean -args-)"
  {:added "3.0"}
  ([name {:keys [simulate interim] :as params} linkages project]
   (let [interim (or interim package/+interim+)
         output (fs/path (:root project) interim (str name))]
     (fs/delete output {:simulate simulate}))))

(defn prepare-artifacts
  "prepares the files for deployment"
  {:added "3.0"}
  ([{:keys [jar pom interim] :as rep}
    {:keys [secure digest] :as params}
    {:keys [deploy] :as project}]
   (let [artifacts [{:file (fs/path interim jar)
                     :extension "jar"}
                    {:file (fs/path interim pom)
                     :extension "pom"}]
         artifacts (cond-> artifacts
                     secure (->> (map #(sign-file % (get-in deploy [:signing :key])))
                                 (concat artifacts))
                     digest (add-digest))]
     artifacts)))

(defn install
  "installs a package to the local `.m2` repository
 
   (install 'xyz.zcaudate/std.object
            {}
            -collected-
            (assoc (project/project) :version \"3.0.1\" :aether (aether/aether)))
   => (contains [artifact/rep? artifact/rep?])"
  {:added "3.0"}
  ([name {:keys [skip bulk] :as params} linkages {:keys [aether] :as project}]
   (let [process (if skip package/infer package/package)
         {:keys [jar pom interim] :as rep} (process name params linkages project)
         artifacts (prepare-artifacts rep params project)]
     (aether/install-artifact aether
                              rep
                              {:artifacts artifacts
                               :print {:title  (not bulk)
                                       :timing (not bulk)}}))))

(defn deploy
  "deploys a package to a test repo
 
   (deploy 'xyz.zcaudate/std.lib
           {:tag :dev}   ;;(read-config)
          -collected-
           (assoc (project/project)
                  :deploy (config/load \"config/deploy.edn\")
                  :aether (aether/aether)))"
  {:added "3.0"}
  ([name {:keys [tag skip bulk digest] :as params} linkages {:keys [aether deploy] :as project}]
   (let [tag     (or tag :public)
         release (get-in deploy [:releases tag])
         process (if skip package/infer package/package)
         {:keys [jar pom interim] :as rep} (process name params linkages project)
         repository (get-in deploy [:repositories (:repository release)])
         artifacts  (prepare-artifacts rep params project)]
     (aether/deploy-artifact aether
                             rep
                             {:artifacts artifacts
                              :repository repository
                              :print {:title  (not bulk)
                                      :timing (not bulk)}}))))

(comment
  
  (->> (slurp "config/repositories.edn")
       (std.config.secure/encrypt-text)
       (spit "config/repositories.edn.secured"))

  (->> (slurp "config/keys/z@caudate.me")
       (std.config.secure/encrypt-text)
       (spit "config/keys/z@caudate.me.secured"))

  (:all (:releases (std.config/load "config/deploy.edn")))
  
  (code.maven/deploy ['foundation] {:tag :all})
  (code.maven/deploy ['hara/io.file] {:tag :dev})
  (code.maven/deploy ['hara] {:tag :jep})
  (code.maven/install-secure ['hara/base] {:tag :dev}))
