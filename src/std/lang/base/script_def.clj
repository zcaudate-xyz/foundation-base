(ns std.lang.base.script-def
  (:require [std.lib :as h]
            [std.string :as str]
            [std.lang.base.util :as ut]))

(defn tmpl-entry
  "forms for various argument types"
  {:added "4.0"}
  [s]
  (let [{:keys [tag type base prefix shrink]
         :or {base []}} (h/template-meta)
        base (if (vector? base) base [base])
        dsym (case type
               :fragment (symbol (str "def$." tag))
               :object (symbol (str "def." tag)))
        [sym msym] (if (vector? s)
                     s
                     [s s])
        sym  (cond-> (name sym)
               :then (.replaceAll "\\." (if shrink
                                          "" "-"))
               :then ut/sym-default-inverse-str
               prefix ((fn [s]
                         (str prefix s))))
        
        mrep (symbol (str/join "." (conj base msym)))]
    (list dsym (symbol sym) mrep)))

(defn tmpl-macro
  "forms for various argument types"
  {:added "4.0"}
  [[s args {:keys [property optional vargs empty]}]]
  (let [{:keys [tag inst prefix subtree]
         :or {inst "obj"}} (h/template-meta)
        
        inst (symbol (str inst))
        [sym msym] (if (vector? s)
                     s
                     [s s])
        sym   (cond-> (ut/sym-default-inverse-str sym) 
                prefix ((fn [s]
                          (str prefix s)))
                :then symbol)
        dargs (cond property []

                    (and optional vargs)
                    (conj args '& (conj optional '& [:as vargs] :as 'targs))
                    
                    optional
                    (conj args '& (conj optional :as 'oargs))
                    
                    vargs
                    (conj args '& [:as vargs])
                    
                    :else
                    args)
        aform (cond (and optional vargs)
                    `(apply
                      list
                      (quote ~msym)
                      ~@args
                      (vec (concat
                            (take
                             (- (count ~'targs)
                                (count ~vargs))
                                  ~optional)
                            ~vargs)))
                    
                    optional
                    `(apply
                      list
                      (quote ~msym)
                      ~@args (vec (take
                                   (count ~'oargs) ~optional)))
                    
                    vargs
                    `(apply list (quote ~msym) ~@args ~vargs)
                    
                    :else 
                    `(list (quote ~msym) ~@args))
        standalone (let [isym (if empty
                                (list 'or inst empty)
                                inst)]
                     (cond property
                           `(~'fn:> [~isym] (~'. ~isym ~msym))
                           
                           :else
                           (let [vargs (if vargs
                                         [(symbol (str "..." vargs))]
                                         [])]
                             `(~'fn:> [~isym ~@args ~@optional ~@vargs]
                                  (~'. ~isym (~msym ~@args ~@optional ~@vargs))))))
        subtree-fn (fn [s] (list 'quote (symbol s)))
        sform (if property
                `(list ~''. ~inst ~@(map subtree-fn subtree) (quote ~msym))
                `(list ~''. ~inst ~@(map subtree-fn subtree) ~aform))]
    `(~(symbol (str "defmacro." tag)) ~(with-meta sym {:standalone (list 'quote standalone)})
      ([~inst ~@dargs]
       ~sform))))
