(ns std.lang.base.emit-top-level-op-test
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-top-level :as top-level]
            [std.lang.base.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit/emit-main :adopt true :added "4.0"}
(fact "emit do"

  (emit/emit-main '(defn hello []
                     (+ 1 2 3))

                  +grammar+
                  {})
  => "function hello(){\n  1 + 2 + 3;\n}"

  (emit/emit-main '(def hello
                     (+ 1 2 3))
                  +grammar+
                  {})
  => "def hello = 1 + 2 + 3;"

  (emit/emit-main '(defglobal hello
                     (+ 1 2 3))
                  +grammar+
                  {})
  => (throws)

  (emit/emit-main '(defrun __init__
                     (+ 1 2 3)

                     (+ 4 5 6))
                  +grammar+
                  {})
  => "1 + 2 + 3;\n4 + 5 + 6;")
