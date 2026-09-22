(ns js.react-native.helper-browser-test
  (:require [clojure.string :as string]
            [lang.core :as l])
  (:use code.test))

(l/script :js
  {:require [[xt.event.base-route :as event-route]
             [js.react-native :as n :include [:fn]]
             [js.react.ext-route :as ext-route]
             [js.react-native.helper-browser :as helper-browser]]})

^{:refer js.react-native.helper-browser/getHash :added "4.0" :unchecked true}
(fact "gets the window location hash")

^{:refer js.react-native.helper-browser/getHashRoute :added "4.0"}
(fact "gets a route from slash-prefixed and bare browser hashes"
  (let [form (pr-str (:form (l/sym-entry :js 'js.react-native.helper-browser/getHashRoute)))]
    (string/includes? form "starts-with?") => true
    (string/includes? form "substring hash 2") => true
    (string/includes? form "substring hash 1") => true))

^{:refer js.react-native.helper-browser/useHashRoute :added "4.0"}
(fact "listens to hash changes and removes both browser listeners"
  (let [form (pr-str (:form (l/sym-entry :js 'js.react-native.helper-browser/useHashRoute)))]
    (string/includes? form "hashchange") => true
    (string/includes? form "popstate") => true
    (= 2 (count (re-seq #"removeEventListener" form))) => true)

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
(fact "sets the hash param")
