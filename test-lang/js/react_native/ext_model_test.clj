(ns js.react-native.ext-model-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
    :require [[js.react :as r :include [:fn]]
              [js.react-native :as n :include [:fn]]
              [js.react.ext-model :as ext-model]
              [xt.event.base-model :as event-model]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-lib :as k]
              [xt.lang.spec-base :as xt]]
   })

^{:refer js.react.ext-model/listenView :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"

  (defn.js ListenViewPane
    [#{view
       type}]
    (var output (ext-model/listenView view type {}))
    (var getCount (r/useGetCount))
    (return
     [:% n/TextDisplay
      {:content (n/format-entry
                 {:type type
                  :result output
                  :count (getCount)
                  :view  (xtd/obj-pick view ["input" "output"])})}]))
  
  (defn.js ListenViewDemo
    []
    (var view (ext-model/makeView
               {:handler (fn:> [x y z]
                               (new Promise
                                  (fn [resolve]
                                    (setTimeout
                                     (fn []
                                       (resolve (+ x y z)))
                                     500))))
                :defaultArgs [1 2 3]
                :options {:init false}}))
    (var [type setType] (r/local "success"))
    (r/init []
      (ext-model/refresh-view view))
    (return
     (n/EnclosedCode 
{:label "js.react.ext-model/listenView"} 
[:% n/Row
       [:% n/Button
        {:title "R"
         :onPress (fn:> (ext-model/refresh-args
                         view
                         [(Math.random)
                          (Math.random)
                          (Math.random)]))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn []
                    (event-model/set-input view {})
                    (ext-model/refresh-view view))}]
       [:% n/Tabs
        {:data ["input" "output" "pending" "elapsed" "disabled" "success"]
         :value type
         :setValue setType}]] 
 [:% n/TextDisplay
        {:key type
         :content (n/format-entry
                   {:type type
                    :result (ext-model/listenView view type {})
                    :count ((r/useGetCount))
                    :view  (xtd/obj-pick view ["input" "output"])})}]))))


^{:refer js.react.ext-model/listenViewOutput :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"

  (defn.js ListenViewOutputPane
    [#{view
       types}]
    (var output (ext-model/listenViewOutput
                 view types {}))
    (var getCount (r/useGetCount))
    (return
     [:% n/TextDisplay
      {:content (n/format-entry
                 {:types types
                  :result output
                  :count (getCount)
                  :view  (xtd/obj-pick view ["input" "output"])})}]))
  
  (defn.js ListenViewOutputDemo
    []
    (var view (ext-model/makeView
               {:handler (fn:> [x y z]
                               (new Promise
                                  (fn [resolve]
                                    (setTimeout
                                     (fn []
                                       (resolve (+ x y z)))
                                     500))))
                :defaultArgs [1 2 3]
                :options {:init false}}))
    (var [types setTypes] (r/local ["pending" "disabled"]))
    (r/init []
      (ext-model/refresh-view view))
    (return
     (n/EnclosedCode 
{:label "js.react.ext-model/listenViewOutput"} 
[:% n/Row
       [:% n/Button
        {:title "R"
         :onPress (fn:> (ext-model/refresh-args
                         view
                         [(Math.random)
                          (Math.random)
                          (Math.random)]))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn []
                    (event-model/set-input view {})
                    (ext-model/refresh-view view))}]
       [:% n/TabsMulti
        {:data ["input" "output" "pending" "elapsed" "disabled"]
         :values types
         :setValues setTypes}]] 
 [:% n/TextDisplay
        {:key types
         :content (n/format-entry
                   {:types types
                    :result (ext-model/listenViewOutput view types {})
                    :count ((r/useGetCount))
                    :view  (xtd/obj-pick view ["input" "output"])})}]))))


^{:refer js.react.ext-model/listenViewOutput.MULTI :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"

  (defn.js ListenViewOutputMultiPane
    [#{view
       types}]
    (var remoteOutput (ext-model/listenViewOutput
                       view types {} "remote"))
    (var mainOutput (ext-model/listenViewOutput
                     view types {}))
    (var syncOutput (ext-model/listenViewOutput
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
                  :view  (xtd/obj-pick view ["input" "output" "sync" "remote"])})}]))
  
  (defn.js ListenViewOutputMultiDemo
    []
    (var view (ext-model/makeView
               {:handler (fn:> [x y z]
                               (new Promise
                                  (fn [resolve]
                                    (setTimeout
                                     (fn []
                                       (resolve (+ x y z)))
                                     500))))
                :pipeline {:sync  {:handler (fn:> [x y z]
                                              (new Promise
                                                   (fn [resolve]
                                                     (setTimeout
                                                      (fn []
                                                        (resolve (+ x y z)))
                                                      500))))}
                           :remote {:handler (fn:> [x y z]
                                               (new Promise
                                                    (fn [resolve]
                                                      (setTimeout
                                                       (fn []
                                                         (resolve (+ x y z)))
                                                       500))))}}
                :defaultArgs [1 2 3]
                :options {:init false}}))
    (var [types setTypes] (r/local ["pending" "disabled"]))
    (r/init []
      (ext-model/refresh-view view))
    (return
     (n/EnclosedCode 
{:label "js.react.ext-model/listenViewOutput.SYNC"} 
[:% n/Row
       [:% n/Button
        {:title "M"
         :onPress (fn:> (ext-model/refresh-args
                         view
                         [(Math.random)
                          (Math.random)
                          (Math.random)]))}]
       [:% n/Button
        {:title "R"
         :onPress (fn:> (ext-model/refresh-args-remote
                         view
                         [(Math.random)
                          (Math.random)
                          (Math.random)]
                         true))}]
       [:% n/Button
        {:title "S"
         :onPress (fn:> (ext-model/refresh-args-sync
                         view
                         [(Math.random)
                          (Math.random)
                          (Math.random)]
                         true))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn []
                    (event-model/set-input view {})
                    (ext-model/refresh-view view))}]
       [:% n/TabsMulti
        {:data ["input" "output" "pending" "elapsed" "disabled"]
         :values types
         :setValues setTypes}]] 
 [:% n/TextDisplay
        {:key types
         :content (n/format-entry
                   {:types types
                    :result {:main   (ext-model/listenViewOutput view types {})
                             :remote (ext-model/listenViewOutput view types {} "remote")
                             :sync   (ext-model/listenViewOutput view types {} "sync")}
                    :count ((r/useGetCount))
                    :view  (xtd/obj-pick view ["input" "output" "sync" "remote"])})}])))
  
  )

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
