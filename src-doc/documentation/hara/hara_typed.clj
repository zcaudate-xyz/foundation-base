(ns documentation.hara-typed
  (:use code.test))

[[:hero {:title "hara.typed"
         :subtitle "Typed xtalk analysis and emission."
         :lead "`hara.typed` analyzes xtalk type declarations, records, functions, calls, compatibility, inference, and lowering for generated target declarations."}]]

[[:chapter {:title "Motivation"}]]
"Typed xtalk examples define records and functions once, then emit language-specific declarations such as Go structs or TypeScript `.d.ts` files."

[[:chapter {:title "Examples"}]]
"See `src-build/play/go_001_xtalk_user_directory` and `src-build/play/ts_001_single_source_user_directory` for single-source typed xtalk projects."

[[:chapter {:title "API"}]]
