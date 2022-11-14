(ns lib.jdbc.error
  (:require [std.string :as str]))

(def +codes+
  (->> [["02" :no-data]
        ["07" :dynamic-sql-error]
        ["08" :connection-exception]
        ["0A" :feature-not-supported]
        ["21" :cardinality-violation]
        ["22" :data-exception]
        ["23" :integrity-constraint-violation]
        ["24" :invalid-cursor-state]
        ["25" :invalid-transaction-state]
        ["26" :invalid-sql-statement]
        ["28" :invalid-authorization-specification]
        ["2B" :dependent-privilege-descriptors-still-exist]
        ["2C" :invalid-character-set]
        ["2D" :invalid-transaction-termination]
        ["2E" :invalid-connection]
        ["33" :invalid-sql-descriptor]
        ["34" :invalid-cursor]
        ["35" :invalid-condition-number]
        ["3C" :ambiguous-cursor]
        ["3D" :invalid-catalog]
        ["3F" :invalid-schema]]
       (into {})))