(ns rt.circom.grammar
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.string :as str]
            [std.lib :as h]))

(defn format-string [x]
  (if (string? x) (pr-str x) x))

;;
;; Transformations for Circom Constructs
;;

(defn tf-template
  "Transforms a template definition.
   (template MyTemplate [args] body...) -> template MyTemplate(args) { body... }"
  [[_ sym args & body]]
  (let [args (if (vector? args) args [args])
        args-str (str "("
                      (str/join ", " (map ut/sym-default-str args))
                      ")")]
    (list :- (str "template " (ut/sym-default-str sym) args-str)
          (list :- "{"
                (apply list 'do body)
                (list :- "\n}")))))

(defn tf-component
  "Transforms component instantiation.
   (component c (MyTemplate arg1 arg2)) -> component c = MyTemplate(arg1, arg2);"
  [[_ name [tmpl & args]]]
  (let [call (cons tmpl args)]
    (list :% (list :- "component" name "=" call) (list :- ";"))))

(defn tf-signal
  "Transforms signal declaration.
   (signal input x) -> signal input x;
   (signal output y) -> signal output y;
   (signal z) -> signal z;"
  [[_ & args]]
  (let [[type name] (if (#{ 'input 'output } (first args))
                      [(first args) (second args)]
                      [nil (first args)])
        type-str (if type (str "signal " type) "signal")]
    (list :% (list :- type-str name) (list :- ";"))))

(defn tf-var
  "Transforms var declaration.
   (var x 10) -> var x = 10;"
  [[_ name val]]
  (list :% (list :- "var" name "=" (format-string val)) (list :- ";")))

(defn tf-pragma
  "Transforms pragma.
   (pragma circom 2.0.0) -> pragma circom 2.0.0;"
  [[_ & args]]
  (list :% (apply list :- "pragma" (map format-string args)) (list :- ";")))

(defn tf-include
  "Transforms include.
   (include \"filename.circom\") -> include \"filename.circom\";"
  [[_ filename]]
  (list :% (list :- "include" (format-string filename)) (list :- ";")))

(defn tf-main
  "Transforms main component definition.
   (main {public [a b]} MyTemplate) -> component main {public [a,b]} = MyTemplate();"
  [[_ opts tmpl-call]]
  (let [public-args (get opts :public)
        public-str (if public-args
                     (str "{public [" (str/join "," (map ut/sym-default-str public-args)) "]}")
                     "")
        call (if (seq? tmpl-call) tmpl-call (list tmpl-call))]
    (list :% (list :- "component" "main" public-str "=" call) (list :- ";"))))

(defn tf-constraint
  "Transforms constraints to add semicolon"
  [[op & args]]
  (list :% (list :- (first args) (str op) (second args)) (list :- ";")))

(defn tf-for
  "Transforms for loop.
   (for [i 0 10] ...) -> for (var i = 0; i < 10; i++) ..."
  [[_ [sym start end step] & body]]
  (let [init (list :- "var" sym "=" start)
        cond (list '< sym end)
        step (if step
               (list :- sym "=" sym "+" step)
               (str sym "++"))
        header (list :- "for" "(" init ";" cond ";" step ")")]
    (list :- header
          (list :- "{"
                (apply list 'do body)
                (list :- "\n}")))))

;;
;; Grammar Definition
;;

(def +features+
  (-> (grammar/build :exclude [:data-shortcuts
                               :control-try-catch
                               :class
                               :macro-arrow])
      (grammar/build:override
       {:var       {:op :var :symbol #{'var} :emit :macro :macro #'tf-var :section :code :type :def}
        :for       {:op :for :symbol #{'for} :emit :macro :macro #'tf-for :section :code :type :block}})
      (grammar/build:extend
       {:template  {:op :template :symbol #{'template} :emit :macro :macro #'tf-template :section :code :type :def}
        :component {:op :component :symbol #{'component} :emit :macro :macro #'tf-component :section :code :type :def}
        :signal    {:op :signal :symbol #{'signal} :emit :macro :macro #'tf-signal :section :code :type :def}
        :pragma    {:op :pragma :symbol #{'pragma} :emit :macro :macro #'tf-pragma :section :code :type :def}
        :include   {:op :include :symbol #{'include} :emit :macro :macro #'tf-include :section :code :type :def}
        :main      {:op :main :symbol #{'main} :emit :macro :macro #'tf-main :section :code :type :def}

        ;; Constraint operators
        :assign-constraint {:op :assign-constraint :symbol #{'<==} :emit :macro :macro #'tf-constraint}
        :constraint-assign {:op :constraint-assign :symbol #{'==>} :emit :macro :macro #'tf-constraint}
        :constraint-eq     {:op :constraint-eq :symbol #{'===} :emit :macro :macro #'tf-constraint}
        :assign-signal     {:op :assign-signal :symbol #{'<--} :emit :macro :macro #'tf-constraint}
        :signal-assign     {:op :signal-assign :symbol #{'-->} :emit :macro :macro #'tf-constraint}
        :inc               {:op :inc :symbol #{'++} :emit :postfix :raw "++"}
        })))

(def +template+
  (->> {:banned #{:set :map :regex}
        :highlight '#{return break template component signal var include pragma main}
        :default {:function  {:raw ""}}
        :data    {:vector    {:start "[" :end "]" :space ","}
                  :tuple     {:start "(" :end ")" :space ","}}
        :block  {:for       {:parameter {:sep ";"}}
                 :script    {:start "" :end ""}}
        :define {:def       {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :circom
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name _ opts]
                        (h/$ (include ~name)))
    :module-export    (fn [{:keys [as refer]} opts])}))

(def +book+
  (book/book {:lang :circom
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
