(ns postgres.core.variant
  "PostgreSQL declaration macros for distributed typed metadata."
  (:require [std.lib.foundation :as f]))

(defmacro defvariant.pg
  "Declares a class-table-specific JSONB field contract.

   The declaration is metadata only. It emits no PostgreSQL code; typed
   parsers and repository tooling retain the source form for validation and
   class-table-aware shape inference."
  [table selector field]
  (when-not (and (symbol? table)
                 (namespace table)
                 (seq (namespace table)))
    (f/error "defvariant.pg requires a qualified table symbol"
             {:table table}))
  (when-not (and (vector? selector)
                 (= 2 (count selector))
                 (= :class-table (first selector))
                 (string? (second selector)))
    (f/error "defvariant.pg requires [:class-table <literal-string>]"
             {:table table
              :selector selector}))
  (when-not (and (vector? field)
                 (= 2 (count field))
                 (keyword? (first field))
                 (map? (second field)))
    (f/error "defvariant.pg requires [:field-key <attrs-map>]"
             {:table table
              :field field}))
  nil)
