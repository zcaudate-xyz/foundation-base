(ns hara.model.annex.spec-xtalk.fn-ruby-test
  (:use code.test)
  (:require [clojure.string :as str]
            [hara.lang :as l]
            [hara.model.annex.spec-xtalk.fn-ruby :refer :all]))

^{:refer hara.model.annex.spec-xtalk.fn-ruby/+ruby-promise+ :added "4.1"}
(fact "promise helpers hard-link through the shared xt promise module"
  [(get-in +ruby-promise+ [:x-promise :emit])
   (get-in +ruby-promise+ [:x-promise :raw])
   (get-in +ruby-promise+ [:x-promise-then :raw])
   (get-in +ruby-promise+ [:x-promise-catch :raw])
   (get-in +ruby-promise+ [:x-promise-finally :raw])
   (get-in +ruby-promise+ [:x-promise-native? :raw])
   (get-in +ruby-promise+ [:x-with-delay :raw])]
  => [:hard-link
      'xt.lang.common-promise/promise
      'xt.lang.common-promise/promise-then
      'xt.lang.common-promise/promise-catch
      'xt.lang.common-promise/promise-finally
      'xt.lang.common-promise/promise-native?
      'xt.lang.common-promise/with-delay])

(fact "simple helper rewrites stay structural and avoid the old raw helper"
  (ruby-tf-x-is-function? '(:x-is-function? e))
  => '(. e (respond_to? :call))

  (ruby-tf-x-lu-set '(:x-lu-set h k v))
  => '(:=
      (.
       h
       [(.
         (fn
          []
          (if
           (or
            (. k nil?)
            (. k (is_a? Numeric))
            (. k (is_a? String))
            (. k (is_a? Symbol))
            (. k (is_a? TrueClass))
            (. k (is_a? FalseClass)))
           (return k)
           (return (. k object_id))))
         (call))])
      v)

  (ruby-tf-x-lu-eq '(:x-lu-eq a b))
  => '(== (. a object_id) (. b object_id))

  (ruby-tf-x-pwd '(:x-pwd))
  => '(or (. ENV ["PWD"]) (. Dir pwd))

  (ruby-tf-x-unpack '(:x-unpack arr))
  => '(:.. arr)

  (str/includes? (slurp "src/hara/model/annex/spec_xtalk/fn_ruby.clj")
                 (str "ruby" "-raw"))
  => false)

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-concurrent-promise-run :added "4.1"}
(fact "runs ruby concurrent promises")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-len :added "4.1"}
(fact "keeps simple builtin-backed helper forms direct"
  (ruby-tf-x-len '(:x-len arr))
  => '(. arr length)

  (ruby-tf-x-cat '(:x-cat "a" "b"))
  => '(+ "a" "b")

  (ruby-tf-x-print '(:x-print "hello"))
  => '(. (fn []
           (puts "hello")
           (return nil))
         (call))

  (ruby-tf-x-random '(:x-random))
  => '(rand)

  (ruby-tf-x-now-ms '(:x-now-ms))
  => '(. (* (. Time.now to_f) 1000) to_i))

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-cat :added "4.1"}
(fact "concatenates strings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-print :added "4.1"}
(fact "prints values")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-random :added "4.1"}
(fact "generates random values")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-now-ms :added "4.1"}
(fact "gets current time in milliseconds")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-del :added "4.1"}
(fact "deletes values")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-apply :added "4.1"}
(fact "applies arguments")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-type-native :added "4.1"}
(fact "detects native type")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-unpack :added "4.1"}
(fact "unpacks values")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-abs :added "4.1"}
(fact "computes absolute value")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-mod :added "4.1"}
(fact "computes modulo")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-max :added "4.1"}
(fact "computes maximum")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-min :added "4.1"}
(fact "computes minimum")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-pow :added "4.1"}
(fact "computes power")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-quot :added "4.1"}
(fact "computes quotient")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-floor :added "4.1"}
(fact "computes floor")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-ceil :added "4.1"}
(fact "computes ceiling")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-acos :added "4.1"}
(fact "computes arc cosine")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-asin :added "4.1"}
(fact "computes arc sine")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-atan :added "4.1"}
(fact "computes arc tangent")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-cosh :added "4.1"}
(fact "computes hyperbolic cosine")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-sinh :added "4.1"}
(fact "computes hyperbolic sine")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-tan :added "4.1"}
(fact "computes tangent")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-tanh :added "4.1"}
(fact "computes hyperbolic tangent")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-m-log10 :added "4.1"}
(fact "computes base-10 logarithm")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-string? :added "4.1"}
(fact "checks string type")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-number? :added "4.1"}
(fact "checks number type")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-integer? :added "4.1"}
(fact "checks integer type")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-boolean? :added "4.1"}
(fact "checks boolean type")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-object? :added "4.1"}
(fact "checks object type")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-array? :added "4.1"}
(fact "checks array type")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-is-function? :added "4.1"}
(fact "checks function type")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-to-string :added "4.1"}
(fact "converts to string")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-to-number :added "4.1"}
(fact "converts to number")

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
(fact "pops array elements")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-push-first :added "4.1"}
(fact "prepends array elements")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-pop-first :added "4.1"}
(fact "removes first array element")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-insert :added "4.1"}
(fact "inserts array elements")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-remove :added "4.1"}
(fact "removes array elements")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-clone :added "4.1"}
(fact "clones arrays")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-each :added "4.1"}
(fact "iterates over arrays")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-every :added "4.1"}
(fact "tests every element")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-sort :added "4.1"}
(fact "sorts arrays")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-comp :added "4.1"}
(fact "compares strings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-split :added "4.1"}
(fact "splits strings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-join :added "4.1"}
(fact "joins strings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-index-of :added "4.1"}
(fact "finds substring index")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-substring :added "4.1"}
(fact "extracts substrings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-to-upper :added "4.1"}
(fact "uppercases strings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-to-lower :added "4.1"}
(fact "lowercases strings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-char :added "4.1"}
(fact "gets string character")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-replace :added "4.1"}
(fact "replaces substrings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-trim :added "4.1"}
(fact "trims strings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-trim-left :added "4.1"}
(fact "trims left whitespace")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-trim-right :added "4.1"}
(fact "trims right whitespace")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-format :added "4.1"}
(fact "formats strings")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-to-fixed :added "4.1"}
(fact "formats numbers")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-starts-with :added "4.1"}
(fact "checks string prefix")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-str-ends-with :added "4.1"}
(fact "checks string suffix")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-global-set :added "4.1"}
(fact "sets global variables")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-global-del :added "4.1"}
(fact "deletes global variables")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-global-has? :added "4.1"}
(fact "checks global variables")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-prototype-create :added "4.1"}
(fact "creates prototypes")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-prototype-get :added "4.1"}
(fact "gets prototypes")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-prototype-set :added "4.1"}
(fact "sets prototypes")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-prototype-method :added "4.1"}
(fact "calls prototype methods")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-create :added "4.1"}
(fact "creates lookup tables")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-get-key :added "4.1"}
(fact "gets object key")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-set-key :added "4.1"}
(fact "sets object key")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-eq :added "4.1"}
(fact "compares lookup tables")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-get :added "4.1"}
(fact "gets lookup table value")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-set :added "4.1"}
(fact "sets lookup table value")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-del :added "4.1"}
(fact "deletes lookup table value")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-obj-clone :added "4.1"}
(fact "clones objects")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-has-key? :added "4.1"}
(fact "checks object key")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-json-encode :added "4.1"}
(fact "encodes JSON")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-json-decode :added "4.1"}
(fact "decodes JSON")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-b64-encode :added "4.1"}
(fact "encodes base64")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-b64-decode :added "4.1"}
(fact "decodes base64")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-pwd :added "4.1"}
(fact "gets working directory")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-file-resolve :added "4.1"}
(fact "resolves file paths")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-file-slurp :added "4.1"}
(fact "reads file contents")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-file-spit :added "4.1"}
(fact "writes file contents")

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

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-async-run :added "4.1"}
(fact "runs asynchronously")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise :added "4.1"}
(fact "transforms x:promise")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise-all :added "4.1"}
(fact "transforms x:promise-all")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise-then :added "4.1"}
(fact "transforms x:promise-then")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise-catch :added "4.1"}
(fact "transforms x:promise-catch")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise-finally :added "4.1"}
(fact "transforms x:promise-finally")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-promise-native? :added "4.1"}
(fact "transforms x:promise-native?")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-with-delay :added "4.1"}
(fact "delays execution")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-from-obj :added "4.1"}
(fact "creates iterators from objects")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-from-arr :added "4.1"}
(fact "creates iterators from arrays")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-from :added "4.1"}
(fact "creates iterators")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-eq :added "4.1"}
(fact "iterator equality still compiles through the ruby emitter"
  (l/emit-as :ruby [(ruby-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"length")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-null :added "4.1"}
(fact "creates null iterators")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-next :added "4.1"}
(fact "advances iterators")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-has? :added "4.1"}
(fact "checks iterator state")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-native? :added "4.1"}
(fact "checks native iterators")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-generator :added "4.1"}
(fact "creates generator iterators")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-socket-connect :added "4.1"}
(fact "connects sockets")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-socket-send :added "4.1"}
(fact "sends socket data")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-socket-close :added "4.1"}
(fact "closes sockets")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-notify-http :added "4.1"}
(fact "notifies via HTTP")

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
(fact "wraps return values")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-return-eval :added "4.1"}
(fact "evaluates return values")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tv-x-get-key :added "4.1"}
(fact "handles ruby tv x get key")

^{:refer hara.model.annex.spec-xtalk.fn-ruby/ruby-tf-x-del-key :added "4.1"}
(fact "deletes object key")
