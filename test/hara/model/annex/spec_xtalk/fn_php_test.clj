(ns hara.model.annex.spec-xtalk.fn-php-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.model.annex.spec-xtalk.fn-php :refer :all]))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-len :added "4.1"}
(fact "returns count of array"
  (php-tf-x-len '(:x-len arr))
  => '(count arr))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (php-tf-x-cat '(:x-cat "a" "b"))
  => '(concat "a" "b"))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-apply :added "4.1"}
(fact "applies function to array"
  (php-tf-x-apply '(:x-apply f args))
  => '(call_user_func_array f args))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-shell :added "4.1"}
(fact "executes shell command"
  (php-tf-x-shell '(:x-shell "ls" {}))
  => '(shell_exec "ls"))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-random :added "4.1"}
(fact "generates random number"
  (php-tf-x-random '(:x-random))
  => '(/ (rand 0 (getrandmax)) (getrandmax)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-type-native :added "4.1"}
(fact "gets native type"
  (php-tf-x-type-native '(:x-type-native obj))
  => '(gettype obj))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-err :added "4.1"}
(fact "throws exception"
  (php-tf-x-err '(:x-err "message"))
  => '(throw (new Exception "message")))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-eval :added "4.1"}
(fact "evaluates code"
  (php-tf-x-eval '(:x-eval "code"))
  => '(eval "code"))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-print :added "4.1"}
(fact "prints values"
  (php-tf-x-print '(:x-print "hello"))
  => '(var_dump "hello"))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-now-ms :added "4.1"}
(fact "gets current time in ms"
  (php-tf-x-now-ms '(:x-now-ms))
  => '(* 1000 (microtime true)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-has-key? :added "4.1"}
(fact "checks that array has key"
  (php-tf-x-has-key? '(:x-has-key? obj k nil))
  => '(array_key_exists k obj))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-m-max :added "4.1"}
(fact "returns maximum"
  (php-tf-x-m-max '(:x-m-max 1 2 3))
  => '(max 1 2 3))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-m-min :added "4.1"}
(fact "returns minimum"
  (php-tf-x-m-min '(:x-m-min 1 2 3))
  => '(min 1 2 3))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-m-mod :added "4.1"}
(fact "returns modulo"
  (php-tf-x-m-mod '(:x-m-mod 10 3))
  => '(:% 10 (:- " % ") 3))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-m-quot :added "4.1"}
(fact "returns quotient"
  (php-tf-x-m-quot '(:x-m-quot 10 3))
  => '(floor (/ 10 3)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-is-string? :added "4.1"}
(fact "checks if string"
  (php-tf-x-is-string? '(:x-is-string? x))
  => '(is_string x))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-is-number? :added "4.1"}
(fact "checks if number"
  (php-tf-x-is-number? '(:x-is-number? x))
  => '(or (is_int x) (is_float x)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-is-integer? :added "4.1"}
(fact "checks if integer"
  (php-tf-x-is-integer? '(:x-is-integer? x))
  => '(is_int x))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-is-boolean? :added "4.1"}
(fact "checks if boolean"
  (php-tf-x-is-boolean? '(:x-is-boolean? x))
  => '(is_bool x))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-is-object? :added "4.1"}
(fact "checks if object"
  (php-tf-x-is-object? '(:x-is-object? x))
  => '(and (is_array x) (not (array_is_list x))))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-is-array? :added "4.1"}
(fact "checks if array"
  (php-tf-x-is-array? '(:x-is-array? x))
  => '(and (is_array x) (array_is_list x)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-lu-get :added "4.1"}
(fact "gets lookup value via object id"
  (php-tf-x-lu-get '(:x-lu-get lu obj))
  => '(:% lu [(spl_object_id obj)]))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-lu-set :added "4.1"}
(fact "sets lookup value via object id"
  (php-tf-x-lu-set '(:x-lu-set lu obj gid))
  => '(:= (:% lu [(spl_object_id obj)]) gid))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-lu-del :added "4.1"}
(fact "deletes lookup value via object id"
  (php-tf-x-lu-del '(:x-lu-del lu obj))
  => '(unset (:% lu [(spl_object_id obj)])))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-push :added "4.1"}
(fact "pushes to array"
  (php-tf-x-arr-push '(:x-arr-push arr item))
  => '(array_push arr item))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-pop :added "4.1"}
(fact "pops from array"
  (php-tf-x-arr-pop '(:x-arr-pop arr))
  => '(array_pop arr))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-push-first :added "4.1"}
(fact "unshifts to array"
  (php-tf-x-arr-push-first '(:x-arr-push-first arr item))
  => '(array_unshift arr item))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-pop-first :added "4.1"}
(fact "shifts from array"
  (php-tf-x-arr-pop-first '(:x-arr-pop-first arr))
  => '(array_shift arr))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-slice :added "4.1"}
(fact "slices array"
  (php-tf-x-arr-slice '(:x-arr-slice arr 0 5))
  => '(array_slice arr 0 (- 5 0)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-insert :added "4.1"}
(fact "inserts into array"
  (php-tf-x-arr-insert '(:x-arr-insert arr 2 item))
  => '(array_splice arr 2 0 [item]))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-remove :added "4.1"}
(fact "removes from array"
  (php-tf-x-arr-remove '(:x-arr-remove arr 2))
  => '(array_splice arr 2 1))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-sort :added "4.1"}
(fact "sorts array with compare fn"
  (php-tf-x-arr-sort '(:x-arr-sort arr compare-fn))
  => '(usort arr compare-fn))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-comp :added "4.1"}
(fact "compares strings for array sorting"
  (php-tf-x-str-comp '(:x-str-comp a b))
  => '(< (strcmp a b) 0))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-char :added "4.1"}
(fact "gets char code"
  (php-tf-x-str-char '(:x-str-char s 0))
  => '(ord (substr s 0 1)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-split :added "4.1"}
(fact "splits string"
  (php-tf-x-str-split '(:x-str-split s ","))
  => '(explode "," s))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (php-tf-x-str-join '(:x-str-join "," arr))
  => '(implode "," arr))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-index-of :added "4.1"}
(fact "finds substring"
  (php-tf-x-str-index-of '(:x-str-index-of s "abc"))
  => '(strpos s "abc"))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-format :added "4.1"}
(fact "formats strings"
  (php-tf-x-str-format '(:x-str-format "%s-%s" a b))
  => '(sprintf "%s-%s" a b))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-substring :added "4.1"}
(fact "extracts substring"
  (php-tf-x-str-substring '(:x-str-substring s 0 5))
  => '(substr s 0 (- 5 0)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-to-upper :added "4.1"}
(fact "converts to uppercase"
  (php-tf-x-str-to-upper '(:x-str-to-upper s))
  => '(strtoupper s))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-to-lower :added "4.1"}
(fact "converts to lowercase"
  (php-tf-x-str-to-lower '(:x-str-to-lower s))
  => '(strtolower s))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-replace :added "4.1"}
(fact "replaces in string"
  (php-tf-x-str-replace '(:x-str-replace s "old" "new"))
  => '(str_replace "old" "new" s))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-to-fixed :added "4.1"}
(fact "formats number with fixed digits"
  (php-tf-x-str-to-fixed '(:x-str-to-fixed n 2))
  => '(number_format n 2 "." ""))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-trim :added "4.1"}
(fact "trims string"
  (php-tf-x-str-trim '(:x-str-trim s))
  => '(trim s))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-trim-left :added "4.1"}
(fact "left trims string"
  (php-tf-x-str-trim-left '(:x-str-trim-left s))
  => '(ltrim s))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-str-trim-right :added "4.1"}
(fact "right trims string"
  (php-tf-x-str-trim-right '(:x-str-trim-right s))
  => '(rtrim s))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-iter-from-obj :added "4.1"}
(fact "creates iterator from object pairs"
  (l/emit-as :php [(php-tf-x-iter-from-obj '(:x-iter-from-obj obj))])
  => #"array_keys")

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-iter-from-arr :added "4.1"}
(fact "creates iterator from array"
  (php-tf-x-iter-from-arr '(:x-iter-from-arr arr))
  => '(new ArrayIterator arr))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-iter-from :added "4.1"}
(fact "creates iterator from generic value"
  (php-tf-x-iter-from '(:x-iter-from obj))
  => '(new ArrayIterator obj))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-iter-eq :added "4.1"}
(fact "compares iterator values"
  (l/emit-as :php [(php-tf-x-iter-eq '(:x-iter-eq it0 it1 eq-fn))])
  => #"valid")

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-iter-next :added "4.1"}
(fact "advances iterator"
  (php-tf-x-iter-next '(:x-iter-next it))
  => '(do (var $res := (. it (current)))
          (. it (next))
          (return $res)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-iter-has? :added "4.1"}
(fact "checks iterator readiness"
  (php-tf-x-iter-has? '(:x-iter-has? it))
  => '(array_is_list it))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-iter-native? :added "4.1"}
(fact "checks native iterator representation"
  (php-tf-x-iter-native? '(:x-iter-native? it))
  => '(instanceof it ArrayIterator))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-socket-connect :added "4.1"}
(fact "connects socket"
  (php-tf-x-socket-connect '(:x-socket-connect host port))
  => '(fsockopen host port))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-socket-send :added "4.1"}
(fact "sends socket payload"
  (php-tf-x-socket-send '(:x-socket-send conn value))
  => '(fwrite conn value))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-socket-close :added "4.1"}
(fact "closes socket"
  (php-tf-x-socket-close '(:x-socket-close conn))
  => '(fclose conn))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-with-delay :added "4.1"}
(fact "with delay emits sleep"
  (l/emit-as :php [(php-tf-x-with-delay '[_ 10 thunk])])
  => #"usleep")

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-file-slurp :added "4.1"}
(fact "reads file contents")

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-file-spit :added "4.1"}
(fact "writes file contents")

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-return-encode :added "4.1"}
(fact "return encode"
  (l/emit-as :php [(php-tf-x-return-encode '[_ out id key])])
  => #"json_encode")

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-return-wrap :added "4.1"}
(fact "return wrap"
  (l/emit-as :php [(php-tf-x-return-wrap '[_ f encode-fn])])
  => #"json_encode")

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-return-eval :added "4.1"}
(fact "return eval"
  (l/emit-as :php [(php-tf-x-return-eval '[_ s wrap-fn])])
  => #"eval")


^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-ex-new :added "4.1"}
(fact "creates plain exceptions or attaches an explicit data payload"
  (php-tf-x-ex-new '(_ "boom")) => '(new Exception "boom")
  (php-tf-x-ex-new '(_ "boom" data))
  => '(do (var e := (new Exception "boom"))
          (:= e->data data)
          (return e)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-ex-message :added "4.1"}
(fact "reads a native exception message"
  (php-tf-x-ex-message '(_ err)) => '(. err getMessage))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-ex-data :added "4.1"}
(fact "reads an exception's attached data"
  (php-tf-x-ex-data '(_ err)) => '(. err ["data"]))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-ex-native? :added "4.1"}
(fact "checks PHP's Throwable contract"
  (php-tf-x-ex-native? '(_ err)) => '(instanceof err Throwable))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-async-run :added "4.1"}
(fact "runs asynchronous thunks synchronously on PHP"
  (php-tf-x-async-run '(_ thunk)) => '(call_user_func_array thunk []))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-m-pow :added "4.1"}
(fact "lowers exponentiation to PHP's power operator"
  (php-tf-x-m-pow '(_ base exponent)) => '(call_user_func_array "pow" [base exponent]))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-obj-keys :added "4.1"}
(fact "returns associative-array keys"
  (php-tf-x-obj-keys '(_ obj)) => '(array_keys obj))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-obj-vals :added "4.1"}
(fact "returns associative-array values"
  (php-tf-x-obj-vals '(_ obj)) => '(array_values obj))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-obj-pairs :added "4.1"}
(fact "builds key/value pairs in source iteration order"
  (php-tf-x-obj-pairs '(_ obj))
  => '((fn []
         (var $out := [])
         (for:object [[$k $v] obj]
           (x:arr-push $out [$k $v]))
         (return $out))))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-obj-clone :added "4.1"}
(fact "clones an associative array without retaining identity"
  (php-tf-x-obj-clone '(_ obj)) => '(array_merge [] obj))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-obj-assign :added "4.1"}
(fact "merges object fields with right-hand precedence"
  (php-tf-x-obj-assign '(_ obj fields)) => '(array_merge obj fields))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-lu-eq :added "4.1"}
(fact "uses strict identity for lookup equality"
  (php-tf-x-lu-eq '(_ left right)) => '(=== left right))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-for-array :added "4.1"}
(fact "lowers value, index, and index/value array iteration"
  (php-tf-for-array '(for:array [value arr] (use value)))
  => '(foreach [(array_values arr) value] (use value))
  (php-tf-for-array '(for:array [[_ value] arr] (use value)))
  => '(foreach [(array_values arr) value] (use value))
  (php-tf-for-array '(for:array [[index _] arr] (use index)))
  => '(foreach [(array_keys arr) index] (use index))
  (php-tf-for-array '(for:array [[index value] arr] (use index value)))
  => '(foreach [(array_keys arr) index]
       (var value := (:% arr [index]))
       (use index value)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-for-index :added "4.1"}
(fact "lowers exclusive numeric ranges through PHP range"
  (php-tf-for-index '(for:index [i [0 4 2]] (use i)))
  => '(foreach [(range 0 (- 4 2) 2) i] (use i)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-for-object :added "4.1"}
(fact "lowers object iteration through keys or values as requested"
  (php-tf-for-object '(for:object [value obj] (use value)))
  => '(foreach [(array_values obj) value] (use value))
  (php-tf-for-object '(for:object [[key value] obj] (use key value)))
  => '(foreach [(array_keys obj) key]
       (var value := (:% obj [key]))
       (use key value)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-for-iter :added "4.1"}
(fact "lowers generic iteration directly to foreach"
  (php-tf-for-iter '(for:iter [item iterable] (use item)))
  => '(foreach [iterable item] (use item)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-assign :added "4.1"}
(fact "appends every source item and returns the destination"
  (php-tf-x-arr-assign '(_ target source))
  => '(do (for:array [e source]
            (x:arr-push target e))
          (return target)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-clone :added "4.1"}
(fact "clones a PHP array"
  (php-tf-x-arr-clone '(_ arr)) => '(array_merge [] arr))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-each :added "4.1"}
(fact "walks each array item with the supplied function"
  (php-tf-x-arr-each '(_ arr f)) => '(array_walk arr f))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-every :added "4.1"}
(fact "short-circuits false and otherwise returns true"
  (php-tf-x-arr-every '(_ arr pred))
  => '((fn []
         (for:array [$e arr]
           (if (not (pred $e))
             (return false)))
         (return true))))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-some :added "4.1"}
(fact "short-circuits true and otherwise returns false"
  (php-tf-x-arr-some '(_ arr pred))
  => '((fn []
         (for:array [$e arr]
           (if (pred $e)
             (return true)))
         (return false))))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-map :added "4.1"}
(fact "maps and normalizes numeric keys"
  (php-tf-x-arr-map '(_ arr f)) => '(array_values (array_map f arr)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-filter :added "4.1"}
(fact "filters and normalizes numeric keys"
  (php-tf-x-arr-filter '(_ arr pred))
  => '(array_values (array_filter arr pred)))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-foldl :added "4.1"}
(fact "reduces arrays from the left"
  (php-tf-x-arr-foldl '(_ arr f init)) => '(array_reduce arr f init))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-foldr :added "4.1"}
(fact "reverses before reducing from the right"
  (php-tf-x-arr-foldr '(_ arr f init))
  => '(array_reduce (array_reverse arr) f init))

^{:refer hara.model.annex.spec-xtalk.fn-php/php-tf-x-arr-find :added "4.1"}
(fact "returns the first matching index or minus one"
  (php-tf-x-arr-find '(_ arr pred))
  => '((fn []
         (for:array [$i (array_keys arr)]
           (if (pred (:% arr [$i]))
             (return $i)))
         (return -1))))
