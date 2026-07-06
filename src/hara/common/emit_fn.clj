(ns hara.common.emit-fn
  (:require [clojure.string]
            [hara.common.emit-block :as block]
            [hara.common.emit-common :as common]
            [hara.common.emit-data :as data]
            [hara.common.emit-helper :as helper]
            [std.lib.collection :as collection]
            [std.lib.env :as env]
            [std.lib.foundation :as f]))

(defn emit-input-rest
  "emits a canonical xtalk rest parameter for the active target language"
  {:added "4.1"}
  [{:keys [symbol]} grammar mopts]
  (let [lang (some-> (:lang mopts) name keyword)
        out  (common/*emit-fn* symbol grammar mopts)
        php-out (if (.startsWith ^String out "$")
                  out
                  (str "$" out))]
    (case lang
      (:js :javascript) (str "..." out)
      :python          (str "*" out)
      :ruby            (str "*" out)
      :php             (str "..." php-out)
      :lua             "..."
      :dart            (str "[" out " = const []]")
      (f/error "Rest parameters are not supported for target"
               {:lang lang
                :symbol symbol}))))

(defn emit-input-default
  "create input arg strings"
  {:added "3.0"}
  ([{:keys [force modifiers type symbol value rest] :as arg} assign grammar mopts]
   (if rest
     (emit-input-rest arg grammar mopts)
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
                        uppercase (clojure.string/upper-case))
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
           _         (env/prn {:mod-rev? mod-rev?
                              :tmodarr tmodarr
                              :kmodarr kmodarr
                              :mod-sym mod-sym
                              :modifiers modifiers
                              :mixarr  mixarr
                              :mod-has? mod-has?})

           mixstr (clojure.string/join " "
                                      (filter (fn [x]
                                                (if (seq x)
                                                  (not-empty x)
                                                  x))
                                              mixarr))]
       (str mixstr
            (if (not-empty vmod)
              (clojure.string/join
               (map (fn [arr]
                      (str start
                           (if-let [n (first arr)]
                             (common/*emit-fn* n grammar mopts))
                           end))
                    vmod)))
            (if (or force value)
              (str " " assign " " (common/*emit-fn* value grammar mopts))))))))

(defn emit-hint-type
  "emits the return type"
  {:added "4.0"}
  ([type name suffix grammar mopts]
   (let [arr  (cond-> (:- (meta name))
                :then vec
                (not-empty suffix) (conj suffix))]
     
     (clojure.string/join " " (map (fn [v]
                                      (cond (or (keyword? v)
                                                (string? v)
                                                (and (vector? v)
                                                     (empty? v)))
                                            (cond-> (f/strn v)
                                              (:uppercase type) (clojure.string/upper-case))

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
  (collection/merge-nested (get-in grammar [:default :function])
                           (get-in grammar [:function key])))

(defn emit-fn-preamble-args
  "constructs the function preamble args"
  {:added "4.0"}
  ([key args grammar mopts]
   (let [args  (helper/emit-typed-args args grammar)
         {:keys [sep space assign start end multiline]}
         (collection/merge-nested (helper/get-options grammar [:default :function :args])
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
         (collection/merge-nested (helper/get-options grammar [:default :function :args])
                                  (get-in grammar [:function key :args]))]
     (str (if (not-empty prefix) (str prefix " "))
          (if name (common/*emit-fn* name grammar mopts))
          space
          (cond (empty? iargs) (str start end)
                
                multiline
                (str start
                     (common/with-indent [2]
                       (str (common/newline-indent)
                            (clojure.string/join (str sep (common/newline-indent))
                                                 iargs)))
                     (common/newline-indent)
                     end)

                :else
                (str start (clojure.string/join (str sep space) iargs) end))))))

(defn- replace-rest-symbol
  [form from to]
  (cond (= form from)
        to

        (and (collection/form? form)
             (= 'quote (first form)))
        form

        (collection/form? form)
        (with-meta (apply list (map #(replace-rest-symbol % from to) form))
          (meta form))

        (vector? form)
        (with-meta (mapv #(replace-rest-symbol % from to) form)
          (meta form))

        (map? form)
        (with-meta (into (empty form)
                         (map (fn [[k v]]
                                [(replace-rest-symbol k from to)
                                 (replace-rest-symbol v from to)]))
                         form)
          (meta form))

        (set? form)
        (with-meta (set (map #(replace-rest-symbol % from to) form))
          (meta form))

        :else
        form))

(defn- prepare-rest-body
  [args body mopts]
  (if-let [rest-sym (some helper/rest-arg-symbol args)]
    (case (some-> (:lang mopts) name keyword)
      :lua (cons (list 'var rest-sym (vector '...)) body)
      :php (let [php-sym (symbol (str "$" (name rest-sym)))]
             (map #(replace-rest-symbol % rest-sym php-sym) body))
      body)
    body))

(defn emit-fn
  "emits a function template"
  {:added "3.0"}
  ([key [tag & body] grammar mopts]
   (let [[name body] (if (symbol? (first body))
                       [(first body) (rest body)]
                       [nil body])
         [args & body] body
         body         (prepare-rest-body args body mopts)
         header-only?  (:header (meta name))
         {:keys [compressed] :as block} (emit-fn-block key grammar)
         {:keys [enabled assign space after]}   (helper/get-options grammar [:default :typehint])
         typestr   (emit-fn-type name (or (:raw block) (f/strn tag)) grammar mopts)
         prestr    (emit-fn-preamble [key name args] grammar mopts)
         hintstr   (cond (or (not enabled)
                             (empty? typestr)) ""
                         
                         after (str assign space typestr)
                         
                         :else (str typestr space assign))
         blockstr  (if header-only?
                     (get-in grammar [:default :common :statement])
                     (binding [common/*compressed* compressed]
                       (block/emit-block-body key block body grammar mopts)))]
     (cond enabled
           (cond after
                 (clojure.string/join space (filter not-empty [prestr hintstr blockstr]))
                 
                 
                 :else
                 (clojure.string/join space (filter not-empty [hintstr prestr blockstr])))
           
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