(ns std.lang.base.emit-test
  (:use code.test)
  (:require [std.lang.base.emit :as emit :refer :all]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.book-entry :as entry]
            [std.lib :as h]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit/default-grammar :added "4.0"}
(fact "returns the default grammar"

  (emit/default-grammar)
  => map?)

^{:refer std.lang.base.emit/emit-main-loop :added "4.0"}
(fact "creates the raw emit"

  (emit/emit-main-loop '(not (+ 1 2 3))
                      +grammar+
                      {})
  => "!((+ 1 2 3))")

^{:refer std.lang.base.emit/emit-main :added "4.0"}
(fact "creates the raw emit with loop"

  (emit/emit-main '(not (+ 1 2 3))
                  +grammar+
                  {})
  => "!(1 + 2 + 3)")

^{:refer std.lang.base.emit/emit :added "4.0"}
(fact "emits form to output string"
  ^:hidden
  
  (emit/emit '(+ 1 2 3)
             @+test-grammar+
             nil
             {})
  => "1 + 2 + 3")

^{:refer std.lang.base.emit/with:emit :added "4.0"}
(fact "binds the top-level emit function to common/*emit-fn*"

  (emit/with:emit
   (common/*emit-fn* '(not (+ 1 2 3))
                     +grammar+
                     {}))
  => "!(1 + 2 + 3)")

^{:refer std.lang.base.emit/prep-options :added "4.0"}
(fact "prepares the options for processing"
  ^:hidden

  (prep-options {})
  => vector?)

^{:refer std.lang.base.emit/prep-form :added "4.0"}
(fact "prepares the form"
  ^:hidden
  
  (prep-form :raw '(+ 1 2 3) nil nil {})
  => '[(+ 1 2 3)]

  (prep-form :input '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3)]
  
  (prep-form :staging '(+ @1 2 3) nil nil {})
  => '[(+ (!:eval 1) 2 3) #{} #{} {}])
