(ns script.sql.table.select-test
  (:require [script.sql.common :as common]
            [script.sql.table.select :refer :all]
            [std.string.common])
  (:use code.test))

^{:refer script.sql.table.select/build-query-columns :added "3.0"}
(fact "build column string"

  (build-query-columns [:id :value] {:column-fn identity})
  => "id, value")

^{:refer script.sql.table.select/build-query-select :added "3.0"}
(fact "build query string"

  (build-query-select "select" nil {})
  => "select *"

  (build-query-select "select" [:id :value] {})
  => "select id, value")

^{:refer script.sql.table.select/build-query-from :added "3.0"}
(fact "build query from string"

  (build-query-from "" :users {})
  => " FROM users")

^{:refer script.sql.table.select/build-query-where-string :added "3.0"}
(fact "build query where string"

  (build-query-where-string [{:id "a"} {:value "data"}] {})
  => "id = 'a' OR value = 'data'")

^{:refer script.sql.table.select/build-query-where :added "3.0"}
(fact "build query where"

  (build-query-where "" [{:id "a"} {:value "data"}] {})
  => " WHERE id = 'a' OR value = 'data'")

^{:refer script.sql.table.select/build-query-order-by :added "3.0"}
(fact "build query order by"

  (build-query-order-by "" [:id] :desc {})
  => " ORDER BY id DESC")

^{:refer script.sql.table.select/build-query-group-by :added "3.0"}
(fact "build query group by"

  (build-query-group-by "" [:id] {})
  => " GROUP BY id")

^{:refer script.sql.table.select/build-query-limit :added "3.0"}
(fact "build query limit"

  (build-query-limit "" 20 {})
  => " LIMIT 20")

^{:refer script.sql.table.select/build-query-offset :added "3.0"}
(fact "build query offset"

  (build-query-offset "" 20 {})
  => " OFFSET 20")

^{:refer script.sql.table.select/sql:query :added "3.0"}
(fact "builds select query" ^:hidden

  (sql:query
   {:select [:id :name :country-id]
    :from :region-city
    :where [{:state-id 1}
            {:country-id "AU"}]
    :limit 10
    :offset 10
    :order-by [:name]}
   common/*options*)
  => (->> ["SELECT \"id\", \"name\", \"country_id\""
           "FROM \"region_city\" WHERE \"state_id\" = '1' OR \"country_id\" = 'AU'"
           "ORDER BY \"name\" ASC LIMIT 10 OFFSET 10"]
          (std.string.common/join " ")))
