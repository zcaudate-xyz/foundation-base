(ns melbourne.slim-style-test
  (:use code.test)
  (:require [clojure.string :as string]
            [lang.core :as l]
            [std.lib :as h]))

(def test-melbourne_slim_style_display true)

(l/script :js
  {:runtime :websocket
   :config {:id :test/slim-style
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n :include [:fn]]
             [melbourne.base-font :as base-font]
             [melbourne.slim-common :as slim-common]]
   :export [MODULE]})

^{:refer melbourne.slim-common/FormEnclosed :added "4.1"}
(fact "accepts styleContainer without changing its field contract"
  (let [entry (l/sym-entry :js 'melbourne.slim-common/FormEnclosed)
        form (pr-str (:form entry))]
    (string/includes? form "styleContainer") => true
    (string/includes? form "arrayify") => true
    (string/includes? form "flexWrap") => true))

^{:id test-melbourne_slim_style_display}
(fact "displays the Slim typography and enclosure styling knobs"
  ^:hidden
  (defn.js SlimStyleLabDemo
    []
    (return
     (n/EnclosedCode
      {:label "melbourne.slim-style"}
      [:% n/View
       {:style {:width "100%"
                :gap 12}}
       [:% n/Text
        {:style [base-font/fontH2
                 {:letterSpacing 0.3}]}
        "SLIM STYLE LAB"]
       [:% n/Text
        {:style [base-font/fontP
                 {:color "#64748b"}]}
        "System font by default; every enclosure can opt into its own label and container treatment."]
       [:% n/Row
        {:style {:gap 12
                 :flexWrap "wrap"
                 :width "100%"}}
        [:% n/View
         {:style {:flex 1
                  :minWidth 250
                  :padding 12
                  :backgroundColor "#f8fafc"}}
         [:% slim-common/FormEnclosed
          {:design {:type "light"}
           :label "LIGHT"
           :styleLabel {:letterSpacing 1.1
                        :fontWeight "800"}
           :styleContainer {:backgroundColor "#ffffff"
                            :borderColor "#e2e8f0"
                            :borderWidth 1
                            :borderRadius 10
                            :padding 12}}
          [:% n/Text
           {:style [base-font/fontP
                    {:color "#0f172a"}]}
           "A filled light surface"]]]
        [:% n/View
         {:style {:flex 1
                  :minWidth 250
                  :padding 12
                  :backgroundColor "#0f172a"}}
         [:% slim-common/FormEnclosed
          {:design {:type "dark"}
           :label "DARK"
           :styleLabel {:letterSpacing 1.1
                        :fontWeight "800"}
           :styleContainer {:backgroundColor "#1e293b"
                            :borderColor "#475569"
                            :borderWidth 1
                            :borderRadius 10
                            :padding 12}}
          [:% n/Text
           {:style [base-font/fontP
                    {:color "#f8fafc"}]}
           "A filled dark surface"]]]]
       [:% n/Text
        {:style [base-font/fontCaption
                 {:color "#64748b"}]}
        "Try changing styleContainer, styleLabel, or the base font styles in this test."]]))))

(def.js MODULE (!:module))
