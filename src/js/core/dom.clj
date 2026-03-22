^{:no-test true}
(ns js.core.dom
  (:require [std.html :as html]
            [std.lang :as l]
            [std.lib.template]
            [std.string.common])
  (:refer-clojure :exclude [remove val]))

(l/script :js)

(defmacro.js body
  "gets the document body
   
   (str (dom/body))
   => #\"<HTMLBodyElement>\""
  {:added "4.0"}
  []
  'document.body)

(defmacro.js id
  "gets element given id"
  {:added "4.0"}
  ([elem? & [id]]
   (let [[elem id] (if id
                    [elem? id]
                    ['document elem?])]
     (std.lib.template/$ (. ~elem (getElementById ~id))))))

(defmacro.js text
  "gets the text of a given root"
  {:added "4.0"}
  ([elem? & [id]]
   (let [[elem id] (if id
                    [elem? id]
                    ['document elem?])]
     (std.lib.template/$ (. (. ~elem (getElementById ~id))
             textContent)))))

(defmacro.js val
  ([elem? & [id]]
   (let [[elem id] (if id
                    [elem? id]
                    ['document elem?])]
     (std.lib.template/$ (JSON.parse (. (. ~elem (getElementById ~id))
                         textContent))))))

(defmacro.js click
  "clicks on an element"
  {:added "4.0"}
  ([elem]
   (if (string? elem)
     (std.lib.template/$ (. (document.getElementById ~elem)
             (click)))
     (std.lib.template/$ (. ~elem (click))))))

(defmacro.js sel
  "selects an element based on id"
  {:added "4.0"}
  ([elem? & [sel]]
   (let [[elem sel] (if sel
                      [elem? sel]
                      ['document elem?])]
     (std.lib.template/$ (. ~elem (querySelector ~sel))))))

(defmacro.js q
  "gets elements given query"
  {:added "4.0"}
  ([elem? & [sel]]
   (let [[elem sel] (if sel
                      [elem? sel]
                    ['document elem?])]
     (std.lib.template/$ (. ~elem (querySelectorAll ~sel))))))

(def$.js createElement
  document.createElement)

(defmacro.js create
  "creates an element from tree form"
  {:added "4.0"}
  [tree]
  (let [html (std.string.common/trim (html/html (eval tree)))]
    (std.lib.template/$ (do:> (let [elem (document.createElement "template")
                    _ (:= (. elem innerHTML) ~html)]
                (return elem.content.firstChild))))))

(defmacro.js appendChild
  "appends a child to the dom"
  {:added "4.0"}
  ([elem? & [child]]
   (let [[elem child] (if child
                        [elem? child]
                        ['document elem?])]
     (std.lib.template/$ (. ~elem (appendChild ~child))))))

(defmacro.js removeChild
  "removes a child from the dom"
  {:added "4.0"}
  ([elem child]
   (std.lib.template/$ (. ~elem (removeChild ~child)))))

(defmacro.js remove
  "removes node from the dom"
  {:added "4.0"}
  ([elem]
   (std.lib.template/$ (. ~elem (remove)))))

(defmacro.js setAttribute
  "sets attribute of an element"
  {:added "4.0"}
  ([elem key val]
   (std.lib.template/$ (. ~elem (setAttribute ~key ~val)))))

(defmacro.js getAttribute
  "gets the attribute of an element"
  {:added "4.0"}
  ([elem key]
   (std.lib.template/$ (. ~elem (getAttribute ~key)))))

(defmacro.js classList
  "returns the class list"
  {:added "4.0"}
  ([elem]
   (std.lib.template/$ (. ~elem classList))))

(defmacro.js className
  "returns the class name"
  {:added "4.0"}
  ([elem]
   (std.lib.template/$ (. ~elem className))))

(defmacro.js outer
  "gets the outer html of an element"
  {:added "4.0"}
  ([elem]
   (std.lib.template/$ (. ~elem outerHTML))))

(defmacro.js inner
  "gets the inner httml of an element"
  {:added "4.0"}
  ([elem]
   (std.lib.template/$ (. ~elem innerHTML))))

(comment
  (./create-tests))
