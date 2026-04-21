(ns std.contract.type-test
  (:require [malli.core :as mc]
            [std.contract.sketch :as s]
            [std.contract.type :as t])
  (:use code.test))

^{:refer std.contract.type/check :added "3.0"}
(fact "checks that data fits the spec"

  (t/check (s/to-schema
            {:a number?})
           {:a 2})
  => {:a 2}

  (t/check (s/lax
            {:a number?})
           {:b 1})
  => {:b 1}


  (t/check (s/closed
            (s/lax
             {:a number?}))
           {:b 1})
  => (throws)

  (t/check (s/to-schema
            {:a 1})
           {:a 2})
  => (throws))

^{:refer std.contract.type/common-spec-invoke :added "3.0"}
(fact "invokes the common spec"

  (t/common-spec-invoke
   (t/common-spec {:a 1})
   {:a 1})
  => {:a 1}

  (t/common-spec-invoke
   (t/common-spec {:a 1})
   {:a 2})
  => (throws))

^{:refer std.contract.type/common-spec-string :added "3.0"}
(fact "displays the common spec"

  (t/common-spec-string
   (t/common-spec {:a 1}))
  => "#spec {:a 1}")

^{:refer std.contract.type/combine :added "3.0"}
(fact "combines spec schemas (usually maps)"

  (mc/form (t/combine {:a 1}
                      [{:b 2}]))
  => [:map [:a [:= 1]] [:b [:= 2]]])

^{:refer std.contract.type/common-spec :added "3.0"}
(fact "creates a common spec"

  (mc/form
   (s/to-schema (t/common-spec {:a 1}
                               {:b 2})))
  => [:map [:a [:= 1]] [:b [:= 2]]])

^{:refer std.contract.type/defspec :added "3.0"}
(fact "macro for defining a spec"

  (macroexpand-1
   '(t/defspec <hello>
      {:a 1}))
  => '(def <hello> (std.contract.type/common-spec {:a 1})))

^{:refer std.contract.type/multi-spec-invoke :added "3.0"}
(fact "invokes the multi spec"

  (t/multi-spec-invoke
   (t/multi-spec :type
                 [[:raw {:data [:or :string]}]])
   {:type :raw
    :data "hello"})
  => {:type :raw, :data "hello"}

  (t/multi-spec-invoke
   (t/multi-spec :type
                 [[:raw {:data [:or :string]}]])
   {:data "hello"})
  => (throws)

  (t/multi-spec-invoke
   (t/multi-spec :type
                 [[:raw {:data [:or :string]}]])
   {:data "hello"})
  => (throws))

^{:refer std.contract.type/multi-spec-string :added "3.0"}
(fact "displays the multi spec"

  (t/multi-spec-string
   (t/multi-spec :type
                 [[:raw {:data [:or :string]}]]))
  => "#spec.multi {:raw {:data [:or [:string]]}}")

^{:refer std.contract.type/multi-gen-final :added "3.0"}
(fact "generates the final schema for a multispec"

  (mc/form
   (t/multi-gen-final :type
                      [[:raw (s/to-schema {:data [:or :string]})]]))
  => [:multi {:dispatch :type} [:raw [:map [:data [:or :string]]]]])

^{:refer std.contract.type/multi-spec-add :added "3.0"}
(fact "adds additional types to the multi spec"

  (t/multi-spec-add
   (t/multi-spec :type
                 [])
   :raw (s/to-schema {:data [:or :string]}))
  => '(:raw)

  (mc/form
   (s/to-schema
    (doto (t/multi-spec :type
                        [])
      (t/multi-spec-add :raw (s/to-schema {:data [:or :string]})))))
  => [:multi {:dispatch :type} [:raw [:map [:data [:or :string]]]]])

^{:refer std.contract.type/multi-spec-remove :added "3.0"}
(fact "removes additional types from the multi spec"

  (t/multi-spec-remove
   (t/multi-spec :type
                 [[:raw {:data [:or :string]}]])
   :raw)
  => nil

  (mc/form
   (s/to-schema
    (doto (t/multi-spec :type
                        [[:raw (s/to-schema {:data [:or :string]})]])
      (t/multi-spec-remove :raw))))
  => [:multi {:dispatch :type}])

^{:refer std.contract.type/multi-spec :added "3.0"}
(fact "creates a multi spec"

  (mc/form
   (s/to-schema
    (t/multi-spec :type
                  [[:raw (s/to-schema {:data [:or :string]})]])))
  => [:multi {:dispatch :type} [:raw [:map [:data [:or :string]]]]])

^{:refer std.contract.type/defmultispec :added "3.0"}
(fact "macro for defining a multispec"

  (macroexpand-1
   '(t/defmultispec <hello> :type
      :raw (s/to-schema {:data [:or :string]})))
  => '(def <hello>
        (std.contract.type/multi-spec :type
                                      {:raw (s/to-schema {:data [:or :string]})})))

^{:refer std.contract.type/defcase :added "3.0"}
(fact "adds an additional case to the multispec"

  (macroexpand-1
   '(t/defcase <hello>
      :raw {:data [:or :string]}))
  => '(do (std.contract.type/multi-spec-add
           <hello>
           :raw (std.contract.type/combine {:data [:or :string]} []))
          (var <hello>)))

^{:refer std.contract.type/spec? :added "3.0"}
(fact "checks that object is of type spec"

  (t/spec? (t/multi-spec :type
                         [[:raw (s/to-schema {:data [:or :string]})]]))
  => true)

^{:refer std.contract.type/valid? :added "3.0"}
(fact "checks that data is valid"

  (t/valid? (t/multi-spec :type
                         [[:raw (s/to-schema {:data [:or :string]})]])

            {:type :raw
             :data "hello"})
  => true

  (t/valid? (t/multi-spec :type
                         [[:raw (s/to-schema {:data [:or :string]})]])

            {:data "hello"})
  => false)

(comment

  (defmultispec <hello> :type)

  (defcase <hello> :human {:type :human
                           :id string?})

  (<hello> {:type :human :id "oeuoeu"})

  (multi-spec :type
              {:human {:type :human
                       :data {:name string?
                              :age integer?}}})

  (-> (mc/schema [:map [:id string?]])
      (mu/closed-schema)
      (mc/explain {:idc 1})
      (me/with-spell-checking)
      (me/humanize)))
