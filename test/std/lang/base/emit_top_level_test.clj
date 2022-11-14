(ns std.lang.base.emit-top-level-test
  (:use code.test)
  (:require [std.lang.base.emit-top-level :refer :all]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammer :as grammer]
            [std.lib :as h]))

(def +reserved+
  (-> (grammer/build-min [:macro-case])
      (grammer/to-reserved)))

(def +grammer+
  (grammer/grammer :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-top-level/transform-defclass-inner :added "4.0"}
(fact "transforms the body to be fn.inner and var.inner"
  ^:hidden
  
  (transform-defclass-inner '[(fn a [])
                              (fn b [])
                              (fn c [])])
  => '((fn.inner a []) (fn.inner b []) (fn.inner c [])))

^{:refer std.lang.base.emit-top-level/emit-def :added "3.0"  }
(fact "creates the def string"
  ^:hidden

  (binding [common/*emit-fn* common/emit-common]
    (emit-def :def
              '(def hello (table 1 2 3))
              +grammer+
              {}))
  => "def hello = table(1,2,3);"

  (binding [common/*emit-fn* common/emit-common]
    (emit-def :defglobal
              '(defglobal hello (table 1 2 3))
              +grammer+
              {}))
  => "global hello = table(1,2,3);")

^{:refer std.lang.base.emit-top-level/emit-declare :added "4.0"  }
(fact "emits declared "

  (emit-declare :def
                '(declare a b c)
                +grammer+
                {})
  => "def a,b,c")

^{:refer std.lang.base.emit-top-level/emit-top-level :added "3.0" }
(fact "generic define form"

  (binding [common/*emit-fn* common/emit-common]
    (emit-top-level :defn
                    '(defn abc [a := 0]
                       (+ 1 2 3))
                    +grammer+
                    {}))
  => "function abc(a = 0){\n  1 + 2 + 3;\n}")

^{:refer std.lang.base.emit-top-level/emit-form :added "4.0"}
(fact "creates a customisable emit and integrating both top-level and statements"
  ^:hiddn
  
  (emit-form :custom
             '(custom 1 2 3)
             (assoc-in +grammer+
                       [:reserved 'custom]
                       {:emit  (fn [_ _ _]
                                 'CUSTOM)})
             [])
  => 'CUSTOM)
