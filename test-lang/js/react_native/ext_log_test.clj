(ns js.react-native.ext-log-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react :as r :include [:fn]]
              [js.react-native :as n :include [:fn]]
              [js.react.ext-log :as ext-log]
              [xt.event.base-log :as event-log]]
    })

^{:refer js.react.ext-log/listenLogLatest :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"

  (defn.js ListenLogLatestDemo
    []
    (var log    (ext-log/makeLog {}))
    (var latest (ext-log/listenLogLatest log))
    (var queueEntry (fn:> []
                      (event-log/queue-entry
                       log
                       {:id (. (Math.random) (toString 36) (substr 2 6))}
                       (fn:> [entry t] (. entry id))
                       (fn:> [entry] entry))))
    (r/init []
      (queueEntry))
    (return
     (n/EnclosedCode
{:label "js.react.ext-log/listenLogLatest"}
[:% n/Row
       [:% n/Button
        {:title "QUEUE"
         :onPress queueEntry}]]
[:% n/TextDisplay
        {:content (n/format-entry {:latest latest
                                   :count (event-log/get-count log)
                                   :tail (event-log/get-tail log 5)})}])))


  )
