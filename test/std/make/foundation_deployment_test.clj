(ns std.make.foundation-deployment-test
  (:require [code.doc :as doc]
            [code.doc.check :as doc-check]
            [code.project :as project]
            [code.tool.maven :as maven]
            [foundation-deployment.task-components :as components]
            [foundation-deployment.task-docs :as docs]
            [foundation-deployment.task-release :as release]
            [foundation-deployment.task-xtbench :as xtbench]
            [lang.seedgen.cli :as seedgen]
            [lang.seedgen.metrics :as metrics]
            [std.fs :as fs]
            [std.make :as make]
            [std.make.github :as github]
            [foundation-deployment.task-web-index :as task])
  (:use code.test))

^{:refer foundation-deployment.task-web-index/project-config :added "4.1.9"}
(fact "resolves the generated project definition on demand"
  (let [config (Object.)]
    (with-redefs [clojure.core/require (fn [& _] nil)
                  clojure.core/resolve (fn [_] (atom config))]
      (task/project-config) => config)))

^{:refer foundation-deployment.task-web-index/task-build :added "4.1.9"}
(fact "builds the generated web project"
  (let [config (Object.)
        calls (atom [])]
    (with-redefs [task/project-config (fn [] config)
                  make/build-all (fn [& args]
                                   (swap! calls conj args)
                                   :built)]
      (task/task-build) => :built
      @calls => [[config]])))

^{:refer foundation-deployment.task-web-index/task-gh-init :added "4.1.9"}
(fact "initialises and publishes the generated project"
  (let [config (Object.)
        calls (atom [])]
    (with-redefs [task/project-config (fn [] config)
                  github/gh-dwim-init (fn [& args]
                                       (swap! calls conj args)
                                       :initialised)]
      (task/task-gh-init "first release") => :initialised
      @calls => [[config "first release"]])))

^{:refer foundation-deployment.task-web-index/task-gh-push :added "4.1.9"}
(fact "pushes the generated project for the gh-pages workflow"
  (let [config (Object.)
        calls (atom [])]
    (with-redefs [task/project-config (fn [] config)
                  github/gh-dwim-push (fn [& args]
                                       (swap! calls conj args)
                                       :pushed)]
      (task/task-gh-push "release") => :pushed
      @calls => [[config "release"]])))

^{:refer foundation-deployment.task-web-index/task-run :added "4.1.9"}
(fact "dispatches publish and rejects unknown tasks"
  (with-redefs [task/task-gh-push (fn [& args] (vec (cons :push args)))]
    (task/task-run "publish" "release") => [:push "release"]
    (task/task-run "push" "release") => [:push "release"]
    (try
      (task/task-run "unknown")
      false
      (catch clojure.lang.ExceptionInfo _
        true)) => true))

