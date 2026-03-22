(ns std.lib.transform
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.lib.transform.allow :as allow]
            [std.lib.transform.apply :as transform]
            [std.lib.transform.base.alias :as alias]
            [std.lib.transform.base.enum :as enum]
            [std.lib.transform.base.keyword :as keyword]
            [std.lib.transform.base.type-check :as type-check]
            [std.lib.transform.convert :as convert]
            [std.lib.transform.fill-assoc :as fill-assoc]
            [std.lib.transform.fill-empty :as fill-empty]
            [std.lib.transform.ignore :as ignore]
            [std.lib.transform.mask :as mask]
            [std.lib.transform.require :as require]
            [std.lib.transform.validate :as validate]
            [std.string.common :as str]
            [std.string.path :as path]
            [std.string.wrap :as wrap]))

(def tree-directives
  #{:pre-require       ;;
    :pre-mask          ;;
    :pre-transform     ;;
    :fill-assoc        ;;
    :fill-empty        ;;
    :ignore            ;;
    :allow             ;;
    :expression        ;;
    :validate          ;;
    :convert           ;;
    :post-transform    ;;
    :post-mask         ;;
    :post-require      ;;
    })

(defn submaps
  "creates a submap based upon a lookup subkey
   (submaps {:allow  {:account :check}
             :ignore {:account :check}} #{:allow :ignore} :account)
   => {:allow :check, :ignore :check}"
  {:added "3.0"}
  ([m options subk]
   (reduce (fn [out option]
             (let [sv (get-in m [option subk])]
               (assoc out option sv)))
           (apply dissoc m (seq options))
           options)))

(defn wrap-plus
  "Allows additional attributes (besides the link :ns) to be added to the entity
   (normalise {:account {:orders {:+ {:account {:user \"Chris\"}}}}}
              {:schema (schema/schema examples/account-orders-items-image)}
              {:normalise [wrap-plus]})
   => {:account {:orders {:+ {:account {:user \"Chris\"}}}}}"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [output (f (dissoc tdata :+) tsch nsv interim fns datasource)
           pinterim  (submaps interim tree-directives :+)]
       (if-let [tplus (:+ tdata)]
         (let [pinterim (update-in pinterim [:key-path] conj :+)]
           (assoc output :+
                  ((:normalise fns) tplus (-> datasource :schema :tree) [] pinterim fns datasource)))
         output)))))

(defn wrap-ref-path
  "Used for tracing the entities through `normalise`
   (normalise {:account {:orders {:+ {:account {:WRONG \"Chris\"}}}}}
              {:schema (schema/schema examples/account-orders-items-image)}
              {:normalise [wrap-ref-path wrap-plus]})
 
   => (throws-info {:ref-path
                    [{:account {:orders {:+ {:account {:WRONG \"Chris\"}}}}}
                     {:account {:WRONG \"Chris\"}}]})"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (f tdata tsch nsv (update-in interim [:ref-path] (fnil #(conj % tdata) [])) fns datasource))))

(defn wrap-key-path
  "Used for tracing the keys through `normalise`
   (normalise {:account {:orders {:+ {:account {:WRONG \"Chris\"}}}}}
              {:schema (schema/schema examples/account-orders-items-image)}
              {:normalise [wrap-plus]
               :normalise-branch [wrap-key-path]
               :normalise-attr [wrap-key-path]})
 
   =>  (throws-info {:key-path [:account :orders :+ :account]})"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (f tdata tsch nsv (update-in interim [:key-path] (fnil #(conj % (last nsv)) [])) fns datasource))))

(defn normalise-loop
  "base loop for the normalise function
 
   (normalise-loop {:name \"Chris\", :age 10}
                   {:name [{:type :string,
                            :cardinality :one,
                            :ident :account/name}],
                    :age [{:type :long,
                           :cardinality :one,
                           :ident :account/age}],
                    :sex [{:type :enum,
                          :cardinality :one,
                           :enum {:ns :account.sex, :values #{:m :f}},
                           :ident :account/sex}]}
                   [:account]
                   {}
                   {:normalise normalise-loop
                    :normalise-single normalise-single
                    :normalise-attr normalise-attr}
                   {:schema (schema/schema examples/account-name-age-sex)})
   => {:name \"Chris\", :age 10}"
  {:added "3.0"}
  ([tdata tsch nsv interim fns datasource]
   (reduce-kv (fn [output k subdata]
                (let [subsch (get tsch k)
                      pinterim (submaps interim tree-directives k)
                      val (cond (nil? subsch)
                                ((:normalise-nil fns)
                                 subdata nil (conj nsv k) pinterim datasource)

                                (coll/hash-map? subsch)
                                ((:normalise-branch fns)
                                 subdata subsch (conj nsv k) pinterim fns datasource)

                                (vector? subsch)
                                ((:normalise-attr fns)
                                 subdata subsch (conj nsv k) pinterim fns datasource)

                                :else
                                (let [nnsv (conj nsv k)]
                                  (h/error (str "NORMALISE_LOOP: In " nsv ", " subdata
                                                " needs to be a vector or hashmap.")
                                           {:id :wrong-input :data subdata :nsv nnsv :key-path (:key-path interim)})))]
                  (cond-> output val (assoc k val))))
              {} tdata)))

(defn normalise-nil
  "base function for treating nil values
 
   (normalise-nil nil [:user :password] {} {} nil)
   => (throws)"
  {:added "3.0"}
  ([subdata _ nsv interim datasource]
   (h/error (str "NORMALISE_NIL: " nsv " is not in the schema.")
            {:id :no-schema :nsv nsv :key-path (:key-path interim)
             :ref-path (:ref-path interim)})))

(defn normalise-attr
  "base function for treating attributes
 
   (normalise-attr \"Chris\"
                   [{:type :string, :cardinality :one, :ident :account/name}]
                   [:account :name]
                   {}
                   {:normalise-single normalise-single}
                   {})
   => \"Chris\""
  {:added "3.0"}
  ([subdata [attr] nsv interim fns datasource]
   (cond (set? subdata)
         (-> (keep #((:normalise-single fns) % [attr] nsv interim fns datasource) subdata)
             (set))

         (and (vector? subdata) (not (vector? (first subdata))))
         (-> (keep #((:normalise-single fns) % [attr] nsv interim fns datasource) subdata)
             (vec))

         :else
         ((:normalise-single fns) subdata [attr] nsv interim fns datasource))))

(defn normalise-single
  "verifies and constructs a ref value
 
   (normalise-single {:value \"world\"}
                     [{:type :ref,
                       :ident :link/next
                       :cardinality :one,
                       :ref {:ns :link,
                             :rval :prev,
                             :type :forward,
                             :key :link/next,
                            :val :next,
                             :rkey :link/_next,
                             :rident :link/prev}}]
 
                     [:link :next]
                     {}
                     {:normalise-attr normalise-attr
                      :normalise normalise-loop
                      :normalise-single normalise-single}
                     {:schema (schema/schema examples/link-value-next)})
   => {:value \"world\"}"
  {:added "3.0"}
  ([subdata [attr] nsv interim fns datasource]
   (if (= (:type attr) :ref)
     (cond (coll/hash-map? subdata)
           (let [nnsv ((wrap/wrap path/path-split) (-> attr :ref :ns))]
             ((:normalise fns)
              (coll/tree-nestify:all subdata)
              (get-in datasource (concat [:schema :tree] nnsv))
              nnsv interim fns datasource))

           :else
           (h/error (str "NORMALISE_SINGLE: In " nsv "," subdata " should be either a hashmaps or ids")
                    {:id :wrong-input :nsv nsv :key-path (:key-path interim)}))
     subdata)))

(defn normalise-expression
  "normalises an expression"
  {:added "3.0"}
  ([subdata [attr] nsv interim datasource]
   subdata))

(defn normalise-wrap
  "helper function for normalise-wrappers"
  {:added "3.0"}
  ([fns wrappers]
   (reduce-kv (fn [out k f]
                (let [nf (if-let [wrapvec (get wrappers k)]
                           (reduce (fn [f wrapper] (wrapper f)) f wrapvec)
                           f)]
                  (assoc out k nf)))
              {} fns)))

(def normalise-wrapper-fns
  {:plus            wrap-plus
   :fill-assoc      fill-assoc/wrap-model-fill-assoc
   :fill-empty      fill-empty/wrap-model-fill-empty
   :pre-transform   transform/wrap-model-pre-transform
   :pre-mask        mask/wrap-model-pre-mask
   :pre-require     require/wrap-model-pre-require
   :post-require    require/wrap-model-post-require
   :post-mask       mask/wrap-model-post-mask
   :post-transform  transform/wrap-model-post-transform
   :ref-path        wrap-ref-path
   :key-path        wrap-key-path
   :alias           alias/wrap-alias
   :ignore          ignore/wrap-nil-model-ignore
   :allow-branch    allow/wrap-branch-model-allow
   :allow-attr      allow/wrap-attr-model-allow
   :keyword         keyword/wrap-single-keyword
   :convert         convert/wrap-single-model-convert
   :validate        validate/wrap-single-model-validate
   :enum            enum/wrap-single-enum
   :type-check      type-check/wrap-single-type-check})

(defn normalise-wrappers
  "adds function wrappers to the normalise functions"
  {:added "3.0"}
  ([datasource]
   (normalise-wrappers datasource {} normalise-wrapper-fns))
  ([{:keys [pipeline options]} additions fns]
   (->> {:normalise  [:plus
                      (if (:fill-assoc pipeline)     :fill-assoc)
                      (if (:fill-empty pipeline)     :fill-empty)
                      (if (:pre-transform pipeline)  :pre-transform)
                      (if (:pre-mask pipeline)       :pre-mask)
                      (if (:pre-require pipeline)    :pre-require)
                      (if (:post-require pipeline)   :post-require)
                      (if (:post-mask pipeline)      :post-mask)
                      (if (:post-transform pipeline) :post-transform)
                      :ref-path
                      :alias]
         :normalise-nil        [(if (:ignore pipeline) :ignore)]
         :normalise-branch     [(if (:allow pipeline)  :allow-branch)
                                :alias
                                :key-path]
         :normalise-attr       [(if (:allow pipeline) :allow-attr)
                                :key-path]

         :normalise-expression []

         :normalise-single     [:enum
                                :keyword
                                (if (:use-type-check options) :type-check)
                                (if (:convert pipeline) :convert)
                                (if (:validate pipeline) :validate)]}
        (reduce-kv (fn [out k v]
                     (assoc out k (->> (concat (get-in additions [k :pre])
                                               v
                                               (get-in additions [k :post]))
                                       (keep identity)
                                       (map fns))))
                   {}))))

(defn normalise-base
  "base normalise function"
  {:added "3.0"}
  ([tdata datasource wrappers]
   (let [tsch (-> datasource :schema :tree)
         interim (:pipeline datasource)
         fns {:normalise normalise-loop
              :normalise-nil normalise-nil
              :normalise-branch normalise-loop
              :normalise-attr normalise-attr
              :normalise-expression normalise-expression
              :normalise-single normalise-single}
         fns (normalise-wrap fns wrappers)]
     ((:normalise fns) tdata tsch [] interim fns datasource))))

(defn normalise
  "base normalise function
 
   (normalise {:account/name \"Chris\"
               :account/age 10}
              {:schema (schema/schema examples/account-name-age-sex)}
              {})
   => {:account {:age 10, :name \"Chris\"}}
 
   (normalise {:link/value \"hello\"}
              {:schema (schema/schema examples/link-value-next)}
              {})
   => {:link {:value \"hello\"}}
 
   (normalise {:link/value \"hello\"
               :link {:next/value \"world\"
                      :next/next {:value \"!\"}}}
              {:schema (schema/schema examples/link-value-next)}
              {})
 
   => {:link {:next {:next {:value \"!\"}
                     :value \"world\"}
              :value \"hello\"}}"
  {:added "3.0"}
  ([data {:keys [pipeline] :as datasource}]
   (let [wrappers (normalise-wrappers datasource)]
     (normalise data datasource wrappers)))
  ([data datasource wrappers]
   (let [tdata (coll/tree-nestify:all data)
         tdata (if-let [pre-process-fn (-> datasource :pipeline :pre-process)]
                 (pre-process-fn tdata datasource)
                 tdata)
         output (normalise-base tdata datasource wrappers)]
     (if-let [post-process-fn (-> datasource :pipeline :post-process)]
       (post-process-fn output datasource)
       output))))
