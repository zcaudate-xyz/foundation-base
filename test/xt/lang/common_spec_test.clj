(ns xt.lang.common-spec-test
 (:require [std.lang :as l])
 (:use code.test))

(l/script- :lua
 {:runtime :basic,
  :require [[xt.lang.common-spec :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-spec/for:array :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:object :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:index :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:iter :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:return :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:try :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/for:async :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:get-idx :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:set-idx :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:first :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:second :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:last :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:second-last :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-remove :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-push :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-pop :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-push-first :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-pop-first :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-insert :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-slice :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-reverse :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cat :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:len :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:err :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:type-native :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:offset :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:offset-rev :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:offset-len :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:offset-rlen :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-create :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-eq :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-get :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-set :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lu-del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-abs :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-acos :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-asin :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-atan :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-ceil :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-cos :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-cosh :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-exp :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-floor :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-loge :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-log10 :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-max :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-mod :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-min :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-pow :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-quot :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-sin :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-sinh :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-sqrt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-tan :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:m-tanh :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:not-nil? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:nil? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:add :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:sub :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:mul :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:div :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:neg :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:inc :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:dec :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:zero? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:pos? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:neg? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:even? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:odd? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:eq :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:neq :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:lte :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:gt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:gte :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:has-key? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:del-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:get-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:get-path :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:set-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:copy-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-keys :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-vals :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-pairs :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-clone :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-assign :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:obj-from-pairs :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:to-string :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:to-number :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-string? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-number? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-integer? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-boolean? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-object? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-array? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:print :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-len :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-comp :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-lt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-gt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-pad-left :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-pad-right :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-starts-with :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-ends-with :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-char :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-format :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-split :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-join :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-index-of :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-substring :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-to-upper :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-to-lower :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-to-fixed :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-replace :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-trim :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-trim-left :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:str-trim-right :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-sort :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-clone :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-each :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-every :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-some :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-map :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-append :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-filter :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-keep :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-foldl :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-foldr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:arr-find :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:is-function? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:callback :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-run :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-then :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-catch :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-finally :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-cancel :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-status :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-await :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:future-from-async :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:eval :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:apply :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-from-obj :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-from-arr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-from :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-eq :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-null :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-next :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-has? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:iter-native? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:return-encode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:return-wrap :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:return-eval :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-and :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-or :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-lshift :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-rshift :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:bit-xor :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:global-set :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:global-del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:global-has? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:this :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:proto-get :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:proto-set :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:proto-create :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:proto-tostring :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:random :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:throw :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:now-ms :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:unpack :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:client-basic :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:client-ws :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:server-basic :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:server-ws :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:socket-connect :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:socket-send :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:socket-close :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:ws-connect :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:ws-send :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:ws-close :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:notify-http :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:notify-socket :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:b64-encode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:b64-decode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-list :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-flush :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-get :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-set :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:cache-incr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:slurp :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:spit :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:json-encode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:json-decode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:shell :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:thread-spawn :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:thread-join :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:with-delay :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:start-interval :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:stop-interval :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:uri-encode :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-spec/x:uri-decode :added "4.1"}
(fact "TODO")
