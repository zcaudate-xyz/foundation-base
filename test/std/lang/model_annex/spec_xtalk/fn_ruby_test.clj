(ns std.lang.model-annex.spec-xtalk.fn-ruby-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lang.model-annex.spec-xtalk.fn-ruby :refer :all]))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-len :added "4.1"}
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

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-arr-push :added "4.1"}
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

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/+ruby-promise+ :added "4.1"}
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

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-return-encode :added "4.1"}
(fact "return helpers still emit runtime encoding wrappers"
  (let [encode-out (l/emit-as :ruby [(ruby-tf-x-return-encode '[_ out id key])])
        wrap-out   (l/emit-as :ruby [(ruby-tf-x-return-wrap '[_ f encode-fn])])
        eval-out   (l/emit-as :ruby [(ruby-tf-x-return-eval '[_ s wrap-fn])])]
    [(boolean (re-find #"JSON\.generate" encode-out))
     (boolean (re-find #"JSON\.generate" wrap-out))
     (boolean (re-find #"eval" eval-out))])
  => [true true true])

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-shell :added "4.1"}
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

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-eq :added "4.1"}
(fact "iterator equality still compiles through the ruby emitter"
  (l/emit-as :ruby [(ruby-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"length")
