(ns xt.ui.state.model-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.event.base-listener :as listener]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.ui.state.model :as ui-model]]})

(defn.js create-store
  []
  (var node (substrate/node-create {"id" "ui-model-test"}))
  (page-core/group-add-attach
   node "app/account" "account/settings"
   {"draft" {"handler" (fn [_context value] (return value))
              "pipeline" {"check_args" (fn [context]
                                           (return [(xt/x:get-key context "input")]))
                          "check_disabled" (fn [_context] (return false))}
              "options" {}
              "defaults" {"args" [] "output" {}}}
    "save" {"handler" (fn [_context value] (return {"saved" value}))
            "pipeline" {"remote" {"handler" (fn [_context value]
                                                (return {"saved" value}))}}
            "options" {}
            "defaults" {"args" [] "output" {}}}})
  (return (ui-model/store-create node "app/account" "account/settings" "local" {})))

^{:refer xt.ui.state.model/listener-key :added "4.1"}
(fact "listener keys agree with the substrate's JSON keys for controlled identifiers"
  (!.js
   [(ui-model/listener-key "app/account" "account/settings" "draft")
    (== (ui-model/listener-key "app/account" "account/settings" "save")
        (xt/x:json-encode ["app/account" ["account/settings" "save"]]))])
  => ["[\"app/account\",[\"account/settings\",\"draft\"]]" true])

^{:refer xt.ui.state.model/store-create :added "4.1"}
(fact "stores preserve their identifiers and default to local mode with empty bookkeeping"
  (!.js (ui-model/store-create "node" "space" "group" nil nil))
  => {"node" "node" "space_id" "space" "group_id" "group" "mode" "local"
      "opts" {} "control" nil "revision" 0 "listeners" {}}
  (!.js
   (var store (ui-model/store-create "node" "space" "group" "proxy" {"remote" "peer"}))
   [(. store ["mode"]) (. store ["opts"])])
  => ["proxy" {"remote" "peer"}])

^{:refer xt.ui.state.model/store-version :added "4.1"}
(fact "reading a store version is stable until a subscribed model emits"
  (!.js
   (var store (-/create-store))
   (var before (ui-model/store-version store))
   (ui-model/subscribe! store "screen" (fn [_id _data _time _meta] (return nil)))
   (page-core/trigger-listeners (. store ["node"]) "app/account" ["account/settings" "draft"] {"type" "test"})
   [before (ui-model/store-version store) (ui-model/store-version store)])
  => [0 1 1])

^{:refer xt.ui.state.model/store-open :added "4.1"}
(fact "opening a local store validates the registered group and returns the same store"
  (notify/wait-on :js
    (var store (-/create-store))
    (promise/x:promise-then
     (ui-model/store-open store)
     (fn [opened]
       (repl/notify [(== store opened) (. opened ["mode"])
                     (ui-model/store-version opened) (. opened ["control"])]))))
  => [true "local" 0 nil]
  (!.js
   (var store (ui-model/store-create (substrate/node-create {"id" "missing-group"}) "space" "missing" nil nil))
   (var message nil)
   (try (ui-model/store-open store)
        (catch e (:= message (xt/x:ex-message e))))
   message)
  => "ERR - Group not found - missing")

^{:refer xt.ui.state.model/model :added "4.1"}
(fact "looks up the actual substrate model and reports a missing model precisely"
  (!.js
   (var store (-/create-store))
   (var pair (page-core/model-ensure (. store ["node"]) "app/account" "account/settings" "draft"))
   (var message nil)
   (try (ui-model/model store "missing")
        (catch e (:= message (xt/x:ex-message e))))
   [(== (ui-model/model store "draft") (xt/x:get-key pair 1)) message])
  => [true "ERR - Model not found - [\"account/settings\",\"missing\"]"])

^{:refer xt.ui.state.model/model-slot :added "4.1"}
(fact "slot fallbacks apply only to nil values, not zero, false or empty strings"
  (!.js
   (var store (-/create-store))
   (var current (ui-model/model store "draft"))
   (xt/x:set-key current "custom" {"zero" 0 "false" false "empty" "" "nil" nil})
   [(ui-model/model-slot store "draft" ["custom"] ["zero"] "fallback")
    (ui-model/model-slot store "draft" ["custom"] ["false"] "fallback")
    (ui-model/model-slot store "draft" ["custom"] ["empty"] "fallback")
    (ui-model/model-slot store "draft" ["custom"] ["nil"] "fallback")
    (ui-model/model-slot store "draft" ["absent"] nil "fallback")
    (ui-model/model-slot store "draft" nil ["custom" "zero"] "fallback")])
  => [0 false "" "fallback" "fallback" 0])

^{:refer xt.ui.state.model/model-input :added "4.1"}
(fact "reads the input current slot as a whole or by a nested path"
  (!.js
   (var store (-/create-store))
   (xt/x:set-key (ui-model/model store "draft") "input" {"current" {"profile" {"name" "Ada"}}})
   [(ui-model/model-input store "draft" nil {})
    (ui-model/model-input store "draft" ["profile" "name"] nil)
    (ui-model/model-input store "draft" ["profile" "missing"] "fallback")])
  => [{"profile" {"name" "Ada"}} "Ada" "fallback"])

