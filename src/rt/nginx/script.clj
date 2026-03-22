(ns rt.nginx.script
  (:require [std.lib.env]
            [std.lib.foundation]
            [std.string.case]
            [std.string.common]
            [std.string.prose]))

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
  (std.string.case/snake-case (std.lib.foundation/strn k)))

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
                             (std.string.prose/spaces *indent*)
                             "}")))
        prose-fn (fn [vs]
                   (->> (map (fn [v]
                               (if (string? v)
                                 (std.string.prose/indent v *indent*)
                                 (str (std.string.prose/spaces *indent*)
                                      (std.string.prose/write-line v))))
                             vs)
                        (std.string.common/join "\n")))
        loop-fn  (fn [[k v & more]]
                   (cond  (block? v)
                          (let [extended (vec (apply concat v (filter identity more)))]
                            (str (ngx-key k) (inner-fn extended)))
                          
                          (nested-block? v)
                          (str (ngx-key k) " " (std.string.common/join " " (map std.lib.foundation/strn (butlast v)))
                               (inner-fn (last v)))
                          
                          (vector? v)
                          (str (ngx-key k) " " (std.string.common/join " " (map std.lib.foundation/strn v)) ";")
                          
                          :else
                          (str (ngx-key k) " " v ";")))
        emit-fn  (fn [m]
                   (cond (prose-block? m)
                         (prose-fn (rest (first m)))

                         :else
                         (->> (filter identity m)
                              (mapv loop-fn)
                              (std.string.common/join (str "\n" (std.string.prose/spaces *indent*)))
                              (str (std.string.prose/spaces *indent*)))))]
    (emit-fn m)))

(defn write
  "link to `std.make.compile`"
  {:added "4.0"}
  ([v]
   (emit-block v)))

(comment
  (./create-tests)
  (./import)
  (std.lib.env/pl (write [[:- "hello \nwhere"]]))
  (prose-block? ))
