(ns std.lang.base.grammar-xtalk
  (:require [std.string :as str]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lib :as h]))

(defn tf-throw
  "wrapper for throw transform"
  {:added "4.0"}
  [[_ obj]]
  (list 'throw obj))

(defn tf-eq-nil?
  "equals nil transform"
  {:added "4.0"}
  [[_ obj]]
  (list '== nil obj))

(defn tf-not-nil?
  "not nil transform"
  {:added "4.0"}
  [[_ obj]]
  (list 'not= nil obj))

(defn tf-proto-create
  "creates the prototype map"
  {:added "4.0"}
  [[_ m]] (list 'return m))

(defn tf-has-key?
  "has key default transform"
  {:added "4.0"}
  [[_ obj key check]]
  (let [val (list 'not= (list 'x:get-key obj key) nil)]
    (if check
      (list '== check val)
      val)))

(defn tf-get-path
  "get-in transform"
  {:added "4.0"}
  [[_ obj ks default]]
  (let [val (if (symbol? obj)
              (apply list '. obj (map vector ks))
              (apply list '. (list 'quote (list obj)) (map vector ks)))]
    (if default
      (list 'or val default)
      val)))

(defn tf-get-key
  "get-key transform"
  {:added "4.0"}
  [[_ obj k default]]
  (let [val (if (symbol? obj)
              (list '. obj [k])
              (list '. (list 'quote (list obj)) [k]))]
    (if default
      (list 'or val default)
      val)))

(defn tf-set-key
  "set-key transform"
  {:added "4.0"}
  [[_ obj k v]]
  (list := (list '. obj [k])
        v))

(defn tf-del-key
  "del-key transform"
  {:added "4.0"}
  [[_ obj k]]
  (list 'x:del (list '. obj [k])))

(defn tf-copy-key
  "copy-key transform"
  {:added "4.0"}
  [[_ dst src idx]]
  (let [[dk sk] (if (vector? idx)
                  idx
                  [idx idx])]
    (tf-set-key [nil dst dk (tf-get-key [nil src sk])])))

;;
;;
;;

(defn tf-grammar-offset
  "del-key transform"
  {:added "4.0"}
  []
  (let [grammar preprocess/*macro-grammar*]
    (or (get-in grammar [:default :index :offset]) 0)))

(defn tf-grammar-end-inclusive
  "gets the end inclusive flag"
  {:added "4.0"}
  []
  (let [grammar preprocess/*macro-grammar*]
    (get-in grammar [:default :index :end-inclusive])))

(defn tf-offset-base
  "calculates the offset"
  {:added "4.0"}
  [offset n]
  (if (nil? n)
    offset
    (cond (zero? offset) n
          
          (integer? n)
          (+ n offset)
          
          :else
          (list '+ n offset))))

(defn tf-offset
  "gets the offset"
  {:added "4.0"}
  [[_ n]]
  (tf-offset-base (tf-grammar-offset)
                  n))

(defn tf-offset-rev
  "gets the reverse offset"
  {:added "4.0"}
  [[_ n]]
  (tf-offset-base (- (tf-grammar-offset) 1)
                  n))

(defn tf-offset-len
  "gets the length offset"
  {:added "4.0"}
  [[_ n]]
  (tf-offset-base (if (tf-grammar-end-inclusive)
                    0 -1) 
                  n))

(defn tf-offset-rlen
  "gets the reverse length offset"
  {:added "4.0"}
  [[_ n]]
  (tf-offset-base (if (tf-grammar-end-inclusive)
                    -1 0) 
                  n))

;;
;; GLOBAL
;;

(defn tf-global-set
  "default global set transform"
  {:added "4.0"}
  [[_ sym val]]
  (list 'x:set-key '!:G (str sym) val))

(defn tf-global-has?
  "default global has transform"
  {:added "4.0"}
  [[_ sym]]
  (list 'not (list 'x:nil? (list 'x:get-key '!:G (str sym)))))

(defn tf-global-del
  "default global del transform"
  {:added "4.0"}
  [[_ sym val]]
  (list 'x:set-key '!:G (str sym) nil))

;;
;; LU
;;

(defn tf-lu-eq
  "lookup equals transform"
  {:added "4.0"}
  [[_ o1 o2]]
  (list '== o1 o2))


;;
;; BIT
;;

(defn tf-bit-and
  "bit and transform"
  {:added "4.0"}
  [[_ i1 i2]]
  (list 'b:& i1 i2))

(defn tf-bit-or
  "bit or transform"
  {:added "4.0"}
  [[_ i1 i2]]
  (list 'b:| i1 i2))

(defn tf-bit-lshift
  "bit left shift transform"
  {:added "4.0"}
  [[_ x n]]
  (list 'b:<< x n))

(defn tf-bit-rshift
  "bit right shift transform"
  {:added "4.0"}
  [[_ x n]]
  (list 'b:>> x n))

(defn tf-bit-xor
  "bit xor transform"
  {:added "4.0"}
  [[_ x n]]
  (list 'b:xor x n))

;;
;; X-LANG
;;

(def +op-xtalk-core+
  [{:op :x-del            :symbol #{'x:del}             :emit :abstract}
   {:op :x-cat            :symbol #{'x:cat}             :emit :abstract}
   {:op :x-len            :symbol #{'x:len}             :emit :abstract}
   {:op :x-err            :symbol #{'x:err}             :emit :abstract}
   {:op :x-throw          :symbol #{'x:throw}           :macro #'tf-throw  :emit :macro}
   {:op :x-eval           :symbol #{'x:eval}            :emit :abstract}
   {:op :x-apply          :symbol #{'x:apply}           :emit :abstract}
   {:op :x-unpack         :symbol #{'x:unpack}          :emit :abstract}
   {:op :x-print          :symbol #{'x:print}           :emit :abstract}
   {:op :x-random         :symbol #{'x:random}          :emit :abstract}
   {:op :x-shell          :symbol #{'x:shell}           :emit :abstract}
   {:op :x-now-ms         :symbol #{'x:now-ms}          :emit :abstract}
   {:op :x-type-native    :symbol #{'x:type-native}     :emit :abstract}])

(def +op-xtalk-proto+
  [{:op :x-this            :symbol #{'x:this}            :emit :abstract}
   {:op :x-proto-get       :symbol #{'x:proto-get}       :emit :abstract}
   {:op :x-proto-set       :symbol #{'x:proto-set}       :emit :abstract}
   {:op :x-proto-create    :symbol #{'x:proto-create}    :macro #'tf-proto-create   :emit :macro}
   {:op :x-proto-tostring  :symbol #{'x:proto-tostring}  :emit :abstract}])

(def +op-xtalk-global+
  [{:op :x-global-set     :symbol #{'x:global-set}      :macro #'tf-global-set   :emit :macro}
   {:op :x-global-del     :symbol #{'x:global-del}      :macro #'tf-global-del   :emit :macro}
   {:op :x-global-has?    :symbol #{'x:global-has?}     :macro #'tf-global-has?  :emit :macro}])

(def +op-xtalk-custom+
  [{:op :x-not-nil?       :symbol #{'x:not-nil?}        :macro #'tf-not-nil?    :emit :macro}
   {:op :x-nil?           :symbol #{'x:nil?}            :macro #'tf-eq-nil?     :emit :macro}
   {:op :x-has-key?       :symbol #{'x:has-key?}        :macro #'tf-has-key?    :emit :macro}
   {:op :x-del-key        :symbol #{'x:del-key}         :macro #'tf-del-key     :emit :macro}
   
   {:op :x-get-key        :symbol #{'x:get-key}         :macro #'tf-get-key     :emit :macro}
   {:op :x-get-path       :symbol #{'x:get-path}        :macro #'tf-get-path    :emit :macro}
   {:op :x-get-idx        :symbol #{'x:get-idx}         :macro #'tf-get-key     :emit :macro}
   
   {:op :x-set-key        :symbol #{'x:set-key}         :macro #'tf-set-key     :emit :macro}
   {:op :x-set-idx        :symbol #{'x:set-idx}         :macro #'tf-set-key     :emit :macro}

   {:op :x-copy-key       :symbol #{'x:copy-key}        :macro #'tf-copy-key    :emit :macro}
   {:op :x-offset         :symbol #{'x:offset}          :macro #'tf-offset      :emit :macro}
   {:op :x-offset-rev     :symbol #{'x:offset-rev}      :macro #'tf-offset-rev  :emit :macro}
   {:op :x-offset-len     :symbol #{'x:offset-len}      :macro #'tf-offset-len  :emit :macro}
   {:op :x-offset-rlen    :symbol #{'x:offset-rlen}     :macro #'tf-offset-rlen :emit :macro}
   {:op :x-callback       :symbol #{'x:callback}        :emit :unit :default nil}])

(def +op-xtalk-math+       
  [{:op :x-m-abs          :symbol #{'x:m-abs}        :emit :abstract}
   {:op :x-m-acos         :symbol #{'x:m-acos}       :emit :abstract}
   {:op :x-m-asin         :symbol #{'x:m-asin}       :emit :abstract}
   {:op :x-m-atan         :symbol #{'x:m-atan}       :emit :abstract}
   {:op :x-m-ceil         :symbol #{'x:m-ceil}       :emit :abstract}
   {:op :x-m-cos          :symbol #{'x:m-cos}        :emit :abstract}
   {:op :x-m-cosh         :symbol #{'x:m-cosh}       :emit :abstract}
   {:op :x-m-exp          :symbol #{'x:m-exp}        :emit :abstract}
   {:op :x-m-floor        :symbol #{'x:m-floor}      :emit :abstract}
   {:op :x-m-loge         :symbol #{'x:m-loge}       :emit :abstract}
   {:op :x-m-log10        :symbol #{'x:m-log10}      :emit :abstract}
   {:op :x-m-max          :symbol #{'x:m-max}        :emit :abstract}
   {:op :x-m-mod          :symbol #{'x:m-mod}        :emit :abstract}
   {:op :x-m-min          :symbol #{'x:m-min}        :emit :abstract}
   {:op :x-m-pow          :symbol #{'x:m-pow}        :emit :abstract}
   {:op :x-m-quot         :symbol #{'x:m-quot}       :emit :abstract}
   {:op :x-m-sin          :symbol #{'x:m-sin}        :emit :abstract}
   {:op :x-m-sinh         :symbol #{'x:m-sinh}       :emit :abstract}
   {:op :x-m-sqrt         :symbol #{'x:m-sqrt}       :emit :abstract}
   {:op :x-m-tan          :symbol #{'x:m-tan}        :emit :abstract}
   {:op :x-m-tanh         :symbol #{'x:m-tanh}       :emit :abstract}])

(def +op-xtalk-type+
  [{:op :x-to-string      :symbol #{'x:to-string}       :emit :abstract}
   {:op :x-to-number      :symbol #{'x:to-number}       :emit :abstract}
   {:op :x-is-string?     :symbol #{'x:is-string?}      :emit :abstract}
   {:op :x-is-number?     :symbol #{'x:is-number?}      :emit :abstract}
   {:op :x-is-integer?    :symbol #{'x:is-integer?}     :emit :abstract}
   {:op :x-is-boolean?    :symbol #{'x:is-boolean?}     :emit :abstract}
   {:op :x-is-function?   :symbol #{'x:is-function?}    :emit :abstract}
   {:op :x-is-object?     :symbol #{'x:is-object?}      :emit :abstract}
   {:op :x-is-array?      :symbol #{'x:is-array?}       :emit :abstract}])

(def +op-xtalk-bit+
  [{:op :x-bit-and         :symbol #{'x:bit-and}          :macro #'tf-bit-and  :emit :macro}
   {:op :x-bit-or          :symbol #{'x:bit-or}           :macro #'tf-bit-or  :emit :macro}
   {:op :x-bit-lshift      :symbol #{'x:bit-lshift}       :macro #'tf-bit-lshift  :emit :macro}
   {:op :x-bit-rshift      :symbol #{'x:bit-rshift}       :macro #'tf-bit-rshift  :emit :macro}
   {:op :x-bit-xor         :symbol #{'x:bit-xor}          :macro #'tf-bit-xor  :emit :macro}])

(def +op-xtalk-lu+       
  [{:op :x-lu-create      :symbol #{'x:lu-create}       :emit :unit :default {}}
   {:op :x-lu-eq          :symbol #{'x:lu-eq}           :macro #'tf-lu-eq :emit :macro}
   {:op :x-lu-get         :symbol #{'x:lu-get}          :emit :abstract}
   {:op :x-lu-set         :symbol #{'x:lu-set}          :emit :abstract}
   {:op :x-lu-del         :symbol #{'x:lu-del}          :emit :abstract}])

(def +op-xtalk-obj+
  [{:op :x-obj-keys       :symbol #{'x:obj-keys}          :type :hard-link
    :raw 'xt.lang.base-lib/obj-keys}
   {:op :x-obj-vals       :symbol #{'x:obj-vals}          :type :hard-link
    :raw 'xt.lang.base-lib/obj-vals}
   {:op :x-obj-pairs      :symbol #{'x:obj-pairs}         :type :hard-link
    :raw 'xt.lang.base-lib/obj-pairs}
   {:op :x-obj-clone      :symbol #{'x:obj-clone}         :type :hard-link
    :raw 'xt.lang.base-lib/obj-clone}
   {:op :x-obj-assign     :symbol #{'x:obj-assign}        :type :hard-link
    :raw 'xt.lang.base-lib/obj-assign}])

(def +op-xtalk-arr+
  [{:op :x-arr-clone       :symbol #{'x:arr-clone}        :type :hard-link
    :raw 'xt.lang.base-lib/arr-clone}
   {:op :x-arr-slice       :symbol #{'x:arr-splice}       :type :hard-link
    :raw 'xt.lang.base-lib/arr-slice}
   {:op :x-arr-reverse     :symbol #{'x:arr-reverse}      :type :hard-link
    :raw 'xt.lang.base-lib/arr-reverse}
   {:op :x-arr-find        :symbol #{'x:arr-find}         :type :hard-link
    :raw 'xt.lang.base-lib/arr-find}
   {:op :x-arr-remove      :symbol #{'x:arr-remove}       :emit :abstract}
   {:op :x-arr-push        :symbol #{'x:arr-push}         :emit :abstract}
   {:op :x-arr-pop         :symbol #{'x:arr-pop}          :emit :abstract}
   {:op :x-arr-push-first  :symbol #{'x:arr-push-first}   :emit :abstract}
   {:op :x-arr-pop-first   :symbol #{'x:arr-pop-first}    :emit :abstract}
   {:op :x-arr-insert      :symbol #{'x:arr-insert}       :emit :abstract}
   {:op :x-arr-sort        :symbol #{'x:arr-sort}         :emit :abstract}
   {:op :x-arr-str-comp    :symbol #{'x:arr-str-comp}     :emit :abstract}])

(def +op-xtalk-str+
  [{:op :x-str-len         :symbol #{'x:str-len}         :emit :alias :raw 'x:len}
   {:op :x-str-char        :symbol #{'x:str-char}        :emit :abstract}
   {:op :x-str-format      :symbol #{'x:str-format}      :emit :abstract}
   {:op :x-str-split       :symbol #{'x:str-split}       :emit :abstract}
   {:op :x-str-join        :symbol #{'x:str-join}        :emit :abstract}
   {:op :x-str-index-of    :symbol #{'x:str-index-of}    :emit :abstract}
   {:op :x-str-substring   :symbol #{'x:str-substring}   :emit :abstract}
   {:op :x-str-to-upper    :symbol #{'x:str-to-upper}    :emit :abstract}
   {:op :x-str-to-lower    :symbol #{'x:str-to-lower}    :emit :abstract}
   {:op :x-str-to-fixed    :symbol #{'x:str-to-fixed}    :emit :abstract}
   {:op :x-str-replace     :symbol #{'x:str-replace}     :emit :abstract}
   {:op :x-str-trim        :symbol #{'x:str-trim}        :emit :abstract}
   {:op :x-str-trim-left   :symbol #{'x:str-trim-left}   :emit :abstract}
   {:op :x-str-trim-right  :symbol #{'x:str-trim-right}  :emit :abstract}])

(def +op-xtalk-js+
  [{:op :x-json-encode      :symbol #{'x:json-encode}       :emit :abstract}
   {:op :x-json-decode      :symbol #{'x:json-decode}       :emit :abstract}])

(def +op-xtalk-return+
  [{:op :x-return-encode   :symbol #{'x:return-encode}   :emit :abstract}
   {:op :x-return-wrap     :symbol #{'x:return-wrap}     :emit :abstract}
   {:op :x-return-eval     :symbol #{'x:return-eval}     :emit :abstract}])

(def +op-xtalk-socket+
  [{:op :x-socket-connect  :symbol #{'x:socket-connect}   :emit :abstract}
   {:op :x-socket-send     :symbol #{'x:socket-send}      :emit :abstract}
   {:op :x-socket-close    :symbol #{'x:socket-close}     :emit :abstract}])

(def +op-xtalk-iter+      
  [{:op :x-iter-from-obj  :symbol #{'x:iter-from-obj}   :emit :abstract}
   {:op :x-iter-from-arr  :symbol #{'x:iter-from-arr}   :emit :abstract}
   {:op :x-iter-from      :symbol #{'x:iter-from}       :emit :abstract}
   {:op :x-iter-eq        :symbol #{'x:iter-eq}         :emit :abstract}
   {:op :x-iter-null      :symbol #{'x:iter-null}       :emit :unit :default '(return)}
   {:op :x-iter-next      :symbol #{'x:iter-next}       :emit :abstract}
   {:op :x-iter-has?      :symbol #{'x:iter-has?}       :emit :abstract}
   {:op :x-iter-native?   :symbol #{'x:iter-native?}    :emit :abstract}])

(def +op-xtalk-cache+
  [{:op :x-cache          :symbol #{'x:cache}           :emit :abstract}
   {:op :x-cache-list     :symbol #{'x:cache-list}      :emit :abstract}
   {:op :x-cache-flush    :symbol #{'x:cache-flush}     :emit :abstract}
   {:op :x-cache-get      :symbol #{'x:cache-get}       :emit :abstract}
   {:op :x-cache-set      :symbol #{'x:cache-set}       :emit :abstract}
   {:op :x-cache-del      :symbol #{'x:cache-del}       :emit :abstract}
   {:op :x-cache-incr     :symbol #{'x:cache-incr}      :emit :abstract}])

(def +op-xtalk-thread+
  [{:op :x-thread-spawn   :symbol #{'x:thread-spawn}    :emit :abstract}
   {:op :x-thread-join    :symbol #{'x:thread-join}     :emit :abstract}
   {:op :x-with-delay     :symbol #{'x:with-delay}      :emit :abstract}
   {:op :x-start-interval :symbol #{'x:start-interval}  :emit :abstract}
   {:op :x-stop-interval  :symbol #{'x:stop-interval}   :emit :abstract}])

(def +op-xtalk-file+
  [{:op :x-slurp          :symbol #{'x:slurp}         :emit :abstract}
   {:op :x-spit           :symbol #{'x:spit}          :emit :abstract}])

(def +op-xtalk-b64+
  [{:op :x-b64-encode      :symbol #{'x:b64-encode}     :emit :abstract}
   {:op :x-b64-decode      :symbol #{'x:b64-decode}     :emit :abstract}])

(def +op-xtalk-uri+
  [{:op :x-uri-encode      :symbol #{'x:uri-encode}     :emit :abstract}
   {:op :x-uri-decode      :symbol #{'x:uri-decode}     :emit :abstract}])

(def +op-xtalk-special+
  [{:op :x-notify-http     :symbol #{'x:notify-http}    :type :hard-link
    :raw 'xt.lang.base-repl/notify-socket-http}])




(comment
  (./reload-specs)

  )


(comment
  (def +op-xtalk-notify+
    [{:op :x-notify-socket   :symbol #{'x:notify-socket}   :emit :abstract}
     {:op :x-notify-http     :symbol #{'x:notify-http}     :emit :abstract}])
  
  (def +op-xtalk-service+
    [{:op :x-client-basic    :symbol #{'x:client-basic}    :emit :abstract}
     {:op :x-client-ws       :symbol #{'x:client-ws}       :emit :abstract}
     {:op :x-server-basic    :symbol #{'x:server-basic}    :emit :abstract}
     {:op :x-server-ws       :symbol #{'x:server-ws}       :emit :abstract}])

  (def +op-xtalk-arr-generic+
  '[{:op :x-arr-every       :symbol #{'x:arr-every}   :emit :alias
     :raw xt.lang.base-lib/arr-every}
    {:op :x-arr-some        :symbol #{'x:arr-some}
     :raw xt.lang.base-lib/arr-every}
    {:op :x-arr-foldl       :symbol #{'x:arr-foldl}
     :raw xt.lang.base-lib/arr-foldl}
    {:op :x-arr-foldr       :symbol #{'x:arr-foldr}         :macro #'tf-arr-foldr   :emit :macro}
    {:op :x-arr-reverse     :symbol #{'x:arr-reverse}       :macro #'tf-arr-reverse :emit :macro}])  
  {:op :x-str-pad-left    :symbol #{'x:str-pad-left}    :macro #'tf-str-pad-left  :emit :macro}
  {:op :x-str-pad-right   :symbol #{'x:str-pad-right}   :macro #'tf-str-pad-right :emit :macro}
  {:op :x-str-starts-with :symbol #{'x:str-starts-with} :macro #'tf-str-starts-with :emit :macro}
  {:op :x-str-ends-with   :symbol #{'x:str-ends-with}   :macro #'tf-str-ends-with :emit :macro}
  {:op :x-ws-connect      :symbol #{'x:ws-connect}       :emit :abstract}
  {:op :x-ws-send         :symbol #{'x:ws-send}          :emit :abstract}
  {:op :x-ws-close        :symbol #{'x:ws-close}         :emit :abstract})
