^{:reference "js.core.common"
  :no-test true}
(ns python.core.common
  (:require [std.lib.foundation]
            [std.lib.template]
            [std.string.common]))

(defn py-tmpl
  [s]
  (let [{:keys [type base prefix]
         :or {base []}} (std.lib.foundation/template-meta)
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
        
        mrep (clojure.core/symbol (std.string.common/join "." (conj base msym)))]
    (std.lib.template/$ (~dsym ~(clojure.core/symbol sym) ~mrep))))

(defn py-proto-tmpl
  [[s args {:keys [property optional vargs empty]}]]
  (let [{:keys [inst  prefix]
         :or {inst "obj"}} (std.lib.foundation/template-meta)
        
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
                    (std.lib.template/$ (clojure.core/apply
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
                    (std.lib.template/$ (clojure.core/apply
                          list
                          (quote ~msym)
                          ~@args (vec (clojure.core/take
                                       (clojure.core/count oargs) ~optional))))
                    
                    vargs
                    (std.lib.template/$ (clojure.core/apply list (quote ~msym) ~@args ~vargs))
                    
                    :else 
                    (std.lib.template/$ (list (quote ~msym) ~@args)))
        standalone (let [isym (if empty
                                (list 'or inst empty)
                                inst)]
                     (cond property
                           (std.lib.template/$ (fn:> [~isym] (. ~isym ~msym)))
                           
                           :else
                           (let [vargs (if vargs
                                         [(symbol (str "..." vargs))]
                                         [])]
                             (std.lib.template/$ (fn:> [~isym ~@args ~@optional ~@vargs]
                                      (. ~isym (~msym ~@args ~@optional ~@vargs)))))))
        sform (if property
                (std.lib.template/$ (list '. ~inst (quote ~msym)))
                (std.lib.template/$ (list '. ~inst ~aform)))]
    (std.lib.template/$ (defmacro.py ~(with-meta sym {:standalone (list 'quote standalone)})
           ([~inst ~@dargs]
            ~sform)))))
