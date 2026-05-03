^{:reference "js.core.common"
  :no-test true}
(ns python.core.common
  (:require [clojure.string]
            [std.lib.foundation :as f]
            [std.lib.template :as template]))

(defn py-tmpl
  [s]
  (let [{:keys [type base prefix]
         :or {base []}} (f/template-meta)
        base (if (vector? base) base [base])
        dsym (case type
               :fragment 'def$.py
               :object 'def.py)
        [sym msym] (if (vector? s)
                     s
                     [s s])
        sym  (cond-> (clojure.core/name sym)
               :then (.replaceAll "\\." "")
               :then (.replaceAll "_" "-")
               prefix ((fn [s]
                         (str prefix s))))
        
        mrep (clojure.core/symbol (clojure.string/join "." (conj base msym)))]
    (template/$ (~dsym ~(clojure.core/symbol sym) ~mrep))))

(defn py-proto-tmpl
  [[s args {:keys [property optional vargs empty]}]]
  (let [{:keys [inst  prefix]
         :or {inst "obj"}} (f/template-meta)
        
        inst (clojure.core/symbol (clojure.core/str inst))
        [sym msym] (if (vector? s)
                     s
                     [s s])
        sym   (cond-> (.replaceAll (name s) "_" "-")
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
                    (template/$ (clojure.core/apply
                          list
                          (quote ~msym)
                          ~@args
                          (vec (clojure.core/concat
                                (clojure.core/take
                                 (- (clojure.core/count targs)
                                    (clojure.core/count ~vargs))
                                 ~optional)
                                ~vargs))))
                    
                    optional
                    (template/$ (clojure.core/apply
                          list
                          (quote ~msym)
                          ~@args (vec (clojure.core/take
                                       (clojure.core/count oargs) ~optional))))
                    
                    vargs
                    (template/$ (clojure.core/apply list (quote ~msym) ~@args ~vargs))
                    
                    :else 
                    (template/$ (list (quote ~msym) ~@args)))
        standalone (let [isym (if empty
                                (list 'or inst empty)
                                inst)]
                     (cond property
                           (template/$ (fn:> [~isym] (. ~isym ~msym)))
                           
                           :else
                           (let [vargs (if vargs
                                         [(symbol (str "..." vargs))]
                                         [])]
                             (template/$ (fn:> [~isym ~@args ~@optional ~@vargs]
                                      (. ~isym (~msym ~@args ~@optional ~@vargs)))))))
        sform (if property
                (template/$ (list '. ~inst (quote ~msym)))
                (template/$ (list '. ~inst ~aform)))]
    (template/$ (defmacro.py ~(with-meta sym {:standalone (list 'quote standalone)})
           ([~inst ~@dargs]
            ~sform)))))
