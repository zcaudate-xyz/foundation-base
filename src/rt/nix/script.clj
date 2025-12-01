(ns rt.nix.script
  (:require [std.string :as str]
            [std.lib :as h]))

(def ^:dynamic *indent* 0)

(def ^:dynamic *space* 2)

(defn- block? [v]
  (or (map? v)
      (and (vector? v)
           (keyword? (first v)))))

(defn- nix-key [k]
  (cond (keyword? k)
        (name k)

        (string? k)
        k

        :else
        (str k)))

(declare emit-nix)

(defn- emit-kv [k v]
  (str (nix-key k) " = " (emit-nix v) ";"))

(defn- map-fn [m]
  (->> (sort-by (comp str first) m)
       (map (fn [[k v]]
              (str (str/spaces *indent*)
                   (emit-kv k v))))
       (str/join "\n")))

(defn- inner-fn [m]
  (str "{\n"
       (binding [*indent* (+ *indent* *space*)]
         (map-fn m))
       "\n"
       (str/spaces *indent*)
       "}"))

(defn- vector-fn [v]
  (str "["
       (binding [*indent* (+ *indent* *space*)]
         (str/join " " (map emit-nix v)))
       "]"))

(defn- fn-block? [v]
  (and (vector? v) (= :fn (first v))))

(defn- emit-fn-block [[_ args body]]
  (str "{ " (str/join ", " (map emit-nix args)) " }:\n\n"
       (emit-nix body)))

(defn- path-block? [v]
  (and (vector? v) (= :path (first v))))

(defn- with-block? [v]
  (and (vector? v) (= :with (first v))))

(defn- emit-with-block [[_ scope body]]
  (str "with " (emit-nix scope) "; " (emit-nix body)))

(defn emit-nix
  "emits a nix config"
  {:added "4.0"}
  [m]
  (cond (map? m)
        (if (empty? m)
          "{}"
          (let [res (inner-fn m)]
            (if (= 0 *indent*)
              (str res "\n")
              res)))

        (fn-block? m)
        (emit-fn-block m)

        (path-block? m)
        (second m)

        (with-block? m)
        (emit-with-block m)

        (vector? m)
        (vector-fn m)

        (string? m)
        (str "\"" m "\"")

        (or (symbol? m)
            (boolean? m)
            (number? m))
        (str m)

        (keyword? m)
        (name m)

        :else
        (str m)))

(defn write
  "link to `std.make.compile`"
  {:added "4.0"}
  ([v]
   (emit-nix v)))
