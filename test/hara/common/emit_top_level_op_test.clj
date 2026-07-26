tahto/common/emit_top_level_op_test.clj:1:(ns tahto.common.emit-top-level-op-test
tahto/common/emit_top_level_op_test.clj:2:  (:require [tahto.common.emit :as emit]
tahto/common/emit_top_level_op_test.clj:3:            [tahto.common.emit-common :as common]
tahto/common/emit_top_level_op_test.clj:4:            [tahto.common.emit-helper :as helper]
tahto/common/emit_top_level_op_test.clj:5:            [tahto.common.emit-top-level :as top-level]
tahto/common/emit_top_level_op_test.clj:6:            [tahto.common.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

tahto/common/emit_top_level_op_test.clj:16:^{:refer tahto.common.emit/emit-main :adopt true :added "4.0"}
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
