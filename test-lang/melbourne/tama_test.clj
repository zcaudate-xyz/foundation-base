(ns melbourne.tama-test
  (:use code.test)
  (:require [clojure.string :as string]
            [lang.core :as l]
            [lang.core.impl :as impl]
            [melbourne.tama :as tama-source]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/tama-web
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.tamagui :as tm]
             [melbourne.tama :as tama]]
   :export [MODULE]})

(defrun.js __import__
  (:- :import (quote [React]) :from "'react'"))

^{:refer melbourne.tama/entryValue :added "4.1"}
(fact "resolves templated values without the legacy renderer"
  (let [entry (l/sym-entry :js 'melbourne.tama/entryValue)
        form  (pr-str (:form entry))]
    (string/includes? form "xt.lang.common-data/template-entry") => true
    (string/includes? form "x:json-encode") => true
    (not (string/includes? form "melbourne.ui-")) => true))

^{:refer melbourne.tama/entryChildren :added "4.1"}
(fact "maps array and object bodies through the supplied renderer"
  (let [entry (l/sym-entry :js 'melbourne.tama/entryChildren)
        deps  (:deps-fragment entry)]
    deps => #{'js.core.impl/map
              'js.react/isValidElement
              'xt.lang.spec-base/x:obj-keys}))

^{:refer melbourne.tama/renderEntry :added "4.1"}
(fact "renders the shared entry types with direct Tamagui primitives"
  (let [entry (l/sym-entry :js 'melbourne.tama/renderEntry)
        form  (pr-str (:form entry))
        emitted (impl/emit-str (:form entry) {:lang :js})
        deps  (:deps-fragment entry)]
    (every? #(contains? deps %)
            ['js.tamagui/XStack
             'js.tamagui/YStack
             'js.tamagui/Card
             'js.tamagui/Text
             'js.tamagui/H6
             'js.tamagui/Paragraph
             'js.tamagui/Button
             'js.tamagui/Input
             'js.tamagui/Separator
             'js.tamagui/Spacer]) => true
    (not-any? #(re-find #"^(melbourne\\.ui-|js\\.react-native/)" (str %))
              deps) => true
    (string/includes? form "T.Card") => true
    (string/includes? form "T.YStack") => true
    (string/includes? form "T.Button") => true
    (string/includes? form "T.Input") => true
    (string/includes? emitted "<T.Card") => true
    (string/includes? emitted "...Object.assign") => true
    (not (string/includes? emitted "<T.Card>{Object.assign")) => true))

^{:refer melbourne.tama/Entry :added "4.1"}
(fact "keeps Entry as a thin renderer seam"
  (pr-str (:form (l/sym-entry :js 'melbourne.tama/Entry)))
  => "(defn Entry [props] (return (renderEntry props (. props impl))))"
  ^:hidden
  (defn.js EntryDemo
    []
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/Entry"}
      [:% tm/YStack
       {:gap "$3"
        :maxWidth 640}
       [:% tm/Card
        {:padding "$4"
         :gap "$3"}
        [:% tama/Entry
         {:entry {:title "Direct Tamagui card"
                  :status "ACTIVE"
                  :description "Shared Slim data contract"}
          :impl {:type "card"
                 :body [{:type "title"
                         :template ["title"]}
                        {:type "bold"
                         :template ["status"]}
                        {:type "p"
                         :template ["description"]}]}}]]
       [:% tama/Entry
        {:entry {:title "Horizontal entry"
                 :description "Tamagui layout primitives"}
         :impl {:type "h"
                :body [{:type "title-h3"
                        :template ["title"]}
                       {:type "p"
                        :template ["description"]}]}}]]))))

^{:refer melbourne.tama/Table :added "4.1"}
(fact "renders table rows directly with Tamagui primitives"
  (let [entry (l/sym-entry :js 'melbourne.tama/Table)
        form  (pr-str (:form entry))
        deps  (:deps-fragment entry)]
    deps => #{'js.core.impl/map
              'js.tamagui/Text
              'js.tamagui/XStack
              'js.tamagui/YStack}
    (string/includes? form "T.YStack") => true
    (string/includes? form "T.XStack") => true
    (not (string/includes? form "TamaguiProvider")) => true)
  ^:hidden
  (defn.js TableDemo
    []
    (var columns [{:key "symbol"
                   :label "Symbol"
                   :data ["symbol"]}
                  {:key "status"
                   :label "Status"
                   :data ["status"]}
                  {:key "description"
                   :label "Description"
                   :data ["description"]}])
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/Table"}
      [:% tm/Card
       {:padding "$4"
        :maxWidth 760}
       [:% tama/Table
        {:columns columns
         :entries [{:id "one"
                    :symbol "STATS"
                    :status "ACTIVE"
                    :description "First row"}
                   {:id "two"
                    :symbol "TAMA"
                    :status "READY"
                    :description "Second row"}
                   {:id "three"
                    :symbol "SLIM"
                    :status "BARE METAL"
                    :description "Third row"}]}]]))))


^{:refer melbourne.tama/TableToolbar :added "4.1"}
(fact "renders toolbar controls with Tamagui primitives"
  (let [entry (l/sym-entry :js 'melbourne.tama/TableToolbar)
        form  (pr-str (:form entry))
        deps  (:deps-fragment entry)]
    deps => #{'js.tamagui/XStack
              'js.tamagui/Button}
    (string/includes? form "showOrderBy") => true
    (not-any? #(re-find #"^(melbourne\\.ui-|js\\.react-native/)" (str %))
              deps) => true)
  ^:hidden
  (defn.js TableToolbarDemo
    []
    (var control (tama/useLocalControl))
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/TableToolbar"}
      [:% tama/TableToolbar
       {:control control}
       [:% tm/Text
        {:color "$color11"}
        "Shared Slim control state, direct Tamagui controls."]]))))

^{:refer melbourne.tama/TableList :added "4.1"}
(fact "renders list cards without the legacy list stack"
  (let [entry (l/sym-entry :js 'melbourne.tama/TableList)
        form  (pr-str (:form entry))
        deps  (:deps-fragment entry)]
    deps => #{'js.core.impl/map
              'js.react/%
              'js.tamagui/ScrollView
              'js.tamagui/YStack}
    (string/includes? form "T.ScrollView") => true
    (string/includes? form "Entry") => true
    (not (string/includes? form "melbourne.ui-section")) => true)
  ^:hidden
  (defn.js TableListDemo
    []
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/TableList"}
      [:% tm/YStack
       {:height 360
        :maxWidth 460}
       [:% tama/TableList
        {:entries [{:id "one"
                    :title "STATS"
                    :status "ACTIVE"}
                   {:id "two"
                    :title "TAMA"
                    :status "READY"}]
         :impl {:item {:type "card"
                       :body {:title {:type "title"
                                      :template ["title"]}
                              :status {:type "bold"
                                       :template ["status"]}}}}}]]))))

^{:refer melbourne.tama/TableStandard :added "4.1"}
(fact "keeps a direct empty state for standard tables"
  (let [entry (l/sym-entry :js 'melbourne.tama/TableStandard)
        form  (pr-str (:form entry))
        deps  (:deps-fragment entry)]
    deps => #{'js.react/%
              'js.tamagui/Button
              'js.tamagui/Text
              'js.tamagui/YStack}
    (string/includes? form "is-empty?") => true
    (string/includes? form "T.Button") => true)
  ^:hidden
  (defn.js TableStandardDemo
    []
    (var control (tama/useLocalControl))
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/TableStandard"}
      [:% tm/YStack
       {:height 260
        :maxWidth 520}
       [:% tama/TableStandard
        {:control control
         :entries []}]]))))

^{:refer melbourne.tama/TableEmbedded :added "4.1"}
(fact "renders embedded tables with a direct create action"
  (let [entry (l/sym-entry :js 'melbourne.tama/TableEmbedded)
        form  (pr-str (:form entry))
        deps  (:deps-fragment entry)]
    deps => #{'js.react/%
              'js.tamagui/Button
              'js.tamagui/YStack}
    (string/includes? form "not-empty?") => true
    (string/includes? form "T.Button") => true)
  ^:hidden
  (defn.js TableEmbeddedDemo
    []
    (var control (tama/useLocalControl))
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/TableEmbedded"}
      [:% tm/YStack
       {:height 320
        :maxWidth 760}
       [:% tama/TableEmbedded
        {:control control
         :columns [{:key "symbol"
                    :label "Symbol"
                    :data ["symbol"]}
                   {:key "status"
                    :label "Status"
                    :data ["status"]}]
         :entries [{:id "one"
                    :symbol "STATS"
                    :status "ACTIVE"}
                   {:id "two"
                    :symbol "TAMA"
                    :status "READY"}]}]]))))

^{:refer melbourne.tama/SheetHeader :added "4.1"}
(fact "renders sheet headings directly"
  (let [entry (l/sym-entry :js 'melbourne.tama/SheetHeader)
        form  (pr-str (:form entry))
        deps  (:deps-fragment entry)]
    deps => #{'js.core.impl/map
              'js.tamagui/Text
              'js.tamagui/XStack}
    (string/includes? form "T.XStack") => true
    (string/includes? form "T.Text") => true)
  ^:hidden
  (defn.js SheetHeaderDemo
    []
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/SheetHeader"}
      [:% tm/Card
       {:padding "$3"}
       [:% tama/SheetHeader
        {:impl {:columns [{:key "symbol"
                           :label "Symbol"}
                          {:key "status"
                           :label "Status"}]}}]]))))

^{:refer melbourne.tama/SheetRow :added "4.1"}
(fact "renders sheet cells with the shared entry renderer"
  (let [entry (l/sym-entry :js 'melbourne.tama/SheetRow)
        form  (pr-str (:form entry))
        deps  (:deps-fragment entry)]
    deps => #{'js.core.impl/map
              'js.react/%
              'js.tamagui/Text
              'js.tamagui/XStack
              'js.tamagui/YStack}
    (string/includes? form "entryValue") => true
    (string/includes? form "Entry") => true
    (not (string/includes? form "js.react-native")) => true)
  ^:hidden
  (defn.js SheetRowDemo
    []
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/SheetRow"}
      [:% tm/Card
       {:padding "$3"}
       [:% tama/SheetRow
        {:entry {:symbol "STATS"
                 :status "ACTIVE"}
         :impl {:columns [{:key "symbol"
                           :label "Symbol"
                           :template ["symbol"]}
                          {:key "status"
                           :label "Status"
                           :template ["status"]}]}}]]))))

^{:refer melbourne.tama/SheetBasic :added "4.1"}
(fact "renders a scrollable sheet from direct primitives"
  (let [entry (l/sym-entry :js 'melbourne.tama/SheetBasic)
        form  (pr-str (:form entry))
        deps  (:deps-fragment entry)]
    deps => #{'js.core.impl/map
              'js.react/%
              'js.tamagui/ScrollView
              'js.tamagui/YStack}
    (string/includes? form "SheetHeader") => true
    (string/includes? form "SheetRow") => true)
  ^:hidden
  (defn.js SheetBasicDemo
    []
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/SheetBasic"}
      [:% tm/YStack
       {:height 360
        :maxWidth 760}
       [:% tama/SheetBasic
        {:impl {:columns [{:key "symbol"
                           :label "Symbol"
                           :template ["symbol"]}
                          {:key "status"
                           :label "Status"
                           :template ["status"]}]}
         :entries [{:id "one"
                    :symbol "STATS"
                    :status "ACTIVE"}
                   {:id "two"
                    :symbol "TAMA"
                    :status "READY"}
                   {:id "three"
                    :symbol "SLIM"
                    :status "BARE METAL"}]}]]))))

