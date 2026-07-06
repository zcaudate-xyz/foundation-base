(ns documentation.xt-examples
  (:use code.test))

[[:hero {:title "xt examples"
         :subtitle "POCs, xtbench, and generated project examples."
         :lead "The example material ties xt libraries back to real generated programs and parity tests."}]]

[[:chapter {:title "Generated project examples" :link "generated"}]]

"The `src-build/play/go_001_xtalk_user_directory` and `src-build/play/ts_001_single_source_user_directory` examples are the clearest single-source xtalk projects. They define typed records and functions once, then emit target artifacts for Go or TypeScript/JavaScript."

[[:chapter {:title "POC tests" :link "poc"}]]

"The `test-lang/xt/db/poc` tests show database clients in basic, webworker, shared tree, shared RPC, shared worker, Supabase auth, and adaptor-client scenarios. These are better documentation examples than raw API lists because they show layer composition."

[[:chapter {:title "Parity tests" :link "parity"}]]

"The `test-lang/xtbench` tests exercise portable common libraries across target languages. Use them when documenting which common library behavior is expected to remain identical between emitted runtimes."
