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

(defn.xt parse-xml-params
  "parses the args"
  {:added "4.0"}
  [s]
  (var params {})
  (var total (xt/x:str-len s))
  (var i 0)
  (while (< i total)
    (while (< i total)
      (var ch (xts/substring s i (+ i 1)))
      (if (or (== " " ch)
              (== "," ch))
        (:= i (+ i 1))
        (break)))
    (when (>= i total)
      (break))
    (var key-start i)
    (while (and (< i total)
                (not= "=" (xts/substring s i (+ i 1))))
      (:= i (+ i 1)))
    (when (>= i total)
      (break))
    (var key (xts/trim (xts/substring s key-start i)))
    (:= i (+ i 1))
    (while (and (< i total)
                (== " " (xts/substring s i (+ i 1))))
      (:= i (+ i 1)))
    (when (>= i total)
      (break))
    (var qchar (xts/substring s i (+ i 1)))
    (var value-start (+ i 1))
    (:= i value-start)
    (while (and (< i total)
                (not= qchar (xts/substring s i (+ i 1))))
      (:= i (+ i 1)))
    (xt/x:set-key params key (xts/substring s value-start i))
    (:= i (+ i 1)))
  (return params))

(defn.xt parse-xml-stack
  "parses the xml into a ast stack"
  {:added "4.0"}
  [s]
  (var output [])
  (var total (xt/x:str-len s))
  (var start 0)
  (while (< start total)
    (var ni start)
    (while (and (< ni total)
                (not= "<" (xts/substring s ni (+ ni 1))))
      (:= ni (+ ni 1)))
    (when (>= ni total)
      (break))
    (var j (+ ni 1))
    (while (and (< j total)
                (not= ">" (xts/substring s j (+ j 1))))
      (:= j (+ j 1)))
    (when (>= j total)
      (break))
    (var text (xts/trim (xts/substring s start ni)))
    (var inner (xts/trim (xts/substring s (+ ni 1) j)))
    (var close (xts/starts-with? inner "/"))
    (var empty (xts/ends-with? inner "/"))
    (when close
      (:= inner (xts/trim (xts/substring inner 1 (xt/x:str-len inner)))))
    (when empty
      (:= inner (xts/trim (xts/substring inner 0 (- (xt/x:str-len inner) 1)))))
    (var split 0)
    (var inner-total (xt/x:str-len inner))
    (while (and (< split inner-total)
                (not= " " (xts/substring inner split (+ split 1))))
      (:= split (+ split 1)))
    (var tag (xts/substring inner 0 split))
    (var params-str (:? (< split inner-total)
                        (xts/trim (xts/substring inner (+ split 1) inner-total))
                        ""))
    (var m {:tag tag})
    (when (< 0 (xt/x:str-len params-str))
      (xt/x:set-key m "params" (-/parse-xml-params params-str)))
    (when (< 0 (xt/x:str-len text))
      (xt/x:set-key m "text" text))
    (when close
      (xt/x:set-key m "close" true))
    (when empty
      (xt/x:set-key m "empty" true))
    (xt/x:arr-push output m)
    (:= start (+ j 1)))
  (return output))

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
                                   (:? (and (xt/x:is-object? v)
                                            (== "xml" (xt/x:get-key v "::/__type__")))
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
    (var etext (xt/x:get-key e "text"))
    (var etag (xt/x:get-key e "tag"))
    (var eparams (xt/x:get-key e "params"))
    (when (xt/x:not-nil? etext)
      (xt/x:arr-push (xt/x:get-key current "children") etext))
    (cond (== true (xt/x:get-key e "empty"))
          (do (var enode {:tag etag})
              (when (and (xt/x:not-nil? eparams)
                         (xtd/obj-not-empty? eparams))
                (xt/x:set-key enode "params" eparams))
              (xt/x:arr-push (xt/x:get-key current "children") enode))
          
          (not= true (xt/x:get-key e "close"))
          (do (var ncurrent {"::/__type__" "xml"
                             :parent current
                             :tag etag
                             :params eparams
                             :children []})
              (xt/x:arr-push (xt/x:get-key current "children") ncurrent)
              (:= current ncurrent))
          
          :else
          (:= current (xt/x:get-key current "parent"))))
  (return (-/to-node-normalise (xt/x:first (xt/x:get-key top "children")))))

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
    (xt/x:arr-assign arr (xt/x:arr-map children
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

(defn.xt to-string-value
  [v]
  (cond (xt/x:is-boolean? v)
        (return (:? v "true" "false"))

        :else
        (return (xt/x:to-string v))))

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
              (:= s (xt/x:cat s " " k "=" (-/to-string-value v))))
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
                             (-/to-string-value e))))))
  (return
   (xt/x:cat "<" tag (-/to-string-params params) ">"
             body
             "</" tag ">")))
