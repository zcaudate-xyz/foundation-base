(ns js.blessed.ui-util-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :basic
   :config   {:emit {:lang/jsx false}}
   :require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.lib.valtio :as v]
              [js.blessed.ui-core :as ui-core]
              [js.blessed.ui-util :as ui-util]
              [js.blessed :as b :include [:fn]]
              [js.lib.chalk :as chk]
              [xt.lang.base-lib :as k]]
   :export  [MODULE]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.blessed.ui-util/copyClipboard :added "4.0" :unchecked true}
(fact "helper function to copy text to clipboard")

^{:refer js.blessed.ui-util/say :added "4.0" :unchecked true}
(fact "helper function to say something"
  ^:hidden
  
  (defn.js SayDemo
    []
    (let [[val setVal] (r/local "Hello World")]
      (return
       [:box {:width 30
              :content "ui-util/say"
              :height 8}
        [:% ui-core/SmallButton
         {:top 2
          :left 0
          :color "green"
          :content (+ "Say " val)
          :onClick (fn []
                     (ui-util/say val))}]]))))

^{:refer js.blessed.ui-util/CopyButton :added "4.0" :unchecked true}
(fact "provides a Copy Button"
  ^:hidden

  (defn.js CopyClipboardDemo
    []
    (var [val setVal] (r/local "Copied Text"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-util/copyClipboard"}
      [:% ui-core/SmallButton
       {:top 2
        :left 0
        :color "yellow"
        :content "Copy Text"
        :onClick (fn []
                   (ui-util/copyClipboard val)
                   (ui-util/say "Text Copied"))}]
      [:% ui-util/CopyButton
       {:top 4
        :color "red"
        :input val
        :onClick (fn []
                   (ui-util/say "Text Copied"))}]])))


^{:refer js.blessed.ui-util/parseErrorTag :added "4.0" :unchecked true}
(fact "parses the error tag")

^{:refer js.blessed.ui-util/ErrorLine :added "4.0" :unchecked true}
(fact "provides an error line"
  ^:hidden
  
  (defn.js ErrorLineDemo
    []
    (var [val setVal] (r/local "Copied Text"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-util/ErrorLine"}
      [:% ui-util/ErrorLine
       {:top 2
        :left 0
        :error {:tag "hello/world"
                :message "HELLO"}
        :content "Copy Text"}]])))

^{:refer js.blessed.ui-util/StatusLine :added "4.0" :unchecked true}
(fact "provides a status line"
  ^:hidden
  
  (defn.js StatusLineDemo
    []
    (var [val setVal] (r/local "Copied Text"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-util/StatusLine"}
      [:% ui-util/StatusLine
       {:top 2
        :left 0
        :message "HELLO WORLD"}]]))
  
  )
