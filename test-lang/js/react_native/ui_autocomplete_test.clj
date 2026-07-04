(ns js.react-native.ui-autocomplete-test
  (:require [clojure.string]
            [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react :as r]
              [js.react.ext-view :as ext-view]
              [js.react-native :as n :include [:fn]]
              [js.react-native.physical-addon :as physical-addon]
              [js.react-native.ui-autocomplete :as ui-autocomplete]
              [js.react-native.ui-input :as ui-input]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-string :as str]]
   })

^{:refer js.react-native.ui-autocomplete/AutocompleteModal :added "4.0" :unchecked true}
(fact "creates the autocomplete modal display"

  (defn.js AutocompleteModalDemo
    []
    (var inputRef (r/ref))
    (var [visible setVisible] (r/local true))
    (var component (r/const
                    (fn [#{entry}]
                      (return
                       [:% n/Text
                        {:style {:padding 5}}
                        (xt/x:json-encode entry)]))))
    (return
     (n/EnclosedCode
{:label "js.react-native.ui-autocomplete/AutocompleteModal"}
[:% n/Isolation
       [:% n/View
        {:style {:width 300
                 :height 150}}
        [:% n/View
         [:% ui-input/Input
          {:design {:type "dark"}
           :refLink inputRef}]]]
       [:% ui-autocomplete/AutocompleteModal
        #{[:hostRef inputRef
           :styleContainer {:backgroundColor "red"}
           :entries [{:name "ABC"}
                     {:name "DEF"}
                     {:name "GHI"}]
           :visible true
           component]}]]))))

^{:refer js.react-native.ui-autocomplete/Autocomplete :added "4.0" :unchecked true}
(fact "creates the autocomplete"

  (def.js NAMES
    (@! (->> (env/sys:resource-content "js.react-native/girl-names.json")
             (std.json/read)
             (mapv clojure.string/upper-case))))

  (defn.js get-names
    [filt]
    (var output [])
    (xt/for:array [n -/NAMES]
      (when (str/starts-with? n (str/to-uppercase filt))
        (xt/x:arr-push output {:name n}))
      (when (< 15 (xt/x:len output))
        (return output)))
    (return output))

  (defn.js AutocompleteDemo
    []
    (var inputRef (r/ref))
    (var [value setValue] (r/local ""))
    (var [visible setVisible] (r/local true))
    (var view    (ext-view/makeView
                  {:handler (fn:> [filt]
                              (new Promise (fn [resolve]
                                (setTimeout (fn []
                                              (resolve (-/get-names filt)))
                                            300))))
                   :defaultOutput []}))
    (var component (r/const
                    (fn [#{entry}]
                      (return
                       [:% n/Text
                        {:style {:padding 5}}
                        (xt/x:json-encode entry)]))))
    (return
     (n/EnclosedCode
{:label "js.react-native.ui-autocomplete/Autocomplete"}
[:% n/Isolation
       [:% n/View
        {:style {:width 300
                 :height 150}}
        [:% n/View
         [:% ui-input/Input
          {:design {:type "dark"}
           :refLink inputRef
           :value value
           :onChangeText setValue
           :onFocus (fn:> (setVisible true))
           :onBlur  (fn:> (setVisible false))}]]]
       [:% ui-autocomplete/Autocomplete
        #{[visible setVisible component
           :hostRef inputRef
           :sourceView view
           :sourceInput [value]
           :styleContainer {:backgroundColor "red"}]}]])))

  )