^{:refer foundation-deployment.task-web-index/-main :added "4.1.9"}
(fact "terminates successfully and reports task failures"
  (let [calls (atom [])]
    (with-redefs [task/task-run (fn [& args]
                                  (swap! calls conj [:run args])
                                  :ran)
                  task/*exit* (fn [status]
                                (swap! calls conj [:exit status])
                                status)]
      (task/-main "publish" "release") => 0
      @calls => [[:run ["publish" "release"]] [:exit 0]]))
  (let [calls (atom [])]
    (with-redefs [task/task-run (fn [& _]
                                  (throw (ex-info "failed" {})))
                  task/*exit* (fn [status]
                                (swap! calls conj status)
                                status)]
      (task/-main "publish") => 1
      @calls => [1])))

^{:refer foundation-deployment.task-release/project-config :added "4.1.9"}
(fact "resolves the current release project"
  (let [config {:name 'xyz.zcaudate/foundation-base
                :version "4.1.8"
                :root "."}]
    (with-redefs [project/project (fn [] config)]
      (release/project-config) => config)))

^{:refer foundation-deployment.task-release/preflight :added "4.1.9"}
(fact "validates the release coordinate, version, and CI context"
  (let [config {:name 'xyz.zcaudate/foundation-base
                :version "4.1.8"
                :root "."}
        env {"GITHUB_REF_NAME" "v4.1.8"
             "GITHUB_REPOSITORY" "zcaudate-xyz/foundation-base"
             "HEAD_REPO" "zcaudate-xyz/foundation-base"
             "HEAD_REF" "main"
             "BASE_REF" "release"}]
    (get-in (with-redefs [release/project-config (fn [] config)
                          release/*env* env]
              (release/preflight)) [:release :version]) => "4.1.8"
    (try
      (with-redefs [release/project-config (fn [] (assoc config :version "4.1.8-SNAPSHOT"))
                    release/*env* (fn [_] nil)]
        (release/preflight))
      false
      (catch clojure.lang.ExceptionInfo _ true)) => true))

^{:refer foundation-deployment.task-release/package :added "4.1.9"}
(fact "packages the root and split release artifacts in order"
  (let [config {:name 'xyz.zcaudate/foundation-base
                :version "4.1.8"
                :root "."}
        calls (atom [])]
    (get-in (with-redefs [release/project-config (fn [] config)
                          release/*env* (fn [_] nil)
                          release/*lein-run* (fn [_ args]
                                               (swap! calls conj args)
                                               {:exit 0})
                          maven/package (fn [_ _] {:errors 0 :packages 2})]
              (release/package {})) [:packages :packages]) => 2
    @calls => [["pom"] ["jar"]]))

^{:refer foundation-deployment.task-release/publish-split :added "4.1.9"}
(fact "publishes split packages"
  (let [config {:name 'xyz.zcaudate/foundation-base
                :version "4.1.8"
                :root "."}]
    (get-in (with-redefs [release/project-config (fn [] config)
                          release/*env* (fn [_] nil)
                          maven/deploy (fn [_ _] {:errors 0 :deployed true})]
              (release/publish-split {})) [:result :deployed]) => true))

^{:refer foundation-deployment.task-release/publish-root :added "4.1.9"}
(fact "publishes the root artifact through Leiningen"
  (let [config {:name 'xyz.zcaudate/foundation-base
                :version "4.1.8"
                :root "."}]
    (get-in (with-redefs [release/project-config (fn [] config)
                          release/*env* (fn [_] nil)
                          release/*lein-run* (fn [_ args] {:exit 0 :args args})]
                (release/publish-root {})) [:result :args]) => ["deploy" "clojars"]))

^{:refer foundation-deployment.task-release/publish :added "4.1.9"}
(fact "runs the release publication stages after credential validation"
  (let [config {:name 'xyz.zcaudate/foundation-base
                :version "4.1.8"
                :root "."}]
    (set (keys (with-redefs [release/project-config (fn [] config)
                             release/*env* (fn [key]
                                             (get {"CLOJARS_USERNAME" "u"
                                                   "CLOJARS_PASSWORD" "p"} key))
                             release/package (fn [_] :package)
                             release/publish-split (fn [_] :split)
                             release/publish-root (fn [_] :root)]
                 (release/publish {})))) => #{:project :package :publish-split :publish-root}))

^{:refer foundation-deployment.task-release/task-run :added "4.1.9"}
(fact "dispatches release tasks"
  (with-redefs [release/preflight (fn [& _] :preflight)]
    (release/task-run "preflight") => :preflight
    (try
      (release/task-run "missing")
      false
      (catch clojure.lang.ExceptionInfo _ true)) => true))

^{:refer foundation-deployment.task-release/-main :added "4.1.9"}
(fact "exits successfully and reports release failures"
  (with-redefs [release/task-run (fn [& _] :ok)
                release/*exit* (fn [status] status)]
    (release/-main "preflight") => 0)
  (with-redefs [release/task-run (fn [& _] (throw (ex-info "failed" {})))
                release/*exit* (fn [status] status)]
    (release/-main "preflight") => 1))

^{:refer foundation-deployment.task-docs/codox-build :added "4.1.9"}
(fact "runs or skips Codox generation"
  (docs/codox-build {:enabled? false}) => {:status :skipped}
  (with-redefs [docs/*codox-run* (fn [] :generated)]
    (docs/codox-build {}) => {:status :built :result :generated}))

^{:refer foundation-deployment.task-docs/build :added "4.1.9"}
(fact "builds documentation sites and Codox"
  (let [calls (atom [])]
    (:sites (with-redefs [doc/deploy-template (fn [site opts]
                                                (swap! calls conj [:template site opts])
                                                :template)
                          doc/publish (fn [args opts]
                                        (swap! calls conj [:publish args opts])
                                        :publish)
                          docs/*codox-run* (fn [] :codox)]
              (docs/build {:sites [:core]}))) => [:core]
    @calls => [[:template :core {:write true}]
               [:publish ['core] {:write true :parallel false}]]))

^{:refer foundation-deployment.task-docs/check :added "4.1.9"}
(fact "returns documentation check failures and status"
  (with-redefs [doc-check/check-failures (fn [_ _] 0)]
    (docs/check :all {}) => {:input :all :failures 0 :ok? true})
  (with-redefs [doc-check/check-failures (fn [_ _] 2)]
    (docs/check :all {}) => {:input :all :failures 2 :ok? false}))

^{:refer foundation-deployment.task-docs/task-run :added "4.1.9"}
(fact "dispatches documentation tasks"
  (with-redefs [docs/check (fn [& _] :checked)]
    (docs/task-run "check") => :checked))

^{:refer foundation-deployment.task-docs/-main :added "4.1.9"}
(fact "exits according to documentation check status"
  (with-redefs [docs/task-run (fn [& _] {:ok? true})
                docs/*exit* (fn [status] status)]
    (docs/-main "check") => 0)
  (with-redefs [docs/task-run (fn [& _] {:ok? false})
                docs/*exit* (fn [status] status)]
    (docs/-main "check") => 1))

^{:refer foundation-deployment.task-components/target-config :added "4.1.9"}
(fact "resolves generated component targets"
  (let [config (Object.)]
    (with-redefs [clojure.core/require (fn [& _] nil)
                  clojure.core/ns-resolve (fn [_ _] (atom config))]
      (components/target-config "web-index") => config)))

^{:refer foundation-deployment.task-components/build :added "4.1.9"}
(fact "builds a generated component target"
  (let [config (Object.)]
    (with-redefs [components/target-config (fn [_] config)
                  make/build-all (fn [value] (assert (= config value)) :built)]
      (components/build "web-index") => :built)))

^{:refer foundation-deployment.task-components/init :added "4.1.9"}
(fact "initialises a generated component target"
  (let [config (Object.)]
    (with-redefs [components/target-config (fn [_] config)
                  github/gh-dwim-init (fn [_ message] message)]
      (components/init "web-index" "message") => "message")))

^{:refer foundation-deployment.task-components/publish :added "4.1.9"}
(fact "publishes a generated component target"
  (let [config (Object.)]
    (with-redefs [components/target-config (fn [_] config)
                  github/gh-dwim-push (fn [_ message] message)]
      (components/publish "web-index" "message") => "message")))

^{:refer foundation-deployment.task-components/task-run :added "4.1.9"}
(fact "dispatches generated component tasks"
  (with-redefs [components/build (fn [_] :built)]
    (components/task-run "build" "web-index") => :built))

^{:refer foundation-deployment.task-components/-main :added "4.1.9"}
(fact "exits after generated component tasks"
  (with-redefs [components/task-run (fn [& _] :ok)
                components/*exit* (fn [status] status)]
    (components/-main "build" "web-index") => 0))

^{:refer foundation-deployment.task-xtbench/suite-config :added "4.1.9"}
(fact "resolves the XTBench suite matrix"
  (count (:jobs (xtbench/suite-config "core-kmi"))) => 1)

^{:refer foundation-deployment.task-xtbench/test :added "4.1.9"}
(fact "runs XTBench jobs and writes metric artifacts"
  (let [result {:generated {:dart []}
                :summary {:passed 1 :failed 0 :throw 0 :timeout 0 :skipped 0}
                :failures []
                :errored []
                :exit 0}]
    (:exit (with-redefs [project/project (fn [] {:root "."})
                          seedgen/seedgen-test (fn [_ _ _] result)
                          metrics/write-test-record! (fn [& _] :written)]
              (xtbench/test "core-kmi" {:metrics-dir "target/test-metrics"}))) => 0))

^{:refer foundation-deployment.task-xtbench/merge :added "4.1.9"}
(fact "merges XTBench metric artifacts"
  (with-redefs [metrics/merge-directory! (fn [input output workflow]
                                           [input output workflow])]
    (xtbench/merge "core" {:input-dir "in" :output-dir "out"}) => ["in" "out" "core"]))

^{:refer foundation-deployment.task-xtbench/publish :added "4.1.9"}
(fact "publishes an XTBench metrics branch"
  (let [calls (atom [])
        fake-git (fn [args opts]
                   (swap! calls conj [args opts])
                   (cond
                     (= "show-ref" (first args)) {:exit 0 :out ""}
                     (= "diff" (first args)) {:exit 1 :out "changed"}
                     (= "push" (first args)) {:exit 0 :out "pushed"}
                     :else {:exit 0 :out ""}))]
    (:status (with-redefs [project/project (fn [] {:root "."})
                           xtbench/*env* (fn [_] nil)
                           xtbench/*git* fake-git
                           xtbench/merge (fn [_ _] {:merged true})
                           fs/exists? (fn [_] false)
                           fs/parent (fn [_] ".")
                           fs/create-directory (fn [_] nil)]
               (xtbench/publish "core" {:artifacts-dir "in"
                                         :output-dir "out"}))) => :published
    (some #(= "push" (first (first %))) @calls) => true))

^{:refer foundation-deployment.task-xtbench/task-run :added "4.1.9"}
(fact "dispatches XTBench tasks"
  (with-redefs [xtbench/publish (fn [& _] {:status :published})]
    (:status (xtbench/task-run "publish" "core")) => :published))

^{:refer foundation-deployment.task-xtbench/-main :added "4.1.9"}
(fact "exits according to XTBench task status"
  (with-redefs [xtbench/task-run (fn [& _] {:exit 0})
                xtbench/*exit* (fn [status] status)]
    (xtbench/-main "test" "core") => 0)
  (with-redefs [xtbench/task-run (fn [& _] {:exit 1})
                xtbench/*exit* (fn [status] status)]
    (xtbench/-main "test" "core") => 1))
