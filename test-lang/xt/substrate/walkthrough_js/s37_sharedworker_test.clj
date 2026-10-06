^{:seedgen/skip true}
(ns xt.substrate.walkthrough-js.s37-sharedworker-test
  (:use code.test)
  (:require [lang.core :as l]
            [lang.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [statsbuild.testing.supabase.gw-min :as gw-min]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as event-node]
             [xt.db.node.runtime :as runtime]
             [xt.db.node.client-supabase :as client-supabase]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(def +gw-min-supabase-anon+
  (let [{:keys [protocol hostname port anon-key]}
        (:api @gw-min/+gw-min-config+)]
    {:host hostname
     :port port
     :secured (= "https" protocol)
     :basepath ""
     :apikey anon-key}))

(defn.js worker-config
  []
  (return
   {"primary" {"id" "db/primary"
               "type" "supabase"
               "defaults" (@! +gw-min-supabase-anon+)}
    "caching" {"id" "db/caching"
               "type" "memory"
               "defaults" {}}}))

(defn.js create-client-node
  []
  
  (return
   (runtime/sharedworker-connect-state
    client
    (-/worker-config)
    nil
    nil
    nil
    nil)))

(defn.js connect-auth-worker
  [client]
  (return
   (runtime/sharedworker-connect-state
    client
    (-/worker-config)
    nil
    nil
    nil
    nil)))

(fact:global
 {:setup [(gw-min/start-gw-min)
          (l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:"
                                  (:http-port (l/default-notify))
                                  "/")
                             4000)]
  :teardown [(l/rt:stop)
             (gw-min/stop-gw-min)]})



^{:refer xt.substrate.walkthrough-js.s37-sharedworker-test/supabase-sharedworker-sign-up-and-sign-in
  :added "4.1"
  :setup [(def +email+ (str "s37-auth-" (java.util.UUID/randomUUID) "@example.com"))]}
(fact "client signs up and signs in through the Supabase kernel on a SharedWorker"


  (notify/wait-on :js
    (var client (event-node/node-create {"id" "s37-auth-client"}))
    (. (runtime/sharedworker-connect-state client (-/worker-config))
       (then (fn [client]
               (repl/notify
                (promise/x:promise-all
                 [(client-supabase/signed-in? client "db/primary" {})
                  (client-supabase/user-info client "db/primary" {})]))))))


  (notify/wait-on :js
    (var client (event-node/node-create {"id" "s37-auth-client"}))
    (runtime/sharedworker-connect-state client (-/worker-config))
    (client-supabase/signed-in? client "db/primary" {}))
  
  
  (notify/wait-on :js
    (var client (event-node/node-create {"id" "s37-auth-client"}))
    (runtime/sharedworker-connect-state client (-/worker-config))
    (repl/notify (client-supabase/signed-in? client "db/primary" {})))
  
  (notify/wait-on :js
    (var client (event-node/node-create {"id" "s37-auth-client"}))
    (-> (runtime/sharedworker-connect-state client (-/worker-config))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (promise/x:promise-all
             [(client-supabase/signed-in? client "db/primary" {})
              #_#_(client-supabase/user-get client "db/primary" {})
              (client-supabase/user-info client "db/primary" {})])
            ))))
    )

  (notify/wait-on :js
    (var client (event-node/node-create {"id" "s37-auth-client"}))
    (-> (runtime/sharedworker-connect-state client (-/worker-config))
        (promise/x:promise-then
         (fn [_]
           (return
            (client-supabase/signed-in? client "db/primary" {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (client-supabase/sign-up client
                                     "db/primary"
                                     {"email" (@! +email+)
                                      "password" "pass123456"}))))
        (promise/x:promise-catch (fn []))
        (promise/x:promise-then
         (fn [_]
           (return
            (client-supabase/sign-in client
                                     "db/primary"
                                     {"email" (@! +email+)
                                      "password" "pass123456"}))))
        (promise/x:promise-then (repl/>notify))))
  
  
  (notify/wait-on :js
    (var client (event-node/node-create {"id" "s37-auth-client"}))
    (-> (runtime/sharedworker-connect-state client (-/worker-config))
        (promise/x:promise-then
         (fn [client]
           (return
            (client-supabase/signed-in? client "db/primary" {})))))
    )

  (notify/wait-on :js
    (promise/x:promise-all ))
  


  (notify/wait-on :js
    (repl/notify (-/run-auth-workflow (@! +email+))))
  => {"sign-up-email" +email+
      "sign-in-email" +email+
      "signed-in" true})




(comment
  
  
  (notify/wait-on :js
    (:= (!:G CLIENT) (event-node/node-create {"id" "s37-auth-client"}))
    (-/connect-auth-worker (!:G CLIENT))
    (repl/notify (!:G CLIENT)))
  
  (!.js
    (!:G CLIENT))
  
  (notify/wait-on :js
    (repl/notify
     (client-supabase/signed-in? (!:G CLIENT) "db/primary" {})))

  (notify/wait-on :js
    (repl/notify
     (client-supabase/user-info (!:G CLIENT) "db/primary" {})))
  
  (notify/wait-on :js
    (repl/notify
     (client-supabase/sign-up (!:G CLIENT) "db/primary"
                              {"email" "abc@abc.com"
                               "password" "pass123456"}
                              {})))
  
  (notify/wait-on :js
    (repl/notify
     (client-supabase/sign-in (!:G CLIENT) "db/primary"
                              {"email" "abc@abc.com"
                               "password" "pass123456"}
                              {})))
  
  (notify/wait-on :js
    (repl/notify
     (client-supabase/sign-out (!:G CLIENT) "db/primary"
                               )))
  
  
  (notify/wait-on :js
    (repl/notify
     (client-supabase/current-session (!:G CLIENT) "db/primary"
                                      )))
  
  
  
  )

(defn.js finish-auth-workflow
  [client state sign-up-session sign-in-session]
  (return
   (-> (client-supabase/signed-in? client "db/primary" {})
       (promise/x:promise-then
        (fn [signed-in]
          (var result
               {"sign-up-email" (xtd/get-in sign-up-session ["user" "email"])
                "sign-in-email" (xtd/get-in sign-in-session ["user" "email"])
                "signed-in" signed-in})
          (return
           (-> (client-supabase/sign-out client "db/primary" {})
               (promise/x:promise-then
                (fn [_]
                  (runtime/sharedworker-disconnect state)
                  (return result))))))))))

(defn.js run-auth-workflow
  [email]
  (var client (event-node/node-create {"id" "s37-auth-client"}))
  (var credentials {"email" email
                    "password" "pass123456"})
  (return
   (-> (-/connect-auth-worker client)
       (promise/x:promise-then
        (fn [state]
          (return
           (-> (client-supabase/sign-up client "db/primary" credentials {})
               (promise/x:promise-then
                (fn [sign-up-session]
                  (return
                   (-> (client-supabase/sign-in client "db/primary" credentials {})
                       (promise/x:promise-then
                        (fn [sign-in-session]
                          (return
                           (-/finish-auth-workflow client
                                                   state
                                                   sign-up-session
                                                   sign-in-session))))))))))))
       (promise/x:promise-catch
        (fn [err]
          (return {"error" (xt/x:ex-message err)}))))))

^{:refer xt.substrate.walkthrough-js.s37-sharedworker-test/supabase-sharedworker-sign-up-and-sign-in
  :added "4.1"}
(fact "client signs up and signs in through the Supabase kernel on a SharedWorker"
  
  (def +email+ (str "s37-auth-" (java.util.UUID/randomUUID) "@example.com"))
  (notify/wait-on :js
    (repl/notify (-/run-auth-workflow (@! +email+))))
  => {"sign-up-email" +email+
      "sign-in-email" +email+
      "signed-in" true})
