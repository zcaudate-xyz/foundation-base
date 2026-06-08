(ns xt.net.lib-supabase
  (:require [hara.lang :as l :refer [defspec.xt]]
            [lib.supabase.api :as api]))

(l/script :xtalk
  {:require [[xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]]})

(l/script :js
  {:runtime :basic
   :require [[xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [js.net.http-fetch :as js-fetch]]})

(defn.xt create-http
  [type methods host port secured basepath apikey]
  (return
   (fetch/create-base type methods
                      {:secured secured
                       :host (or host "127.0.0.1")
                       :port (or port "55121")
                       :headers {"apikey" apikey
                                 "Content-Type" "application/json"}
                       :basepath (or basepath "")})))


(defn.xt signup
  [client data opts]
  (return
   (fetch/request-http client
                       {:method "POST"
                        :path "/auth/v1/signup"
                        :body (xt/x:json-encode data)}
                       opts)))

(defn.xt signin-with-password
  [client data opts])



(comment

  (xt.lang.common-notify/wait-on :js
    (-> (js-fetch/create
         {:secured false
          :host "127.0.0.1"
          :port "55121"
          :headers {"apikey" (@! +token+)
                    "Content-Type" "application/json"}
          :basepath "/auth/v1"})
        (fetch/request-http {:method "POST"
                             :path "signup"
                             :body (xt/x:json-encode
                                    {:email "alice@example.com"
                                     :password "123456789"})})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out))))))

