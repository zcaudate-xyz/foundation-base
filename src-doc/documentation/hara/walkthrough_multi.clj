(ns documentation.hara-walkthrough-multi
  (:require [hara.lang :as l]
            [std.lib :as h]
            [std.string :as str])
  (:use code.test))

[[:hero {:title "Walkthrough: multi"
         :subtitle "Source walkthrough from src-build/walkthrough/std_lang_01_multi.clj"
         :lead "This page promotes the existing walkthrough source into the public Hara docs. The implementation source remains in `src-build/walkthrough/std_lang_01_multi.clj`; this page explains the intent and links it to the surrounding Hara layers."}]]

[[:chapter {:title "Motivation"}]]
"The walkthrough shows how hara.lang scripts define target contexts, how forms are emitted or executed, and how generated pointers connect Clojure authoring to target language code."

[[:chapter {:title "How to use it"}]]
"Read the source file directly for the executable facts. The docs page keeps high-level explanation here and uses selected fact snippets below."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "JavaScript context"}]]

"`l/script- :js` installs a JavaScript book into the current namespace. `!.js` evaluates a form and returns emitted JS; `defn.js` stores a function entry."

(fact "emit JS from a script context"
  ^{:refer hara.lang/script- :added "4.0"}
  (l/script- :js
    {:require [[xt.lang.spec-base :as xt]]})
  (!.js (+ 1 2 3))
  => "1 + 2 + 3"

  (defn.js hello []
    (return (+ 1 2 3)))
  (l/emit-ptr hello)
  => "export function hello(){\n  return 1 + 2 + 3;\n}")

[[:section {:title "Lua context"}]]

"`l/script- :lua` does the same for Lua. The same hara.lang form now produces Lua syntax."

(fact "emit Lua from a script context"
  ^{:refer hara.lang/script- :added "4.0"}
  (l/script- :lua
    {:require [[xt.lang.spec-base :as xt]]})
  (!.lua (+ 1 2 3))
  => "1 + 2 + 3"

  (defn.lua world []
    (return (+ 1 2 3)))
  (l/emit-ptr world)
  => "local function world()\n  return 1 + 2 + 3\nend")

[[:section {:title "Python and R contexts"}]]

"The same pattern extends to Python and R. Each runtime has its own book, module, and emitted syntax."

(fact "emit Python from a script context"
  ^{:refer hara.lang/script- :added "4.0"}
  (l/script- :python
    {:require [[xt.lang.spec-base :as xt]]})
  (!.py (+ 1 2 3))
  => "1 + 2 + 3"

  (defn.py again []
    (return (+ 1 2 3)))
  (l/emit-ptr again)
  => "def again():\n  return 1 + 2 + 3")

(fact "emit R from a script context"
  ^{:refer hara.lang/script- :added "4.0"}
  (l/script- :r
    {:require [[xt.lang.spec-base :as xt]]})
  (!.R (+ 1 2 3))
  => "1 + 2 + 3"

  (def.R stuff
    (fn []
      (return (+ 1 2 3))))
  (l/emit-ptr stuff)
  => "stuff <- function (){\n  return(1 + 2 + 3);\n};")

[[:section {:title "Multiple contexts in one namespace"}]]

"`l/script+` creates additional named contexts when you need more than one runtime of the same language in a namespace. The first argument is a `[env-name language]` vector."

(fact "create two Python contexts and two JS contexts"
  ^{:refer hara.lang/script+ :added "4.0"}
  (l/script+ [:env1 :python]
    {:require [[xt.lang.spec-base :as xt]]})
  (l/script+ [:env2 :js]
    {:require [[xt.lang.spec-base :as xt]]})
  [(l/! [:env1]
     (fn [] (return (+ 1 2 3))))
   (l/! [:env2]
     (fn [] (return (+ 1 2 3))))]
  => ["lambda *__args : 1 + 2 + 3"
      "function (){\n  return 1 + 2 + 3;\n}"])

[[:chapter {:title "Source"}]]
[[:file {:src "src-doc/walkthrough/std_lang_01_multi.clj"}]]
