tahto/common/emit_test.clj:1:(ns tahto.common.emit-test
  (:require [tahto.base.book-entry :as entry]
tahto/common/emit_test.clj:3:  	        [tahto.common.emit :as emit :refer :all]
tahto/common/emit_test.clj:4:  	        [tahto.common.emit-common :as common]
tahto/common/emit_test.clj:5:  	        [tahto.common.emit-helper :as helper]
tahto/common/emit_test.clj:6:            [tahto.common.emit-preprocess :as preprocess] [tahto.common.preprocess-base :as preprocess-base]
tahto/common/emit_test.clj:7:  	        [tahto.common.emit-prep-lua-test :as prep]
tahto/common/emit_test.clj:8:            [tahto.common.emit-rewrite :as rewrite]
tahto/common/emit_test.clj:9:  	        [tahto.common.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

tahto/common/emit_test.clj:19:^{:refer tahto.common.emit/default-grammar :added "4.0"}
(fact "returns the default grammar"

  (emit/default-grammar)
  => map?)

tahto/common/emit_test.clj:25:^{:refer tahto.common.emit/emit-main-loop :added "4.0"}
(fact "creates the raw emit"

  (emit/emit-main-loop '(not (+ 1 2 3))
                      +grammar+
                      {})
  => "!((+ 1 2 3))")

tahto/common/emit_test.clj:33:^{:refer tahto.common.emit/emit-main :added "4.0"}
(fact "creates the raw emit with loop"

  (emit/emit-main '(not (+ 1 2 3))
                  +grammar+
                  {})
  => "!(1 + 2 + 3)")

tahto/common/emit_test.clj:41:^{:refer tahto.common.emit/emit :added "4.0"}
(fact "emits form to output string"

  (emit/emit '(+ 1 2 3)
             @+test-grammar+
             nil
             {})
  => "1 + 2 + 3")

tahto/common/emit_test.clj:50:^{:refer tahto.common.emit/with:emit :added "4.0"}
(fact "binds the top-level emit function to common/*emit-fn*"

  (emit/with:emit
   (common/*emit-fn* '(not (+ 1 2 3))
                     +grammar+
                     {}))
  => "!(1 + 2 + 3)")

tahto/common/emit_test.clj:59:^{:refer tahto.common.emit/prep-options :added "4.0"}
(fact "prepares the options for processing"

  (prep-options {})
  => vector?)

tahto/common/emit_test.clj:65:^{:refer tahto.common.emit/prep-form :added "4.0"}
(fact "prepares the form"

  (prep-form :raw '(+ 1 2 3) nil nil {})
  => '[(+ 1 2 3)]

  (prep-form :input '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3)]

  (prep-form :staging '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3) #{} #{} {}])

tahto/common/emit_test.clj:77:^{:refer tahto.common.emit/prep-form :added "4.1"
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
