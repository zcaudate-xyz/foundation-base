(ns rt.postgres.grammar.form-defn-hydrate-test
  (:require [rt.postgres.grammar.form-defn-hydrate :as hydrate]
            [std.lang.base.book-entry :as book])
  (:use code.test))

^{:refer rt.postgres.grammar.form-defn-hydrate/pg-defn-hydrate-hook :added "4.1"}
(fact "pg-defn-hydrate-hook does not attach eager infer metadata"
  (let [entry (book/book-entry {:lang :postgres
                                :module 'rt.postgres.script.test.scratch-v2
                                :namespace 'rt.postgres.script.test.scratch-v2
                                :section :code
                                :op 'defn
                                :id 'insert-entry
                                :form-input '(defn insert-entry
                                               [:text i-name :jsonb i-tags :jsonb o-op]
                                               (return nil))
                                :static/dbtype :function})
        out   (hydrate/pg-defn-hydrate-hook entry)]
    out => entry
    (:rt.postgres/infer out) => nil
    (:rt.postgres/infer-json out) => nil))
