(ns js.lib.react-hook-form-test
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]
            [lang.core.impl :as impl]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [js.react :as r]
             [js.react.helper-jsdom :as helper]
             [js.lib.react-hook-form :as form]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.react-hook-form/useFormState :added "4.1"}
(fact "creates a form with default values"
  (helper-source/test
   (fn []
     (var controls (form/useFormState
                    {"defaultValues" {"name" "Ada"}}))
     (var getValues (. controls ["getValues"]))
     (var values (getValues))
     (return (r/createElement "span" nil
                              (. values ["name"]))))
   {}
   (fn [_ document _ _]
     (return document.body.innerHTML)))
  => "<div id=\"root\"><span>Ada</span></div>")

^{:refer js.lib.react-hook-form/useFormStateMap :added "4.1"}
(fact "passes a form state map through unchanged"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var input {"value" 1})
     (return {"map" (form/useFormStateMap input)
              "same" (== input (form/useFormStateMap input))})))
  => {"map" {"value" 1}
      "same" true})

^{:refer js.lib.react-hook-form/useControls :added "4.1"}
(fact "passes form controls through unchanged"
  (helper-source/test
   (fn [] (return nil))
   {}
   (fn [_ _ _ _]
     (var input {"value" 1})
     (return {"controls" (form/useControls input)
              "same" (== input (form/useControls input))})))
  => {"controls" {"value" 1}
      "same" true})

^{:refer js.lib.react-hook-form/mergeContexts :added "4.1"}
(fact "emits a JavaScript rest parameter and spreads contexts"
  (impl/emit-str
   '(defn mergeContexts
      [(:.. contexts)]
      (return (Object.assign {} (:.. contexts))))
   {:lang :js
    :layout :flat})
  => "function mergeContexts(...contexts){\n  return Object.assign({},...contexts);\n}")
