(ns std.object.element.impl.type-test
  (:require [std.object.element.impl.type :as common]
            [std.object.query :as query])
  (:use code.test))

^{:refer std.object.element.impl.type/set-accessible :added "3.0"}
(fact "sets the accessible flag in the class to be true"

  (common/set-accessible (.getDeclaredMethod String "charAt"
                                             (doto ^"[Ljava.lang.Class;"
                                              (make-array Class 1)
                                               (aset 0 Integer/TYPE)))
                         true))

^{:refer std.object.element.impl.type/add-annotations :added "3.0"}
(fact "adds additional annotations to the class"

  (common/add-annotations {} String)
  => {})

^{:refer std.object.element.impl.type/seed :added "3.0"}
(fact "returns the preliminary attributes for creating an element"

  (common/seed :class String)
  => (contains {:name "java.lang.String",
                :tag :class,
                :modifiers #{:instance :public :final :class},
                :static false,
                :delegate java.lang.String})

  (common/seed :method (.getDeclaredMethod String "charAt"
                                           (doto ^"[Ljava.lang.Class;"
                                            (make-array Class 1)
                                             (aset 0 Integer/TYPE))))
  => (contains {:name "charAt",
                :tag :method,
                :container java.lang.String,
                :modifiers #{:instance :method :public}
                :static false,
                :delegate  java.lang.reflect.Method}))

(comment
  (code.manage/import)

  (.setAccessible
   (.getDeclaredMethod String "charAt"
                       (doto ^"[Ljava.lang.Class;"
                           (make-array Class 1)
                         (aset 0 Integer/TYPE)))
   true))
