(ns hara.lang.pointer-test
  (:require [std.json :as json]
            [hara.lang.book :as book]
            [hara.common.emit-prep-lua-test :as prep]
            [hara.lang.impl-entry :as entry]
            [hara.lang.library :as lib]
            [hara.lang.library-snapshot :as snap]
            [hara.lang.pointer :refer :all]
            [hara.common.util :as ut]
            [std.lib.foundation :as f]
            [std.string.prose :as prose])
  (:use code.test))

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
       :namespace 'L.util
       :module 'L.util}
      {}))
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn add-fn
         [a b]
         (return ((u/identity-fn u/add) a (-/sub-fn b 0))))
      {:lang :lua
       :namespace 'L.util
       :module 'L.util}
      {}))))

(def +ptr+
  (ut/lang-pointer :lua
                   {:module 'L.core
                    :id 'add
                    :section :fragment
                    :library +library-ext+}))

^{:refer hara.lang.pointer/with:clip :added "4.0"}
(fact "form to control `clip` option"
  (with:clip *clip*) => true)

^{:refer hara.lang.pointer/with:print :added "4.0"}
(fact "form to control `print` option"
  (with:print *print*) => #{:input})

^{:refer hara.lang.pointer/with:print-all :added "4.0"}
(fact "toggles print for all intermediate steps"
  (with:print-all *print*) => #{:input-form :raw-input :raw-output})

^{:refer hara.lang.pointer/with:rt-wrap :added "4.0"}
(fact "wraps an additional function to the invoke function"
  (with:rt-wrap [inc] *rt-wrap*) => inc)

^{:refer hara.lang.pointer/with:rt :added "4.0"}
(fact "forcibly applies a runtime"
  (with:rt [1] std.lib.context.pointer/*runtime*) => 1)

^{:refer hara.lang.pointer/with:input :added "4.0"}
(fact "form to control `input` option"
  (with:input *input*) => #{:default})

^{:refer hara.lang.pointer/with:raw :added "4.0"}
(fact "form to control `raw` option"
  (with:raw *output*) => #{:raw})

^{:refer hara.lang.pointer/get-entry :added "4.0"}
(fact "gets the library entry given pointer"

  (get-entry +ptr+)
  => book/book-entry?)

^{:refer hara.lang.pointer/ptr-tag :added "4.0"}
(fact "creates a tag for the pointer"

  (ptr-tag +ptr+ :lib)
  => '[:lib L.core/add :fragment])

^{:refer hara.lang.pointer/free-form :added "4.1"}
(fact "normalizes free-pointer bodies"
  (free-form [1 2 3]) => '(do 1 2 3)
  (free-form '[(+ 1 2)]) => '(+ 1 2))

^{:refer hara.lang.pointer/free-form-body :added "4.1"}
(fact "expands canonical free-pointer forms into body forms"
  (free-form-body '(do 1 2 3)) => '(1 2 3)
  (free-form-body '(+ 1 2)) => '((+ 1 2)))

^{:refer hara.lang.pointer/ptr-deref :added "4.0"}
(fact "gets the entry or the free pointer data"

  (ptr-deref +ptr+)
  => book/book-entry?

   (ptr-deref (dissoc +ptr+ :id))
   => map?)

^{:refer hara.lang.pointer/ptr-display :added "4.0"}
(fact "emits the display string for pointer"

  (ptr-display +ptr+ {})
  "(fn:> [x y] (+ x y))"


  (ptr-display (ut/lang-pointer :lua
                                {:module 'L.core
                                 :id 'identity-fn
                                 :section :code
                                 :library +library-ext+})
               {:layout :full})
  => "function L_core____identity_fn(x){\n  return x;\n}")

^{:refer hara.lang.pointer/ptr-invoke-meta :added "4.0"}
(fact "prepares the meta for a pointer"

  (ptr-invoke-meta +ptr+ {})
  => map?)

^{:refer hara.lang.pointer/rt-macro-opts :added "4.0"}
(fact "creates the default macro-opts for a runtime"

  (rt-macro-opts :lua)
  => map?)

^{:refer hara.lang.pointer/ptr-invoke-string :added "4.0"}
(fact "emits the invoke string"

  (ptr-invoke-string +ptr+ [1 2] {:layout :full})
  => "1 + 2")

^{:refer hara.lang.pointer/ptr-invoke-script :added "4.0"}
(fact "emits a script with dependencies"

  (ptr-invoke-script +ptr+ [1 2] {:layout :full})
  => "1 + 2"

  (ptr-invoke-script (ut/lang-pointer :lua
                                      {:module 'L.core
                                       :form '(do 1 2 3)
                                       :library +library-ext+})
                     []
                     {:layout :full})
  => #"1;?\n2;?\n3;?"

  (ptr-invoke-script (ut/lang-pointer :lua
                                      {:module 'L.core
                                       :id 'identity-fn
                                       :section :code
                                       :library +library-ext+})
                     [1] {:layout :full
                          :emit {:body {:transform (fn [x _]
                                                     (list 'print x))}}})
  => (prose/|
      "function L_core____identity_fn(x){"
      "  return x;"
      "}"
      ""
      "print(L_core____identity_fn(1))"))

^{:refer hara.lang.pointer/ptr-intern :added "4.0"}
(fact "interns the symbol into the workspace environment"
  (ptr-intern *ns* 'foo {:lang :lua}) => #'hara.lang.pointer-test/foo)

^{:refer hara.lang.pointer/ptr-output-json :added "4.0"}
(fact "extracetd function from ptr-output"
  (ptr-output-json {"type" "data" "value" 1}) => 1)

^{:refer hara.lang.pointer/ptr-output :added "4.0"}
(fact "output types for embedded return values"

  (ptr-output "[1,2,3]"
              false)
  => "[1,2,3]"

  (ptr-output (f/wrapped "[1,2,3]")
              :string)
  => [1 2 3]

  (ptr-output "[1,2,3]"
              true)
  => [1 2 3]

  (ptr-output (json/write {:type "data"
                           :value [1 2 3]})
              :full)
  => [1 2 3]

  (ptr-output (json/write {:type "error"
                           :value "Errored"})
              :full)
  => (throws)

  (ptr-output (json/write {:type "error"
                           :value {:a 1}})
              :full)
  => (throws-info)

  (str
   (ptr-output (json/write {:type "raw"
                            :return "Hello"
                            :value "1 + 3"})
               :full))
  => "<Hello>\n1 + 3")

^{:refer hara.lang.pointer/ptr-invoke :added "4.0"}
(fact "invokes the pointer"
  (ptr-invoke nil (fn [_ x] x) 1 {} nil) => 1)

(comment
  (./import)
  (./create-tests))