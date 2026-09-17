(ns xt.ui.state.form-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.state.form :as form]]})

^{:refer xt.ui.state.form/clone :added "4.1"}
(fact "deep clones nested form values and defaults nil to an empty object"
  (!.js
   (var original {"profile" {"name" "Ada"}})
   (var copy (form/clone original))
   (xt/x:set-key (. copy ["profile"]) "name" "Grace")
   [original copy (form/clone nil)])
  => [{"profile" {"name" "Ada"}} {"profile" {"name" "Grace"}} {}])

^{:refer xt.ui.state.form/create :added "4.1"}
(fact "creates independent initial and draft snapshots with clean flags"
  (!.js (form/create nil nil))
  => {"initial" {} "draft" {} "validators" {} "errors" {} "touched" {}
      "dirty" false "valid" true "pending" false}
  (!.js
   (var values {"profile" {"name" "Ada"}})
   (var current (form/create values nil))
   (xt/x:set-key (. values ["profile"]) "name" "outside")
   (xt/x:set-key (. current ["draft"] ["profile"]) "name" "draft")
   [(. current ["initial"]) (. current ["draft"]) values])
  => [{"profile" {"name" "Ada"}} {"profile" {"name" "draft"}} {"profile" {"name" "outside"}}])

^{:refer xt.ui.state.form/validate-value :added "4.1"}
(fact "validators receive value and draft and stop after the first non-nil error"
  (!.js
   (var seen [])
   (var message
        (form/validate-value
         [(fn [value draft] (xt/x:arr-push seen [value (. draft ["other"])]) (return nil))
          (fn [_value _draft] (xt/x:arr-push seen "required") (return "Required"))
          (fn [_value _draft] (xt/x:arr-push seen "must not run") (return "Later"))]
         "" {"other" "context"}))
   [message seen (form/validate-value nil "anything" {})])
  => ["Required" [["" "context"] "required"] nil])

^{:refer xt.ui.state.form/validate! :added "4.1"}
(fact "validation replaces stale errors and reflects cross-field validation"
  (!.js
   (var current (form/create {"name" "" "confirmation" ""}
                 {"name" [(fn [value _draft] (return (:? (== value "") "Required" nil)))]
                  "confirmation" [(fn [value draft] (return (:? (== value (. draft ["name"])) nil "Mismatch")))]}))
   (var invalid (form/validate! current))
   (var errors (. current ["errors"]))
   (form/set-field! current ["name"] "Ada")
   (var mismatch (. current ["errors"]))
   (form/set-field! current ["confirmation"] "Ada")
   [invalid errors mismatch (. current ["valid"]) (. current ["errors"])])
  => [false {"name" "Required"} {"confirmation" "Mismatch"} true {}])

^{:refer xt.ui.state.form/set-field! :added "4.1"}
(fact "nested field updates copy the draft, mark touched paths and preserve initial values"
  (!.js
   (var current (form/create {"profile" {"name" "Ada" "city" "London"}} nil))
   (var before (. current ["draft"]))
   (var result (form/set-field! current ["profile" "name"] "Grace"))
   [before result (. current ["initial"]) (. current ["touched"])
    (. current ["dirty"]) (. current ["valid"])])
  => [{"profile" {"name" "Ada" "city" "London"}}
      {"profile" {"name" "Grace" "city" "London"}}
      {"profile" {"name" "Ada" "city" "London"}}
      {"profile" {"name" true}} true true])

^{:refer xt.ui.state.form/reset! :added "4.1"}
(fact "reset restores initial values and clears errors, touches and submission state"
  (!.js
   (var current (form/create {"name" "Ada"} nil))
   (form/set-field! current ["name"] "Grace")
   (xt/x:set-key current "errors" {"name" "Rejected"})
   (xt/x:set-key current "valid" false)
   (form/pending! current true)
   (var result (form/reset! current))
   [(== result current) current])
  => [true {"initial" {"name" "Ada"} "draft" {"name" "Ada"} "validators" {}
            "errors" {} "touched" {} "dirty" false "valid" true "pending" false}])

^{:refer xt.ui.state.form/pending! :added "4.1"}
(fact "submission flags only accept true and leave draft state intact"
  (!.js
   (var current (form/create {"name" "Ada"} nil))
   (form/pending! current true)
   (var pending (. current ["pending"]))
   (form/pending! current "yes")
   [pending (. current ["pending"]) (. current ["draft"]) (. current ["dirty"])])
  => [true false {"name" "Ada"} false])
