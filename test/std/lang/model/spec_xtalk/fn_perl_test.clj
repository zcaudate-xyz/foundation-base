(ns std.lang.model.spec-xtalk.fn-perl-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.fn-perl :refer :all]
            [std.lib :as h]))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-len :added "4.1"}
(fact "perl-tf-x-len"
  (perl-tf-x-len '(x:len arr))
  => '(scalar arr))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-cat :added "4.1"}
(fact "perl-tf-x-cat"
  (perl-tf-x-cat '(x:cat "a" "b"))
  => '(concat "a" "b"))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-print :added "4.1"}
(fact "perl-tf-x-print"
  (perl-tf-x-print '(x:print "hello"))
  => '(print "hello" "\n"))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-m-abs :added "4.1"}
(fact "perl-tf-x-m-abs"
  (perl-tf-x-m-abs '(x:m-abs n))
  => '(abs n))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-m-cos :added "4.1"}
(fact "perl-tf-x-m-cos"
  (perl-tf-x-m-cos '(x:m-cos n))
  => '(cos n))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-m-sin :added "4.1"}
(fact "perl-tf-x-m-sin"
  (perl-tf-x-m-sin '(x:m-sin n))
  => '(sin n))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-m-sqrt :added "4.1"}
(fact "perl-tf-x-m-sqrt"
  (perl-tf-x-m-sqrt '(x:m-sqrt n))
  => '(sqrt n))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-m-log :added "4.1"}
(fact "perl-tf-x-m-log"
  (perl-tf-x-m-log '(x:m-log n))
  => '(log n))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-m-exp :added "4.1"}
(fact "perl-tf-x-m-exp"
  (perl-tf-x-m-exp '(x:m-exp n))
  => '(exp n))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-m-pow :added "4.1"}
(fact "perl-tf-x-m-pow"
  (perl-tf-x-m-pow '(x:m-pow b p))
  => '(** b p))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-arr-push :added "4.1"}
(fact "perl-tf-x-arr-push"
  (perl-tf-x-arr-push '(x:arr-push arr item))
  => '(push arr item))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-arr-pop :added "4.1"}
(fact "perl-tf-x-arr-pop"
  (perl-tf-x-arr-pop '(x:arr-pop arr))
  => '(pop arr))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-arr-push-first :added "4.1"}
(fact "perl-tf-x-arr-push-first"
  (perl-tf-x-arr-push-first '(x:arr-push-first arr item))
  => '(unshift arr item))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-arr-pop-first :added "4.1"}
(fact "perl-tf-x-arr-pop-first"
  (perl-tf-x-arr-pop-first '(x:arr-pop-first arr))
  => '(shift arr))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-str-split :added "4.1"}
(fact "perl-tf-x-str-split"
  (perl-tf-x-str-split '(x:str-split s tok))
  => '(split tok s))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-str-join :added "4.1"}
(fact "perl-tf-x-str-join"
  (perl-tf-x-str-join '(x:str-join s arr))
  => '(join s arr))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-str-index-of :added "4.1"}
(fact "perl-tf-x-str-index-of"
  (perl-tf-x-str-index-of '(x:str-index-of s tok))
  => '(index s tok))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-str-substring :added "4.1"}
(fact "perl-tf-x-str-substring"
  (perl-tf-x-str-substring '(x:str-substring s start))
  => '(substr s start)
  (perl-tf-x-str-substring '(x:str-substring s start len))
  => '(substr s start len))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-str-to-upper :added "4.1"}
(fact "perl-tf-x-str-to-upper"
  (perl-tf-x-str-to-upper '(x:str-to-upper s))
  => '(uc s))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-str-to-lower :added "4.1"}
(fact "perl-tf-x-str-to-lower"
  (perl-tf-x-str-to-lower '(x:str-to-lower s))
  => '(lc s))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-return-encode :added "4.1"}
(fact "perl-tf-x-return-encode"
  (let [res (perl-tf-x-return-encode '(x:return-encode out id key))]
    (first res) => 'do
    (second res) => '(:- "use JSON::PP;")))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-return-wrap :added "4.1"}
(fact "perl-tf-x-return-wrap"
  (let [res (perl-tf-x-return-wrap '(x:return-wrap f enc))]
    (first res) => 'do
    (second res) => '(:- "use JSON::PP;")))

^{:refer std.lang.model.spec-xtalk.fn-perl/perl-tf-x-return-eval :added "4.1"}
(fact "perl-tf-x-return-eval"
  (perl-tf-x-return-eval '(x:return-eval s wrap))
  => (contains '(return (wrap (fn [] (return (eval s)))))))
