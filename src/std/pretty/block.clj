(ns std.pretty.block
  (:require [std.block.base :as base]
            [std.pretty.protocol :as protocol.pretty]
            [std.pretty.color :as color]))

(defn- format-delimiters [printer block children]
  (let [limits (get base/*container-limits* (base/block-tag block))]
    [:group
     (color/-document printer :delimiter (:start limits))
     [:align (interpose :line children)]
     (color/-document printer :delimiter (:end limits))]))

(defn format-block
  "The main handler function for `std.block` objects.

   It checks if the block is a container and formats it recursively,
   or formats it as a simple token if not."
  [format-fn printer block]
  (if (base/container? block)
    (let [children (map (partial format-fn printer)
                        (base/block-children block))]
      (format-delimiters printer block children))
    (color/-document printer
                     (if (= :keyword (base/block-tag block))
                       :keyword
                       :symbol)
                     (base/block-string block))))
