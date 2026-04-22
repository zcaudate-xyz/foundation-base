(ns scripts.extract-formal-specs
  "Extracts complete formal specifications from std.lang for training data.
   
   Sources:
   - std.lang.base.grammar-spec (+op-* definitions)
   - std.lang.base.grammar-macro (macro transformations)
   - std.lang.base.grammar-xtalk (xtalk transforms)
   - std.lang.model.spec-* (language-specific specs)
   
   Usage: lein exec -p src-training/scripts/extract_formal_specs.clj"
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

;; ============================================================
;; EXTRACT GRAMMAR SPEC DEFINITIONS
;; ============================================================

(defn read-grammar-spec-file
  "Reads the grammar-spec file and extracts +op-* definitions"
  []
  (let [content (slurp "src/std/lang/base/grammar_spec.clj")
        ;; Extract def forms for +op-* variables
        op-defs (re-seq #"\(def\s+\+op-([\w-]+\+)\s+\[([^\]]*)\]" content)]
    (into {}
          (map (fn [[_ name body]]
                 [(keyword name)
                  (str "+op-" name "+")])
               op-defs))))

;; ============================================================
;; COMPLETE SPEC REGISTRY
;; Based on actual source files
;; ============================================================

(def +complete-specs+
  "Complete registry of all std.lang specifications"
  {
   ;; =========================================================
   ;; GRAMMAR-SPEC
   ;; =========================================================
   :grammar-spec
   {:source "std.lang.base.grammar-spec"
    :categories
    {:builtin
     {:spec "+op-builtin+"
      :operators [{:op :with-lang :symbol "!:lang" :emit :with-lang}
                  {:op :with-eval :symbol "!:eval" :emit :with-eval}
                  {:op :free :symbol "-" :emit :free :type :free}
                  {:op :squash :symbol "%" :emit :squash}
                  {:op :comment :symbol "#" :emit :comment}
                  {:op :index :symbol "." :emit :index}
                  {:op :quote :symbol "quote" :emit :quote}]}
     
     :math
     {:spec "+op-math+"
      :operators [{:op :add :symbol "+" :emit :infix :raw "+"}
                  {:op :sub :symbol "-" :emit :infix- :raw "-"}
                  {:op :mul :symbol "*" :emit :infix :raw "*"}
                  {:op :div :symbol "/" :emit :infix* :raw "/" :default 1}
                  {:op :pow :symbol "pow" :emit :bi :raw "^"}
                  {:op :mod :symbol "mod" :emit :bi :raw "%"}]}
     
     :compare
     {:spec "+op-compare+"
      :operators [{:op :eq :symbol "==" :emit :bi :raw "=="}
                  {:op :neq :symbol "not=" :emit :bi :raw "!="}
                  {:op :lt :symbol "<" :emit :infix :raw "<"}
                  {:op :lte :symbol "<=" :emit :bi :raw "<="}
                  {:op :gt :symbol ">" :emit :bi :raw ">"}
                  {:op :gte :symbol ">=" :emit :bi :raw ">="}]}
     
     :logic
     {:spec "+op-logic+"
      :operators [{:op :inif :symbol ":?" :emit :infix-if}
                  {:op :not :symbol "not" :emit :pre :raw "!"}
                  {:op :or :symbol "or" :emit :infix :raw "||"}
                  {:op :and :symbol "and" :emit :infix :raw "&&"}]}
     
     :counter
     {:spec "+op-counter+"
      :operators [{:op :incby :symbol ":+=" :emit :assign :raw "+="}
                  {:op :decby :symbol ":-=" :emit :assign :raw "-="}
                  {:op :mulby :symbol ":*=" :emit :assign :raw "*="}
                  {:op :incto :symbol ":++" :emit :pre :raw "++"}
                  {:op :decto :symbol ":--" :emit :pre :raw "--"}]}
     
     :return
     {:spec "+op-return+"
      :operators [{:op :ret :symbol "return" :emit :return :raw "return"}
                  {:op :break :symbol "break" :emit :return :raw "break"}]}
     
     :throw
     {:spec "+op-throw+"
      :operators [{:op :throw :symbol "throw" :emit :return :raw "throw"}]}
     
     :await
     {:spec "+op-await+"
      :operators [{:op :await :symbol "await" :emit :return :raw "await"}]}
     
     :async
     {:spec "+op-async+"
      :operators [{:op :async :symbol "async" :emit :return :raw "async"}]}
     
     :vars
     {:spec "+op-vars+"
      :operators [{:op :seteq :symbol ":=" :emit :assign :raw "="}
                  {:op :var :symbol "var" :emit :def-assign :raw "" :assign "="}]}
     
     :bit
     {:spec "+op-bit+"
      :operators [{:op :bor :symbol "b:|" :emit :infix :raw "|"}
                  {:op :band :symbol "b:&" :emit :infix :raw "&"}
                  {:op :bxor :symbol "b:xor" :emit :infix :raw "^"}
                  {:op :bsr :symbol "b:>>" :emit :bi :raw ">>"}
                  {:op :bsl :symbol "b:<<" :emit :bi :raw "<<"}]}
     
     :pointer
     {:spec "+op-pointer+"
      :operators [{:op :ptr-deref :symbol "&" :raw "&" :emit :pre}
                  {:op :ptr-ref :symbol "*" :raw "*" :emit :pre}]}
     
     :fn
     {:spec "+op-fn+"
      :operators [{:op :fn :symbol "fn" :type :fn
                   :block {:raw "function" :main #{:body}}}]}
     
     :block
     {:spec "+op-block+"
      :operators [{:op :block :symbol "block" :type :block
                   :block {:raw "" :main #{:body}}}]}
     
     :control-base
     {:spec "+op-control-base+"
      :operators [{:op :do* :symbol "do*" :emit :do* :type :block}
                  {:op :do :symbol "do" :emit :do :type :block}]}
     
     :control-general
     {:spec "+op-control-general+"
      :operators [{:op :for :symbol "for" :type :block
                   :block {:main #{:parameter :body}}}
                  {:op :while :symbol "while" :type :block
                   :block {:main #{:parameter :body}}}
                  {:op :branch :symbol "br*" :type :block
                   :block {:main #{}
                           :raw ""
                           :control [[:if {:required true :main #{:parameter :body}}]
                                     [:elseif {:main #{:parameter :body}}]
                                     [:else {:main #{:body}}]]}}]}
     
     :control-try-catch
     {:spec "+op-control-try-catch+"
      :operators [{:op :try :symbol "try" :type :block
                   :block {:main #{:body}
                           :control [[:catch {:required true :main #{:parameter :body}}]
                                     [:finally {:main #{:body}}]]}}]}
     
     :top-base
     {:spec "+op-top-base+"
      :operators [{:op :defn :symbol "defn" :spec :defn :type :def :section :code :fn true}
                  {:op :def :symbol "def" :spec :def :type :def :section :code}
                  {:op :defrun :symbol "defrun" :spec :defrun :type :def :section :code}]}
     
     :top-global
     {:spec "+op-top-global+"
      :operators [{:op :defglobal :symbol "defglobal" :spec :def :type :def :section :code :raw ""}]}
     
     :class
     {:spec "+op-class+"
      :operators [{:op :new :symbol "new" :emit :new :value true :raw "new"}
                  {:op :defclass :symbol "defclass" :spec :defclass :type :def :section :code :abstract true}
                  {:op :this :symbol "this" :emit :throw :raw "this" :value true}
                  {:op :super :symbol "super" :emit :invoke :raw "super" :value true}]}
     
     :for
     {:spec "+op-for+"
      :operators [{:op :for-index :symbol "for:index" :emit :macro :macro "tf-for-index" :style/indent 1}
                  {:op :for-object :symbol "for:object" :emit :abstract :style/indent 1}
                  {:op :for-array :symbol "for:array" :emit :abstract :style/indent 1}
                  {:op :for-iter :symbol "for:iter" :emit :abstract :style/indent 1}
                  {:op :for-return :symbol "for:return" :emit :abstract :style/indent 1}
                  {:op :for-async :symbol "for:async" :emit :abstract :style/indent 1}
                  {:op :for-try :symbol "for:try" :emit :abstract :style/indent 1}]}
     
     :coroutine
     {:spec "+op-coroutine+"
      :operators [{:op :defgen :symbol "defgen" :spec :defgen :type :def :section :code :abstract true :fn true}
                  {:op :yield :symbol "yield" :emit :return :raw "yield"}]}}}
   
   ;; =========================================================
   ;; GRAMMAR-MACRO
   ;; =========================================================
   :grammar-macro
   {:source "std.lang.base.grammar-macro"
    :macros
    [{:macro "if" :symbol "if" :emit :macro :fn "tf-if" :type :block
      :transform "(if cond then else) => (br* (if cond then) (else else))"}
     {:macro "when" :symbol "when" :emit :macro :fn "tf-when" :type :block
      :transform "(when cond body...) => (br* (if cond body...))"}
     {:macro "cond" :symbol "cond" :emit :macro :fn "tf-cond" :type :block
      :transform "(cond c1 r1 c2 r2 :else r3) => (br* (if c1 r1) (elseif c2 r2) (else r3))"}
     {:name "let" :symbol "let" :macro-fn "tf-let-bind" :type :macro
      :transform "(let [x 10 y 20] body) => (do* (var x := 10) (var y := 20) body)"}
     {:macro "case" :symbol "case" :emit :macro :fn "tf-case" :type :block
      :transform "(case val opts...) => (switch [val] (case [...] ...) (default ...))"}
     {:macro "fn:>" :symbol "fn:>" :emit :macro :fn "tf-lambda-arrow" :type :template
      :transform "(fn:> args body) => (fn args body) with implicit return"}
     {:macro "do:>" :symbol "do:>" :emit :macro :fn "tf-do-arrow" :type :template
      :transform "(do:> body...) => ((fn [] body...))"}
     {:name "xor" :symbol "xor" :macro-fn "tf-xor" :type :macro
      :transform "(xor a b) => (:? a b (not b))"}
     {:macro "forange" :symbol "forange" :emit :macro :fn "tf-forange" :type :block :style/indent 1
      :transform "(forange [i 10] body) => (for [(var i 0) (< i 10) [(:= i (+ i 1))]] body)"}
     {:macro "->" :symbol "->" :emit :macro :fn "tf-macroexpand" :type :template
      :transform "Thread-first: (-> x (f a)) => (f x a)"}
     {:macro "->>" :symbol "->>" :emit :macro :fn "tf-macroexpand" :type :template
      :transform "Thread-last: (->> x (f a)) => (f a x)"}
     {:macro "doto" :symbol "doto" :emit :macro :fn "tf-doto" :type :block
      :transform "(doto obj forms...) => (do (. obj form1) (. obj form2) ...)"}]}
   
   ;; =========================================================
   ;; GRAMMAR-XTALK
   ;; =========================================================
   :grammar-xtalk
   {:source "std.lang.base.grammar-xtalk"
    :transforms
    [{:name "tf-throw" :form "(throw obj)" :expands-to "(throw obj)"}
     {:name "tf-eq-nil?" :form "(x:nil? obj)" :expands-to "(== nil obj)"}
     {:name "tf-not-nil?" :form "(x:not-nil? obj)" :expands-to "(not= nil obj)"}
     {:name "tf-proto-create" :form "(x:proto-create m)" :expands-to "(return m)"}
     {:name "tf-has-key?" :form "(x:has-key? obj key check)" :expands-to "(not= (x:get-key obj key) nil)"}
     {:name "tf-get-path" :form "(x:get-path obj ks default)" :expands-to "(. obj ks) with or default"}
     {:name "tf-get-key" :form "(x:get-key obj k default)" :expands-to "(. obj [k]) with or default"}
     {:name "tf-set-key" :form "(x:set-key obj k v)" :expands-to "(:= (. obj [k]) v)"}
     {:name "tf-del-key" :form "(x:del-key obj k)" :expands-to "(x:del (. obj [k]))"}
     {:name "tf-copy-key" :form "(x:copy-key dst src idx)" :expands-to "(tf-set-key ...)"}
     {:name "tf-offset" :form "(x:offset n)" :expands-to "(+ n offset)"}
     {:name "tf-offset-rev" :form "(x:offset-rev n)" :expands-to "(+ n (- offset 1))"}
     {:name "tf-offset-len" :form "(x:offset-len n)" :expands-to "(+ n (if end-inclusive 0 -1))"}
     {:name "tf-global-set" :form "(x:global-set sym val)" :expands-to "(x:set-key !:G (str sym) val)"}
     {:name "tf-global-has?" :form "(x:global-has? sym)" :expands-to "(not (x:nil? (x:get-key !:G (str sym))))"}
     {:name "tf-global-del" :form "(x:global-del sym val)" :expands-to "(x:set-key !:G (str sym) nil)"}
     {:name "tf-lu-eq" :form "(x:lu-eq o1 o2)" :expands-to "(== o1 o2)"}
     {:name "tf-bit-and" :form "(x:bit-and i1 i2)" :expands-to "(b:& i1 i2)"}
     {:name "tf-bit-or" :form "(x:bit-or i1 i2)" :expands-to "(b:| i1 i2)"}
     {:name "tf-bit-lshift" :form "(x:bit-lshift x n)" :expands-to "(b:<< x n)"}
     {:name "tf-bit-rshift" :form "(x:bit-rshift x n)" :expands-to "(b:>> x n)"}]}
   
   ;; =========================================================
   ;; XTALK PRIMITIVES (from spec-xtalk/fn-*.clj)
   ;; =========================================================
   :xtalk-primitives
   {:source "std.lang.model.spec-xtalk"
    :categories
    {:core
     {:primitives [:x-del :x-cat :x-len :x-err :x-eval :x-apply :x-unpack
                   :x-print :x-random :x-shell :x-now-ms :x-type-native]
      :emit-types {:x-del :alias :x-cat :macro :x-len :macro :x-err :alias
                   :x-eval :alias :x-apply :macro :x-unpack :alias
                   :x-print :alias :x-random :alias :x-shell :macro
                   :x-now-ms :alias :x-type-native :macro}}
     
     :proto
      {:primitives [:x-proto-get :x-proto-set :x-proto-create :x-proto-tostring]
       :emit-types {:x-proto-get :macro :x-proto-set :macro
                    :x-proto-create :macro :x-proto-tostring :unit}}
     
     :math
     {:primitives [:x-m-abs :x-m-acos :x-m-asin :x-m-atan :x-m-ceil :x-m-cos
                   :x-m-cosh :x-m-exp :x-m-floor :x-m-loge :x-m-log10
                   :x-m-max :x-m-min :x-m-mod :x-m-pow :x-m-quot
                   :x-m-sin :x-m-sinh :x-m-sqrt :x-m-tan :x-m-tanh]
      :emit-types {:x-m-abs :alias :x-m-acos :alias :x-m-asin :alias
                   :x-m-ceil :alias :x-m-cos :alias :x-m-floor :alias
                   :x-m-loge :alias :x-m-max :macro :x-m-min :macro
                   :x-m-mod :macro :x-m-pow :alias :x-m-quot :macro}}
     
     :type
     {:primitives [:x-to-string :x-to-number :x-is-string? :x-is-number?
                   :x-is-integer? :x-is-boolean? :x-is-function? :x-is-object?
                   :x-is-array?]
      :emit-types {:x-to-string :alias :x-to-number :alias :x-is-string? :macro
                   :x-is-number? :macro :x-is-integer? :macro :x-is-boolean? :macro
                   :x-is-function? :macro :x-is-object? :macro :x-is-array? :alias}}
     
     :lu
     {:primitives [:x-lu-create :x-lu-get :x-lu-set :x-lu-del]
      :emit-types {:x-lu-create :default :x-lu-get :macro :x-lu-set :macro :x-lu-del :macro}}
     
     :obj
     {:primitives [:x-obj-keys :x-obj-vals :x-obj-pairs :x-obj-clone :x-obj-assign]
      :emit-types {:x-obj-keys :macro :x-obj-vals :macro :x-obj-pairs :macro
                   :x-obj-clone :macro :x-obj-assign :macro}}
     
     :arr
     {:primitives [:x-arr-clone :x-arr-slice :x-arr-reverse :x-arr-push-first
                   :x-arr-pop-first :x-arr-insert :x-arr-sort :x-arr-remove
                   :x-arr-push :x-arr-pop]
      :emit-types {:x-arr-clone :macro :x-arr-slice :macro :x-arr-reverse :macro
                   :x-arr-push-first :macro :x-arr-pop-first :macro :x-arr-insert :macro
                   :x-arr-sort :macro :x-arr-remove :macro :x-arr-push :macro :x-arr-pop :macro}}
     
     :str
     {:primitives [:x-str-split :x-str-join :x-str-index-of :x-str-substring
                   :x-str-to-upper :x-str-to-lower :x-str-replace :x-str-char
                   :x-str-trim :x-str-trim-left :x-str-trim-right]
      :emit-types {:x-str-split :macro :x-str-join :macro :x-str-index-of :macro
                   :x-str-substring :macro :x-str-to-upper :alias :x-str-to-lower :alias
                   :x-str-replace :macro :x-str-char :macro :x-str-trim :macro
                   :x-str-trim-left :macro :x-str-trim-right :macro}}
     
     :json
     {:primitives [:x-json-encode :x-json-decode]
      :emit-types {:x-json-encode :macro :x-json-decode :macro}}
     
     :iter
     {:primitives [:x-iter-from-obj :x-iter-from-arr :x-iter-from :x-iter-next
                   :x-iter-eq :x-iter-has? :x-iter-native?]
      :emit-types {:x-iter-from-obj :macro :x-iter-from-arr :macro :x-iter-from :macro
                   :x-iter-next :macro :x-iter-eq :macro :x-iter-has? :macro :x-iter-native? :macro}}
     
     :task
     {:primitives [:x-task-run :x-task-then :x-task-catch :x-task-finally
                   :x-task-cancel :x-task-status :x-task-await :x-task-from-async]
      :emit-types {:x-task-run :macro :x-task-then :macro :x-task-catch :macro
                   :x-task-finally :macro :x-task-cancel :macro :x-task-status :macro
                   :x-task-await :macro :x-task-from-async :macro}}
     
     :bit
     {:primitives [:x-bit-and :x-bit-or :x-bit-xor :x-bit-lshift :x-bit-rshift]
      :emit-types {:x-bit-and :macro :x-bit-or :macro :x-bit-xor :macro
                   :x-bit-lshift :macro :x-bit-rshift :macro}}
     
     :cache
     {:primitives [:x-cache :x-cache-list :x-cache-flush :x-cache-get
                   :x-cache-set :x-cache-del :x-cache-incr]
      :emit-types {:x-cache :macro :x-cache-list :macro :x-cache-flush :macro
                   :x-cache-get :macro :x-cache-set :macro :x-cache-del :macro :x-cache-incr :macro}}
     
     :thread
     {:primitives [:x-thread-spawn :x-thread-join]
      :emit-types {:x-thread-spawn :macro :x-thread-join :macro}}
     
     :socket
     {:primitives [:x-socket-connect :x-socket-send :x-socket-close]
      :emit-types {:x-socket-connect :macro :x-socket-send :macro :x-socket-close :macro}}
     
     :io
     {:primitives [:x-slurp :x-spit]
      :emit-types {:x-slurp :macro :x-spit :macro}}}}
   
   ;; =========================================================
   ;; LANGUAGE-SPECIFIC SPECS
   ;; =========================================================
   :language-specs
   {:js {:source "std.lang.model.spec-js"
         :file "src/std/lang/model/spec_js.clj"
         :includes [:spec-js/jsx :spec-js/meta :spec-js/qml]
         :features [:js-core :js-proto :js-math :js-type :js-lu :js-obj :js-arr :js-str :js-json]}
    :python {:source "std.lang.model.spec-python"
             :file "src/std/lang/model/spec_python.clj"
             :features [:py-core :py-math :py-type :py-obj :py-arr :py-str]}
    :lua {:source "std.lang.model.spec-lua"
          :file "src/std/lang/model/spec_lua.clj"
          :features [:lua-core :lua-math :lua-type :lua-obj :lua-arr :lua-str]}
    :xtalk {:source "std.lang.model.spec-xtalk"
            :file "src/std/lang/model/spec_xtalk.clj"
            :annex "std.lang.model.spec-xtalk"
            :features [:+features+ :+grammar+ :+meta+ :+book+ :+init+]}}})

;; ============================================================
;; GENERATE TRAINING PAIRS FROM SPECS
;; ============================================================

(defn spec->training-pair
  "Convert a spec definition to a training pair"
  [category spec-def]
  (let [op (:op spec-def)
        symbol (:symbol spec-def)
        emit (:emit spec-def)
        raw (:raw spec-def)
        type-val (:type spec-def)]
    {:category "grammar-spec"
     :subcategory (name category)
     :operator op
     :symbol symbol
     :spec_ref (:spec spec-def)
     :emit_type (or emit type-val :unknown)
     :raw raw
     :example_xtalk (str "(" symbol " args...)")
     :description (str (name (or emit type-val)) " type" (when raw (str " with raw '" raw "'")))}))

(defn generate-all-pairs
  "Generate all training pairs from specs"
  []
  (let [;; Grammar-spec pairs
        grammar-pairs (mapcat (fn [[cat cat-def]]
                                (map #(spec->training-pair cat %)
                                     (:operators cat-def)))
                              (get-in +complete-specs+ [:grammar-spec :categories]))
        
        ;; Macro pairs
        macro-pairs (map (fn [m]
                           {:category "grammar-macro"
                            :subcategory (:type m "macro")
                            :macro (:macro m)
                            :symbol (:symbol m)
                            :spec_ref "grammar-macro"
                            :emit_type (:emit m)
                            :transform (:transform m)
                            :example_xtalk (str "(" (:symbol m) " ...)")
                            :description (str "Macro: " (:transform m))})
                         (get-in +complete-specs+ [:grammar-macro :macros]))
        
        ;; Xtalk transform pairs
        transform-pairs (map (fn [t]
                               {:category "grammar-xtalk"
                                :subcategory "transform"
                                :transform (:name t)
                                :spec_ref "grammar-xtalk"
                                :example_xtalk (:form t)
                                :expands_to (:expands-to t)
                                :description (str "Transform: " (:form t) " => " (:expands-to t))})
                             (get-in +complete-specs+ [:grammar-xtalk :transforms]))
        
        ;; Xtalk primitive pairs
        primitive-pairs (mapcat (fn [[cat cat-def]]
                                  (map (fn [prim]
                                         {:category "xtalk-primitive"
                                          :subcategory (name cat)
                                          :primitive prim
                                          :spec_ref "spec-xtalk"
                                          :emit_type (get-in cat-def [:emit-types prim] :unknown)
                                          :example_xtalk (str "(" (name prim) " ...)")
                                          :description (str "Xtalk primitive: " (name prim))})
                                       (:primitives cat-def)))
                                (get-in +complete-specs+ [:xtalk-primitives :categories]))]
    
    (concat grammar-pairs macro-pairs transform-pairs primitive-pairs)))

;; ============================================================
;; OUTPUT
;; ============================================================

(defn pair->jsonl
  "Convert a pair to JSONL"
  [idx pair]
  (str "{"
       "\"id\":" (inc idx) ","
       "\"category\":\"" (:category pair) "\","
       "\"subcategory\":\"" (:subcategory pair) "\","
       (when (:operator pair) (str "\"operator\":\"" (:operator pair) "\","))
       (when (:macro pair) (str "\"macro\":\"" (:macro pair) "\","))
       (when (:primitive pair) (str "\"primitive\":\"" (:primitive pair) "\","))
       (when (:symbol pair) (str "\"symbol\":\"" (:symbol pair) "\","))
       (when (:transform pair) (str "\"transform\":\"" (:transform pair) "\","))
       "\"spec_ref\":\"" (:spec_ref pair) "\","
       "\"emit_type\":\"" (str (:emit_type pair)) "\","
       "\"example_xtalk\":\"" (:example_xtalk pair) "\","
       "\"description\":\"" (str/replace (:description pair) #"\"" "\\\"") "\""
       "}"))

(defn pairs->jsonl
  "Convert pairs to JSONL"
  [pairs]
  (str/join "\n" (map-indexed pair->jsonl pairs)))

(defn generate-report
  "Generate a summary report"
  [pairs]
  (let [by-cat (group-by :category pairs)
        by-subcat (group-by (juxt :category :subcategory) pairs)]
    (str "FORMAL SPECIFICATION TRAINING DATA REPORT\n"
         "==========================================\n\n"
         "Total Training Pairs: " (count pairs) "\n\n"
         "By Category:\n"
         (str/join "\n"
                   (map (fn [[cat cat-pairs]]
                          (str "  " cat ": " (count cat-pairs) " pairs"))
                        (sort-by key by-cat)))
         "\n\nBy Subcategory:\n"
         (str/join "\n"
                   (map (fn [[[cat subcat] sub-pairs]]
                          (str "  " cat "/" subcat ": " (count sub-pairs)))
                        (sort-by key by-subcat)))
         "\n\nEmit Types Distribution:\n"
         (str/join "\n"
                   (map (fn [[emit emit-pairs]]
                          (str "  " emit ": " (count emit-pairs)))
                        (->> pairs
                             (group-by :emit_type)
                             (sort-by #(count (second %)) >))))
         "\n\nSource Files Referenced:\n"
         "  - std.lang.base.grammar-spec\n"
         "  - std.lang.base.grammar-macro\n"
         "  - std.lang.base.grammar-xtalk\n"
         "  - std.lang.model.spec-js\n"
         "  - std.lang.model.spec-python\n"
         "  - std.lang.model.spec-lua\n"
         "  - std.lang.model.spec-xtalk\n"
         "  - std.lang.model.spec-xtalk.fn-js\n"
         "  - std.lang.model.spec-xtalk.fn-python\n"
         "  - std.lang.model.spec-xtalk.fn-lua\n")))

;; ============================================================
;; MAIN
;; ============================================================

(defn -main
  [& args]
  (println "╔════════════════════════════════════════════════════════════════╗")
  (println "║     EXTRACTING FORMAL SPECIFICATIONS FOR TRAINING             ║")
  (println "║     std.lang Grammar, Macros, and Xtalk Primitives           ║")
  (println "╚════════════════════════════════════════════════════════════════╝")
  
  (let [pairs (generate-all-pairs)
        jsonl-file "training/FORMAL_SPECS_COMPLETE.jsonl"
        report-file "training/FORMAL_SPECS_REPORT.txt"
        jsonl-content (pairs->jsonl pairs)
        report-content (generate-report pairs)]
    
    ;; Write files
    (spit jsonl-file jsonl-content)
    (spit report-file report-content)
    
    (println (str "\n✓ Generated: " (count pairs) " specification entries"))
    (println (str "✓ JSONL: " jsonl-file))
    (println (str "✓ Report: " report-file))
    
    ;; Print report to console
    (println "\n" report-content)
    
    ;; Sample entries
    (println "\n=== SAMPLE ENTRIES ===")
    (doseq [pair (take 5 pairs)]
      (println (str "\n[" (:category pair) "/" (:subcategory pair) "]"))
      (println (str "  Symbol: " (or (:symbol pair) (:macro pair) (:primitive pair))))
      (println (str "  Emit: " (:emit_type pair)))
      (println (str "  Xtalk: " (:example_xtalk pair)))
      (println (str "  Desc: " (:description pair))))
    
    (println "\n✓ Complete formal specification training data generated!")))

;; Run if executed directly
(apply -main *command-line-args*)
