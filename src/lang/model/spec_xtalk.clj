(ns lang.model.spec-xtalk
  (:require [lang.common.book :as book]
            [lang.base.emit :as emit]
            [lang.base.grammar :as grammar]
            [lang.core.script :as script]
            [lang.base.util :as ut]))

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
