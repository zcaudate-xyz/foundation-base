(ns std.concurrent.request-command-test
  (:require [std.concurrent.request-command :refer :all])
  (:use [code.test :exclude [run]]))

^{:refer std.concurrent.request-command/format-input :added "3.0"}
(fact "helper for formatting command input")

^{:refer std.concurrent.request-command/format-output :added "3.0"}
(fact "helper for formatting command input")

^{:refer std.concurrent.request-command/run-request :added "3.0"}
(fact "extensible function for command templates")

^{:refer std.concurrent.request-command/req:run :added "3.0"}
(fact "runs a command")

^{:refer std.concurrent.request-command/req:command :added "3.0"}
(fact "constructs a command")

(comment
  (./import)
  {:type :single
   :arguments    [:key]
   :function     (fn [])
   :options      {:select [:format :namespace]}
   :format       {:input  (fn [input  {:keys [format] :as opts}])
                  :output (fn [output {:keys [format] :as opts}])}
   :process      {:chain  []
                  :post   []}}

  {:type :bulk
   :command (fn [key data]
              (for (in:xadd key id args)))
   :return  {:chain []
             :post  []}}

  {:type :transact
   :command (fn [])
   :return  {:chain []
             :post  []}}

  {:type :iterate
   :command (fn [])
   :return  {:chain []
             :post  []}}

  {:type :script
   :command (fn [])
   :return  {:chain []
             :post  []}})
