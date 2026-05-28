(ns lib.supabase.rpc-test
  (:use code.test)
  (:require [lib.supabase.rpc :refer :all]
            [lib.supabase.common :as common]))

(defn sample-client
  []
  (common/create-client "http://localhost:54321" "key-123" {}))

^{:refer lib.supabase.rpc/fn-meta :added "4.1"}
(fact "extracts rpc function metadata"
  (fn-meta (atom {:id 'echo_name :static/schema "scratch"}))
  => {:id 'echo_name :schema "scratch"})

^{:refer lib.supabase.rpc/api-rpc :added "4.1"}
(fact "routes rpc calls through api-call"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (api-rpc {:fn (atom {:id 'echo_name :static/schema "scratch"})
              :args {"input" "hello"}
              :key "key"}))
  => #(let [[opts body] %]
        (and (= {"input" "hello"} body)
             (= "key" (:key opts))
             (= :post (:method opts))
             (= {"Content-Profile" "scratch"} (:headers opts))
             (= "/rpc/echo_name" (:route opts)))))

^{:refer lib.supabase.rpc/rpc :added "4.1"}
(fact "wraps rpc into api-rpc with a client"
  (with-redefs [api-rpc identity]
    (rpc (sample-client) 'echo_name {"input" "hello"} {:count :exact}))
  => #(and (map? %)
           (= 'echo_name (:fn %))
           (= {"input" "hello"} (:args %))
           (= :exact (:count %))
           (map? (:client %))))
