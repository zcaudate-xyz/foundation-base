(ns postgres.typed.typed-view-test
  (:require [postgres.typed.typed-view :as view])
  (:use code.test))

^{:refer postgres.typed.typed-view/view-entries :added "4.1"}
(fact "binds schema-backed select and return entries in stable order"
  (let [entries (view/view-entries ['postgres.sample.scratch-v0])]
    (mapv (fn [[sym descriptor]]
            [sym (:id descriptor) (:table descriptor)
             (cond (contains? descriptor :select-entry) :select
                   (contains? descriptor :return-entry) :return)])
          entries)
    =>
    '[[postgres.sample.scratch-v0/log-all "log_all" "Log" :select]
      [postgres.sample.scratch-v0/log-default "log_default" "Log" :return]]
    (get-in entries [1 1 :return-entry :input])
    =>
    [{:symbol "i_log_id" :type "uuid"}]))
