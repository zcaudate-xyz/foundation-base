(ns code.java.compile-test
  (:use code.test)
  (:require [code.java.compile :refer :all]
            [code.project :as project]
            [std.fs :as fs])
  (:refer-clojure :exclude [supers]))

^{:refer code.java.compile/path->class :added "3.0"}
(fact "Creates a class symbol from a file"

  (path->class "test/Dog.java")
  => 'test.Dog

  (path->class "test/Cat.class")
  => 'test.Cat)

^{:refer code.java.compile/class->path :added "3.0"}
(fact "creates a file path from a class"

  (class->path test.Dog)
  => "test/Dog.class"

  (class->path 'test.Cat)
  => "test/Cat.class")

^{:refer code.java.compile/java-sources :added "3.0"}
(fact "lists source classes in a project"

  (->> (java-sources (project/project))
       (keys)
       (filter (comp #(.startsWith ^String % "test") str))
       (sort))
  => (contains '[test.Cat test.Dog test.DogBuilder
                 test.Person test.PersonBuilder test.Pet]))

^{:refer code.java.compile/class-object :added "3.0"}
(fact "creates a class object for use with compiler"
  
  (class-object "test.Cat"
                (fs/read-all-bytes "target/classes/test/Cat.class"))
  => javax.tools.SimpleJavaFileObject)

^{:refer code.java.compile/class-manager :added "3.0"}
(fact "creates a `ForwardingJavaFileManager`"

  (let [compiler  (javax.tools.ToolProvider/getSystemJavaCompiler)
        collector (javax.tools.DiagnosticCollector.)
        manager   (.getStandardFileManager compiler collector nil nil)
        cache     (atom {})]
    (class-manager manager cache))
  => javax.tools.ForwardingJavaFileManager)

^{:refer code.java.compile/supers :added "3.0"}
(fact "finds supers of a class given it's bytecode"

  (org.objectweb.asm.ClassReader.
   ^bytes (fs/read-all-bytes "target/classes/test/Cat.class"))
  
  (supers (fs/read-all-bytes "target/classes/test/Cat.class"))
  => #{"java.lang.Object" "test.Pet"})

^{:refer code.java.compile/javac-output :added "3.0"}
(fact "displays output of compilation")

^{:refer code.java.compile/javac-process :added "3.0"}
(fact "processes Java compilation, handling class reloading and output options"

  (javac-process {"test.Pet" (fs/read-all-bytes "target/classes/test/Pet.class")
                  "test.Cat" (fs/read-all-bytes "target/classes/test/Cat.class")}
                 {:output "target/classes"
                  :reload false}))

^{:refer code.java.compile/javac :added "3.0"}
(fact "compiles classes using the built-in compiler"

  (javac '[test]
         {:output "target/classes"
          :reload false})
  ;;=> outputs `.class` files in target directory
  )