^{:refer xt.ui.state.model/model-output :added "4.1"}
(fact "reads output current without confusing it with input current"
  (!.js
   (var store (-/create-store))
   (xt/x:set-key (ui-model/model store "draft") "output" {"current" {"count" 0}})
   [(ui-model/model-output store "draft" nil nil)
    (ui-model/model-output store "draft" ["count"] 9)
    (ui-model/model-output store "draft" ["missing"] 9)])
  => [{"count" 0} 0 9])

^{:refer xt.ui.state.model/model-pending? :added "4.1"}
(fact "pending reflects the model output flag rather than result truthiness"
  (!.js
   (var store (-/create-store))
   (var current (ui-model/model store "draft"))
   (xt/x:set-key current "output" {"current" "ready" "pending" true})
   (var pending (ui-model/model-pending? store "draft"))
   (xt/x:set-key (. current ["output"]) "pending" false)
   [pending (ui-model/model-pending? store "draft")])
  => [true false])

^{:refer xt.ui.state.model/model-disabled? :added "4.1"}
(fact "disabled reflects its own flag independently of pending"
  (!.js
   (var store (-/create-store))
   (var current (ui-model/model store "draft"))
   (xt/x:set-key current "output" {"disabled" true "pending" false})
   (var disabled (ui-model/model-disabled? store "draft"))
   (xt/x:set-key current "output" {"pending" true})
   [disabled (ui-model/model-disabled? store "draft")])
  => [true false])

^{:refer xt.ui.state.model/model-error :added "4.1"}
(fact "returns structured output only when the output is explicitly errored"
  (!.js
   (var store (-/create-store))
   (var current (ui-model/model store "draft"))
   (xt/x:set-key current "output" {"current" {"code" "DENIED"} "errored" false})
   (var healthy (ui-model/model-error store "draft"))
   (xt/x:set-key (. current ["output"]) "errored" true)
   [healthy (ui-model/model-error store "draft")])
  => [nil {"code" "DENIED"}])

^{:refer xt.ui.state.model/model-remote :added "4.1"}
(fact "remote reads are separate from output reads and preserve false results"
  (!.js
   (var store (-/create-store))
   (xt/x:set-key (ui-model/model store "draft") "remote" {"current" {"saved" false}})
   [(ui-model/model-remote store "draft" nil {})
    (ui-model/model-remote store "draft" ["saved"] true)
    (ui-model/model-remote store "draft" ["missing"] "fallback")])
  => [{"saved" false} false "fallback"])

^{:refer xt.ui.state.model/model-sync :added "4.1"}
(fact "sync reads expose the sync current slot and its nested defaults"
  (!.js
   (var store (-/create-store))
   (xt/x:set-key (ui-model/model store "draft") "sync" {"current" {"revision" 0}})
   [(ui-model/model-sync store "draft" nil {})
    (ui-model/model-sync store "draft" ["revision"] 99)
    (ui-model/model-sync store "draft" ["missing"] "fallback")])
  => [{"revision" 0} 0 "fallback"])

^{:refer xt.ui.state.model/set-input! :added "4.1"}
(fact "replaces the actual model input with the supplied draft"
  (notify/wait-on :js
    (var store (-/create-store))
    (promise/x:promise-then
     (ui-model/set-input! store "draft" {"name" "Ada" "active" false} nil)
     (fn [_]
       (repl/notify [(ui-model/model-input store "draft" nil {})
                     (ui-model/model-input store "draft" ["active"] true)]))))
  => [{"name" "Ada" "active" false} false])

^{:refer xt.ui.state.model/patch-input! :added "4.1"}
(fact "patches drafts and reads standardized model slots without changing the previous draft"
  (notify/wait-on :js
    (var store (-/create-store))
    (promise/x:promise-then
     (ui-model/patch-input! store "draft" ["first_name"] "Ada" {})
     (fn [_]
       (repl/notify [(ui-model/model-input store "draft" ["first_name"] nil)
                     (ui-model/model-pending? store "draft")
                     (ui-model/model-error store "draft")]))))
  => ["Ada" false nil]
  (notify/wait-on :js
    (var store (-/create-store))
    (var original {"profile" {"name" "Ada" "city" "London"}})
    (xt/x:set-key (ui-model/model store "draft") "input" {"current" {"data" original}})
    (promise/x:promise-then
     (ui-model/patch-input! store "draft" ["profile" "name"] "Grace" nil)
     (fn [_]
       (repl/notify [original (ui-model/model-input store "draft" nil {})]))))
  => [{"profile" {"name" "Ada" "city" "London"}}
      {"profile" {"name" "Grace" "city" "London"}}])

