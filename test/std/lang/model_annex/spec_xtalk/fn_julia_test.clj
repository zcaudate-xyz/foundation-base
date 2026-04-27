(ns std.lang.model-annex.spec-xtalk.fn-julia-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-xtalk.fn-julia :refer :all]))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-del :added "4.1"}
(fact "deletes an element from a collection"
  (julia-tf-x-del '(:x-del obj))
  => '(delete! obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-eval :added "4.1"}
(fact "evaluates a string as Julia code"
  (julia-tf-x-eval '(:x-eval "1 + 1"))
  => '(eval (Meta.parse "1 + 1")))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-get-key :added "4.1"}
(fact "gets a value from a dict with an explicit or implicit default"
  (julia-tf-x-get-key '(:x-get-key obj key default))
  => '(get obj key default)

  (julia-tf-x-get-key '(:x-get-key obj key))
  => '(get obj key nil))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-apply :added "4.1"}
(fact "applies a function to a list of arguments"
  (julia-tf-x-apply '(:x-apply f args))
  => '(f (... args))

  (julia-tf-x-unpack '(:x-unpack args))
  => '(... args))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-type-native :added "4.1"}
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

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-ceil :added "4.1"}
(fact "returns the ceiling of a number"
  (julia-tf-x-m-ceil '(:x-m-ceil 3.14))
  => '(ceil Int 3.14))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-m-floor :added "4.1"}
(fact "returns the floor of a number"
  (julia-tf-x-m-floor '(:x-m-floor 3.14))
  => '(floor Int 3.14))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-to-number :added "4.1"}
(fact "converts a string to a number"
  (julia-tf-x-to-number '(:x-to-number "123.45"))
  => '(parse Float64 "123.45"))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-is-string? :added "4.1"}
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

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-obj-keys :added "4.1"}
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
  => '(do
        (var out obj)
        (when (== out nil)
          (:= out (Dict())))
        (if (== m nil)
          (return out)
          (return (merge out m)))))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-arr-slice :added "4.1"}
(fact "returns array helpers"
  (julia-tf-x-arr-slice '(:x-arr-slice arr start end))
  => '(. arr [(to (x:offset start) 1 end)])

  (julia-tf-x-arr-insert '(:x-arr-insert arr idx item))
  => '(insert! arr idx item)

  (julia-tf-x-arr-remove '(:x-arr-remove arr idx))
  => '(splice! arr (x:offset idx))

  (julia-tf-x-arr-sort '(:x-arr-sort arr key-fn compare-fn))
  => '(:- "sort!(" (% arr) ", by = " (% key-fn) ", lt = " (% compare-fn) ")"))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-str-char :added "4.1"}
(fact "returns string helpers"
  (julia-tf-x-str-char '(:x-str-char s i))
  => '(Int (x:get-idx s i))

  (julia-tf-x-str-join '(:x-str-join "-" arr))
  => '(join arr "-")

  (julia-tf-x-str-index-of '(:x-str-index-of s "abc"))
  => '(:-
       "begin\n"
        "local idx = findnext("
        (% "abc")
        ", "
        (% s)
        ", "
        (% (x:offset 0))
        ")\n"
        "if idx === nothing\n"
        "  nothing\n"
        "else\n"
        "  (isa(idx, Integer) ? Int(idx) - 1 : Int(first(idx)) - 1)\n"
        "end\n"
        "end")

  (julia-tf-x-str-substring '(:x-str-substring s start end))
  => '(. s [(to start 1 end)])

  (julia-tf-x-str-replace '(:x-str-replace s "-" "/"))
  => '(replace s (=> "-" "/")))

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-return-encode :added "4.1"}
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

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-return-wrap :added "4.1"}
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

^{:refer std.lang.model-annex.spec-xtalk.fn-julia/julia-tf-x-return-eval :added "4.1"}
(fact "evaluates code with a wrapper function"
  (julia-tf-x-return-eval '(:x-return-eval s wrap-fn))
  => '(return
       (wrap-fn
        (fn []
          (return (include_string Main s))))))
