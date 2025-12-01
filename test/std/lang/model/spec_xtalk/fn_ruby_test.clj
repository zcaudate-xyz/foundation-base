(ns std.lang.model.spec-xtalk.fn-ruby-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.fn-ruby :refer :all]
            [std.lib :as h]))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-len :added "4.1"}
(fact "ruby-tf-x-len"
  (ruby-tf-x-len '(x:len arr))
  => '(. arr length))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-cat :added "4.1"}
(fact "ruby-tf-x-cat"
  (ruby-tf-x-cat '(x:cat "a" "b"))
  => '(+ "a" "b"))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-print :added "4.1"}
(fact "ruby-tf-x-print"
  (ruby-tf-x-print '(x:print "hello"))
  => '(puts "hello"))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-random :added "4.1"}
(fact "ruby-tf-x-random"
  (ruby-tf-x-random '(x:random))
  => '(rand))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-now-ms :added "4.1"}
(fact "ruby-tf-x-now-ms"
  (ruby-tf-x-now-ms '(x:now-ms))
  => '(. (* (. Time.now to_f) 1000) to_i))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-m-mod :added "4.1"}
(fact "ruby-tf-x-m-mod"
  (ruby-tf-x-m-mod '(x:m-mod a b))
  => '(% a b))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-arr-push :added "4.1"}
(fact "ruby-tf-x-arr-push"
  (ruby-tf-x-arr-push '(x:arr-push arr item))
  => '(. arr (push item)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-arr-pop :added "4.1"}
(fact "ruby-tf-x-arr-pop"
  (ruby-tf-x-arr-pop '(x:arr-pop arr))
  => '(. arr (pop)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-arr-push-first :added "4.1"}
(fact "ruby-tf-x-arr-push-first"
  (ruby-tf-x-arr-push-first '(x:arr-push-first arr item))
  => '(. arr (unshift item)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-arr-pop-first :added "4.1"}
(fact "ruby-tf-x-arr-pop-first"
  (ruby-tf-x-arr-pop-first '(x:arr-pop-first arr))
  => '(. arr (shift)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-arr-insert :added "4.1"}
(fact "ruby-tf-x-arr-insert"
  (ruby-tf-x-arr-insert '(x:arr-insert arr i item))
  => '(. arr (insert i item)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-arr-remove :added "4.1"}
(fact "ruby-tf-x-arr-remove"
  (ruby-tf-x-arr-remove '(x:arr-remove arr i))
  => '(. arr (delete_at i)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-arr-sort :added "4.1"}
(fact "ruby-tf-x-arr-sort"
  (ruby-tf-x-arr-sort '(x:arr-sort arr k c))
  => '(. arr (sort!)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-str-split :added "4.1"}
(fact "ruby-tf-x-str-split"
  (ruby-tf-x-str-split '(x:str-split s tok))
  => '(. s (split tok)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-str-join :added "4.1"}
(fact "ruby-tf-x-str-join"
  (ruby-tf-x-str-join '(x:str-join s arr))
  => '(. arr (join s)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-str-index-of :added "4.1"}
(fact "ruby-tf-x-str-index-of"
  (ruby-tf-x-str-index-of '(x:str-index-of s tok))
  => '(. s (index tok)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-str-substring :added "4.1"}
(fact "ruby-tf-x-str-substring"
  (ruby-tf-x-str-substring '(x:str-substring s start))
  => '(. s (slice start))
  (ruby-tf-x-str-substring '(x:str-substring s start len))
  => '(. s (slice start len)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-str-to-upper :added "4.1"}
(fact "ruby-tf-x-str-to-upper"
  (ruby-tf-x-str-to-upper '(x:str-to-upper s))
  => '(. s (upcase)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-str-to-lower :added "4.1"}
(fact "ruby-tf-x-str-to-lower"
  (ruby-tf-x-str-to-lower '(x:str-to-lower s))
  => '(. s (downcase)))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-return-encode :added "4.1"}
(fact "ruby-tf-x-return-encode"
  (ruby-tf-x-return-encode '(x:return-encode out id key))
  => (contains '(:- "require 'json'")))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-return-wrap :added "4.1"}
(fact "ruby-tf-x-return-wrap"
  (ruby-tf-x-return-wrap '(x:return-wrap f enc))
  => (contains '(:- "require 'json'")))

^{:refer std.lang.model.spec-xtalk.fn-ruby/ruby-tf-x-return-eval :added "4.1"}
(fact "ruby-tf-x-return-eval"
  (ruby-tf-x-return-eval '(x:return-eval s wrap))
  => (contains '(return (wrap (fn [] (return (eval s)))))))
