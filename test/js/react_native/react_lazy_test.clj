(ns js.react-native.react-lazy-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]
            [js.cell.playground :as browser]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react-native.ui-button :as ui-button]]
   :static {:import/async #{js.react-native.ui-button}}
   })

^{:refer js.react/useLazy :adopt true :added "4.0" :unchecked true}
(fact "various lays of loading lazy function"
  ^:hidden

  (defn.js LazyView
    []
    (return
     [:% n/Text "LAZY VIEW LOADED"]))
  
  (defn.js UseLazyDemo
    []
    (var LazyButton (r/useLazy ui-button/Button))
    (var refresh    (r/useRefresh))
    (var getCount   (r/useGetCount))
    (var module 
         (j/module:async
          (j/future-delayed [100]
            (return
             {"__esMODULE" true
              :default {:lazyView -/LazyView}}))))
    (var LazyComponent (r/lazy (fn:> (j/future
                                       (return {"__esMODULE" true
                                                :default -/LazyView})))))
    (r/init []
      (j/future-delayed [500]
        (refresh)))
    (return
     (n/EnclosedCode 
{:label "js.react/useLazy"} 
[:% r/Suspense
       {:fallback [:% n/Text "LOADING"]}
       [:% LazyComponent]
       [:% LazyButton {:text "HELLO"}]
       (r/createElement (r/lazy (fn:> module.lazyView)))] 
[:% n/TextDisplay
       {:count (getCount)}])))
  
  )
