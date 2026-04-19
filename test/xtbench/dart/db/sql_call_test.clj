(ns
 xtbench.dart.db.sql-call-test
 (:use code.test)
 (:require
  [rt.postgres :as pg]
  [std.lang :as l]
  [xt.lang.common-notify :as notify]))

(l/script-
 :postgres
 {:runtime :jdbc.client,
  :config {:dbname "test-scratch"},
  :require [[rt.postgres.test.scratch-v1 :as scratch]]})

^#:xtalk{:template true}
(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.lang.common-spec :as xt]
   [xt.lang.common-repl :as repl]
   [xt.db.sql-call :as call]
   [xt.sys.conn-dbsql :as driver]
   [js.lib.driver-postgres :as js-postgres]]})

(fact:global
 {:setup [(l/rt:restart) (l/rt:setup :postgres)],
  :teardown [(l/rt:teardown :postgres) (l/rt:stop)]})

^{:refer xt.db.sql-call/decode-return, :added "4.0"}
(fact
 "decodes the return value"
 ^{:hidden true}
 (!.dt
  (call/decode-return (xt/x:json-encode {:status "ok", :data 1}) nil))
 =>
 1)

^{:refer xt.db.sql-call/call-format-input, :added "4.0"}
(fact
 "formats the inputs"
 ^{:hidden true}
 (!.dt
  (call/call-format-input
   {:input [{:type "numeric"} {:type "jsonb"}]}
   [1 ["hello"]]))
 =>
 ["'1'" "'[\"hello\"]'"])

^{:refer xt.db.sql-call/call-format-query, :added "4.0"}
(fact
 "formats a query"
 ^{:hidden true}
 (!.dt
  (call/call-format-query (@! (pg/bind-function scratch/divf)) [1 2]))
 =>
 "SELECT \"scratch\".divf('1', '2');")
