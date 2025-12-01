(ns rt.fortran.grammar
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as emit-common]
            [std.lang.base.emit-fn :as emit-fn]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.string :as str]
            [std.lib :as h]))

(defn fortran-type
  "formats fortran types"
  [t]
  (let [s (str t)]
    (case s
      "int" "INTEGER"
      "long" "INTEGER*8"
      "float" "REAL"
      "double" "DOUBLE PRECISION"
      "bool" "LOGICAL"
      "str" "CHARACTER(LEN=*)"
      "void" ""
      (str/upper-case s))))

(defn fortran-args
  "custom Fortran argument emission"
  [[_ args] grammar mopts]
  (let [args (if (and (list? args) (= 'quote (first args)))
               (second args)
               args)
        args (if (vector? args) args [args])
        arg-names (map (fn [arg]
                         (if (vector? arg)
                           (emit/emit-main (second arg) grammar mopts)
                           (emit/emit-main arg grammar mopts)))
                       args)]
    (str "(" (str/join ", " arg-names) ")")))

(defn fortran-decl
  "declaration helper"
  [args]
  (keep (fn [a]
          (when (vector? a)
            (let [[t n] a]
              (list :- (fortran-type t) "::" n))))
        args))

(defn fortran-defn
  "transforms defn to SUBROUTINE or FUNCTION"
  [[_ sym args & body]]
  (let [ret-type (-> sym meta :tag)
        is-function (some? ret-type)
        fname (h/strn sym)
        header (if is-function
                 (str (fortran-type ret-type) " FUNCTION " fname)
                 (str "SUBROUTINE " fname))
        decls (fortran-decl args)
        body-combined (concat decls body)
        footer (str "END " (if is-function "FUNCTION" "SUBROUTINE") " " fname)]
    (list :- header
          (if (seq args)
            (list :fortran-args (list 'quote args))
            "")
          (list :- "\n")
          (list :%
                (list \\ (apply list 'do body-combined)))
          (list :- footer))))

(defn fortran-defprogram
  "transforms defprogram to PROGRAM"
  [[_ sym & body]]
  (let [fname (h/strn sym)]
    (list :- "PROGRAM" fname
          (list :- "\n")
          (list :%
                (list \\ (list :- "IMPLICIT NONE\n") (apply list 'do body)))
          (list :- "END PROGRAM" fname))))

(defn fortran-def
  "transforms def to declaration and assignment"
  [[_ sym val]]
  (let [t (-> sym meta :tag)
        type-str (if t (fortran-type t) "TYPE(UNKNOWN)")]
    (if val
      (list :- type-str "::" sym "=" val)
      (list :- type-str "::" sym))))

(defn fortran-print
  "transforms print"
  [[_ & args]]
  (let [args (map (fn [x]
                    (if (string? x)
                      (str "'" x "'")
                      x))
                  args)]
    (apply list :- "PRINT *," (interpose "," args))))

(defn fortran-module
  "transforms module"
  [[_ sym & body]]
  (let [fname (h/strn sym)]
    (list :- "MODULE" fname
          (list :- "\n")
          (list :%
                (list \\ (list :- "IMPLICIT NONE\n") (apply list 'do body)))
          (list :- "END MODULE" fname))))

(def +features+
  (-> (grammar/build :exclude [:control-try-catch
                               :class
                               :macro-arrow
                               :macro-case])
      (grammar/build:override
       {:defn    {:macro #'fortran-defn :emit :macro}
        :ret     {:raw "RETURN"}
        :def     {:macro #'fortran-def :emit :macro}
        :free    {:op :free :symbol #{:-} :emit :free :sep " " :type :free}
        :eq      {:raw "=="}
        :neq     {:raw "/="}
        :and     {:raw ".AND."}
        :or      {:raw ".OR."}
        :not     {:raw ".NOT."}})
      (grammar/build:extend
       {:fortran-args {:op :fortran-args :symbol #{:fortran-args} :emit #'fortran-args}
        :program {:op :program :symbol #{'program} :macro #'fortran-defprogram :emit :macro :type :def :section :code}
        :module  {:op :module  :symbol #{'module}  :macro #'fortran-module :emit :macro :type :def :section :code}
        :print   {:op :print   :symbol #{'print}   :macro #'fortran-print :emit :macro}

        })))

(def +template+
  (->> {:token {:comment {:start "!"}
                :string  {:quote "'"}}
        :data {:vector {:start "(/ " :end " /)" :sep ", "}
               :tuple  {:start "(" :end ")" :sep ", "}}
        :block {:do {:start "" :end "" :sep "\n" :indent false}} ;; do block in clojure is just grouping
        :control {:if {:start "IF (" :end "END IF"}
                  }
        }
       (h/merge-nested (emit/default-grammar))))

(defn fortran-emit-if
  "custom if emission"
  [[_ test then else] grammar mopts]
  (let [test-str (emit/emit-main test grammar mopts)
        then-block (emit/emit-main then grammar mopts)
        else-block (if else (emit/emit-main else grammar mopts))]
    (str "IF (" test-str ") THEN\n"
         (str/indent then-block 2)
         (when else
           (str "\nELSE\n" (str/indent else-block 2)))
         "\nEND IF")))

(defn fortran-emit-for
  "custom do loop emission"
  [[_ [var start end step] body] grammar mopts]
  (let [v (emit/emit-main var grammar mopts)
        s (emit/emit-main start grammar mopts)
        e (emit/emit-main end grammar mopts)
        st (if step (str ", " (emit/emit-main step grammar mopts)) "")
        b (emit/emit-main body grammar mopts)]
    (str "DO " v " = " s ", " e st "\n"
         (str/indent b 2)
         "\nEND DO")))

;; Patching if and for into grammar
(def +grammar+
  (grammar/grammar :fortran
    (grammar/to-reserved
     (-> +features+
         (grammar/build:override
          {:if {:emit #'fortran-emit-if}
           :for {:emit #'fortran-emit-for :symbol #{'do:loop}}})))
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name _ opts]
                        (h/$ (:- "USE" ~name)))
    :module-export    (fn [{:keys [as refer]} opts])}))

(def +book+
  (book/book {:lang :fortran
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
