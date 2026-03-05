(ns bb.string.case
  (:require [bb.string.common :as common]))

(defn- ^String re-sub
  "substitute a pattern by applying a function

   (re-sub \"aTa\" +hump-pattern+ (fn [_] \"$\"))
   => \"$a\""
  {:added "3.0"}
  ([^String value pattern sub-func]
   (loop [matcher (re-matcher pattern value)
          result []
          last-end 0]
     (if (.find matcher)
       (recur matcher
              (conj result
                    (.substring value last-end (.start matcher))
                    (sub-func (re-groups matcher)))
              (.end matcher))
       (apply str (conj result (.substring value last-end)))))))

(def ^:private +hump-pattern+ #"[a-z0-9][A-Z]")
(def ^:private +non-camel-pattern+ #"[_| |\-][A-Za-z]")
(def ^:private +non-snake-pattern+ #"[ |\-]")
(def ^:private +non-spear-pattern+ #"[ |\_]")
(def ^:private +non-dot-pattern+ #"[ |\-\_]")

(defn- ^String separate-humps
  "separate words that are camel cased

   (separate-humps \"aTaTa\")
   => \"a Ta Ta\""
  {:added "3.0"}
  ([^String value]
   (re-sub value +hump-pattern+ #(common/joinl (seq %) " "))))

(defn ^String camel-case
  "converts a string-like object to camel case representation

   (camel-case \"hello-world\")
   => \"helloWorld\"

   ((wrap camel-case) 'hello_world)
   => 'helloWorld"
  {:added "3.0"}
  ([^String value]
   (let [s (re-sub value
                   +non-camel-pattern+
                   (fn [s] (common/upper-case (apply str (rest s)))))]
     (str (.toLowerCase (subs s 0 1))
          (subs s 1)))))


(defn ^String upper-camel-case
  "convert string to uppercase camel

   (upper-camel-case \"hello-world\")
   => \"HelloWorld\""
  {:added "4.0"}
  ([^String value]
   (let [s (re-sub value
                   +non-camel-pattern+
                   (fn [s] (common/upper-case (apply str (rest s)))))]
     (str (.toUpperCase (subs s 0 1))
          (subs s 1)))))

(defn ^String capital-sep-case
  "converts a string-like object to captital case representation

   (capital-sep-case \"hello world\")
   => \"Hello World\"

   (str ((wrap capital-sep-case) :hello-world))
   => \":Hello World\""
  {:added "3.0"}
  ([^String value]
   (-> (separate-humps value)
       (common/split #"[ |\-|_]")
       (->> (map common/capital-case))
       (common/joinl " "))))

(defn ^String lower-sep-case
  "converts a string-like object to a lower case representation

   (lower-sep-case \"helloWorld\")
   => \"hello world\"

   ((wrap lower-sep-case) 'hello-world)
   => (symbol \"hello world\")"
  {:added "3.0"}
  ([^String value]
   (-> (separate-humps value)
       (common/split #"[ |\-|_]")
       (->> (map common/lower-case))
       (common/joinl " "))))

(defn ^String pascal-case
  "converts a string-like object to a pascal case representation

   (pascal-case \"helloWorld\")
   => \"HelloWorld\"

   ((wrap pascal-case) :hello-world)
   => :HelloWorld"
  {:added "3.0"}
  ([^String value]
   (let [s (re-sub value +non-camel-pattern+
                   (fn [s] (common/upper-case (apply str (rest s)))))]
     (str (.toUpperCase (subs s 0 1))
          (subs s 1)))))

(defn ^String phrase-case
  "converts a string-like object to snake case representation

   (phrase-case \"hello-world\")
   => \"Hello world\""
  {:added "3.0"}
  ([^String value]
   (let [s (lower-sep-case value)]
     (str (.toUpperCase (subs s 0 1))
          (subs s 1)))))

(defn ^String dot-case
  "creates a dot-case representation

   (dot-case \"hello-world\")
   => \"hello.world\"

   ((wrap dot-case) 'helloWorld)
   => 'hello.world"
  {:added "3.0"}
  ([value]
   (-> (separate-humps value)
       (common/lower-case)
       (common/replace +non-dot-pattern+ "."))))

(defn ^String snake-case
  "converts a string-like object to snake case representation

   (snake-case \"hello-world\")
   => \"hello_world\"

   ((wrap snake-case) 'helloWorld)
   => 'hello_world"
  {:added "3.0"}
  ([value]
   (-> (separate-humps value)
       (common/lower-case)
       (common/replace +non-snake-pattern+ "_"))))

(defn ^String spear-case
  "converts a string-like object to spear case representation

   (spear-case \"hello_world\")
   => \"hello-world\"

   ((wrap spear-case) 'helloWorld)
   => 'hello-world"
  {:added "3.0"}
  ([value]
   (-> (separate-humps value)
       (common/lower-case)
       (common/replace +non-spear-pattern+ "-"))))

(defn ^String upper-sep-case
  "converts a string-like object to upper case representation

   (upper-sep-case \"hello world\")
   => \"HELLO WORLD\"

   (str ((wrap upper-sep-case) 'hello-world))
   => \"HELLO WORLD\""
  {:added "3.0"}
  ([^String value]
   (-> (separate-humps value)
       (common/split #"[ |\-|_]")
       (->> (map common/upper-case))
       (common/joinl " "))))

(defn ^String typeless=
  "compares two representations

   (typeless= \"helloWorld\" \"hello_world\")
   => true

   ((wrap typeless=) :a-b-c \"a b c\")
   => true

   ((wrap typeless=) 'getMethod :get-method)
   => true"
  {:added "3.0"}
  ([x y]
   (= (lower-sep-case x)
      (lower-sep-case y))))
