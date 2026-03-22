(ns code.framework.link.java
  (:require [clojure.string]
            [code.framework.link.common :as common])
  (:import (java.io File)))

(defn get-class
  "grabs the symbol of the class in the java file
   (get-class
    (io/file \"test-java/test/Cat.java\"))
   => 'test.Cat"
  {:added "3.0"}
  ([^File file]
   (let [pkg (-> (->> (slurp file)
                      (clojure.string/split-lines)
                      (filter #(.startsWith ^String % "package"))
                      (first))
                 (clojure.string/split #"[ ;]")
                 (second))
         nm  (let [nm (.getName file)]
               (subs nm 0 (- (count nm) 5)))]
     (symbol (str pkg "." nm)))))

(defn get-imports
  "grabs the symbol of the class in the java file
   (get-imports
    (io/file \"test-java/test/Cat.java\"))
   => '()"
  {:added "3.0"}
  ([file]
   (->> (slurp file)
        (clojure.string/split-lines)
        (filter #(.startsWith ^String % "import"))
        (map #(clojure.string/split % #"[ ;]"))
        (map second)
        (map symbol))))

(defmethod common/-file-linkage :java
  ([file]
   {:file file
    :exports #{[:class (get-class file)]}
    :imports (set (map (fn [jv] [:class jv]) (get-imports file)))}))
