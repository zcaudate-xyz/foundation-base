(ns js.core.impl
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str])
  (:refer-clojure :exclude [abs concat eval filter find keys map
                            max min name pop read reduce replace reverse some sort]))

(l/script :js js.core)

(defmacro.js ^{:style/indent 1}
  delayed
  "constructs a setTimeout function
 
   (notify/wait-on :js
    (j/delayed [10]
      (repl/notify 1)))
   => 1"
  {:added "4.0"}
  [[ms] & body]
  (h/$ (setTimeout (fn [] (new Promise (fn []
                                         ~@body)))
                   ~ms)))

(defmacro.js  ^{:style/indent 1}
  repeating
  "constructs a setInterval function
 
   ((:template @j/repeating) [10]
    '(doSomething))
   => '(setInterval (fn [] (new Promise (fn [] (doSomething)))) 10)"
  {:added "4.0"}
  [[ms] & body]
  (h/$ (setInterval (fn [] (new Promise (fn []
                                          ~@body)))
                    ~ms)))

(defmacro.js postMessage
  "post message
 
   ((:template @j/postMessage) 'worker [1 2 3])
   => '(. worker (postMessage [1 2 3]))"
  {:added "4.0"}
  [worker value]
  (list '. worker (list 'postMessage value)))

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :tag "js"}]
  [super
   [JSGlobal globalThis]
   setInterval
   clearInterval
   setTimeout
   clearTimeout
   [write JSON.stringify]
   [read JSON.parse]
   eval
   decodeURI
   decodeURIComponent
   encodeURI
   encodeURIComponent

   atob
   btoa   
   
   [JSObject Object]
   [JSFunction Function]
   [JSSymbol Symbol]
   [JSBoolean Boolean]
   [JSNumber Number]
   [JSMath Math]
   [JSBigInt BigInt]
   [JSDate  Date]
   [JSString String]
   [JSRegExp RegExp]
   [JSArray Array]
   [JSArrayBuffer ArrayBuffer]
   [JSSet Set]
   [JSMap Map]
   
   [JSError Error]
   [JSInternalError InternalError]

   JSON
   AggregateError
   EvalError
   
   RangeError
   ReferenceError
   SyntaxError
   TypeError
   URIError
   
   Int8Array
   Uint8Array
   Uint8ClampedArray
   Int16Array
   Uint16Array
   Int32Array
   Uint32Array
   Float32Array
   Float64Array
   BigInt64Array
   BigUint64Array

   WeakSet
   WeakMap
   
   Atomics
   DataView

   GeneratorFunction
   AsyncGeneratorFunction
   Generator
   AsyncGenerator
   AsyncFunction
   Promise
   Reflect
   Proxy

   Intl
   Intl.Collator
   Intl.DateTimeFormat
   Intl.DisplayNames
   Intl.ListFormat
   Intl.Locale
   Intl.NumberFormat
   Intl.PluralRules
   Intl.RelativeTimeFormat

   WebAssembly
   WebAssembly.Module
   WebAssembly.Instance
   WebAssembly.Memory
   WebAssembly.Table
   WebAssembly.CompileError
   WebAssembly.LinkError
   WebAssembly.RuntimeError])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :tag "js"
                                   :base "Math"}]
  [E LN10 LN2 LOG10E LOG2E PI SQRT1_2 SQRT2
   abs acos acosh asin asinh atan atanh atan2 cbrt
   ceil clz32 cos cosh exp expm1 floor fround hypot imul
   log log1p log10 log2 max min pow
   random round sign sin sinh sqrt tan tanh trunc])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :tag "js"
                                   :base "Number"}]
  [EPSILON
   MAX_SAFE_INTEGER
   MAX_VALUE
   MIN_SAFE_INTEGER
   MIN_VALUE
   NaN
   NEGATIVE_INFINITY
   POSITIVE_INFINITY
   isNaN
   isFinite
   isInteger
   isSafeInteger
   parseFloat
   parseInt])

