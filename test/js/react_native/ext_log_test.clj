(ns js.react-native.ext-log-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]
            [js.cell.playground :as browser]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react.ext-log :as ext-log]
             [xt.lang.event-log :as event-log]
             [xt.lang.base-lib :as k]
             ]
   })

^{:refer js.react.ext-log/listenLogLatest :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"
  ^:hidden
  
  (defn.js ListenLogLatestDemo
    []
    (var log    (ext-log/makeLog {}))
    (var latest (ext-log/listenLogLatest log))
    (r/init []
      (event-log/queue-entry
       log
       {:id (j/randomId 6)}
       k/id-fn
       k/identity))
    (return
     (n/EnclosedCode 
{:label "js.react.ext-log/listenLogLatest"} 
[:% n/Row
       [:% n/Button
        {:title "QUEUE"
         :onPress (fn:> (event-log/queue-entry
                         log
                         {:id (j/randomId 6)}
                         k/id-fn
                         k/identity))}]] 
[:% n/TextDisplay
       {:content (n/format-entry {:latest latest
                                  :count (event-log/get-count log)
                                  :tail (event-log/get-tail log 5)})}])))
  
  
  )
