(ns hara.lang.model-annex.spec-xtalk.fn-perl-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.lang.model-annex.spec-xtalk.fn-perl :refer :all]))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-len :added "4.1"}
(fact "returns scalar length"
  (perl-tf-x-len '(:x-len arr))
  => '(scalar arr))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-cat :added "4.1"}
(fact "concatenates strings"
  (perl-tf-x-cat '(:x-cat "a" "b"))
  => '(concat "a" "b"))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-print :added "4.1"}
(fact "prints with newline"
  (perl-tf-x-print '(:x-print "hello"))
  => '(print "hello" "\n"))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-m-abs :added "4.1"}
(fact "returns absolute value"
  (perl-tf-x-m-abs '(:x-m-abs -5))
  => '(abs -5))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-m-cos :added "4.1"}
(fact "returns cosine"
  (perl-tf-x-m-cos '(:x-m-cos 0))
  => '(cos 0))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-m-sin :added "4.1"}
(fact "returns sine"
  (perl-tf-x-m-sin '(:x-m-sin 0))
  => '(sin 0))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-m-sqrt :added "4.1"}
(fact "returns square root"
  (perl-tf-x-m-sqrt '(:x-m-sqrt 16))
  => '(sqrt 16))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-m-log :added "4.1"}
(fact "returns natural log"
  (perl-tf-x-m-log '(:x-m-loge 10))
  => '(log 10))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-m-exp :added "4.1"}
(fact "returns e^x"
  (perl-tf-x-m-exp '(:x-m-exp 1))
  => '(exp 1))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-m-pow :added "4.1"}
(fact "returns power"
  (perl-tf-x-m-pow '(:x-m-pow 2 3))
  => '(** 2 3))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-arr-push :added "4.1"}
(fact "pushes to array"
  (perl-tf-x-arr-push '(:x-arr-push arr item))
  => '(push arr item))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-arr-pop :added "4.1"}
(fact "pops from array"
  (perl-tf-x-arr-pop '(:x-arr-pop arr))
  => '(pop arr))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-arr-push-first :added "4.1"}
(fact "unshifts to array"
  (perl-tf-x-arr-push-first '(:x-arr-push-first arr item))
  => '(unshift arr item))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-arr-pop-first :added "4.1"}
(fact "shifts from array"
  (perl-tf-x-arr-pop-first '(:x-arr-pop-first arr))
  => '(shift arr))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-str-split :added "4.1"}
(fact "splits string"
  (perl-tf-x-str-split '(:x-str-split s ","))
  => '(split "," s))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-str-join :added "4.1"}
(fact "joins strings"
  (perl-tf-x-str-join '(:x-str-join "," arr))
  => '(join "," arr))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-str-index-of :added "4.1"}
(fact "finds index of substring"
  (perl-tf-x-str-index-of '(:x-str-index-of s "abc"))
  => '(index s "abc"))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-str-substring :added "4.1"}
(fact "extracts substring"
  (perl-tf-x-str-substring '(:x-str-substring s 0 5))
  => '(substr s 0 5)

  (perl-tf-x-str-substring '(:x-str-substring s 0))
  => '(substr s 0))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-str-to-upper :added "4.1"}
(fact "converts to uppercase"
  (perl-tf-x-str-to-upper '(:x-str-to-upper s))
  => '(uc s))

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-str-to-lower :added "4.1"}
(fact "converts to lowercase"
  (perl-tf-x-str-to-lower '(:x-str-to-lower s))
  => '(lc s))


^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-return-encode :added "4.1"}
(fact "return encode"
  (pr-str (perl-tf-x-return-encode '[_ out id key]))
  => #"encode_json")

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-return-wrap :added "4.1"}
(fact "return wrap"
  (pr-str (perl-tf-x-return-wrap '[_ f encode-fn]))
  => #"encode_json")

^{:refer hara.lang.model-annex.spec-xtalk.fn-perl/perl-tf-x-return-eval :added "4.1"}
(fact "return eval"
  (pr-str (perl-tf-x-return-eval '[_ s wrap-fn]))
  => #"CORE::eval")