(h/template-entries [l/tmpl-macro {:base "Number"
                                   :inst "num"
                                   :tag "js"}]
  [[toExponetial [d]]
   [toFixed [d]]
   [toPrecision [d]]
   [[toRadix toString] [radix]]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :tag "js"
                                   :base "Object"}]
  [assign
   [createObject create]
   defineProperties
   defineProperty
   entries
   freeze
   fromEntries
   getOwnPropertyDescriptor
   getOwnPropertyDescriptors
   getOwnPropertyNames
   getOwnPropertySymbols
   getPrototypeOf
   isExtensible
   isFrozen
   isSealed
   keys
   preventExtenions
   seal
   setPrototypeOf
   values])

(h/template-entries [l/tmpl-macro {:base "Object"
                                   :inst "obj"
                                    :tag "js"}]
  [[hasOwnProperty [prop]]
   [isPrototypeOf  [type]]
   [propertyIsEnumerable [prop]]
   [toLocaleString []]
   [toString []]
   [valueOf []]
   [length nil {:property true}]
   [name   nil {:property true}]])

(h/template-entries [l/tmpl-macro {:base "Function"
                                   :inst "func"
                                   :tag "js"}]
  [[applyThis [thisArg args]]
   [bind      [thisArg] {:vargs arrs}]
   [callThis  [thisArg] {:vargs arrs}]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :tag "js"
                                   :base "Array"}]
  [[toArray from]
   isArray
   [createArray of]])

(h/template-entries [l/tmpl-macro {:base "Iterable"
                                   :inst "seq"
                                   :empty []
                                   :tag "js"}]
  [[concat     [arr]    {:vargs arrs}]
   [indexOf    [val] {:optional [startIdx]}]
   [includes   [val] {:optional [startIdx]}]
   [lastIndexOf  [val] {:optional [fromIdx]}]
   [slice [] {:optional [start end]}]
   [splice [start] {:optional [count e]
                    :vargs es}]])

(h/template-entries [l/tmpl-macro {:base "Array"
                                   :inst "arr"
                                   :empty []
                                   :tag "js"}]
  [[copyWithin [target] {:optional [start end]}]
   [every  [f]]
   [fill  [value] {:optional [start end]}]
   [filter [f]]
   [find [f]]
   [findIndex [f]]
   [flat [] {:optional [depth]}]
   [flatMap [f]]
   [forEach [f]]
   [join [] {:optional [sep]}]
   [map [f]]
   [reduce [f] {:optional [init]}]
   [reduceRight [f] {:optional [init]}]
   [reverse []]
   [some [f]]
   [sort []  {:optional [comp]}]])

(h/template-entries [l/tmpl-macro {:base "PArray"
                                   :inst "arr"
                                   :empty []
                                   :tag "js"}]
  [[pop []]
   [push [e] {:vargs es}]
   [shift []]
   [unshift [e] {:vargs es}]])

(h/template-entries [l/tmpl-macro {:base "TArray"
                                   :inst "tarr"
                                    :tag "js"}]
  [[[setTArr set] [arr] {:optional [offset]}]
   [subarray [] {:optional [start end]}]])

(h/template-entries [l/tmpl-macro {:base "String"
                                   :inst  "s"
                                   :empty ""
                                   :tag "js"}]
  [[charAt [idx]]
   [charCodeAt [idx]]
   [codePointAt [pos]]

   [endsWith [match] {:optional [pos]}]
   [localeCompare [match] {:optional [locales opts]}]
   [match [re]]
   [matchAll [re]]
   [normalize [form]]
   [padEnd [n] {:optional [fill]}]
   [padStart [n] {:optional [fill]}]
   [replace [match val]]
   [replaceAll [match val]]
   [search [re]]
   [split [match] {:optional [limit]}]
   [startsWith [match] {:optional [pos]}]
   [substr [start] {:optional [len]}]
   [substring [start] {:optional [pos]}]
   [toLocaleLowerCase [locales]]
   [toLowerCase []]
   [toLocaleUpperCase [locales]]
   [toUpperCase []]
   [trim []]
   [trimStart []]
   [trimEnd []]])

