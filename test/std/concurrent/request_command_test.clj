(ns std.concurrent.request-command-test
  (:require [std.concurrent.request-command :refer :all])
  (:use [code.test :exclude [run]]))

^{:refer std.concurrent.request-command/format-input :added "3.0"}
(fact "helper for formatting command input"
  (format-input {:format {:input (fn [input opts]
                                   [input opts])}
                 :options {:select [:id]}}
                :hello
                {:id 1 :skip true})
  => [:hello {:id 1}])

^{:refer std.concurrent.request-command/format-output :added "3.0"}
(fact "helper for formatting command input"
  (format-output {:format {:output (fn [output opts]
                                     [output opts])}
                  :options {:select [:id]}}
                 :hello
                 {:id 1 :skip true})
  => [:hello {:id 1}])

^{:refer std.concurrent.request-command/run-request :added "3.0"}
(fact "extensible function for command templates"
  (with-redefs [std.concurrent.request/req-fn (fn [client command opts]
                                                [client command opts])]
    (run-request {:type :single
                  :function (fn [args _]
                              {:command args})}
                 :client
                 [:hello]
                 {:async true}))
  => [:client {:command [:hello]} {:async true}])

^{:refer std.concurrent.request-command/req:run :added "3.0"}
(fact "runs a command"
  (with-redefs [run-request (fn [_ client input opts]
                              [client input opts])]
    (let [[client input opts] (req:run {:options {:select [:id]
                                                  :input (fn [args]
                                                           {:id (:id args)})
                                                  :output (fn [_]
                                                            {:id 2})}
                                        :format {:input (fn [input opts]
                                                          [input opts])
                                                 :output (fn [output opts]
                                                           [output opts])}
                                        :process {:chain [inc]}}
                                       :client
                                       {:id 1 :value 2}
                                       {:extra true})]
      [client
       input
       (map? opts)
       (= [inc] (:chain opts))
       (= 1 (count (:post opts)))
       (fn? (first (:post opts)))]))
  => [:client [{:id 1, :value 2} {:id 1}] true true true true])

^{:refer std.concurrent.request-command/req:command :added "3.0"}
(fact "constructs a command"
  (req:command {:type :single
                :name :echo})
  => (contains {:type :single
                :name :echo}))

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
