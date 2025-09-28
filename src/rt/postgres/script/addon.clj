(ns rt.postgres.script.addon
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [std.json :as json]
            [rt.postgres.grammar.tf :as tf]
            [rt.postgres.grammar.common :as common])
  (:refer-clojure :exclude [case update assert throw]))

(l/script :postgres
  rt.postgres
  {:macro-only true})


(defmacro.pg ^{:- [:array]}
  ARRAY
  "generates random hex"
  {:added "4.0"}
  ([& args]
   (list :% (list :- "ARRAY[") (list 'quote (vec args)) (list :- "]"))))

(defmacro.pg ^{:- [:text]}
  id
  "generates random hex"
  {:added "4.0"}
  ([sym & [type]]
   `(~(or type :uuid) (:->> ~sym "id"))))

(defmacro.pg ^{:- [:jsonb]}
  full
  "generates random hex"
  {:added "4.0"}
  ([table]
   (let [entry (deref (deref (resolve table)))]
     (json/write
      [(:static/schema entry)
       (str (:id entry))]))))

(defmacro.pg ^{:- [:text]}
  rand-hex
  "generates random hex"
  {:added "4.0"}
  ([n]
   `(~'encode (~'gen-random-bytes ~(quot n 2)) "hex")))

(defmacro.pg ^{:- [:string]}
  sha1
  "calculates the sha1"
  {:added "4.0"}
  ([text]
   `(~'encode (~'digest (:bytea ~text) "sha1") "hex")))

(defmacro.pg ^{:- [:block]}
  client-list
  "gets the client list for pg"
  {:added "4.0"}
  []
  '[:select * :from pg_stat_activity])

;;
;; time
;; 

(defmacro.pg ^{:- [:bigint]}
  time-ms
  "returns the time in ms"
  {:added "4.0"}
  ([]
   '(* 1000 (extract [epoch :from (now)]))))

(defmacro.pg ^{:- [:bigint]}
  time-us
  "returns the time in us"
  {:added "4.0"}
  ([]
   '(* 1000000 (extract [epoch :from (now)]))))

(defmacro.pg ^{:- [:block]}
  throw
  "raises a json exception"
  {:added "4.0"}
  ([{:as m}]
   (tf/pg-tf-throw [nil m])))

(defmacro.pg ^{:- [:block]}
  error
  "raises a json error with value"
  {:added "4.0"}
  ([{:as m}]
   (tf/pg-tf-error [nil m])))

(defmacro.pg ^{:- [:block]
               :style/indent 1}
  assert
  "asserts given a block"
  {:added "4.0"}
  ([chk [tag data]]
   (tf/pg-tf-assert [nil chk [tag data]])))


(defmacro.pg ^{:style/indent 0}
  case
  "builds a case form
 
   (pg/case 1 2 3 4)
   => \"CASE WHEN 1 THEN 2\\nWHEN 3 THEN 4\\nEND\"
 
   ((:template @pg/case) 1 2 3 4)
   => '(% [:case :when 1 :then 2 \\ :when 3 :then 4 \\ :end])"
  {:added "4.0"}
  ([& args]
   (let [args  (partition 2 args)
         block (mapcat (fn [[chk body]]
                         (if (= :else chk)
                           [:else (list :% body) \\]
                           [:when (list :% chk) :then (list :% body) \\]))
                       args)]
     (list '% (vec (concat [:case]
                           block
                           [:end]))))))

(defmacro.pg
  field-id
  "shorthand for getting the field-id for a linked map"
  {:added "4.0"}
  [m field]
  (h/$ (coalesce (:->> ~m ~(str field "_id"))
                 (:->> (:-> ~m ~field) "id"))))

;;
;; map/reduce
;;

(defmacro.pg ^{:- [:anyarray]}
  map:rel
  "basic map across relation"
  {:added "4.0"}
  [f rel & args]
  `(~'% [(~'jsonb-agg (~f ~'o-ret ~@args)) :from ~rel :as ~'o-ret]))

(defmacro.pg ^{:- [:jsonb]}
  map:js
  "basic map across json"
  {:added "4.0"}
  ([f arr & args]
   (h/$ (% [(coalesce (jsonb-agg (~f o-ret ~@args))
                      (js []))
            \\ :from (jsonb-array-elements ~arr) :as o-ret]))))

(defmacro.pg ^{:- [:anyelement]}
  do:reduce
  "basic reduce macro"
  {:added "4.0"}
  ([out f type arr]
   `(~'let:block {:declare [(~type ~'e)]}
     (~'for:each [~'e :in ~'(% ARRAY) ~arr]
      (:= ~out (~f ~out ~'e)))
     (~'return ~out))))
  
(defmacro.pg ^{:- [:block]}
  b:select
  "basic select macro"
  {:added "4.0"}
  ([& args]
   `[:select ~@args]))

(defmacro.pg ^{:- [:block]}
  ret
  "returns a value alias for select"
  {:added "4.0"}
  ([& args]
   `[:select ~@args]))

(defmacro.pg ^{:- [:block]}
  b:update
  "update macro"
  {:added "4.0"}
  ([& args]
   `[:update ~@args]))

(defmacro.pg ^{:- [:block]}
  b:insert
  "insert macro"
  {:added "4.0"}
  ([& args]
   `[:insert ~@args]))

(defmacro.pg ^{:- [:block]}
  b:delete
  "delete macro"
  {:added "4.0"}
  ([& args]
   `[:delete ~@args]))

(defmacro.pg ^{:- [:block]}
  perform
  "perform macro"
  {:added "4.0"}
  ([& args]
   `[:perform ~@args]))

(defmacro.pg ^{:- [:block]}
  random-enum
  "gets random enum"
  {:added "4.0"}
  [enum]
  (h/$ [:select p :from (unnest (enum-range (++ nil ~enum))) :as p :order-by (random) :limit 1]))

(defmacro.pg
  do:plpgsql
  [& forms]
  `[:DO :$$
    :BEGIN
    ~@forms
    :END
    :$$ :LANGUAGE "plpgsql"])

