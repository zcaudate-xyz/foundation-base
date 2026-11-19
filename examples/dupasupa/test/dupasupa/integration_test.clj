(ns dupasupa.integration-test
  (:use code.test)
  (:require [lang.core :as l]
            [clojure.java.io :as io]
            [clojure.data.json]
            [app.manage.integration :as integration]
            [app.manage.integration.postgres :as database]
            [app.manage.postgres :as publication]
            [dupasupa.config :as config]
            [dupasupa.auth-fixture :as auth]))

(def DATABASE
  (database/database {:label "dupasupa"
                      :admin-url (or (System/getenv "APP_TEST_POSTGRES_URL")
                                     "postgresql://postgres:postgres@127.0.0.1:55122/postgres")}))
(def AUTH (auth/fixture))
(def OWNER "10000000-0000-0000-0000-000000000001")
(def CAMPAIGN "10000000-0000-0000-0000-000000000002")
(defn registry
  []
  (let [registry (publication/registry config/DATABASE)]
    (into {}
          (for [[name declaration] (:get-rpc registry)]
            [name ((:rpc-spec registry) declaration)]))))

(l/script- :postgres
  {:runtime :jdbc.client
   :test-mode true
   :config (database/config DATABASE)
   :require [[postgres.core :as pg]
             [dupasupa.db.rpc :as rpc]
             [dupasupa.db.tables :as tables]]})
(l/script- :js
  {:runtime :basic
   :test-mode true
   :import [["node:worker_threads" :as threads] ["node:url" :as urls]]
   :require [[xt.lang.common-repl :as repl]
             [app.manage.integration.client :as client]
             [dupasupa.model :as model]
             [dupasupa.pages.main.campaigns :as ui]
             [xt.substrate.page-core :as page]
             [xt.event.base-model :as event-model]]})

