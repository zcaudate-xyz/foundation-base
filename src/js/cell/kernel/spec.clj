(ns js.cell.kernel.spec
  (:require [std.lang.typed.xtalk :refer [defspec.xt]]))

(defspec.xt AnyMap
  [:xt/dict :xt/str :xt/any])

(defspec.xt AnyList
  [:xt/array :xt/any])

(defspec.xt StringList
  [:xt/array :xt/str])

(defspec.xt Path
  [:xt/array :xt/str])

(defspec.xt RequestFrame
  [:xt/record
   ["op" :xt/str]
   ["id" [:xt/maybe :xt/str]]
   ["action" [:xt/maybe :xt/str]]
   ["signal" [:xt/maybe :xt/str]]
   ["status" [:xt/maybe :xt/str]]
   ["async" [:xt/maybe :xt/bool]]
   ["body" :xt/any]
   ["meta" [:xt/maybe AnyMap]]])

(defspec.xt ResponseFrame
  [:xt/record
   ["op" :xt/str]
   ["id" [:xt/maybe :xt/str]]
   ["action" [:xt/maybe :xt/str]]
   ["signal" [:xt/maybe :xt/str]]
   ["status" [:xt/maybe :xt/str]]
   ["body" :xt/any]])

(defspec.xt LinkCallback
  [:xt/record
   ["key" :xt/str]
   ["pred" :xt/any]
   ["handler" [:fn [:xt/any :xt/any] :xt/any]]])

(defspec.xt LinkCallbackMap
  [:xt/dict :xt/str LinkCallback])

(defspec.xt ActiveCallEntry
  [:xt/record
   ["resolve" [:fn [:xt/any] :xt/any]]
   ["reject" [:fn [:xt/any] :xt/any]]
   ["input" :xt/any]
   ["time" :xt/int]])

(defspec.xt ActiveCallMap
  [:xt/dict :xt/str ActiveCallEntry])

(defspec.xt LinkRecord
  [:xt/record
   ["::" :xt/str]
   ["id" :xt/str]
   ["worker" :xt/any]
   ["active" ActiveCallMap]
   ["callbacks" LinkCallbackMap]])

(defspec.xt CellInit
  [:xt/record
   ["resolve" [:fn [:xt/any] :xt/any]]
   ["reject" [:fn [:xt/any] :xt/any]]
   ["current" :xt/any]])

(defspec.xt ViewSpec
  [:xt/record
   ["handler" [:xt/maybe :xt/any]]
   ["remote_handler" [:xt/maybe :xt/any]]
   ["pipeline" [:xt/maybe AnyMap]]
   ["default_args" [:xt/maybe :xt/any]]
   ["default_output" [:xt/maybe :xt/any]]
   ["default_process" [:xt/maybe :xt/any]]
   ["default_init" [:xt/maybe :xt/any]]
   ["trigger" [:xt/maybe :xt/any]]
   ["options" [:xt/maybe AnyMap]]])

(defspec.xt ViewRecord
  [:xt/record
   ["options" [:xt/maybe AnyMap]]
   ["deps" [:xt/maybe [:xt/array :xt/any]]]])

(defspec.xt ViewMap
  [:xt/dict :xt/str ViewRecord])

(defspec.xt ModelDeps
  [:xt/dict :xt/str [:xt/dict :xt/str [:xt/dict :xt/str :xt/bool]]])

(defspec.xt ViewDependents
  [:xt/dict :xt/str StringList])

(defspec.xt ModelRecord
  [:xt/record
   ["name" :xt/str]
   ["views" ViewMap]
   ["throttle" :xt/any]
   ["deps" ModelDeps]])

(defspec.xt ModelMap
  [:xt/dict :xt/str ModelRecord])

(defspec.xt CellRecord
  [:xt/record
   ["::" :xt/str]
   ["id" :xt/str]
   ["link" LinkRecord]
   ["models" ModelMap]
   ["init" :xt/any]])

(defspec.xt WorkerState
  [:xt/record
   ["eval" :xt/bool]
   ["final" [:xt/maybe :xt/bool]]])

(defspec.xt WorkerActionEntry
  [:xt/record
   ["handler" :xt/any]
   ["is_async" :xt/bool]
   ["args" StringList]])

(defspec.xt WorkerActionMap
  [:xt/dict :xt/str WorkerActionEntry])

(defspec.xt MockWorkerRecord
  [:xt/record
   ["::" :xt/str]
   ["listeners" AnyList]
   ["post_message" [:xt/maybe [:fn [:xt/any] :xt/any]]]
   ["post_request" [:xt/maybe [:fn [:xt/any] :xt/any]]]])
