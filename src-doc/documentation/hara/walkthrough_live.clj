(ns documentation.hara-walkthrough-live
  (:require [hara.lang :as l])
  (:use code.test))

[[:hero {:title "Walkthrough: live"
         :subtitle "Source walkthrough from src-build/walkthrough/std_lang_02_live.clj"
         :lead "This page promotes the existing walkthrough source into the public Hara docs. The implementation source remains in `src-build/walkthrough/std_lang_02_live.clj`; this page explains the intent and links it to the surrounding Hara layers."}]]

[[:chapter {:title "Motivation"}]]
"The walkthrough shows how hara.lang scripts define target contexts, how forms are emitted or executed, and how generated pointers connect Clojure authoring to target language code."

[[:chapter {:title "How to use it"}]]
"Read the source file directly for the executable facts. The docs page keeps high-level explanation here and uses selected fact snippets below."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Live JS execution"}]]

"Adding `:runtime :basic` to `l/script-` makes `!.js` execute the generated code and return the result. You can define functions and call them like ordinary Clojure values."

(fact "evaluate JS arithmetic live"
  ^{:refer hara.lang/script- :added "4.0"}
  (do
    (l/script- :js
      {:runtime :basic
       :require [[xt.lang.spec-base :as xt]]})
    (!.js (+ 1 2 3)))
  => 6)

(fact "define and call a JS function"
  ^{:refer hara.lang/emit-ptr :added "4.0"}
  (do
    (l/script- :js
      {:runtime :basic
       :require [[xt.lang.spec-base :as xt]]})
    (defn.js hello []
      (return (+ 1 2 3)))
    [(hello)
     (!.js (+ (-/hello) (-/hello)))])
  => [6 12])

[[:section {:title "Observing emitted code"}]]

"`l/with:print-all` echoes the generated code that is sent to the runtime. Wrap it in `with-out-str` to capture the output as a string."

(fact "capture emitted JS with print-all"
  ^{:refer hara.lang/with:print-all :added "4.0"}
  (do
    (l/script- :js
      {:runtime :basic
       :require [[xt.lang.spec-base :as xt]]})
    (defn.js hello []
      (return (+ 1 2 3)))
    (string?
     (with-out-str
       (l/with:print-all
         (!.js (+ (-/hello) (-/hello)))))))
  => true)

[[:section {:title "Live Lua and Python execution"}]]

"The `:basic` runtime works for Lua and Python too. The same hara.lang function body runs in each target process."

(fact "evaluate Lua live"
  ^{:refer hara.lang/script- :added "4.0"}
  (do
    (l/script- :lua
      {:runtime :basic
       :require [[xt.lang.spec-base :as xt]]})
    (defn.lua world []
      (return (+ 1 2 3)))
    (world))
  => 6)

(fact "evaluate Python live"
  ^{:refer hara.lang/script- :added "4.0"}
  (do
    (l/script- :python
      {:runtime :basic
       :require [[xt.lang.spec-base :as xt]]})
    (defn.py again []
      (return (+ 1 2 3)))
    (again))
  => 6)

[[:section {:title "End-to-end: combine live results"}]]

"Because each runtime returns Clojure values, you can mix results from several languages in a single Clojure expression."

(fact "sum results from live JS, Lua, and Python runtimes"
  (do
    (l/script- :js
      {:runtime :basic
       :require [[xt.lang.spec-base :as xt]]})
    (defn.js hello []
      (return 1))
    (l/script- :lua
      {:runtime :basic
       :require [[xt.lang.spec-base :as xt]]})
    (defn.lua world []
      (return 2))
    (l/script- :python
      {:runtime :basic
       :require [[xt.lang.spec-base :as xt]]})
    (defn.py again []
      (return 3))
    (+ (hello) (world) (again)))
  => 6)

[[:chapter {:title "Source"}]]
[[:file {:src "src-doc/walkthrough/std_lang_02_live.clj"}]]
