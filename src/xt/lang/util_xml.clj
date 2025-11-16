(ns xt.lang.util-xml
  (:require [std.lib :as h]
            [std.lang :as l]))

;;
;; LUA
;;

(l/script :lua
  {:require [[xt.lang.base-lib :as k]]})

(defn.lua parse-xml-params
  "parses the args"
  {:added "4.0"}
  [s]
  (var params {})
  (string.gsub s "([%-%w]+)=([\"'])(.-)%2"
               (fn [w n a]
                 (k/set-key params w a)))
  (return params))

(defn.lua parse-xml-stack
  "parses the xml into a ast stack"
  {:added "4.0"}
  [s]
  (var output [])
  (local '[ni c tag params empty])
  (local '[i j] '[1 1])
  
  (while true
    (:= '[ni j c tag params empty]
        (string.find s "<(%/?)([%w:]+)(.-)(%/?)>" i))
    (if (not ni) (break))
    (local text  (k/trim (string.sub s i (- ni 1))))
    (var m {:tag tag})
    (when (k/not-empty? params)
      (k/set-key m "params" (-/parse-xml-params params)))
    (when (k/not-empty? text)
      (k/set-key m "text" text))
    (when (== c "/")
      (k/set-key m "close" true))
    (when (== empty "/")
      (k/set-key m "empty" true))
    (x:arr-push output m)
    (:= i (+ j 1)))
  (return output))

;;
;; XTALK
;;

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defabstract.xt parse-xml-params [s])

(defabstract.xt parse-xml-stack [s])

(defn.xt to-node-normalise
  "normalises the node for viewing"
  {:added "4.0"}
  [node]
  (var #{tag params children} node)
  (return {:tag tag
           :params params
           :children (:? (k/not-empty? children)
                         (k/arr-map children
                                    (fn:> [v]
                                      (:? (== "xml" (. v ["::/__type__"]))
                                          (-/to-node-normalise v)
                                          v))))}))

(defn.xt to-node
  "transforms stack to node"
  {:added "4.0"}
  [stack]
  (var top  {:tag "<TOP>"
             :children []})
  (var levels [top])
  (var current top)
  (k/for:array [e stack]
    (when (. e text)
      (x:arr-push (. current children) (. e text)))
    (cond (. e empty)
          (x:arr-push (. current children) {:tag (. e tag)
                                            :params (. e params)})
          
          (not (. e close))
          (do (var ncurrent {"::/__type__" "xml"
                             :parent current
                             :tag (. e tag)
                             :params (. e params)
                             :children []})
              (x:arr-push (. current children) ncurrent)
              (:= current ncurrent))
          
          :else
          (:= current (. current parent))))
  (return (-/to-node-normalise (k/first (. top children)))))

(defn.xt parse-xml
  "parses xml"
  {:added "4.0"}
  [s]
  (return
   (-/to-node (-/parse-xml-stack s))))

(defn.xt to-tree
  "to node to tree"
  {:added "4.0"}
  [node]
  (var #{tag params children} node)
  (var arr [tag])
  (when (k/not-empty? params)
    (x:arr-push arr params))
  (when (k/not-empty? children)
    (k/arr-append arr (k/arr-map children
                                 (fn:> [e]
                                   (:? (k/obj? e)
                                       (-/to-tree e)
                                       e)))))
  (return arr))

(defn.xt from-tree
  "creates nodes from tree"
  {:added "4.0"}
  [tree]
  (var count (k/len tree))
  (var elem-fn (fn:> [e]
                 (:? (k/arr? e)
                     (-/from-tree e)
                     e)))
  (cond (== count 1)
        (return {:tag (k/first tree)})

        (k/obj? (k/second tree))
        (return {:tag (k/first tree)
                 :params (k/second tree)
                 :children (k/arr-map (k/arr-slice tree 2 (k/len tree))
                                      elem-fn)})

        :else
        (return {:tag (k/first tree)
                 :params {}
                 :children (k/arr-map (k/arr-slice tree 1 (k/len tree))
                                      elem-fn)})))

(defn.xt to-brief
  "xml to a more readable form"
  {:added "4.0"}
  [node]
  (var #{children tag} node)
  (var sub-fn (fn:> [e]
                (:? (k/obj? e)
                    (-/to-brief e)
                    e)))
  (cond (k/is-empty? children)
        (return {tag true})

        (< 2 (k/len children))
        (cond (or (k/arr-some children k/is-string?)
                  (not= (k/len (k/obj-keys (k/arr-juxt children
                                                       (k/key-fn "tag")
                                                       k/T)))
                        (k/len children)))
              (return {tag (k/arr-map children sub-fn)})

              :else
              (return {tag (k/arr-foldl (k/arr-map children -/to-brief)
                                            k/obj-assign
                                            {})})) 
        
        :else
        (return
         {tag (sub-fn (k/first children))})))

;;
;; TO STRING
;;

(defn.xt to-string-params
  "to node params"
  {:added "4.0"}
  [params]
  (cond (k/is-empty? params)
        (return "")

        :else
        (do (var s "")
            (k/for:object [[k v] params]
              (:= s (k/cat s " " k "=" v)))
            (return s))))

(defn.xt to-string
  "node to string"
  {:added "4.0"}
  [node]
  (var #{tag params children} node)
  (return
   (k/cat "<" tag (-/to-string-params params) ">"
          (k/arr-join (k/arr-map (or children [])
                                 (fn:> [e]
                                   (:? (k/obj? e)
                                       (-/to-string e)
                                       (k/to-string e))))
                      "")
          "</" tag ">")))

