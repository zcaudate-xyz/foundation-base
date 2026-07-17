(ns dart.net.http-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]
            [scaffold.supabase.local-min :as local-min]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :test-mode true
   :require [[dart.net.http-fetch :as dart-fetch]
             [xt.net.http-fetch :as fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(local-min/start-supabase)
          (l/rt:restart)]
  :teardown [(l/rt:stop)
             (local-min/stop-supabase nil)]})

^{:refer dart.net.http-fetch/request-http-raw :added "4.1"}
(fact "performs and closes a native Dart HTTP request"
  (notify/wait-on [:dart 15000]
    (-> (dart-fetch/create
         {:headers {"apikey" (@! (-> local-min/+config+ :api :anon-key))}
          :host (@! (-> local-min/+config+ :api :hostname))
          :port (@! (-> local-min/+config+ :api :port))}
         nil)
        (fetch/request-http {"path" "/auth/v1/health"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (xt/x:get-key out "status"))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" (xt/x:ex-message err)})))))
  => 200)
