(ns xt.lang.util-http-test
  (:require [net.http :as nhttp]
            [org.httpkit.server :as server]
  	        [rt.nginx :as nginx]
            [std.lib.os :as os]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.util-http :as http]
             [xt.lang.common-repl :as repl]]})

(l/script+ [:es :lua]
  {:runtime :nginx.instance
   :require [[lua.nginx.websocket :as ws]
             [lua.nginx :as n]]})

(def CANARY-NGINX
  (not= "Mac OS X" (os/os)))

(fact:global
 {:setup    [(l/rt:restart)
             (!.js
              (:= (!:G fetch) (require "node-fetch"))
              (:= (!:G EventSource) (require "eventsource")))
             (when CANARY-NGINX
               (l/annex:restart-all)
               (l/! [:es]
                 (do:> (ws/service-register "ES_DEBUG" {} nil)
                       (:= (. DEBUG ["es_handler"])
                           (fn []
                             (ws/es-test-loop "ES_DEBUG"
                                              100
                                              5
                                              (fn [n]
                                                (return (cat "TEST-" n))))))))
               (let [url (str "http://localhost:" (:port (l/annex:get :es)) "/eval/es")
                     ok? (loop [attempt 0]
                           (let [status (try (:status (nhttp/get url {:headers {"Accept" "text/event-stream"}}))
                                             (catch Exception _ nil))]
                             (cond (= 200 status)
                                   true

                                   (< attempt 19)
                                   (do (Thread/sleep 100)
                                       (recur (inc attempt)))

                                   :else
                                   false)))]
                 (when-not ok?
                   (throw (ex-info "Event source did not become ready." {:url url})))))]
   :teardown [(l/rt:stop)
              (when CANARY-NGINX
                (l/annex:stop-all))]})

^{:refer xt.lang.util-http/CANARY :adopt true :added "4.0"}
(fact "tests that scaffold is working"
  true
  => true)

^{:refer xt.lang.util-http/fetch-call :added "4.0"}
(fact "completes a http call with options"
  ;;^:hidden

  #_#_#_
  (notify/wait-on :js
    (-> (http/fetch-call (+ "http://localhost:"
                            (@! (:http-port (l/default-notify))))
                         {:as "text"})
        (. (then (repl/>notify)))))
  => "OK")

^{:refer xt.lang.util-http/es-connect :added "4.0"}
(fact "connects to an event source"
  
  (when CANARY-NGINX
    (notify/wait-on :js
      (var es (http/es-connect
               (@! (str "http://localhost:" (:port (l/annex:get :es))
                         "/eval/es"))
               {:on-message (fn [msg]
                              (repl/notify msg.data)
                              (es.close))}))))
  => (if CANARY-NGINX "TEST-5" nil))

^{:refer xt.lang.util-http/es-active? :added "4.0"}
(fact "checks if event source is active")

^{:refer xt.lang.util-http/es-close :added "4.0"}
(fact "closes the event source")

^{:refer xt.lang.util-http/ws-connect :added "4.0"}
(fact "connects to a websocket source")

^{:refer xt.lang.util-http/ws-active? :added "4.0"}
(fact "checks if websocket is active")

^{:refer xt.lang.util-http/ws-close :added "4.0"}
(fact "closes the websocket")

^{:refer xt.lang.util-http/ws-send :added "4.0"}
(fact "sends text through websocket")


(comment

  
  (l/with:input
      (!.js
       (:= EventSource (require "react-native-sse"))
       (var es (new EventSource
                    (@! (str "http://localhost:" (:port (l/rt:inner :lua))
                             "/eval/es"))))
       (es.addEventListener "message" console.log)
       (es.addEventListener "open" console.log)))

  (l/with:input
      (!.js
       (:= EventSource (require "eventsource"))
       (var es (new EventSource
                    (@! (str "http://localhost:" (:port (l/rt:inner :lua))
                             "/eval/es"))))
       (es.addEventListener "message" console.log)
       (es.addEventListener "open" console.log)
       (es.addEventListener "close" (fn []
                                      (es.close))))))
