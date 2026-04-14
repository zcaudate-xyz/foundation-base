(ns xt.lang.util-xml
  (:require [std.lang :as l]))

;;
;; LUA
;;

(l/script :lua
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]]})

(defn.lua parse-xml-params
  "parses the args"
  {:added "4.0"}
  [s]
  (var params {})
  (string.gsub s "([%-%w]+)=([\"'])(.-)%2"
               (fn [w n a]
                 (xt/x:set-key params w a)))
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
    (local text  (xts/trim (string.sub s i (- ni 1))))
    (local params-str (xts/trim params))
    (var m {:tag tag})
    (when (< 0 (xt/x:str-len params-str))
      (xt/x:set-key m "params" (-/parse-xml-params params-str)))
    (when (< 0 (xt/x:str-len text))
      (xt/x:set-key m "text" text))
    (when (== c "/")
      (xt/x:set-key m "close" true))
    (when (== empty "/")
      (xt/x:set-key m "empty" true))
    (xt/x:arr-push output m)
    (:= i (+ j 1)))
  (return output))

;;
;; XTALK
;;

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]]})

(defabstract.xt parse-xml-params [s])

(defabstract.xt parse-xml-stack [s])

(defn.xt to-node-normalise
  "normalises the node for viewing"
  {:added "4.0"}
  [node]
  (var #{tag params children} node)
  (var out {:tag tag})
  (when (and (xt/x:not-nil? params)
             (xtd/obj-not-empty? params))
    (xt/x:set-key out "params" params))
  (when (xtd/arr-not-empty? children)
    (xt/x:set-key out "children"
                  (xt/x:arr-map children
                                (fn [v]
                                  (return
                                   (:? (== "xml" (. v ["::/__type__"]))
                                       (-/to-node-normalise v)
                                       v))))))
  (return out))

(defn.xt to-node
  "transforms stack to node"
  {:added "4.0"}
  [stack]
  (var top  {:tag "<TOP>"
             :children []})
  (var levels [top])
  (var current top)
  (xt/for:array [e stack]
    (when (. e text)
      (xt/x:arr-push (. current children) (. e text)))
    (cond (. e empty)
          (xt/x:arr-push (. current children) {:tag (. e tag)
                                               :params (. e params)})
          
          (not (. e close))
          (do (var ncurrent {"::/__type__" "xml"
                             :parent current
                             :tag (. e tag)
                             :params (. e params)
                             :children []})
              (xt/x:arr-push (. current children) ncurrent)
              (:= current ncurrent))
          
          :else
          (:= current (. current parent))))
  (return (-/to-node-normalise (xt/x:first (. top children)))))

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
  (when (and (xt/x:not-nil? params)
             (xtd/obj-not-empty? params))
    (xt/x:arr-push arr params))
  (when (xtd/arr-not-empty? children)
    (xt/x:arr-append arr (xt/x:arr-map children
                                        (fn [e]
                                          (return
                                          (:? (xt/x:is-object? e)
                                              (-/to-tree e)
                                              e))))))
  (return arr))

(defn.xt from-tree
  "creates nodes from tree"
  {:added "4.0"}
  [tree]
  (var count (xt/x:len tree))
  (var elem-fn (fn:> [e]
                 (:? (xt/x:is-array? e)
                     (-/from-tree e)
                     e)))
  (cond (== count 1)
        (return {:tag (xt/x:first tree)})
        
        (xt/x:is-object? (xt/x:second tree))
        (return {:tag (xt/x:first tree)
                 :params (xt/x:second tree)
                 :children (xt/x:arr-map (xt/x:arr-slice tree 2 (xt/x:len tree))
                                         elem-fn)})
        
        :else
        (return {:tag (xt/x:first tree)
                 :params {}
                 :children (xt/x:arr-map (xt/x:arr-slice tree 1 (xt/x:len tree))
                                         elem-fn)})))

(defn.xt to-brief
  "xml to a more readable form"
  {:added "4.0"}
  [node]
  (var #{children tag} node)
  (var sub-fn (fn:> [e]
                (:? (xt/x:is-object? e)
                    (-/to-brief e)
                    e)))
  (cond (xtd/arr-empty? children)
        (return {tag true})

        (< 2 (xt/x:len children))
        (do (var has-string (xt/x:arr-some children xt/x:is-string?))
            (var unique {})
            (xt/for:array [e children]
              (when (xt/x:is-object? e)
                (xt/x:set-key unique (xt/x:get-key e "tag") true)))
            (if (or has-string
                    (not= (xt/x:len (xt/x:obj-keys unique))
                          (xt/x:len children)))
              (return {tag (xt/x:arr-map children sub-fn)})
              (do (var out {})
                  (xt/for:array [e children]
                    (xt/x:obj-assign out (-/to-brief e)))
                  (return {tag out}))))
         
        :else
        (return
         {tag (sub-fn (xt/x:first children))})))

;;
;; TO STRING
;;

(defn.xt to-string-params
  "to node params"
  {:added "4.0"}
  [params]
  (cond (or (xt/x:nil? params)
            (xtd/obj-empty? params))
        (return "")

        :else
        (do (var s "")
            (xt/for:object [[k v] params]
              (:= s (xt/x:cat s " " k "=" v)))
            (return s))))

(defn.xt to-string
  "node to string"
  {:added "4.0"}
  [node]
  (var #{tag params children} node)
  (var body "")
  (when (xtd/arr-not-empty? children)
    (xt/for:array [e children]
      (:= body (xt/x:cat body
                         (:? (xt/x:is-object? e)
                             (-/to-string e)
                             (xt/x:to-string e))))))
  (return
   (xt/x:cat "<" tag (-/to-string-params params) ">"
             body
             "</" tag ">")))
