(ns std.lang.base.emit-fn
  (:require [std.string :as str]
            [std.lib :as h]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-block :as block]))

(defn emit-input-default
  "create input arg strings"
  {:added "3.0"}
  ([{:keys [force modifiers type symbol value] :as arg} assign grammar mopts]
   (let [{:keys [start end]} (helper/get-options grammar [:default :index])
         {:keys [reversed hint]
          :as invoke}  (helper/get-options grammar [:default :invoke])
         {vmod true kmod false} (if (:vector-last (helper/get-options grammar [:default :modifier]))
                                  (group-by vector? modifiers)
                                  {false modifiers})
         {:keys [uppercase]} (:type invoke)
         arr-fn (fn [mod]
                  (if (keyword? mod)
                    (cond-> (name mod)
                      uppercase (str/upper-case))
                    (common/*emit-fn* mod grammar mopts)))
         tmod   (if type
                  (vec type)
                  [])
         
         tmodarr (mapv arr-fn tmod)
         kmodarr (mapv arr-fn kmod)
         mod-rev?    (and (or (empty tmodarr)
                              (not-empty kmodarr))
                          reversed)
         mod-has?    (or (not-empty tmodarr)
                         (not-empty kmodarr))
         mod-sym     (str (if symbol
                            (common/*emit-fn* symbol grammar mopts))
                          (if (and mod-rev?
                                   mod-has?)
                            hint))
         mixarr    (concat (if (not mod-rev?)
                             (concat kmodarr tmodarr)
                             (if (not= kmodarr mod-has?)
                               kmodarr))
                           (filter not-empty [mod-sym])
                           (if mod-rev? mod-has?))
         
         #_#_
         _         (h/prn {:mod-rev? mod-rev?
                           :tmodarr tmodarr
                           :kmodarr kmodarr
                           :mod-sym mod-sym
                           :modifiers modifiers
                           :mixarr  mixarr
                           :mod-has? mod-has?})

         mixstr (str/join " "
                          (filter (fn [x]
                                    (if (seq x)
                                      (not-empty x)
                                      x))
                                  mixarr))]
     (str mixstr
          (if (not-empty vmod)
            (str/join
             (map (fn [arr]
                    (str start
                         (if-let [n (first arr)]
                           (common/*emit-fn* n grammar mopts))
                         end))
                  vmod)))
          (if (or force value)
            (str " " assign " " (common/*emit-fn* value grammar mopts)))))))

(defn emit-hint-type
  "emits the return type"
  {:added "4.0"}
  ([type name suffix grammar mopts]
   (let [arr  (cond-> (:- (meta name))
                :then vec
                (not-empty suffix) (conj suffix))]
     
     (str/join " " (map (fn [v]
                          (cond (or (keyword? v)
                                    (string? v)
                                    (and (vector? v)
                                         (empty? v)))
                                (cond-> (h/strn v)
                                  (:uppercase type) (str/upper-case))

                                :else (common/*emit-fn* v grammar mopts)))
                        arr)))))

(defn emit-def-type
  "emits the def type"
  {:added "4.0"}
  ([name suffix grammar mopts]
   (let [{:keys [type]} (helper/get-options grammar [:default :define])]
     (emit-hint-type type name suffix grammar mopts))))

(defn emit-fn-type
  "returns the function type"
  {:added "3.0"}
  ([name suffix grammar mopts]
   (let [{:keys [type]} (helper/get-options grammar [:default :invoke])]
     (emit-hint-type type name suffix grammar mopts))))

(defn emit-fn-block
  "gets the block options for a given grammar"
  {:added "4.0"}
  [key grammar]
  (h/merge-nested (get-in grammar [:default :function])
                  (get-in grammar [:function key])))

(defn emit-fn-preamble-args
  "constructs the function preamble args"
  {:added "4.0"}
  ([key args grammar mopts]
   (let [args  (helper/emit-typed-args args grammar)
         {:keys [sep space assign start end multiline]}
         (h/merge-nested (helper/get-options grammar [:default :function :args])
                         (get-in grammar [:function key :args]))]
     (map #(emit-input-default % assign grammar mopts)
          args))))

(defn emit-fn-preamble
  "constructs the function preamble"
  {:added "4.0"}
  ([[key name args] grammar mopts]
   (let [iargs (emit-fn-preamble-args key args grammar mopts)
         {:keys [prefix]} (helper/get-options grammar [:default :function])
         {:keys [sep space assign start end multiline]}
         (h/merge-nested (helper/get-options grammar [:default :function :args])
                         (get-in grammar [:function key :args]))]
     (str (if prefix (str prefix " "))
          (if name (common/*emit-fn* name grammar mopts))
          space
          (cond (empty? iargs) (str start end)
                
                multiline
                (str start
                     (common/with-indent [2]
                       (str (common/newline-indent)
                            (str/join (str sep (common/newline-indent))
                                      iargs)))
                     (common/newline-indent)
                     end)

                :else
                (str start (str/join (str sep space) iargs) end))))))

(defn emit-fn
  "emits a function template"
  {:added "3.0"}
  ([key [tag & body] grammar mopts]
   (let [[name body] (if (symbol? (first body))
                       [(first body) (rest body)]
                       [nil body])
         [args & body] body
         header-only?  (:header (meta name))
         dynamic?      (or (:dynamic (meta name))
                           (:dynamic (:module mopts)))
         {:keys [compressed] :as block} (emit-fn-block key grammar)
         {:keys [enabled assign space after]}   (helper/get-options grammar [:default :typehint])
         typestr   (emit-fn-type name (or (:raw block) (h/strn tag)) grammar mopts)
         prestr    (emit-fn-preamble [key name args] grammar mopts)
         hintstr   (cond (or (not enabled)
                             (empty? typestr)) ""
                         
                         after (str assign space typestr)
                         
                         :else (str typestr space assign))
         body      (if dynamic?
                     `[(let [~'rt (std.lang.base.util/rt-current ~(:lang mopts))]
                         ~@body)]
                     body)
         blockstr  (if header-only?
                     (get-in grammar [:default :common :statement])
                     (binding [common/*compressed* compressed]
                       (block/emit-block-body key block body grammar mopts)))]
     (cond enabled
           (cond after
                 (str/join space (filter not-empty [prestr hintstr blockstr]))
                 
                 
                 :else
                 (str/join space (filter not-empty [hintstr prestr blockstr])))
           
           (not-empty typestr)
           (str typestr " " prestr blockstr)
           
           :else
           (str prestr " " blockstr)))))

;;
;;
;;

(defn test-fn-loop
  "add blocks, fn, var and const to emit"
  {:added "4.0"}
  [form grammar mopts]
  (common/emit-common-loop form
                           grammar
                           mopts
                           (assoc common/+emit-lookup+
                                  :data data/emit-data
                                  :block block/emit-block)
                           (fn [key form grammar mopts]
                             (case key
                               :fn (emit-fn :function form grammar mopts)
                               (common/emit-op key form grammar mopts
                                               {:quote data/emit-quote
                                                :table data/emit-table})))))

(defn test-fn-emit
  "add blocks, fn, var and const to emit"
  {:added "4.0"}
  [form grammar mopts]
  (binding [common/*emit-fn* test-fn-loop]
    (test-fn-loop form grammar mopts)))
