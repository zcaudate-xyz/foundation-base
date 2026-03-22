(ns std.string.wrap
  (:require [std.lib.foundation :as h]
            [std.lib.invoke :refer [definvoke]]
            [std.string.case :as case]
            [std.string.coerce :as coerce]
            [std.string.common :as common]
            [std.string.path :as path]))

(defmulti wrap-fn
  "multimethod for extending wrap
 
   (let [f #(str '+ % '+)]
     (((wrap-fn f) f)
      :hello))
   => :+hello+"
  {:added "3.0"}
  identity)

(defmethod wrap-fn :default
  ([_]
   coerce/str:op))

(defmethod wrap-fn :return
  ([_]
   #(coerce/str:op % true)))

(defmethod wrap-fn :compare
  ([_]
   coerce/str:compare))

(def +defaults+
  {:compare  [clojure.core/=
              common/starts-with?
              common/ends-with?
              common/includes?
              common/caseless=
              case/typeless=]
   :return   [common/blank?
              path/path-count]
   :default  [common/escape
              common/lower-case
              common/upper-case
              common/capital-case
              common/joinl
              common/split
              common/split-lines
              common/replace
              common/reverse
              common/trim
              common/trim-left
              common/trim-right
              common/trim-newlines
              common/truncate

              case/camel-case
              case/capital-sep-case
              case/lower-sep-case
              case/pascal-case
              case/phrase-case
              case/snake-case
              case/spear-case
              case/upper-sep-case

              path/path-split
              path/path-join
              path/path-stem
              path/path-stem-array
              path/path-root
              path/path-ns
              path/path-ns-array
              path/path-val
              path/path-nth
              path/path-sub
              path/path-sub-array

              clojure.core/subs
              clojure.core/format]})

(def ^:dynamic *lookup*
  (reduce (fn [out [k arr]]
            (into out (map vector arr (repeat k))))
          {}
          +defaults+))

(defn join
  "extends common/join to all string-like types"
  {:added "3.0"}
  ([arr]
   (join arr h/*sep*))
  ([sep arr]
   (coerce/from-string (common/join sep (map coerce/to-string arr))
                       (type (first arr)))))

(defmethod wrap-fn common/join
  [_]
  join)

(definvoke wrap
  "enables string-like ops for more types
 
   ((wrap #(str '+ % '+)) 'hello)
   => '+hello+"
  {:added "3.0"}
  [:memoize]
  ([f]
   (let [wrapper (get *lookup* f)
         wrapper (if wrapper
                   (wrap-fn wrapper)
                   (wrap-fn f))]
     (wrapper f))))
