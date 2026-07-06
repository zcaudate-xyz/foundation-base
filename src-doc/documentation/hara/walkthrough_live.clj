(ns documentation.hara-walkthrough-live
  (:use code.test))

[[:hero {:title "Walkthrough: live"
         :subtitle "Source walkthrough from src-build/walkthrough/std_lang_02_live.clj"
         :lead "This page promotes the existing walkthrough source into the public Hara docs. The implementation source remains in `src-build/walkthrough/std_lang_02_live.clj`; this page explains the intent and links it to the surrounding Hara layers."}]]

[[:chapter {:title "Motivation"}]]
"The walkthrough shows how hara.lang scripts define target contexts, how forms are emitted or executed, and how generated pointers connect Clojure authoring to target language code."

[[:chapter {:title "How to use it"}]]
"Read the source file directly for the executable facts. The docs page should keep high-level explanation here and use selected fact snippets when examples are expanded further."

[[:chapter {:title "Source"}]]
[[:file {:src "src-build/walkthrough/std_lang_02_live.clj"}]]
