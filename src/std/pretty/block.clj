(ns std.pretty.block
  (:require [std.block.base :as base]
            [std.pretty.protocol :as protocol.pretty]
            [std.pretty.color :as color]))

(defn- format-delimiters [format-fn printer block]
  (let [limits (get base/*container-limits* (base/block-tag block))
        children (map (partial format-fn printer) (base/block-children block))]
    [:group
     (color/-document printer :delimiter (:start limits))
     [:align (interpose :line children)]
     (color/-document printer :delimiter (:end limits))]))

(defn format-block
  "The main handler function for `std.block` objects.

   It dispatches on the block's type to provide correct formatting
   for containers, tokens, comments, and void elements."
  [format-fn printer block]
  (case (base/block-type block)
    (:container :collection) (format-delimiters format-fn printer block)

    :token (color/-document printer
                            (if (= :keyword (base/block-tag block))
                              :keyword
                              :symbol)
                            (base/block-string block))

    :comment (color/-document printer :comment (base/block-string block))

    :void (case (base/block-tag block)
            :linebreak :break
            (base/block-string block))

    ;; Default fallback for any other block type
    (base/block-string block)))
