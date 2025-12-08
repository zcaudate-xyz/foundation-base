(ns jvm.artifact.search
  (:require [std.fs.archive :as archive]
            [jvm.artifact :as artifact]))

(defn class-seq
  "creates a sequence of class names
 
   (-> (all-jars '[org.eclipse.aether/aether-api \"1.1.0\"])
       (class-seq)
       (count))
   => 128"
  {:added "3.0"}
  ([] (class-seq nil))
  ([coords]
   (->> (for [jar  (map #(artifact/artifact :path %) coords)
              item (archive/list jar)]
          (str item))
        (filter #(.endsWith ^String % ".class"))
        (map #(.substring ^String % 1 (- (.length ^String %) 6)))
        (map #(.replaceAll ^String % "/" ".")))))

(defn search-match
  "constructs a matching function for filtering
 
   ((search-match #\"hello\") \"hello.world\")
   => true
 
   ((search-match java.util.List) java.util.ArrayList)
   => true"
  {:added "3.0"}
  ([match]
   (cond (fn? match)
         ^:post (fn [cls]
                  (boolean (match cls)))

         (instance? Class match)
         ^:post (fn [cls] (.isAssignableFrom ^Class match cls))

         (instance? java.util.regex.Pattern match)
         ^:pre (fn [cls] (boolean (re-find match cls)))

         (string? match)
         (let [pat (re-pattern match)]
           ^:pre (fn [cls]
                   (boolean (re-find pat cls))))

         :else (throw (Exception. (str "Cannon compile match: " match))))))

(defn search
  "searches a pattern for class names
 
   (->> (.getURLs cls/+base+)
        (map #(-> % str (subs (count \"file:\"))))
        (filter #(.endsWith % \"jfxrt.jar\"))
        (class-seq)
        (search [#\"^javafx.*[A-Za-z0-9]Builder$\"])
        (take 5))
   => (javafx.animation.AnimationBuilder
       javafx.animation.FadeTransitionBuilder
      javafx.animation.FillTransitionBuilder
       javafx.animation.ParallelTransitionBuilder
       javafx.animation.PathTransitionBuilder)"
  {:added "3.0"}
  ([matches classes]
   (let [match-fns (map search-match matches)
         pre-fns  (filter #(-> % meta :pre) match-fns)
         post-fns (filter #(-> % meta :post) match-fns)]
     (->> classes
          (filter (fn [cls] (every? #(% cls) pre-fns)))
          (map #(Class/forName %))
          (filter (fn [cls] (every? #(% cls) post-fns)))
          (sort-by #(.getName ^Class %))))))
