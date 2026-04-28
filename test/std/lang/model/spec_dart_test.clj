(ns std.lang.model.spec-dart-test
  (:require [std.lang :as l]
            [std.lang.model.spec-dart :as spec-dart])
  (:use code.test))

(fact "basic dart emission"

  (l/emit-as :dart ['(var greeting "hello")])
  => "var greeting = \"hello\""

  (l/emit-as :dart ['(defn hello [name] (return (x:cat "hi " name)))])
  => "hello(name) {\n  return \"hi \" + name;\n}"

  (l/emit-as :dart ['(new Person name)])
  => "new Person(name)"

  (l/emit-as :dart ['(for [(var i := 0) (< i 3) (:++ i)] (print i))])
  => "for(var i = 0; i < 3; ++i){\n  print(i);\n}"

  (l/emit-as :dart ['{:name "hello" :count 2}])
  => "<dynamic, dynamic>{\"name\":\"hello\",\"count\":2}")

(fact "xtalk helper rewrites for dart"
  (l/emit-as :dart ['(x:print "hello")])
  => "(() {\n  print(\"hello\");\n  return null;\n})()"

  (l/emit-as :dart ['(x:len [1 2 3])])
  => "[1,2,3].length"

  (l/emit-as :dart ['(x:len [1 2 3])])
  => "[1,2,3].length"

  (l/emit-as :dart ['(x:arr-push items 1)])
  => "items.add(1)")

(fact "dart truthy rewrites evaluate expressions once"
  (let [out (l/emit-as :dart ['(if (not (probe value))
                                 1
                                 2)])]
    (count (re-seq #"probe\(value\)" out)))
  => 1)

(fact "dart globals target the injected runtime map"
  (let [out (l/emit-as :dart ['(do (x:global-set HELLO 1)
                                   (x:global-has? HELLO))])]
    [(boolean (re-find #"__globals__" out))
     (boolean (re-find #"globalThis" out))])
  => [true false])

^{:refer std.lang.model.spec-dart/dart-map-key :added "4.1"}
(fact "emits map keys for dart"

  (spec-dart/dart-map-key :hello spec-dart/+grammar+ {})
  => "\"hello\""

  (spec-dart/dart-map-key '(+ a 1) spec-dart/+grammar+ {})
  => "a + 1")

(fact "xtalk error throws"
  (l/emit-as :dart ['(x:err "error")])
  => "throw \"error\"")

(fact "xtalk exception helpers emit structured throw/catch for dart"
  (let [out (l/emit-as :dart ['(try
                                (throw (x:ex "error" {:a 1}))
                                (catch e
                                  (x:print (x:ex-message e))
                                  (x:print (x:ex-data e))))])]
    [(boolean (re-find #"xt\.exception" out))
     (boolean (re-find #"\[\"message\"\]" out))
     (boolean (re-find #"\[\"data\"\]" out))
     (boolean (re-find #"print" out))])
  => [true true true true])

(fact "for:* transforms for dart"
  ^{:hidden true}

  (let [[tag [entry-binding in entries] k-binding v-binding body]
        (spec-dart/tf-for-object '(for:object [[k v] obj]
                                   [k v]))]
    [(= 'for tag)
     (= :in in)
     (= '(. obj entries) entries)
     (= 'var* (first entry-binding))
     (= k-binding (list 'var* 'k := (list '. (second entry-binding) 'key)))
     (= v-binding (list 'var* 'v := (list '. (second entry-binding) 'value)))
     (= '[k v] body)])
  => [true true true true true true true]

  (let [[tag [v-binding in values] body]
        (spec-dart/tf-for-object '(for:object [[_ v] obj]
                                   v))]
    [(= 'for tag)
     (= '(var* v) v-binding)
     (= :in in)
     (= '(. obj values) values)
     (= 'v body)])
  => [true true true true true]

  (let [[tag init loop]
        (spec-dart/tf-for-array '(for:array [[i e] arr]
                                   [i e]))
        arr-sym (second init)
        [_ [i-binding test step] e-binding body] loop]
    [(= 'do tag)
     (= '(var* arr) (list (first init) (last init)))
     (= 'for (first loop))
     (= '(var* i := 0) i-binding)
     (= test (list '< 'i (list '. arr-sym 'length)))
     (= '(:++ i) step)
     (= e-binding (list 'var* 'e := (list '. arr-sym ['i])))
     (= '[i e] body)])
  => [true true true true true true true true]

  (let [[tag init loop]
        (spec-dart/tf-for-array '(for:array [e arr]
                                   e))
        arr-sym (second init)
        [_ [i-binding test step] e-binding body] loop]
    [(= 'do tag)
     (= '(var* arr) (list (first init) (last init)))
     (= 'for (first loop))
     (= 'var* (first i-binding))
     (= 0 (last i-binding))
     (= test (list '< (second i-binding) (list '. arr-sym 'length)))
     (= step (list ':++ (second i-binding)))
     (= e-binding (list 'var* 'e := (list '. arr-sym [(second i-binding)])))
     (= 'e body)])
  => [true true true true true true true true true]

  (let [[tag init loop]
        (spec-dart/tf-for-iter '(for:iter [e iter]
                                  e))
        iter-sym (second init)
        [_ test e-binding body] loop]
    [(= 'do tag)
     (= '(var* iter) (list (first init) (last init)))
     (= 'while (first loop))
     (= test (list '. iter-sym (list 'moveNext)))
     (= e-binding (list 'var* 'e := (list '. iter-sym 'current)))
     (= 'e body)])
  => [true true true true true true]
  )

(fact "for:* emission for dart"
  (let [out (l/emit-as :dart ['(for:object [[k v] obj]
                               [k v])])]
    [(boolean (re-find #"for\(var entry_" out))
     (boolean (re-find #"in obj\.entries\)\{" out))
     (boolean (re-find #"var k = entry_.*\.key;" out))
     (boolean (re-find #"var v = entry_.*\.value;" out))])
  => [true true true true]

  (let [out (l/emit-as :dart ['(for:array [[i e] arr]
                               [i e])])]
    [(boolean (re-find #"var arr_" out))
     (boolean (re-find #"for\(var i = 0; i < arr_.*\.length; \+\+i\)" out))
     (boolean (re-find #"var e = arr_.*\[i\];" out))])
  => [true true true])

^{:refer std.lang.model.spec-dart/dart-var :added "4.1"}
(fact "transforms var destructuring for dart"

  (let [[op init a b] (spec-dart/dart-var '(var [a b] expr))]
    [op
     (and (= 'var* (first init)) (= 'expr (last init)))
     (= a (list 'var* 'a := (list '. (second init) [0])))
     (= b (list 'var* 'b := (list '. (second init) [1])))])
  => ['do* true true true]

  (spec-dart/dart-var '(var #{id} data-obj))
  => '(do* (var* id := (. data-obj ["id"])))

  (let [result (spec-dart/dart-var '(var #{name args} v))]
    [(first result) (count result)])
  => ['do* 3]

  (spec-dart/dart-var '(var x 42))
  => '(var* x := 42)

  (spec-dart/dart-var '(var entry))
  => '(var* entry))

(fact "dart var destructuring emission"

  (l/emit-as :dart ['(var [a b] expr)])
  => (fn [s]
       (and (string? s)
            (clojure.string/includes? s "var a = ")
            (clojure.string/includes? s "var b = ")))

  (l/emit-as :dart ['(var #{id} data_obj)])
  => "var id = data_obj[\"id\"]"

  (l/emit-as :dart ['(var x 42)])
  => "var x = 42")

(fact "dart promise helpers emit native future chains"

  (let [out (l/emit-as :dart ['(x:promise (fn [] (return 5)))])]
    [(boolean (re-find #"Future\.sync" out))
     (boolean (re-find #"return 5;" out))])
  => [true true]

  (let [out (l/emit-as :dart ['(x:promise-finally
                               (x:promise-catch
                                (x:promise-then p
                                                (fn [ok]
                                                  (return ok)))
                                (fn [err]
                                  (return err)))
                               (fn []
                                 (return true)))])]
    [(boolean (re-find #"\.then\(" out))
     (boolean (re-find #"\.catchError\(" out))
     (boolean (re-find #"\.whenComplete\(" out))])
  => [true true true]

  (let [out (l/emit-as :dart ['(x:with-delay 100
                                             (fn []
                                               (return "ok")))])]
    [(boolean (re-find #"Future\.delayed" out))
     (boolean (re-find #"Duration\(milliseconds:\s*100\s*\)" out))
     (boolean (re-find #"return \"ok\";" out))])
  => [true true true])


^{:refer std.lang.model.spec-dart/tf-for-object :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-dart/tf-for-array :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-dart/tf-for-iter :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-dart/dart-tf-ternary :added "4.1"}
(fact "TODO")