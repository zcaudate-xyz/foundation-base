(ns documentation.xt-db-text
  (:use code.test))

[[:hero {:title "xt.db.text"
         :subtitle "Schema, graph, tree, SQL, and PGREST builders."
         :lead "`xt.db.text` turns schema and query descriptions into portable database text: flattened schemas, scoped trees, SQL calls, raw SQL, tables, views, and PGREST graph/tree forms."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Database backends differ, but most application code wants to describe schemas, joins, scopes, and views once. The text layer is where those descriptions become concrete query strings or REST shapes."

[[:chapter {:title "Internal usage" :link "internal"}]]

"System and node layers depend on the text builders for backend-specific execution. Tests under `test-lang/xt/db/text` are the best source of examples for schema, SQL, tree, graph, and view behavior."

[[:chapter {:title "API" :link "api"}]]

