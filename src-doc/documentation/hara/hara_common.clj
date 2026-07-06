(ns documentation.hara-common
  (:use code.test))

[[:hero {:title "hara.common"
         :subtitle "Shared grammar, emit, and preprocess layers."
         :lead "`hara.common` holds reusable grammar, emission, preprocess, rewrite, and utility functions shared by target language models."}]]

[[:chapter {:title "Motivation"}]]
"Target models should not each reinvent expression emission, assignment handling, function rendering, top-level forms, or preprocessing. `hara.common` keeps those concerns reusable."

[[:chapter {:title "API"}]]
