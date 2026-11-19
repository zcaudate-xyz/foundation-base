(ns dupasupa.build-test
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [app.manage :as app]
            [app.manage.server :as server]
            [app.manage.postgres :as postgres]
            [postgres.typed :as typed]
            [dupasupa.config :as config]
            [dupasupa.build :as build])
  (:use code.test))

(defn project
  []
  (assoc build/PROJECT :root (if (.isDirectory (io/file "src/dupasupa")) "." "examples/dupasupa")))

^{:refer dupasupa.build/PROJECT :id publication :added "4.1" :timeout 60000}
(fact "Dupasupa publishes database DSL, substrate libraries, worker and pages through one graph"
  (let [project (project)
        {:keys [artifacts]} (app/render project)
        main (json/read-str (get artifacts "generated/web-main/worker/site-map/manifest.json"))
        public (json/read-str (get artifacts "generated/web-public/worker/site-map/manifest.json"))]
    [(app/plan project) (get main "tables") (get main "rpcs") (get public "tables")
     (get public "rpcs")
     (every? #(contains? artifacts %)
             ["generated/database/schema.sql" "generated/database/schema.edn"
              "generated/lib/model.mjs" "generated/web-main/worker.mjs" "generated/web-main/app.mjs"
              "generated/web-main/index.html" "generated/web-public/index.html"
              "generated/next/app/campaigns.js" "generated/next/app/api/campaigns/route.js"])
     (str/includes? (get artifacts "generated/database/schema.sql") "Campaign title is required")])
  =>
  [[:database :contracts-main :library :workers :web-main :next :contracts-public :web-public
    :page-verifier :integration :application-manifest] ["User" "Campaign"]
   ["create-owned-campaign" "list-campaigns" "list-owned-campaigns" "ret-profile" "save-campaign"
    "save-owned-campaign" "save-profile"] [] ["ping"] true true])

^{:refer dupasupa.build/PROJECT :id impact :added "4.1"}
(fact "database edits invalidate both sites while page edits leave the library alone"
  [(set (app/affected (project) ["src/dupasupa/db/tables.clj"]))
   (app/affected (project) ["src/dupasupa/pages/main/campaigns.clj"])]
  =>
  [#{:database :contracts-main :contracts-public :library :workers :web-main :next :web-public
     :page-verifier :integration :application-manifest} [:contracts-main :web-main :next :integration :page-verifier]])

^{:refer dupasupa.build/PROJECT :id database-isolation :added "4.1" :timeout 60000}
(fact "loading another Foundation database application does not contaminate Dupasupa publications"
  (let [before (typed/export-edn-string (postgres/context config/DATABASE))]
    (require 'postgres.sample.scratch-v0)
    (let [after (postgres/context config/DATABASE)]
      [(set (map :name (vals (get-in after [:typed :tables]))))
       (= before (typed/export-edn-string after))]))
  =>
  [#{"User" "Campaign"} true])

^{:refer dupasupa.build/PROJECT :id workbench :added "4.1" :timeout 60000}
(fact
  "manager previews mount the copied runtime devtools with the application's existing auth client"
  (let [project (project)
        {:keys [artifacts]} (app/render project)
        auth (get artifacts "generated/next/app/auth.js")
        panel (get artifacts "generated/next/lib/devtools/devtools.mjs")
        previews (server/preview-index project
                                       [{:id "next"}
                                        {:id "web-public" :outputs ["generated/web-public"]}])]
    [(mapv :url (take 2 previews))
     (every? #(str/includes? auth %)
             ["FrameDevtoolProvider" "FrameDevtoolPanel" "\"client\":client" "inspection.queries"])
     (every? #(str/includes? panel %)
             ["Test accounts" "Inspect runtime" "Dev Queries" "SharedWorker"])
     (contains? artifacts "generated/next/lib/devtools/devtools/runtime.mjs")])
  =>
  [["http://127.0.0.1:8000/web-main" "http://127.0.0.1:8000/profile"] true true true])

^{:refer dupasupa.build/PROJECT :id declaration-impact :added "4.1"}
(fact "application declaration edits invalidate production bindings and workbench artifacts together"
  (set (app/affected (project) ["src/dupasupa/pages.clj"]))
  => #{:library :workers :contracts-main :web-main :next :integration :page-verifier :application-manifest})
