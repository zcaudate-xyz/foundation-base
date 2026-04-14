(ns std.lang.model.spec-python-test
  (:require [std.lang :as l]
            [std.lang.model.spec-python :as py]
            [std.string.prose :as prose])
  (:use code.test))

^{:refer std.lang.model.spec-python/python-defn- :added "4.0"}
(fact "hidden function without decorators"
  ^:hidden
  
  (l/emit-as
   :python '[(defn- hello [] (return 1))])
  => "def hello():\n  return 1")

^{:refer std.lang.model.spec-python/python-defn :added "4.0"}
(fact "creates a defn function for python"
  ^:hidden
  
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
  ^:hidden
  
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
   :python '[(fn hello [] (return 1))])
  => "def hello():\n  return 1")

^{:refer std.lang.model.spec-python/python-defclass :added "4.0"}
(fact "emits a defclass template for python"
  ^:hidden
  
  (l/emit-as
   :python '[(var* :% (StringProperty :default "*.text"
                                      :options #{"HIDDEN"}
                                      :maxlen 255)
                   bl-text)])
  => "bl_text: StringProperty(default=\"*.text\",options={\"HIDDEN\"},maxlen=255)"
  
  (l/emit-as
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
                   
                   (return #{"FINISHED"}))))])
  => (prose/|
      "class ReloadScriptsOperator(bpy.types.Operator):"
      "  bl_idname = \"script\""
      "  bl_label = \"Reload code\""
      "  bl_description = \"Reloads all distance code.\""
      "  bl_text: StringProperty(default=\"*.text\",options={\"HIDDEN\"},maxlen=255)"
      "  "
      "  @classmethod"
      "  @classmethod"
      "  @classmethod"
      "  def execute(self,context):"
      "    my_path = bpy.path.abspath(\"//\")"
      "    if not my_path:"
      "      self.report({\"ERROR\"},\"Save the Blend file first\")"
      "      return {\"CANCELLED\"}"
      "    return {\"FINISHED\"}"
      ""))

^{:refer std.lang.model.spec-python/python-var :added "4.0"}
(fact "var -> fn.inner shorthand"
  ^:hidden
  
  (py/python-var '(var hello (fn [])))
  => '(fn.inner hello [])

  (py/python-var '(var hello))
  => '(var* hello := nil))

^{:refer std.lang.model.spec-python/tf-for-object :added "4.0"}
(fact "for object loop"
  ^:hidden
  
  (py/tf-for-object '(for:object [[k v] arr]
                                 [k v]))
  
  => '(for [[k v] :in (. arr (items))] [k v]))

^{:refer std.lang.model.spec-python/tf-for-array :added "4.0"}
(fact  "for array loop"
  ^:hidden

  (py/tf-for-array '(for:array [[i e] arr]
                               [i e]))
  => '(for [i :in (range (len arr))] (var e (. arr [i])) [i e])

  (py/tf-for-array '(for:array [e arr]
                               e))
  => '(for [e :in arr] e))

^{:refer std.lang.model.spec-python/tf-for-iter :added "4.0"}
(fact "for iter loop"
  ^:hidden
  
  (py/tf-for-array '(for:iter [e it]
                               e))
  => '(for [e :in it] e))

^{:refer std.lang.model.spec-python/tf-for-index :added "4.0"}
(fact "for index transform"
  ^:hidden
  
  (py/tf-for-index '(for:index [i [0 2 10]]
                               i))
  => '(for [i :in (range 0 2 10)] i))

^{:refer std.lang.model.spec-python/tf-for-return :added "4.0"}
(fact "for return transform"
  ^:hidden
  
  (py/tf-for-return '(for:return [[ok err] (call)]
                                 {:success (return ok)
                                  :error   (return err)}))
  => '(try (var ok (call))
           (return ok)
           (catch [Exception :as err]
               (return err)))

  (py/tf-for-return '(for:return [[ok err] (x:return-run runner)]
                                 {:success (return ok)
                                  :error   (return err)}))
  => '(do (var ok nil)
          (try
            (runner
             (fn [value]
               (:= ok value))
             (fn [value]
               (throw value)))
            (return ok)
            (catch [Exception :as err]
              (return err)))))
