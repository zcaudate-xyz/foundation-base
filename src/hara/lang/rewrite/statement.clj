(ns hara.lang.rewrite.statement
  (:require [hara.lang.rewrite.common :as common]
            [hara.lang.rewrite.fn :as fnrw]))

(def with-form-meta
  common/with-form-meta)

(defn rewrite-for-statement
  [form rewrite-binding rewrite-statements]
  (let [[tag binding & body] form]
    (with-form-meta
      form
      (apply list tag
             (concat [(rewrite-binding binding)]
                     (rewrite-statements body))))))

(defn rewrite-cond-statement
  [form rewrite-conditional-expression rewrite-statement]
  (with-form-meta
    form
    (apply list 'cond
           (mapcat (fn [[test body]]
                     (if (= :else test)
                       [test
                        (rewrite-statement body)]
                       [(rewrite-conditional-expression test)
                        (rewrite-statement body)]))
                   (partition 2 (rest form))))))

(defn rewrite-branch-control
  [form rewrite-conditional-expression rewrite-statements]
  (let [[tag & args] form]
    (with-form-meta
      form
      (case tag
        else
        (apply list tag
               (rewrite-statements args))

        (let [[test & body] args]
          (apply list tag
                 (concat [(rewrite-conditional-expression test)]
                         (rewrite-statements body))))))))

(defn rewrite-branch-statement
  [form rewrite-branch-control]
  (with-form-meta
    form
    (apply list 'br*
           (map rewrite-branch-control (rest form)))))

(defn rewrite-do-statement
  ([form rewrite-statements]
   (rewrite-do-statement form rewrite-statements identity))
  ([form rewrite-statements prepare-body]
   (let [[tag & body] form
         body (prepare-body body)]
     (with-form-meta
       form
       (apply list tag
               (-> body
                   rewrite-statements
                   fnrw/splice-do*))))))

(defn rewrite-var-statement
  [form rewrite-expression]
  (let [[tag target & args] form]
    (if (empty? args)
      form
      (let [bound   (last args)
            leading (butlast args)]
        (with-form-meta
          form
          (apply list tag target
                 (concat leading
                         [(rewrite-expression bound)])))))))

(defn rewrite-return-statement
  [form rewrite-expression]
  (let [[tag & args] form]
    (with-form-meta
      form
      (apply list tag
             (map rewrite-expression args)))))

(defn rewrite-if-statement
  [form rewrite-conditional-expression rewrite-statement]
  (let [[tag test then & [else]] form]
    (with-form-meta
      form
      (apply list tag
             (cond-> [(rewrite-conditional-expression test)
                      (rewrite-statement then)]
               else (conj (rewrite-statement else)))))))

(defn rewrite-when-statement
  [form rewrite-conditional-expression rewrite-statements]
  (let [[tag test & body] form]
    (with-form-meta
      form
      (apply list tag
             (rewrite-conditional-expression test)
             (rewrite-statements body)))))

(defn rewrite-while-statement
  [form rewrite-conditional-expression rewrite-statements]
  (let [[tag test & body] form]
    (with-form-meta
      form
      (apply list tag
             (rewrite-conditional-expression test)
             (rewrite-statements body)))))

(defn rewrite-defn-statement
  ([form rewrite-statements]
   (rewrite-defn-statement form rewrite-statements identity))
  ([form rewrite-statements finalize-body]
   (let [[tag name args & body] form]
     (with-form-meta
       form
       (apply list tag name args
               (-> body
                   rewrite-statements
                   finalize-body
                   fnrw/splice-do*
                   fnrw/wrap-body))))))
