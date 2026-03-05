(ns bb.lang.model.spec-xtalk
  (:require [bb.lang.base.emit :as emit]
            [bb.lang.base.grammar :as grammar]
            [bb.lang.base.util :as ut]
            [bb.lang.base.book :as book]
            [bb.lang.base.script :as script]
            [bb.string :as str]
            [bb.lib :as h]))

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
