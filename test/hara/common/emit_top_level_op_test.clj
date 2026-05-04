(ns hara.common.emit-top-level-op-test
  (:require [hara.common.emit :as emit]
            [hara.common.emit-common :as common]
            [hara.common.emit-helper :as helper]
            [hara.common.emit-top-level :as top-level]
            [hara.common.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer hara.common.emit/emit-main :adopt true :added "4.0"}
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
