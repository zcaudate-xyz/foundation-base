^{:no-test true}
(ns js.core.dom
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.html :as html]
            [std.string :as str])
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
     (h/$ (. ~elem (getElementById ~id))))))

(defmacro.js text
  "gets the text of a given root"
  {:added "4.0"}
  ([elem? & [id]]
   (let [[elem id] (if id
                    [elem? id]
                    ['document elem?])]
     (h/$ (. (. ~elem (getElementById ~id))
             textContent)))))

(defmacro.js val
  ([elem? & [id]]
   (let [[elem id] (if id
                    [elem? id]
                    ['document elem?])]
     (h/$ (JSON.parse (. (. ~elem (getElementById ~id))
                         textContent))))))

(defmacro.js click
  "clicks on an element"
  {:added "4.0"}
  ([elem]
   (if (string? elem)
     (h/$ (. (document.getElementById ~elem)
             (click)))
     (h/$ (. ~elem (click))))))

(defmacro.js sel
  "selects an element based on id"
  {:added "4.0"}
  ([elem? & [sel]]
   (let [[elem sel] (if sel
                      [elem? sel]
                      ['document elem?])]
     (h/$ (. ~elem (querySelector ~sel))))))

(defmacro.js q
  "gets elements given query"
  {:added "4.0"}
  ([elem? & [sel]]
   (let [[elem sel] (if sel
                      [elem? sel]
                    ['document elem?])]
     (h/$ (. ~elem (querySelectorAll ~sel))))))

(def$.js createElement
  document.createElement)

(defmacro.js create
  "creates an element from tree form"
  {:added "4.0"}
  [tree]
  (let [html (str/trim (html/html (eval tree)))]
    (h/$ (do:> (let [elem (document.createElement "template")
                    _ (:= (. elem innerHTML) ~html)]
                (return elem.content.firstChild))))))

(defmacro.js appendChild
  "appends a child to the dom"
  {:added "4.0"}
  ([elem? & [child]]
   (let [[elem child] (if child
                        [elem? child]
                        ['document elem?])]
     (h/$ (. ~elem (appendChild ~child))))))

(defmacro.js removeChild
  "removes a child from the dom"
  {:added "4.0"}
  ([elem child]
   (h/$ (. ~elem (removeChild ~child)))))

(defmacro.js remove
  "removes node from the dom"
  {:added "4.0"}
  ([elem]
   (h/$ (. ~elem (remove)))))

(defmacro.js setAttribute
  "sets attribute of an element"
  {:added "4.0"}
  ([elem key val]
   (h/$ (. ~elem (setAttribute ~key ~val)))))

(defmacro.js getAttribute
  "gets the attribute of an element"
  {:added "4.0"}
  ([elem key]
   (h/$ (. ~elem (getAttribute ~key)))))

(defmacro.js classList
  "returns the class list"
  {:added "4.0"}
  ([elem]
   (h/$ (. ~elem classList))))

(defmacro.js className
  "returns the class name"
  {:added "4.0"}
  ([elem]
   (h/$ (. ~elem className))))

(defmacro.js outer
  "gets the outer html of an element"
  {:added "4.0"}
  ([elem]
   (h/$ (. ~elem outerHTML))))

(defmacro.js inner
  "gets the inner httml of an element"
  {:added "4.0"}
  ([elem]
   (h/$ (. ~elem innerHTML))))

(comment
  (./create-tests))
