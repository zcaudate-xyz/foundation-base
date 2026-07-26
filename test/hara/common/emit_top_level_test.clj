tahto/common/emit_top_level_test.clj:1:(ns tahto.common.emit-top-level-test
tahto/common/emit_top_level_test.clj:2:  (:require [tahto.common.emit-common :as common]
tahto/common/emit_top_level_test.clj:3:            [tahto.common.emit-helper :as helper]
tahto/common/emit_top_level_test.clj:4:            [tahto.common.emit-top-level :refer :all]
tahto/common/emit_top_level_test.clj:5:            [tahto.common.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build-min [:macro-case])
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

tahto/common/emit_top_level_test.clj:15:^{:refer tahto.common.emit-top-level/transform-defclass-inner :added "4.0"}
(fact "transforms the body to be fn.inner and var.inner"

  (transform-defclass-inner '[(fn a [])
                              (fn b [])
                              (fn c [])])
  => '((fn.inner a []) (fn.inner b []) (fn.inner c [])))

tahto/common/emit_top_level_test.clj:23:^{:refer tahto.common.emit-top-level/emit-def :added "3.0"  }
(fact "creates the def string"

  (binding [common/*emit-fn* common/emit-common]
    (emit-def :def
              '(def hello (table 1 2 3))
              +grammar+
              {}))
  => "def hello = table(1,2,3);"

  (binding [common/*emit-fn* common/emit-common]
    (emit-def :defglobal
              '(defglobal hello (table 1 2 3))
              +grammar+
              {}))
  => "global hello = table(1,2,3);")

tahto/common/emit_top_level_test.clj:40:^{:refer tahto.common.emit-top-level/emit-declare :added "4.0"  }
(fact "emits declared "

  (emit-declare :def
                '(declare a b c)
                +grammar+
                {})
  => "def a,b,c")

tahto/common/emit_top_level_test.clj:49:^{:refer tahto.common.emit-top-level/emit-top-level :added "3.0" }
(fact "generic define form"

  (binding [common/*emit-fn* common/emit-common]
    (emit-top-level :defn
                    '(defn abc [a := 0]
                       (+ 1 2 3))
                    +grammar+
                    {}))
  => "function abc(a = 0){\n  1 + 2 + 3;\n}")

tahto/common/emit_top_level_test.clj:60:^{:refer tahto.common.emit-top-level/emit-form :added "4.0"}
(fact "creates a customisable emit and integrating both top-level and statements"
  ^:hiddn

  (emit-form :custom
             '(custom 1 2 3)
             (assoc-in +grammar+
                       [:reserved 'custom]
                       {:emit  (fn [_ _ _]
                                 'CUSTOM)})
             [])
  => 'CUSTOM)