(def SCAFFOLD
  (integration/fixture
   [{:id :auth
     :start (fn [_]
              (auth/start! AUTH)
              (alter-var-root #'OWNER (constantly (:id (first (deref (:accounts AUTH))))))
              true)
     :stop (fn [_ _] (auth/stop! AUTH))} (database/resource DATABASE)
    {:id :runtimes
     :start (fn [_]
              (l/rt:restart)
              (l/rt:setup :postgres)
              (pg/t:insert tables/User {:id OWNER :handle "owner"})
              (doseq [account (rest (deref (:accounts AUTH)))]
                (pg/t:insert tables/User
                  {:id (:id account) :handle (:handle account) :display-name "Bob Demo"}))
              (pg/t:insert tables/Campaign {:id CAMPAIGN :owner OWNER :title "Before"})
              true)
     :stop (fn [_ _] (l/rt:stop))}
    {:id :client
     :start (fn [_]
              (!.js (Object.assign
                     process.env
                     (@!
                      (let [{:keys [host port user pass]} (:admin DATABASE)]
                        {"DUPASUPA_DATABASE_URL"
                         (str "postgresql://" user ":" pass "@" host ":" port "/" (:name DATABASE))
                         "DUPASUPA_SUPABASE_URL" (get-in AUTH [:config :url])
                         "DUPASUPA_SUPABASE_KEY" (get-in AUTH [:config :anon-key])
                         "DUPASUPA_DEV_ACCOUNTS" (clojure.data.json/write-str (auth/public-accounts
                                                                               AUTH))}))))
              (integration/await-js (. (client/open (@! (database/client-config DATABASE))
                                                    (@! (registry)))
                                       (then (fn [db] (:= (!:G DB) db) (return true)))))
              true)
     :stop (fn [_ _] (integration/await-js (client/close DB)))}]))

(fact:global {:setup [(integration/start! SCAFFOLD)] :teardown [(integration/stop! SCAFFOLD)]})

^{:refer dupasupa.model/loadRows :id live-read}
(fact "the actual UI model loads rows from PostgreSQL"
  (!.js (:= (!:G HANDLE)
            (model/open {"list" (fn [] (return (client/call DB "list-campaigns" [])))
                         "call" (fn [name values]
                                  (return (client/call DB name [values.i_id values.i_title])))})))
  (!.js (:= (!:G CHANGES) [])
        (:= (!:G ROW_MODEL) (. (page/model-ensure HANDLE.node "app" model/GROUP_ID "rows") [1]))
        (event-model/add-listener
         ROW_MODEL
         "integration"
         (fn []
           (var rows (model/snapshot HANDLE))
           (. CHANGES (push (:? (and rows (> rows.length 0)) (. rows [0] ["title"]) nil))))
         nil
         nil))
  (integration/await-js (model/loadRows HANDLE))
  =>
  [{"id" CAMPAIGN "owner_id" OWNER "title" "Before"}]
  (rpc/ping) => "pong")

^{:refer dupasupa.model/save :id live-write}
(fact "saving through the page model changes the database and refreshed model output"
  (integration/await-js (model/save HANDLE (@! CAMPAIGN) "Updated <title>"))
  =>
  [{"id" CAMPAIGN "owner_id" OWNER "title" "Updated <title>"}]
  (:title (pg/t:get tables/Campaign {:where {:id CAMPAIGN}})) => "Updated <title>"
  (!.js (. (model/snapshot HANDLE) [0] ["title"])) => "Updated <title>"
  (!.js (CHANGES.includes "Updated <title>")) => true
  (!.js (. (ui/renderCampaigns (model/snapshot HANDLE)) (includes "Updated &lt;title&gt;")))
  =>
  true)

^{:refer dupasupa.model/save :id database-rejection}
(fact "database rejection propagates through the model and leaves persisted data unchanged"
  (integration/await-js
   (. (model/save HANDLE (@! CAMPAIGN) "   ")
      (then (fn [] (return {"rejected" false})))
      (catch (fn [error] (return {"rejected" true "message" error.message "code" error.code})))))
  =>
  {"rejected" true "message" "Campaign title is required" "code" "P0001"}
  (:title (pg/t:get tables/Campaign {:where {:id CAMPAIGN}})) => "Updated <title>"
  (!.js (. (model/snapshot HANDLE) [0] ["title"])) => "Updated <title>")

(defn.js ^{:- [:async]} worker-round-trip
  []
  (var url
       (. (urls.pathToFileURL (+ (@! (.getCanonicalPath (io/file ".")))
                                 "/generated/web-main/worker.mjs"))
          href))
  (var
   harness
   (+
    "import {parentPort} from 'node:worker_threads';globalThis.self={postMessage:v=>parentPort.postMessage(v)};await import("
    (JSON.stringify url)
    ");parentPort.on('message',data=>self.onmessage({data}));"))
  (var worker
       (new threads.Worker
            (new URL (+ "data:text/javascript," (encodeURIComponent harness)))
            {"type" "module"}))
  (var request
       (fn [message]
         (return (new
                  Promise
                  (fn [resolve reject]
                    (var timer
                         (setTimeout (fn [] (reject (new Error "Worker response timeout"))) 5000))
                    (. worker (once "message" (fn [value] (clearTimeout timer) (resolve value))))
                    (. worker (once "error" (fn [error] (clearTimeout timer) (reject error))))
                    (. worker (postMessage message)))))))
  (try (var loaded (await (request {"action" "load" "request" 1})))
       (var saved
            (await (request
                    {"action" "save" "id" "campaign-1" "title" "Worker saved" "request" 2})))
       (var rejected (await (request {"action" "unknown" "request" 3})))
       (return [(. loaded.result [0] ["title"]) (. saved.result [0] ["title"]) rejected.error])
       (catch error (throw error))
       (finally (await (. worker (terminate))))))

^{:refer dupasupa.worker/handleMessage :id worker-round-trip :timeout 30000}
(fact
  "the generated demo worker handles real thread messages independently of the database transport"
  (integration/await-js (-/worker-round-trip))
  =>
  ["First campaign" "Worker saved" "Unknown action"])

(defn.js ^{:- [:async]} page-save-workflow
  []
  (var moduleURL
       (. (urls.pathToFileURL (@! (.getCanonicalPath (io/file "generated/web-main/app.mjs"))))
          href))
  (var workerURL
       (. (urls.pathToFileURL (@! (.getCanonicalPath (io/file "generated/web-main/worker.mjs"))))
          href))
  (var app (await (import moduleURL)))
  (var Window (require "window"))
  (var win (new Window))
  (var root (win.document.createElement "main"))
  (var originalWorker globalThis.Worker)
  (var originalFormData globalThis.FormData)
  (var thread nil)
  (var dispose nil)
  (:= globalThis.FormData win.FormData)
  (:=
   globalThis.Worker
   (fn [url options]
     (when (or (not= url "./worker.mjs") (not= options.type "module"))
       (throw (new Error "Page did not request its module worker")))
     (var
      harness
      (+
       "import {parentPort} from 'node:worker_threads';globalThis.self={postMessage:v=>parentPort.postMessage(v)};await import("
       (JSON.stringify workerURL)
       ");parentPort.on('message',data=>self.onmessage({data}));"))
     (:= thread
         (new threads.Worker
              (new URL (+ "data:text/javascript," (encodeURIComponent harness)))
              {"type" "module"}))
     (return {"postMessage" (fn [value] (. thread (postMessage value)))
              "terminate" (fn [] (return (. thread (terminate))))
              "addEventListener" (fn [name callback]
                                   (. thread
                                      (on name
                                          (fn [value]
                                            (callback (:? (== name "message")
                                                          {"data" value}
                                                          {"message" value.message}))))))})))
  (var reply
       (fn []
         (return (new
                  Promise
                  (fn [resolve reject]
                    (var timer
                         (setTimeout (fn [] (reject (new Error "Page worker reply timeout"))) 5000))
                    (. thread (once "message" (fn [value] (clearTimeout timer) (resolve value))))
                    (. thread (once "error" (fn [error] (clearTimeout timer) (reject error)))))))))
  (var submit
       (fn [title]
         (:= (. (. root (querySelector "input")) value) title)
         (. (. root (querySelector "form"))
            (dispatchEvent (new win.Event "submit" {"bubbles" true "cancelable" true})))
         (return (reply))))
  (try (:= dispose (app.mount root))
       (await (reply))
       (var loaded (. (. root (querySelector "input")) value))
       (var saved (await (submit "Saved from the page")))
       (var savedStatus (. (. root (querySelector "[data-status]")) textContent))
       (var rejected (await (submit "   ")))
       (var rejectedStatus (. (. root (querySelector "[data-status]")) textContent))
       (var retained (. (. root (querySelector "input")) value))
       (await (submit "Corrected title"))
       (return [loaded (. saved.result [0] ["title"]) savedStatus rejected.error rejectedStatus
                retained (. (. root (querySelector "input")) value)
                (. (. root (querySelector "[data-status]")) textContent)])
       (catch error (throw error))
       (finally (when dispose
                  (dispose))
                (when thread
                  (await (. thread (terminate))))
                (:= globalThis.Worker originalWorker)
                (:= globalThis.FormData originalFormData)
                (win.close))))

^{:refer dupasupa.pages.main.campaigns/mount :id page-save-workflow :timeout 30000}
(fact
  "the emitted page submits real forms to its worker, confirms saves, and allows correction after rejection"
  (integration/await-js (-/page-save-workflow))
  =>
  ["First campaign" "Saved from the page" "Saved." "Campaign title is required"
   "Campaign title is required" "   " "Corrected title" "Saved."])

(defn.js ^{:- [:async]} react-save-workflow
  []
  (var localRequire
       (. (require "node:module")
          (createRequire (@! (.getCanonicalPath (io/file "package.json"))))))
  (var Window (localRequire "window"))
  (var win (new Window {"url" "http://localhost/"}))
  (var keys ["window" "document" "navigator" "HTMLElement" "Event" "IS_REACT_ACT_ENVIRONMENT"])
  (var before {})
  (for:array
   [key keys]
   (:= (. before [key]) (Object.getOwnPropertyDescriptor globalThis key))
   (Object.defineProperty
    globalThis
    key
    {"configurable" true
     "writable" true
     "value"
     (:? (== key "window") win (== key "IS_REACT_ACT_ENVIRONMENT") true :else (. win [key]))}))
  (var React (localRequire "react"))
  (var ReactDOM (localRequire "react-dom/client"))
  (var componentURL
       (urls.pathToFileURL (@! (.getCanonicalPath (io/file "generated/next/app/campaigns.js")))))
  (var component (await (import componentURL.href)))
  (var element (win.document.createElement "div"))
  (. win.document.body (appendChild element))
  (var root (ReactDOM.createRoot element))
  (var transport
       {"list" (fn [] (return (client/call DB "list-campaigns" [])))
        "call" (fn [name values]
                 (return (client/call DB
                                      name
                                      (:? (== name "create-owned-campaign")
                                          [(. (JSON.parse process.env.DUPASUPA_DEV_ACCOUNTS)
                                              [0]
                                              ["id"]) values.i_title]
                                          [values.i_id values.i_title]))))})
  (var waitFor
       (async (fn [predicate]
                (var i 0)
                (for [(:= i 0) (< i 100)
                      (:= i (+ i 1))]
                  (when (predicate)
                    (return true))
                  (await (React.act
                          (async (fn []
                                   (await (new Promise (fn [resolve] (setTimeout resolve 20)))))))))
                (throw (new Error "React state did not update")))))
  (var render
       (async (fn []
                (await (React.act (async (fn []
                                           (. root
                                              (render (React.createElement component.default
                                                                           {"transport"
                                                                            transport})))))))
                (await (waitFor (fn [] (return (not= (. element (querySelector "input")) nil))))))))
  (var
   submit
   (async
    (fn [title]
      (await (React.act
              (async
               (fn []
                 (var input (. element (querySelector "input")))
                 (. (. (Object.getOwnPropertyDescriptor win.HTMLInputElement.prototype "value") set)
                    (call input title))
                 (. input (dispatchEvent (new win.Event "input" {"bubbles" true})))))))
      (await (React.act (async (fn []
                                 (. (. element (querySelector "form"))
                                    (dispatchEvent (new win.Event
                                                        "submit"
                                                        {"bubbles" true "cancelable" true})))))))
      (await (waitFor (fn [] (return (not (. (. element (querySelector "button")) disabled)))))))))
  (var submitCreate
       (async
        (fn [title]
          (await (React.act
                  (async (fn []
                           (var input (. element (querySelector "input[name=new-title]")))
                           (. (. (Object.getOwnPropertyDescriptor win.HTMLInputElement.prototype
                                                                  "value")
                                 set)
                              (call input title))
                           (. input (dispatchEvent (new win.Event "input" {"bubbles" true})))))))
          (await (React.act (async
                             (fn []
                               (. (. element (querySelector "form[aria-label=\"Create campaign\"]"))
                                  (dispatchEvent
                                   (new win.Event "submit" {"bubbles" true "cancelable" true})))))))
          (await (waitFor (fn []
                            (return (not (. (. element (querySelector "input[name=new-title]"))
                                            disabled)))))))))
  (try (await (render))
       (await (submit "React saved title"))
       (var saved
            [(. (. element (querySelector "h2")) textContent)
             (. (. element (querySelector "[role=status]")) textContent)])
       (await (submit "   "))
       (var rejected
            [(. (. element (querySelector "h2")) textContent)
             (. (. element (querySelector "[role=status]")) textContent)])
       (await (React.act (async (fn [] (. root (unmount))))))
       (:= root (ReactDOM.createRoot element))
       (await (render))
       (var reloaded (. (. element (querySelector "h2")) textContent))
       (await (submitCreate "Created in React"))
       (return [saved rejected reloaded
                [(. element.textContent (includes "Created in React"))
                 (. (. element (querySelector "input[name=new-title]")) value)
                 (. (. element (querySelectorAll "[data-saved-title]")) length)
                 (. (. (. element (querySelector "form[aria-label=\"Create campaign\"]"))
                       parentElement
                       (querySelector "[role=status]"))
                    textContent)]])
       (catch error (throw error))
       (finally (await (React.act (async (fn [] (. root (unmount))))))
                (for:array [key keys]
                           (if (. before [key])
                             (Object.defineProperty globalThis key (. before [key]))
                             (delete (. globalThis [key]))))
                (win.close))))

^{:refer dupasupa.pages.main.react-campaigns/Campaigns :id react-save-workflow :timeout 30000}
(fact
  "React submits the edited title, displays the committed database value, and reloads it after remount"
  (integration/await-js (-/react-save-workflow))
  =>
  [["React saved title" "Saved."] ["React saved title" "Campaign title is required"]
   "React saved title" [true "" 2 "Campaign created."]]
  (:title (pg/t:get tables/Campaign {:where {:id CAMPAIGN}})) => "React saved title")

(defn.js ^{:- [:async]} apiRequest
  [actor path method values]
  (var moduleURL
       (urls.pathToFileURL (@! (.getCanonicalPath (io/file "generated/next/lib/server.mjs")))))
  (var api (await (import moduleURL.href)))
  (var headers {"content-type" "application/json"})
  (when actor
    (:= headers.authorization (+ "Bearer " actor)))
  (var request
       (new
        Request
        (+ "http://localhost" path)
        {"method" method "headers" headers "body" (:? values (JSON.stringify values) undefined)}))
  (var handler
       (:? (== path "/api/profile")
           (:? (== method "GET") api.GET_PROFILE api.POST_PROFILE)
           :else
           (:? (== method "GET") api.GET api.POST)))
  (return (await (handler request))))

^{:refer dupasupa.server/verifiedUser :id verified-auth}
(fact "profile endpoints require verified Supabase Auth and ignore caller-supplied user ids"
  (integration/await-js (. (-/apiRequest nil "/api/profile" "GET" nil)
                           (then (fn [response] (return response.status)))))
  =>
  401
  (integration/await-js (. (-/apiRequest "invalid" "/api/profile" "GET" nil)
                           (then (fn [response] (return response.status)))))
  =>
  401
  (integration/await-js (. (-/apiRequest (@! (:token (first (deref (:accounts AUTH)))))
                                         "/api/profile"
                                         "POST"
                                         {"id" (@! (:id (second (deref (:accounts AUTH)))))
                                          "handle" "alice"
                                          "display_name" "Alice Profile"
                                          "bio" "Diving and building"})
                           (then (async (fn [response]
                                          (var profile (await (. response (json))))
                                          (return [response.status profile.id profile.display_name
                                                   profile.bio]))))))
  =>
  [200 OWNER "Alice Profile" "Diving and building"]
  (:display-name (pg/t:get tables/User {:where {:id (:id (second (deref (:accounts AUTH))))}}))
  =>
  "Bob Demo"
  (integration/await-js
   (. (-/apiRequest (@! (:token (second (deref (:accounts AUTH))))) "/api/campaigns" "GET" nil)
      (then (fn [response] (return (. response (json)))))))
  =>
  []
  (integration/await-js
   (. (-/apiRequest (@! (:token (second (deref (:accounts AUTH)))))
                    "/api/campaigns"
                    "POST"
                    {"id" (@! CAMPAIGN) "title" "Stolen title"})
      (then (async (fn [response] (return [response.status (await (. response (json)))]))))))
  =>
  [400 {"error" "Campaign is not available to this account"}]
  (:title (pg/t:get tables/Campaign {:where {:id CAMPAIGN}})) => "React saved title")

(defn.js ^{:- [:async]} profile-auth-workflow
  []
  (var localRequire
       (. (require "node:module")
          (createRequire (@! (.getCanonicalPath (io/file "package.json"))))))
  ;; Use the same browser aliases as the generated Next.js bundler, without mocks.
  (var hooks
       (. (require "node:module")
          (registerHooks {"resolve" (fn [specifier context next]
                                      (return (next (:? (== specifier "react-native-svg")
                                                        "@tamagui/react-native-svg"
                                                        (== specifier "react-native")
                                                        "react-native-web"
                                                        :else
                                                        specifier)
                                                    context)))})))
  (var Window (localRequire "window"))
  (var win (new Window {"url" "http://localhost/" "pretendToBeVisual" true}))
  ;; JSDOM has no layout/media APIs; provide browser environment adapters only.
  (:= win.matchMedia
      (fn [query]
        (var target (new win.EventTarget))
        (:= target.media query)
        (:= target.matches
            (. (localRequire "css-mediaquery")
               (match query {"type" "screen" "width" win.innerWidth "height" win.innerHeight})))
        (:= target.addListener (fn [listener] (. target (addEventListener "change" listener))))
        (:= target.removeListener
            (fn [listener] (. target (removeEventListener "change" listener))))
        (return target)))
  (var keys
       ["window" "document" "navigator" "HTMLElement" "Element" "Node" "NodeFilter" "Document"
        "DocumentFragment" "HTMLInputElement" "HTMLTextAreaElement" "HTMLSelectElement" "ShadowRoot"
        "MutationObserver" "ResizeObserver" "SVGElement" "HTMLBodyElement" "HTMLDocument"
        "CustomEvent" "Event" "addEventListener" "removeEventListener" "getComputedStyle"
        "requestAnimationFrame" "cancelAnimationFrame" "DevQueries" "IS_REACT_ACT_ENVIRONMENT"])
  (var before {})
  (for:array [key keys]
             (:= (. before [key]) (Object.getOwnPropertyDescriptor globalThis key))
             (Object.defineProperty globalThis
                                    key
                                    {"configurable" true
                                     "writable" true
                                     "value" (:? (== key "window")
                                                 win
                                                 (== key "IS_REACT_ACT_ENVIRONMENT")
                                                 false
                                                 :else
                                                 (:? (. ["addEventListener" "removeEventListener"
                                                         "getComputedStyle" "requestAnimationFrame"
                                                         "cancelAnimationFrame"]
                                                        (includes key))
                                                     (. (. win [key]) (bind win))
                                                     (. win [key])))}))
  (:= win.ResizeObserver (. (localRequire "@juggle/resize-observer") ResizeObserver))
  (:= globalThis.ResizeObserver win.ResizeObserver)
  (var React (localRequire "react"))
  (var ReactDOM (localRequire "react-dom/client"))
  (var flushSync (. (localRequire "react-dom") flushSync))
  (var componentURL
       (urls.pathToFileURL (@!
                            (.getCanonicalPath (io/file "generated/next/app/profile-client.js")))))
  (var component (await (import componentURL.href)))
  (var element (win.document.createElement "div"))
  (. win.document.body (appendChild element))
  (var root (ReactDOM.createRoot element))
  (var authURL (urls.pathToFileURL (@! (.getCanonicalPath (io/file "generated/next/app/auth.js")))))
  (var auth (await (import authURL.href)))
  (var apiURL
       (urls.pathToFileURL (@! (.getCanonicalPath (io/file "generated/next/lib/server.mjs")))))
  (var api (await (import apiURL.href)))
  (var originalFetch globalThis.fetch)
  (var originalMode process.env.NODE_ENV)
  (:= process.env.NODE_ENV "development")
  (:= globalThis.fetch
      (fn [url options]
        (when (and (== (typeof url) "string") (. url (startsWith "/api/")))
          (when (== url "/api/auth/config")
            (return (Promise.resolve (api.GET_CONFIG))))
          (var request (new Request (+ "http://localhost" url) options))
          (return
           (:? (== options.method "POST") (api.POST_PROFILE request) (api.GET_PROFILE request))))
        (return (originalFetch url options))))
  (var waitFor
       (async (fn [predicate]
                (var i 0)
                (for [(:= i 0) (< i 150)
                      (:= i (+ i 1))]
                  (when (predicate)
                    (return true))
                  (await (new Promise (fn [resolve] (setTimeout resolve 20)))))
                (throw (new Error "React state did not update")))))
  (var render
       (async (fn []
                (flushSync (fn []
                             (. root
                                (render (React.createElement auth.AuthProvider
                                                             {}
                                                             (React.createElement component.default
                                                                                  {}))))))
                (await (waitFor (fn []
                                  (var button
                                       (. element (querySelector "[aria-label=\"Test accounts\"]")))
                                  (return (and button (not button.disabled)))))))))
  (var click
       (async (fn [selector]
                (flushSync (fn []
                             (. (. win.document (querySelector selector))
                                (dispatchEvent (new win.MouseEvent "click" {"bubbles" true}))))))))
  (var inspectQueries
       (async (fn []
                (when (not (. win.document (querySelector "[data-devtool-action=queries]")))
                  (await (click "[aria-label=\"Inspect runtime\"]")))
                (await (waitFor (fn []
                                  (return (. win.document
                                             (querySelector "[data-devtool-action=queries]"))))))
                (await (click "[data-devtool-action=queries]"))
                (return globalThis.DevQueries))))
  (var signIn
       (async (fn [handle name]
                (var selector (+ "[aria-label=\"Sign in as " handle "\"]"))
                (when (not (. win.document (querySelector selector)))
                  (await (click "[aria-label=\"Test accounts\"]")))
                (await (waitFor (fn [] (return (. win.document (querySelector selector))))))
                (await (click selector))
                (await (waitFor (fn []
                                  (var heading (. element (querySelector "h2")))
                                  (return (and heading (== heading.textContent name)))))))))
  (var
   submit
   (async
    (fn [title]
      (flushSync (fn []
                   (var input (. element (querySelector "input")))
                   (. (. (Object.getOwnPropertyDescriptor win.HTMLInputElement.prototype "value")
                         set)
                      (call input title))
                   (. input (dispatchEvent (new win.Event "input" {"bubbles" true})))))
      (flushSync (fn []
                   (. (. element (querySelector "form"))
                      (dispatchEvent (new win.Event "submit" {"bubbles" true "cancelable" true})))))
      (await (waitFor (fn [] (return (not (. (. element (querySelector "button")) disabled)))))))))
  (try (await (render))
       (await (signIn "alice" "Alice Profile"))
       (await (submit "Profile saved"))
       (var saved
            [(. (. element (querySelector "h2")) textContent)
             (. (. element (querySelector "main [role=status]")) textContent)])
       (var savedQuery (await (inspectQueries)))
       (var savedInspection savedQuery.profile.output.display_name)
       (await (submit "   "))
       (var rejected
            [(. (. element (querySelector "h2")) textContent)
             (. (. element (querySelector "main [role=status]")) textContent)])
       (await (signIn "bob" "Bob Demo"))
       (var switched
            [(. (. element (querySelector "h2")) textContent)
             (. (. element (querySelector "input")) value)])
       (var switchedQuery (await (inspectQueries)))
       (var switchedInspection switchedQuery.profile.output.display_name)
       (await (click "[aria-label=\"Sign out\"]"))
       (await (waitFor (fn [] (return (== (. element (querySelector "h2")) nil)))))
       (var clearedQuery (await (inspectQueries)))
       (var clearedInspection (. (Object.keys clearedQuery) length))
       (return [saved rejected switched (. (. element (querySelector "main")) textContent)
                [savedInspection switchedInspection clearedInspection]])
       (catch error (throw error))
       (finally (await (React.act (async (fn [] (. root (unmount))))))
                (await (new Promise (fn [resolve] (setTimeout resolve 20))))
                (for:array [key keys]
                           (if (. before [key])
                             (Object.defineProperty globalThis key (. before [key]))
                             (delete (. globalThis [key]))))
                (:= globalThis.fetch originalFetch)
                (if (== originalMode undefined)
                  (delete process.env.NODE_ENV)
                  (:= process.env.NODE_ENV originalMode))
                (. hooks (deregister))
                (win.close))))

^{:refer dupasupa.pages.main.profile/Profile :id profile-auth-workflow :timeout 30000}
(fact
  "the shared auth devtools signs in real users, edits profiles, switches accounts and clears private UI on sign-out"
  (integration/await-js (-/profile-auth-workflow))
  =>
  [["Profile saved" "Saved."] ["Profile saved" "Display name must contain 1 to 80 characters"]
   ["Bob Demo" "Bob Demo"] "DUPASUPAYour profileSign in using Devtools to view your profile."
   ["Profile saved" "Bob Demo" 0]]
  (:display-name (pg/t:get tables/User {:where {:id OWNER}})) => "Profile saved"
  (:display-name (pg/t:get tables/User {:where {:id (:id (second (deref (:accounts AUTH))))}}))
  =>
  "Bob Demo")

(defn.js ^{:- [:async]} production-auth-config
  []
  (var url (urls.pathToFileURL (@! (.getCanonicalPath (io/file "generated/next/lib/server.mjs")))))
  (var api (await (import url.href)))
  (var mode process.env.NODE_ENV)
  (try (:= process.env.NODE_ENV "production")
       (var value (await (. (api.GET_CONFIG) (json))))
       (return [(. (Object.keys value) (sort)) value.accounts.length])
       (catch error (throw error))
       (finally
        (if (== mode undefined) (delete process.env.NODE_ENV) (:= process.env.NODE_ENV mode)))))

^{:refer dupasupa.server/GET_CONFIG :id production-auth-config}
(fact
  "production config exposes only the public auth connection and excludes dev account credentials"
  (integration/await-js (-/production-auth-config)) => [["accounts" "key" "url"] 0])

^{:refer dupasupa.model/close :id cleanup}
(fact "closing the model and scaffold releases client state and its owned database"
  (integration/await-js (Promise.resolve ((fn []
                                            (event-model/remove-listener ROW_MODEL "integration")
                                            (return (model/close HANDLE))))))
  =>
  true
  (integration/await-js (. (model/loadRows HANDLE)
                           (then (fn [] (return "unexpected")))
                           (catch (fn [error] (return error.message)))))
  =>
  "Campaign page is closed."
  (integration/stop! SCAFFOLD) => true
  (database/exists? DATABASE) => false)
