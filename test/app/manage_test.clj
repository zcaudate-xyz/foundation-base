(ns app.manage-test
  (:require [clojure.java.io :as io]
            [app.manage :as app])
  (:use code.test))

(defn with-project [f]
  (let [root (.toFile (java.nio.file.Files/createTempDirectory "manage-app-"
                           (make-array java.nio.file.attribute.FileAttribute 0)))
        project {:id 'test :root (.getPath root) :default-targets [:page]
                 :targets
                 {:db {:sources ["src/db"] :outputs ["generated/db.edn"]
                       :render (fn [_] {"generated/db.edn" "{:version 1}\n"})}
                  :page {:depends [:db] :sources ["src/pages"] :outputs ["generated/page.edn"]
                         :render (fn [{:keys [results]}]
                                   {"generated/page.edn" (str "uses " (get-in results [:db "generated/db.edn"]))})}}}]
    (try (f project)
         (finally (doseq [file (reverse (file-seq root))] (.delete file))))))

(defn error-data [f]
  (try (f) :accepted (catch clojure.lang.ExceptionInfo e (ex-data e))))

^{:refer app.manage/plan :added "4.1"}
(fact "plans dependencies once and rejects missing targets and cycles"
  (app/plan {:targets {:a {} :b {:depends [:a]} :c {:depends [:a]}}} [:c :b])
  => [:a :c :b]
  (error-data #(app/plan {:targets {:a {:depends [:missing]}}} [:a]))
  => {:target :missing}
  (error-data #(app/plan {:targets {:a {:depends [:b]} :b {:depends [:a]}}} [:a]))
  => {:cycle [:a :b :a]})

^{:refer app.manage/affected :added "4.1"}
(fact "database changes select dependants; unrelated prefixes do not match"
  (with-project (fn [p] [(app/affected p ["src/db/schema.clj"])
                        (app/affected p ["src/pages/index.clj"])
                        (app/affected p ["src/database/schema.clj"])]))
  => [[:db :page] [:page] []])

^{:refer app.manage/generate :added "4.1"}
(fact "dry runs do not write; fresh generation is ordered and idempotent"
  (with-project
    (fn [p]
      (let [dry (app/generate p)
            absent (not (.exists (io/file (:root p) "generated")))
            first (app/generate p {:write true})
            content (slurp (io/file (:root p) "generated/page.edn"))
            second (app/generate p {:write true})]
        [(:changed dry) absent (:written first) content (:changed second) (:status (app/check p))])))
  => [["generated/db.edn" "generated/page.edn"] true true "uses {:version 1}\n" [] :ok])

^{:refer app.manage/check :added "4.1"}
(fact "checks fresh source output rather than blessed hashes, including orphaned artifacts"
  (with-project
    (fn [p]
      (app/generate p {:write true})
      (spit (io/file (:root p) "generated/page.edn") "manually edited")
      (.delete (io/file (:root p) "generated/db.edn"))
      (select-keys (app/check p) [:status :missing :changed])))
  => {:status :error :missing ["generated/db.edn"] :changed ["generated/page.edn"]}
  (with-project
    (fn [p]
      (app/generate p {:write true})
      (let [changed (assoc-in p [:targets :db :render] (fn [_] {"generated/db.edn" "v2"}))]
        (:changed (app/check changed)))))
  => ["generated/db.edn" "generated/page.edn"]
  (with-project
    (fn [p]
      (app/generate p {:write true})
      (let [removed (assoc-in p [:targets :page :render] (constantly {}))]
        [(:unexpected (app/check removed))
         (:unexpected (app/generate removed {:write true}))
         (.isFile (io/file (:root p) "generated/page.edn"))])))
  => [["generated/page.edn"] ["generated/page.edn"] true])

^{:refer app.manage/render :added "4.1"}
(fact "rejects conflicting ownership and out-of-project paths before writing"
  (with-project
    (fn [p]
      (let [bad (assoc-in p [:targets :page :render] (constantly {"../escape" "bad"}))]
        [(boolean (:path (error-data #(app/generate bad {:write true}))))
         (not (.exists (io/file (:root p) "generated")))])))
  => [true true]
  (with-project
    (fn [p]
      (let [bad (-> p
                    (assoc-in [:targets :page :outputs] ["generated/db.edn"])
                    (assoc-in [:targets :page :render] (constantly {"generated/db.edn" "bad"})))]
        (:path (error-data #(app/render bad))))))
  => "generated/db.edn")

^{:refer app.manage/project-file :added "4.1"}
(fact "rejects traversal and absolute paths, including aliases inside an owned directory"
  (with-project
    (fn [p]
      (mapv (fn [path] (boolean (:path (error-data #(app/project-file p path)))))
            ["../escape" "/tmp/escape" "generated/../other" "generated/./item" "generated//item"])))
  => [true true true true true])

^{:refer app.manage/analyse :added "4.1"}
(fact "invalid declarations yield machine-readable findings"
  (select-keys (app/analyse {:id 'broken :targets {}}) [:status :project])
  => {:status :error :project 'broken})

^{:refer app.manage/build :added "4.1"}
(fact "builds prerequisites before checks, reports rejected checks and supports generation-only builds"
  (with-project
    (fn [p]
      (let [project (assoc p :checks
                           {:read {:targets [:page]
                                   :run (fn [_]
                                          {:status (if (.isFile (io/file (:root p) "generated/page.edn"))
                                                     :ok :error)})}
                            :reject {:targets [:page] :run (fn [_] {:status :error :reason :expected})}})
            result (app/build project)
            skipped (app/build project {:tests? false})]
        [(:status result) (mapv :status (get-in result [:verification :checks]))
         (:status skipped) (get-in skipped [:verification :checks])])))
  => [:error [:ok :error] :ok []])

^{:refer app.manage/test-project :added "4.1"}
(fact "an undeclared test suite is an error rather than a vacuous pass"
  (try (app/test-project {:id 'empty :targets {}}) :accepted
       (catch clojure.lang.ExceptionInfo e (:project (ex-data e))))
  => 'empty)
