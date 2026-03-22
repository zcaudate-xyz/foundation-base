(ns rt.nginx.script
  (:require [clojure.string]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.string.case :as case]
            [std.string.prose :as prose]))

(def ^:dynamic *indent* 0)

(def ^:dynamic *space* 2)

(defn- dvector?
  ([v]
   (and (vector? v)
        (vector? (first v)))))

(defn- block? [v]
  (or (map? v)
      (dvector? v)))

(defn- nested-block? [v]
  (and (vector? v)
       (block? (last v))))

(defn- prose-block? [v]
  (and (dvector? v)
       (= :- (ffirst v))))

(defn- ngx-key [k]
  (case/snake-case (f/strn k)))

(defn emit-block
  "emits a block"
  {:added "4.0"}
  [m]
  (let [inner-fn     (fn inner-fn
                       ([v]
                        (inner-fn v emit-block))
                       ([v emit-fn]
                        (str " {\n"
                             (binding [*indent* (+ *indent* *space*)]
                               (emit-fn v))
                             "\n"
                             (prose/spaces *indent*)
                             "}")))
        prose-fn (fn [vs]
                   (->> (map (fn [v]
                               (if (string? v)
                                 (prose/indent v *indent*)
                                 (str (prose/spaces *indent*)
                                      (prose/write-line v))))
                             vs)
                        (clojure.string/join "\n")))
        loop-fn  (fn [[k v & more]]
                   (cond  (block? v)
                          (let [extended (vec (apply concat v (filter identity more)))]
                            (str (ngx-key k) (inner-fn extended)))
                          
                          (nested-block? v)
                          (str (ngx-key k) " " (clojure.string/join " " (map f/strn (butlast v)))
                               (inner-fn (last v)))
                          
                          (vector? v)
                          (str (ngx-key k) " " (clojure.string/join " " (map f/strn v)) ";")
                          
                          :else
                          (str (ngx-key k) " " v ";")))
        emit-fn  (fn [m]
                   (cond (prose-block? m)
                         (prose-fn (rest (first m)))

                         :else
                         (->> (filter identity m)
                              (mapv loop-fn)
                              (clojure.string/join (str "\n" (prose/spaces *indent*)))
                              (str (prose/spaces *indent*)))))]
    (emit-fn m)))

(defn write
  "link to `std.make.compile`"
  {:added "4.0"}
  ([v]
   (emit-block v)))

(comment
  (./create-tests)
  (./import)
  (env/pl (write [[:- "hello \nwhere"]]))
  (prose-block? ))
