(ns rt.llvm.grammar
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lib :as h]
            [std.string :as str]))

(defn tf-define
  "transforms llvm define"
  {:added "4.0"}
  [[_ ret-type name args & body]]
  (let [args-str (if (vector? args)
                   (str "("
                        (str/join ", " (map (fn [[t n]]
                                              (str (common/emit-common t preprocess/*macro-grammar* {})
                                                   " "
                                                   (common/emit-common n preprocess/*macro-grammar* {})))
                                            (partition 2 args)))
                        ")")
                   (common/emit-common args preprocess/*macro-grammar* {}))
        name-str (common/emit-common name preprocess/*macro-grammar* {})]
    (list :- "define" ret-type (str name-str args-str)
          (list :- "{"
                (list \\
                      \\ (list \| (apply list 'do body)))
                (list :- "\n}")))))

(defn tf-declare
  "transforms llvm declare"
  {:added "4.0"}
  [[_ ret-type name args]]
  (let [args-str (if (vector? args)
                   (str "("
                        (str/join ", " (map (fn [x]
                                              (common/emit-common x preprocess/*macro-grammar* {}))
                                            args))
                        ")")
                   (common/emit-common args preprocess/*macro-grammar* {}))
        name-str (common/emit-common name preprocess/*macro-grammar* {})]
    (list :- "declare" ret-type (str name-str args-str))))

(defn tf-label
  "transforms label"
  {:added "4.0"}
  [[_ name]]
  (list :- (str (common/emit-common name preprocess/*macro-grammar* {}) ":")))

(defn tf-ret
  "transforms ret"
  {:added "4.0"}
  [[_ type val]]
  (if val
    (list :- "ret" type val)
    (list :- "ret" type)))

(defn tf-assign
  "transforms assignment"
  {:added "4.0"}
  [[_ left right]]
  (list :- left "=" right))

(defn tf-inst-bin
  "helper for binary instructions"
  {:added "4.0"}
  [op]
  (fn [[_ type op1 op2]]
    (list :- op type (str (common/emit-common op1 preprocess/*macro-grammar* {}) ",") op2)))

(defn tf-icmp
  "transforms icmp"
  {:added "4.0"}
  [[_ cond type op1 op2]]
  (list :- "icmp" cond type (str (common/emit-common op1 preprocess/*macro-grammar* {}) ",") op2))

(defn tf-br
  "transforms br"
  {:added "4.0"}
  [[_ & args]]
  (if (= 1 (count args))
    (list :- "br" "label" (first args))
    (let [[cond true-label false-label] args
          cond-str (str (common/emit-common cond preprocess/*macro-grammar* {}) ",")
          true-label-str (str "label " (common/emit-common true-label preprocess/*macro-grammar* {}) ",")
          false-label-str (str "label " (common/emit-common false-label preprocess/*macro-grammar* {}))]
      (list :- "br" "i1" cond-str true-label-str false-label-str))))

(defn tf-call
  "transforms call"
  {:added "4.0"}
  [[_ ret-type name args]]
  (let [args-str (str "("
                      (str/join ", " (map (fn [arg]
                                            (if (vector? arg)
                                              (let [[t v] arg]
                                                (str (common/emit-common t preprocess/*macro-grammar* {})
                                                     " "
                                                     (common/emit-common v preprocess/*macro-grammar* {})))
                                              (common/emit-common arg preprocess/*macro-grammar* {})))
                                          args))
                      ")")]
    (list :- "call" ret-type (str (common/emit-common name preprocess/*macro-grammar* {}) args-str))))

(defn tf-alloca
  "transforms alloca"
  {:added "4.0"}
  [[_ type & [count]]]
  (if count
    (list :- "alloca" type "," (first count) (second count)) ;; assuming count is [type val]
    (list :- "alloca" type)))

(defn tf-store
  "transforms store"
  {:added "4.0"}
  [[_ type val ptr-type ptr]]
  (list :- "store" type (str (common/emit-common val preprocess/*macro-grammar* {}) ",") ptr-type ptr))

(defn tf-load
  "transforms load"
  {:added "4.0"}
  [[_ type ptr-type ptr]]
  (list :- "load" (str (common/emit-common type preprocess/*macro-grammar* {}) ",") ptr-type ptr))

(def +features+
  (-> (grammar/build :include [:builtin :builtin-helper :free-control :control-base])
      (grammar/build:extend
       {:define  {:op :define :symbol #{'define} :macro #'tf-define :emit :macro :section :code :type :def}
        :declare {:op :declare :symbol #{'declare} :macro #'tf-declare :emit :macro :section :code :type :def}
        :label   {:op :label :symbol #{'label} :macro #'tf-label :emit :macro}
        :ret     {:op :ret :symbol #{'ret} :macro #'tf-ret :emit :macro}
        :assign  {:op :assign :symbol #{:=} :macro #'tf-assign :emit :macro}

        ;; Arithmetic
        :add     {:op :add :symbol #{'add} :macro (tf-inst-bin "add") :emit :macro}
        :sub     {:op :sub :symbol #{'sub} :macro (tf-inst-bin "sub") :emit :macro}
        :mul     {:op :mul :symbol #{'mul} :macro (tf-inst-bin "mul") :emit :macro}
        :div     {:op :div :symbol #{'sdiv 'udiv 'fdiv} :macro (fn [[sym & args]] (apply (tf-inst-bin (name sym)) nil args)) :emit :macro}
        :rem     {:op :rem :symbol #{'srem 'urem 'frem} :macro (fn [[sym & args]] (apply (tf-inst-bin (name sym)) nil args)) :emit :macro}

        ;; Logic/Compare
        :icmp    {:op :icmp :symbol #{'icmp} :macro #'tf-icmp :emit :macro}

        ;; Control
        :br      {:op :br :symbol #{'br} :macro #'tf-br :emit :macro}
        :call    {:op :call :symbol #{'call} :macro #'tf-call :emit :macro}

        ;; Memory
        :alloca  {:op :alloca :symbol #{'alloca} :macro #'tf-alloca :emit :macro}
        :store   {:op :store :symbol #{'store} :macro #'tf-store :emit :macro}
        :load    {:op :load :symbol #{'load} :macro #'tf-load :emit :macro}})))

(def +template+
  (->> {:default {:common    {:statement "" :start "" :end "" :sep "\n"}
                  :block     {:statement "" :start "" :end "" :sep "\n"}
                  :define    {:raw ""}
                  :function  {:raw ""}}
        :token   {:symbol    {:replace {\- "_"}}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :llvm
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta {}))

(def +book+
  (book/book {:lang :llvm
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
