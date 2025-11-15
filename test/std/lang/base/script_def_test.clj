(ns std.lang.base.script-def-test
  (:use code.test)
  (:require [std.lang.base.script-def :refer :all]
            [std.lib :as h]))

^{:refer  std.lang.base.script-def/tmpl-entry :added "4.0"}
(fact "forms for various argument types"
  ^:hidden
  
  (h/with:template-meta {:type :fragment
                         :tag "js"}
    (tmpl-entry '[createObject create]))
  => '(def$.js createObject create)

  (h/with:template-meta {:type :fragment
                         :base "Webassembly"
                         :prefix "ws-"
                         :tag "js"}
    (tmpl-entry 'compile))
  => '(def$.js ws-compile Webassembly.compile))

^{:refer  std.lang.base.script-def/tmpl-macro :added "4.0"}
(fact "forms for various argument types"
  ^:hidden

  (h/with:template-meta {:tag "js"}
    (tmpl-macro '[length [arr]  {:property true
                                 :tag "js"}]))
  => '(defmacro.js length
        ([obj] (clojure.core/list (quote .) obj (quote length))))
  
  (h/with:template-meta '{:tag "js"
                          :subtree [auth]}
    (tmpl-macro '[length [arr]  {:property true
                                 }]))
  => '(defmacro.js length ([obj] (clojure.core/list (quote .) obj (quote auth) (quote length))))
  
  (h/with:template-meta {:tag "js"}
    (tmpl-macro '[forEach [f]]))
  => '(defmacro.js forEach
        ([obj f]
         (clojure.core/list (quote .) obj (clojure.core/list (quote forEach) f))))

  (h/with:template-meta {:tag "js"}
    (tmpl-macro '[concat [arr]  {:vargs arrs}]))
  => '(defmacro.js concat
        ([obj arr & [:as arrs]]
         (clojure.core/list
          (quote .)
          obj
          (clojure.core/apply clojure.core/list (quote concat) arr arrs))))
  
  (h/with:template-meta {:tag "js"}
    (tmpl-macro '[includes   [val] {:optional [startIdx]}]))
  => '(defmacro.js includes
        ([obj val & [startIdx :as oargs]]
         (clojure.core/list
          (quote .) obj
          (clojure.core/apply
           clojure.core/list (quote includes) val
           (clojure.core/vec (clojure.core/take (clojure.core/count oargs) [startIdx]))))))
  
  (h/with:template-meta {:tag "js"}
    (tmpl-macro '[splice [start] {:optional [count e]
                                  :vargs es}]))
  => '(defmacro.js splice
        ([obj start & [count e & [:as es] :as targs]]
         (clojure.core/list
          (quote .) obj
          (clojure.core/apply
           clojure.core/list
           (quote splice) start
           (clojure.core/vec
            (clojure.core/concat (clojure.core/take
                                  (clojure.core/- (clojure.core/count targs) (clojure.core/count es))
                                  [count e])
                                 es)))))))
