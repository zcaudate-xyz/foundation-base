(ns std.block.highlight
  (:require [std.pretty.protocol :as protocol.pretty]
            [std.lib :as h]))

(defrecord HighlightingVisitor [visitor]
  protocol.pretty/IVisitor
  (-visit-keyword [_ x]
    [:color :yellow (protocol.pretty/-visit-keyword visitor x)])

  (-visit-seq [_ x]
    (let [doc (protocol.pretty/-visit-seq visitor x)]
      (if (symbol? (first x))
        (let [[lparen & rest] doc]
          (apply list lparen [:color :cyan (first rest)] (rest rest)))
        doc)))

  (-visit-nil [_] (protocol.pretty/-visit-nil visitor))
  (-visit-boolean [_ x] (protocol.pretty/-visit-boolean visitor x))
  (-visit-string [_ x] (protocol.pretty/-visit-string visitor x))
  (-visit-character [_ x] (protocol.pretty/-visit-character visitor x))
  (-visit-symbol [_ x] (protocol.pretty/-visit-symbol visitor x))
  (-visit-number [_ x] (protocol.pretty/-visit-number visitor x))
  (-visit-vector [_ x] (protocol.pretty/-visit-vector visitor x))
  (-visit-map [_ x] (protocol.pretty/-visit-map visitor x))
  (-visit-set [_ x] (protocol.pretty/-visit-set visitor x))
  (-visit-tagged [_ x] (protocol.pretty/-visit-tagged visitor x))
  (-visit-meta [_ meta x] (protocol.pretty/-visit-meta visitor meta x))
  (-visit-var [_ x] (protocol.pretty/-visit-var visitor x))
  (-visit-pattern [_ x] (protocol.pretty/-visit-pattern visitor x))
  (-visit-record [_ x] (protocol.pretty/-visit-record visitor x))
  (-visit-unknown [_ x] (protocol.pretty/-visit-unknown visitor x)))
