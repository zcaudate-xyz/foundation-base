(ns melbourne.tama-overlay-test
  (:use code.test)
  (:require [clojure.string :as string]
            [lang.core :as l]
            [std.lib :as h]))

(def test-melbourne_tama_overlay_display true)

(l/script :js
  {:runtime :websocket
   :config {:id :test/tama-overlay
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.tamagui :as tm]
             [melbourne.tama-overlay :as tama-overlay]]
   :export [MODULE]})

^{:refer melbourne.tama-overlay/Dialog :added "4.1"}
(fact "keeps the Slim dialog contract on direct Tamagui primitives"
  (let [entry (l/sym-entry :js 'melbourne.tama-overlay/Dialog)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "T.Dialog") => true
    (string/includes? form "T.DialogContent") => true
    (string/includes? form "T.DialogOverlay") => true
    (contains? deps 'js.tamagui/Dialog) => true
    (not-any? #(re-find #"^(melbourne\\.ui-|js\\.react-native/)" (str %)) deps) => true))

^{:refer melbourne.tama-overlay/Popover :added "4.1"}
(fact "keeps popup content direct and dismissible"
  (let [entry (l/sym-entry :js 'melbourne.tama-overlay/Popover)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "T.Popover") => true
    (string/includes? form "T.PopoverContent") => true
    (string/includes? form "T.PopoverClose") => true
    (contains? deps 'js.tamagui/Popover) => true))

^{:id test-melbourne_tama_overlay_display}
(fact "displays the direct overlay contracts"
  ^:hidden
  (defn.js TamaOverlayDemo
    []
    (var [visible setVisible] (r/local false))
    (return
     (n/EnclosedCode
      {:label "melbourne.tama-overlay"}
      [:% tm/XStack
       {:gap "$3"
        :flexWrap "wrap"}
       [:% tm/Button
        {:size "$4"
         :onPress (fn [] (setVisible true))}
        "Open dialog"]
       [:% tama-overlay/Dialog
        {:visible visible
         :setVisible setVisible
         :title "Review changes"
         :body "This action stays in the direct Tamagui layer."
         :onSubmit (fn [] (setVisible false))}]
       [:% tama-overlay/Popover
        {:trigger [:% tm/Button
                   {:size "$4"
                    :chromeless true}
                   "Open popup"]
         :content [:% tm/YStack
                   {:gap "$2"}
                   [:% tm/Text
                    {:fontWeight "800"}
                    "Quick actions"]
                   [:% tm/Button
                    {:chromeless true}
                    "Duplicate"]
                   [:% tm/Button
                    {:chromeless true}
                    "Archive"]]}]]))))

(def.js MODULE (!:module))
