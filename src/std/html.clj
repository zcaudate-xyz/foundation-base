(ns std.html
  (:require [std.lib.collection]
            [std.lib.foundation]
            [std.string.common])
  (:import (org.jsoup Jsoup) (org.jsoup.nodes Attributes Comment DataNode Document Element Node TextNode) (org.jsoup.parser Tag) (org.jsoup.select Selector Elements)))

(defmulti node->tree
  "converts a Jsoup node to tree
 
   (-> (parse \"<body><div>hello</div>world</body>\")
       (node->tree))
   => [:body [:div \"hello\"] \"world\"]"
  {:added "3.0"}
  type)

(defmethod node->tree Comment
  ([^Comment node]
   nil))

(defmethod node->tree TextNode
  ([^TextNode node]
   (std.string.common/trim (.text node))))

(defmethod node->tree DataNode
  ([^DataNode node]
   (.getWholeData node)))

(defmethod node->tree Element
  ([^Element node]
   (let [children (->> (map node->tree (.childNodes node))
                       (remove #{" "}))
         attrs (->> (.attributes node)
                    (std.lib.collection/map-keys keyword))
         attrs (if (:class attrs)
                 (assoc attrs :class
                        (vec (sort (filter not-empty (std.string.common/split (:class attrs) #" ")))))
                 attrs)
         children (if (empty? attrs)
                    children
                    (cons attrs children))]
     (apply vector
            (keyword (.getName (.tag node)))
            (filter not-empty children)))))

(defmethod node->tree Document
  ([^Document node]
   (node->tree (first (.children node)))))

(defn tree->node
  "converts a tree to a Jsoup node
 
   (tree->node [:body [:div \"hello\"] \"world\"])
   => org.jsoup.nodes.Element"
  {:added "3.0"}
  ([rep]
   (cond (vector? rep)
         (let [[tag attrs? & elements] rep
               _ (if-not (keyword? tag)
                   (throw (ex-info "Invalid form:" {:tag tag :form rep})))
               [attrs elements] (if (map? attrs?)
                                  [attrs? elements]
                                  [{} (cons attrs? elements)])
               nattrs    (reduce-kv (fn [out k v]
                                      (let [k (if (keyword? k)
                                                (name k)
                                                (str k))
                                            v (if (vector? v)
                                                (std.string.common/join " " (map std.lib.foundation/strn v))
                                                (str v))]
                                        (.put ^Attributes out (name k) v)))
                                    (Attributes.)
                                    attrs)
               children (->> elements
                             (filter identity)
                             (mapv tree->node))]

           (doto (Element. (Tag/valueOf (name tag)) "" nattrs)
             (.insertChildren 0 ^java.util.List children)))

         (string? rep)
         (DataNode. rep)

         :else
         (throw (ex-info "Only strings or vectors allowed"
                         {:data rep})))))

(defmethod print-method Node
  ([v ^java.io.Writer w]
   (.write w (str "#html" (node->tree v)))))

(defn parse
  "reads a Jsoup node from string
 
   (parse \"<body><div>hello</div>world</body>\")
   => org.jsoup.nodes.Element"
  {:added "3.0"}
  ([^String s]
   (let [s (std.string.common/trim s)
         html? (or (.startsWith s "<html")
                   (.startsWith s "<!DOCTYPE")
                   (.startsWith s "<!doctype"))
         body? (.startsWith s "<body")]
     (cond html?
           (-> (Jsoup/parse s)
               (.children)
               (first))

           body?
           (-> (Jsoup/parseBodyFragment s)
               (.body))

           :else
           (-> (Jsoup/parseBodyFragment s)
               (.body)
               (.children)
               (first))))))

(defn inline
  "emits function without any newlines
 
   (inline (html [:body [:div \"hello\"] \"world\"]))
   => \"<body>  <div>hello</div>world</body>\""
  {:added "4.0"}
  ([^String html]
   (.replaceAll html "\\n" "")))

(defn tighten
  "removes lines for elements that contain no internal elements
 
   (tighten \"<b>\\nhello\\n</b>\")
   => \"<b>hello</b>\""
  {:added "3.0"}
  ([^String html]
   (.replaceAll html "(?m)<(\\w+)>\\s*([^<>]*)$\\s*</\\1>" "<$1>$2</$1>")))

(defn generate
  "generates string html for element
 
   (generate (tree->node [:body [:div \"hello\"] \"world\"]))
   => \"<body>\\n  <div>hello</div>world\\n</body>\""
  {:added "3.0"}
  ([^Element elem]
   (let [output  (-> (doto (Document. "")
                       (-> (.outputSettings) (.indentAmount 2)))
                     (.appendChild elem)
                     (.html))]
     (tighten output))))



(defn html
  "converts either node or tree representation to a html string
 
   (html [:body [:div \"hello\"] \"world\"])
   => \"<body>\\n  <div>hello</div>world\\n</body>\""
  {:added "3.0"}
  ([rep]
   (cond (string? rep)
         rep

         (vector? rep)
         (generate (tree->node rep))

         (instance? Node rep)
         (generate rep)

         :else
         (throw (ex-info "Invalid input" {:input rep})))))

(defn html-inline
  "emits body with no newline
 
   (html-inline [:body [:div \"hello\"] \"world\"])
   => \"<body>  <div>hello</div>world</body>\""
  {:added "4.0"}
  [rep]
  (inline (html rep)))

(defn node
  "converts either a string or tree representation to a Jsoup node
 
   (node [:body [:div \"hello\"] \"world\"])
   => org.jsoup.nodes.Element"
  {:added "3.0"}
  ([rep]
   (cond (string? rep)
         (parse rep)

         (instance? Node rep)
         rep

         (vector? rep)
         (tree->node rep)

         :else
         (throw (ex-info "Invalid input" {:input rep})))))

(defn tree
  "converts either a string or node representation into a tree
 
   (tree +content+)
   => [:html {:id \"3\"}
       [:head [:title \"First parse\"]]
       [:body [:p \"Parsed HTML into a doc.\"]]]"
  {:added "3.0"}
  ([rep]
   (cond (string? rep)
         (node->tree (parse rep))

         (instance? Node rep)
         (node->tree rep)

         (vector? rep) rep

         :else
         (throw (ex-info "Invalid input" {:input rep})))))

(defprotocol IText
  (text [_]))

(extend-protocol IText
  Elements
  (text [e] (.text e))
  Element
  (text [e] (.text e)))

(defn select
  "applies css selector to node"
  {:added "3.0"}
  ([^Element node ^String query]
   (Selector/select query node)))

(defn select-first
  "gets the first match given selector and node"
  {:added "3.0"}
  ([^Element node ^String query]
   (Selector/selectFirst query node)))
