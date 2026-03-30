(ns std.lang.model-annex.spec-xtalk.fn-ruby-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-xtalk.fn-ruby :refer :all]))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-len :added "4.1"}
(fact "returns array length"
  (ruby-tf-x-len '(:x-len arr))
  => '(. arr length))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (ruby-tf-x-cat '(:x-cat "a" "b"))
  => '(+ "a" "b"))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-print :added "4.1"}
(fact "prints values"
  (ruby-tf-x-print '(:x-print "hello"))
  => '(puts "hello"))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-random :added "4.1"}
(fact "generates random number"
  (ruby-tf-x-random '(:x-random))
  => '(rand))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-now-ms :added "4.1"}
(fact "returns current time in ms"
  (ruby-tf-x-now-ms '(:x-now-ms))
  => '(. (* (. Time.now to_f) 1000) to_i))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-mod :added "4.1"}
(fact "returns modulo"
  (ruby-tf-x-m-mod '(:x-m-mod 10 3))
  => '(% 10 3))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-push :added "4.1"}
(fact "pushes to array"
  (ruby-tf-x-arr-push '(:x-arr-push arr item))
  => '(. arr (push item)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-pop :added "4.1"}
(fact "pops from array"
  (ruby-tf-x-arr-pop '(:x-arr-pop arr))
  => '(. arr (pop)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-push-first :added "4.1"}
(fact "unshifts to array"
  (ruby-tf-x-arr-push-first '(:x-arr-push-first arr item))
  => '(. arr (unshift item)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-pop-first :added "4.1"}
(fact "shifts from array"
  (ruby-tf-x-arr-pop-first '(:x-arr-pop-first arr))
  => '(. arr (shift)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-insert :added "4.1"}
(fact "inserts at index"
  (ruby-tf-x-arr-insert '(:x-arr-insert arr 2 item))
  => '(. arr (insert 2 item)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-remove :added "4.1"}
(fact "removes at index"
  (ruby-tf-x-arr-remove '(:x-arr-remove arr 2))
  => '(. arr (delete_at 2)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-sort :added "4.1"}
(fact "sorts array"
  (ruby-tf-x-arr-sort '(:x-arr-sort arr key comp))
  => '(. arr (sort!)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-split :added "4.1"}
(fact "splits string"
  (ruby-tf-x-str-split '(:x-str-split s ","))
  => '(. s (split ",")))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (ruby-tf-x-str-join '(:x-str-join "," arr))
  => '(. arr (join ",")))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-index-of :added "4.1"}
(fact "finds substring index"
  (ruby-tf-x-str-index-of '(:x-str-index-of s "abc"))
  => '(. s (index "abc")))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-substring :added "4.1"}
(fact "extracts substring"
  (ruby-tf-x-str-substring '(:x-str-substring s 0 5))
  => '(. s (slice 0 5))

  (ruby-tf-x-str-substring '(:x-str-substring s 0))
  => '(. s (slice 0)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-to-upper :added "4.1"}
(fact "converts to uppercase"
  (ruby-tf-x-str-to-upper '(:x-str-to-upper s))
  => '(. s (upcase)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-to-lower :added "4.1"}
(fact "converts to lowercase"
  (ruby-tf-x-str-to-lower '(:x-str-to-lower s))
  => '(. s (downcase)))


^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-return-encode :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-return-wrap :added "4.1"}
(fact "TODO")

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-return-eval :added "4.1"}
(fact "TODO")