(ns std.lang.model-annex.spec-xtalk
  (:require [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]))

(def +features+
  (-> (grammar/build-min [:top-declare
                          :coroutine
                          :macro
                          :macro-arrow
                          :macro-let])))

(def +grammar+
  (grammar/grammar :xt
    (grammar/to-reserved +features+)
    (emit/default-grammar
     {:banned #{:keyword}
      :allow   {:assign  #{:symbol :vector :set}}})))

(def +meta+ (book/book-meta {}))

(def +book+
  (book/book {:lang :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
