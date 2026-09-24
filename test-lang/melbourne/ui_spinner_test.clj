(ns melbourne.ui-spinner-test
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
             [js.react-native :as n]
             [melbourne.ui-spinner :as ui-spinner]]
   :export [MODULE]})

^{:refer melbourne.ui-spinner/SpinnerControls :added "0.1"}
(fact "creates spinner controls"
  ^:hidden

  (defn.js SpinnerControlsDemo
    []
    (var [value setValue] (r/local 155))
    (var [max min step decimal] [1000 0 2 2])
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-spinner/SpinnerControls"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/SpinnerValues
         #{value setValue max min step decimal 
           {:design {:type "light"
                    }}}]
        [:% n/Text " "]
        [:% ui-spinner/SpinnerControls
         #{value setValue max min step decimal 
           {:design {:type "light"
                    }}}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/SpinnerValues
         #{value setValue max min step decimal
           {:design {:type "dark"
                    }}}]
        [:% n/Text " "]
        [:% ui-spinner/SpinnerControls
         #{value setValue max min step decimal 
           {:design {:type "dark"
                    }}}]
       ]]))))

^{:refer melbourne.ui-spinner/SpinnerValues :added "0.1"}
(fact "creates only spinner values"
  ^:hidden
  
  (defn.js SpinnerValuesDemo
    []
    (var [value setValue] (r/local 155))
    (var [max min step decimal] [1000 0 2 2])
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-spinner/SpinnerValues"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/SpinnerValues
         #{value setValue max min step decimal 
           {:design {:type "light"
                    }}}]
        [:% n/Text " "]
        [:% ui-spinner/SpinnerValues
         #{value setValue max min step decimal
           {:design {:type "light"
                    :mode "secondary"}}}]
        [:% n/Text " "]
        [:% ui-spinner/SpinnerValues
         #{value setValue max min step decimal
           {:design {:type "light"
                    }}}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/SpinnerValues
         #{value setValue max min step decimal
           {:design {:type "dark"
                    }}}]
        [:% n/Text " "]
        [:% ui-spinner/SpinnerValues
         #{value setValue max min step decimal
           {:design {:type "dark"
                    :mode "secondary"}}}]
        [:% n/Text ""]
        [:% ui-spinner/SpinnerValues
         #{value setValue max min step decimal
           {:design {:type "dark"
                    }}}]]]))))

^{:refer melbourne.ui-spinner/Spinner :added "0.1"}
(fact "Creates a spinner"
  ^:hidden
  
  (defn.js SpinnerDemo
    []
    (var [value setValue] (r/local 155))
    (var [max min step decimal] [1000 0 2 2])
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-spinner/Spinner"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/Spinner
         #{value setValue max min step decimal 
           {:design {:type "light"
                    }}}]
        [:% n/Text " "]
        [:% ui-spinner/Spinner
         #{value setValue max min step decimal
           {:design {:type "light"
                    :mode "secondary"}}}]
        [:% n/Text " "]
        [:% ui-spinner/Spinner
         #{value setValue max min step decimal
           {:design {:type "light"
                    }}}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/Spinner
         #{value setValue max min step decimal
           {:design {:type "dark"
                    }}}]
        [:% n/Text " "]
        [:% ui-spinner/Spinner
         #{value setValue max min step decimal
           {:design {:type "dark"
                    :mode "secondary"}}}]
        [:% n/Text ""]
        [:% ui-spinner/Spinner
         #{value setValue max min step decimal
           {:design {:type "dark"
                    }}}]]])))
  
  (def.js MODULE (!:module))
  )
