(ns app.nextjs.devtools-test
  (:use code.test)
  (:require [app.nextjs.build :as build]
            [app.manage :as manage]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [lang.core :as l]
            [app.manage.integration :as integration]))

(l/script- :js
  {:runtime :basic
   :test-mode true
   :require [[app.nextjs.devtools.runtime :as runtime]
             [xt.lang.common-repl :as repl]]})
(fact:global {:setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

^{:refer app.nextjs.devtools.runtime/snapshot :id inspection}
(fact "inspection preserves useful state while removing secrets and terminating cycles"
  (!.js (var state
             {"user" {"id" "alice" "email" "alice@example.test"}
              "access_token" "private-token"
              "nested" {"password" "private-password"}
              "query" {"output" [{"id" "campaign" "title" "Saved"}]}})
        (:= state.self state)
        (runtime/snapshot state))
  =>
  {"user" {"id" "alice" "email" "alice@example.test"}
   "access_token" "[redacted]"
   "nested" {"password" "[redacted]"}
   "query" {"output" [{"id" "campaign" "title" "Saved"}]}
   "self" "[circular]"})

^{:refer app.nextjs.devtools.runtime/sign-in :id auth-client}
(fact "auth actions use the supplied Supabase client and propagate its actual result or rejection"
  (integration/await-js (runtime/sign-in {"auth" {"signInWithPassword"
                                                  (fn [input]
                                                    (return (Promise.resolve
                                                             {"data" {"user" {"id" "alice"
                                                                              "email" input.email}}
                                                              "error" nil})))}}
                                         {"email" "alice@example.test" "password" "fixture"}))
  =>
  {"data" {"user" {"id" "alice" "email" "alice@example.test"}} "error" nil}
  (integration/await-js (. (runtime/sign-in nil {"email" "alice@example.test" "password" "fixture"})
                           (then (fn [] (return {"rejected" false})))
                           (catch (fn [error] (return {"rejected" true "message" error.message})))))
  =>
  {"rejected" true "message" "Supply the application's Supabase client."}
  (integration/await-js
   (. (runtime/sign-in
       {"auth" {"signInWithPassword"
                (fn [] (return (Promise.resolve {"error" (new Error "Invalid credentials")})))}}
       {"email" "alice@example.test" "password" "wrong"})
      (then (fn [] (return {"rejected" false})))
      (catch (fn [error] (return {"rejected" true "message" error.message})))))
  =>
  {"rejected" true "message" "Invalid credentials"})

^{:refer app.nextjs.build/artifacts :id devtools-boundary :timeout 60000}
(fact
  "publication includes the copied panel and inspection hooks without application layouts or StatsTrade modules"
  (let [files (build/artifacts {:root (.getCanonicalFile (io/file "."))})
        code (get files "target/app-nextjs/src/devtools.mjs")]
    [(boolean (re-find #"export function FrameDevtoolPanel" code))
     (every? #(str/includes? code %)
             ["useDevtoolInjection" "useDevtoolSharedWorker" "FrameDevtoolUser"
              "FrameDevtoolSharedWorker" "FrameDevtoolActivity"])
     (some #(or (str/includes? % "layout-base")
                (str/includes? % "layout-auth")
                (str/includes? % "statsui"))
           (keys files))
     (some #(re-find #"@statstrade/|statsui[./]|szn_type|statsdb[./]" %) (vals files))
     (boolean (get files "target/app-nextjs/src/devtools/runtime.mjs"))])
  =>
  [true true nil nil true])
