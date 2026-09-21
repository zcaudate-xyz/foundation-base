(ns melbourne.ui-text-dialog-test
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
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-text-dialog :as ui-text-dialog]
             [melbourne.ui-section :as ui-section]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]]
   :export [MODULE]})

^{:refer melbourne.ui-text-dialog/ConfirmDialog :added "4.0"}
(fact  "creates a button with dialog confirmation"
  ^:hidden

  (defn.js ConfirmDialogDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text-dialog/ConfirmDialog"} 
[:% n/Row
       [:% ui-section/SectionBase
        {:design {:type "light"}}
        [:% n/Row
         [:% ui-text-dialog/ConfirmDialog
          {:design {:type "light"}
           :text  "Press"
           :onPress (fn:> (alert "HELLO"))}]
         [:% ui-text-dialog/ConfirmDialog
          {:design {:type "light"}
           :component "accent"
           :text  "Press"
           :onPress (fn:> (alert "HELLO"))}]]]
       [:% ui-section/SectionBase
        {:design {:type "dark"}}
        [:% n/Row
         [:% ui-text-dialog/ConfirmDialog
          {:design {:type "dark"}
           :text  "Press"
           :onPress (fn:> (alert "HELLO"))}]
         [:% ui-text-dialog/ConfirmDialog
          {:design {:type "dark"}
           :component "accent"
           :text  "Press"
           :onPress (fn:> (alert "HELLO"))}]]]])))

  (def.js MODULE (!:module)))


