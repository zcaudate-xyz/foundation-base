(ns hara.model.annex.spec-xtalk.fn-julia-test
  (:use code.test)
  (:require [hara.model.annex.spec-xtalk.fn-julia :refer :all]))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-free-infix :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-global-key :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-free-try-catch :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-free-iife :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-promise-native-check :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-promise-resolve-form :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-promise-reject-form :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-promise-wrap-expr :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-error-value-expr :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-shell-read-expr :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-del :added "4.1"}
(fact "deletes an element from a collection"
  (julia-tf-x-del '(:x-del obj))
  => '(delete! obj))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-get-key :added "4.1"}
(fact "gets a value from a dict with an explicit or implicit default"
  (julia-tf-x-get-key '(:x-get-key obj key default))
  => '(get obj key default)

  (julia-tf-x-get-key '(:x-get-key obj key))
  => '(get obj key nil))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-eval :added "4.1"}
(fact "evaluates a string as Julia code"
  (julia-tf-x-eval '(:x-eval "1 + 1"))
  => '(eval (Meta.parse "1 + 1")))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-apply :added "4.1"}
(fact "applies a function to a list of arguments"
  (julia-tf-x-apply '(:x-apply f args))
  => '(f (... args))

  (julia-tf-x-unpack '(:x-unpack args))
  => '(... args))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-unpack :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-type-native :added "4.1"}
(fact "returns the native type of an object as a string"
  (julia-tf-x-type-native '(:x-type-native obj))
  => '(cond (== obj nil)
            (return nil)
            (isa obj Dict)
            (return "object")
            (isa obj AbstractArray)
            (return "array")
            (isa obj Function)
            (return "function")
            (isa obj Bool)
            (return "boolean")
            (isa obj Number)
            (return "number")
            (isa obj AbstractString)
            (return "string")
            :else
            (return (string (typeof obj)))))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-m-ceil :added "4.1"}
(fact "returns the ceiling of a number"
  (julia-tf-x-m-ceil '(:x-m-ceil 3.14))
  => '(ceil Int 3.14))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-m-floor :added "4.1"}
(fact "returns the floor of a number"
  (julia-tf-x-m-floor '(:x-m-floor 3.14))
  => '(floor Int 3.14))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-to-number :added "4.1"}
(fact "converts a string to a number"
  (julia-tf-x-to-number '(:x-to-number "123.45"))
  => '(parse Float64 "123.45"))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-string? :added "4.1"}
(fact "checks Julia type predicates"
  (julia-tf-x-is-string? '(:x-is-string? x))
  => '(isa x String)

  (julia-tf-x-is-number? '(:x-is-number? x))
  => '(isa x Number)

  (julia-tf-x-is-integer? '(:x-is-integer? x))
  => '(isa x Integer)

  (julia-tf-x-is-boolean? '(:x-is-boolean? x))
  => '(isa x Bool)

  (julia-tf-x-is-function? '(:x-is-function? x))
  => '(isa x Function)

  (julia-tf-x-is-object? '(:x-is-object? x))
  => '(isa x Dict)

  (julia-tf-x-is-array? '(:x-is-array? x))
  => '(isa x AbstractArray))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-number? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-integer? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-boolean? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-function? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-object? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-array? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-get :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-create :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-set :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-eq :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-obj-keys :added "4.1"}
(fact "returns object helpers"
  (julia-tf-x-obj-keys '(:x-obj-keys obj))
  => '(collect (keys obj))

  (julia-tf-x-obj-vals '(:x-obj-vals obj))
  => '(collect (values obj))

  (julia-tf-x-obj-pairs '(:x-obj-pairs obj))
  => '(collect
       (map (fn [pair]
              (return [(first pair) (last pair)]))
            (collect obj)))

  (julia-tf-x-obj-assign '(:x-obj-assign obj m))
  => '(:-
       "(function()\n"
       (%
        (do
          (var out (if (== obj nil) (Dict) (copy obj)))
          (if
           (not (== m nil))
           (for
            [pair :in (collect m)]
            (:= (. out [(first pair)]) (last pair)))
           nil)
          (return out)))
       "\nend)()"))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-obj-vals :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-obj-pairs :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-obj-assign :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-slice :added "4.1"}
(fact "returns array helpers"
  (julia-tf-x-arr-slice '(:x-arr-slice arr start end))
  => '(. arr [(to (x:offset start) 1 end)])

  (julia-tf-x-arr-insert '(:x-arr-insert arr idx item))
  => '(insert! arr idx item)

  (julia-tf-x-arr-remove '(:x-arr-remove arr idx))
  => '(splice! arr (x:offset idx))

  (julia-tf-x-arr-sort '(:x-arr-sort arr key-fn compare-fn))
  => '(:- "sort!(" (% arr) ", by = " (% key-fn) ", lt = " (% compare-fn) ")"))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-insert :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-remove :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-sort :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-foldr :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-char :added "4.1"}
(fact "returns string helpers"
  (julia-tf-x-str-char '(:x-str-char s i))
  => '(Int (x:get-idx s i))

  (julia-tf-x-str-join '(:x-str-join "-" arr))
  => '(join arr "-")

  (julia-tf-x-str-index-of '(:x-str-index-of s "abc"))
  => '(do
        (var start-idx (:? (or (== nil nil) (< nil 1)) 1 nil))
        (var idx (findnext "abc" s start-idx))
        (return
         (:?
          (== idx nothing)
          -1
          (:? (isa idx Integer) (Int idx) (Int (first idx))))))

  (julia-tf-x-str-substring '(:x-str-substring s start end))
  => '(. s [(to (max 1 start) 1 end)])

  (julia-tf-x-str-replace '(:x-str-replace s "-" "/"))
  => '(replace s (=> "-" "/")))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-index-of :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-substring :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-join :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-replace :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-to-fixed :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-has-key? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-global-set :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-global-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-global-has? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-socket-connect :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-socket-send :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-socket-close :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-notify-http :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-iter-from-obj :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-iter-eq :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-iter-has? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-iter-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-shell :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-file-resolve :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-file-slurp :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-file-spit :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-return-encode :added "4.1"}
(fact "encodes a return value with id and key"
  (julia-tf-x-return-encode '(:x-return-encode out id key))
  => '(:-
       "(function()\n"
       (% (do
            (var type-fn
                 (fn [obj]
                   (cond (== obj nil)
                         (return "nil")
                         (isa obj Dict)
                         (return "object")
                         (isa obj AbstractArray)
                         (return "array")
                         (isa obj Function)
                         (return "function")
                         (isa obj Bool)
                         (return "boolean")
                         (isa obj Number)
                         (return "number")
                         (isa obj AbstractString)
                         (return "string")
                         :else
                         (return (string (typeof obj))))))
            (var ts (type-fn out))
            (if (== ts "function")
              (return (JSON.json {:id id
                                  :key key
                                  :type "raw"
                                  :return ts
                                  :value (string out)}))
              (return (JSON.json {:id id
                                  :key key
                                  :type "data"
                                  :return ts
                                  :value out})))))
       "\nend)()"))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-return-wrap :added "4.1"}
(fact "wraps a function with error handling"
  (julia-tf-x-return-wrap '(:x-return-wrap f encode-fn))
  => '(:-
       "try\n"
       (% (do
            (var out (f))
            (if (applicable encode-fn out)
              (return (encode-fn out))
              (return (encode-fn out nil nil)))))
       "\ncatch "
       "e"
       "\n"
       (% (return (JSON.json {:type "error"
                              :value (sprint showerror e)})))
       "\nend"))

^{:refer hara.model.annex.spec-xtalk.fn-julia/julia-tf-x-return-eval :added "4.1"}
(fact "evaluates code with a wrapper function"
  (julia-tf-x-return-eval '(:x-return-eval s wrap-fn))
  => '(return
       (wrap-fn
        (fn []
          (return (include_string Main s))))))
