tahto/model/annex/spec_xtalk/fn_julia_test.clj:1:(ns tahto.model.annex.spec-xtalk.fn-julia-test
  (:use code.test)
tahto/model/annex/spec_xtalk/fn_julia_test.clj:3:  (:require [tahto.model.annex.spec-xtalk.fn-julia :refer :all]))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:5:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-free-infix :added "4.1"}
(fact "extracts julia infix expressions")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:8:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-global-key :added "4.1"}
(fact "emits julia global key access")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:11:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-free-try-catch :added "4.1"}
(fact "extracts julia try/catch expressions")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:14:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-free-iife :added "4.1"}
(fact "extracts julia iife expressions")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:17:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-promise-native-check :added "4.1"}
(fact "checks native julia promises")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:20:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-promise-resolve-form :added "4.1"}
(fact "builds julia promise resolution forms")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:23:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-promise-reject-form :added "4.1"}
(fact "builds julia promise rejection forms")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:26:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-promise-wrap-expr :added "4.1"}
(fact "wraps julia promise expressions")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:29:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-error-value-expr :added "4.1"}
(fact "builds julia error value expressions")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:32:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-shell-read-expr :added "4.1"}
(fact "reads julia shell expressions")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:35:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-del :added "4.1"}
(fact "deletes an element from a collection"
  (julia-tf-x-del '(:x-del obj))
  => '(delete! obj))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:40:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-get-key :added "4.1"}
(fact "gets a value from a dict with an explicit or implicit default"
  (julia-tf-x-get-key '(:x-get-key obj key default))
  => '(get obj key default)

  (julia-tf-x-get-key '(:x-get-key obj key))
  => '(get obj key nil))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:48:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-eval :added "4.1"}
(fact "evaluates a string as Julia code"
  (julia-tf-x-eval '(:x-eval "1 + 1"))
  => '(eval (Meta.parse "1 + 1")))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:53:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-apply :added "4.1"}
(fact "applies a function to a list of arguments"
  (julia-tf-x-apply '(:x-apply f args))
  => '(f (... args))

  (julia-tf-x-unpack '(:x-unpack args))
  => '(... args))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:61:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-unpack :added "4.1"}
(fact "unpacks values")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:64:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-type-native :added "4.1"}
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

tahto/model/annex/spec_xtalk/fn_julia_test.clj:84:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-m-ceil :added "4.1"}
(fact "returns the ceiling of a number"
  (julia-tf-x-m-ceil '(:x-m-ceil 3.14))
  => '(ceil Int 3.14))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:89:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-m-floor :added "4.1"}
(fact "returns the floor of a number"
  (julia-tf-x-m-floor '(:x-m-floor 3.14))
  => '(floor Int 3.14))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:94:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-to-number :added "4.1"}
(fact "converts a string to a number"
  (julia-tf-x-to-number '(:x-to-number "123.45"))
  => '(parse Float64 "123.45"))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:99:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-string? :added "4.1"}
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

tahto/model/annex/spec_xtalk/fn_julia_test.clj:122:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-number? :added "4.1"}
(fact "checks number type")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:125:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-integer? :added "4.1"}
(fact "checks integer type")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:128:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-boolean? :added "4.1"}
(fact "checks boolean type")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:131:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-function? :added "4.1"}
(fact "checks function type")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:134:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-object? :added "4.1"}
(fact "checks object type")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:137:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-is-array? :added "4.1"}
(fact "checks array type")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:140:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-get :added "4.1"}
(fact "gets lookup table value")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:143:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-create :added "4.1"}
(fact "creates lookup tables")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:146:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-set :added "4.1"}
(fact "sets lookup table value")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:149:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-del :added "4.1"}
(fact "deletes lookup table value")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:152:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-lu-eq :added "4.1"}
(fact "compares lookup tables")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:155:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-obj-keys :added "4.1"}
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

tahto/model/annex/spec_xtalk/fn_julia_test.clj:184:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-obj-vals :added "4.1"}
(fact "lists object values")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:187:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-obj-pairs :added "4.1"}
(fact "lists object pairs")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:190:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-obj-assign :added "4.1"}
(fact "assigns objects")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:193:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-slice :added "4.1"}
(fact "returns array helpers"
  (julia-tf-x-arr-slice '(:x-arr-slice arr start end))
  => '(. arr [(to (x:offset start) 1 end)])

  (julia-tf-x-arr-insert '(:x-arr-insert arr idx item))
  => '(insert! arr idx item)

  (julia-tf-x-arr-remove '(:x-arr-remove arr idx))
  => '(splice! arr (x:offset idx))

  (julia-tf-x-arr-sort '(:x-arr-sort arr key-fn compare-fn))
  => '(:- "sort!(" (% arr) ", by = " (% key-fn) ", lt = " (% compare-fn) ")"))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:207:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-insert :added "4.1"}
(fact "inserts array elements")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:210:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-remove :added "4.1"}
(fact "removes array elements")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:213:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-sort :added "4.1"}
(fact "sorts arrays")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:216:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-arr-foldr :added "4.1"}
(fact "folds arrays right")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:219:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-char :added "4.1"}
(fact "returns string helpers"
  (julia-tf-x-str-char '(:x-str-char s i))
  => '(Int (x:get-idx s i))

  (julia-tf-x-str-join '(:x-str-join "-" arr))
  => '(join arr "-")

  (julia-tf-x-str-index-of '(:x-str-index-of s "abc"))
  => '(:- "(function()\n"
        (% (do
             (var start-idx (:? (or (== nil nil) (< nil 1)) 1 nil))
             (var idx (findnext "abc" s start-idx))
             (return
              (:?
               (== idx nothing)
               -1
               (:? (isa idx Integer)
                   (- (Int idx) 1)
                   (- (Int (first idx)) 1))))))
        "\nend)()")

  (julia-tf-x-str-substring '(:x-str-substring s start end))
  => '(. s [(to (max 1 start) 1 end)])

  (julia-tf-x-str-replace '(:x-str-replace s "-" "/"))
  => '(replace s (=> "-" "/")))

tahto/model/annex/spec_xtalk/fn_julia_test.clj:247:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-index-of :added "4.1"}
(fact "finds substring index")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:250:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-substring :added "4.1"}
(fact "extracts substrings")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:253:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-join :added "4.1"}
(fact "joins strings")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:256:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-replace :added "4.1"}
(fact "replaces substrings")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:259:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-str-to-fixed :added "4.1"}
(fact "formats numbers")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:262:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-has-key? :added "4.1"}
(fact "checks object key")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:265:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-global-set :added "4.1"}
(fact "sets global variables")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:268:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-global-del :added "4.1"}
(fact "deletes global variables")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:271:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-global-has? :added "4.1"}
(fact "checks global variables")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:274:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-socket-connect :added "4.1"}
(fact "connects sockets")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:277:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-socket-send :added "4.1"}
(fact "sends socket data")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:280:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-socket-close :added "4.1"}
(fact "closes sockets")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:283:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-notify-http :added "4.1"}
(fact "notifies via HTTP")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:286:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-iter-from-obj :added "4.1"}
(fact "creates iterators from objects")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:289:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-iter-eq :added "4.1"}
(fact "compares iterators")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:292:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-iter-has? :added "4.1"}
(fact "checks iterator state")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:295:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-iter-native? :added "4.1"}
(fact "checks native iterators")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:298:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-shell :added "4.1"}
(fact "runs shell commands")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:301:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-file-resolve :added "4.1"}
(fact "resolves file paths")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:304:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-file-slurp :added "4.1"}
(fact "reads file contents")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:307:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-file-spit :added "4.1"}
(fact "writes file contents")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:310:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-return-encode :added "4.1"}
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

tahto/model/annex/spec_xtalk/fn_julia_test.clj:348:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-return-wrap :added "4.1"}
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

tahto/model/annex/spec_xtalk/fn_julia_test.clj:365:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-return-eval :added "4.1"}
(fact "evaluates code with a wrapper function"
  (julia-tf-x-return-eval '(:x-return-eval s wrap-fn))
  => '(return
       (wrap-fn
        (fn []
          (return (include_string Main s))))))


tahto/model/annex/spec_xtalk/fn_julia_test.clj:374:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-prototype-create :added "4.1"}
(fact "creates prototypes")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:377:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-prototype-get :added "4.1"}
(fact "gets prototypes")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:380:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-prototype-set :added "4.1"}
(fact "sets prototypes")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:383:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-prototype-method :added "4.1"}
(fact "calls prototype methods")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:386:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-prototype-tostring :added "4.1"}
(fact "converts prototype to string")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:389:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:392:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:395:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:398:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

tahto/model/annex/spec_xtalk/fn_julia_test.clj:401:^{:refer tahto.model.annex.spec-xtalk.fn-julia/julia-tf-x-async-run :added "4.1"}
(fact "runs asynchronously")