^{:refer melbourne.tama/Sheet :added "4.1"}
(fact "keeps Sheet as a thin direct renderer seam"
  (pr-str (:form (l/sym-entry :js 'melbourne.tama/Sheet)))
  => "(defn Sheet [props] (return (React.createElement SheetBasic props)))"
  ^:hidden
  (defn.js SheetDemo
    []
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/Sheet"}
      [:% tm/YStack
       {:height 360
        :maxWidth 760}
       [:% tama/Sheet
        {:impl {:columns [{:key "symbol"
                           :label "Symbol"
                           :template ["symbol"]}
                          {:key "status"
                           :label "Status"
                           :template ["status"]}]}
         :entries [{:id "one"
                    :symbol "STATS"
                    :status "ACTIVE"}
                   {:id "two"
                    :symbol "TAMA"
                    :status "READY"}]}]]))))

^{:refer melbourne.tama/createEntry :added "4.1"}
(fact "creates Entry elements without introducing a wrapper component"
  (let [entry (l/sym-entry :js 'melbourne.tama/createEntry)
        form  (pr-str (:form entry))]
    (:deps-fragment entry) => #{'js.react/%}
    (string/includes? form "React.createElement Entry") => true)
  ^:hidden
  (defn.js CreateEntryDemo
    []
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/createEntry"}
      [:% tm/Card
       {:padding "$4"}
       (tama/createEntry
        {:entry {:title "Created directly"}
         :impl {:type "title-h3"
                :template ["title"]}})]))))

^{:refer melbourne.tama/entry :added "4.1"}
(fact "merges implementation options at the direct Entry seam"
  (let [entry (l/sym-entry :js 'melbourne.tama/entry)
        form  (pr-str (:form entry))]
    (:deps-fragment entry) => #{'js.react/%}
    (string/includes? form "Object.assign") => true
    (string/includes? form "or opts") => true)
  ^:hidden
  (defn.js EntryOptionsDemo
    []
    (return
     (n/EnclosedCode
      {:label "melbourne.tama/entry"}
      [:% tm/Card
       {:padding "$4"}
       (tama/entry
        {:entry {:title "Option merge"}}
        {:type "title-h3"
         :template ["title"]}
        {:custom {"title" {:color "$blue10"}}})]))))

;; tama menu helper candidate

;; tama menu insertion point
(do
  (def.js MODULE (!:module))
  :ok)

^{:refer component.web-tama-slim/TamaProvider :added "4.1"}
(fact "selects the light Tamagui theme for the web demo"
  (let [form (pr-str (:form (l/sym-entry :js 'component.web-tama-slim/TamaProvider)))]
    (clojure.string/includes? form ":defaultTheme \"light\"") => true))
