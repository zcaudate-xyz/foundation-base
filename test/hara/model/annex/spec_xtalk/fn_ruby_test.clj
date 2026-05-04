(ns hara.model.annex.spec-xtalk.fn-ruby-test
  (:use code.test)
  (:require [clojure.string :as str]
            [hara.lang :as l]
            [hara.model.annex.spec-xtalk.fn-ruby :refer :all]))

^{:refer hara.model.annex.spec-xtalk.fn-ruby/+ruby-promise+ :added "4.1"}
(fact "promise helpers stay wired to local concurrent-ruby macros"
  [(get-in +ruby-promise+ [:x-promise :emit])
   (get-in +ruby-promise+ [:x-promise :macro])
   (get-in +ruby-promise+ [:x-promise-then :macro])
   (get-in +ruby-promise+ [:x-promise-catch :macro])
   (get-in +ruby-promise+ [:x-promise-finally :macro])
   (get-in +ruby-promise+ [:x-promise-native? :macro])
   (get-in +ruby-promise+ [:x-with-delay :macro])]
  => [:macro
      #'ruby-tf-x-promise
      #'ruby-tf-x-promise-then
      #'ruby-tf-x-promise-catch
      #'ruby-tf-x-promise-finally
      #'ruby-tf-x-promise-native?
      #'ruby-tf-x-with-delay])

(fact "simple helper rewrites stay structural and avoid the old raw helper"
  [(ruby-tf-x-is-function? '(:x-is-function? e))
    (ruby-tf-x-lu-set '(:x-lu-set h k v))
    (ruby-tf-x-lu-eq '(:x-lu-eq a b))
    (ruby-tf-x-pwd '(:x-pwd))
    (ruby-tf-x-unpack '(:x-unpack arr))
    (str/includes? (slurp "src/hara/model/annex/spec_xtalk/fn_ruby.clj")
                   (str "ruby" "-raw"))]
  => ['(. e (respond_to? :call))
      '(:=
        (. h [(. (fn []
                   (if (or (. k nil?)
                           (. k (is_a? Numeric))
                           (. k (is_a? String))
                           (. k (is_a? Symbol))
                           (. k (is_a? TrueClass))
                           (. k (is_a? FalseClass)))
                     (return k)
                     (return (. k object_id))))
                 (call))])
        v)
      '(== (. a object_id) (. b object_id))
      '(or (. ENV ["PWD"]) (. Dir pwd))
      '(:.. arr)
      false])

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-concurrent-promise-run :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-len :added "4.1"}
(fact "keeps simple builtin-backed helper forms direct"
  (ruby-tf-x-len '(:x-len arr))
  => '(. arr length)

  (ruby-tf-x-cat '(:x-cat "a" "b"))
  => '(+ "a" "b")

  (ruby-tf-x-print '(:x-print "hello"))
  => '(puts "hello")

  (ruby-tf-x-random '(:x-random))
  => '(rand)

  (ruby-tf-x-now-ms '(:x-now-ms))
  => '(. (* (. Time.now to_f) 1000) to_i))

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-cat :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-print :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-random :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-now-ms :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-apply :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-type-native :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-unpack :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-abs :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-mod :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-max :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-min :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-pow :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-quot :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-floor :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-ceil :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-acos :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-asin :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-atan :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-cosh :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-sinh :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-tan :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-tanh :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-log10 :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-string? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-number? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-integer? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-boolean? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-object? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-array? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-function? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-to-string :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-to-number :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-push :added "4.1"}
(fact "keeps common array and string helpers as native method calls"
  [(ruby-tf-x-arr-push '(:x-arr-push arr item))
   (ruby-tf-x-arr-pop '(:x-arr-pop arr))
   (ruby-tf-x-str-split '(:x-str-split s ","))
   (ruby-tf-x-str-join '(:x-str-join "," arr))
   (ruby-tf-x-json-encode '(:x-json-encode obj))
   (ruby-tf-x-json-decode '(:x-json-decode s))]
  => ['(. arr (push item))
      '(. arr (pop))
      '(. s (split ","))
      '(. arr (join ","))
      '(JSON.generate obj)
      '(JSON.parse s)])

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-pop :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-push-first :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-pop-first :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-insert :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-remove :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-sort :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-comp :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-split :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-join :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-index-of :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-substring :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-to-upper :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-to-lower :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-char :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-replace :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-trim :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-trim-left :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-trim-right :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-format :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-to-fixed :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-starts-with :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-ends-with :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-create :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-eq :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-get :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-set :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-del :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-has-key? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-json-encode :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-json-decode :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-b64-encode :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-b64-decode :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-pwd :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-file-resolve :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-file-slurp :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-file-spit :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-shell :added "4.1"}
(fact "runtime helpers emit callback-oriented Ruby integrations"
  (let [shell-out  (l/emit-as :ruby [(ruby-tf-x-shell '[_ command root cb])])
        slurp-out  (l/emit-as :ruby [(ruby-tf-x-file-slurp '[_ path opts cb])])
        spit-out   (l/emit-as :ruby [(ruby-tf-x-file-spit '[_ path content opts cb])])
        socket-out (l/emit-as :ruby [(ruby-tf-x-socket-connect '[_ host port opts cb])])
        notify-out (l/emit-as :ruby [(ruby-tf-x-notify-http '[_ host port value id key opts])])]
    [(boolean (re-find #"Open3\.capture3" shell-out))
     (boolean (re-find #"File\.read" slurp-out))
     (boolean (re-find #"File\.write" spit-out))
     (boolean (re-find #"TCPSocket\.new" socket-out))
     (boolean (re-find #"Net::HTTP\.new" notify-out))])
  => [true true true true true])

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise-then :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise-catch :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise-finally :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-with-delay :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-from-obj :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-from-arr :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-from :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-eq :added "4.1"}
(fact "iterator equality still compiles through the ruby emitter"
  (l/emit-as :ruby [(ruby-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"length")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-null :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-next :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-has? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-native? :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-socket-connect :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-socket-send :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-socket-close :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-notify-http :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-return-encode :added "4.1"}
(fact "return helpers still emit runtime encoding wrappers"
  (let [encode-out (l/emit-as :ruby [(ruby-tf-x-return-encode '[_ out id key])])
        wrap-out   (l/emit-as :ruby [(ruby-tf-x-return-wrap '[_ f encode-fn])])
        eval-out   (l/emit-as :ruby [(ruby-tf-x-return-eval '[_ s wrap-fn])])]
    [(boolean (re-find #"JSON\.generate" encode-out))
     (boolean (re-find #"JSON\.generate" wrap-out))
     (boolean (re-find #"eval" eval-out))])
  => [true true true])

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-return-wrap :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-return-eval :added "4.1"}
(fact "TODO")
