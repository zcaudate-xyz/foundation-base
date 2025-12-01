(ns js.react-native.ext-view-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react.ext-view :as ext-view]
             [xt.lang.event-view :as event-view]
             [xt.lang.base-lib :as k]]
   :export [MODULE]})

^{:refer js.react.ext-view/listenView :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"

  (defn.js ListenViewPane
    [#{view
       type}]
    (var output (ext-view/listenView view type {}))
    (var getCount (r/useGetCount))
    (return
     [:% n/TextDisplay
      {:content (n/format-entry
                 {:type type
                  :result output
                  :count (getCount)
                  :view  (k/obj-pick view ["input" "output"])})}]))
  
  (defn.js ListenViewDemo
    []
    (var view (ext-view/makeView
               {:handler (fn:> [x y z]
                               (j/future-delayed [500]
                                 (return (+ x y z))))
                :defaultArgs [1 2 3]
                :options {:init false}}))
    (var [type setType] (r/local "success"))
    (r/init []
      (ext-view/refresh-view view))
    (return
     (n/EnclosedCode 
{:label "js.react.ext-view/listenView"} 
[:% n/Row
       [:% n/Button
        {:title "R"
         :onPress (fn:> (ext-view/refresh-args
                         view
                         [(j/random)
                          (j/random)
                          (j/random)]))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn []
                    (event-view/set-input view {})
                    (ext-view/refresh-view view))}]
       [:% n/Tabs
        {:data ["input" "output" "pending" "elapsed" "disabled" "success"]
         :value type
         :setValue setType}]] 
[:% -/ListenViewPane
       #{view type
         {:key type}}]))))


^{:refer js.react.ext-view/listenViewOutput :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"

  (defn.js ListenViewOutputPane
    [#{view
       types}]
    (var output (ext-view/listenViewOutput
                 view types {}))
    (var getCount (r/useGetCount))
    (return
     [:% n/TextDisplay
      {:content (n/format-entry
                 {:types types
                  :result output
                  :count (getCount)
                  :view  (k/obj-pick view ["input" "output"])})}]))
  
  (defn.js ListenViewOutputDemo
    []
    (var view (ext-view/makeView
               {:handler (fn:> [x y z]
                               (j/future-delayed [500]
                                 (return (+ x y z))))
                :defaultArgs [1 2 3]
                :options {:init false}}))
    (var [types setTypes] (r/local ["pending" "disabled"]))
    (r/init []
      (ext-view/refresh-view view))
    (return
     (n/EnclosedCode 
{:label "js.react.ext-view/listenViewOutput"} 
[:% n/Row
       [:% n/Button
        {:title "R"
         :onPress (fn:> (ext-view/refresh-args
                         view
                         [(j/random)
                          (j/random)
                          (j/random)]))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn []
                    (event-view/set-input view {})
                    (ext-view/refresh-view view))}]
       [:% n/TabsMulti
        {:data ["input" "output" "pending" "elapsed" "disabled"]
         :values types
         :setValues setTypes}]] 
[:% -/ListenViewOutputPane
       #{view types
         {:key types}}]))))


^{:refer js.react.ext-view/listenViewOutput.MULTI :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"

  (defn.js ListenViewOutputMultiPane
    [#{view
       types}]
    (var remoteOutput (ext-view/listenViewOutput
                       view types {} "remote"))
    (var mainOutput (ext-view/listenViewOutput
                     view types {}))
    (var syncOutput (ext-view/listenViewOutput
                     view types {} "sync"))
    (var getCount (r/useGetCount))
    (return
     [:% n/TextDisplay
      {:content (n/format-entry
                 {:types types
                  :result {:main mainOutput
                           :remote remoteOutput
                           :sync syncOutput}
                  :count (getCount)
                  :view  (k/obj-pick view ["input" "output" "sync" "remote"])})}]))
  
  (defn.js ListenViewOutputMultiDemo
    []
    (var view (ext-view/makeView
               {:handler (fn:> [x y z]
                               (j/future-delayed [500]
                                 (return (+ x y z))))
                :pipeline {:sync  {:handler (fn:> [x y z]
                                              (j/future-delayed [500]
                                                (return (+ x y z))))}
                           :remote {:handler (fn:> [x y z]
                                               (j/future-delayed [500]
                                                 (return (+ x y z))))}}
                :defaultArgs [1 2 3]
                :options {:init false}}))
    (var [types setTypes] (r/local ["pending" "disabled"]))
    (r/init []
      (ext-view/refresh-view view))
    (return
     (n/EnclosedCode 
{:label "js.react.ext-view/listenViewOutput.SYNC"} 
[:% n/Row
       [:% n/Button
        {:title "M"
         :onPress (fn:> (ext-view/refresh-args
                         view
                         [(j/random)
                          (j/random)
                          (j/random)]))}]
       [:% n/Button
        {:title "R"
         :onPress (fn:> (ext-view/refresh-args-remote
                         view
                         [(j/random)
                          (j/random)
                          (j/random)]
                         true))}]
       [:% n/Button
        {:title "S"
         :onPress (fn:> (ext-view/refresh-args-sync
                         view
                         [(j/random)
                          (j/random)
                          (j/random)]
                         true))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn []
                    (event-view/set-input view {})
                    (ext-view/refresh-view view))}]
       [:% n/TabsMulti
        {:data ["input" "output" "pending" "elapsed" "disabled"]
         :values types
         :setValues setTypes}]] 
[:% -/ListenViewOutputMultiPane
       #{view types
         {:key types}}])))
  
  (def.js MODULE (!:module)))

(comment
  
  (j/<!
   (do:> (var m (ar/createAsync {:name "add"
                                 :handler (fn:> [x y z] (+ x y z))
                                 :argsFn  (fn:> [] [1 2 3])}))
         (return
          (. (j/future
               (return (link-view/view-stage {:cell {}
                                              :args [1 2 3]
                                              :view m})))
             (then (fn:> [acc] [acc (. m ["output"])]))))))
  
  (k/trace-log-clear)
  (k/trace-log))
