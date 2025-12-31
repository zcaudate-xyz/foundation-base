(ns rt.postgres.grammar.form-deftype-composite-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-deftype :as sut]
            [std.lib :as h]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as book]
            [rt.postgres.grammar.common :as common]))

(def mock-book
  {})

(def mock-entry
  {:static/schema "public"
   :form [nil nil {:id {:type :uuid} :type {:type :text}}]})

(defn mock-get-book [& _] mock-book)
(defn mock-get-base-entry [& _] mock-entry)

(fact "pg-deftype-ref-name respects raw"
  (sut/pg-deftype-ref-name :u {:raw "uid"})
  => "uid"
  (sut/pg-deftype-ref-name :u {})
  => "u_id")

(fact "pg-deftype-col-fn skips inline ref if group exists"
  (with-redefs [snap/get-book mock-get-book
                book/get-base-entry mock-get-base-entry
                common/pg-type-alias identity
                common/pg-base-token (fn [n s] (list 'token n s))]

    ;; With group -> No :references
    (sut/pg-deftype-col-fn [:a {:type :ref
                                :ref {:ns :N :group :g
                                      :link {:id :Target :lang :postgres}}}] {})
    => [#{"a_id"} :uuid]

    ;; Without group -> Has :references
    (let [res (sut/pg-deftype-col-fn [:a {:type :ref
                                          :ref {:ns :N
                                                :link {:id :Target :lang :postgres}}}] {})]
      (count res) => 4
      (let [refs (last res)]
        (first refs) => (list (list 'token #{"N"} "public") #{"id"})))))

(fact "pg-deftype-foreigns generates constraints"
  (with-redefs [snap/get-book mock-get-book
                book/get-base-entry mock-get-base-entry
                common/pg-base-token (fn [n s] (list 'token n s))]

    ;; Case 1: Composite FK via :group
    (let [res (sut/pg-deftype-foreigns 'tbl
                                       [[:a {:type :ref :ref {:ns :N :group :g :link {:id :Target :lang :postgres}}}]
                                        [:b {:type :ref :ref {:ns :N :group :g :link {:id :Target :lang :postgres} :column :other}}]]
                                       {:snapshot {}})
          def (first res)]
      (count res) => 1
      (first def) => '%
      (let [body (second def)
            [_ name _ cols _ refs] body]
        name => 'fk_tbl_g
        cols => (list 'quote '(a_id b_id))
        refs => (list (list 'token #{"Target"} "public") (list 'quote '(id other)))))

    ;; Case 2: Mixed :ref and :foreign
    (let [res (sut/pg-deftype-foreigns 'tbl
                                       [[:type {:type :text
                                                :foreign {:g1 {:ns :N :group :g1 :column :type :link {:id :Target :lang :postgres}}}}]
                                        [:ref_id {:type :ref
                                                  :ref {:ns :N :group :g1 :link {:id :Target :lang :postgres}
                                                        :raw "custom_id"}}]]
                                       {:snapshot {}})
          def (first res)]
      (count res) => 1
      (first def) => '%
      (let [body (second def)
            [_ name _ cols _ refs] body]
        name => 'fk_tbl_g1
        cols => (list 'quote '(type custom_id))
        refs => (list (list 'token #{"Target"} "public") (list 'quote '(type id)))))
    ))
