(ns std.object.framework.vector-like
  (:require [std.object.framework.print :as print]
            [std.protocol.object :as protocol.object]
            [std.object.framework.read :as read]
            [std.object.framework.write :as write]
            [std.lib :as h]))

(defmacro extend-vector-like
  "sets the fields of an object with keyword
 
   (extend-vector-like test.Cat {:read (fn [x] (seq (.getName x)))
                                 :write (fn [arr] (test.Cat. (apply str arr)))})
 
   (test.Cat. \"spike\")
   ;=> #test.Cat(\\s \\p \\i \\k \\e)"
  {:added "3.0"}
  ([cls {:keys [read write] :as opts}]
   (cond-> []
     read  (conj `(defmethod protocol.object/-meta-read ~cls
                    ([~'_]
                     ~(-> {:to-vector read}
                          (print/assoc-print-vars opts)))))
     write (conj `(defmethod protocol.object/-meta-write ~cls
                    ([~'_]
                     {:from-vector ~write})))

     true  (conj `(do (h/memoize:remove read/meta-read-exact ~cls)
                      (h/memoize:remove write/meta-write-exact ~cls)
                      (print/extend-print ~cls))))))
