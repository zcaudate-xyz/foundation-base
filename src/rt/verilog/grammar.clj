(ns rt.verilog.grammar
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-fn :as emit-fn]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.string :as str]
            [std.lib :as h]))

(defn tf-module
  "transforms module definition"
  {:added "4.0"}
  [[_ sym args & body]]
  (let [ports (if (and (list? args) (= 'quote (first args)))
                (second args)
                args)
        ports-str (str "(" (str/join ", " (map ut/sym-default-str ports)) ")")]
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
                      (str "(" (str/join " " (map ut/sym-default-str trigger)) ")")
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
  (list :- (str "{" (str/join ", " (map ut/sym-default-str args)) "}")))

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
       {:assign  {:op :assign :symbol #{'assign} :macro #'tf-assign :emit :macro :section :code :type :def}
        :initial {:op :initial :symbol #{'initial} :macro #'tf-initial :emit :macro :section :code :type :def}
        :always  {:op :always :symbol #{'always} :macro #'tf-always :emit :macro :section :code :type :def}
        :reg     {:op :reg :symbol #{'reg} :macro #'tf-reg :emit :macro :section :code :type :def}
        :wire    {:op :wire :symbol #{'wire} :macro #'tf-wire :emit :macro :section :code :type :def}
        :delay   {:op :delay :symbol #{'delay} :macro #'tf-delay :emit :macro}
        :cat     {:op :cat :symbol #{'cat} :macro #'tf-concatenation :emit :macro}})))

(def +template+
  (h/merge-nested
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
                        (h/$ (:- "`include" ~(str "\"" name ".v\"")))) ;; Simple include
    :module-export    (fn [{:keys [as refer]} opts])}))

(def +book+
  (book/book {:lang :verilog
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
