(ns js.react-native.ext-route-test
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
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react.ext-route :as ext-route]
             [xt.lang.event-route :as event-route]]
   })

^{:refer js.react.ext-route/useRouteSegment :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"
  ^:hidden
  
  (defn.js UseRouteSegmentDemo
    []
    (var route (ext-route/makeRoute "account/user"))
    (var url  (ext-route/listenRouteUrl route))
    (var tree (ext-route/listenRouteTree route))
    (var [value setValue] (ext-route/useRouteSegment route ["account"]))
    (var getCount (r/useGetCount))
    (return
     (n/EnclosedCode 
      {:label "js.react.ext-route/useRouteSegment"} 
      [:% n/Row
       [:% n/TextInput
        {:value value
         :onChangeText setValue}]
       [:% n/Button
        {:title "User"
         :onPress (fn:> (setValue "user"))
         }]
       [:% n/Text " "]
       [:% n/Button
        {:title "Settings"
         :onPress (fn:> (setValue "settings"))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "GUEST"
         :onPress (fn:> (event-route/set-path
                         route ["guest"]))
         }]
       [:% n/Text " "]
       [:% n/Button
        {:title "ACCOUNT"
         :onPress (fn:> (event-route/set-path
                         route ["account"]))}]
       [:% n/Text " "]
       ] 
      [:% n/TextDisplay
       {:content (n/format-entry {:url url
                                  :tree tree
                                  :value value
                                  :count (getCount)})}])))

  )
