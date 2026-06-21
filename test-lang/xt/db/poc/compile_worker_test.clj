(ns xt.db.poc.compile-worker-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [postgres.core :as pg]
            [std.fs :as fs]
            [std.make :as make :refer [def.make]]
            [std.make.common :as common]
            [xt.lang.common-notify :as notify]
            [xt.lang.common-repl :as repl]
            [scaffold.supabase.local-min :as local-min]
            [postgres.sample.scratch-v0 :as scratch-v0]
            [xt.db.system.main]))

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (. port (postMessage {"type" "worker-started"}))
            (var schema {"Log" {"id" {"ident" "id"
                                       "type" "uuid"
                                       "primary" true
                                       "order" 0}
                                "message" {"ident" "message"
                                           "type" "text"
                                           "order" 1}}})
            (var lookup {"Log" {"position" 0}})
            (var primary-impl (xt.db.system.main/create-impl
                               "supabase"
                               (@! local-min/+config-supabase-anon+)
                               schema
                               lookup))
            (var caching-impl (xt.db.system.main/create-impl
                               "sqlite"
                               {}
                               schema
                               lookup))
            (. port (postMessage {"type" "sqlite-created"}))
            (. port (postMessage {"type" "raw-impl"}))
            (. (xt.db.system.main/create-impl-init primary-impl)
               (then
                (fn [_]
                  (. port (postMessage {"type" "primary-created"}))
                  (return (xt.db.system.main/create-impl-init caching-impl))))
               (then
                (fn [_]
                  (. port (postMessage {"type" "impl-initialized"}))
                  (return nil)))
               (catch
                (fn [err]
                  (. port (postMessage {"type" "error"
                                        "stage" "init"
                                        "message" (. err ["message"])
                                        "stack" (. err ["stack"])}))))))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(def +gitignore+
  ["node_modules"
   ".build"])

(def +package+
  {"name" "db-model-service-worker-sqlite-init"
   "private" true
   "type" "module"})

(def +makefile+
  [[:.PHONY {:- ["build" "test"]}]
   [:build
    ["@echo 'build complete'"]]
   [:test
    ["node test/smoke.js"]]])

(def +smoke-script+
  "import fs from 'fs';
import { execSync } from 'child_process';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __dirname = dirname(fileURLToPath(import.meta.url));
const workerPath = join(__dirname, '..', 'src', 'worker.js');
const workerSource = fs.readFileSync(workerPath, 'utf8');

try {
  execSync(`node --check ${workerPath}`, { stdio: 'inherit' });
} catch (e) {
  process.exit(1);
}

const markers = ['worker-started', 'sqlite-created', 'raw-impl', 'primary-created', 'impl-initialized'];
for (const marker of markers) {
  if (!workerSource.includes(marker)) {
    console.error(`Missing marker: ${marker}`);
    process.exit(1);
  }
}

console.log('smoke test passed');")

(def.make PROJECT
  {:sections {:setup [{:type :gitignore
                       :main +gitignore+}
                      {:type :makefile
                       :main +makefile+}
                      {:type :package.json
                       :main +package+}]}
   :default [{:type :custom
              :file "src/worker.js"
              :fn (fn [_] +sharedworker-script+)}
             {:type :custom
              :file "test/smoke.js"
              :fn (fn [_] +smoke-script+)}]})

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-sqlite/impl-sqlite-init
  :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "SharedWorker can import sqlite wasm and initialize the sqlite impl"

  (notify/wait-on [:js 30000]
    (var messages [])
    (var blob (new Blob [(@! +sharedworker-script+)] {"type" "text/javascript"}))
    (var url (. (!:G URL) (createObjectURL blob)))
    (var shared (new SharedWorker url {"type" "module"}))
    (var port (. shared ["port"]))
    (. port (start))
    (. port (addEventListener
              "message"
              (fn [event]
                (. messages (push (. event ["data"]))))
              false))
    (. shared (addEventListener
               "error"
               (fn [event]
                 (. messages (push {"type" "error"
                                    "message" (. event ["message"])
                                    "filename" (. event ["filename"])
                                    "lineno" (. event ["lineno"])
                                    "colno" (. event ["colno"])
                                    "error" (. event ["error"])})))
               false))
    (. (!:G URL) (revokeObjectURL url))
    (setTimeout (fn [] (repl/notify messages))
                15000)
    (return shared))
  => (contains-in
      [{"type" "worker-started"}
       {"type" "sqlite-created"}
       {"type" "raw-impl"}
       {"type" "primary-created"}
       {"type" "impl-initialized"}]))

^{:refer xt.db.poc.db-model-service-worker-sqlite-init-test/PROJECT :added "4.1"}
(fact "def.make project builds node files and passes smoke test"
  (let [root (str (fs/create-tmpdir "db-sqlite-init"))
        project (common/make-config
                 (assoc @(:instance PROJECT)
                        :root root
                        :build ".build"))
        out-dir (common/make-dir project)]
    (try
      (make/build-all project)
      (make/run-internal project :test)
      {:worker-exists (fs/exists? (str out-dir "/src/worker.js"))
       :smoke-exists (fs/exists? (str out-dir "/test/smoke.js"))}
      (finally
        (common/make-dir-teardown project))))
  => {:worker-exists true
      :smoke-exists true})
