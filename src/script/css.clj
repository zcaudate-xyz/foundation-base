(ns script.css
  (:require [clojure.string]
            [garden.core :as garden]
            [std.object :as object]
            [std.string.common :as common])
  (:import (com.steadystate.css.parser CSSOMParser SelectorListImpl) (com.steadystate.css.dom CSSStyleRuleImpl CSSStyleDeclarationImpl CSSStyleSheetImpl CSSRuleListImpl Property) (org.w3c.dom.css CSSRule CSSStyleSheet CSSStyleRule) (org.w3c.css.sac InputSource) (java.io StringReader)))

(def +parser+ (CSSOMParser.))

(defn generate-style
  "creates a style string
 
   (generate-style {:bold true})
   => \"bold: true;\""
  {:added "3.0"}
  ([m]
   (garden/style m)))

(defn generate-css
  "creates a stylesheet
 
   (generate-css [[:node {:bold true
                          :color \"black\"}]
                  [:h1  {:align :left}]])
   => \"node {\\n  bold: true;\\n  color: black;\\n}\\nh1 {\\n  align: left;\\n}\""
  {:added "3.0"}
  ([v]
   (common/joinl (map garden/css v) "\n")))

(defn- to-string
  [s]
  (cond (string? s) s
        :else (.toString ^Property s)))

(defn parse-pair
  "parses a style pair
 
   (parse-pair \"bold: true\")
   => [:bold \"true\"]"
  {:added "3.0"}
  ([text]
   (let [props (-> text to-string (clojure.string/split #": "))]
     (-> props
         (update-in [0] (comp keyword clojure.string/trim))))))

(defn parse-rule
  "helper function for parse-css"
  {:added "3.0"}
  ([^CSSStyleRuleImpl rule]
   (let [k  (->> ^SelectorListImpl (.getSelectors rule)
                 (.getSelectors)
                 (map str))
         k  (if (= 1 (count k))
              (-> k first keyword)
              (clojure.string/join " " k))
         v  (->> (.getProperties ^CSSStyleDeclarationImpl (.getStyle rule))
                 (map parse-pair)
                 (into {}))]

     [k v])))

(defn parse-css
  "reads a stylesheet entry from a string
 
   (parse-css \"node {\\n  bold: true;\\n  color: black;\\n}\\nh1 {\\n  align: left;\\n}\")
   => [[:node {:bold \"true\", :color \"black\"}]
       [:h1 {:align \"left\"}]]"
  {:added "3.0"}
  ([s]
   (let [source  (InputSource. (StringReader. s))
         ^CSSStyleSheetImpl stylesheet (.parseStyleSheet ^CSSOMParser +parser+ source nil nil)
         rule-list (.getRules ^CSSRuleListImpl (.getCssRules stylesheet))]
     (mapv parse-rule rule-list))))

(defn parse-style
  "reads style map from string
 
   (parse-style \"bold: true;\")
   => {:bold \"true\"}"
  {:added "3.0"}
  ([s]
   (let [source  (InputSource. (StringReader. s))
         ^CSSStyleDeclarationImpl style  (.parseStyleDeclaration ^CSSOMParser +parser+ source)]
     (->> (.getProperties style)
          (map parse-pair)
          (into {})))))

