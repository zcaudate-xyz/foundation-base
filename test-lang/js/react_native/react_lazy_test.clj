(ns js.react-native.react-lazy-test
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
             [xt.lang.spec-base :as xt]
             [js.react-native.ui-button :as ui-button]]
   :static {:import/async #{js.react-native.ui-button}}
   })

^{:refer js.react/useLazy :adopt true :added "4.0" :unchecked true}
(fact "various lays of loading lazy function"

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
         (new Proxy
              {}
              {"get"
               (fn [target prop]
                 (return
                  (new Promise
                       (fn [resolve]
                         (setTimeout
                          (fn []
                            (resolve {"__esMODULE" true
                                      :default -/LazyView}))
                          100)))))}))
    (var LazyComponent (r/lazy (fn:> (new Promise
                                          (fn [resolve]
                                            (resolve {"__esMODULE" true
                                                      :default -/LazyView}))))))
    (r/init []
      (new Promise
           (fn [resolve]
             (setTimeout
              (fn []
                (resolve (refresh)))
              500))))
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
