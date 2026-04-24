(ns std.lang.model.spec-xtalk.fn-js-test
  (:require [std.lang :as l]
            [std.lang.model.spec-xtalk.fn-js :refer :all])
  (:use code.test))

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-len :added "4.0"}
(fact "gets length"
  (l/emit-as :js [(js-tf-x-len '[_ arr])])
  => #"\.length")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-cat :added "4.0"}
(fact "concatenates"
  (l/emit-as :js [(js-tf-x-cat '[_ "a" "b"])])
  => #"\+")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-apply :added "4.0"}
(fact "applies function"
  (l/emit-as :js [(js-tf-x-apply '[_ f args])])
  => #"apply")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-shell :added "4.0"}
(fact "executes shell command"
  (l/emit-as :js [(js-tf-x-shell '[_ "ls" opts])])
  => #"child_process")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-random :added "4.0"}
(fact "generates random number"
  (l/emit-as :js [(js-tf-x-random '[_])])
  => #"Math.random")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-type-native :added "4.0"}
(fact "gets native type"

  (js-tf-x-type-native '[_ obj])
  => '(do (when (== obj nil) (return nil))
          (var t := (typeof obj))
          (if
              (== t "object")
            (cond
              (Array.isArray obj)
              (return "array")
              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object") (return "object") (return tn))))
            (return t))))

 (fact "supports staged value-position lowering for native type"
   (l/emit-as :js ['(fn [obj]
                     (return (x:type-native obj)))])
   => #"function \(obj\)\{[\s\S]*typeof obj[\s\S]*Array\.isArray\(obj\)"

   (l/emit-as :js ['(fn [obj f g]
                     (return (f (g (x:type-native obj)))))])
   => #"return f\(g\(\(function \(value\)\{[\s\S]*typeof value[\s\S]*\}\)\(obj\)\)\);"

   (l/emit-as :js ['x:type-native])
   => #"function \(value\)\{[\s\S]*typeof value[\s\S]*Array\.isArray\(value\)")


(comment

  ;; -------
  ;; return case
  (return (x:type-native obj))

  ;;
  (do (when (== obj nil) (return nil))
      (var t := (typeof obj))
      (if (== t "object")
        (cond (Array.isArray obj)
              (return "array")

              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object")
                  (return "object")
                  (return tn))))
        (return t)))
  
  ;; -------
  ;; assign case
  (var a (x:type-native obj))
  (:= a  (x:type-native obj))

  ;;
  (var a nil)
  (do (when (== obj nil) (return nil))
      (var t := (typeof obj))
      (if (== t "object")
        (cond (Array.isArray obj)
              (:= a "array")

              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object")
                  (:= a "object")
                  (:= a tn))))
        (:= a t)))

  ;; -------
  ;; general usage case
  (f (g (x:type-native obj)))

  (var type-native-fn
       (fn type-native-lambda [obj]
         (when (== obj nil) (return nil))
         (var t := (typeof obj))
         (if (== t "object")
           (cond (Array.isArray obj)
                 (return "array")

                 :else
                 (do
                   (var tn := (. obj ["constructor"] ["name"]))
                   (if (== tn "Object")
                     (return "object")
                     (return tn))))
           (return t))))
  (f (g (type-native-fn obj)))

  ;; -------
  ;; standalone
  x:type-native

  ;;
  (fn type-native-lambda [obj]
    (when (== obj nil) (return nil))
    (var t := (typeof obj))
    (if (== t "object")
      (cond (Array.isArray obj)
            (return "array")

            :else
            (do
              (var tn := (. obj ["constructor"] ["name"]))
              (if (== tn "Object")
                (return "object")
                (return tn))))
      (return t)))
  )

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-proto-get :added "4.0"}
(fact "gets prototype"
  (l/emit-as :js [(js-tf-x-proto-get '[_ obj])])
  => #"Object.getPrototypeOf")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-proto-set :added "4.0"}
(fact "sets prototype"
  (l/emit-as :js [(js-tf-x-proto-set '[_ obj proto])])
  => #"Object.setPrototypeOf")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-proto-create :added "4.0"}
(fact "creates prototype"
  (l/emit-as :js [(js-tf-x-proto-create '[_ {:a 1}])])
  => #"\.\.\.args"

  (l/emit-as :js [(js-tf-x-proto-create '[_ {:a 1}])])
  => #"Object.entries")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-m-max :added "4.0"}
(fact "gets max"
  (l/emit-as :js [(js-tf-x-m-max '[_ 1 2])])
  => #"Math.max")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-m-min :added "4.0"}
(fact "gets min"
  (l/emit-as :js [(js-tf-x-m-min '[_ 1 2])])
  => #"Math.min")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-m-mod :added "4.0"}
(fact "gets mod"
  (l/emit-as :js [(js-tf-x-m-mod '[_ 1 2])])
  => #"%")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-m-quot :added "4.0"}
(fact "gets quotient"
  (l/emit-as :js [(js-tf-x-m-quot '[_ 1 2])])
  => #"Math.floor")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-is-string? :added "4.0"}
(fact "checks if string"
  (l/emit-as :js [(js-tf-x-is-string? '[_ x])])
  => #"typeof")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-is-number? :added "4.0"}
(fact "checks if number"
  (l/emit-as :js [(js-tf-x-is-number? '[_ x])])
  => #"typeof")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-is-integer? :added "4.0"}
(fact "checks if integer"
  (l/emit-as :js [(js-tf-x-is-integer? '[_ x])])
  => #"Number.isInteger")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-is-boolean? :added "4.0"}
(fact "checks if boolean"
  (l/emit-as :js [(js-tf-x-is-boolean? '[_ x])])
  => #"typeof")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-is-object? :added "4.0"}
(fact "checks if object"
  (l/emit-as :js [(js-tf-x-is-object? '[_ x])])
  => #"typeof")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-is-function? :added "4.0"}
(fact "checks if function"
  (l/emit-as :js [(js-tf-x-is-function? '[_ x])])
  => #"typeof")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-lu-get :added "4.0"}
(fact "gets lookup"
  (l/emit-as :js [(js-tf-x-lu-get '[_ lu key])])
  => #"\.get")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-lu-set :added "4.0"}
(fact "sets lookup"
  (l/emit-as :js [(js-tf-x-lu-set '[_ lu key val])])
  => #"\.set")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-lu-del :added "4.0"}
(fact "deletes lookup"
  (l/emit-as :js [(js-tf-x-lu-del '[_ lu key])])
  => #"\.del")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-obj-keys :added "4.0"}
(fact "gets object keys"
  (l/emit-as :js [(js-tf-x-obj-keys '[_ obj])])
  => #"Object.keys")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-obj-vals :added "4.0"}
(fact "gets object values"
  (l/emit-as :js [(js-tf-x-obj-vals '[_ obj])])
  => #"Object.values")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-obj-pairs :added "4.0"}
(fact "gets object pairs"
  (l/emit-as :js [(js-tf-x-obj-pairs '[_ obj])])
  => #"Object.entries")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-obj-clone :added "4.0"}
(fact "clones object"
  (l/emit-as :js [(js-tf-x-obj-clone '[_ obj])])
  => #"Object.assign")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-obj-assign :added "4.0"}
(fact "assigns object"
  (l/emit-as :js [(js-tf-x-obj-assign '[_ obj1 obj2])])
  => #"Object.assign")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-slice :added "4.0"}
(fact "slices array"
  (l/emit-as :js [(js-tf-x-arr-slice '[_ arr 0 1])])
  => #"slice")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-reverse :added "4.0"}
(fact "reverses array"
  (l/emit-as :js [(js-tf-x-arr-reverse '[_ arr])])
  => #"reverse")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-push :added "4.0"}
(fact "pushes to array"
  (l/emit-as :js [(js-tf-x-arr-push '[_ arr 1])])
  => #"push")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-pop :added "4.0"}
(fact "pops from array"
  (l/emit-as :js [(js-tf-x-arr-pop '[_ arr])])
  => #"pop")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-push-first :added "4.0"}
(fact "pushes first"
  (l/emit-as :js [(js-tf-x-arr-push-first '[_ arr 1])])
  => #"unshift")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-pop-first :added "4.0"}
(fact "pops first"
  (l/emit-as :js [(js-tf-x-arr-pop-first '[_ arr])])
  => #"shift")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-insert :added "4.0"}
(fact "inserts into array"
  (l/emit-as :js [(js-tf-x-arr-insert '[_ arr 0 1])])
  => #"splice")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-remove :added "4.0"}
(fact "removes from array"
  (l/emit-as :js [(js-tf-x-arr-remove '[_ arr 0])])
  => #"splice")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-sort :added "4.0"}
(fact "sorts array"
  (l/emit-as :js [(js-tf-x-arr-sort '[_ arr key-fn comp-fn])])
  => #"sort")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-clone :added "4.0"}
(fact "clones array"
  (l/emit-as :js [(js-tf-x-arr-clone '[_ arr])])
  => #"slice")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-assign :added "4.1"}
(fact "assigns into the original array"
  (l/emit-as :js [(js-tf-x-arr-assign '[_ arr other])])
  => #"push")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-concat :added "4.1"}
(fact "concatenates into a new array"
  (l/emit-as :js [(js-tf-x-arr-concat '[_ arr other])])
  => #"concat")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-each :added "4.0"}
(fact "iterates array"
  (l/emit-as :js [(js-tf-x-arr-each '[_ arr f])])
  => #"forEach")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-every :added "4.0"}
(fact "checks every element"
  (l/emit-as :js [(js-tf-x-arr-every '[_ arr pred])])
  => #"every")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-some :added "4.0"}
(fact "checks some element"
  (l/emit-as :js [(js-tf-x-arr-some '[_ arr pred])])
  => #"some")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-map :added "4.0"}
(fact "maps array"
  (l/emit-as :js [(js-tf-x-arr-map '[_ arr f])])
  => #"map")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-filter :added "4.0"}
(fact "filters array"
  (l/emit-as :js [(js-tf-x-arr-filter '[_ arr pred])])
  => #"filter")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-foldl :added "4.0"}
(fact "reduces from the left"
  (l/emit-as :js [(js-tf-x-arr-foldl '[_ arr f init])])
  => #"reduce")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-foldr :added "4.0"}
(fact "reduces from the right"
  (l/emit-as :js [(js-tf-x-arr-foldr '[_ arr f init])])
  => #"reduceRight")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-arr-find :added "4.0"}
(fact "finds array index"
  (l/emit-as :js [(js-tf-x-arr-find '[_ arr pred])])
  => #"findIndex")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-comp :added "4.0"}
(fact "compares strings"
  (l/emit-as :js [(js-tf-x-str-comp '[_ a b])])
  => #"localeCompare")

^{:refer std.lang.model.spec-xtalk.fn-js/+js+ :added "4.0"}
(fact "registers functional array templates"
  (mapv (fn [k] (get-in +js+ [k :macro]))
        [:x-arr-clone :x-arr-each :x-arr-every :x-arr-some
          :x-arr-map :x-arr-filter :x-arr-keep
          :x-arr-foldl :x-arr-foldr :x-arr-find :x-arr-sort])
  => [#'js-tf-x-arr-clone #'js-tf-x-arr-each #'js-tf-x-arr-every #'js-tf-x-arr-some
       #'js-tf-x-arr-map #'js-tf-x-arr-filter
       #'js-tf-x-arr-keep
       #'js-tf-x-arr-foldl #'js-tf-x-arr-foldr #'js-tf-x-arr-find #'js-tf-x-arr-sort])

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-char :added "4.0"}
(fact "gets char"
  (l/emit-as :js [(js-tf-x-str-char '[_ s 0])])
  => #"charCodeAt")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-split :added "4.0"}
(fact "splits string"
  (l/emit-as :js [(js-tf-x-str-split '[_ s " "])])
  => #"split")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-join :added "4.0"}
(fact "joins string"
  (l/emit-as :js [(js-tf-x-str-join '[_ s arr])])
  => #"join")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-index-of :added "4.0"}
(fact "index of"
  (l/emit-as :js [(js-tf-x-str-index-of '[_ s "a"])])
  => #"indexOf")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-substring :added "4.0"}
(fact "substring"
  (l/emit-as :js [(js-tf-x-str-substring '[_ s 0 1])])
  => #"substring")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-to-upper :added "4.0"}
(fact "to upper"
  (l/emit-as :js [(js-tf-x-str-to-upper '[_ s])])
  => #"toUpperCase")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-to-lower :added "4.0"}
(fact "to lower"
  (l/emit-as :js [(js-tf-x-str-to-lower '[_ s])])
  => #"toLowerCase")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-to-fixed :added "4.0"}
(fact "to fixed"
  (l/emit-as :js [(js-tf-x-str-to-fixed '[_ n 2])])
  => #"toFixed")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-replace :added "4.0"}
(fact "replaces string"
  (l/emit-as :js [(js-tf-x-str-replace '[_ s "a" "b"])])
  => #"replace")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-trim :added "4.0"}
(fact "trims string"
  (l/emit-as :js [(js-tf-x-str-trim '[_ s])])
  => #"trim")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-trim-left :added "4.0"}
(fact "trims left"
  (l/emit-as :js [(js-tf-x-str-trim-left '[_ s])])
  => #"trimLeft")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-str-trim-right :added "4.0"}
(fact "trims right"
  (l/emit-as :js [(js-tf-x-str-trim-right '[_ s])])
  => #"trimRight")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-return-encode :added "4.0"}
(fact "encodes return"
  (l/emit-as :js [(js-tf-x-return-encode '[_ out id key])])
  => #"JSON.stringify")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-return-wrap :added "4.0"}
(fact "wraps return"
  (l/emit-as :js [(js-tf-x-return-wrap '[_ f encode-fn])])
  => #"try")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-return-eval :added "4.0"}
(fact "evals return"
  (l/emit-as :js [(js-tf-x-return-eval '[_ s wrap-fn])])
  => #"eval")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-socket-connect :added "4.0"}
(fact "connects socket"
  (l/emit-as :js [(js-tf-x-socket-connect '[_ host port opts cb])])
  => #"net.Socket")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-socket-send :added "4.0"}
(fact "sends socket"
  (l/emit-as :js [(js-tf-x-socket-send '[_ conn s])])
  => #"write")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-socket-close :added "4.0"}
(fact "closes socket"
  (l/emit-as :js [(js-tf-x-socket-close '[_ conn])])
  => #"end")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-iter-from-obj :added "4.0"}
(fact "iter from obj"
  (l/emit-as :js [(js-tf-x-iter-from-obj '[_ obj])])
  => #"Symbol.iterator")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-iter-from-arr :added "4.0"}
(fact "iter from arr"
  (l/emit-as :js [(js-tf-x-iter-from-arr '[_ arr])])
  => #"Symbol.iterator")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-iter-from :added "4.0"}
(fact "iter from"
  (l/emit-as :js [(js-tf-x-iter-from '[_ obj])])
  => #"Symbol.iterator")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-iter-eq :added "4.0"}
(fact "iter eq"
  (l/emit-as :js [(js-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"next")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-iter-next :added "4.0"}
(fact "iter next"
  (l/emit-as :js [(js-tf-x-iter-next '[_ it])])
  => #"next")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-iter-has? :added "4.0"}
(fact "iter has?"
  (l/emit-as :js [(js-tf-x-iter-has? '[_ obj])])
  => #"Symbol.iterator")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-iter-native? :added "4.0"}
(fact "iter native?"
  (l/emit-as :js [(js-tf-x-iter-native? '[_ it])])
  => #"next")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-cache :added "4.0"}
(fact "cache"
  (l/emit-as :js [(js-tf-x-cache '[_ "GLOBAL"])])
  => #"localStorage")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-cache-list :added "4.0"}
(fact "cache list"
  (l/emit-as :js [(js-tf-x-cache-list '[_ cache])])
  => #"Object.keys")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-cache-flush :added "4.0"}
(fact "cache flush"
  (l/emit-as :js [(js-tf-x-cache-flush '[_ cache])])
  => #"clear")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-cache-get :added "4.0"}
(fact "cache get"
  (l/emit-as :js [(js-tf-x-cache-get '[_ cache key])])
  => #"getItem")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-cache-set :added "4.0"}
(fact "cache set"
  (l/emit-as :js [(js-tf-x-cache-set '[_ cache key val])])
  => #"setItem")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-cache-del :added "4.0"}
(fact "cache del"
  (l/emit-as :js [(js-tf-x-cache-del '[_ cache key])])
  => #"removeItem")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-cache-incr :added "4.0"}
(fact "cache incr"
  (l/emit-as :js [(js-tf-x-cache-incr '[_ cache key 1])])
  => #"getItem")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-slurp-file :added "4.1"}
(fact "slurp file"
  (l/emit-as :js [(js-tf-x-slurp-file '[_ filename opts cb])])
  => #"readFileSync")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-spit-file :added "4.1"}
(fact "spit file"
  (l/emit-as :js [(js-tf-x-spit-file '[_ filename s opts cb])])
  => #"writeFileSync")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-thread-spawn :added "4.0"}
(fact "thread spawn"
  (l/emit-as :js [(js-tf-x-thread-spawn '[_ thunk])])
  => #"Promise")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-thread-join :added "4.0"}
(fact "thread join"
  (l/emit-as :js [(js-tf-x-thread-join '[_ thread])])
  => #"x:error")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-with-delay :added "4.0"}
(fact "with delay"
  (l/emit-as :js [(js-tf-x-with-delay '[_ thunk 100])])
  => #"setTimeout")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-start-interval :added "4.0"}
(fact "start interval"
  (l/emit-as :js [(js-tf-x-start-interval '[_ thunk 100])])
  => #"setInterval")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-stop-interval :added "4.0"}
(fact "stop interval"
  (l/emit-as :js [(js-tf-x-stop-interval '[_ instance])])
  => #"clearInterval")

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-notify-http :added "4.0"}
(fact "notify http"
  (comment
    (l/emit-as :js [(js-tf-x-notify-http '[_ host port value id key opts])])
    => #"fetch"))


^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-has-key? :added "4.1"}
(fact "has key"
  (l/emit-as :js [(js-tf-x-has-key? '[_ obj "k" nil])])
  => #"\[\"k\"\]")
