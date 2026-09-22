(ns pune.ui-searchbar-test
  (:use code.test)
  (:require [clojure.string :as string]
            [lang.core :as l]
            [std.lib :as h]))

(def test-pune_ui_searchbar_display true)

(l/script :js
  {:runtime :websocket
   :config {:id :dev/pune-searchbar
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [pune.common.style :as common-style]
             [pune.ui-searchbar :as searchbar]]
   :export [MODULE]})

^{:refer pune.common.style/tokens :added "4.1"}
(fact "selects compact Pune light and dark tokens"
  (let [entry (l/sym-entry :js 'pune.common.style/tokens)
        form (pr-str (:form entry))]
    (string/includes? form "Light") => true
    (string/includes? form "Dark") => true
    (string/includes? form "design") => true))

^{:refer pune.common.style/controlStyle :added "4.1"}
(fact "turns the tokens into a control style"
  (let [entry (l/sym-entry :js 'pune.common.style/controlStyle)
        form (pr-str (:form entry))]
    (string/includes? form "backgroundColor") => true
    (string/includes? form "borderColor") => true
    (string/includes? form "color") => true))

^{:refer pune.ui-searchbar/SearchBar :added "4.1"}
(fact "keeps searchbar on direct React Native primitives"
  (let [entry (l/sym-entry :js 'pune.ui-searchbar/SearchBar)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "ReactNative.TextInput") => true
    (string/includes? form "pune.common.style/controlStyle") => true
    (contains? deps 'js.react-native/TextInput) => true
    (not-any? #(re-find #"^(melbourne\\.ui-|js\\.tamagui/)" (str %)) deps) => true))

^{:id test-pune_ui_searchbar_display}
(fact "displays light and dark search controls with an overridable style"
  ^:hidden
  (defn.js SearchBarDemo
    []
    (var [query setQuery] (r/local ""))
    (return
     (n/EnclosedCode
      {:label "pune.ui-searchbar/SearchBar"}
      [:% n/View
       {:style {:gap 12
                :width "100%"
                :maxWidth 640}}
       [:% n/Text
        {:style {:fontWeight "800"
                 :fontSize 13}}
        "PUNE SEARCH"]
       [:% n/View
        {:style {:padding 12
                 :backgroundColor "#f8fafc"}}
        [:% searchbar/SearchBar
         {:design {:type "light"}
          :value query
          :setValue setQuery
          :placeholder "Search markets"}]]
       [:% n/View
        {:style {:padding 12
                 :backgroundColor "#0f172a"}}
        [:% searchbar/SearchBar
         {:design {:type "dark"}
          :value query
          :setValue setQuery
          :placeholder "Search dark surface"}]]
       [:% n/Text
        {:style {:fontSize 12
                 :color "#64748b"}}
        "Query: " query]]))))

(def.js MODULE (!:module))
