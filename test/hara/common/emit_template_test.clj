(ns hara.common.emit-template-test
  (:use code.test)
  (:require [hara.common.emit-template :refer :all]
            [hara.model.spec-js :as js]))

^{:refer hara.common.emit-template/entry-reserved :added "4.1"}
(fact "gets the reserved grammar entry for a code entry"
  (entry-reserved {:reserved {:add {:emit :macro}}}
                  {:op :add})
  => {:emit :macro}

  (entry-reserved {:reserved {:add {:emit :macro}}}
                  {:op :sub})
  => nil)

^{:refer hara.common.emit-template/materialize-code-entry :added "4.1"}
(fact "returns the entry unchanged when it cannot be materialized"
  (materialize-code-entry {:grammar {}
                           :modules {:math {}}}
                          {:op :add})
  => {:op :add}

  (materialize-code-entry {:grammar {:reserved {:add {:emit :macro}}}
                           :modules nil}
                          {:op :add})
  => {:op :add})

(def +js-entry+
  {:op 'defn
   :form-input '(defn foo [x] x)
   :module 'JS.core
   :lang :js})

(def +js-modules+
  {'JS.core {:id 'JS.core :lang :js :link '{- JS.core}}})

^{:refer hara.common.emit-template/create-code-state :added "4.1"}
(fact "hydrates and stages a code entry for the current grammar"
  (let [reserved (get-in js/+grammar+ [:reserved 'defn])]
    (create-code-state +js-entry+ reserved js/+grammar+ +js-modules+))
  => (contains {:form '(defn foo [x] x)
                :deps #{}},
               :in-any-order)

  (let [reserved (get-in js/+grammar+ [:reserved 'defn])]
    (keys (create-code-state +js-entry+ reserved js/+grammar+ +js-modules+)))
  => '(:hmeta :form :deps :deps-fragment :deps-native :xtalk-ops :xtalk-profiles :polyfill-modules))

^{:refer hara.common.emit-template/cached-code-state :added "4.1"}
(fact "restages a code entry using the per-entry cache"
  (let [reserved (get-in js/+grammar+ [:reserved 'defn])
        cache    (atom {})
        entry    (assoc +js-entry+ :static/code.cache cache)]
    (= (cached-code-state entry reserved js/+grammar+ +js-modules+)
       (cached-code-state entry reserved js/+grammar+ +js-modules+)))
  => true

  (let [reserved (get-in js/+grammar+ [:reserved 'defn])
        cache    (atom {})
        entry    (assoc +js-entry+ :static/code.cache cache)]
    (cached-code-state entry reserved js/+grammar+ +js-modules+)
    (count @cache))
  => 1)

^{:refer hara.common.emit-template/cached-entry-deps :added "4.1"}
(fact "returns the dependencies of a cached code entry"
  (let [reserved (get-in js/+grammar+ [:reserved 'defn])
        book     {:grammar js/+grammar+
                  :modules +js-modules+
                  :lang :js}]
    (cached-entry-deps book (assoc +js-entry+ :static/code.cache (atom {}))))
  => #{})

^{:refer hara.common.emit-template/code-state-computing? :added "4.1"}
(fact "returns true if the entry is currently being staged"
  (code-state-computing? {:lang :js :module 'M :id 'foo})
  => false

  (let [reserved {:hydrate (fn [_ _ context]
                             [nil (code-state-computing? (:entry context))])}]
    (:form (create-code-state {:op 'test
                               :form-input '(foo)
                               :module 'M
                               :lang :js
                               :id 'foo}
                              reserved
                              js/+grammar+
                              {})))
  => true)
