(ns std.lang.base.script-macro-test
  (:use code.test)
  (:require [std.lang.base.script-macro :as macro]
            [std.lang.base.impl :as impl]
            [std.lang.base.book :as book]
            [std.lang.base.library :as lib]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.library-snapshot-prep-test :as prep]
            [std.lang.model.spec-js :as js]
            [std.lang.model.spec-lua :as lua]
            [std.lib :as h]))

(def +library+ (lib/library {:snapshot prep/+snap+}))

^{:refer std.lang.base.script-macro/body-arglists :added "4.0"}
(fact "makes arglists from body"
  ^:hidden
  
  (macro/body-arglists '([a b c]))
  => '([a b c])

  (macro/body-arglists '(([a b c := 5])))
  => '([a b c]))

^{:refer std.lang.base.script-macro/intern-in :added "4.0"}
(fact "interns a macro"
  ^:hidden
  
  (do (macro/intern-in 'toy '.hello 1)
      (eval '@(var toy.hello)))
  => 1)

^{:refer std.lang.base.script-macro/intern-prep :added "4.0"}
(fact "outputs the module and form meta"
  ^:hidden
  
  (macro/intern-prep :lua
                     (with-meta
                       '(def$.lua hello hello)
                       {:module 'L.core
                        :line 1}))
  => '[L.core {:module L.core, :line 1}])

^{:refer std.lang.base.script-macro/intern-def$-fn :added "4.0"}
(fact "interns a fragment macro"
  ^:hidden
  
  (impl/with:library [+library+]
    (macro/intern-def$-fn :lua
                          (with-meta
                            '(def$.lua hello hello)
                            {:module 'L.core})
                          {:time 1
                           :line 1}))
  => #'std.lang.base.script-macro-test/hello

  (impl/with:library [+library+]
    (h/filter-vals identity @hello))
  => '{:section :fragment,
       :time 1,
       :deps-native {},
       :op def$,
       :module L.core,
       :lang :lua,
       :line 1,
       :deps-fragment #{},
       :id hello,
       :display :default,
       :form hello,
       :namespace std.lang.base.script-macro-test,
       :deps #{}}
  
  (impl/with:library [+library+]
    (ptr/ptr-display hello {}))
  => "hello"

  (ptr/ptr-invoke-string (assoc hello :library +library+)
                         [1 2 3]
                         {})
  => "hello(1,2,3)")

^{:refer std.lang.base.script-macro/intern-def$ :added "4.0"}
(fact "intern a def fragment macro"
  ^:hidden
  
  (macro/intern-def$ :x "hello")
  => #'std.lang.base.script-macro-test/def$.hello

  (impl/with:library [+library+]
    ^{:module x.core}
    (def$.hello x x))

  (impl/with:library [+library+]
    (h/filter-vals identity (ptr/ptr-deref x)))
  => (contains {:section :fragment,
                :time number?
                :op 'def$,
                :module 'x.core,
                :lang :x, :line number?
                :id 'x,
                :display :default,
                :form 'x,
                :namespace 'std.lang.base.script-macro-test})
  
  (impl/with:library [+library+]
    (ptr/ptr-display x {}))
  => "x"

  (into {} x)
  => (contains {:context :lang/x, :lang :x, :id 'x, :module 'x.core, :section :fragment})

  (lib/get-entry +library+ x)
  => book/book-entry?


  (impl/with:library [+library+]
    ^{:module x.core}
    (def$.hello ^{:arglists '([a b])
                  :rt/kernel "hello"}
      multiply multiply))

  (impl/with:library [+library+]
    (ptr/ptr-invoke-string multiply 
                           [1 2 3] {}))
  => "multiply(1,2,3)"
  
  (meta #'multiply)
  => (contains '{:rt/kernel "hello", :arglists ([a b])}))

^{:refer std.lang.base.script-macro/intern-defmacro-fn :added "4.0"}
(fact "function to intern a macro"
  ^:hidden
  
  (impl/with:library [+library+]
    (macro/intern-defmacro-fn
     :lua
     (with-meta
       '(defmacro.lua make-array-0 [& args]
          (vec args))
       '{:module L.core})
     {}))
  => #'std.lang.base.script-macro-test/make-array-0

  (impl/with:library [+library+]
    @make-array-0)
  => book/book-entry?

  (impl/with:library [+library+]
    (ptr/ptr-invoke-string make-array-0 [1 2 3] {}))
  => "[1,2,3]")

^{:refer std.lang.base.script-macro/intern-defmacro :added "4.0"}
(fact "the intern macro function"
  ^:hidden
  
  (macro/intern-defmacro :x "hello")
  => #'std.lang.base.script-macro-test/defmacro.hello

  (impl/with:library [+library+]
    ^{:module x.core}
    (defmacro.hello 
      divide [x y]
      (list '/ x y)))
  => #'std.lang.base.script-macro-test/divide

  (impl/with:library [+library+]
    (ptr/ptr-deref divide))
  => book/book-entry?

  (impl/with:library [+library+]
    (ptr/ptr-invoke-string divide [1 2] {}))
  => "1 / 2")

^{:refer std.lang.base.script-macro/call-thunk :added "4.0"}
(fact "calls the thunk given meta to control pointer output"
  
  (macro/call-thunk {:debug true}
                    (fn [] ptr/*print*))
  => #{:input})

^{:refer std.lang.base.script-macro/intern-!-fn :added "4.0"}
(fact "interns a free pointer macro"
  ^:hidden
  
  (macro/intern-!-fn :lua [1 2 3] {})
  => "1\n2\n3"

  (macro/intern-!-fn :js [1 2 3] {})
  => "1;\n2;\n3;")

^{:refer std.lang.base.script-macro/intern-! :added "4.0"}
(fact "interns a macro for free evalutation"
  ^:hidden
  
  (macro/intern-! :lua "hello")
  => #'std.lang.base.script-macro-test/!.hello

  (!.hello 1 2 3 4 5)
  => "1\n2\n3\n4\n5")

^{:refer std.lang.base.script-macro/intern-free-fn :added "4.0"}
(fact "interns a free pointer in the namespace"
  
  (macro/intern-free-fn :lua '(defptr.lua hello 1)
                        {})
  => #'std.lang.base.script-macro-test/hello)

^{:refer std.lang.base.script-macro/intern-free :added "4.0"}
(fact "creates a defptr macro"

  (macro/intern-free :lua "hello")
  => #'std.lang.base.script-macro-test/defptr.hello)

^{:refer std.lang.base.script-macro/intern-top-level-fn :added "4.0"}
(fact "interns a top level function"
  ^:hidden
  
  (impl/with:library [+library+]
    (macro/intern-top-level-fn
     :lua
     ['defn (get-in (lib/get-book +library+ :lua)
                    [:grammar :reserved 'defn])]
     (with-meta
       '(defn.lua ^{:b 2} add-more
          "hello"
          {:a 1}
          [x y z]
          (return (+ x y z)))
       {:module 'L.core})
     {}))
  => #'std.lang.base.script-macro-test/add-more

  (lib/get-entry +library+ '{:lang :lua
                             :module L.core
                             :id add-more})
  => book/book-entry?
  
  (meta #'add-more)
  => (contains {:a 1, :doc "hello"})
  
  (impl/with:library [+library+]
    (ptr/ptr-invoke-string add-more [1 2 3] {}))
  => "add_more(1,2,3)"

  (impl/with:library [+library+]
    (ptr/ptr-invoke-string add-more [1 2 3] {:layout :full}))
  => "L_core____add_more(1,2,3)")

^{:refer std.lang.base.script-macro/intern-top-level :added "4.0"}
(fact "interns a top level macro"

  (impl/with:library [+library+]
    (macro/intern-top-level :lua "hello" 'def))
  => #'std.lang.base.script-macro-test/def.hello

  (impl/with:library [+library+]
    ^{:module L.core}
    (def.hello abc 1))
  => #'std.lang.base.script-macro-test/abc
  
  (impl/with:library [+library+]
    (ptr/ptr-deref abc))

  (impl/with:library [+library+]
    (ptr/ptr-display abc {}))
  => "def abc = 1;")

^{:refer std.lang.base.script-macro/intern-macros :added "4.0"}
(fact "interns the top-level macros in the grammar")

^{:refer std.lang.base.script-macro/intern-highlights :added "4.0"}
(fact "interns the highlight macros in the grammar")

^{:refer std.lang.base.script-macro/intern-grammar :added "4.0"}
(fact "interns a bunch of macros in the namespace"

  (:macros (:grammar (lib/get-book +library+ :lua)))
  => '#{defrun defn defglobal defgen defn- deftemp defclass defabstract def}
  
  (impl/with:library [+library+]
    (macro/intern-grammar :lua (:grammar (lib/get-book +library+ :lua))))
  => map?)

^{:refer std.lang.base.script-macro/intern-defmacro-rt-fn :added "4.0"}
(fact "defines both a library entry as well as a runtime macro")

^{:refer std.lang.base.script-macro/defmacro.! :added "4.0"}
(fact "macro for runtime lang macros")
