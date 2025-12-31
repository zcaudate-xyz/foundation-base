# Comprehensive Usage Guide for `deftype.pg`

This guide provides a detailed overview of `deftype.pg`, the macro used within the `std.lang` ecosystem to define PostgreSQL table schemas. It covers basic syntax, column configuration, relationships, and advanced settings.

## 1. Introduction

`deftype.pg` serves as the primary mechanism for defining data models in the `rt.postgres` runtime. It declaratively defines tables, columns, constraints, and relationships, which are then compiled into standard PostgreSQL `CREATE TABLE` statements. It integrates with the project's registry for type checking and reference resolution.

## 2. Basic Syntax

The basic structure of a `deftype.pg` form is:

```clojure
(deftype.pg TableName
  [column-1 {:key val ...}
   column-2 {:key val ...}]
  {table-options})
```

-   **TableName**: A symbol representing the table name.
-   **Column Vector**: A vector of alternating column names (symbols/keywords) and property maps.
-   **Table Options**: A map for table-level configuration (e.g., constraints, partitioning).

## 3. Column Definitions

Each column is defined by a property map.

### Core Properties

| Key | Description | Example |
| :--- | :--- | :--- |
| `:type` | The PostgreSQL type (e.g., `:text`, `:int`, `:uuid`, `:boolean`, `:jsonb`). | `{:type :text}` |
| `:primary` | Marks the column as a primary key. | `{:primary true}` |
| `:required` | Adds a `NOT NULL` constraint. | `{:required true}` |
| `:unique` | Adds a `UNIQUE` constraint. | `{:unique true}` |
| `:default` | Sets the default value. | `{:default "now()"}` |
| `:enum` | Specifies an enum type definition. | `{:type :enum :enum {:ns 'my.ns/MyEnum}}` |

### Example

```clojure
(deftype.pg User
  [:id    {:type :uuid :primary true}
   :name  {:type :text :required true}
   :role  {:type :enum :enum {:ns 'my.app/UserRole}}])
```

## 4. SQL Customization (`:sql`)

The `:sql` sub-map allows fine-grained control over the generated SQL for a specific column.

-   **`:cascade`**: Adds `ON DELETE CASCADE` (often used with foreign keys).
-   **`:check`**: Adds a column-level `CHECK` constraint.
-   **`:unique`**: If a string is provided, groups columns into a composite unique constraint.
-   **`:index`**: Creates an index for the column.
    -   `true`: Simple index.
    -   `{:using :gin :where ...}`: Advanced index configuration.

```clojure
(deftype.pg Item
  [:data {:type :jsonb :sql {:index {:using :gin}}}
   :age  {:type :int   :sql {:check (> age 0)}}])
```

## 5. Relationships (Foreign Keys)

Foreign keys are defined using the `:ref` type. The system automatically handles naming (e.g., appending `_id`) and validation.

### Syntax

```clojure
:owner {:type :ref
        :ref  {:ns 'target.namespace/TargetTable}}
```

### Features
-   **Automatic Naming**: A column named `:user` referencing `User` becomes `user_id` in SQL.
-   **Hydration**: Validates that the referenced namespace and table exist in the project registry.
-   **Cross-Module References**: Supports referencing tables in different schemas/modules.

## 6. Table Options

The third argument to `deftype.pg` handles table-wide settings.

### Constraints
Define named `CHECK` constraints.

```clojure
{:constraints {:check_positive_balance (> balance 0)}}
```

### Partitioning
Define table partitioning strategies.

```clojure
{:partition-by [:range :created_at]}
```

### Custom SQL
Append raw SQL fragments to the table definition.

```clojure
{:custom ["CONSTRAINT custom_rule ..."]}
```

## 7. Metadata Configuration

Behavior can be controlled via metadata on the table symbol.

-   **Schema**: `^{:static/schema "public"}` defines the PostgreSQL schema.
-   **Tracking**: `^{:track [...]}` adds audit columns (e.g., `created_at`, `updated_at`) via a tracking strategy (e.g., `rt.postgres.grammar.common-tracker/TrackingMin`).
-   **Lifecycle**: `^{:final true}` prevents `DROP TABLE IF EXISTS` generation (useful for production).
-   **Composition**: `^{:prepend [...] :append [...]}` mixes in column definitions from fragments.

## 8. Advanced Features

### Composite Keys
Defining multiple columns with `:primary true` creates a composite primary key.

### Composite Uniques
Use the `:sql {:unique "group_name"}` property on multiple columns to create a composite unique constraint.

### Indexes
Indexes are automatically collected and generated as separate `CREATE INDEX` statements.

## 9. Comprehensive Example

```clojure
(deftype.pg ^{:static/schema "app"
              :track [rt.postgres.grammar.common-tracker/TrackingMin]}
  Order
  [:id          {:type :uuid :primary true}
   :user        {:type :ref  :ref {:ns 'app/User} :sql {:cascade true}}
   :status      {:type :text :default "pending"}
   :items       {:type :jsonb :sql {:index {:using :gin}}}
   :total       {:type :numeric :required true}]
  {:constraints {:positive_total (> total 0)}})
```
