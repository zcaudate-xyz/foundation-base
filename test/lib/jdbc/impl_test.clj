(ns lib.jdbc.impl-test
  (:use code.test)
  (:require [lib.jdbc.impl :refer :all])
  (:import (java.net URI)
           (lib.jdbc.types Cursor)))

^{:refer lib.jdbc.impl/uri->dbspec :added "4.0"}
(fact 
  "Parses a dbspec as uri into a plain dbspec. This function
  accepts `java.net.URI` or `String` as parameter."
  (uri->dbspec (URI. "postgresql://user:pass@localhost:5432/db?k=v"))
  => {:subprotocol "postgresql"
      :subname "//localhost:5432/db"
      :user "user"
      :password "pass"
      :k "v"})

^{:refer lib.jdbc.impl/cursor->lazyseq :added "4.0"}
(fact "converts a cursor to a lazyseq"
  (with-redefs [lib.jdbc.resultset/result-set->lazyseq (constantly [])]
    (let [stmt (reify java.sql.PreparedStatement
                 (executeQuery [_] (reify java.sql.ResultSet))
                 (getConnection [_] (reify java.sql.Connection)))
          cursor (Cursor. stmt)]
      (cursor->lazyseq cursor {})))
  => [])
