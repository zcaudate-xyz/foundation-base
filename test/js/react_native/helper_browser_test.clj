(ns js.react-native.helper-browser-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.event-route :as event-route]
             [js.react-native :as n :include [:fn]]
             [js.react.ext-route :as ext-route]
             [js.react-native.helper-browser :as helper-browser]]
   })

^{:refer js.react-native.helper-browser/getHash :added "4.0" :unchecked true}
(fact "gets the window location hash")

^{:refer js.react-native.helper-browser/getHashRoute :added "4.0" :unchecked true}
(fact "gets the browser hash route")

^{:refer js.react-native.helper-browser/useHashRoute :added "4.0" :unchecked true}
(fact "listens to the browser hash route"
  ^:hidden
  
  (defn.js UseHashRouteDemo
    []
    (var route (ext-route/makeRoute "hello"))
    (var url (ext-route/listenRouteUrl route))
    (helper-browser/useHashRoute route)
    (return
     (n/EnclosedCode 
{:label "js.react-native.helper-browser/useHashRoute"} 
[:% n/Row
       [:% n/Button
        {:title   "A"
         :onPress (fn:> (event-route/set-url route "hello/a"))}]
       [:% n/Text " "]
       [:% n/Button
        {:title   "B"
         :onPress (fn:> (event-route/set-url route "hello/b"))}]
       [:% n/Padding {:style {:flex 1}}]
       [:% n/Text (+ "route: " url)]]))))

^{:refer js.react-native.helper-browser/setHashParam :added "4.0" :unchecked true}
(fact "sets the hash param"
  ^:hidden
  
  )
