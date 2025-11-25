(ns rt.postgres.grammar.form-deftype-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-deftype :refer :all]
            [rt.postgres.grammar :as g]
            [rt.postgres.script.scratch :as scratch]
            [std.lang :as l]
            [rt.postgres.grammar.common :as common]
            [std.lang.base.library-snapshot :as snap]
            [std.lib :as h]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-enum-col :added "4.0"}
(fact "creates the enum column"
  (with-redefs [common/pg-linked-token (fn [& _] '(:enum))]
    (pg-deftype-enum-col [:col :attr] {} {}))
  => [:col :attr '(:enum)])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref-link :added "4.0"}
(fact "creates the ref entry for "
  ;; Requires book access mock
  )

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref-current :added "4.0"}
(fact "creates the ref entry for "
  (pg-deftype-ref-current :col {:current {:id "id" :schema "schema" :type :uuid}} {})
  => vector?)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref :added "4.0"}
(fact "creates the ref entry"
  ;; delegates
  )

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-col-sql :added "4.0"}
(fact "formats the sql on deftype"
  (pg-deftype-col-sql [] {:cascade true :default 1})
  => [:on-delete-cascade :default 1])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-col-fn :added "4.0"}
(fact "formats the column on deftype"
  (with-redefs [common/pg-type-alias (fn [x] x)]
    (pg-deftype-col-fn [:col {:type :uuid :primary true}] {}))
  => (contains [:uuid :primary-key]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-uniques :added "4.0"}
(fact "collect unique keys on deftype"
  (pg-deftype-uniques [[:col {:type :text :sql {:unique true}}]])
  => list?)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-indexes :added "4.0"}
(fact "create index statements"
  (pg-deftype-indexes [[:col {:type :text :sql {:index true}}]] "table")
  => vector?)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype :added "4.0"}
(fact "creates a deftype statement"
  (with-redefs [common/pg-full-token (fn [s sch] (str sch "." s))]
    (pg-deftype '(deftype ^{:static/schema "s"} t [] {})))
  => list?)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-fragment :added "4.0"}
(fact "parses the fragment contained by the symbol"
  ;; Requires resolving symbol
  )

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-format :added "4.0"}
(fact "formats an input form"
  (pg-deftype-format '(deftype t [:a {:type :int}] {}))
  => vector?)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-check-link :added "4.0"}
(fact "checks a link making sure it exists and is correct type"
  (with-redefs [snap/get-book (fn [& _] nil)
                std.lang.base.book/get-base-entry (fn [& _] {:static/dbtype :table})]
    (pg-deftype-hydrate-check-link nil {} :table))
  => true)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate :added "4.0"}
(fact "hydrates the form with linked ref information"
  ;; Complex
  )

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-hook :added "4.0"}
(fact "updates the application schema"
  ;; side effect on atom
  )


^{:refer rt.postgres.grammar.form-deftype/pg-deftype-primaries :added "4.0"}
(fact "gets the primary keys for deftype"
  (pg-deftype-primaries [{:id "a" :type :uuid} {:id "b" :type :uuid}])
  => list?)
