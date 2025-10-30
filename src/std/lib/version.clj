(ns std.lib.version
  (:require [std.lib.foundation :as h]
            [std.lib.invoke :refer [definvoke]])
  (:refer-clojure :exclude [clojure-version]))

(defonce +pattern+
  (->> ["^"
        "(?:(\\d+)\\.)?"                          ;; major
        "(?:(\\d+)\\.)?"                          ;; minor
        "(\\*|\\d+)"                              ;; patch
        "(?:[-_]([\\w\\d-]+(?:\\.[\\w\\d]+)*))?"  ;; qualifier
        "(?:\\+([\\w\\d-]+(?:\\.[\\w\\d-]+)*))?"  ;; build
        "$"]
       (apply str)
       (re-pattern)))

(defonce +qualifiers+
  {"alpha"     0
   "beta"      1
   "milestone" 2
   "rc"        3
   "snapshot"  5
   "final"     6
   "stable"    6})

(defonce +order+
  [:major :minor :incremental :qualifier :release :build])

(defn parse-number
  "parse a number from string input
 
   (parse-number \"1\") => 1"
  {:added "3.0"}
  ([s]
   (if s (Long/parseLong s))))

(defn parse-qualifier
  "parses a qualifier from string input
 
   (parse-qualifier \"\" \"\") => 6
   
   (parse-qualifier \"alpha\" \"\") => 0"
  {:added "3.0"}
  ([release build]
   (let [qstring (cond (and (empty? release) (not (empty? build)))
                       build

                       (empty? release)
                       "stable"

                       :else
                       (first (keep #(if (.startsWith ^String release %)
                                       %)
                                    (keys +qualifiers+))))]
     (if qstring
       (get +qualifiers+ qstring -1)
       -1))))

(defn parse
  "parses a version input
   (parse \"1.0.0-final\")
   => {:major 1, :minor 0, :incremental 0, :qualifier 6, :release \"final\", :build \"\"}
 
   (parse \"1.0.0-alpha+build.123\")
   => {:major 1,
       :minor 0,
       :incremental 0,
       :qualifier 0,
       :release \"alpha\",
       :build \"build.123\"}"
  {:added "3.0"}
  ([s]
   (if-let [elems (first (re-seq +pattern+ s))]
     (let [elems  (rest elems)
           [major minor patch] (keep identity (take 3 elems))
           [release build]     (drop 3 elems)
           release (if release (.toLowerCase ^String release) "")
           build   (if build (.toLowerCase ^String build) "")]
       {:major (parse-number major)
        :minor (parse-number minor)
        :incremental (parse-number patch)
        :qualifier (parse-qualifier release build)
        :release release
        :build build})
     {:release s})))

(defn version
  "like parse but also accepts maps
 
   (version \"1.0-RC5\")
   => {:major 1, :minor 0, :incremental nil, :qualifier 3, :release \"rc5\", :build \"\"}"
  {:added "3.0"}
  ([x]
   (cond (string? x)
         (parse x)

         (map? x) x

         :else
         (throw (ex-info "Not a valid input" {:input x})))))

(def order (apply juxt +order+))

(defn equal?
  "compares if two versions are the same
 
   (equal? \"1.2-final\" \"1.2\")
   => true"
  {:added "3.0"}
  ([a b]
   (= (dissoc (version a) :release)
      (dissoc (version b) :release))))

(defn newer?
  "returns true if the the first argument is newer than the second
 
   (newer? \"1.2\" \"1.0\")
   => true
 
   (newer? \"1.2.2\" \"1.0.4\")
   => true"
  {:added "3.0"}
  ([a b]
   (pos? (compare (order (version a))
                  (order (version b))))))

(defn older?
  "returns true if first argument is older than the second
 
   (older? \"1.0-alpha\" \"1.0-beta\")
   => true
 
   (older? \"1.0-rc1\" \"1.0\")
   => true"
  {:added "3.0"}
  ([a b]
   (neg? (compare (order (version a))
                  (order (version b))))))

(definvoke not-equal?
  "returns true if first argument is not older than the second
 
   (not-equal? \"3.0\" \"3.0\")
   => false"
  {:added "3.0"}
  [:compose {:arglists '([a b])
             :val (comp not equal?)}])

(definvoke not-newer?
  "returns true if first argument is not older than the second
 
   (not-newer? \"3.0\" \"3.0\")
   => true"
  {:added "3.0"}
  [:compose {:arglists '([a b])
             :val (comp not newer?)}])

(definvoke not-older?
  "returns true if first argument is not older than the second
 
   (not-older? \"3.0\" \"3.0\")
   => true"
  {:added "3.0"}
  [:compose {:arglists '([a b])
             :val (comp not older?)}])

(def +lookup+
  {:newer newer?
   :not-newer not-newer?
   :older older?
   :not-older not-older?
   :equal equal?
   :not-equal not-equal?})

(defn clojure-version
  "returns the current clojure version
   (clojure-version)
   => (contains
       {:major anything,
        :minor anything,
        :incremental anything
        :qualifier anything})"
  {:added "3.0"}
  ([]
   *clojure-version*))

(defn java-version
  "returns the current java version
   (java-version)
   => (contains
       {:major anything,
        :minor anything,
        :incremental anything
        :qualifier anything})"
  {:added "3.0"}
  ([]
   (version (System/getProperty "java.version"))))

(defn system-version
  "alternate way of getting clojure and java version
   (system-version :clojure)
   => (clojure-version)
 
   (system-version :java)
   => (java-version)"
  {:added "3.0"}
  ([tag]
   (case tag
     :clojure (clojure-version)
     :java    (java-version))))

(defn satisfied
  "checks to see if the current version satisfies the given constraints
   (satisfied [:java    :newer {:major 1 :minor 7}]
              {:major 1  :minor 8})
   => true
 
   (satisfied [:java  :older {:major 1 :minor 7}]
              {:major 1  :minor 7})
   => false
 
   (satisfied [:java  :not-newer  {:major 30 :minor 0}])
   => true"
  {:added "3.0"}
  ([[type compare constraint :as entry]]
   (let [current (system-version type)]
     (satisfied entry current)))
  ([[type compare constraint] current]
   (if-let [pred (get +lookup+ compare)]
     (pred current constraint)
     (throw (ex-info "input not valid" {:input compare
                                        :options (keys +lookup+)})))))

(defmacro init
  "only attempts to load the files when the minimum versions have been met
 
   (version/init [[:java    :newer {:major 1 :minor 8}]
                  [:clojure :newer {:major 1 :minor 6}]]
                (:import java.time.Instant))"
  {:added "3.0"}
  ([constraints & statements]
   (if (->> constraints
            (map satisfied)
            (every? true?))
     (let [trans-fn (fn [[k & rest]]
                      (let [sym (symbol (str "clojure.core/" (name k)))]
                        (cons sym (map (fn [w]
                                         (if (keyword? w)
                                           w
                                           (list 'quote w)))
                                       rest))))]
       (cons 'do (map trans-fn statements))))))

(defmacro run
  "only runs the following code is the minimum versions have been met
 
   (version/run [[:java    :newer {:major 1 :minor 8}]
                 [:clojure :newer {:major 1 :minor 6}]]
               (eval '(Instant/ofEpochMilli 0)))"
  {:added "3.0"}
  ([constraints & body]
   (if (->> constraints
            (map satisfied)
            (every? true?))
     (cons 'do body))))
