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
  => "{\"name\":\"hello\",\"count\":2}")

(fact "xtalk helper rewrites for dart"
  (l/emit-as :dart ['(x:print "hello")])
  => "print(\"hello\")"

  (l/emit-as :dart ['(x:len [1 2 3])])
  => "[1,2,3].length"

  (l/emit-as :dart ['(x:len [1 2 3])])
  => "[1,2,3].length"

  (l/emit-as :dart ['(x:arr-push items 1)])
  => "items.add(1)")

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

(fact "for:* transforms for dart"
  ^{:hidden true}

  (spec-dart/tf-for-object '(for:object [[k v] obj]
                             [k v]))
  => '(for [(var entry) :in (. obj entries)]
        (var k (. entry key))
        (var v (. entry value))
        [k v])

  (spec-dart/tf-for-object '(for:object [[_ v] obj]
                             v))
  => '(for [(var v) :in (. obj values)]
        v)

  (spec-dart/tf-for-array '(for:array [[i e] arr]
                            [i e]))
  => '(for [(var i := 0) (< i (. arr length)) (:++ i)]
        (var e (. arr [i]))
        [i e])

  (spec-dart/tf-for-array '(for:array [e arr]
                            e))
  => '(for [(var e) :in arr]
        e)

  (spec-dart/tf-for-iter '(for:iter [e iter]
                           e))
  => '(for [(var e) :in iter]
        e)

  (spec-dart/tf-for-return '(for:return [[ok err] (call (x:callback))]
                               {:success (return ok)
                                :error   (return err)}))
  => '(call (fn [err ok]
              (if (not= err nil)
                (return err)
                (return ok))))

  (spec-dart/tf-for-return '(for:return [[ok err] (x:return-run runner)]
                               {:success (return ok)
                                :error   (return err)}))
  => '(do
        (var ok nil)
        (var err nil)
        (try
          (runner
           (fn [value]
             (:= ok value)
             (:= err nil))
           (fn [value]
             (:= ok nil)
             (:= err value)))
          (if err
            (return err)
            (return ok))
          (catch err
            (return err))))

  (spec-dart/tf-for-return '(for:return [[ok err] (x:return-run runner)]
                               {:success ok
                                :error   err
                                :final   true}))
  => '(do
        (var ok nil)
        (var err nil)
        (try
          (runner
           (fn [value]
             (:= ok value)
             (:= err nil))
           (fn [value]
             (:= ok nil)
             (:= err value)))
          (if err
            (return err)
            (return ok))
          (catch err
            (return err)))))

  (spec-dart/tf-for-try '(for:try [[ok err] (call)]
                            {:success (return ok)
                             :error   (return err)}))
  => '(try
        (var ok := (call))
        (return ok)
        (catch err (return err)))

  (spec-dart/tf-for-async '(for:async [[ok err] (call)]
                              {:success (return ok)
                               :error   (return err)
                               :finally (return true)}))
  => '(. (Future (fn []
                   (return (call))))
        (then (fn [ok]
                (return ok)))
        (catchError (fn [err]
                      (return err)))
        (whenComplete (fn []
                        (return true))))

(fact "for:* emission for dart"
  (l/emit-as :dart ['(for:object [[k v] obj]
                       [k v])])
  => "for(var entry in obj.entries){\n  var k = entry.key;\n  var v = entry.value;\n  [k,v];\n}"

  (l/emit-as :dart ['(for:array [[i e] arr]
                       [i e])])
  => "for(var i = 0; i < arr.length; ++i){\n  var e = arr[i];\n  [i,e];\n}"

  (l/emit-as :dart ['(for:async [[ok err] (call)]
                       {:success (return ok)
                        :error   (return err)
                        :finally (return true)})])
  => "Future(() {\n  return call();\n}).then((ok) {\n  return ok;\n}).catchError((err) {\n  return err;\n}).whenComplete(() {\n  return true;\n})")


^{:refer std.lang.model.spec-dart/tf-for-object :added "4.1"}
(fact "transforms for:object loops"
  (spec-dart/tf-for-object '(for:object [[k _] obj]
                             k))
  => '(for [(var k) :in (. obj keys)]
        k))

^{:refer std.lang.model.spec-dart/tf-for-array :added "4.1"}
(fact "transforms for:array loops"
  (spec-dart/tf-for-array '(for:array [[i e] arr]
                            [i e]))
  => '(for [(var i := 0) (< i (. arr length)) (:++ i)]
        (var e (. arr [i]))
        [i e]))

^{:refer std.lang.model.spec-dart/tf-for-iter :added "4.1"}
(fact "transforms for:iter loops"
  (spec-dart/tf-for-iter '(for:iter [e iter]
                           e))
  => '(for [(var e) :in iter]
        e))

^{:refer std.lang.model.spec-dart/tf-for-return :added "4.1"}
(fact "transforms for:return callbacks"
  (spec-dart/tf-for-return '(for:return [[ok err] (call (x:callback))]
                               {:success (return ok)
                                :error   (return err)}))
  => '(call (fn [err ok]
              (if (not= err nil)
                (return err)
                (return ok)))))

^{:refer std.lang.model.spec-dart/tf-for-try :added "4.1"}
(fact "transforms for:try blocks"
  (spec-dart/tf-for-try '(for:try [[ok err] (call)]
                            {:success (return ok)
                             :error   (return err)}))
  => '(try
        (var ok := (call))
        (return ok)
        (catch err (return err))))

^{:refer std.lang.model.spec-dart/tf-for-async :added "4.1"}
(fact "transforms for:async chains"
  (spec-dart/tf-for-async '(for:async [[ok err] (call)]
                              {:success (return ok)
                               :error   (return err)
                               :finally (return true)}))
  => '(. (Future (fn []
                   (return (call))))
        (then (fn [ok]
                (return ok)))
        (catchError (fn [err]
                      (return err)))
        (whenComplete (fn []
                        (return true))))

  (spec-dart/tf-for-async '(for:async [[ok err] (x:return-run runner)]
                              {:success (return ok)
                               :error   (return err)
                               :finally (return true)}))
  => '(. (do
           (var completer (new Completer))
           (try
             (runner
              (fn [ok]
                (. completer (complete ok)))
              (fn [err]
                (. completer (completeError err))))
             (catch err
               (. completer (completeError err))))
           (. completer future))
        (then (fn [ok]
                (return ok)))
        (catchError (fn [err]
                      (return err)))
        (whenComplete (fn []
                        (return true)))))

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
