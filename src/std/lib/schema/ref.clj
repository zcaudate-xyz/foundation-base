(ns std.lib.schema.ref
  (:require [std.lib.foundation :as h]
            [std.lib.schema.find :as find]
            [std.string.case :as case]
            [std.string.common :as str]
            [std.string.path :as path]
            [std.string.plural :as plural]
            [std.string.wrap :as wrap]))

(def ^:dynamic *ref-fn* nil)

(defmacro ^{:style/indent 1}
  with:ref-fn
  "passes a function for use in `reverse-ref-attr` method to add additional params to schema"
  {:added "4.0"}
  [[ref-fn] & body]
  `(binding [*ref-fn* ~ref-fn]
     ~@body))

(defn keyword-reverse
  "reverses the keyword by either adding or removing '_' in the value"
  {:added "3.0"}
  ([k]
   (if-let [kval ((wrap/wrap path/path-stem) k)]
     (let [sval   (name kval)
           rsval  (if (.startsWith sval "_")
                    (.substring sval 1)
                    (str "_" sval))]
       ((wrap/wrap path/path-join) [((wrap/wrap path/path-ns) k) rsval]))
     (throw (Exception. (str "Keyword " k " is not reversible."))))))

(defn keyword-reversed?
  "checks whether the keyword is reversed (begins with '_')"
  {:added "3.0"}
  ([k]
   (if-let [kval ((wrap/wrap path/path-stem) k)]
     (-> kval name (.startsWith "_"))
     false)))
		 
(defn is-reversible?
  "determines whether a ref attribute is reversible or not"
  {:added "3.0"}
  ([attr]
   (if (and (= :ref (:type attr))
            (-> attr :ref :ns)
            (not (-> attr :ref :norev))
            (not (-> attr :ref :mutual))
            ((wrap/wrap path/path-ns) (:ident attr))
            (-> attr :ident keyword-reversed? not))
     true false)))

(defn determine-rval
  "outputs the :rval value of a :ref schema reference"
  {:added "3.0"}
  ([[[root ref-ns many?] [attr] :as entry]]
   (if-let [rval (-> attr :ref :rval)]
     rval
     (let [ident  (h/strn (:ident attr))
           ival   (path/path-stem ident)]
       (cond (= root ref-ns)
             (let [rvec (concat (path/path-stem-array ident) '("of"))]
               (keyword (path/path-join (map case/spear-case rvec) "-")))

             many?
             (let [rvec (concat (path/path-stem-array ident)
                                (list (->> root name plural/plural)))]
               (keyword (path/path-join (map case/spear-case rvec) "-")))

             :else
             (->> root name case/spear-case plural/plural keyword))))))

(defn forward-ref-attr
  "creates the :ref schema attribute for the forward reference case"
  {:added "3.0"}
  ([[attr]]
   (let [{:keys [ident ref]} attr
         {:keys [ns rval]}   ref]
     (if (and ident ref ns rval)
       [(update-in attr [:ref] merge
                   {:type    :forward
                    :key     ident
                    :val     ((wrap/wrap path/path-stem)  ident)
                    :rkey    (keyword-reverse ident)
                    :rident  ((wrap/wrap path/path-join) [ns rval])})]
       (h/error  (str "PREPARE_FORWARD_ATTR: Required keys: [ident, ref [ns rval]] " attr))))))

(defn reverse-ref-attr
  "creates the reverse :ref schema attribute for backward reference"
  {:added "3.0"}
  ([[attr]]
   (let [{:keys [ident ref]} attr
         {:keys [key val rkey rval rident]}  ref]
     (if (and ident ref key val rkey rval rident)
       [{:ident       rident
         :cardinality :many
         :type        :ref
         :ref         (merge {:ns      ((wrap/wrap path/path-root) ident)
                              :type    :reverse
                              :val     rval
                              :key     rkey
                              :rval    val
                              :rkey    key
                              :rident  ident}
                             (if *ref-fn* (*ref-fn* attr)))}]
       (h/error  (str "PREPARE_REVERSE_ATTR: Required keys: [ident, ref [key val rkey rval rident]" attr))))))

(defn forward-ref-attr-fn
  "helper for `forward-ref-attr`"
  {:added "3.0"}
  ([[_ [attr] :as entry]]
   (forward-ref-attr
    [(assoc-in attr [:ref :rval] (determine-rval entry))])))

(defn attr-ns-pair
  "constructs a :ns and :ident root pair for comparison"
  {:added "3.0"}
  ([[attr]]
   (let [ident  (:ident attr)
         ref-ns (->  attr :ref :ns)]
     [((wrap/wrap path/path-root) ident) ref-ns])))

(defn mark-multiple
  "marks multiple ns/ident groups"
  {:added "3.0"}
  ([nsgroups] (mark-multiple nsgroups []))
  ([[nsgroup & more] output]
   (if-let [[nspair entries] nsgroup]
     (cond (< 1 (count entries))
           (recur more
                  (concat output
                          (map (fn [m] [(conj nspair true) m]) entries)))
           :else
           (recur more
                  (conj output [(conj nspair false) (first entries)])))
     output)))

(defn ref-attrs
  "creates forward and reverse attributes for a flattened schema"
  {:added "3.0"}
  ([fschm]
   (let [refs  (vals (find/all-attrs fschm is-reversible?))
         lus   (group-by attr-ns-pair refs)
         fwds  (->> (seq lus)
                    (mark-multiple)
                    (map forward-ref-attr-fn))
         revs (->> fwds
                   (filter (fn [[attr]] (is-reversible? attr)))
                   (map reverse-ref-attr))
         all   (concat fwds revs)]
     (zipmap (map (fn [[attr]] (:ident attr)) all) all))))
