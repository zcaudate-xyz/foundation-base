(ns xtalk.lib.db-test
  (:require [std.lang :as l]
            [xtalk.lib.db.call]
            [xtalk.lib.db.check]
            [xtalk.lib.db.sql])
  (:use code.test))

^{:refer xtalk.lib.db.check/is-uuid? :added "4.1"}
(fact "the new xtalk db check namespace resolves across emitters"
  ^:hidden

  (l/emit-as :lua
             '[(xtalk.lib.db.check/is-uuid? user_id)])
  => "xtalk.lib.db.check.is_uuidp(user_id)"

  (l/emit-as :python
             '[(xtalk.lib.db.check/is-uuid? user_id)])
  => "xtalk.lib.db.check.is_uuidp(user_id)"

  (l/emit-as :dart
             '[(xtalk.lib.db.check/is-uuid? user_id)])
  => "xtalk.lib.db.check.is_uuidp(user_id)")

^{:refer xtalk.lib.db.sql/encode-value :added "4.1"}
(fact "the new xtalk db sql namespace resolves across emitters"
  ^:hidden

  (l/emit-as :lua
             '[(xtalk.lib.db.sql/encode-value payload)])
  => "xtalk.lib.db.sql.encode_value(payload)"

  (l/emit-as :python
             '[(xtalk.lib.db.sql/encode-value payload)])
  => "xtalk.lib.db.sql.encode_value(payload)"

  (l/emit-as :dart
             '[(xtalk.lib.db.sql/encode-value payload)])
  => "xtalk.lib.db.sql.encode_value(payload)")

^{:refer xtalk.lib.db.call/call-format-query :added "4.1"}
(fact "the new xtalk db call namespace resolves across emitters"
  ^:hidden

  (l/emit-as :lua
             '[(xtalk.lib.db.call/call-format-query
                {"schema" "scratch"
                 "id" "addf"
                 "input" [{"type" "numeric"}
                          {"type" "jsonb"}]}
                [1 ["hello"]])])
  => "xtalk.lib.db.call.call_format_query({\n  schema='scratch',\n  id='addf',\n  input={{type='numeric'},{type='jsonb'}}\n},{1,{'hello'}})"

  (l/emit-as :python
             '[(xtalk.lib.db.call/call-format-query
                {"schema" "scratch"
                 "id" "addf"
                 "input" [{"type" "numeric"}
                          {"type" "jsonb"}]}
                [1 ["hello"]])])
  => "xtalk.lib.db.call.call_format_query({\n  \"schema\":\"scratch\",\n  \"id\":\"addf\",\n  \"input\":[{\"type\":\"numeric\"},{\"type\":\"jsonb\"}]\n},[1,[\"hello\"]])"

  (l/emit-as :dart
             '[(xtalk.lib.db.call/call-format-query
                {"schema" "scratch"
                 "id" "addf"
                 "input" [{"type" "numeric"}
                          {"type" "jsonb"}]}
                [1 ["hello"]])])
  => "xtalk.lib.db.call.call_format_query({\n  \"schema\":\"scratch\",\n  \"id\":\"addf\",\n  \"input\":[{\"type\":\"numeric\"},{\"type\":\"jsonb\"}]\n},[1,[\"hello\"]])")
