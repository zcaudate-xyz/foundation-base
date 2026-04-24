(ns std.lang.model.spec-python-test
  (:require [std.lang :as l]
            [std.lang.model.spec-python :as py]
            [std.string.prose :as prose])
  (:use code.test))

^{:refer std.lang.model.spec-python/python-defn- :added "4.0"}
(fact "hidden function without decorators"

  (l/emit-as
   :python '[(defn- hello [] (return 1))])
  => "def hello():\n  return 1")

^{:refer std.lang.model.spec-python/python-defn :added "4.0"}
(fact "creates a defn function for python"

  (l/emit-as
   :python '[(defn ^{:decorators
                     [classmethod
                      classmethod
                      (app.route "/about")]}
               hello [] (return 1))])
  => (prose/|
      ""
      "@classmethod"
      "@classmethod"
      "@app.route(\"/about\")"
      "def hello():"
      "  return 1"))

^{:refer std.lang.model.spec-python/python-fn :added "4.0"}
(fact "basic transform for python lambdas"

  (l/emit-as
   :python '[(fn:> 1)])
  => "lambda : 1"

  (l/emit-as
   :python '[(fn [] 1)])
  => "lambda : 1"

  (l/emit-as
   :python '[(fn [] (return 1))])
  => "lambda : 1"

  (l/emit-as
   :python '[(fn [x] (return [x x]))])
  => "lambda x : [x,x]"

  (l/emit-as
   :python '[(fn [x] (return {:value x}))])
  => "lambda x : {\"value\":x}"

  (l/emit-as
   :python '[(fn hello [] (return 1))])
  => "def hello():\n  return 1")

^{:refer std.lang.model.spec-python/python-defclass :added "4.0"}
(fact "emits a defclass template for python"

  (l/emit-as
   :python '[(var* :% (StringProperty :default "*.text"
                                      :options #{"HIDDEN"}
                                      :maxlen 255)
                   bl-text)])
  => "bl_text: StringProperty(default=\"*.text\",options={\"HIDDEN\"},maxlen=255)"

  (let [out (l/emit-as
             :python '[(defclass ReloadScriptsOperator
                         [bpy.types.Operator]

                         (var bl-idname "script")
                         (var bl-label  "Reload code")
                         (var bl-description "Reloads all distance code.")

                         (var* :% (StringProperty :default "*.text"
                                                  :options #{"HIDDEN"}
                                                  :maxlen 255)
                               bl-text)

                         ^{:decorators
                           [classmethod
                            classmethod
                            classmethod]}
                         (fn execute [self context]
                           (let [my-path (bpy.path.abspath "//")]
                             (when [:not my-path]
                               (self.report #{"ERROR"} "Save the Blend file first")
                               (return #{"CANCELLED"}))

                             (return #{"FINISHED"}))))])]
    [(boolean (re-find #"class ReloadScriptsOperator\(bpy.types.Operator\):" out))
     (boolean (re-find #"bl_idname = \"script\"" out))
     (boolean (re-find #"bl_label = \"Reload code\"" out))
     (boolean (re-find #"bl_description = \"Reloads all distance code\.\"" out))
     (boolean (re-find #"bl_text: StringProperty\(default=\"\*\.text\",options=\{\"HIDDEN\"\},maxlen=255\)" out))
     (boolean (re-find #"def execute\(self,context\):" out))
     (boolean (re-find #"self\.report\(\{\"ERROR\"\},\"Save the Blend file first\"\)" out))
     (boolean (re-find #"return \{\"FINISHED\"\}" out))])
  => [true true true true true true true true])

^{:added "4.1"}
(fact "return can lower iterator-yield blocks in python"

  (l/emit-as
   :python '[(defn iter-fn []
               (return (x:iter-null)))])
  => "def iter_fn():\n  if False:\n    yield")

^{:refer std.lang.model.spec-python/python-var :added "4.0"}
(fact "var -> fn.inner shorthand"

  (py/python-var '(var hello (fn [x] x)))
  => '(fn.inner hello [x] x)

  (py/python-var '(var hello (fn [])))
  => '(fn.inner hello [])

  (py/python-var '(var hello))
  => '(var* hello := nil))

^{:refer std.lang.model.spec-python/tf-for-object :added "4.0"}
(fact "for object loop"

  (py/tf-for-object '(for:object [[k v] arr]
                                 [k v]))

  => '(for [[k v] :in (. arr (items))] [k v]))

^{:refer std.lang.model.spec-python/tf-for-array :added "4.0"}
(fact  "for array loop"

  (py/tf-for-array '(for:array [[i e] arr]
                               [i e]))
  => '(for [i :in (range (len arr))] (var e (. arr [i])) [i e])

  (py/tf-for-array '(for:array [e arr]
                               e))
  => '(for [e :in arr] e))

^{:refer std.lang.model.spec-python/tf-for-iter :added "4.0"}
(fact "for iter loop"

  (py/tf-for-array '(for:iter [e it]
                               e))
  => '(for [e :in it] e))

^{:refer std.lang.model.spec-python/tf-for-index :added "4.0"}
(fact "for index transform"

  (py/tf-for-index '(for:index [i [0 2 10]]
                               i))
  => '(for [i :in (range 0 2 10)] i))

^{:refer std.lang.model.spec-python/tf-for-return :added "4.0"}
(fact "for return transform"

  (py/tf-for-return '(for:return [[ok err] (call)]
                                 {:success (return ok)
                                  :error   (return err)}))
  => '(try (var ok (call))
           (return ok)
           (catch [Exception :as err]
               (return err)))

  (let [out (py/tf-for-return '(for:return [[ok err] (x:return-run runner)]
                                           {:success (return ok)
                                            :error   (return err)}))]
    [(= 'do (first out))
     (= '(var ok nil) (nth out 2))
     (= '(var err nil) (nth out 3))
     (= 'try (first (nth out 6)))
     (= 'runner (first (second (nth out 6))))
     (= '(if (not= nil err) (return err) (return ok))
        (nth (nth out 6) 4))])
  => [true true true true true true])

^{:refer std.lang.model.spec-python/tf-for-try :added "4.0"}
(fact "for try transform"

  (py/tf-for-try '(for:try [[ok err] (call)]
                            {:success (return ok)
                             :error   (return err)}))
  => '(try (var ok (call))
           (return ok)
           (catch [Exception :as err]
             (return err)))

  (let [out (py/tf-for-try '(for:try [[ok err] (do:> (x:err "ERROR"))]
                                      {:success (return ok)
                                       :error   (return err)}))]
    (and (= 'try (first out))
         (= 'fn.inner (-> out second first))
         (= 'var (-> out (nth 2) first))
         (= 'catch (-> out last first))))
  => true)
