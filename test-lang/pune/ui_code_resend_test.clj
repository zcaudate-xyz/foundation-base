(ns pune.ui-code-resend-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react.ext-form :as ext-form]
             [js.react-native :as n :include [:fn]]
             [melbourne.ui-static :as ui-static]
             [pune.ui-code-resend :as ui-code-resend]
             [melbourne.base-validators :as validators]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

^{:refer pune.ui-code-resend/CodeResendButton :added "0.1"}
(fact "creates the submit resend form"
  ^:hidden
  
  (defn.js  CodeResendButtonDemo
    []
    (var [result0 setResult0] (r/local (fn:>)))
    (var [result1 setResult1] (r/local (fn:>)))
    (var sink0Id (r/id))
    (var sink1Id (r/id))
    (var [key setKey] (r/local (Math.random)))
    (var form (ext-form/makeForm (fn:> {:email "test00001@statstrade.io"
                                        :code  "1234"})
                                 {:email []
                                  :code  [(validators/is-length-n 6)]}))
    (return
     (n/EnclosedCode 
{:label "pune.ui-code-resend/CodeResendButton"} 
[:% n/Row
       [:% n/Button {:title "R"
                     :onPress (fn:> (setKey (Math.random)))}]] 
[:% n/View
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :padding 20}}
        [:% ui-code-resend/CodeResendButton
         #{form 
           {:key key
            :design {:type "light"}
            :sinkId sink0Id
            :submitProps {:onResult setResult0
                          :onSubmit (fn []
                                      (return (jc/future-delayed [300]
                                               (return
                                                {:status "ok"
                                                 :updated (xt/x:now-ms)}))))}}}]
        [:% n/PortalSink
         {:name sink0Id
          :style {:height 80}}]]
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :padding 20}}
        [:% ui-code-resend/CodeResendButton
         #{form
           {:key key
            :design {:type "dark"}
            :sinkId sink1Id
            :submitProps {:onResult setResult1
                          :onSubmit (fn []
                                      (return (jc/future-delayed [300]
                                               (return
                                                {:status "error"}))))}}}]
        [:% n/PortalSink
         {:name sink1Id
          :style {:height 80}}]]] 
[:% n/TextDisplay
       {:content (n/format-entry
                  #{result0 result1
                    {:data form.data}})}])))

  (def.js MODULE (!:module)))