(h/template-entries [l/tmpl-macro {:base "Map"
                                   :inst "h"
                                   :tag "js"}]
  [[[hGet get] [key]]
   [[hHas has] [key]]
   [[hSet set] [key val]]
   [[hAdd add] [key]]
   [[hClear clear] []]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :tag "js"
                                   :base "Map"}]
  [[hDel prototype.delete]])

;;
;; Date
;;


(h/template-entries [l/tmpl-entry {:type :fragment
                                   :tag "js"
                                   :base "Date"}]
  [now
   [parseDate parse]
   UTC])

(h/template-entries [l/tmpl-macro {:base "Date"
                                   :inst "func"
                                    :tag "js"}]
  [[getDate []]
   [getDay []]
   [getFullYear []]
   [getHours []]
   [getMilliseconds []]
   [getMinutes []]
   [getMonth []]
   [getSeconds []]
   [getTime []]
   [getTimezoneOffset []]
   [getUTCDate []]
   [getUTCDay []]
   [getUTCFullYear []]
   [getUTCHours []]
   [getUTCMilliseconds []]
   [getUTCMinutes []]
   [getUTCMonth []]
   [getUTCSeconds []]
   [getYear []]
   [toDateString []]
   [toISOString []]
   [toJSON []]
   [toGMTString []]
   [toLocaleDateString []]
   [toLocaleFormat []]
   [toLocaleString []]
   [toLocaleTimeString []]
   [toString []]
   [toTimeString []]
   [toUTCString []]

   [setDate [val]]
   [setFullYear [val]]
   [setHours [val]]
   [setMilliseconds [val]]
   [setMinutes [val]]
   [setMonth [val]]
   [setSeconds [val]]
   [setTime [val]]
   [setUTCDate [val]]
   [setUTCFullYear [val]]
   [setUTCHours [val]]
   [setUTCMilliseconds [val]]
   [setUTCMinutes [val]]
   [setUTCMonth [val]]
   [setUTCSeconds [val]]
   [setYear [val]]])

(h/template-entries [l/tmpl-macro {:base "Generator"
                                   :inst "gen"
                                   :tag "js"}]
  [[[genNext next]  []]
   [[genReturn return]   []]
   [[genThrow throw] []]])

;;
;; Promise
;;

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :tag "js"
                                   :base "Promise"}]
  [[onAll all]
   [onAllSettled allSettled]
   [onAny any]
   [onRace race]
   [onReject reject]
   [onResolve resolve]])

(h/template-entries [l/tmpl-macro {:base "Promise"
                                   :inst "p"
                                   :tag "js"}]
  [[catch [onError]]
   [then  [onSuccess] {:optional [onError]}]
   [finally [onDone]]])

(h/template-entries [l/tmpl-macro {:base "DataView"
                                   :inst "dv"
                                   :tag "js"}]
  [[getUint8 [idx]]
   [getInt16 [idx]]
   [getUint16 [idx]]
   [getInt32 [idx]]
   [getUint32 [idx]]
   [getFloat32 [idx]]
   [getFloat64 [idx]]
   [getBigInt64 [idx]]
   [getBigUint64 [idx]]

   [setInt8 [idx val]]
   [setUint8 [idx val]]
   [setInt16 [idx val]]
   [setUint16 [idx val]]
   [setInt32 [idx val]]
   [setUint32 [idx val]]
   [setFloat32 [idx val]]
   [setFloat64 [idx val]]
   [setBigInt64 [idx val]]
   [setBigUint64 [idx val]]])

;;
;; Atomics
;;

(comment
  
  (h/template-entries [l/tmpl-entry {:type :fragment
                                     :tag "js"
                                     :base "Atomics"
                                     :prefix "at"}]
    [add
     and
     compareExchange
     exchange
     isLockFree
     load
     notify
     or
     store
     sub
     wait
     xor])


  ;;
  ;; WebAssembly
  ;;

  (h/template-entries [l/tmpl-entry {:type :fragment
                                     :tag "js"
                                     :base "WebAssembly"
                                     :prefix "ws"}]
    [instantiate
     instantiateStreaming
     compile
     compileStreaming
     validate]))
