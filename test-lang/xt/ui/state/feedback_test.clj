(ns xt.ui.state.feedback-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.state.feedback :as feedback]]})

^{:refer xt.ui.state.feedback/create :added "4.1"}
(fact "creates an idle feedback state"
  (!.js (feedback/create))
  => {"pending" false "error" nil "message" nil "retryable" false})

^{:refer xt.ui.state.feedback/pending! :added "4.1"}
(fact "starting a request clears a previous error while stopping preserves it"
  (!.js
   (var current (feedback/create))
   (feedback/fail! current "offline" true)
   (feedback/pending! current false)
   (var stopped [(. current ["pending"]) (. current ["error"])])
   (feedback/pending! current true)
   [stopped (. current ["pending"]) (. current ["error"])])
  => [[false "offline"] true nil])

^{:refer xt.ui.state.feedback/fail! :added "4.1"}
(fact "failure exits pending state and preserves structured errors"
  (!.js
   (var current (feedback/create))
   (feedback/pending! current true)
   (feedback/fail! current {"code" "OFFLINE"} true)
   (var failed [(. current ["pending"]) (. current ["error"]) (. current ["retryable"])])
   (feedback/fail! current "denied" false)
   [failed (. current ["retryable"])])
  => [[false {"code" "OFFLINE"} true] false])

^{:refer xt.ui.state.feedback/clear! :added "4.1"}
(fact "clear removes all pending, error, message and retry flags"
  (!.js
   (var current (feedback/create))
   (feedback/fail! current "offline" true)
   (xt/x:set-key current "message" "Try again")
   (var result (feedback/clear! current))
   [(== result current) current])
  => [true {"pending" false "error" nil "message" nil "retryable" false}])
