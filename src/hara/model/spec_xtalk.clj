(ns hara.model.spec-xtalk
  (:require [hara.lang.base.book :as book]
            [hara.common.emit :as emit]
            [hara.common.grammar :as grammar]
            [hara.lang.base.script :as script]
            [hara.common.util :as ut]))

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
