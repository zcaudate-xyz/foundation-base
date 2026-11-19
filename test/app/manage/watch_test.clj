(ns app.manage.watch-test
  (:require [app.manage :as app] [app.manage.watch :as watch]
            [clojure.java.io :as io] [clojure.string :as str])
  (:use code.test))

^{:refer app.manage.watch/reload-plan :added "4.1"}
(fact "reload follows project-local Clojure dependants and retains deleted namespace identity"
  (let [before {"src/a.clj" {:namespace 'demo.a :depends []}
                "src/b.clj" {:namespace 'demo.b :depends ['demo.a]}
                "src/c.clj" {:namespace 'demo.c :depends []}}]
    [(watch/reload-plan before before ["src/a.clj"])
     (watch/reload-plan before (dissoc before "src/a.clj") ["src/a.clj"])
     (watch/reload-plan before before ["asset.css"])])
  => [['demo.a 'demo.b] ['demo.a 'demo.b] []])

^{:refer app.manage.watch/sources :added "4.1"}
(fact "source discovery reads Clojure and XTalk dependency edges without evaluating forms"
  (let [root (.toFile (java.nio.file.Files/createTempDirectory "app-watch-source-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        source (io/file root "src/page.clj")]
    (try
      (io/make-parents source)
      (spit source "(ns never.evaluate (:require [demo.config :as config]))\n(l/script :postgres {:require [[demo.db :as db]]})\n(throw (Exception. \"must not execute\"))")
      (get-in (watch/sources {:root (str root) :source-paths ["src"]}) ["src/page.clj" :depends])
      (finally (doseq [file (reverse (file-seq root))] (.delete file)))))
  => ['demo.config 'demo.db])

^{:refer app.manage.watch/start! :added "4.1"}
(fact "the OS watcher rebuilds changes, recovers after a render failure and stops idempotently"
  (let [root (.toFile (java.nio.file.Files/createTempDirectory "app-watch-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        source (io/file root "src/demo.clj")
        _ (io/make-parents source)
        _ (spit source "(ns watch.fixture)\n; original")
        project {:root (str root) :source-paths ["src"] :targets
                 {:page {:sources ["src"] :outputs ["generated/page.txt"]
                         :render (fn [_]
                                   (let [text (slurp source)]
                                     (when (str/includes? text "broken")
                                       (throw (ex-info "Deliberate render failure" {})))
                                     {"generated/page.txt" text}))}}}
        failed (promise) recovered (promise)
        handle (watch/start! (constantly project)
                            {:reload? false
                             :on-result (fn [result]
                                          (if (= :error (:status result))
                                            (deliver failed true)
                                            (deliver recovered true)))})]
    (try
      (spit source "(ns watch.fixture)\n; broken")
      (let [failure (deref failed 5000 :timeout)
            retained (str/includes? (slurp (io/file root "generated/page.txt")) "original")]
        (spit source "(ns watch.fixture)\n; recovered")
        (let [recovery (deref recovered 5000 :timeout)
              final (str/includes? (slurp (io/file root "generated/page.txt")) "recovered")]
          (watch/stop! handle) (watch/stop! handle)
          [failure retained recovery final (:status @(:state handle))]))
      (finally (watch/stop! handle)
               (doseq [file (reverse (file-seq root))] (.delete file)))))
  => [true true true true :stopped])

^{:refer app.manage.watch/rebuild! :added "4.1"}
(fact "Clojure reload updates dependent namespaces and a syntax error preserves the last output"
  (let [root (.toFile (java.nio.file.Files/createTempDirectory "app-reload-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        scope (str "watchfixture" (str/replace (str (java.util.UUID/randomUUID)) "-" ""))
        a (symbol (str scope ".value")) b (symbol (str scope ".page"))
        source (io/file root "src" scope "value.clj")
        page (io/file root "src" scope "page.clj")
        loader (clojure.lang.DynamicClassLoader. (clojure.lang.RT/baseLoader))
        write-value (fn [value] (spit source (str "(ns " a ") (def value " value ")")))
        _ (io/make-parents source)
        _ (write-value 1)
        _ (spit page (str "(ns " b " (:require [" a " :as value])) (def result value/value)"))
        _ (.addURL loader (.toURL (.toURI (io/file root "src"))))
        project {:root (str root) :source-paths ["src"]
                 :targets {:page {:sources ["src"] :outputs ["generated/value.txt"]
                                  :render (fn [_] {"generated/value.txt" (str (var-get (ns-resolve b 'result)))})}}}]
    (try
      (with-bindings {clojure.lang.Compiler/LOADER loader}
        (require b)
        (app/generate project {:write true})
        (let [handle {:state (atom {:active true :project project :sources (watch/sources project)})
                      :supplier (constantly project) :lock (Object.) :options {}}
              path (str "src/" scope "/value.clj")
              output (io/file root "generated/value.txt")]
          (write-value 2)
          (let [updated (:status (watch/rebuild! handle [path]))
                value (slurp output)]
            (spit source (str "(ns " a ") (def value"))
            (let [failed (:status (watch/rebuild! handle [path])) retained (slurp output)]
              (write-value 3)
              [updated value failed retained (:status (watch/rebuild! handle [path])) (slurp output)]))))
      (finally
        (when (find-ns b) (remove-ns b)) (when (find-ns a) (remove-ns a))
        (doseq [file (reverse (file-seq root))] (.delete file)))))
  => [:ok "2" :error "2" :ok "3"])
