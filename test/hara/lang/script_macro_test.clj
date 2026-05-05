(ns hara.lang.script-macro-test
  (:require [clojure.string]
             [hara.model.spec-postgres :as pg]
             [hara.lang.book :as book]
             [hara.lang.book-module :as module]
             [hara.lang.impl :as impl]
             [hara.lang.library :as lib]
             [hara.lang.library-snapshot-prep-test :as prep]
             [hara.lang.pointer :as ptr]
             [hara.lang.runtime :as rt]
             [hara.lang.script-macro :as macro]
             [hara.model.spec-js :as js]
             [hara.model.spec-lua :as lua]
             [hara.model.spec-xtalk :as xtalk]
             [std.lib.collection :as collection])
  (:use code.test))

(def +library+ (lib/library {:snapshot prep/+snap+}))

(rt/install-lang! :lua)
(rt/install-lang! :js)
(rt/install-lang! :xtalk)
(rt/install-lang! :postgres)

^{:refer hara.lang.script-macro/body-arglists :added "4.0"}
(fact "makes arglists from body"

  (macro/body-arglists '([a b c]))
  => '([a b c])

  (macro/body-arglists '(([a b c := 5])))
  => '([a b c]))

^{:refer hara.lang.script-macro/intern-in :added "4.0"}
(fact "interns a macro"

  (do (macro/intern-in 'toy '.hello 1)
      (eval '@(var toy.hello)))
  => 1)

^{:refer hara.lang.script-macro/intern-prep :added "4.0"}
(fact "outputs the module and form meta"

  (macro/intern-prep :lua
                     (with-meta
                       '(def$.lua hello hello)
                       {:module 'L.core
                        :line 1}))
  => '[L.core {:module L.core, :line 1}])

^{:refer hara.lang.script-macro/intern-def$-fn :added "4.0"}
(fact "interns a fragment macro"

  (impl/with:library [+library+]
    (macro/intern-def$-fn :lua
                          (with-meta
                            '(def$.lua hello hello)
                            {:module 'L.core})
                          {:time 1
                           :line 1}))
  => #'hara.lang.script-macro-test/hello

  (impl/with:library [+library+]
    (collection/filter-vals identity @hello))
  => (contains {:section :fragment,
                :time 1,
                :op 'def$,
                :module 'L.core,
                :lang :lua,
                :line 1,
                :id 'hello,
                :display :default,
                :form 'hello,
                :namespace 'hara.lang.script-macro-test})

  (impl/with:library [+library+]
    (ptr/ptr-display hello {}))
  => "hello"

  (ptr/ptr-invoke-string (assoc hello :library +library+)
                         [1 2 3]
                         {})
  => "hello(1,2,3)")

^{:refer hara.lang.script-macro/intern-def$ :added "4.0"}
(fact "intern a def fragment macro"

  (macro/intern-def$ :x "hello")
  => #'hara.lang.script-macro-test/def$.hello

  (impl/with:library [+library+]
    ^{:module x.core}
    (def$.hello x x))

  (impl/with:library [+library+]
    (collection/filter-vals identity (ptr/ptr-deref x)))
  => (contains {:section :fragment,
                :time number?
                :op 'def$,
                :module 'x.core,
                :lang :x, :line number?
                :id 'x,
                :display :default,
                :form 'x,
                :namespace 'hara.lang.script-macro-test})

  (impl/with:library [+library+]
    (ptr/ptr-display x {}))
  => "x"

  (into {} x)
  => (contains {:lang :x, :id 'x, :module 'x.core, :section :fragment})

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

(fact "xtalk fragments display their stored form"

  (let [xlib (lib/library {})]
    (lib/add-book! xlib (assoc xtalk/+book+ :modules {}))
    (lib/add-module! xlib (module/book-module {:lang :xtalk
                                               :id 'xt.lang.common-math}))
    (impl/with:library [xlib]
      (let [frag-var (macro/intern-def$-fn
                      :xtalk
                      (with-meta
                        '(def$.xt sin x:m-sin)
                        {:module 'xt.lang.common-math})
                      {})]
        [(ptr/ptr-display @frag-var {})
         (string? (pr-str @frag-var))])))
  => '["x:m-sin" true])

^{:refer hara.lang.script-macro/intern-defmacro-fn :added "4.0"}
(fact "function to intern a macro"

  (impl/with:library [+library+]
    (macro/intern-defmacro-fn
     :lua
     (with-meta
       '(defmacro.lua make-array-0 [& args]
          (vec args))
       '{:module L.core})
     {}))
  => #'hara.lang.script-macro-test/make-array-0

  (impl/with:library [+library+]
    @make-array-0)
  => book/book-entry?

  (impl/with:library [+library+]
    (ptr/ptr-invoke-string make-array-0 [1 2 3] {}))
  => "[1,2,3]")

(fact "function to intern a macro supports multi-arity clauses"

  (let [xlib (lib/library {})]
    (lib/add-book! xlib (assoc xtalk/+book+ :modules {}))
    (lib/add-module! xlib (module/book-module {:lang :xtalk
                                               :id 'xt.lang.common-lib}))
    (impl/with:library [xlib]
      (let [macro-var (macro/intern-defmacro-fn
                       :xtalk
                       (with-meta
                         '(defmacro.xt get-idx
                            "gets array indices"
                            {:standalone true}
                            ([arr idx]
                             (list 'x:get-idx arr idx))
                            ([arr idx default]
                             (list 'x:get-idx arr idx default)))
                         '{:module xt.lang.common-lib})
                       {})
            entry @@macro-var]
        [(-> macro-var meta :arglists)
         ((:template entry) 'arr 'idx)
         ((:template entry) 'arr 'idx 'fallback)])))
  => '[([arr idx]
         [arr idx default])
        (x:get-idx arr idx)
        (x:get-idx arr idx fallback)])

(fact "top level function and macro pointers can be printed"

  (impl/with:library [+library+]
    (let [macro-var (macro/intern-defmacro-fn
                     :lua
                     (with-meta
                       '(defmacro.lua make-array-printable [& args]
                          (vec args))
                       '{:module L.core})
                     {})
          fn-var    (macro/intern-top-level-fn
                     :lua
                     ['defn (get-in (lib/get-book +library+ :lua)
                                    [:grammar :reserved 'defn])]
                     (with-meta
                       '(defn.lua always-false
                          [x]
                          (return false))
                       {:module 'L.core})
                     {})]
      (every? string?
              [(pr-str @macro-var)
               (pr-str @fn-var)])))
  => true)

(fact "xtalk top level forms print and hydrate without forcing abstract emit"

  (let [xlib (lib/library {})]
    (lib/add-book! xlib (assoc xtalk/+book+ :modules {}))
    (lib/add-module! xlib (module/book-module {:lang :xtalk
                                               :id 'xt.lang.common-lib}))
    (impl/with:library [xlib]
      (let [book      (lib/get-book xlib :xtalk)
            reserved  ['defn (get-in book [:grammar :reserved 'defn])]
            macro-var (macro/intern-defmacro-fn
                       :xtalk
                       (with-meta
                         '(defmacro.xt make-type-native-printable [x]
                            (list 'x:type-native x))
                         '{:module xt.lang.common-lib})
                       {})
            fn-var    (macro/intern-top-level-fn
                       :xtalk
                       reserved
                       (with-meta
                         '(defn.xt type-native-printable
                            "gets the native type"
                            {:added "4.1"}
                            [obj]
                            (return (x:type-native obj)))
                         {:module 'xt.lang.common-lib})
                       {})
            if-var    (macro/intern-top-level-fn
                       :xtalk
                       reserved
                       (with-meta
                          '(defn.xt type-class-printable
                             "gets the type of an object"
                             {:added "4.1"}
                             [x]
                             (var ntype (-/type-native-printable x))
                             (if (== ntype "object")
                               (return (x:get-key x "::" ntype))
                               (return ntype)))
                          {:module 'xt.lang.common-lib})
                        {})]
        (every? true?
                  [(string? (pr-str @macro-var))
                   (string? (pr-str @fn-var))
                   (string? (pr-str @if-var))]))))
  => true)

(fact "postgres top level functions initialize against a runtime context"

  (let [plib (lib/library {:snapshot prep/+snap+})]
    (lib/add-book! plib (assoc pg/+book+ :modules {}))
    (lib/add-module! plib (module/book-module {:lang :postgres
                                               :id 'rt.postgres.test.scratch-v1}))
    (impl/with:library [plib]
      (let [book     (lib/get-book plib :postgres)
            reserved ['defn (get-in book [:grammar :reserved 'defn])]
            fn-var   (macro/intern-top-level-fn
                      :postgres
                      reserved
                      (with-meta
                        '(defn.pg as-array
                           "returns a jsonb array"
                           {:added "4.0"}
                           [:jsonb input]
                           (when (== input "{}")
                             (return "[]"))
                           (return input))
                        {:module 'rt.postgres.test.scratch-v1})
                      {})]
        (string? (pr-str @fn-var)))))
  => true)

^{:refer hara.lang.script-macro/intern-defmacro :added "4.0"}
(fact "the intern macro function"

  (macro/intern-defmacro :x "hello")
  => #'hara.lang.script-macro-test/defmacro.hello

  (impl/with:library [+library+]
    ^{:module x.core}
    (defmacro.hello
      divide [x y]
      (list '/ x y)))
  => #'hara.lang.script-macro-test/divide

  (impl/with:library [+library+]
    (ptr/ptr-deref divide))
  => book/book-entry?

  (impl/with:library [+library+]
    (ptr/ptr-invoke-string divide [1 2] {}))
  => "1 / 2")

^{:refer hara.lang.script-macro/call-thunk :added "4.0"}
(fact "calls the thunk given meta to control pointer output"

  (macro/call-thunk {:debug true}
                    (fn [] ptr/*print*))
  => #{:input})

^{:refer hara.lang.script-macro/intern-!-fn :added "4.0"}
(fact "interns a free pointer macro"

  (-> (macro/intern-!-fn :lua [1 2 3] {})
      (clojure.string/replace ";" ""))
  => "1\n2\n3"

  (macro/intern-!-fn :js [1 2 3] {})
  => "1;\n2;\n3;"

  (with-redefs [std.lib.context.pointer/rt-invoke-ptr (fn [_ ptr args]
                                                        [(select-keys ptr [:lang :form])
                                                         args])]
    (macro/intern-!-fn :lua [1 2 3] {}))
  => [{:lang :lua
       :form '(do 1 2 3)}
      []]

  (with-redefs [std.lib.context.pointer/rt-invoke-ptr (fn [_ ptr args]
                                                        [(select-keys ptr [:lang :form])
                                                         args])]
    (macro/intern-!-fn :lua ['(+ 1 2)] {}))
  => [{:lang :lua
       :form '(+ 1 2)}
      []])

^{:refer hara.lang.script-macro/intern-! :added "4.0"}
(fact "interns a macro for free evalutation"

  (macro/intern-! :lua "hello")
  => #'hara.lang.script-macro-test/!.hello

  (-> (!.hello 1 2 3 4 5)
      (clojure.string/replace ";" ""))
  => "1\n2\n3\n4\n5")

^{:refer hara.lang.script-macro/intern-free-fn :added "4.0"}
(fact "interns a free pointer in the namespace"

  (macro/intern-free-fn :lua '(defptr.lua hello 1)
                        {})
  => #'hara.lang.script-macro-test/hello)

^{:refer hara.lang.script-macro/intern-free :added "4.0"}
(fact "creates a defptr macro"

  (macro/intern-free :lua "hello")
  => #'hara.lang.script-macro-test/defptr.hello)

^{:refer hara.lang.script-macro/intern-top-level-fn :added "4.0"}
(fact "interns a top level function"

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
  => #'hara.lang.script-macro-test/add-more

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

^{:refer hara.lang.script-macro/intern-top-level :added "4.0"}
(fact "interns a top level macro"

  (impl/with:library [+library+]
    (macro/intern-top-level :lua "hello" 'def))
  => #'hara.lang.script-macro-test/def.hello

  (impl/with:library [+library+]
    ^{:module L.core}
    (def.hello abc 1))
  => #'hara.lang.script-macro-test/abc

  (impl/with:library [+library+]
    (ptr/ptr-deref abc))
  => book/book-entry?

  (impl/with:library [+library+]
    (ptr/ptr-display abc {}))
  => "def abc = 1;")

^{:refer hara.lang.script-macro/intern-macros :added "4.0"}
(fact "interns the top-level macros in the grammar"
  (macro/intern-macros :lua (:grammar (lib/get-book +library+ :lua)))
  => vector?)

^{:refer hara.lang.script-macro/intern-highlights :added "4.0"}
(fact "interns the highlight macros in the grammar"
  (macro/intern-highlights :lua (:grammar (lib/get-book +library+ :lua)))
  => vector?)

^{:refer hara.lang.script-macro/intern-grammar :added "4.0"}
(fact "interns a bunch of macros in the namespace"

  (:macros (:grammar (lib/get-book +library+ :lua)))
  => '#{defrun defn defglobal defgen defn- deftemp defclass defabstract def}

  (impl/with:library [+library+]
    (macro/intern-grammar :lua (:grammar (lib/get-book +library+ :lua))))
  => map?)

^{:refer hara.lang.script-macro/intern-defmacro-rt-fn :added "4.0"}
(fact "defines both a library entry as well as a runtime macro"
  (impl/with:library [+library+]
    (macro/intern-defmacro-rt-fn
     :lua
     (with-meta
       '(defmacro.lua make-array-0 [& args]
          (vec args))
       '{:module L.core})
     {}))
  => vector?)

^{:refer hara.lang.script-macro/defmacro.! :added "4.0"}
(fact "macro for runtime lang macros")