^{:refer xt.ui.state.model/invoke! :added "4.1"}
(fact "invocation executes the remote handler and saves its domain result"
  (notify/wait-on :js
    (var store (-/create-store))
    (promise/x:promise-then
     (ui-model/invoke! store "save" [{"name" "Ada"}])
     (fn [_]
       (repl/notify [(ui-model/model-output store "save" nil nil)
                     (ui-model/model-error store "save")]))))
  => [{"saved" {"name" "Ada"}} nil])

^{:refer xt.ui.state.model/refresh! :added "4.1"}
(fact "refresh recomputes the local output from the current input"
  (notify/wait-on :js
    (var store (-/create-store))
    (xt/x:set-key (. (ui-model/model store "draft") ["input"]) "current" {"name" "Ada"})
    (promise/x:promise-then
     (ui-model/refresh! store "draft" nil)
     (fn [_]
       (repl/notify [(ui-model/model-output store "draft" nil nil)
                     (ui-model/model-pending? store "draft")
                     (ui-model/model-error store "draft")]))))
  => [{"name" "Ada"} false nil])

^{:refer xt.ui.state.model/invoke! :id proxy-intent-forwarding :added "4.1"}
(fact "proxy model intents retain arguments, save-output flags and default events"
  (!.js
   (var store (-/create-store))
   (var group (page-core/group-ensure (. store ["node"]) "app/account" "account/settings"))
   (xt/x:set-key group "proxy_dispatch"
                 (fn [op node space group args] (return [op space group args])))
   [(ui-model/invoke! store "save" nil)
    (ui-model/refresh! store "draft" nil)
    (ui-model/set-input! store "draft" {"name" "Ada"} nil)])
  => [["proxy-call" "app/account" "account/settings" ["save" [] true]]
      ["model-update" "app/account" "account/settings" ["draft" {}]]
      ["model-set-input" "app/account" "account/settings" ["draft" {"name" "Ada"} {}]]])

^{:refer xt.ui.state.model/subscribe! :added "4.1"}
(fact "subscribes to every model with exact callback metadata and removes listeners on close"
  (notify/wait-on :js
    (var store (-/create-store))
    (var captured [])
    (ui-model/subscribe! store "screen"
     (fn [id data t meta]
       (xt/x:arr-push captured [id (. data ["type"]) t (. meta ["model_id"]) (. data ["path"])])))
    (page-core/trigger-listeners (. store ["node"]) "app/account" ["account/settings" "draft"] {"type" "input" "time" 10})
    (page-core/trigger-listeners (. store ["node"]) "app/account" ["account/settings" "save"] {"type" "output" "time" 20})
    (promise/x:promise-then
     (ui-model/store-close store)
     (fn [_]
       (page-core/trigger-listeners (. store ["node"]) "app/account" ["account/settings" "draft"] {"type" "after-close"})
       (repl/notify [captured (ui-model/store-version store) (. store ["listeners"])]))))
  => [[["screen/draft" "input" 10 "draft" ["account/settings" "draft"]]
       ["screen/save" "output" 20 "save" ["account/settings" "save"]]] 2 {}])

^{:refer xt.ui.state.model/unsubscribe! :added "4.1"}
(fact "unsubscription removes only the requested subscriber and is idempotent"
  (!.js
   (var store (-/create-store))
   (var seen [])
   (ui-model/subscribe! store "a" (fn [_id _data _t _meta] (xt/x:arr-push seen "a")))
   (ui-model/subscribe! store "b" (fn [_id _data _t _meta] (xt/x:arr-push seen "b")))
   (var removed (ui-model/unsubscribe! store "a"))
   (var repeated (ui-model/unsubscribe! store "a"))
   (page-core/trigger-listeners (. store ["node"]) "app/account" ["account/settings" "draft"] {"type" "test"})
   [removed repeated seen (. store ["listeners"])
    (listener/list-keyed-listeners (. store ["node"]) (ui-model/listener-key "app/account" "account/settings" "save"))])
  => [true true ["b"] {"b" true} ["b/save"]])

^{:refer xt.ui.state.model/store-close :added "4.1"}
(fact "close removes all model subscriptions before invoking proxy control cleanup"
  (notify/wait-on :js
    (var store (-/create-store))
    (var seen [])
    (ui-model/subscribe! store "a" (fn [_id _data _t _meta] (xt/x:arr-push seen "a")))
    (ui-model/subscribe! store "b" (fn [_id _data _t _meta] (xt/x:arr-push seen "b")))
    (xt/x:set-key store "control"
                  {"close" (fn []
                             (xt/x:arr-push seen (. store ["listeners"]))
                             (return (promise/x:promise-run "closed")))})
    (promise/x:promise-then
     (ui-model/store-close store)
     (fn [closed]
       (page-core/trigger-listeners (. store ["node"]) "app/account" ["account/settings" "draft"] {"type" "test"})
       (repl/notify [closed seen
                     (listener/list-keyed-listeners (. store ["node"]) (ui-model/listener-key "app/account" "account/settings" "draft"))
                     (listener/list-keyed-listeners (. store ["node"]) (ui-model/listener-key "app/account" "account/settings" "save"))]))))
  => ["closed" [{}] [] []])
