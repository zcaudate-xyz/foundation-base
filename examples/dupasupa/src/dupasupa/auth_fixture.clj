(ns dupasupa.auth-fixture
  "Owns only the temporary local Supabase Auth users created for this run."
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import [java.net URI]
           [java.net.http HttpClient HttpRequest HttpRequest$BodyPublishers
            HttpResponse$BodyHandlers]))

(defn config
  []
  (let [api (:api (edn/read-string (slurp (io/file "../../config/scaffold/supabase-local.edn"))))]
    {:url (str (:protocol api) "://" (:hostname api) ":" (:port api))
     :anon-key (:anon-key api)
     :service-key (:service-key api)}))
(defn fixture [] {:config (config) :accounts (atom [])})
(defn request!
  [fixture method path body key token]
  (let [config (:config fixture)
        uri (URI. (:url config))]
    (when-not (#{"127.0.0.1" "localhost" "[::1]"} (.getHost uri))
      (throw (ex-info "Auth fixtures require local Supabase" {})))
    (let [builder (doto (HttpRequest/newBuilder (URI. (str (:url config) "/auth/v1" path)))
                    (.timeout (java.time.Duration/ofSeconds 15))
                    (.header "apikey" key)
                    (.header "Authorization" (str "Bearer " token))
                    (.header "content-type" "application/json"))
          response (.send (HttpClient/newHttpClient)
                          (.build (.method builder
                                           method
                                           (if body
                                             (HttpRequest$BodyPublishers/ofString (json/write-str
                                                                                   body))
                                             (HttpRequest$BodyPublishers/noBody))))
                          (HttpResponse$BodyHandlers/ofString))]
      (when-not (<= 200 (.statusCode response) 299)
        (throw (ex-info "Local Supabase Auth request failed"
                        {:status (.statusCode response) :path path})))
      (when (seq (.body response))
        (json/read-str (.body response) :key-fn keyword)))))
(defn start!
  [fixture]
  (let [key (get-in fixture [:config :service-key])
        tag (str (java.util.UUID/randomUUID))]
    (doseq [handle ["alice" "bob"]]
      (let [email (str "dupasupa-" tag "-" handle "@example.test")
            password (str "Dupasupa-" (java.util.UUID/randomUUID))
            user (request! fixture
                           "POST"
                           "/admin/users"
                           {:email email :password password :email_confirm true}
                           key
                           key)
            account {:id (:id user) :email email :password password :handle handle}]
        (when-not (:id account) (throw (ex-info "Auth did not create a fixture user" {})))
        (swap! (:accounts fixture) conj account)
        (let [session (request! fixture
                                "POST"
                                "/token?grant_type=password"
                                {:email email :password password}
                                key
                                key)]
          (swap! (:accounts fixture) update
            (dec (count @(:accounts fixture)))
            assoc
            :token
            (:access_token session)))))
    true))
(defn stop!
  [fixture]
  (let [key (get-in fixture [:config :service-key])
        errors (atom [])]
    (doseq [account (reverse @(:accounts fixture))]
      (when-let [token (:token account)]
        (try (request! fixture "POST" "/logout?scope=global" nil key token)
             (catch Throwable _ nil)))
      (try (request! fixture "DELETE" (str "/admin/users/" (:id account)) nil key key)
           (catch Throwable error (swap! errors conj error))))
    (reset! (:accounts fixture) [])
    (when-let [error (first @errors)] (throw error))
    true))
(defn public-accounts
  [fixture]
  (mapv #(select-keys % [:id :email :password :handle]) @(:accounts fixture)))
