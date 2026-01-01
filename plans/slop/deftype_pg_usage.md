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

Foreign keys are defined using the `:ref` type. This powerful feature automates column typing, naming, and constraints.

### 5.1 Standard Reference
To reference another table defined with `deftype.pg`, use the symbol of that table.

```clojure
:owner {:type :ref
        :ref  {:ns 'my.app/User}}
```
*   **Result**: Creates a column named `owner_id` (not `owner`).
*   **Type**: Inherits the type of the `User` table's primary key (usually `:uuid`).
*   **Validation**: The macro verifies that `my.app/User` exists in the project registry.

### 5.2 Self Reference
To reference the current table (e.g., for parent/child hierarchies), use the `-` namespace alias.

```clojure
:parent {:type :ref
         :ref  {:ns '-/Category}} ;; Assumes current table is 'Category'
```

### 5.3 External/Manual Reference
To reference a table that exists in the database but is *not* defined in your `std.lang` project (or to avoid strict validation), use the vector syntax.

```clojure
:legacy_item {:type :ref
              :ref [:public :legacy_items :int]} ;; [schema table type]
```
*   **Schema**: `:public`
*   **Table**: `:legacy_items`
*   **Type**: `:int` (Explicit type required since it can't be looked up)

### 5.4 Cross-Module References
You can reference tables in other modules or schemas. The system handles the schema qualification in the generated SQL.

```clojure
:account {:type :ref
          :ref {:ns 'finance.schema/Account}} ;; Becomes "finance.account" in SQL
```

### 5.5 Cascading
To automatically delete rows when the referenced record is deleted, use `:sql {:cascade true}`.

```clojure
:user {:type :ref
       :ref {:ns 'app/User}
       :sql {:cascade true}} ;; Adds ON DELETE CASCADE
```

## 6. Table Options

The third argument to `deftype.pg` is a map that configures table-level behavior.

### 6.1 Named Constraints
Define `CHECK` constraints that apply to the table row. Keys become constraint names.

```clojure
{:constraints
 {:valid_range  (and (> start_date "2000-01-01") (< end_date "2100-01-01"))
  :check_balance (> balance 0)}}
```
*   **SQL**: `CONSTRAINT valid_range CHECK ((start_date > '2000-01-01') AND ...)`

### 6.2 Partitioning
`std.lang` supports declarative table partitioning.

```clojure
{:partition-by [:range :created_at]}
;; OR
{:partition-by [:list :region_code]}
```
*   **Syntax**: `[:method column1 column2 ...]`
*   **SQL**: `PARTITION BY RANGE (created_at)`

**Note on Foreign Keys**: Partitioned tables in Postgres require special handling for foreign keys. `deftype.pg` includes logic (`pg-deftype-partition-constraints`) to generate individual `FOREIGN KEY` constraints for partitions if needed.

### 6.3 Custom SQL
For features not directly supported by the DSL, use `:custom` to inject raw SQL strings or forms into the `CREATE TABLE` body.

```clojure
{:custom ["CONSTRAINT exclude_overlap EXCLUDE USING gist (ts WITH &&)"]}
```

### 6.4 Composite Primary Keys
To define a composite primary key, marking multiple columns with `:primary true` is supported but can be tricky. It is often clearer to use `:custom` or a constraint for complex primary keys if the default behavior is insufficient.

## 7. Metadata Configuration

Behavior can be controlled via metadata on the table symbol.

-   **Schema**: `^{:static/schema "public"}` defines the PostgreSQL schema.
-   **Tracking**: `^{:track [...]}` adds audit columns (e.g., `created_at`, `updated_at`) via a tracking strategy (e.g., `rt.postgres.grammar.common-tracker/TrackingMin`).
-   **Lifecycle**: `^{:final true}` prevents `DROP TABLE IF EXISTS` generation (useful for production).
-   **Composition**: `^{:prepend [...] :append [...]}` mixes in column definitions from fragments.

## 8. Advanced Features

### Composite Uniques
To enforce uniqueness across multiple columns, assign them the same string value in their `:unique` SQL property.

```clojure
[:org_id {:type :uuid :sql {:unique "org_user_idx"}}
 :email  {:type :text :sql {:unique "org_user_idx"}}]
```
*   **Result**: `UNIQUE (org_id, email)`

### Indexes
Indexes are automatically collected and generated as separate `CREATE INDEX` statements. Complex indexes (like GIN/GIST) are defined via the `:sql` map on the column.

## 9. Comprehensive Example

```clojure
(deftype.pg ^{:static/schema "app"
              :track [rt.postgres.grammar.common-tracker/TrackingMin]}
  Order
  [:id          {:type :uuid :primary true}
   :user        {:type :ref  :ref {:ns 'app/User} :sql {:cascade true}}
   :parent_order {:type :ref :ref {:ns '-/Order}} ;; Self-reference
   :status      {:type :text :default "pending"}
   :items       {:type :jsonb :sql {:index {:using :gin}}}
   :total       {:type :numeric :required true}]
  {:constraints {:positive_total (> total 0)}
   :partition-by [:range :created_at]})
```
