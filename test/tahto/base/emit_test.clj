(ns tahto.base.emit-test
  (:require [tahto.common.book-entry :as entry]
            [tahto.base.emit :as emit :refer :all]
            [tahto.base.emit-common :as common]
            [tahto.base.emit-helper :as helper]
            [tahto.base.emit-preprocess :as preprocess] [tahto.base.preprocess-base :as preprocess-base]
            [tahto.base.emit-prep-lua-test :as prep]
            [tahto.base.emit-rewrite :as rewrite]
            [tahto.base.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer tahto.base.emit/default-grammar :added "4.0"}
(fact "returns the default grammar"

  (emit/default-grammar)
  => map?)

^{:refer tahto.base.emit/emit-main-loop :added "4.0"}
(fact "creates the raw emit"

  (emit/emit-main-loop '(not (+ 1 2 3))
                      +grammar+
                      {})
  => "!((+ 1 2 3))")

^{:refer tahto.base.emit/emit-main :added "4.0"}
(fact "creates the raw emit with loop"

  (emit/emit-main '(not (+ 1 2 3))
                  +grammar+
                  {})
  => "!(1 + 2 + 3)")

^{:refer tahto.base.emit/emit :added "4.0"}
(fact "emits form to output string"

  (emit/emit '(+ 1 2 3)
             @+test-grammar+
             nil
             {})
  => "1 + 2 + 3")

^{:refer tahto.base.emit/with:emit :added "4.0"}
(fact "binds the top-level emit function to common/*emit-fn*"

  (emit/with:emit
   (common/*emit-fn* '(not (+ 1 2 3))
                     +grammar+
                     {}))
  => "!(1 + 2 + 3)")

^{:refer tahto.base.emit/prep-options :added "4.0"}
(fact "prepares the options for processing"

  (prep-options {})
  => vector?)

^{:refer tahto.base.emit/prep-form :added "4.0"}
(fact "prepares the form"

  (prep-form :raw '(+ 1 2 3) nil nil {})
  => '[(+ 1 2 3)]

  (prep-form :input '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3)]

  (prep-form :staging '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3) #{} #{} {}])

^{:refer tahto.base.emit/prep-form :added "4.1"
  :id test-prep-form-staging-rewrites}
(fact "runs staging rewrites after to-staging"
  (with-redefs [preprocess/to-input
                (fn [_]
                  'input-form)

                preprocess/to-staging
                (fn [form _ _ _]
                  (if (= form 'input-form)
                    ['staged-form #{:dep} #{:fragment} {:native :dep}]
                    (throw (ex-info "Unexpected pre-staging form" {:form form}))))

                rewrite/rewrite-stage
                (fn [stage form _ _]
                  (if (and (= :staging stage)
                           (= form 'staged-form))
                    'rewritten-form
                    (throw (ex-info "Rewrite saw wrong form" {:stage stage
                                                              :form form}))))]
    (prep-form :staging '(ignored) nil {:modules {}} {}))
  => '[rewritten-form #{:dep} #{:fragment} {:native :dep}])
