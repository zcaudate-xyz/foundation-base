tahto/model/spec_xtalk/fn_go_test.clj:1:(ns tahto.model.spec-xtalk.fn-go-test
tahto/model/spec_xtalk/fn_go_test.clj:2:  (:require [tahto.model.spec-xtalk.fn-go :refer :all])
  (:use code.test))

tahto/model/spec_xtalk/fn_go_test.clj:5:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-len :added "4.1"}
(fact "go-tf-x-len"
  (go-tf-x-len '(x:len arr))
  => '(len arr))

tahto/model/spec_xtalk/fn_go_test.clj:10:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-cat :added "4.1"}
(fact "go-tf-x-cat"
  (go-tf-x-cat '(x:cat "a" "b"))
  => '(+ "a" "b"))

tahto/model/spec_xtalk/fn_go_test.clj:15:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-print :added "4.1"}
(fact "go-tf-x-print"
  (go-tf-x-print '(x:print "hello"))
  => '(fmt.Println "hello"))

tahto/model/spec_xtalk/fn_go_test.clj:20:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-m-abs :added "4.1"}
(fact "go-tf-x-m-abs"
  (go-tf-x-m-abs '(x:m-abs n))
  => '(math.Abs n))

tahto/model/spec_xtalk/fn_go_test.clj:25:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-m-max :added "4.1"}
(fact "go-tf-x-m-max"
  (go-tf-x-m-max '(x:m-max a b))
  => '(math.Max a b))

tahto/model/spec_xtalk/fn_go_test.clj:30:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-m-min :added "4.1"}
(fact "go-tf-x-m-min"
  (go-tf-x-m-min '(x:m-min a b))
  => '(math.Min a b))

tahto/model/spec_xtalk/fn_go_test.clj:35:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-m-ceil :added "4.1"}
(fact "go-tf-x-m-ceil"
  (go-tf-x-m-ceil '(x:m-ceil n))
  => '(math.Ceil n))

tahto/model/spec_xtalk/fn_go_test.clj:40:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-m-floor :added "4.1"}
(fact "go-tf-x-m-floor"
  (go-tf-x-m-floor '(x:m-floor n))
  => '(math.Floor n))

tahto/model/spec_xtalk/fn_go_test.clj:45:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-m-sqrt :added "4.1"}
(fact "go-tf-x-m-sqrt"
  (go-tf-x-m-sqrt '(x:m-sqrt n))
  => '(math.Sqrt n))

tahto/model/spec_xtalk/fn_go_test.clj:50:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-m-pow :added "4.1"}
(fact "go-tf-x-m-pow"
  (go-tf-x-m-pow '(x:m-pow b e))
  => '(math.Pow b e))

tahto/model/spec_xtalk/fn_go_test.clj:55:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-str-split :added "4.1"}
(fact "go-tf-x-str-split"
  (go-tf-x-str-split '(x:str-split s sep))
  => '(strings.Split s sep))

tahto/model/spec_xtalk/fn_go_test.clj:60:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-str-join :added "4.1"}
(fact "go-tf-x-str-join"
  (go-tf-x-str-join '(x:str-join sep arr))
  => '(strings.Join arr sep))

tahto/model/spec_xtalk/fn_go_test.clj:65:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-str-index-of :added "4.1"}
(fact "go-tf-x-str-index-of"
  (go-tf-x-str-index-of '(x:str-index-of s sub))
  => '(strings.Index s sub))

tahto/model/spec_xtalk/fn_go_test.clj:70:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-str-to-upper :added "4.1"}
(fact "go-tf-x-str-to-upper"
  (go-tf-x-str-to-upper '(x:str-to-upper s))
  => '(strings.ToUpper s))

tahto/model/spec_xtalk/fn_go_test.clj:75:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-str-to-lower :added "4.1"}
(fact "go-tf-x-str-to-lower"
  (go-tf-x-str-to-lower '(x:str-to-lower s))
  => '(strings.ToLower s))

tahto/model/spec_xtalk/fn_go_test.clj:80:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-str-trim :added "4.1"}
(fact "go-tf-x-str-trim"
  (go-tf-x-str-trim '(x:str-trim s))
  => '(strings.TrimSpace s))

tahto/model/spec_xtalk/fn_go_test.clj:85:^{:refer tahto.model.spec-xtalk.fn-go/go-tf-x-arr-push :added "4.1"}
(fact "go-tf-x-arr-push"
  (go-tf-x-arr-push '(x:arr-push arr item))
  => '(:= arr (append arr item)))
