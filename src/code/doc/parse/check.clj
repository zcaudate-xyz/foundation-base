(ns code.doc.parse.check
  (:require [code.query :as query]
            [std.block :as block]
            [std.block.navigate :as nav]))

(def directives
  #{:article :file :reference :ns
    :appendix :chapter
    :section :subsection :subsubsection
    :image :paragraph :code
    :equation :citation
    :html :api :manage})

(defn wrap-meta
  "helper function for navigating `^:meta` tags
 
   ((wrap-meta query/match) (nav/parse-string \"^:hello ()\") list?)
   => true"
  {:added "3.0"}
  ([f]
   (fn [nav selector]
     (if (= :meta (nav/tag nav))
       (f (-> nav nav/down nav/right) selector)
       (f nav selector)))))

(defn directive?
  "check if element is a directive
 
   (directive? (nav/parse-string \"[[:chapter {:title \\\"hello\\\"}]]\"))
   => true"
  {:added "3.0"}
  ([nav]
   ((wrap-meta query/match) nav {:pattern [[#'keyword? #'map?]]}))
  ([nav kw]
   ((wrap-meta query/match) nav {:pattern [[kw #'map?]]})))

(defn attribute?
  "check if element is an attribute
 
   (attribute? (nav/parse-string \"[[{:title \\\"hello\\\"}]]\"))
   => true"
  {:added "3.0"}
  ([nav]
   ((wrap-meta query/match) nav {:pattern [[#'map?]]})))

(defn code-directive?
  "check if element is a code directive
 
   (code-directive? (nav/parse-string \"[[:code {:lang \\\"python\\\"} \\\"1 + 1\\\"]]\"))
   => true"
  {:added "3.0"}
  ([nav]
   ((wrap-meta query/match) nav {:pattern [[:code map? string?]]})))

(defn ns?
  "check if element is a `ns` form
 
   (ns? (nav/parse-string \"(ns code.manage)\"))
   => true"
  {:added "3.0"}
  ([nav]
   ((wrap-meta query/match) nav {:form 'ns})))

(defn fact?
  "check if element is a `fact` form
 
   (fact? (nav/parse-string \"(fact 1 => 1)\"))
   => true"
  {:added "3.0"}
  ([nav]
   ((wrap-meta query/match) nav {:form 'fact})))

(defn facts?
  "check if element is a `facts` form
 
   (facts? (nav/parse-string \"(facts 1 => 1)\"))
   => true"
  {:added "3.0"}
  ([nav]
   ((wrap-meta query/match) nav {:form 'facts})))

(defn comment?
  "check if element is a `comment` form
 
   (comment? (nav/parse-string \"(comment 1 => 1)\"))
   => true"
  {:added "3.0"}
  ([nav]
   ((wrap-meta query/match) nav {:form 'comment})))

(defn deftest?
  "check if element is a `deftest` form
 
   (deftest? (nav/parse-string \"(deftest ...)\"))
   => true"
  {:added "3.0"}
  ([nav]
   ((wrap-meta query/match) nav {:form 'deftest})))

(defn is?
  "check if element is an `is` form
 
   (is? (nav/parse-string \"(is ...)\"))
   => true"
  {:added "3.0"}
  ([nav]
   ((wrap-meta query/match) nav {:form 'is})))

(defn paragraph?
  "check if element is a paragraph (string)
 
   (paragraph? (nav/parse-string \"\\\"hello world\\\"\"))
   => true"
  {:added "3.0"}
  ([nav]
   (string? (nav/value nav))))

(defn whitespace?
  "check if element is whitespace
 
   (whitespace? (nav/parse-string \" \"))
   => true"
  {:added "3.0"}
  ([nav]
   (let [b (nav/block nav)]
     (or (block/comment? b)
         (block/void? b)))))
