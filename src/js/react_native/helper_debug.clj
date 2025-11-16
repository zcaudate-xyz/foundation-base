(ns js.react-native.helper-debug
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [js.react :as r]
             [js.react-native :as n]
             [xt.lang.base-client :as client]]})

(defn.js create-client
  "creates the debug client ws"
  {:added "4.0"}
  [host port path]
  (return
   (client/client-ws (or host window.location.hostname)
                     (or port window.location.port)
                     {:secured (== window.location.protocol
                                   "https:")
                      :path (or path "dev/ws")})))

(defn.js DebugClient
  "creates the debug client ui"
  {:added "4.0"}
  [props]
  (var #{globalField
         host
         port
         path} props)
  (var [client setClient] (r/local))
  (var connFn
       (fn []
         (var conn (-/create-client host port path))
         (. conn
            (addEventListener
             "close"
             (fn:> (setClient nil))))
         (when globalField
           (:= (. !:G [globalField]) conn))
         (setClient conn)))
  (r/init [] (connFn))
  (return (:? (not client)
              [:% n/View
               {:style {:position "absolute"
                        :top 0
                        :left 0
                        :zIndex 10000}}
               [:% n/Button
                {:title "CONNECT"
                 :onPress connFn}]])))

