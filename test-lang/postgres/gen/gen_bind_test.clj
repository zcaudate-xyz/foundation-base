(ns postgres.gen.gen-bind-test
  (:require [postgres.sample.scratch-v1 :as scratch]
            [postgres.gen.gen-bind :as bind]
            [xt.db.helpers.seed-system-test :as sample-data])
  (:use code.test))

^{:refer postgres.gen.gen-bind/tmpl-route :added "4.0"}
(fact "creates a route template"

  (bind/tmpl-route '[addf scratch/addf])
  => '(def.xt addf {:input [{:symbol "x", :type "numeric"} {:symbol "y", :type "numeric"}],
                    :return "numeric",
                    :schema "scratch",
                    :id "addf",
                    :flags {},
                    :url "/addf"}))

^{:refer postgres.gen.gen-bind/tmpl-view :added "4.0"}
(fact "creates a view template"

  (bind/tmpl-view '[currency-all sample-data/currency-all])
  => '(def.xt currency-all
        {:input [],
         :return "jsonb",
         :schema "scratch-sample-db",
         :id "currency_all",
         :flags {:public true},
         :view
         {:table "Currency", :type "select", :tag "all", :query nil}}))

^{:refer postgres.gen.gen-bind/route-map :added "4.0"}
(fact "returns a map of routes")

^{:refer postgres.gen.gen-bind/route-list :added "4.0"}
(fact "lists all routes"

  (bind/route-list)
  => vector?)

^{:refer postgres.gen.gen-bind/view-list :added "4.0"}
(fact "lists all views"

  (bind/view-list)
  => vector?)
