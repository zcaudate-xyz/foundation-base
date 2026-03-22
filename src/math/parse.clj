(ns math.parse
  (:require [std.lib.walk :as walk])
  (:import (org.scijava.parsington ExpressionParser SyntaxTree Operator Variable Function Group Token Operators)))

(defn parse-expr
  "parses string into SyntaxTree
 
   (parse-expr \"1 + 1\")
   => org.scijava.parsington.SyntaxTree"
  {:added "3.0"}
  ([expr]
   (-> (ExpressionParser.)
       (.parseTree expr))))

(defmulti to-clj
  "converts syntax tree into clojure
 
   (to-clj (parse-expr \"1+1\"))
   => '({:op \"+\"} 1 1)
 
   (to-clj (parse-expr \"sin((1+1))\"))
   => '(sin (:infix/splice ({:op \"+\"} 1 1)))"
  {:added "3.0"}
  type)

(defmethod to-clj :default
  ([t] t))

(defmethod to-clj Token
  ([^Token token]
   (symbol (.getToken token))))

(defmethod to-clj Operator
  ([^Operator token]
   {:op (.getToken token)}))

(defmethod to-clj Variable
  ([^Variable token]
   (symbol (.getToken token))))

(defmethod to-clj Group
  ([^Variable token]
   :infix/splice))

(defmethod to-clj SyntaxTree
  ([^SyntaxTree tree]
   (let [token  (.token tree)
         children (->> (.iterator tree)
                       (iterator-seq))]
     (cond (instance? Function token)
           (apply list (to-clj (first children))
                  (map to-clj (-> ^SyntaxTree (second children)
                                  (.iterator)
                                  (iterator-seq))))

           (empty? children)
           (to-clj token)

           :else
           (->> children
                (map to-clj)
                (apply list (to-clj token)))))))

(defn walk-postfix
  "fixes splice forms occuring in parsing
 
   (walk-postfix '(sin (:infix/splice ({:op \"+\"} 1 1))))
   => '(sin ({:op \"+\"} 1 1))"
  {:added "3.0"}
  ([form]
   (walk/postwalk (fn [form]
                    (cond (list? form)
                          (apply list
                                 (mapcat (fn [sym]
                                           (and (list? sym) (= :infix/splice (first sym)))
                                           (if (and (list? sym)
                                                    (= :infix/splice (first sym)))
                                             (rest sym)
                                             [sym]))
                                         form))

                          :else form))
                  form)))

(defn parse
  "parses string into clojure data structure
 
   (parse \"sin((1+1))\")
   => '(sin ({:op \"+\"} 1 1))
 
   (parse \"a+b*c^f(1,2)\")
   => '({:op \"+\"} a ({:op \"*\"} b ({:op \"^\"} c (f 1 2))))
 
   (parse \"a+b+c+d\")
   => '({:op \"+\"} ({:op \"+\"} ({:op \"+\"} a b) c) d)"
  {:added "3.0"}
  ([expr]
   (-> (parse-expr expr)
       (to-clj)
       (walk-postfix))))
