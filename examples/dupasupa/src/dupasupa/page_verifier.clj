(ns dupasupa.page-verifier
  (:require [lang.core :as l]
            [dupasupa.pages :as pages]))
(l/script :js
  {:require [[app.manage.page-runtime :as runtime]]
   :import [["../lib/model.mjs" :as model] ["../next/lib/server.mjs" :as api]
            ["node:fs" :as [* fs]]]
   :static {:export [execute]}})

(def.js DEFINITIONS (@! (pages/definitions)))

(defn.js ^{:- [:async]} execute
  [request]
  (var actors (JSON.parse (or process.env.DUPASUPA_VERIFY_ACTORS "{}")))
  (var handles {})
  (var trace [])
  (var results {})
  (var send
       (async
        (fn [actor method path body]
          (var user (. actors [actor]))
          (var headers {"content-type" "application/json"})
          (when user
            (:= headers.authorization (+ "Bearer " user.token)))
          (var req
               (new Request
                    (+ "http://localhost" path)
                    {"method" method
                     "headers" headers
                     "body" (:? body (JSON.stringify body) undefined)}))
          (var routes
               {"GET /api/profile" api.GET_PROFILE
                "GET /api/campaigns" api.GET
                "POST /api/campaigns" api.POST})
          (var handler (. routes [(+ method " " path)]))
          (when (not handler)
            (throw (new Error "Unsupported verified route.")))
          (var response (await (handler req)))
          (var value (await (. response (json))))
          (. trace (push {"actor" actor "method" method "path" path "status" response.status}))
          (when (not response.ok)
            (throw (Object.assign (new Error value.error) {"status" response.status})))
          (return value))))
  (var
   run
   (async
    (fn [step]
      (try
        (var actor (or step.actor "alice"))
        (when (and (not (. actors [actor])) (not= actor "anon"))
          (return {"blocked" "Choose alice, bob or anon as the fixture actor."}))
        (var handleFor
             (fn []
               (when (not (. handles [actor]))
                 (:= (. handles [actor])
                     (model.open
                      {"list" (fn []
                                (var binding
                                     (runtime/request (runtime/operation -/DEFINITIONS
                                                                         "list-owned-campaigns")
                                                      {}))
                                (return (send actor binding.method binding.path binding.body)))
                       "call" (fn [operation values]
                                (var entry
                                     (runtime/operation -/DEFINITIONS
                                                        (:? (== operation "save-campaign")
                                                            "save-owned-campaign"
                                                            operation)))
                                (var binding (runtime/request entry values))
                                (return (send actor binding.method binding.path binding.body)))})))
               (return (. handles [actor]))))
        (var adapters
             {"ret-profile"
              (async (fn [input entry]
                       (var binding (runtime/request entry (or input.payload {})))
                       (return {"result" (await
                                          (send actor binding.method binding.path binding.body))})))
              "list-owned-campaigns" (async (fn []
                                              (var rows (await (model.loadRows (handleFor))))
                                              (return {"result" {"output" rows "count" rows.length}
                                                       "state" rows})))
              "create-owned-campaign"
              (async (fn [input]
                       (var handle (handleFor))
                       (var created (await (model.create handle input.payload.title)))
                       (return {"result" created "state" (model.snapshot handle)})))
              "save-owned-campaign"
              (async
               (fn [input]
                 (var rows (await (model.save (handleFor) input.payload.id input.payload.title)))
                 (return {"result" (. rows (find (fn [row] (return (== row.id input.payload.id)))))
                          "state" rows})))})
        (return (await (runtime/execute -/DEFINITIONS adapters step)))
        (catch error (return {"error" {"message" error.message
                                       "status" (or error.status 400)}}))))))
  (try (var actual (await (runtime/scenario request run)))
       (:= actual.trace trace)
       (return actual)
       (catch error (throw error))
       (finally (for:array [handle (Object.values handles)] (model.close handle)))))

(defrun.js run
  (when (== process.env.DUPASUPA_PAGE_VERIFY "1")
    (var request (JSON.parse (fs.readFileSync 0 "utf8")))
    (. (-/execute request)
       (then (fn [result] (process.stdout.write (JSON.stringify result))))
       (catch (fn [error] (process.stderr.write error.message) (:= process.exitCode 1))))))
