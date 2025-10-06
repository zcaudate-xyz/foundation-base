(ns rt.postgres.grammar.tf
  (:require [std.lib :as h]
            [std.string :as str]))

(def ^:dynamic *input-syms* nil)

(defn pg-js-idx
  "ignores single letter prefix"
  {:added "4.0"}
  ([esym]
   (let [estr (str esym)
         estr (if (re-find #"\w-\w+" estr)
                (subs estr 2)
                estr)]
     (str/snake-case estr))))

(defn pg-tf-js
  "converts a map to js object"
  {:added "4.0"}
  ([[_ val]]
   (let [obj-fn (fn obj-fn [val]
                  (cond (map? val)
                        (let [arr (mapcat (fn [[k v]]
                                            [(obj-fn k)
                                             (obj-fn v)])
                                          val)]
                          (apply list 'jsonb-build-object arr))
                        
                        (vector? val)
                        (let [arr (map obj-fn val)]
                          (apply list 'jsonb-build-array arr))
                        
                        (keyword? val)
                        (str/snake-case (h/strn val))

                        (set? val)
                        (let [pairs  (loop [acc []
                                            [x & more :as arr] (seq val)]
                                       (cond (empty? arr) acc
                                             (keyword? x) (recur (conj acc [(str/snake-case (h/strn x))
                                                                            (obj-fn (first more))])
                                                                 (rest more))
                                             (symbol? x) (recur (conj acc [(pg-js-idx x) x])
                                                                more)
                                             :else (h/error "Not Valid" {:value x})))]
                          (apply list 'jsonb-build-object (apply concat pairs)))
                        
                        :else val))]
     (cond (string? val)
           (str "\"" val "\"")
           
           (number? val)
           (str val)
           
           :else
           (obj-fn val)))))

(defn pg-tf-for
  "creates for loop"
  {:added "4.0"}
  ([[_ args & body]]
   `[:FOR ~@args :LOOP
     \\ (\| (do ~@body))
     \\ :END-LOOP \;]))

(defn pg-tf-foreach
  "creates foreach loop"
  {:added "4.0"}
  ([[_ args & body]]
   `[:FOREACH ~@args :LOOP
     \\ (\| (do ~@body))
     \\ :END-LOOP \;]))

(defn pg-tf-loop
  "creates loop"
  {:added "4.0"}
  ([[_ & body]]
   `[:LOOP
    \\ (\| (do ~@body))
    \\ :END-LOOP \;]))

;;
;; js basic
;;

(defn pg-tf-throw
  "creates throw transform"
  {:added "4.0"}
  ([[_ {:as m} tag]]
   [:raise-exception
    :using (list 'quote
                 [[:detail := (list '% m)]
                  [:message := (str tag)]])]))

(defn pg-tf-error
  "creates error transform"
  {:added "4.0"}
  ([[_ & [{:as m} tag]]]
   (let [m (if (map? m) m {:value m})]
     (pg-tf-throw [nil (merge {:status "error"} m) tag]))))

(defn pg-tf-assert
  "creates assert transform"
  {:added "4.0"}
  ([[_ chk [tag data]]]
   (let [m (if (map? data)
             (merge {:tag tag} data)
             {:tag  tag :data data})]
     `(~'if [:NOT '(~chk)] ~(pg-tf-error [nil m tag])))))


(comment
  (h/pl (std.lang/pg
         (let [(:jsonb out)  {}
               _  (do:reduce out || :jsonb arr)]
           (return out))))
  (h/pl (std.lang/pg (do:reduce out || :json [{} {}]))))





