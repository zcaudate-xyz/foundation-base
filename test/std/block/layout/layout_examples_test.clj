(ns std.block.layout.layout-examples-test
  (:use code.test)
  (:require [std.block.layout :as layout]
            [std.block.construct :as construct]
            [std.block.base :as base]
            [std.string :as str]
            [std.lib :as h]))

(defn split-block [form]
  (str/split-lines (base/block-string (layout/layout-main form))))

^{:refer std.block.layout/layout-examples :added "4.0"}
(fact "Default Formatting Examples"

  (split-block '(defn calculate-sum
                  "Calculates the sum of two numbers."
                  [a b]
                  (+ a b)))
  => ["(defn calculate-sum"
      "  \"Calculates the sum of two numbers.\""
      "  [a b]"
      "  (+ a b))"]

  (split-block '(defn.pg create-user
                  "Creates a user in Postgres."
                  [db user-data]
                  (pg/insert! db :users user-data)))
  => ["(defn.pg create-user"
      "  \"Creates a user in Postgres.\""
      "  [db user-data]"
      "  (pg/insert! db"
      "    :users"
      "    user-data))"]

  (split-block '(let [a 10
                      b 20]
                  (println (+ a b))))
  => ["(let [a 10 b 20]"
      "  (println (+ a b)))"]

  (split-block '(h/with:component [sys system]
                  (start sys)
                  (run sys)))
  => ["(h/with:component [sys system]"
      "  (start sys)"
      "  (run sys))"]

  (split-block '(defn configure
                  [{:keys [host port] :as opts}]
                  (connect host port)))
  => ["(defn configure"
      "  [{:as opts :keys [host port]}]"
      "  (connect host port))"]

  (split-block '(def config
                  {:server {:host "localhost"
                            :port 8080}
                   :db     {:type "postgres"
                            :url  "jdbc:postgresql://..."}}))
  => ["(def config"
      "  {:server {:host \"localhost\""
      "            :port 8080}"
      "   :db     {:type \"postgres\""
      "            :url  \"jdbc:postgresql://...\"}})"]

  (split-block '(do-things
                 (first-step a b c)
                 (second-step d e f)))
  => ["(do-things (first-step a b c)"
      "           (second-step d e f))"]

  (split-block '(do-things
                 (first-step-complex
                  (sub-step-1 a)
                  (sub-step-2 b))))
  => ["(do-things"
      " (first-step-complex"
      "  (sub-step-1 a)"
      "  (sub-step-2 b)))"]

  ;; Long map destructuring (Pairing Test)
  (split-block '(defn foo
                  [{:keys [a b c d e f g h i j k l m n o p] :as opts}]
                  (println a)))
  => ["(defn foo"
      "  [{:keys [a"
      "           b"
      "           c"
      "           d"
      "           e"
      "           f"
      "           g"
      "           h"
      "           i"
      "           j"
      "           k"
      "           l"
      "           m"
      "           n"
      "           o"
      "           p]"
      "    :as opts}]"
      "  (println a))"])
