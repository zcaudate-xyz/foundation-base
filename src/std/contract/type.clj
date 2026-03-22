(ns std.contract.type
  (:require [malli.core :as mc]
            [malli.error :as me]
            [malli.util :as mu]
            [std.contract.sketch :as sketch]
            [std.lib.atom :as atom]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]))

(defn check
  "checks that data fits the spec"
  {:added "3.0"}
  [schema data]
  (if (mc/validate schema data)
    data
    (let [raw (mc/explain schema data)
          err (-> (me/with-spell-checking raw)
                  (me/humanize))]
      (f/error (str err) {:error err
                          :raw raw}))))

(declare common-spec-string
         common-spec-invoke
         multi-spec-string
         multi-spec-invoke
         spec?)

(impl/defimpl CommonSpec [schema]
  :type   deftype
  :string common-spec-string
  :invoke common-spec-invoke)

(defn common-spec-invoke
  "invokes the common spec"
  {:added "3.0"}
  ([^CommonSpec spec] (sketch/from-schema (.schema spec)))
  ([^CommonSpec spec data] (check (.schema spec) data))
  ([^CommonSpec spec data _] (check (.schema spec) data)))

(defn common-spec-string
  "displays the common spec"
  {:added "3.0"}
  ([^CommonSpec spec]
   (str "#spec " (sketch/from-schema (.schema spec)))))

(defn combine
  "combines spec schemas (usually maps)"
  {:added "3.0"}
  ([sketch more]
   (let [schema (sketch/to-schema sketch)
         others (map sketch/to-schema more)]
     (reduce mu/merge schema others))))

(defn common-spec
  "creates a common spec"
  {:added "3.0"}
  ([sketch & more]
   (CommonSpec. (combine sketch more))))

(defmethod sketch/to-schema-extend CommonSpec
  [^CommonSpec c]
  (.schema c))

(defmacro defspec
  "macro for defining a spec"
  {:added "3.0"}
  ([sym sketch & more]
   `(def ~sym (common-spec ~sketch ~@more))))

(impl/defimpl MultiSpec [state dispatch]
  :type   deftype
  :string multi-spec-string
  :invoke multi-spec-invoke)

(defn multi-spec-invoke
  "invokes the multi spec"
  {:added "3.0"}
  ([^MultiSpec spec] (:options @(.state spec)))
  ([^MultiSpec spec data] (check (:final @(.state spec)) data))
  ([^MultiSpec spec data _] (check (:final @(.state spec)) data)))

(defn multi-spec-string
  "displays the multi spec"
  {:added "3.0"}
  ([^MultiSpec spec]
   (str "#spec.multi " (collection/map-vals sketch/from-schema (:options @(.state spec))))))

(defmethod sketch/to-schema-extend MultiSpec
  [^MultiSpec c]
  (:final @(.state c)))

(defn multi-gen-final
  "generates the final schema for a multispec"
  {:added "3.0"}
  ([dispatch options]
   (mc/schema (apply vector :multi {:dispatch dispatch}
                     (map (fn [[k v]] [k v]) options)))))

(defn multi-spec-add
  "adds additional types to the multi spec"
  {:added "3.0"}
  ([^MultiSpec spec dispatch-val schema]
   (atom/swap-return! (.state spec)
                   (fn [{:keys [options] :as m}]
                     (let [options (assoc options dispatch-val (sketch/to-schema schema))
                           final (multi-gen-final (.dispatch spec) options)]
                       [(keys options) {:options options :final final}])))))

(defn multi-spec-remove
  "removes additional types from the multi spec"
  {:added "3.0"}
  ([^MultiSpec spec dispatch-val]
   (atom/swap-return! (.state spec)
                   (fn [{:keys [options] :as m}]
                     (let [options (dissoc options dispatch-val)
                           final  (multi-gen-final (.dispatch spec) options)]
                       [(keys options) {:options options
                                        :final final}])))))

(defn multi-spec
  "creates a multi spec"
  {:added "3.0"}
  ([dispatch options]
   (let [options (collection/map-vals (fn [s]
                               (if (spec? s)
                                 [:fn s]
                                 (sketch/to-schema s)))
                             options)
         final (multi-gen-final dispatch options)]
     (MultiSpec. (atom {:final final :options options})
                 dispatch))))

(defmacro defmultispec
  "macro for defining a multispec"
  {:added "3.0"}
  ([sym dispatch & pairs]
   `(def ~sym (multi-spec ~dispatch
                          ~(apply hash-map pairs)))))

(defmacro defcase
  "adds an additional case to the multispec"
  {:added "3.0"}
  ([sym dispatch sketch & more]
   `(do (multi-spec-add ~sym ~dispatch
                        (combine ~sketch ~(vec more)))
        (var ~sym))))

(defn spec?
  "checks that object is of type spec"
  {:added "3.0"}
  [x]
  (or (instance? CommonSpec x)
      (instance? MultiSpec x)))

(defn valid?
  "checks that data is valid"
  {:added "3.0"}
  ([spec data]
   (mc/validate (sketch/to-schema-extend spec)
                data)))
