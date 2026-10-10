(ns lang-demos.js-007-ext-page.app.api.log.route
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]]})

(defn.js json-response
  [body status]
  (return
   (new Response
    (JSON.stringify body)
    {"status" status
     "headers" {"Content-Type" "application/json"}})))

(defn.js append-log
  [message]
  (var service-key (. process env SUPABASE_SERVICE_ROLE_KEY))
  (when (or (not service-key)
            (== "" service-key))
    (return (-/json-response
             {"ok" false
              "error" "SUPABASE_SERVICE_ROLE_KEY is not configured."}
             500)))
  (var endpoint (or (. process env SUPABASE_URL)
                    "http://127.0.0.1:55121"))
  (:= endpoint (:? (. endpoint (endsWith "/"))
                   (. endpoint (slice 0 -1))
                   endpoint))
  (return
   (. (fetch (+ endpoint "/rest/v1/rpc/log_append_public")
             {"method" "POST"
              "headers" {"apikey" service-key
                         "Authorization" (+ "Bearer " service-key)
                         "Content-Type" "application/json"
                         "Content-Profile" "scratch_v0"
                         "Accept-Profile" "scratch_v0"}
              "body" (JSON.stringify {"i_message" message})})
      (then
       (fn [response]
         (return
          (:? (. response ok)
              (-/json-response {"ok" true} 201)
              (-/json-response
               {"ok" false
                "error" "Supabase could not append the log entry."}
               502)))))
      (catch
       (fn [_]
         (return (-/json-response
                  {"ok" false
                   "error" "Could not reach the local Supabase API."}
                  502)))))))

(defn.js POST
  [request]
  (return
   (. (. request (json))
      (then
       (fn [body]
         (var message (. body message))
         (when (not (xt/x:is-string? message))
           (return (-/json-response
                    {"ok" false "error" "Message must be text."}
                    400)))
         (:= message (. message (trim)))
         (when (== 0 (. message length))
           (return (-/json-response
                    {"ok" false "error" "Enter a message first."}
                    400)))
         (when (> (. message length) 500)
           (return (-/json-response
                    {"ok" false "error" "Messages are limited to 500 characters."}
                    400)))
         (return (-/append-log message))))
      (catch
       (fn [_]
         (return (-/json-response
                  {"ok" false "error" "Request body must be valid JSON."}
                  400)))))))
