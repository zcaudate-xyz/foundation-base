(ns std.block.layout.bind
  (:require [std.block.layout.common :as common]
            [std.block.layout.estimate :as est]
            [std.block.construct :as construct]
            [std.block.base :as base]
            [std.string :as str]
            [std.lib.function :as f]
            [std.lib.collection :as c]
            [std.lib.walk :as walk]
            [std.lib :as h]))

(def +bindings+
  '#{if let while ns 
     if-let if-some if-not
     try
     when when-not when-some when-first
     doto dotimes 
     binding with-bindings with-bindings*
     with-local-vars with-redefs for loop doseq
     with-open with-in-str with-precision
     extend extend-protocol extend-type})

(def +defs+
  '#{fn defn defmacro defrecord deftype
     defn-
     defmacro.js defn.js def.js def$.js def-.js
     defmacro.py defn.py def.py def$.py def-.py
     defmacro.lua defn.lua def.lua def$.lua def-.lua
     defmacro.pg defn.pg deftype.pg defenum.pg defindex.pg})

(def +pairing+
  '#{case cond-> cond->> some-> some->>
     as-> })



(defn layout-hiccup-like
  "checks if form is hiccup structure"
  {:added "4.0"}
  [v]
  (and (vector? v)
       (or (and (= :% (first v))
                (symbol? (second v)))
           (and (= :<> (first v))
                (some vector? (rest v)))
           (and (keyword? (first v))
                (or (map? (second v))
                    (vector? (second v)))
                (and (every? (comp not keyword?) (rest (rest v)))
                     (every? (comp not map?) (rest (rest v))))))))

(defn layout-spec-fn
  "gets the layout spec"
  {:added "4.0"}
  [form is-multiline]
  (if is-multiline
    (cond (h/form? form)
          (cond (coll? (first form))
                {:col-from  0
                 :col-start 1
                 :col-call true}

                (= 'catch (first form))
                {:col-from 2
                 :col-start 2}
                
                (= 'assoc (first form))
                {:columns 2
                 :col-from 1
                 :col-call true}

                (= 'hash-map (first form))
                {:columns 2
                 :col-from 0}                
                
                (+bindings+ (first form))
                {:col-from 1
                 :col-start 2}

                (+defs+ (first form))
                {:col-from 1
                 :col-start 2}

                (+pairing+ (first form))
                {:columns 2
                 :col-from 1
                 :col-start 2
                 :col-align true
                 :col-call true}

                :else
                {:col-from  1
                 :col-call true}))))

(defn layout-annotate-arglist
  "adds layout metadat to arglists"
  {:added "4.0"}
  [args]
  (mapv (fn [v]
          (walk/postwalk
           (fn [x]
             (cond  (vector? x)
                    (c/merge-meta x {:tag :vector
                                     :readable-len 10
                                     :spec {:columns 1}})

                    (map? x)
                    (c/merge-meta x {:readable-len 10})

                    :else
                    x))
           v))
        args))

(defn layout-annotate-bindings
  "adds layout metadata to bindings"
  {:added "4.0"}
  [bindings]
  (if (vector? bindings)
    (->> (partition 2 bindings)
         (mapcat (fn [[k v]]
                   [(walk/postwalk
                     (fn [x]
                       (cond (vector? x)
                             (c/merge-meta x {:tag :vector
                                              :readable-len 10
                                              :spec {:columns 1}})

                             (map? x)
                             (c/merge-meta x {:readable-len 10})
                             
                             :else
                             x))
                     k) v]))
         (vec))
    bindings))

(defn layout-annotate-fn-named
  "adds layout metadata to named functions"
  {:added "4.0"}
  [[_ sym doc? attr? & body :as form]]
  (let [top-index  (or (if (vector? doc?) 2)
                       (if (vector? attr?) 3)
                       (if (vector? (first body)) 4))
        list-index (or (if (h/form? doc?) 2)
                       (if (h/form? attr?) 3)
                       (if (h/form? (first body)) 4))]
    (if top-index
      (apply list
             (concat (take top-index form)
                     [(layout-annotate-arglist (nth form top-index))]
                     (drop (inc top-index) form)))
      (apply list
             (concat (take list-index form)
                     (->> (drop list-index form)
                          (map (fn [[args & rest]]
                                 (apply list
                                        (layout-annotate-arglist args)
                                        rest)))))))))

(defn layout-annotate-fn-anon
  "adds layout metadata to `fn` calls"
  {:added "4.0"}
  [[_ sym? & body :as form]]
  (let [top-index  (or (if (vector? sym?) 1)
                       (if (vector? (first body)) 2))
        list-index (or (if (h/form? sym?) 1)
                       (if (h/form? (first body)) 2))]
    (if top-index
      (apply list
             (concat (take top-index form)
                     [(layout-annotate-arglist (nth form top-index))]
                     (drop (inc top-index) form)))
      (apply list
             (concat (take list-index form)
                     (->> (drop list-index form)
                          (map (fn [[args & rest]]
                                 (apply list
                                        (layout-annotate-arglist args)
                                        rest)))))))))

(defn layout-annotate-svg-path
  [[tag attrs & more :as form]]
  (let [path   (with-meta
                 (str/split (:d attrs)
                            #" ")
                 {:tag :vector
                  :spec {:columns 5}})]
    (apply vector tag (assoc attrs :d path) more)))

(defn layout-annotate
  "adds metadata annotation to form"
  {:added "4.0"}
  [form]
  (cond (h/form? form)
        (cond ('#{fn} (first form))
              (layout-annotate-fn-anon form)

              ('#{defn- defn defmacro} (first form))
              (layout-annotate-fn-named form)
              
              (+bindings+ (first form))
              (let [[sym bindings & more] form]
                (apply list
                       sym
                       (layout-annotate-bindings bindings)
                       more))

              :else form)

        (and (vector? form)
             (= :path (first form)))
        (layout-annotate-svg-path form)
        
        :else form))

(defn layout-default-fn
  "the default function for level 1 transformation"
  {:added "4.0"}
  [form opts]
  (let [is-multiline (est/estimate-multiline form {})
        {:keys [metadata
                spec]}  (if (coll? form)
                          (meta form))
        spec  (or spec (layout-spec-fn form is-multiline))
        form  (layout-annotate form)
        nopts (assoc opts :spec spec)]
    (cond (not is-multiline) (construct/block form)
          
          (map? form) (common/layout-multiline-hashmap form nopts)

          (set? form) (common/layout-multiline-hashset form nopts)
          
          (vector? form) (if (layout-hiccup-like form)
                           (common/layout-multiline-hiccup form nopts)
                           (common/layout-multiline-vector form nopts))

          (h/form? form) (common/layout-multiline-list form nopts)
          
          :else (construct/block form))))

;;
;; layout loop
;;

(defn layout-main
  "performs the main layout"
  {:added "4.0"}
  [form & [opts]]
  (binding [common/*layout-fn* layout-default-fn]
    (layout-default-fn form opts)))



