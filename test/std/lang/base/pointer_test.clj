(ns std.lang.base.pointer-test
  (:use code.test)
  (:require [std.lang.base.pointer :refer :all]
            [std.lang.base.util :as ut]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.book :as book]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.json :as json]
            [std.lib :as h]))

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

^{:refer std.lang.base.pointer/with:clip :added "4.0"}
(fact "form to control `clip` option"
  (with:clip *clip*) => true)

^{:refer std.lang.base.pointer/with:print :added "4.0"}
(fact "form to control `print` option"
  (with:print *print*) => #{:input})

^{:refer std.lang.base.pointer/with:print-all :added "4.0"}
(fact "toggles print for all intermediate steps"
  (with:print-all *print*) => #{:input-form :raw-input :raw-output})

^{:refer std.lang.base.pointer/with:rt :added "4.0"}
(fact "forcibly applies a runtime"
  (with:rt [1] std.lib.context.pointer/*runtime*) => 1)

^{:refer std.lang.base.pointer/with:rt-wrap :added "4.0"}
(fact "wraps an additional function to the invoke function"
  (with:rt-wrap [inc] *rt-wrap*) => inc)

^{:refer std.lang.base.pointer/with:input :added "4.0"}
(fact "form to control `input` option"
  (with:input *input*) => #{:default})

^{:refer std.lang.base.pointer/with:raw :added "4.0"}
(fact "form to control `raw` option"
  (with:raw *output*) => #{:raw})

^{:refer std.lang.base.pointer/get-entry :added "4.0"}
(fact "gets the library entry given pointer"
  ^:hidden
  
  (get-entry +ptr+)
  => book/book-entry?)

^{:refer std.lang.base.pointer/ptr-tag :added "4.0"}
(fact "creates a tag for the pointer"
  ^:hidden
  
  (ptr-tag +ptr+ :lib)
  => '[:lib L.core/add :fragment])

^{:refer std.lang.base.pointer/ptr-deref :added "4.0"}
(fact "gets the entry or the free pointer data"
  ^:hidden
  
  (ptr-deref +ptr+)
  => book/book-entry?

  (ptr-deref (dissoc +ptr+ :id))
  => map?)

^{:refer std.lang.base.pointer/ptr-display :added "4.0"}
(fact "emits the display string for pointer"
  ^:hidden
  
  (ptr-display +ptr+ {})
  "(fn:> [x y] (+ x y))"

  
  (ptr-display (ut/lang-pointer :lua
                                {:module 'L.core
                                 :id 'identity-fn
                                 :section :code
                                 :library +library-ext+})
               {:layout :full})
  => "function L_core____identity_fn(x){\n  return x;\n}")

^{:refer std.lang.base.pointer/ptr-invoke-meta :added "4.0"}
(fact "prepares the meta for a pointer"
  ^:hidden
  
  (ptr-invoke-meta +ptr+ {})
  => map?)

^{:refer std.lang.base.pointer/rt-macro-opts :added "4.0"}
(fact "creates the default macro-opts for a runtime"
  ^:hidden
  
  (rt-macro-opts :lua)
  => map?)

^{:refer std.lang.base.pointer/ptr-invoke-string :added "4.0"}
(fact "emits the invoke string"
  ^:hidden
  
  (ptr-invoke-string +ptr+ [1 2] {:layout :full})
  => "1 + 2")

^{:refer std.lang.base.pointer/ptr-invoke-script :added "4.0"}
(fact "emits a script with dependencies"
  ^:hidden
  
  (ptr-invoke-script +ptr+ [1 2] {:layout :full})
  => "1 + 2"
 
  (ptr-invoke-script (ut/lang-pointer :lua
                                      {:module 'L.core
                                       :id 'identity-fn
                                       :section :code
                                       :library +library-ext+})
                     [1] {:layout :full
                          :emit {:body {:transform (fn [x _]
                                                     (list 'print x))}}})
  => (std.string/|
      "function L_core____identity_fn(x){"
      "  return x;"
      "}"
      ""
      "print(L_core____identity_fn(1))"))

^{:refer std.lang.base.pointer/ptr-intern :added "4.0"}
(fact "interns the symbol into the workspace environment"
  (ptr-intern *ns* 'foo {:lang :lua}) => #'std.lang.base.pointer-test/foo)

^{:refer std.lang.base.pointer/ptr-output-json :added "4.0"}
(fact "extracetd function from ptr-output"
  (ptr-output-json {"type" "data" "value" 1}) => 1)

^{:refer std.lang.base.pointer/ptr-output :added "4.0"}
(fact "output types for embedded return values"
  ^:hidden
  
  (ptr-output "[1,2,3]"
              false)
  => "[1,2,3]"

  (ptr-output (h/wrapped "[1,2,3]")
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

^{:refer std.lang.base.pointer/ptr-invoke :added "4.0"}
(fact "invokes the pointer"
  (ptr-invoke nil (fn [_ x] x) 1 {} nil) => 1)

(comment
  (./import)
  (./create-tests))
