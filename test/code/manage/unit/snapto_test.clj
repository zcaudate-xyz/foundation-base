(ns code.manage.unit.snapto-test
  (:require [code.manage.unit.snapto :refer :all]
            [code.project :as project])
  (:use code.test))

(def +wrong-fact+
  "^{:refer xt.lang.common-spec/x:str-replace, :added \"4.1\"}
(fact
 \"replaces matching substrings\"
 (!.lua (xt/x:str-replace \"hello-world\" \"-\" \"/\"))
 =>
 \"hello/world\"
 (!.lua (xt/x:str-replace \"hello-world\" \"_\" \"/\"))
 =>
 \"hello-world\")")

(def +right-fact+
  "^{:refer xt.lang.common-spec/x:str-replace, :added \"4.1\"}
(fact \"replaces matching substrings\"
  (!.lua (xt/x:str-replace \"hello-world\" \"-\" \"/\"))
  => \"hello/world\"

  (!.lua (xt/x:str-replace \"hello-world\" \"_\" \"/\"))
  => \"hello-world\")")

(def +wrong-file+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       +wrong-fact+))

(def +right-file+
  (str "(ns example.core-test\n"
       "  (:use code.test))\n\n"
       +right-fact+))

^{:refer code.manage.unit.snapto/parse-body :added "4.1"}
(fact "partitions a fact body into plain forms and expression/check pairs"
  (parse-body '((+ 1 1)
                (odd? 3) => true))
  => '[{:type :form
        :expr (+ 1 1)}
       {:type :check
        :expr (odd? 3)
        :expected true}])

^{:refer code.manage.unit.snapto/snap-form-string :added "4.1"}
(fact "formats a single fact form into snap-to layout"
  (snap-form-string (read-string +wrong-fact+))
  => +right-fact+)

^{:refer code.manage.unit.snapto/snapto-string :added "4.1"}
(fact "formats all top-level fact forms in a test file"
  (snapto-string +wrong-file+)
  => +right-file+)

^{:refer code.manage.unit.snapto/snapto :added "4.1"}
(fact "formats fact tests into a consistent snap-to layout"
  (project/in-context (snapto {:write false}))
  => map?)
