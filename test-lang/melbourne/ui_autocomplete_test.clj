(ns melbourne.ui-autocomplete-test
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
             [js.react-native :as n :include [:fn]]
             [js.react.ext-form :as ext-form]
             [js.react.ext-model :as ext-view]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [melbourne.ui-autocomplete :as ui-autocomplete]
             [melbourne.slim-sheet :as slim-sheet]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(def.js NAMES
  (@! (->> (h/sys:resource-content "melbourne/girl-names.json")
           (std.json/read)
           (mapv std.string/upper-case))))

(defn.js get-names
  [filt]
  (var output [])
  (xt/for:array [n -/NAMES]
    (when (j/startsWith n (j/toUpperCase filt))
      (x:arr-push output {:name n}))
    (when (< 15 (xt/x:len output))
      (return output)))
  (return output))
  

^{:refer melbourne.ui-autocomplete/SelectComponentEmpty :added "4.0"}
(fact "default empty component")

^{:refer melbourne.ui-autocomplete/SelectComponentBusy :added "4.0"}
(fact "default busy component")

^{:refer melbourne.ui-autocomplete/SelectComponentEntry :added "4.0"}
(fact "default entry component")

^{:refer melbourne.ui-autocomplete/SelectSingle :added "4.0"}
(fact "creates a single select autocomplete"
  ^:hidden
  
  (defn.js SelectSingleDemo
    []
    (var view    (ext-view/makeView
                  {:handler (fn:> [filt]
                              (jc/future-delayed [300]
                                (return (-/get-names filt))))
                   :defaultOutput []}))
    (var [selected
          setSelected] (r/local))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-autocomplete/SelectSingle"} 
[:% n/Isolation
       [:% ui-autocomplete/SelectSingle
        #{selected
          setSelected
          {:source {:key-fn data/id-fn
                    :val-fn lib/identity
                    :view view}}}]])))

  (def.js MODULE (!:module)))
