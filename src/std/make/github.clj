(ns std.make.github
  (:require [std.lib.os :as os]
            [std.lib.impl :refer [defimpl]]
            [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.string :as str]
            [std.make.common :as common]
            [std.make.compile :as compile]
            [std.make.project :as project]
            [std.fs :as fs]))

(def ^:dynamic *verbose* true)

(defmacro with-verbose
  "with verbose github messages"
  {:added "4.0"}
  [verbose & body]
  `(binding [*verbose* ~verbose] ~@body))

;;
;; Publish to Github
;;

(defn gh-sh-opts
  "creates standard shell opts"
  {:added "4.0"}
  [{:keys [instance] :as mcfg}]
  (let [{:keys [root build] :as mcfg} @instance
        dir (str root "/" build)
        opts {:root  dir
              :print *verbose*
              :ignore-errors (not *verbose*)
              :inherit true
              :wait true}]
    opts))

(defn gh-commit
  "creates a commit in the repo"
  {:added "4.0"}
  ([mcfg message]
   (let [opts (gh-sh-opts mcfg)]
     (os/sh "git" "add" "-A" opts)
     (os/sh "git" "commit" "-m" message opts))))

(defn gh-push
  "pushes to github"
  {:added "4.0"}
  ([mcfg]
   (let [opts (gh-sh-opts mcfg)]
     (os/sh "git" "push" opts))))

(defn gh-save
  "does a commit and push to github"
  {:added "4.0"}
  ([mcfg message]
   (gh-commit mcfg message)
   (gh-push mcfg)))

(defn gh-user
  "gets the github user"
  {:added "4.0"}
  ([]
   (System/getenv "GITHUB_USER")))

(defn gh-token
  "gets the github token"
  {:added "4.0"}
  ([]
   (System/getenv "GITHUB_TOKEN")))

(defn gh-exists?
  "checks that github repo exists"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg}]
   (let [{:keys [github]} @instance
         {:keys [repo]} github
         user  (gh-user)
         token (gh-token)]
     (-> @(os/sh "curl" "-u" (str user ":" token)
                 (str "https://api.github.com/repos/" repo)
                 {:print *verbose*})
         (str/starts-with? "{\n  \"message\": \"Not Found\"")
         not))))

(defn gh-setup-remote
  "creates the github remote repo"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg}]
   (let [{:keys [github]} @instance
         {:keys [repo description private]} github
         user  (gh-user)
         token (gh-token)
         [repo-ns repo-name] (str/split repo #"/")
         is-org (not= user repo-ns)
         url  (if is-org
                (str "https://api.github.com/orgs/" repo-ns "/repos")
                (str "https://api.github.com/user/repos"))]
     (os/sh "curl" "-i" "-u" (str user ":" token) url
            "-d" (format "{\"name\":\"%s\", \"private\": %s, \"description\": %s}"
                         repo-name
                         (if private "true" "false")
                         (pr-str (or description "")))
            {:print *verbose* :inherit *verbose* :wait true}))))

(defn gh-setup-local-init
  "initiates local repo to remote"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg}]
   (let [{:keys [root build github]} @instance
         out-dir (str root "/" build)
         {:keys [repo]} github
         opts {:root out-dir  :print *verbose* :inherit *verbose*
               :ignore-errors (not *verbose*) :wait true}]
     (os/sh "git" "init" "." opts)
     (os/sh "git" "remote" "add" "origin"
            (format "git@github.com:%s.git" repo)
            opts)
     (os/sh "git" "add" "-A" opts)
     (os/sh "git" "commit" "-m" "Initial commit" opts)
     (os/sh "git" "push" "-u" "origin" "master" opts))))

(defn gh-setup-local-clone
  "clones `.build` directory from remote"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg}]
   (let [{:keys [root build github]} @instance
         {:keys [repo]} github]
     (os/sh "git" "clone" (format "git@github.com:%s.git" repo)
            build
            {:root root :print *verbose* :inherit *verbose*
             :ignore-errors (not *verbose*)
             :wait true}))))

(defn gh-clone
  "forces a git clone"
  {:added "4.0"}
  ([mcfg]
   (if (common/make-dir-exists? mcfg)
     (h/error "Directory exists." {:dir (common/make-dir mcfg)})
     (gh-setup-local-clone mcfg))))

(defn gh-setup
  "sets up repo project"
  {:added "4.0"}
  ([mcfg]
   (env/p "\nGH SETUP STARTING\n")
   (if (gh-exists? mcfg)
     (do
       (common/make-dir-teardown mcfg)
       (gh-setup-local-clone mcfg))
     (do
       (gh-setup-remote mcfg)
       (gh-setup-local-init mcfg)))
   (env/p "\nGH SETUP DONE\n")))

(defn gh-local-purge
  "purges all checked in files from git repo"
  {:added "4.0"}
  ([mcfg]
   (let [out-dir (common/make-dir mcfg)]
     (os/sh "bash" "-c" "git ls-files -z | xargs -0 rm -f" {:root out-dir}))))

(defn gh-refresh
  "regenerates the git repo"
  {:added "4.0"}
  ([mcfg]
   (h/error "TODO")
   #_(gh-purge mcfg)))



(defn gh-dwim-init
  "gh dwim init"
  {:added "4.0"}
  [mcfg & [message]]
  (do (gh-local-purge mcfg)
      (gh-setup-remote mcfg)
      (gh-setup mcfg)
      (gh-local-purge mcfg)
      (project/build-all mcfg)
      (gh-save mcfg (or message
                           (str "SRC:"
                                (str/trim (str (os/sh "git" "rev-parse" "HEAD"))))))
      (gh-push mcfg)))

(defn gh-dwim-push
  "gh dwim push"
  {:added "4.0"}
  [mcfg & [message]]
  (do (project/build-all mcfg)
      (gh-commit mcfg
                 (or message
                     (str "SRC:"
                          (str/trim (str (os/sh "git" "rev-parse" "HEAD"))))))
      (gh-push mcfg)))
