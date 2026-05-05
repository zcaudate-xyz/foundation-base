(ns hara.model.sql.spec-common
  (:require [hara.model.sql.spec-common.common :as common]
            [hara.model.sql.spec-common.form-defenum :as form-defenum]
            [hara.model.sql.spec-common.form-defn :as form-defn]
            [hara.model.sql.spec-common.form-deftype :as form-deftype]
            [hara.lang.book :as book]
            [hara.common.emit :as emit]
            [hara.common.grammar :as grammar]
            [std.lib.collection :as collection]))

(def +dialect-sql+
  {:bool-literal {true "TRUE"
                  false "FALSE"}
   :comment-prefix "--"
   :enum-column-mode :native
   :enum-mode :native
   :function-before-body nil
   :function-prefix "CREATE FUNCTION"
   :function-return-keyword "RETURNS"
   :identifier-style :quoted
   :type-alias {:array "ARRAY"
                :bigint "BIGINT"
                :boolean "BOOLEAN"
                :double "DOUBLE"
                :float "FLOAT"
                :integer "INTEGER"
                :json "JSON"
                :keyword "TEXT"
                :long "BIGINT"
                :map "JSON"
                :numeric "NUMERIC"
                :object "JSON"
                :string "TEXT"
                :text "TEXT"
                :time "TIMESTAMP"
                :uuid "UUID"
                :void "VOID"}})

(defn build-features
  "creates common sql features for a dialect"
  {:added "4.1"}
  [dialect]
  (-> (grammar/build :exclude [:data-shortcuts
                               :control-try-catch
                               :class
                               :macro-arrow])
      (grammar/build:override
       {:eq   {:raw "="}
        :not  {:raw "NOT " :wrap true}
        :or   {:raw "OR" :wrap true}
        :and  {:raw "AND" :wrap true}
        :ret  {:raw "RETURN"}
        :defn {:format  #'form-defn/sql-defn-format
               :hydrate #'common/sql-hydrate
               :emit    #'form-defn/sql-defn
               :static/dbtype :function}})
      (grammar/build:extend
       {:defenum {:op :defenum :symbol '#{defenum}
                  :type :def :section :code
                  :format  #'form-defenum/sql-defenum-format
                  :hydrate #'common/sql-hydrate
                  :emit    #'form-defenum/sql-defenum
                  :static/dbtype :enum}
        :deftype {:op :deftype :symbol '#{deftype}
                  :type :def :section :code
                  :format  #'form-deftype/sql-deftype-format
                  :hydrate #'common/sql-hydrate
                  :emit    #'form-deftype/sql-deftype
                  :static/dbtype :table}})))

(defn build-template
  "creates a template for a sql dialect"
  {:added "4.1"}
  [dialect]
  (->> {:banned #{:regex}
        :dialect dialect
        :highlight '#{return break}
        :default {:comment  {:prefix (:comment-prefix dialect) :suffix ""}
                  :common   {:statement ";"}
                  :function {:raw ""}}
        :block   {:script {:start "" :end ""}}
        :token   {:string {:custom #'common/sql-string}
                  :symbol {:replace {\- "_" \? "p_"}}}}
       (collection/merge-nested (emit/default-grammar))))

(defn build-grammar
  "creates a grammar for a sql dialect"
  {:added "4.1"}
  [tag dialect]
  (grammar/grammar tag
                   (grammar/to-reserved (build-features dialect))
                   (build-template dialect)))

(defn build-meta
  "creates meta for a sql dialect"
  {:added "4.1"}
  []
  (book/book-meta {}))

(defn build-book
  "creates a concrete sql dialect book"
  {:added "4.1"}
  [lang tag dialect suffix]
  (book/book {:lang lang
              :meta (build-meta)
              :grammar (build-grammar tag dialect)
              :file {:suffix suffix}}))
