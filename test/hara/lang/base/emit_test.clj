(ns hara.common.emit-test
  (:require [hara.lang.base.book-entry :as entry]
  	        [hara.common.emit :as emit :refer :all]
  	        [hara.common.emit-common :as common]
  	        [hara.common.emit-helper :as helper]
            [hara.common.emit-preprocess :as preprocess] [hara.common.preprocess-base :as preprocess-base]
  	        [hara.common.emit-prep-lua-test :as prep]
            [hara.common.emit-rewrite :as rewrite]
  	        [hara.common.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer hara.common.emit/default-grammar :added "4.0"}
(fact "returns the default grammar"

  (emit/default-grammar)
  => map?)

^{:refer hara.common.emit/emit-main-loop :added "4.0"}
(fact "creates the raw emit"

  (emit/emit-main-loop '(not (+ 1 2 3))
                      +grammar+
                      {})
  => "!((+ 1 2 3))")

^{:refer hara.common.emit/emit-main :added "4.0"}
(fact "creates the raw emit with loop"

  (emit/emit-main '(not (+ 1 2 3))
                  +grammar+
                  {})
  => "!(1 + 2 + 3)")

^{:refer hara.common.emit/emit :added "4.0"}
(fact "emits form to output string"

  (emit/emit '(+ 1 2 3)
             @+test-grammar+
             nil
             {})
  => "1 + 2 + 3")

^{:refer hara.common.emit/with:emit :added "4.0"}
(fact "binds the top-level emit function to common/*emit-fn*"

  (emit/with:emit
   (common/*emit-fn* '(not (+ 1 2 3))
                     +grammar+
                     {}))
  => "!(1 + 2 + 3)")

^{:refer hara.common.emit/prep-options :added "4.0"}
(fact "prepares the options for processing"

  (prep-options {})
  => vector?)

^{:refer hara.common.emit/prep-form :added "4.0"}
(fact "prepares the form"

  (prep-form :raw '(+ 1 2 3) nil nil {})
  => '[(+ 1 2 3)]

  (prep-form :input '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3)]

  (prep-form :staging '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3) #{} #{} {}])

^{:refer hara.common.emit/prep-form :added "4.1"}
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
