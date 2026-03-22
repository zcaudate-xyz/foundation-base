(ns script.sql
  (:require [script.sql.common :as common]
            [script.sql.expr :as expr]
            [script.sql.table :as table]
            [script.sql.table.compile :as compile]
            [script.sql.table.manage :as manage]
            [script.sql.table.select :as select]
            [std.lib.foundation :as f]))

(f/intern-in common/sql:compare
             common/sql:type
             common/sql:format

             compile/transform
             compile/in:fn-map
             compile/out:fn-map
             compile/transform:fn
             compile/transform:in
             compile/transform:out

             expr/for-cas
             expr/for-delete
             expr/for-insert
             expr/for-insert-multi
             expr/for-update
             expr/for-upsert
             expr/for-upsert-multi

             manage/table-create
             manage/table-drop
             manage/create:enums
             manage/create:tables
             manage/drop:enums
             manage/drop:tables
             manage/parse:aliases
             manage/parse:enums
             manage/parse:formats
             manage/parse:tables
             manage/parse:relationships

             [q select/sql:query]
             [sql:select select/sql:query]

             table/schema:ids
             table/schema:order
             
             table/sql:insert
             table/sql:delete
             table/sql:update
             table/sql:upsert
             table/sql:insert-multi
             table/sql:upsert-multi
             table/sql:cas

             table/table-compile
             table/table-common-options
             table/table:batch
             table/table:clear
             table/table:count
             table/table:delete
             table/table:get
             table/table:cas
             table/table:keys
             table/table:put:batch
             table/table:put:single
             table/table:query:id
             table/table:select
             table/table:set:batch
             table/table:set:single
             table/table:update
             table/table:delete)
