(ns documentation.hara-common
  (:require [hara.common.util :as util]
            [hara.common.grammar :as grammar]
            [hara.common.emit-helper :as emit-helper]
            [hara.common.provenance :as provenance])
  (:use code.test))

[[:hero {:title "hara.common"
         :subtitle "Shared grammar, emit, and preprocess layers."
         :lead "`hara.common` holds reusable grammar, emission, preprocess, rewrite, and utility functions shared by target language models."}]]

[[:chapter {:title "Motivation"}]]
"Target models should not each reinvent expression emission, assignment handling, function rendering, top-level forms, or preprocessing. `hara.common` keeps those concerns reusable."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Symbol utilities"}]]

"Target language models work with a lot of symbols. `hara.common.util` provides small helpers for decomposing and recombining qualified symbols, as well as converting between idiomatic naming styles."

(fact "decompose and recompose symbols"
  (util/sym-id 'L.core/identity)
  => 'identity

  (util/sym-module 'L.core/identity)
  => 'L.core

  (util/sym-pair 'L.core/identity)
  => '[L.core identity]

  (util/sym-full 'L.core 'identity)
  => 'L.core/identity)

(fact "convert between dash and underscore naming"
  (util/sym-default-str :hello-world)
  => "hello_world"

  (util/sym-default-inverse-str "hello_world")
  => "hello-world")

[[:section {:title "Language contexts and pointers"}]]

"When emitting code for a target language, the model needs to know which runtime context to use. `lang-context` turns a keyword into a namespaced lang keyword, and `lang-pointer` builds a pointer that the runtime can resolve."

(fact "create a language context"
  (util/lang-context :lua)
  => :lang/lua)

(fact "create a language pointer"
  (into {} (util/lang-pointer :lua {:module 'L.core}))
  => {:context :lang/lua
      :module 'L.core
      :lang :lua
      :context/fn #'hara.common.util/lang-rt-default})

[[:section {:title "Building grammars"}]]

"The grammar is the dictionary that maps xtalk operators to target-language emission rules. `hara.common.grammar` collects operator definitions from spec and macro namespaces and lets you build a concrete operator table."

(fact "list grammar categories"
  (take 5 (grammar/ops-list))
  => '(:builtin :builtin-global :builtin-module :builtin-helper :free-control))

(fact "build a grammar for a category"
  (keys (grammar/build :include [:vars]))
  => '(:seteq :var)

  (grammar/ops-summary [:counter])
  => '([:counter {:incby #{:+=}, :decby #{:-=}, :mulby #{:*=}, :incto #{:++}, :decto #{:--}}]))

(fact "construct a grammar object"
  (grammar/grammar? (grammar/grammar :test
                                     (grammar/to-reserved (grammar/build-min))
                                     {}))
  => true)

[[:section {:title "Emit helpers"}]]

"`hara.common.emit-helper` contains low-level helpers used by the emitter, such as classifying forms and reading grammar options."

(fact "classify forms"
  (emit-helper/form-key-base :a)
  => [:keyword :token true]

  (emit-helper/form-key-base 'x)
  => [:symbol :token true]

  (emit-helper/form-key-base [1 2])
  => [:vector :data])

(fact "read default grammar options"
  (select-keys (emit-helper/get-options emit-helper/+default+ [:data :map-entry])
               [:assign :keyword])
  => {:assign ":" :keyword :string})

(fact "quote a string for single-quoted targets"
  (emit-helper/pr-single "hello")
  => "'hello'")

[[:section {:title "Provenance tracking"}]]

"As code passes through preprocessing and emission, errors need to be traced back to their source. `hara.common.provenance` extracts line numbers and module context from forms and merges provenance frames."

(fact "extract line information"
  (provenance/line-of (with-meta '(+ 1 2) {:line 5}))
  => 5)

(fact "build a provenance frame"
  (provenance/provenance {:hara/module {:id 'L.core}
                          :hara/namespace 'documentation.hara-common
                          :hara/form (with-meta '(+ 1 2) {:line 5})})
  => '{:hara/module L.core
       :hara/namespace documentation.hara-common
       :hara/form (+ 1 2)
       :hara/line 5})

(fact "merge provenance into options"
  (-> {:lang :lua}
      (provenance/with-provenance {:hara/phase :emit/direct}
                                  {:hara/module 'L.core})
      :hara/provenance)
  => '{:hara/phase :emit/direct
       :hara/module L.core})

[[:chapter {:title "API"}]]
