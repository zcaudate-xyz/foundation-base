(ns std.lang.model-annex.spec-xtalk.fn-ruby-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lang.model-annex.spec-xtalk.fn-ruby :refer :all]))

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


^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-del :added "4.1"}
(fact "deletes variable"
  (ruby-tf-x-del '(:x-del var))
  => '(= var nil))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-apply :added "4.1"}
(fact "applies function with args"
  (ruby-tf-x-apply '(:x-apply f args))
  => '(. f (call (* args))))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-shell :added "4.1"}
(fact "executes shell command"
  (ruby-tf-x-shell '(:x-shell "ls" nil))
  => (list (symbol "`") "ls"))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-type-native :added "4.1"}
(fact "returns native type"
  (ruby-tf-x-type-native '(:x-type-native obj))
  => '(. obj class))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-unpack :added "4.1"}
(fact "unpacks array"
  (ruby-tf-x-unpack '(:x-unpack arr))
  => '(* arr))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-return-encode :added "4.1"}
(fact "return encode"
  (l/emit-as :ruby [(ruby-tf-x-return-encode '[_ out id key])])
  => #"JSON.generate")

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-return-wrap :added "4.1"}
(fact "return wrap"
  (l/emit-as :ruby [(ruby-tf-x-return-wrap '[_ f encode-fn])])
  => #"JSON.generate")

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-return-eval :added "4.1"}
(fact "return eval"
  (l/emit-as :ruby [(ruby-tf-x-return-eval '[_ s wrap-fn])])
  => #"eval")

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-max :added "4.1"}
(fact "returns max of numbers"
  (ruby-tf-x-m-max '(_ a b))
  => '(. [a b] max))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-min :added "4.1"}
(fact "returns min of numbers"
  (ruby-tf-x-m-min '(:x-m-min a b))
  => '(. [a b] min))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-pow :added "4.1"}
(fact "raises base to exponent"
  (ruby-tf-x-m-pow '(:x-m-pow base exp))
  => '(** base exp))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-quot :added "4.1"}
(fact "returns integer division"
  (ruby-tf-x-m-quot '(:x-m-quot num denom))
  => '(. num (div denom)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-floor :added "4.1"}
(fact "returns floor of number"
  (ruby-tf-x-m-floor '(:x-m-floor num))
  => '(. num floor))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-ceil :added "4.1"}
(fact "returns ceil of number"
  (ruby-tf-x-m-ceil '(:x-m-ceil num))
  => '(. num ceil))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-acos :added "4.1"}
(fact "returns arc cosine"
  (ruby-tf-x-m-acos '(:x-m-acos num))
  => '(Math.acos num))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-asin :added "4.1"}
(fact "returns arc sine"
  (ruby-tf-x-m-asin '(:x-m-asin num))
  => '(Math.asin num))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-atan :added "4.1"}
(fact "returns arc tangent"
  (ruby-tf-x-m-atan '(:x-m-atan num))
  => '(Math.atan num))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-cosh :added "4.1"}
(fact "returns hyperbolic cosine"
  (ruby-tf-x-m-cosh '(:x-m-cosh num))
  => '(Math.cosh num))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-sinh :added "4.1"}
(fact "returns hyperbolic sine"
  (ruby-tf-x-m-sinh '(:x-m-sinh num))
  => '(Math.sinh num))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-tan :added "4.1"}
(fact "returns tangent"
  (ruby-tf-x-m-tan '(:x-m-tan num))
  => '(Math.tan num))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-tanh :added "4.1"}
(fact "returns hyperbolic tangent"
  (ruby-tf-x-m-tanh '(:x-m-tanh num))
  => '(Math.tanh num))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-m-log10 :added "4.1"}
(fact "returns base‑10 logarithm"
  (ruby-tf-x-m-log10 '(:x-m-log10 num))
  => '(Math.log10 num))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-is-string? :added "4.1"}
(fact "checks if value is a string"
  (ruby-tf-x-is-string? '(:x-is-string? e))
  => '(. e (is_a? String)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-is-number? :added "4.1"}
(fact "checks if value is a number"
  (ruby-tf-x-is-number? '(:x-is-number? e))
  => '(. e (is_a? Numeric)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-is-integer? :added "4.1"}
(fact "checks if value is an integer"
  (ruby-tf-x-is-integer? '(:x-is-integer? e))
  => '(. e (is_a? Integer)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-is-boolean? :added "4.1"}
(fact "checks if value is a boolean"
  (ruby-tf-x-is-boolean? '(:x-is-boolean? e))
  => '(or (== e true) (== e false)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-is-object? :added "4.1"}
(fact "checks if value is an object"
  (ruby-tf-x-is-object? '(:x-is-object? e))
  => '(. e (is_a? Object)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-is-array? :added "4.1"}
(fact "checks if value is an array"
  (ruby-tf-x-is-array? '(:x-is-array? e))
  => '(. e (is_a? Array)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-is-function? :added "4.1"}
(fact "checks if value is a function"
  (ruby-tf-x-is-function? '(:x-is-function? e))
  => '(. e (respond_to? :call)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-to-string :added "4.1"}
(fact "converts value to string"
  (ruby-tf-x-to-string '(:x-to-string e))
  => '(. e to_s))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-to-number :added "4.1"}
(fact "converts value to number"
  (ruby-tf-x-to-number '(:x-to-number e))
  => '(. e to_f))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-comp :added "4.1"}
(fact "compares strings"
  (ruby-tf-x-str-comp '(_ a b))
  => '(< a b))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-char :added "4.1"}
(fact "gets character code"
  (ruby-tf-x-str-char '(_ s i))
  => '(ord (. s (slice i 1))))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-replace :added "4.1"}
(fact "replaces substring"
  (ruby-tf-x-str-replace '(:x-str-replace s tok replacement))
  => '(. s (gsub tok replacement)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-trim :added "4.1"}
(fact "trims whitespace from both ends"
  (ruby-tf-x-str-trim '(:x-str-trim s))
  => '(. s strip))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-trim-left :added "4.1"}
(fact "trims whitespace from left"
  (ruby-tf-x-str-trim-left '(:x-str-trim-left s))
  => '(. s lstrip))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-trim-right :added "4.1"}
(fact "trims whitespace from right"
  (ruby-tf-x-str-trim-right '(:x-str-trim-right s))
  => '(. s rstrip))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-format :added "4.1"}
(fact "formats string"
  (ruby-tf-x-str-format '(:x-str-format fmt a b))
  => '(sprintf fmt a b))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-to-fixed :added "4.1"}
(fact "formats number with fixed decimal places"
  (ruby-tf-x-str-to-fixed '(:x-str-to-fixed n 2))
  => '(sprintf "%.2f" n))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-starts-with :added "4.1"}
(fact "checks if string starts with prefix"
  (ruby-tf-x-str-starts-with '(:x-str-starts-with s prefix))
  => '(. s (start_with? prefix)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-str-ends-with :added "4.1"}
(fact "checks if string ends with suffix"
  (ruby-tf-x-str-ends-with '(:x-str-ends-with s suffix))
  => '(. s (end_with? suffix)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-create :added "4.1"}
(fact "creates empty lookup map"
  (ruby-tf-x-lu-create '(:x-lu-create))
  => '{})

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-get :added "4.1"}
(fact "gets value from lookup map"
  (ruby-tf-x-lu-get '(:x-lu-get h k))
  => '(. h ([] k))
  
  (ruby-tf-x-lu-get '(:x-lu-get h k default))
  => '(. h (fetch k default)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-set :added "4.1"}
(fact "sets value in lookup map"
  (ruby-tf-x-lu-set '(:x-lu-set h k v))
  => '(= (. h ([] k)) v))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-lu-del :added "4.1"}
(fact "deletes key from lookup map"
  (ruby-tf-x-lu-del '(:x-lu-del h k))
  => '(. h (delete k)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-has-key? :added "4.1"}
(fact "checks hash key"
  (ruby-tf-x-has-key? '(_ obj key))
  => '(. obj (has_key? key)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-json-encode :added "4.1"}
(fact "encodes object to JSON"
  (ruby-tf-x-json-encode '(:x-json-encode obj))
  => '(JSON.generate obj))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-json-decode :added "4.1"}
(fact "decodes JSON string"
  (ruby-tf-x-json-decode '(:x-json-decode s))
  => '(JSON.parse s))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-b64-encode :added "4.1"}
(fact "encodes base64"
  (ruby-tf-x-b64-encode '(_ obj))
  => '(. Base64 (encode64 obj)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-b64-decode :added "4.1"}
(fact "decodes base64"
  (ruby-tf-x-b64-decode '(_ s))
  => '(. Base64 (decode64 s)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-slurp-file :added "4.1"}
(fact "reads file contents through callback"
  (ruby-tf-x-slurp-file '(:x-slurp-file path opts cb))
  => '(. cb (call nil (. File (read path)))))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-spit-file :added "4.1"}
(fact "writes content to file through callback"
  (ruby-tf-x-spit-file '(:x-spit-file path content opts cb))
  => '(do (. File (write path content))
          (. cb (call nil path))))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-uri-encode :added "4.1"}
(fact "URI‑encodes string"
  (ruby-tf-x-uri-encode '(:x-uri-encode s))
  => '(. URI (encode s)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-uri-decode :added "4.1"}
(fact "URI‑decodes string"
  (ruby-tf-x-uri-decode '(:x-uri-decode s))
  => '(. URI (decode s)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-prototype-get :added "4.1"}
(fact "gets object's class"
  (ruby-tf-x-prototype-get '(:x-prototype-get obj))
  => '(. obj class))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-prototype-set :added "4.1"}
(fact "sets object prototype (no‑op in Ruby)"
  (ruby-tf-x-prototype-set '(:x-prototype-set obj prototype))
  => 'obj)

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-prototype-create :added "4.1"}
(fact "creates object with prototype (empty hash)"
  (ruby-tf-x-prototype-create '(:x-prototype-create prototype))
  => '{})

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-prototype-tostring :added "4.1"}
(fact "converts object to string"
  (ruby-tf-x-prototype-tostring '(:x-prototype-tostring obj))
  => '(. obj to_s))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-thread-spawn :added "4.1"}
(fact "spawns a new thread"
  (ruby-tf-x-thread-spawn '(:x-thread-spawn thunk))
  => '(Thread.new (fn [] (. thunk call))))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-thread-join :added "4.1"}
(fact "joins a thread"
  (ruby-tf-x-thread-join '(:x-thread-join thread))
  => '(. thread join))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-with-delay :added "4.1"}
(fact "delays execution"
  (ruby-tf-x-with-delay '(:x-with-delay thunk 1000))
  => '(do (sleep (/ 1000 1000.0)) (. thunk call)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-start-interval :added "4.1"}
(fact "starts a repeating interval"
  (ruby-tf-x-start-interval '(:x-start-interval thunk 2000))
  => '(Thread.new (fn [] (while true (do (. thunk call) (sleep (/ 2000 1000.0)))))))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-stop-interval :added "4.1"}
(fact "stops an interval thread"
  (ruby-tf-x-stop-interval '(:x-stop-interval instance))
  => '(. instance kill))


^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-from-obj :added "4.1"}
(fact "converts hash to array of pairs"
  (ruby-tf-x-iter-from-obj '(:x-iter-from-obj obj))
  => '(. obj to_a))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-from-arr :added "4.1"}
(fact "returns array unchanged"
  (ruby-tf-x-iter-from-arr '(:x-iter-from-arr arr))
  => 'arr)

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-from :added "4.1"}
(fact "returns object unchanged"
  (ruby-tf-x-iter-from '(:x-iter-from obj))
  => 'obj)

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-eq :added "4.1"}
(fact "compares two iterables elementwise"
  (l/emit-as :ruby [(ruby-tf-x-iter-eq '[_ it0 it1 eq-fn])])
  => #"length")

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-next :added "4.1"}
(fact "shifts first element from array"
  (ruby-tf-x-iter-next '(:x-iter-next it))
  => '(. it shift))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-has? :added "4.1"}
(fact "checks if object is non‑empty array"
  (ruby-tf-x-iter-has? '(:x-iter-has? obj))
  => '(and (. obj is_a? Array) (> (. obj length) 0)))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-iter-native? :added "4.1"}
(fact "checks if object is an array"
  (ruby-tf-x-iter-native? '(:x-iter-native? it))
  => '(. it is_a? Array))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-socket-connect :added "4.1"}
(fact "connects socket"
  (ruby-tf-x-socket-connect '(:x-socket-connect host port))
  => '(TCPSocket.new host port))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-socket-send :added "4.1"}
(fact "sends socket payload"
  (ruby-tf-x-socket-send '(:x-socket-send conn value))
  => '(. conn puts value))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-socket-close :added "4.1"}
(fact "closes socket"
  (ruby-tf-x-socket-close '(:x-socket-close conn))
  => '(. conn close))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-notify-socket :added "4.1"}
(fact "notify socket emits async marker"
  (l/emit-as :ruby [(ruby-tf-x-notify-socket '[_ message])])
   => #"async")

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-ws-connect :added "4.1"}
(fact "ws connect delegates to socket connect"
  (ruby-tf-x-ws-connect '(:x-ws-connect url))
  => '(TCPSocket.new url))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-ws-send :added "4.1"}
(fact "ws send delegates to socket send"
  (ruby-tf-x-ws-send '(:x-ws-send conn value))
  => '(. conn puts value))

^{:refer std.lang.model-annex.spec-xtalk.fn-ruby/ruby-tf-x-ws-close :added "4.1"}
(fact "ws close delegates to socket close"
  (ruby-tf-x-ws-close '(:x-ws-close conn))
  => '(. conn close))
