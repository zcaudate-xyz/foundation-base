(ns lang-demos.js-007-statstrade-substrate.main
  (:require [lang.core :as l]))

(l/script :js
  {:config {:emit {:lang/jsx false}}
   :require [[lang-demos.js-007-statstrade-substrate.fixture :as fixture]
             [lang-demos.js-007-statstrade-substrate.model :as model]
             [lang-demos.js-007-statstrade-substrate.hook :as hook]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react-native :as n]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-input :as ui-input]]})

(def.js DESIGN {:type "light"})

(defn.js EntryFields
  [#{entry fields}]
  (return
   [:% n/View
    (. fields
       (map (fn [field]
              (return
               [:% ui-static/Text
                {:key field :design -/DESIGN :style {:marginBottom 4}}
                (+ field ": " (or (. entry [field]) ""))]))))]))

(defn.js TopicEditor
  [#{context}]
  (var [selected setSelected] (r/local "topic-1"))
  (var [title setTitle] (r/local ""))
  (var [message setMessage] (r/local ""))
  (var list-view (hook/useListView model/TOPIC {} context))
  (var detail-view (hook/useSingleView model/TOPIC {} context selected))
  (var views {:list list-view :detail detail-view})
  (var actions (hook/useActions model/TOPIC context))
  (var rows (or (ext-model/listenView (. views list) "success") []))
  (var detail (ext-model/listenViewOutput (. views detail) ["output" "pending"] {}))
  (var entry (. detail current))
  (r/watch [entry]
    (setTitle (or (and entry (. entry title)) "")))
  (var #{waiting errored result onAction}
       (r/useSubmitResult
        {:onSubmit (fn [] (return (. actions (modify selected title))))
         :onError (fn [err] (return {:status "error" :message (. err message)}))
         :onSuccess (fn [] (setMessage "Saved; the topic/changed event refreshed the views."))}))
  (return
   [:% n/View {:style {:gap 20}}
    [:% ui-static/Text {:design -/DESIGN :style {:fontSize 24 :fontWeight "700"}}
     "Topics"]
    [:% n/View {:style {:gap 8}}
     (. rows
        (map (fn [row]
               (return
                [:% n/View {:key (. row id) :style {:padding 12 :borderWidth 1 :borderColor "#ddd"}}
                 [:% -/EntryFields {:entry row :fields (. model/TOPIC entries list)}]
                 [:% ui-button/Button
                  {:design -/DESIGN :text "Select"
                   :textProps {:accessibilityLabel (+ "Select " (. row id))}
                   :onPress (fn [] (setSelected (. row id)) (setMessage ""))}]]))))]
    [:% ui-static/Text {:design -/DESIGN :style {:fontSize 20 :fontWeight "700"}}
     "Detail / modify"]
    (:? entry
        [:% n/View {:style {:gap 12}}
         [:% -/EntryFields {:entry entry :fields (. model/TOPIC entries detail)}]
         (. (. model/TOPIC entries modify)
            (map (fn [field]
                   (return
                    [:% ui-input/Input
                     {:key field :design -/DESIGN :value title :onChangeText setTitle
                      :accessibilityLabel field :editable (not waiting)}]))))
         [:% ui-button/Button
          {:design -/DESIGN :text (:? waiting "Saving..." "Save title")
           :textProps {:accessibilityLabel "Save title"}
           :disabled (or waiting (. detail pending))
           :onPress onAction}]]
        [:% ui-static/Text {:design -/DESIGN} "Loading topic..."])
    [:% ui-static/Text {:design -/DESIGN :accessibilityRole "status"}
     (:? errored (or (. result message) "Unable to save") message)]]))

(defn.js App
  []
  (var [state setState] (r/local nil))
  (var [error setError] (r/local nil))
  (r/init []
    (var active true)
    (var connected nil)
    (. (fixture/bootstrap)
       (then (fn [value]
               (:= connected value)
               (if active (setState value) (fixture/close value))))
       (catch (fn [err] (when active (setError (. err message))))))
    (return (fn []
              (:= active false)
              (when connected (fixture/close connected)))))
  (return
   [:% n/View {:style {:maxWidth 640 :width "100%" :alignSelf "center" :padding 24}}
    (:? error
        [:% n/Text error]
        state
        [:% -/TopicEditor {:context (. state context)}]
        :else
        [:% n/Text "Connecting..."])]))

(defn.js mount
  []
  (return (r/renderDOMRoot "app" -/App)))
