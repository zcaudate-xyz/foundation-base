(ns script.sql.common-test
  (:require [script.sql.common :refer :all])
  (:use code.test))

^{:refer script.sql.common/sql:type :added "3.0"}
(fact "constructs sql type from `std.lib.schema` type"

  (sql:type :string)
  => :text)

^{:refer script.sql.common/sql:compare :added "3.0"}
(fact "implements equal comparator for more numbers"

  (sql:compare {:a 1}
               {:a (bigdec 1)})
  => true)

^{:refer script.sql.common/sql:parse :added "3.0"}
(fact "splits string into strings"

  (sql:parse "SELECT ? FROM ?")
  => ["SELECT " " FROM " ""])

^{:refer script.sql.common/sql:escape :added "3.0"}
(fact "escapes a string for sql usage"

  (sql:escape "aoe")
  => "aoe"

  (sql:escape "ao'e")
  => "ao''e")

^{:refer script.sql.common/sql:entry :added "3.0"}
(fact "constructs a quoted entry"

  (sql:entry "hello")
  => "'hello'")

^{:refer script.sql.common/sql:format :added "3.0"}
(fact "formats `?` placeholders with escaped arguments"

  (sql:format "SELECT * FROM table WHERE id = ?"
              "u-0")
  => "SELECT * FROM table WHERE id = 'u-0'")
