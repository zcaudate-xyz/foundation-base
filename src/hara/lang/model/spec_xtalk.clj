(ns hara.lang.model.spec-xtalk
  (:require [hara.lang.base.book :as book]
            [hara.lang.base.emit :as emit]
            [hara.lang.base.grammar :as grammar]
            [hara.lang.base.script :as script]
            [hara.lang.base.util :as ut]))

(def +features+
  (-> (grammar/build-min [:top-declare
                          :coroutine
                          :macro
                          :macro-arrow
                          :macro-let])
      (merge (grammar/build-xtalk))))

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
