(ns documentation.xt-examples
  (:use code.test)
  (:require [clojure.string :as str]
            [hara.typed :as typed]
            [hara.typed.xtalk-parse :as xtalk-parse]
            [hara.model.spec-go.typed :as go-typed]
            [hara.model.spec-js.ts :as ts-typed]))

[[:hero {:title "xt examples"
         :subtitle "POCs, xtbench, and generated project examples."
         :lead "The example material ties xt libraries back to real generated programs and parity tests."}]]

[[:chapter {:title "Generated project examples" :link "generated"}]]

"The `src-build/play/go_001_xtalk_user_directory` and `src-build/play/ts_001_single_source_user_directory` examples are the clearest single-source xtalk projects. They define typed records and functions once, then emit target artifacts for Go or TypeScript/JavaScript."

[[:chapter {:title "POC tests" :link "poc"}]]

"The `test-lang/xt/db/poc` tests show database clients in basic, webworker, shared tree, shared RPC, shared worker, Supabase auth, and adaptor-client scenarios. These are better documentation examples than raw API lists because they show layer composition."

[[:chapter {:title "Parity tests" :link "parity"}]]

"The `test-lang/xtbench` tests exercise portable common libraries across target languages. Use them when documenting which common library behavior is expected to remain identical between emitted runtimes."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Inspecting generated xtalk source"}]]

"Generated projects are authored as xtalk source files. The `hara.typed` loader turns a source file into a context that exposes specs, functions, and values declared in the file."

(fact "load the Go user directory example as a typed context"
  (-> (typed/load-file "src-build/play/go_001_xtalk_user_directory/main.clj")
      :domain)
  => :xtalk)

(fact "inspect the User record and lookupUser function"
  (let [ctx (typed/load-file "src-build/play/go_001_xtalk_user_directory/main.clj")]
    [(-> (typed/spec-def ctx 'play.go-001-xtalk-user-directory.main/User)
         :type
         :kind)
     (mapv (comp str :name)
           (typed/function-input ctx 'play.go-001-xtalk-user-directory.main/lookupUser))
     (-> (typed/function-output ctx 'play.go-001-xtalk-user-directory.main/lookupUser)
         :kind)])
  => [:record ["users" "id"] :maybe])

[[:section {:title "Emitting target language declarations"}]]

"The typed analysis is fed into language-specific emitters to produce Go or TypeScript declarations. This is the same pipeline the example build files use."

(fact "emit Go type declarations"
  (-> (xtalk-parse/analyze-file "src-build/play/go_001_xtalk_user_directory/main.clj")
      (go-typed/emit-analysis-declarations)
      (str/includes? "type User"))
  => true)

(fact "emit TypeScript type declarations"
  (-> (xtalk-parse/analyze-file "src-build/play/ts_001_single_source_user_directory/main.clj")
      (ts-typed/emit-analysis-declarations)
      (str/includes? "export interface User"))
  => true)

[[:section {:title "POC and parity material"}]]

"The database POC tests under `test-lang/xt/db/poc` compose the same xt.db and xt.substrate layers against real services, so they are best explored after the xt.db and xt.substrate walkthroughs. The xtbench suite then runs portable common library tests across emitted targets to enforce parity."
