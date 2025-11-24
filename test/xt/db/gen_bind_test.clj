(ns xt.db.gen-bind-test
  (:use code.test)
  (:require [xt.db.gen-bind :as bind]
            [xt.db.sample-user-test :as sample-user]
            [xt.db.sample-data-test :as sample-data]
            [rt.postgres.script.scratch :as scratch]))

^{:refer xt.db.gen-bind/tmpl-route :added "4.0"}
(fact "creates a route template"
  ^:hidden
  
  (bind/tmpl-route '[addf scratch/addf])
  => '(def.xt addf {:input [{:symbol "x", :type "numeric"} {:symbol "y", :type "numeric"}],
                    :return "numeric",
                    :schema "scratch",
                    :id "addf",
                    :flags {},
                    :url "/addf"}))

^{:refer xt.db.gen-bind/tmpl-view :added "4.0"}
(fact "creates a view template"
  ^:hidden
  
  (bind/tmpl-view '[currency-all sample-data/currency-all])
  
  => '(def.xt currency-all {:input [],
                            :return "jsonb",
                            :schema "scratch/xt.db.sample-data-test",
                            :id "currency_all",
                            :flags {:public true},
                            :view {:table "Currency",
                                   :type "select",
                                   :tag "all",
                                   :access {:query nil, :roles {}, :relation nil, :symbol nil},
                                   :query nil,
                                   :autos []
                                   :guards []}}))

^{:refer xt.db.gen-bind/route-list :added "4.0"}
(fact "lists all routes"
  ^:hidden
  
  (bind/route-list)
  => vector?)

^{:refer xt.db.gen-bind/view-list :added "4.0"}
(fact "lists all views"
  ^:hidden
  
  (bind/view-list)
  => vector?)




^{:refer xt.db.gen-bind/route-map :added "4.0"}
(fact "returns a map of routes")