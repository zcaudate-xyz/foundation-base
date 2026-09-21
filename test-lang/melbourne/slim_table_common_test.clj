(ns melbourne.slim-table-common-test
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
             [melbourne.slim :as slim]
             [melbourne.slim-table-common :as slim-table-common]
             [melbourne.slim-entry :as slim-entry]
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

^{:refer melbourne.slim-table-common/useTableEntry :added "4.0"}
(fact "uses the table entry")

^{:refer melbourne.slim-table-common/TableDefaultNotFound :added "4.0"}
(fact "default not found placeholder"
  ^:hidden
  
  (defn.js TableDefaultNotFoundDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-common/TableDefaultNotFound"} 
[:% n/Row
       [:% slim-table-common/TableDefaultNotFound
        {:design {:type "light"}}]]))))

^{:refer melbourne.slim-table-common/TableDefaultIsLoading :added "4.0"}
(fact "default is loading placeholder"
  ^:hidden
  
  (defn.js TableDefaultIsLoadingDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-common/TableDefaultIsLoading"} 
[:% n/Row
       [:% slim-table-common/TableDefaultIsLoading
        {:design {:type "light"}}]]))))

^{:refer melbourne.slim-table-common/TableBackButton :added "4.0"}
(fact "creates a back button"
  ^:hidden
  
  (defn.js TableBackButtonDemo
    []
    (var control (slim/useLocalControl))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-common/TableBackButton"} 
[:% n/Row
       [:% slim-table-common/TableBackButton
        {:design {:type "light"}
         :control control}]]))))

^{:refer melbourne.slim-table-common/tablePageHooks :added "4.0"}
(fact "uses page hooks")

^{:refer melbourne.slim-table-common/tablePageView :added "4.0"}
(fact "generic page view"
  ^:hidden
  
  (defn.js TablePageViewDemo
    []
    (var control (slim/useLocalControl))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-table-common/tablePageView"} 
[:% n/Row
       #_[:% slim-table-common/TableBackButton
        {:design {:type "light"}
         :control control}]])))
  
  (def.js MODULE (!:module)))
