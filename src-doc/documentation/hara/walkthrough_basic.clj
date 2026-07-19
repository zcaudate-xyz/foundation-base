(ns documentation.hara-walkthrough-basic
  (:require [hara.lang :as l]
            [std.lib :as h])
  (:use code.test))

[[:hero {:title "Walkthrough: basic"
         :subtitle "Source walkthrough from src-doc/walkthrough/std_lang_00_basic.clj"
         :lead "The basic walkthrough introduces hara.lang script contexts: how a target language is installed into a Clojure namespace, how forms are emitted as target code, and how saved entries link together as a dependency graph."}]]

[[:chapter {:title "Motivation"}]]

"hara.lang lets you write target language code — JavaScript, Lua, Python, R, SQL — as ordinary Clojure forms. A *script context* installs the macros for one target into the current namespace. Inside that context you can emit code for inspection, or save entries into a *book* that tracks every definition, its dependencies, and its emitted output."

"The walkthrough builds up the four kinds of book entries: plain definitions (`def.js`), functions (`defn.js`), fragments (`def$.js`), and emission-time macros (`defmacro.js`)."

[[:chapter {:title "How to use it"}]]

"Each section below explains one concept and shows an executable fact. The complete, unabridged source — with deeper assertions on the internal entry structures — is included at the end of the page and lives at `src-doc/walkthrough/std_lang_00_basic.clj`."

"None of these examples execute target code; they only *emit* it. For live execution against a real runtime, see the live walkthrough."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Setting up a script context"}]]

"`l/script-` installs a script context for a target language into the current namespace. It reads like an `ns` form — `:require` pulls in the runtime libraries the emitted code may call. The trailing `-` marks the *testing* context; `l/script` (no dash) is used in source files."

"Once installed, `!.js` evaluates a form *for emission* and returns the generated JavaScript as a string. No JavaScript runtime is involved."

(fact "!.js returns emitted JavaScript as a string"
  ^{:refer hara.lang/script- :id wt-basic/emit}
  (l/script- :js
    {:require [[xt.lang.spec-base :as xt]]})

  (!.js
   (+ 1 2 3))
  => "1 + 2 + 3;"

  (!.js
   (fn [] (return (+ 1 2 3))))
  => "function (){\n  return 1 + 2 + 3;\n}"

  (!.js
   (k/obj-pick {:a 1 :b 2} ["a"]))
  => "k.obj_pick({\"a\":1,\"b\":2},[\"a\"]);")

"The third form calls `k/obj-pick`, a function from the required runtime library (`xt.lang.spec-base`, aliased to `k` in emitted code). Emitted calls to runtime libraries are resolved against the book's requires, not against Clojure vars."

[[:section {:title "Saving definitions with def.js"}]]

"`def.js` looks like `def` but instead of a value it produces a *pointer*. The pointer wraps a *book entry*: the recorded form, its language, its module, and its dependency set."

(fact "def.js saves a :def entry in the book"
  ^{:refer hara.lang/script- :id wt-basic/def-js}
  (def.js answer (+ 1 2 3))

  (!.js -/answer)
  => "answer;"

  (type answer)
  => std.lib.context.pointer.Pointer

  (into {} answer)
  => {:context :lang/js
      :lang :js
      :id 'answer
      :module 'documentation.hara-walkthrough-basic
      :section :code
      :context/fn #'hara.common.util/lang-rt-default})

"Inside an emission form, `-/answer` refers to the pointer named `answer` in the current namespace. Dereferencing the pointer (`@answer`) yields the underlying `BookEntry` with the full record: operation, input form, display settings, and dependencies."

[[:section {:title "Functions with defn.js"}]]

"`defn.js` saves a `:defn` entry. Calling the pointer like a function does not invoke JavaScript — it emits the *call* as a string, which is exactly what you want when building larger generated expressions."

(fact "defn.js saves a :defn entry and emits calls"
  ^{:refer hara.lang/script- :id wt-basic/defn-js}
  (defn.js greet
    [a b]
    (return (+ a b)))

  (greet 1 2)
  => "greet(1,2)"

  (-> @greet (into {}) :op)
  => 'defn)

[[:section {:title "Linking entries with -/"}]]

"Only `-/` symbols are treated as links. When a saved form references `-/other-entry`, the reference is rewritten to the fully qualified symbol and recorded in the entry's `:deps` set. The book therefore knows the true dependency graph of every definition — which is what later stages use to emit complete, ordered source files."

(fact "-/ references are recorded as dependencies"
  ^{:refer hara.lang/script- :id wt-basic/linking}
  (def.js base (+ 1 2 3))

  (defn.js add-base
    [c]
    (return (+ -/base c)))

  (-> @add-base (into {}) :form)
  => '(defn add-base
       [c]
       (return (+ (documentation.hara-walkthrough-basic/base)
                  c)))

  (-> @add-base (into {}) :deps)
  => '#{documentation.hara-walkthrough-basic/base})

[[:section {:title "Reusable fragments with def$.js"}]]

"`def$.js` saves a `:fragment` entry — a named, replaceable snippet. Referencing the fragment with `-/` inlines its emitted body rather than a symbol, so fragments behave like copy-paste units that still show up in the book."

(fact "def$.js creates an inlineable fragment"
  ^{:refer hara.lang/script- :id wt-basic/fragment}
  (def$.js greeting (+ 1 2 3))

  (!.js -/greeting)
  => "1 + 2 + 3;"

  (-> @greeting (into {}) :section)
  => :fragment)

[[:section {:title "Emission-time macros with defmacro.js"}]]

"`defmacro.js` saves a macro entry. The body is an ordinary Clojure function over *forms*, evaluated while the target code is being emitted — the generated output is whatever the macro returns, re-emitted."

(fact "defmacro.js expands forms during emission"
  ^{:refer hara.lang/script- :id wt-basic/defmacro}
  (defmacro.js double-add
    [a b]
    (list '+ a a b b))

  (!.js (-/double-add 1 2))
  => "1 + 1 + 2 + 2;")

[[:chapter {:title "What comes next"}]]

"The multi walkthrough repeats this setup for Lua, Python, and R and shows `l/script+` for several named contexts in one namespace. The live walkthrough adds `:runtime :basic` so emitted code is executed and results come back as Clojure values."

[[:chapter {:title "Source"}]]
[[:file {:src "src-doc/walkthrough/std_lang_00_basic.clj"}]]
