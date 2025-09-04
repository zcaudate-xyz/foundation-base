(ns std.lang.base.emit-helper
  (:require [std.string :as str]
            [std.lib :as h]
            [std.lang.base.util :as ut]))

(defn default-emit-fn
  "the default emit function"
  {:added "4.0"}
  [form grammar mopts]
  (pr-str form))

(defn pr-single
  "prints a single quoted string"
  {:added "3.0"}
  ([s]
   (-> (pr-str s)
       (str/replace #"'" "\\\\'")
       (str/replace #"^\"" "'")
       (str/replace #"\"$" "'")
       (str/replace #"\\\"" "\""))))

(def +sym-replace+
  {\- "_"
   \* "__"
   \? "p"
   \! "f"
   \= "_eq"
   \< "_lt"
   \> "_gt"})

(def +default+
  {:type   :std/algol
   :banned #{:set :keyword}
   :allow  {:assign #{:symbol}}
   :highlight '#{return break set= br}
   :default {:modifier  {:vector-last true}
             :comment   {:prefix "//" :suffix ""}
             :common    {:start  "(" :end ")"
                         :space " "
                         :assign "="
                         :statement ";"
                         :range ":"
                         :sep ","
                         :namespace "."
                         :namespace-full "____"
                         :access "."
                         :static "."
                         :apply "."
                         :line-spacing 1}
             :symbol    {:full {:replace {\. "_"}
                                :sep "_"}
                         :global identity}
             :index     {:start "[" :end "]"
                         :offset 0  :end-inclusive false}
             :invoke    {:space ""}
             :macro     {:statement ""}
             :block     {:statement ""
                         :parameter {:start "(" :end ")"}
                         :body      {:start "{" :end "}"}}
             :typehint  {:enabled false :assign "->" :space " " :after false}
             :function  {:raw "function"
                         :args      {:start "(" :end ")" :space ""}
                         :body      {:start "{" :end "}"}}
             :module    {:key-fn #'ut/sym-default-str
                         :allow  '#{defn def defgen defclass defabstract}}}
   :token  {:string     {:quote nil}
            :symbol     {:replace +sym-replace+}}
   :data   {:map        {:start "{" :end "}" :space ""}
            :map-entry  {:start ""  :end ""  :assign ":" :space "" :keyword :string}
            :vector     {:start "[" :end "]" :space ""}
            :tuple      {:start "(" :end ")" :space "" :tighten true}
            :free       {:start ""  :end ""  :space ""}}
   :block  {:branch     {:control {:elseif {:raw "else if"}}}
            :switch     {:control {:default {:parameter {:start " " :end ""}
                                             :body      {:start ":" :end ""}}}}}
   :define {:defglobal  {:raw "global"}
            :def        {:raw "def"}}})

(defn get-option
  "gets either the path option or the default one"
  {:added "3.0"}
  ([grammar path option]
   (let [default [:default :common]]
     (or (get-in grammar (conj path option))
         (get-in grammar (conj default option))))))

(defn get-options
  "gets the path option merged with defaults"
  {:added "3.0"}
  ([grammar path]
   (merge (get-in grammar [:default :common])
          (get-in grammar path))))

;;
;; CLASSIFY
;;

(defn form-key-base
  "gets the key for a form
 
   (form-key-base :a)
   => [:keyword :token true]
 
   (form-key-base ())
   => :expression"
  {:added "4.0"}
  [form]
  (cond (char? form)      [:char :token true]
        (keyword? form)   [:keyword :token true]
        (symbol? form)    [:symbol :token true]
        
        (h/form? form)    :expression

        (number? form)    [:number :token]
        (boolean? form)   [:boolean :token]
        (h/regexp? form)  [:regex :token]
        
        (string? form)    [:string :token]
        (nil? form)       [:nil :token]
        
        (map? form)       [:map :data]
        (map-entry? form) [:map-entry :data]
        (vector? form)    [:vector :data]
        (set? form)       [:set :data]

        :else (h/error "Not Valid" {:input form
                                    :type (type form)})))

(defn basic-typed-args
  "typed args without grammar checks
 
   (mapv (juxt meta identity)
         (basic-typed-args '(:int i, :const :int j)))
   => '[[{:- [:int]} i]
        [{:- [:const :int]} j]]"
  {:added "4.0"}
  ([args]
   (:all (reduce (fn [{:keys [modifiers all]} curr]
                   (if (symbol? curr)
                     {:modifiers []
                      :all (conj all (with-meta curr {:- modifiers}))}
                     {:modifiers (conj modifiers curr)
                      :all all}))
                 {:modifiers []
                  :all []}
                 args))))

;;
;; DEFINE
;;

(defn emit-typed-allowed-args
  "allowed declared args other than symbols"
  {:added "4.0"}
  [[all curr] grammar]
  (let [{:keys [modifiers]} curr
        arg (last modifiers)]
    (if (contains? (get-option grammar [:allow] :assign)
                   (first (h/seqify (form-key-base arg))))
      [all (-> curr
               (assoc :symbol arg
                      :modifiers (vec (butlast modifiers))
                      :assign true))]
      (h/error "Cannot assign to non symbol" {:curr curr :all all}))))

(defn emit-typed-args
  "create types args from declarationns"
  {:added "3.0"}
  ([args grammar]
   (loop [all  []
          curr {:modifiers []}
          [sym & more :as args] args]
     (cond (empty? args)
           (if (:symbol curr)
             (conj all curr)
             all)
           
           (= := sym)
           (if (:symbol curr)
             (recur all (assoc curr :assign true :force true) more)
             (let [[all curr] (emit-typed-allowed-args [all curr] grammar)]
               (recur all (assoc curr :force true) more)))
           
           (= :% sym)
           (if (:symbol curr)
             (recur (conj all curr) {:modifiers [(first more)]} (rest more))
             (recur all (update curr :modifiers conj (first more)) (rest more)))
           
           (:assign curr)
           (recur (conj all (assoc curr :value sym))
                  {:modifiers []}
                  more)
           
           (and (not (:symbol curr))
                (list? sym)
                (not (neg? (h/index-at #{:=} sym))))
           (let [[symbol val] (remove #{:=} sym)]
             (recur (conj all (assoc curr :symbol symbol :value val))
                    {:modifiers []}
                    more))

           (or (symbol? sym)
               (and (set? sym)
                    (-> grammar
                        :allow
                        :assign
                        :set)
                    (not (:assign curr)))
               (and (vector? sym)
                    (-> grammar
                        :allow
                        :assign
                        :vector))
               (and (h/form? sym)
                    (= 'quote (first sym))
                    (-> grammar
                        :allow
                        :assign
                        :quote))
               (and (h/form? sym)
                    (= := (first sym))))
           (if (:symbol curr)
             (recur (conj all curr) {:modifiers [] :symbol sym} more)
             (recur all (assoc curr :symbol sym) more))

           (and (h/form? sym)
                (keyword? (first sym)))
           (if (:symbol curr)
             (recur (conj all curr) {:type   (butlast sym)
                                     :symbol (last sym)}
                    more)
             (recur all (assoc curr
                               :symbol sym
                               :type   (butlast sym)
                               :symbol (last sym))
                    more))
           
           (or (keyword? sym) (vector? sym))
           (if (:symbol curr)
             (recur (conj all curr) {:modifiers [sym]} more)
             (recur all (update curr :modifiers conj sym) more))
           
           (:symbol curr)
           (recur (conj all (assoc curr :value sym)) {:modifiers []} more)
           
           :else
           (h/error "Not a valid input" {:input sym
                                         :all all
                                         :curr curr})))))

(defn emit-symbol-full
  "emits a full symbol"
  {:added "4.0"}
  [sym ns grammar]
  (let [dopts (get-in grammar [:default :common])
        sopts (get-in grammar [:default :symbol :full])
        topts (get-in grammar [:token :symbol])
        sym-str  (if ns
                   (str ns
                        (:namespace-full dopts)
                        (name sym))
                   (name sym))]
    (cond->> (. sym-str (replaceAll "\\." (or (:namespace-sep dopts)
                                              "_")))
      :then   (replace (merge (:replace sopts)
                              (:replace topts)))
      :then   (apply str))))

(defn emit-type-record
  "formats to standard"
  {:added "4.0"}
  [{:keys [modifiers symbol]}]
  {:symbol (ut/sym-default-str symbol)
   :type (name (first modifiers))})
