(ns melbourne.slim-table-toolbar-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react.ext-model :as ext-view]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-group :as ui-group]
             [melbourne.slim-table-toolbar :as slim-table-toolbar]
             [melbourne.slim :as slim]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-static :as ui-static]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [xt.event.base-route :as event-route]]
   :export [MODULE]})

^{:refer melbourne.slim-table-toolbar/TableToolbar :added "0.1"}
(fact "creates the table toolbar"
  ^:hidden
  
  (defn.js TableToolbarDemo
    []
    (var control (slim/useLocalControl))
    (return
     (n/EnclosedCode 
{:label "slim-table-toolbar/TableToolbar"} 
[:% ui-static/Div
       {:design {:type "light"}}
       [:% slim-table-toolbar/TableToolbar
        {:design {:type "light"}
         :mini true
         :actions {}
         :control control}]]))))
