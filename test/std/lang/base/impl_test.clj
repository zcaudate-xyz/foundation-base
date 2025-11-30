(ns std.lang.base.impl-test
  (:use code.test)
  (:require [std.lang.base.impl :refer :all]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.impl-deps :as deps]
            [std.lang.base.impl-lifecycle :as lifecycle]
            [std.lang.base.book :as book]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.string :as str]
            [std.lib :as h]))

(def +library+
  (lib/library {:snapshot (snap/snapshot {:lua {:id :lua
                                                :book prep/+book-min+}})}))

(def +library-ext+
  (doto (lib/library:create
         {:snapshot (snap/snapshot {:lua {:id :lua
                                          :book prep/+book-min+}})})
    (lib/install-module! :lua 'L.util
                         {:require '[[L.core :as u]]
                          :import '[["cjson" :as cjson]]})
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn sub-fn
         [a b]
         (return ((u/identity-fn u/sub) a b)))
      {:lang :lua
       :module 'L.util}
      {}))
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn add-fn
         [a b]
         (return ((u/identity-fn u/add) a (-/sub-fn b 0))))
      {:lang :lua
       :module 'L.util}
      {}))))

^{:refer std.lang.base.impl-lifecycle/emit-module-prep :adopt true :added "4.0"}
(fact "prepare data for emit of module"

  (-> (lifecycle/emit-module-prep
       'L.util
       {:lang :lua
        :library +library-ext+})
      second
      (dissoc :code))
  => '{:setup nil, :teardown nil, :native {}, :direct #{L.core}})

^{:refer std.lang.base.impl-lifecycle/emit-module :adopt true :added "4.0"}
(fact "emits the module string"

  (lifecycle/emit-module-setup
   'L.util
   {:lang :lua
    :library +library-ext+
    :layout :module
    :emit {:export {:suppress true}
           :link   {:suppress true}}})
  => string?)

^{:refer std.lang.base.impl/with:library :added "4.0"}
(fact "injects a library as the default"
  (with:library [+library+]
    *library*)
  => +library+)

^{:refer std.lang.base.impl/default-library :added "4.0"}
(fact "gets the default library"
  ^:hidden
  
  (default-library)
  => lib/library?)

^{:refer std.lang.base.impl/default-library:reset :added "4.0"}
(fact "clears the default library, including all grammars"
  (with-default-library
    (default-library:reset))
  => anything)

^{:refer std.lang.base.impl/runtime-library :added "4.0"}
(fact "gets the current runtime (annex or default)"

  (runtime-library)
  => lib/library?)

^{:refer std.lang.base.impl/grammar :added "4.0"}
(fact "gets the grammar"
  ^:hidden
  
  (with:library [+library+]
    (grammar :lua))
  => map?)

^{:refer std.lang.base.impl/emit-options :added "4.0"}
(fact "create emit options"

  (emit-options {:lang :lua})
  => vector?)

^{:refer std.lang.base.impl/to-form :added "4.0"}
(fact "input to form"
  ^:hidden
  
  (to-form '(+ @1 2 3)
           {:- :raw
            :lang :xtalk})
  => '(+ (clojure.core/deref 1) 2 3)
  
  (to-form '(+ @1 2 3)
           {:lang :lua})
  => '(+ (!:eval 1) 2 3))

^{:refer std.lang.base.impl/%.form :added "4.0"}
(fact "converts to a form"
  ^:hidden
  
  ^{:- :raw}
  (%.form (+ @1 2 3))
  => '(+ (clojure.core/deref 1) 2 3)
  
  (%.form (+ @1 2 3))
  => '(+ (!:eval 1) 2 3))

^{:refer std.lang.base.impl/emit-direct :added "4.0"}
(fact "adds additional controls to transform form"
  ^:hidden
  
  (emit-direct (:grammar prep/+book-min+)
               '(:% (:- "   ") (+ 1 2 3)  (:- "   "))
                *ns*
                {:emit {:trim str/trim}
                 :lang :lua})
  => "1 + 2 + 3")

^{:refer std.lang.base.impl/emit-str :added "4.0"}
(fact  "converts to an output string"
  ^:hidden
  
  (emit-str '(+ 1 2 3)
            {:- :raw
             :lang :lua})
  => "1 + 2 + 3"

  (emit-str '(+ @1 2 3)
            {:lang :lua})
  => "1 + 2 + 3")

^{:refer std.lang.base.impl/%.str :added "4.0"}
(fact "converts to an output string"
  ^:hidden
  
  ^{:- :raw}
  (%.str (+ @1 2 3))
  => (throws)
  
  (%.str (+ @1 2 3))
  => "1 + 2 + 3"

  (%.str (+ (@! 1) 2 3))
  => "1 + 2 + 3"
  
  ^{:lang :js}
  (%.str
   (defn hello
     [:int x :int y := 0]
     (\\
      \\ (if (not= i y)
           (if (not= 1 2)
             (return 1))))
     
     (+ 6 x y)
     (return (or (not (+ 1 2))
                 (not (+ 1 2))))))
  => (std.string/|
      "function hello(int x,int y = 0){"
      "  "
      "  if(i != y){"
      "    if(1 != 2){"
      "      return 1;"
      "    }"
      "  }"
      "  6 + x + y;"
      "  return !(1 + 2) || !(1 + 2);"
      "}")

  ^{:lang :js}
  (%.str
   ^{:indent 5 :full-body true}
   (\\ (do (+ 1 2 3)
           (+ 1 2 3))))
  => (std.string/| "     1 + 2 + 3;"
                   "     1 + 2 + 3;")

  ^{:lang :js}
  (%.str
   ^{:indent 5}
   (\\
    \\  (do (+ 1 2 3)
            (+ 1 2 3))))
  => (std.string/| ""
                   "     1 + 2 + 3;"
                   "     1 + 2 + 3;")

  ^{:lang :js}
  (%.str
   (:# (do (+ 1 2 3)
           (+ 1 2 3))))
  => (std.string/| "// 1 + 2 + 3;"
                   "// 1 + 2 + 3;")  

  ^{:lang :js}
  (%.str
   ^{:prefix "|   "}
   (:# (do (+ 1 2 3)
           (+ 1 2 3))))
  => (std.string/|
      "|    1 + 2 + 3;"
      "|    1 + 2 + 3;"))

^{:refer std.lang.base.impl/emit-as :added "4.0"}
(fact "helper function for emitting multiple forms"
  ^:hidden
  
  (emit-as :lua '[(+ 1 2 3)
                  (+ 4 5 6)]
           {:library +library+})
  => "1 + 2 + 3\n\n4 + 5 + 6")

