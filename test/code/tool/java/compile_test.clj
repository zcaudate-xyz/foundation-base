(ns code.tool.java.compile-test
  (:use code.test)
  (:require [code.tool.java.compile :refer :all]
            [code.project :as project]
            [std.fs :as fs])
  (:refer-clojure :exclude [supers]))

^{:refer code.tool.java.compile/path->class :added "3.0"}
(fact "Creates a class symbol from a file"

  (path->class "test/Dog.java")
  => 'test.Dog

  (path->class "test/Cat.class")
  => 'test.Cat)

^{:refer code.tool.java.compile/class->path :added "3.0"}
(fact "creates a file path from a class"

  (class->path 'test.Dog)
  => "test/Dog.class"

  (class->path 'test.Cat)
  => "test/Cat.class")

^{:refer code.tool.java.compile/java-sources :added "3.0"}
(fact "lists source classes in a project"

  (->> (java-sources (project/project))
       (keys)
       (filter (comp #(.startsWith ^String % "test") str))
       (sort))
  => seq?)

^{:refer code.tool.java.compile/class-object :added "3.0"}
(fact "creates a class object for use with compiler"
  (class-object "test.Cat"
                (byte-array 0))
  => javax.tools.SimpleJavaFileObject)

^{:refer code.tool.java.compile/class-manager :added "3.0"}
(fact "creates a `ForwardingJavaFileManager`"

  (let [compiler  (javax.tools.ToolProvider/getSystemJavaCompiler)
        collector (javax.tools.DiagnosticCollector.)
        manager   (.getStandardFileManager compiler collector nil nil)
        cache     (atom {})]
    (class-manager manager cache))
  => javax.tools.ForwardingJavaFileManager)

^{:refer code.tool.java.compile/supers :added "3.0"}
(fact "finds supers of a class given it's bytecode"
  (supers (fs/read-all-bytes "target/classes/code/java/compile.class"))
  => (contains "java.lang.Object"))

^{:refer code.tool.java.compile/javac-output :added "3.0"}
(fact "displays output of compilation"
  (javac-output (javax.tools.DiagnosticCollector.))
  => nil)

^{:refer code.tool.java.compile/javac-process :added "3.0"}
(fact "processes Java compilation, handling class reloading and output options"
  (with-redefs [std.fs/create-directory (constantly nil)
                std.fs/write-all-bytes (constantly nil)]
    (javac-process {"test.Pet" (byte-array 0)}
                   {:output "target/classes"
                    :reload false}))
  => (contains {:output {"test.Pet" nil}}))

^{:refer code.tool.java.compile/javac :added "3.0"}
(fact "compiles classes using the built-in compiler"

  (javac '[test]
         {:output "target/classes"
          :reload false})
  ;;=> outputs `.class` files in target directory
  )
