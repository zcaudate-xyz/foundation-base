(ns code.refactor.base.expression
  (:require [code.query :as q]
            [code.edit :as nav]))

(defn rewrite-if-not
  "rewrites (if (not ...) ...)

   (nav/string
    (rewrite-if-not (nav/parse-root \"(if (not a) b c)\")))
   => \"(if-not a b c)\"

   (nav/string
    (rewrite-if-not (nav/parse-root \"(if (not a) b)\")))
   => \"(if-not a b)\""
  {:added "3.0"}
  [zloc]
  (q/modify zloc
            ['(if (not _) & _)]
            (fn [zloc]
              (let [form (nav/value zloc)
                    [_ [_ pred] then & else] form]
                (nav/replace zloc (apply list 'if-not pred then else))))))

(defn rewrite-when-not
  "rewrites (when (not ...) ...)

   (nav/string
    (rewrite-when-not (nav/parse-root \"(when (not a) b)\")))
   => \"(when-not a b)\""
  {:added "3.0"}
  [zloc]
  (q/modify zloc
            ['(when (not _) & _)]
            (fn [zloc]
              (let [form (nav/value zloc)
                    [_ [_ pred] & body] form]
                (nav/replace zloc (apply list 'when-not pred body))))))

(defn rewrite-not-empty
  "rewrites (not (empty? ...))

   (nav/string
    (rewrite-not-empty (nav/parse-root \"(not (empty? a))\")))
   => \"(seq a)\""
  {:added "3.0"}
  [zloc]
  (q/modify zloc
            ['(not (empty? _))]
            (fn [zloc]
              (let [form (nav/value zloc)
                    [_ [_ x]] form]
                (nav/replace zloc (list 'seq x))))))

(defn rewrite-not-seq
  "rewrites (not (seq ...))

   (nav/string
    (rewrite-not-seq (nav/parse-root \"(not (seq a))\")))
   => \"(empty? a)\""
  {:added "3.0"}
  [zloc]
  (q/modify zloc
            ['(not (seq _))]
            (fn [zloc]
              (let [form (nav/value zloc)
                    [_ [_ x]] form]
                (nav/replace zloc (list 'empty? x))))))

(defn rewrite-if-to-when
  "rewrites (if ... (do ...))

   (nav/string
    (rewrite-if-to-when (nav/parse-root \"(if a (do b))\")))
   => \"(when a b)\""
  {:added "3.0"}
  [zloc]
  (q/modify zloc
            ['(if _ (do & _))]
            (fn [zloc]
              (let [form (nav/value zloc)
                    [_ pred [_ & body]] form]
                (nav/replace zloc (apply list 'when pred body))))))