^{:refer std.lang.base.impl/emit-symbol :added "4.0"}
(fact "emits string given symbol and grammar"
  ^:hidden

  (with:library [+library+]
    (emit-symbol :lua 'L.core/identity-fn
                 {:module (lib/get-module +library+
                                          :lua 'L.core)}))
  => "identity_fn"

  (with:library [+library+]
    (emit-symbol :lua 'L.core/identity-fn
                 {:layout :full
                  :module (lib/get-module +library+
                                          :lua 'L.core)}))
  => "L_core____identity_fn"
  
  (with:library [+library+]
    (emit-symbol :lua 'L.core/WRONG
                 {:library +library+}))
  => (throws))

^{:refer std.lang.base.impl/get-entry :added "4.0"}
(fact "gets an entry"
  ^:hidden
  
  (get-entry +library+
             :lua
             'L.core
             'identity-fn)
  => book/book-entry?)

^{:refer std.lang.base.impl/emit-entry :added "4.0"}
(fact "emits an entry given parameters and options"
  ^:hidden
  
  (emit-entry '{:lang :lua
                :module L.core
                :id identity-fn}
              {:lang :lua
               :library +library+})
  => "function identity_fn(x){\n  return x;\n}"

  (emit-entry '{:lang :lua
                :module L.core
                :id identity-fn}
              {:lang :lua
               :library +library+
               :layout :full})
  => "function L_core____identity_fn(x){\n  return x;\n}")

^{:refer std.lang.base.impl/emit-entry-deps-collect :added "4.0"}
(fact "only collects the dependencies"
  (emit-entry-deps-collect '{:lang :lua
                              :module L.core
                              :id identity-fn}
                            {:lang :lua
                             :library +library+})
  => (fn [x] (and (seq x) (book/book-entry? (first x)))))

^{:refer std.lang.base.impl/emit-entry-deps :added "4.0"}
(fact "emits only the entry deps"
  ^:hidden
  
  (emit-entry-deps '{:lang :lua
                     :module L.util
                     :id add-fn}
                   {:lang :lua
                    :library +library-ext+
                    :layout :full})
  => (std.string/|
   "function L_core____identity_fn(x){"
   "  return x;"
   "}"
   ""
   "function L_util____add_fn(a,b){"
   "  return L_core____identity_fn(function (x,y){"
   "    return x + y;"
   "  })(a,L_util____sub_fn(b,0));"
   "}"
   ""
   "function L_util____sub_fn(a,b){"
   "  return L_core____identity_fn(function (x,y){"
   "    return x - y;"
   "  })(a,b);"
   "}"))

^{:refer std.lang.base.impl/emit-script-imports :added "4.0"}
(fact "emit imports"
  ^:hidden
  
  (emit-script-imports
   '{"cjson" {:as cjson}}
   nil
   (emit-options
    {:lang :lua
     :library +library-ext+
     :module (lib/get-module +library-ext+ :lua 'L.util)
     :layout :flat}))
  => '("var__(local=cjson,==require(\"cjson\"));"))

^{:refer std.lang.base.impl/emit-script-deps :added "4.0"}
(fact "emits the script deps"
  ^:hidden
  
  (let [[stage grammar book namespace mopts] (emit-options
                                              {:lang :lua
                                               :library +library-ext+
                                               :module (lib/get-module +library-ext+ :lua 'L.util)
                                               :layout :flat})
        [deps _]  (deps/collect-script-entries book `[L.util/add-fn])]
    (emit-script-deps deps nil [stage grammar book namespace mopts]))
  => coll?)

^{:refer std.lang.base.impl/emit-script-join :added "4.0"}
(fact "joins the necessary parts of the script"
  (emit-script-join ["import"] ["deps"] "body")
  => "import\n\ndeps\n\nbody")

^{:refer std.lang.base.impl/emit-script :added "4.0"}
(fact "emits a script with all dependencies"
  ^:hidden
  
  (emit-script '(-/add-fn 1 2)
               {:lang :lua
                :library +library-ext+
                :module (lib/get-module +library-ext+ :lua 'L.util)
                :layout :flat})
  => (std.string/|
      "function identity_fn(x){"
      "  return x;"
      "}"
      ""
      "function add_fn(a,b){"
      "  return identity_fn(function (x,y){"
      "    return x + y;"
      "  })(a,sub_fn(b,0));"
      "}"
      ""
      "function sub_fn(a,b){"
      "  return identity_fn(function (x,y){"
      "    return x - y;"
      "  })(a,b);"
      "}"
      ""
      "add_fn(1,2)")
  
  
  (emit-script '(-/add-fn 1 2)
               {:lang :lua
                :library +library-ext+
                :module (lib/get-module +library-ext+ :lua 'L.util)
                :layout :full})
  => (std.string/|
      "function L_core____identity_fn(x){"
      "  return x;"
      "}"
      ""
      "function L_util____add_fn(a,b){"
      "  return L_core____identity_fn(function (x,y){"
      "    return x + y;"
      "  })(a,L_util____sub_fn(b,0));"
      "}"
      ""
      "function L_util____sub_fn(a,b){"
      "  return L_core____identity_fn(function (x,y){"
      "    return x - y;"
      "  })(a,b);"
      "}"
      ""
      "L_util____add_fn(1,2)"))

^{:refer std.lang.base.impl/emit-scaffold-raw-imports :added "4.0"}
(fact "gets only the scaffold imports"
  (emit-scaffold-raw-imports [] nil (emit-options {:lang :lua :library +library-ext+}))
  => vector?)

^{:refer std.lang.base.impl/emit-scaffold-raw :added "4.0"}
(fact "creates entries only for defglobal and defrun entries"
  (emit-scaffold-raw identity 'L.core {:lang :lua :library +library-ext+})
  => string?)

^{:refer std.lang.base.impl/emit-scaffold-for :added "4.0"}
(fact "creates scaffold for module and its deps"
  (emit-scaffold-for 'L.core {:lang :lua :library +library-ext+})
  => string?)

^{:refer std.lang.base.impl/emit-scaffold-to :added "4.0"}
(fact "creates scaffold up to module"
  (emit-scaffold-to 'L.core {:lang :lua :library +library-ext+})
  => string?)

^{:refer std.lang.base.impl/emit-scaffold-imports :added "4.0"}
(fact "create scaggold to expose native imports "
  (emit-scaffold-imports 'L.core {:lang :lua :library +library-ext+})
  => string?)
