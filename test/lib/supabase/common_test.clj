(ns lib.supabase.common-test
  (:use code.test)
  (:require [lib.supabase.common :refer :all]
            [net.http :as http]))

(defn sample-client
  []
  (create-client "http://localhost:54321"
                 "key-123"
                 {:headers {"X-Test" "1"}
                  :schema_name "scratch"}))

^{:refer lib.supabase.common/client? :added "4.1"}
(fact "checks for a lib.supabase client"
  [(client? (sample-client))
   (client? {})]
  => [true false])

^{:refer lib.supabase.common/create-client :added "4.1"}
(fact "creates a client with state, headers, and schema defaults"
  (let [client (create-client "http://localhost:54321" "key-123" {})]
    [(:base_url client)
     (:api_key client)
     (:schema_name client)
     (:headers client)
     (map? @(state-atom client))])
  => ["http://localhost:54321" "key-123" "public" {} true])

^{:refer lib.supabase.common/state-atom :added "4.1"}
(fact "returns the client state atom"
  (state-atom (sample-client))
  => clojure.lang.IDeref)

^{:refer lib.supabase.common/raw-state :added "4.1"}
(fact "returns the dereferenced client state"
  (raw-state (sample-client))
  => #(and (map? %)
           (nil? (:session %))
           (contains? % :ref_counter)))

^{:refer lib.supabase.common/swap-state! :added "4.1"}
(fact "updates the client state atom"
  (let [client (sample-client)]
    (swap-state! client assoc :session {"access_token" "abc"})
    (:session (raw-state client)))
  => {"access_token" "abc"})

^{:refer lib.supabase.common/decode-body :added "4.1"}
(fact "decodes JSON strings and preserves non-strings"
  [(decode-body "{\"ok\":true}")
   (decode-body "")
   (decode-body {"ok" true})]
  => [{"ok" true} nil {"ok" true}])

^{:refer lib.supabase.common/join-url :added "4.1"}
(fact "joins urls without duplicating slashes"
  [(join-url "http://a" "/b")
   (join-url "http://a/" "/b")
   (join-url "http://a" "b")]
  => ["http://a/b" "http://a/b" "http://a/b"])

^{:refer lib.supabase.common/auth-url :added "4.1"}
(fact "builds auth urls"
  (auth-url (sample-client) "/signup")
  => "http://localhost:54321/auth/v1/signup")

^{:refer lib.supabase.common/rest-url :added "4.1"}
(fact "builds rest urls"
  (rest-url (sample-client) "/Entry")
  => "http://localhost:54321/rest/v1/Entry")

^{:refer lib.supabase.common/admin-url :added "4.1"}
(fact "builds admin urls"
  (admin-url (sample-client) "/users")
  => "http://localhost:54321/auth/v1/admin/users")

^{:refer lib.supabase.common/resolve-host :added "4.1"}
(fact "resolves the host from opts before the client"
  (resolve-host (sample-client) {:host "http://override"})
  => "http://override")

^{:refer lib.supabase.common/resolve-key :added "4.1"}
(fact "resolves the API key from opts before the client"
  [(resolve-key (sample-client) {:key "override"})
   (resolve-key (sample-client) {})]
  => ["override" "key-123"])

^{:refer lib.supabase.common/resolve-auth :added "4.1"}
(fact "resolves auth from opts or stored state"
  (let [client (sample-client)]
    (swap-state! client assoc :auth_token "token-1")
    [(resolve-auth client {:auth "token-2"} "key-123")
     (resolve-auth client {} "key-123")])
  => ["token-2" "token-1"])

^{:refer lib.supabase.common/request-fn :added "4.1"}
(fact "returns the net.http request function for a method"
  [(fn? (request-fn :get))
   (fn? (request-fn :post))]
  => [true true])

^{:refer lib.supabase.common/response-error :added "4.1"}
(fact "throws on error statuses"
  ^:hidden
  (response-error {:route "/bad"} {:status 401 :body {"message" "bad"}})
  => (throws clojure.lang.ExceptionInfo "Supabase API request failed"))

^{:refer lib.supabase.common/api-call :added "4.1"}
(fact "calls a Supabase endpoint and decodes the response"
  (let [seen (atom nil)]
    (with-redefs [http/post (fn [url opts]
                              (reset! seen [url opts])
                              {:status 200
                               :body "{\"ok\":true}"})]
      [(api-call {:client (sample-client)
                  :route "/auth/v1/signup"}
                 {"email" "a@a.com"})
       @seen]))
  => #(let [[response [url opts]] %]
        (and (= {:status 200 :body {"ok" true}} response)
             (= "http://localhost:54321/auth/v1/signup" url)
             (= "key-123" (get-in opts [:headers "apikey"]))
             (= "{\"email\":\"a@a.com\"}" (:body opts)))))

^{:refer lib.supabase.common/next-ref! :added "4.1"}
(fact "increments the websocket reference counter"
  (let [client (sample-client)]
    [(next-ref! client)
     (next-ref! client)])
  => [1 2])
