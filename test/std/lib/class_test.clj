(ns std.lib.class-test
  (:use code.test)
  (:require [std.lib.class :refer :all]))

^{:refer std.lib.class/class:array? :added "3.0"}
(fact "checks if a class is an array class"

  (class:array? (type (int-array 0)))
  => true)

^{:refer std.lib.class/primitive? :added "3.0"}
(fact "checks if a class of primitive type"

  (primitive? Integer/TYPE) => true
  (primitive? Integer) => false)

^{:refer std.lib.class/primitive:array? :added "3.0"}
(fact "checks if class is a primitive array"

  (primitive:array? (type (int-array 0)))
  => true

  (primitive:array? (type (into-array [1 2 3])))
  => false)

^{:refer std.lib.class/class:array-component :added "3.0"}
(fact "returns the array element within the array"

  (class:array-component (type (int-array 0)))
  => Integer/TYPE

  (class:array-component (type (into-array [1 2 3])))
  => java.lang.Long)

^{:refer std.lib.class/class:interface? :added "3.0"}
(fact "returns `true` if `class` is an interface"

  (class:interface? java.util.Map) => true
  
  (class:interface? Class) => false)

^{:refer std.lib.class/class:abstract? :added "3.0"}
(fact "returns `true` if `class` is an abstract class"

  (class:abstract? java.util.Map) => true

  (class:abstract? Class) => false)

^{:refer std.lib.class/create-lookup :added "3.0"}
(fact "creates a path lookup given a record"

  (create-lookup {:byte {:name "byte" :size 1}
                  :long {:name "long" :size 4}})
  => {"byte" [:byte :name]
      1 [:byte :size]
      "long" [:long :name]
      4 [:long :size]})

^{:refer std.lib.class/primitive :added "3.0"}
(fact "Converts primitive values across their different representations. The choices are:
   :raw       - The string in the jdk (i.e. `Z` for Boolean, `C` for Character)
   :symbol    - The symbol that std.object.query uses for matching (i.e. boolean, char, int)
   :string    - The string that std.object.query uses for matching
   :class     - The primitive class representation of the primitive
   :container - The containing class representation for the primitive type" ^:hidden

  (primitive Boolean/TYPE :symbol)
  => 'boolean

  (primitive "Z" :symbol)
  => 'boolean

  (primitive "int" :symbol)
  => 'int

  (primitive Character :string)
  => "char"

  (primitive "V" :class)
  => Void/TYPE

  (primitive 'long :container)
  => Long

  (primitive 'long :type)
  => :long)

^{:refer std.lib.class/ancestor:list :added "3.0"}
(fact "Lists the direct ancestors of a class"
  (ancestor:list clojure.lang.PersistentHashMap)
  => [clojure.lang.PersistentHashMap
      clojure.lang.APersistentMap
      clojure.lang.AFn
      java.lang.Object])

^{:refer std.lib.class/class:interfaces :added "3.0"}
(fact "Lists all interfaces for a class"

  (class:interfaces clojure.lang.IFn)
  => #{java.lang.Runnable java.util.concurrent.Callable}
  
  (class:interfaces clojure.lang.AFn)
  => #{java.lang.Runnable
       java.util.concurrent.Callable
       clojure.lang.IFn})

^{:refer std.lib.class/ancestor:tree :added "3.0"}
(fact "Lists the hierarchy of bases and interfaces of a class."
  (ancestor:tree Class)
  => [[java.lang.Object
       #{java.lang.constant.Constable
         java.lang.invoke.TypeDescriptor
         java.lang.reflect.GenericDeclaration
         java.io.Serializable
         java.lang.reflect.Type
         java.lang.reflect.AnnotatedElement
         java.lang.invoke.TypeDescriptor$OfField}]]
  
  ^:hidden
  (ancestor:tree clojure.lang.PersistentHashMap)
  => [[clojure.lang.APersistentMap #{clojure.lang.IMapIterable
                                     clojure.lang.IMeta
                                     clojure.lang.IKVReduce
                                     clojure.lang.IObj
                                     clojure.lang.IEditableCollection}]
      [clojure.lang.AFn #{java.lang.Iterable
                          clojure.lang.ILookup
                          clojure.lang.MapEquivalence
                          java.io.Serializable
                          clojure.lang.IPersistentMap
                          clojure.lang.IPersistentCollection
                          java.util.Map
                          clojure.lang.Counted
                          clojure.lang.Associative
                          clojure.lang.IHashEq
                          clojure.lang.Seqable}]
      [java.lang.Object #{java.lang.Runnable
                          java.util.concurrent.Callable
                          clojure.lang.IFn}]])

^{:refer std.lib.class/ancestor:all :added "3.0"}
(fact "returns all ancestors for a given type, itself included"

  (ancestor:all String)
  => #{java.lang.CharSequence
       java.lang.constant.ConstantDesc
       java.lang.constant.Constable
       java.lang.Comparable
       java.lang.String
       java.io.Serializable
       java.lang.Object})

^{:refer std.lib.class/class:inherits? :added "3.0"}
(fact "checks if one class inherits from another"

  (class:inherits? clojure.lang.ILookup clojure.lang.APersistentMap)
  => true)

^{:refer std.lib.class/class:match :added "3.0"}
(fact "finds the best matching interface or class from a list of candidates"

  (class:match #{Object} Long) => Object
  (class:match #{String} Long) => nil
  (class:match #{Object Number} Long) => Number)
