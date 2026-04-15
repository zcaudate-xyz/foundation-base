(ns js.react-native.ext-cell-test
  (:require [js.cell.playground :as browser]
            [std.lang :as l])
  (:use code.test))

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
              [js.cell :as cl]
              [js.cell.kernel.worker-mock :as worker-mock]
               [js.cell.kernel.base-impl :as impl-common]
              [js.cell.kernel.base-link-local :as link-fn]
               [js.react.ext-cell :as cr]
               [xt.lang.common-data :as xtd]
               [xt.lang.common-sort-by :as xtsb]
               [xt.lang.common-spec :as xt]
              [xt.lang.common-lib :as k]
              ]
   })

^{:refer js.react-native.ext-cell-test/SimpleCell :adopt true :added "0.1"}
(fact "creates a  Tree Pane"
  ^:hidden

  (!.js
   (var cell (impl-common/new-cell
              {:create-fn
               (fn:> [listener]
                     (worker-mock/create-worker listener {} true))}))
   (cl/view-get-output ["basic" "ping"] cell))
  
  (defn.js SimpleCellDemo
    []
    (var cell (r/const (impl-common/new-cell
                        {:create-fn
                         (fn:> [listener]
                               (worker-mock/create-worker listener {} true))})))
    (cr/listenCellOutput ["basic" "ping"]
                         ["pending" "disabled"]
                         {}
                         cell)
    (var getCount (r/useGetCount))
    
    (r/init []
      (cl/add-model "basic"
                    {:ping  {:handler link-fn/ping
                             :defaultArgs []
                             :defaultOutput {:output "NOT AVAILABLE"}}}
                    cell))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ext-cell-test/SimpleCell"} 
[:% n/Row
       [:% n/Button
        {:title "Print"
         :onPress (fn:> (console.log cell))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "Trigger"
         :onPress (fn:> (cl/view-refresh ["basic" "ping"] cell))}]] 
[:% n/Caption
       {:text (xt/x:json-encode {:count (getCount)
                            :data (cl/get-val ["basic" "ping"]
                                              []
                                              cell)})
        :style {:marginTop 10}}]))))

^{:refer js.react-native.ext-cell-test/SimpleCellViews :adopt true :added "0.1"}
(fact "creates a  Tree Pane"
  ^:hidden
  
  (defn.js SimpleCellViewsDemo
    []
    (var [l0 setL0] (r/local "basic0"))
    (var [l1 setL1] (r/local "ping1"))
    (var getCount (r/useGetCount))
    (var cell (r/const (impl-common/new-cell
                        {:create-fn
                         (fn:> [listener]
                               (worker-mock/create-worker listener {} true))})))
    (cr/listenCellOutput ["basic0" "ping0"]
                         ["output"]
                         {}
                         cell)
    (cr/listenCellOutput ["basic1" "ping1"]
                         ["output"]
                         {}
                         cell)
    (cr/listenCellOutput ["basic2" "ping2"]
                         ["output"]
                         {}
                         cell)
    (cr/listenCellOutput ["basic3" "ping3"]
                         ["output"]
                         {}
                         cell)
    
    (r/init []
      (cl/add-model "basic0"
                       {:ping0  {:handler link-fn/ping
                                 :defaultArgs []
                                 :defaultOutput {:output "NOT AVAILABLE0"}}
                        :ping1  {:handler link-fn/ping
                                 :defaultArgs []
                                 :defaultOutput {:output "NOT AVAILABLE1"}}}
                       cell)
      (j/delayed [100]
        (cl/add-model "basic1"
                      {:ping2  {:handler link-fn/ping
                                :defaultArgs []
                                :defaultOutput {:output "NOT AVAILABLE2"}}
                       :ping3  {:handler link-fn/ping
                                :defaultArgs []
                                :defaultOutput {:output "NOT AVAILABLE3"}}}
                      cell)))
    (r/useInterval (fn []
                     (j/delayed [(+ 1000 (* 1000 (j/random)))]
                       (cl/view-refresh ["basic0" "ping0"]
                                        cell))
                     (j/delayed [(+ 1000 (* 1000 (j/random)))]
                       (cl/view-refresh ["basic0" "ping1"]
                                        cell))
                     (j/delayed [(+ 1000 (* 1000 (j/random)))]
                       (cl/view-refresh ["basic1" "ping2"]
                                        cell))
                     (j/delayed [(+ 1000 (* 1000 (j/random)))]
                       (cl/view-refresh ["basic1" "ping3"]
                                        cell)))
                   600)
    (return
     (n/EnclosedCode 
{:label "js.react-native.ext-cell-test/SimpleCellViews"} 
[:% n/TreePane
       {:tree  cell
        :levels [{:type "list"
                  :initial l0
                  :setInitial setL0
                  :listWidth 100
                  :listFormat j/toUpperCase
                  :formatFn xt/x:json-encode
                  :branchesFn
                  (fn:> [cell]
                    (xtsb/sort-by (cl/list-models cell) [k/identity]))
                  :targetFn
                  (fn [cell model]
                    (return (cl/get-model model cell)))}
                 {:type "tabs"
                  :initial l1
                  :setInitial setL1
                  :listWidth 100
                  :listFormat j/toUpperCase
                  :formatFn xt/x:json-encode
                  :branchesFn 
                  (fn [model parents cell]
                    (when (and (xtd/first parents)
                               (cl/get-model (xtd/first parents) cell))
                      (return (cl/list-views (xtd/first parents) cell)))
                    (return []))
                  :targetFn
                  (fn [model modelKey parents cell]
                    (return (cl/get-val [(:.. (j/arrayify parents))
                                         modelKey]
                                        []
                                        cell)))}]}] 
[:% n/Caption
       {:text (n/format-entry
               #{[:count (getCount)
                  :data (cl/cell-vals cell)
                  l1 l0]})
        :style {:marginTop 10}}]))))

^{:refer js.react-native.ext-cell-test/listen-current :adopt true :added "0.1"}
(fact "creates a  Tree Pane"
  ^:hidden
  
  (defn.js ListenCurrentDemo
    []
    (var [initial setInitial] (r/local "a"))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ext-cell-test/listen-current"} 
[:% n/TreePane
       {:tree  {}
        :levels []}] 
[:% n/Caption
       {:text (xt/x:json-encode #{initial})
        :style {:marginTop 10}}])))
  
  
  
  )
