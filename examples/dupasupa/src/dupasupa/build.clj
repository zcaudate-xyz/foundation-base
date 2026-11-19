(ns dupasupa.build
  "Project declaration only: shared managers own compilation, watching and verification."
  (:require [clojure.data.json :as json]
            [app.manage.make :as make]
            [app.manage.manifest :as manifest]
            [app.manage.verify :as verify]
            [app.manage.integration :as integration]
            [app.manage.postgres :as postgres]
            [app.manage.site-map :as site-map]
            [dupasupa.config :as config]
            [dupasupa.groups]))

(def PROJECT
  (manifest/with-workbench
   {:id 'dupasupa
    :root "."
    :source-paths ["src"]
    :project-sources ["src/dupasupa/build.clj" "src/dupasupa/config.clj"]
    :page-verifier 'dupasupa.verification/execute
    :page-test-fixture 'dupasupa.verification/with-fixture
    :verification-targets [:page-verifier]
    :verification-receipt "generated/verification/receipt.edn"
    :slices
    (into {}
          (for [[id label] [[:campaigns "Owned campaigns"] [:profile "Own profile"]]]
            [id
             {:label label
              :description "Real database → RPC → generated Substrate model → React UI"
              :stages
              [{:id :stack
                :label "Local Supabase stack"
                :targets []
                :blocker "Dedicated min/dev profiles and live PostgREST checks still required"}
               {:id :schema :label "DB schema" :targets [:database]}
               {:id :logic :label "DB application logic" :targets [:database]}
               {:id :rpc :label "RPC and specs" :targets [:contracts-main]}
               {:id :interop
                :label "JS interop"
                :targets [:next]
                :blocker "Generated RPC bindings and live Supabase provider adapter still required"}
               {:id :webhooks :label "Webhooks" :targets [] :not-applicable true}
               {:id :backbone
                :label "Substrate backbone"
                :targets [:library]
                :blocker
                (when (= id :profile)
                  "Profile currently uses component state; Substrate profile model still required")}
               {:id :types
                :label "Generated UI types"
                :targets []
                :blocker "RPC JSON payload shapes and TypeScript publication still required"}
               {:id :ui :label "UI" :targets [:next] :checks [:next-build :integration]}]}]))
    :previews
    [{:id :application
      :label "Campaigns · App Workbench"
      :target :next
      :url "http://127.0.0.1:8000/web-main"
      :command "make serve"}
     {:id :profile
      :label "Profile · App Workbench"
      :target :next
      :url "http://127.0.0.1:8000/profile"
      :command "make serve"}
     {:id :public :label "Public page" :target :web-public :path "generated/web-public/index.html"}]
    :default-targets [:next :web-public :page-verifier :integration]
    :targets
    {:database (postgres/target (merge config/DATABASE
                                       {:sources ["src/dupasupa/db.clj" "src/dupasupa/db"
                                                  "src/dupasupa/config.clj"]
                                        :output-root "generated/database"
                                        :emit {:code {:label false}}}))
     :library (make/target {:depends [:database]
                            :sources ["src/dupasupa/model.clj" "src/dupasupa/groups.clj"
                                      "src/dupasupa/seed.clj" "src/dupasupa/pages.clj" "src/dupasupa/bindings.clj"]
                            :outputs ["generated/lib"]
                            :load ['dupasupa.model]
                            :sections [{:type :module.graph
                                        :lang :js
                                        :main 'dupasupa.model
                                        :target "generated/lib"
                                        :emit {:code {:label false :link {:path-suffix ".mjs"}}}}]})
     :contracts-main (site-map/target #(postgres/registry config/DATABASE)
                                      {:site "web-main"
                                       :source-root "src/dupasupa/pages/main"
                                       :sources ["src/dupasupa/pages.clj" "src/dupasupa/groups.clj"]
                                       :depends [:database]
                                       :output-root "generated/web-main/worker/site-map"}
                                      json/write-str)
     :contracts-public (site-map/target #(postgres/registry config/DATABASE)
                                        {:site "web-public"
                                         :source-root "src/dupasupa/pages/public"
                                         :depends [:database]
                                         :output-root "generated/web-public/worker/site-map"}
                                        json/write-str)
     :workers (make/target {:depends [:library]
                            :sources ["src/dupasupa/worker.clj"]
                            :outputs ["generated/web-main/worker.mjs"]
                            :load ['dupasupa.worker]
                            :sections [{:type :module.single
                                        :lang :js
                                        :main 'dupasupa.worker
                                        :file "generated/web-main/worker.mjs"
                                        :emit {:code {:label false}}}]})
     :web-main
     (make/target
      {:depends [:contracts-main :workers]
       :sources ["src/dupasupa/pages/main"]
       :outputs ["generated/web-main/app.mjs" "generated/web-main/index.html"]
       :load ['dupasupa.pages.main.campaigns]
       :sections
       [{:type :module.single
         :lang :js
         :main 'dupasupa.pages.main.campaigns
         :file "generated/web-main/app.mjs"
         :emit {:code {:label false}}}
        {:type :raw
         :file "generated/web-main/index.html"
         :main
         ["<!doctype html><html lang=\"en\"><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
          "<title>Dupasupa campaigns</title><style>body{font:18px system-ui;max-width:48rem;margin:4rem auto;padding:0 1rem}form{display:flex;gap:1rem;margin:1rem 0}input,button{font:inherit;padding:.5rem}label{display:grid;gap:.5rem}</style>"
          "<main id=\"app\">Loading campaigns…</main><script type=\"module\" src=\"./app.mjs\"></script></html>"]}]})
     :web-public
     (make/target
      {:depends [:contracts-public]
       :sources ["src/dupasupa/pages/public"]
       :outputs ["generated/web-public/index.html"]
       :sections
       [{:type :raw
         :file "generated/web-public/index.html"
         :main
         ["<!doctype html><html lang=\"en\"><meta charset=\"utf-8\"><title>Dupasupa</title>"
          "<h1>Dupasupa</h1><p>A Clojure-authored application managed by app.manage.</p><a href=\"../web-main/\">Open campaigns</a></html>"]}]})
     :next
     {:depends [:web-main]
      :sources ["src/dupasupa/pages/main/react_campaigns.clj" "src/dupasupa/pages/main/profile.clj"
                "src/dupasupa/auth.clj" "src/dupasupa/auth_context.clj" "src/dupasupa/server.clj"]
      :outputs ["generated/next/app" "generated/next/lib" "generated/next/next.config.mjs"]
      :render
      (fn [context]
        (let [files (make/render-sections
                     context
                     {:load ['dupasupa.pages.main.react-campaigns 'dupasupa.server
                             'dupasupa.pages.main.profile 'dupasupa.auth 'dupasupa.auth-context
                             'app.nextjs.devtools]
                      :sections [{:type :module.graph
                                  :lang :js
                                  :main 'app.nextjs.devtools
                                  :target "generated/next/lib/devtools"
                                  :emit {:lang/jsx false
                                         :code {:label false :link {:path-suffix ".mjs"}}}}
                                 {:type :module.single
                                  :lang :js
                                  :main 'dupasupa.auth-context
                                  :file "generated/next/app/auth-context.js"
                                  :emit {:code {:label false}}}
                                 {:type :module.single
                                  :lang :js
                                  :main 'dupasupa.auth
                                  :file "generated/next/app/auth.js"
                                  :emit {:code {:label false}}}
                                 {:type :module.single
                                  :lang :js
                                  :main 'dupasupa.pages.main.profile
                                  :file "generated/next/app/profile-client.js"
                                  :emit {:code {:label false}}}
                                 {:type :module.single
                                  :lang :js
                                  :main 'dupasupa.pages.main.react-campaigns
                                  :file "generated/next/app/campaigns.js"
                                  :emit {:code {:label false}}}
                                 {:type :module.single
                                  :lang :js
                                  :main 'dupasupa.server
                                  :file "generated/next/lib/server.mjs"
                                  :emit {:code {:label false}}}]})]
          (merge
           (reduce (fn [out file] (update out file #(str "\"use client\";\n" %)))
                   files
                   ["generated/next/app/campaigns.js" "generated/next/app/auth.js"
                    "generated/next/app/profile-client.js" "generated/next/app/auth-context.js"])
           {"generated/next/app/api/campaigns/route.js"
            "export const runtime='nodejs';export const dynamic='force-dynamic';export {GET,POST} from '../../../lib/server.mjs';\n"
            "generated/next/app/api/profile/route.js"
            "export const runtime='nodejs';export const dynamic='force-dynamic';export {GET_PROFILE as GET,POST_PROFILE as POST} from '../../../lib/server.mjs';\n"
            "generated/next/app/api/auth/config/route.js"
            "export const runtime='nodejs';export const dynamic='force-dynamic';export {GET_CONFIG as GET} from '../../../../lib/server.mjs';\n"
            "generated/next/app/profile/page.js" "export {default} from '../profile-client.js';\n"
            "generated/next/app/page.js" "export {default} from './campaigns.js';\n"
            "generated/next/app/web-main/page.js" "export {default} from '../campaigns.js';\n"
            "generated/next/app/layout.js"
            "import React from 'react';import {AuthProvider} from './auth.js';import './style.css';export const metadata={title:'Dupasupa campaigns'};export default function Layout({children}){return React.createElement('html',{lang:'en'},React.createElement('body',null,React.createElement(AuthProvider,null,children)));}\n"
            "generated/next/app/style.css"
            "*{box-sizing:border-box}body{margin:0;background:#f4f5f3;color:#182a24;font:17px system-ui}main{max-width:760px;margin:72px auto;padding:24px}header{margin-bottom:40px}h1{font-size:42px;margin:8px 0}.eyebrow{font-size:13px;letter-spacing:.15em}.campaign{background:white;border:1px solid #d7ded8;border-radius:16px;padding:28px;margin:20px 0}h2{margin:0 0 24px}form{display:flex;align-items:flex-end;gap:16px}label{display:grid;gap:8px;flex:1}input,button{font:inherit;padding:12px;border-radius:8px}input{border:1px solid #a6b6ad;min-width:0}button{background:#205b43;color:white;border:0;cursor:pointer}button:disabled{opacity:.6;cursor:wait}[role=status]{min-height:24px;color:#205b43}.app-nav{display:flex;gap:24px;padding:22px 32px;background:white;border-bottom:1px solid #d7ded8}a{color:#205b43}.profile-form{display:grid;gap:18px}.profile-form button{justify-self:start}textarea{font:inherit;padding:12px;border:1px solid #a6b6ad;border-radius:8px;resize:vertical}.profile-email{margin-bottom:24px;color:#587063}.devtools{position:fixed;bottom:20px;right:20px;padding:14px;background:#fff;border:1px solid #a6b6ad;border-radius:12px;max-width:440px;box-shadow:0 4px 20px #0001}.devtools button{font-size:13px;margin:8px 8px 0 0}.devtools p{font-size:13px;overflow-wrap:anywhere}.devtools summary{cursor:pointer;font-weight:600}"
            "generated/next/next.config.mjs"
            "import {fileURLToPath} from 'node:url';const root=fileURLToPath(new URL('../../',import.meta.url));export default {serverExternalPackages:['pg'],devIndicators:false,agentRules:false,distDir:process.env.DUPASUPA_NEXT_DIST_DIR||'.next',turbopack:{root,resolveAlias:{'react-native':'react-native-web','react-native-svg':'@tamagui/react-native-svg'}},webpack(config){config.resolve.alias={...config.resolve.alias,'react-native$':'react-native-web','react-native-svg$':'@tamagui/react-native-svg'};return config},outputFileTracingRoot:root};\n"})))}
     :page-verifier (verify/with-receipt
                     (make/target
                      {:depends [:next]
                       :sources ["src/dupasupa/pages.clj" "src/dupasupa/page_verifier.clj"
                                 "src/dupasupa/verification.clj" "src/dupasupa/auth_fixture.clj"]
                       :outputs ["generated/verification"]
                       :load ['dupasupa.page-verifier]
                       :sections [{:type :module.graph
                                   :lang :js
                                   :main 'dupasupa.page-verifier
                                   :target "generated/verification"
                                   :emit {:code {:label false :link {:path-suffix ".mjs"}}}}
                                  {:type :raw
                                   :file "generated/verification/worker.mjs"
                                   :main ["import './page-verifier.mjs';"]}]})
                     "generated/verification/receipt.edn")
     :integration {:depends [:next :web-public]
                   :sources ["test/dupasupa/integration_test.clj"]
                   :outputs []
                   :render (fn [_] {})}}
    :checks {:page-model-verification (integration/check {:targets [:page-verifier]
                                                          :namespaces
                                                          ['dupasupa.page-verification-test
                                                           'dupasupa.workbench-test]})
             :next-build {:targets [:next] :command ["npm" "run" "build"] :timeout-ms 120000}
             :integration (integration/check {:targets [:integration]
                                              :namespaces ['dupasupa.integration-test]})}}
   {:application 'dupasupa.pages/definitions
    :depends [:database]
    :sources ["src/dupasupa/pages.clj" "src/dupasupa/db"]}))
