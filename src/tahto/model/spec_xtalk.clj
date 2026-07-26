(ns tahto.model.spec-xtalk
  (:require [tahto.base.book :as book]
            [tahto.common.emit :as emit]
            [tahto.common.grammar :as grammar]
            [tahto.core.script :as script]
            [tahto.common.util :as ut]))

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
