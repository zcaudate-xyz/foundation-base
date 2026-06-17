(ns hara.model.annex.spec-verilog
  (:require [clojure.string]
            [hara.lang.book :as book]
            [hara.common.emit :as emit]
            [hara.common.emit-fn :as emit-fn]
            [hara.common.emit-helper :as helper]
            [hara.common.grammar :as grammar]
            [hara.lang.script :as script]
            [hara.common.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.template :as template]))

(defn tf-module
  "transforms module definition"
  {:added "4.0"}
  [[_ sym args & body]]
  (let [ports (if (and (list? args) (= 'quote (first args)))
                (second args)
                args)
        ports-str (str "(" (clojure.string/join ", " (map ut/sym-default-str ports)) ")")]
    (list :- "module" sym ports-str ";"
          (list :- "\n")
          (list \\
                \\ (list \| (apply list 'do body)))
          (list :- "\nendmodule"))))

(defn tf-assign
  "transforms assign"
  {:added "4.0"}
  [[_ left right]]
  (list :- "assign" left "=" right ";"))

(defn tf-initial
  "transforms initial block"
  {:added "4.0"}
  [[_ & body]]
  (list :- "initial"
        (list :- "begin")
        (list \\
              \\ (list \| (apply list 'do body)))
        (list :- "\nend")))

(defn tf-always
  "transforms always block"
  {:added "4.0"}
  [[_ trigger & body]]
  (let [trigger-str (if (vector? trigger)
                      (str "(" (clojure.string/join " " (map ut/sym-default-str trigger)) ")")
                      (str "(" (ut/sym-default-str trigger) ")"))]
    (list :- "always" (str "@" trigger-str)
          (list :- "begin")
          (list \\
                \\ (list \| (apply list 'do body)))
          (list :- "\nend"))))

(defn tf-non-blocking
  "transforms non-blocking assignment <="
  {:added "4.0"}
  [[_ left right]]
  (list :- left "<=" right ";"))

(defn tf-blocking
  "transforms blocking assignment ="
  {:added "4.0"}
  [[_ left right]]
  (list :- left "=" right ";"))

(defn tf-reg
  "transforms reg declaration"
  {:added "4.0"}
  [[_ & args]]
  (let [type "reg"
        decl (if (vector? (first args))
               (let [[range name] args]
                 (str "[" (first range) ":" (second range) "] " (ut/sym-default-str name)))
               (ut/sym-default-str (first args)))]
    (list :- type (str decl ";"))))

(defn tf-wire
  "transforms wire declaration"
  {:added "4.0"}
  [[_ & args]]
  (let [type "wire"
        decl (if (vector? (first args))
               (let [[range name] args]
                 (str "[" (first range) ":" (second range) "] " (ut/sym-default-str name)))
               (ut/sym-default-str (first args)))]
    (list :- type (str decl ";"))))

(defn tf-delay
  "transforms delay #10"
  {:added "4.0"}
  [[_ val]]
  (list :- (str "#" val ";")))

(defn tf-concatenation
  "transforms concatenation {a, b}"
  {:added "4.0"}
  [[_ & args]]
  (list :- (str "{" (clojure.string/join ", " (map ut/sym-default-str args)) "}")))

(defn tf-port
  "transforms port declarations (input, output, inout)"
  {:added "4.0"}
  [dir [_ & args]]
  (let [decl (if (vector? (first args))
               (let [[range name] args]
                 (str "[" (first range) ":" (second range) "] " (ut/sym-default-str name)))
               (ut/sym-default-str (first args)))]
    (list :- dir (str decl ";"))))

(defn tf-input
  "transforms input declaration"
  {:added "4.0"}
  [form]
  (tf-port "input" form))

(defn tf-output
  "transforms output declaration"
  {:added "4.0"}
  [form]
  (tf-port "output" form))

(defn tf-inout
  "transforms inout declaration"
  {:added "4.0"}
  [form]
  (tf-port "inout" form))

(defn tf-display
  "transforms $display(...)"
  {:added "4.0"}
  [[_ & args]]
  (let [arg-strs (map (fn [x]
                        (cond (string? x) (str "\"" x "\"")
                              (number? x) (str x)
                              :else (ut/sym-default-str x)))
                      args)]
    (list :- (str "$display(" (clojure.string/join ", " arg-strs) ")"))))

(defn tf-finish
  "transforms $finish"
  {:added "4.0"}
  [_]
  (list :- "$finish;"))

(defn tf-parameter
  "transforms parameter NAME = VALUE"
  {:added "4.0"}
  [[_ name val]]
  (list :- "parameter" (str (ut/sym-default-str name) " = " (ut/sym-default-str val) ";")))

(defn tf-localparam
  "transforms localparam NAME = VALUE"
  {:added "4.0"}
  [[_ name val]]
  (list :- "localparam" (str (ut/sym-default-str name) " = " (ut/sym-default-str val) ";")))

(def +features+
  (-> (grammar/build :exclude [:data-shortcuts
                               :control-try-catch
                               :class
                               :macro-arrow])
      (grammar/build:override
       {:defn    {:macro #'tf-module :emit :macro}
        :lte     {:macro #'tf-non-blocking :emit :macro}
        :seteq   {:macro #'tf-blocking :emit :macro}})
      (grammar/build:extend
       {:assign    {:op :assign :symbol #{'assign} :macro #'tf-assign :emit :macro :section :code :type :def}
        :initial   {:op :initial :symbol #{'initial} :macro #'tf-initial :emit :macro :section :code :type :def}
        :always    {:op :always :symbol #{'always} :macro #'tf-always :emit :macro :section :code :type :def}
        :reg       {:op :reg :symbol #{'reg} :macro #'tf-reg :emit :macro :section :code :type :def}
        :wire      {:op :wire :symbol #{'wire} :macro #'tf-wire :emit :macro :section :code :type :def}
        :input     {:op :input :symbol #{'input} :macro #'tf-input :emit :macro :section :code :type :def}
        :output    {:op :output :symbol #{'output} :macro #'tf-output :emit :macro :section :code :type :def}
        :inout     {:op :inout :symbol #{'inout} :macro #'tf-inout :emit :macro :section :code :type :def}
        :parameter {:op :parameter :symbol #{'parameter} :macro #'tf-parameter :emit :macro :section :code :type :def}
        :localparam {:op :localparam :symbol #{'localparam} :macro #'tf-localparam :emit :macro :section :code :type :def}
        :display   {:op :display :symbol #{'$display} :macro #'tf-display :emit :macro}
        :finish    {:op :finish :symbol #{'$finish} :macro #'tf-finish :emit :macro}
        :delay     {:op :delay :symbol #{'delay} :macro #'tf-delay :emit :macro}
        :cat       {:op :cat :symbol #{'cat} :macro #'tf-concatenation :emit :macro}})))

(def +template+
  (collection/merge-nested
   helper/+default+
   {:banned #{:set :map :regex}
    :highlight '#{return break}
    :default {:function  {:raw ""}
              :common    {:statement ""}}
    :data    {:vector    {:start "" :end "" :space ""}
              :tuple     {:start "{" :end "}" :space ""}}
    :block  {:for       {:parameter {:sep ";"}}
             :script    {:start "" :end ""}
             :body      {:sep "\n" :end ""}
             :branch    {:wrap    {:start "" :end ""}
                         :control {:default {:parameter  {:start " (" :end ") "}
                                             :body {:start "begin" :end "end"}}
                                   :if      {:raw "if"}
                                   :else    {:raw "else "}}}}
    :define {:def       {:raw ""}}}))

(def +grammar+
  (grammar/grammar :verilog
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name _ opts]
                        (template/$ (:- "`include" ~(str "\"" name ".v\"")))) ;; Simple include
    :module-export    (fn [{:keys [as refer]} opts])}))

(def +book+
  (book/book {:lang :verilog
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
