(ns std.string.plural
  (:require [clojure.string]
            [std.lib.collection :as coll]
            [std.lib.foundation :as h]
            [std.string.common :as str]))

(def ^:dynamic *uncountable* (atom #{}))

(def ^:dynamic *singular-irregular* (atom {}))

(def ^:dynamic *plural-irregular* (atom {}))

(def ^:dynamic *plural-rules* (atom []))

(def ^:dynamic *singular-rules* (atom []))

(defonce +base-uncountable+
  ["air" "alcohol" "art" "blood" "butter" "cheese" "chewing" "coffee"
   "confusion" "cotton" "education" "electricity" "entertainment" "equipment"
   "experience" "fiction" "fish" "food" "forgiveness" "fresh" "gold" "gossip" "grass"
   "ground" "gum" "happiness" "history" "homework" "honey" "ice" "information" "jam"
   "knowledge" "lightning" "liquid" "literature" "love" "luck" "luggage" "meat" "milk"
   "mist" "moose" "money" "music" "news" "oil" "oxygen" "paper" "patience" "peanut" "pepper"
   "petrol" "pork" "power" "pressure" "research" "rice" "sadness" "series" "sheep"
   "shopping" "silver" "snow" "space" "species" "speed" "steam" "sugar" "sunshine" "tea"
   "tennis" "thunder" "time" "toothpaste" "traffic" "up" "vinegar" "washing" "wine"
   "wood" "wool"])

(defonce +base-irregular+
  [["amenity" "amenities"]
   ["child" "children"]
   ["cow" "kine"]
   ["foot" "feet"]
   ["goose" "geese"]
   ["louse" "lice"]
   ["mailman" "mailmen"]
   ["man" "men"]
   ["mouse" "mice"]
   ["move" "moves"]
   ["ox" "oxen"]
   ["person" "people"]
   ["sex" "sexes"]
   ["tooth" "teeth"]
   ["woman" "women"]])

(defonce +base-plural+
  [[#"(?i)$" "s"]
   [#"(?i)s$" "s"]
   [#"(?i)(ax|test)is$" "$1es"]
   [#"(?i)(octop|vir)us$" "$1i"]
   [#"(?i)(alias|status)$" "$1es"]
   [#"(?i)(bu)s$" "$1ses"]
   [#"(?i)(buffal|tomat)o$" "$1oes"]
   [#"(?i)([ti])um$" "$1a"]
   [#"(?i)sis$" "ses"]
   [#"(?i)(?:([^f])fe|([lr])f)$" "$1$2ves"]
   [#"(?i)(hive)$" "$1s"]
   [#"(?i)([^aeiouy]|qu)y$" "$1ies"]
   [#"(?i)(x|ch|ss|sh)$" "$1es"]
   [#"(?i)(matr|vert|ind)(?:ix|ex)$" "$1ices"]
   [#"(?i)([m|l])ouse$" "$1ice"]
   [#"(?i)^(ox)$" "$1en"]
   [#"(?i)(quiz)$" "$1zes"]])

(defonce +base-singular+
  [[#"(?i)s$" ""]
   [#"(?i)(ss)$" "$1"]
   [#"(?i)(n)ews$" "$1ews"]
   [#"(?i)([ti])a$" "$1um"]
   [#"(?i)((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)(sis|ses)$" "$1$2sis"]
   [#"(?i)(^analy)(sis|ses)$" "$1sis"]
   [#"(?i)([^f])ves$" "$1fe"]
   [#"(?i)(hive)s$" "$1"]
   [#"(?i)(tive)s$" "$1"]
   [#"(?i)([lr])ves$" "$1f"]
   [#"(?i)([^aeiouy]|qu)ies$" "$1y"]
   [#"(?i)(s)eries$" "$1eries"]
   [#"(?i)(m)ovies$" "$1ovie"]
   [#"(?i)(x|ch|ss|sh)es$" "$1"]
   [#"(?i)([m|l])ice$" "$1ouse"]
   [#"(?i)(bus)(es)?$" "$1"]
   [#"(?i)(o)es$" "$1"]
   [#"(?i)(shoe)s$" "$1"]
   [#"(?i)(cris|ax|test)(is|es)$" "$1is"]
   [#"(?i)(octop|vir)(us|i)$" "$1us"]
   [#"(?i)(alias|status)(es)?$" "$1"]
   [#"(?i)^(ox)en" "$1"]
   [#"(?i)(vert|ind)ices$" "$1ex"]
   [#"(?i)(matr)ices$" "$1ix"]
   [#"(?i)(quiz)zes$" "$1"]
   [#"(?i)(database)s$" "$1"]])

(defn uncountable?
  "checks if a word is uncountable
 
   (uncountable? \"gold\")
   => true
 
   (uncountable? \"moose\")
   => true"
  {:added "3.0"}
  ([^String word]
   (contains? @*uncountable* (.toLowerCase word))))

(defn irregular?
  "checks if a work is irregular
 
   (irregular? \"geese\")
   => true"
  {:added "3.0"}
  ([^String word]
   (or (contains? @*singular-irregular* (.toLowerCase word))
       (contains? @*plural-irregular* (.toLowerCase word)))))

(defn resolve-rules
  "goes through rules and checks if there is a matching pattern
 
   (resolve-rules @*singular-rules*
                  \"hello\")
   => nil
 
   (resolve-rules @*singular-rules*
                  \"hellos\")
   => \"hello\""
  {:added "3.0"}
  ([rules word]
   (->> rules
        (keep (fn [[pattern replacement]]
                (cond (string? pattern)
                      (if (= pattern word)
                        replacement)

                      :else
                      (if (re-find pattern word)
                        (clojure.string/replace word pattern replacement)))))
        (first))))

(defn singular
  "converts a word into its singular
 
   (singular \"rats\")
   => \"rat\"
 
   (singular \"geese\")
   => \"goose\""
  {:added "3.0"}
  ([s]
   (cond (uncountable? s)
         s

         :else
         (or (@*singular-irregular* s)
             (resolve-rules @*singular-rules* s)
             s))))

(defn plural
  "converts a word into its plural
 
   (plural \"man\")
   => \"men\"
 
   (plural \"rate\")
   => \"rates\"
 
   (plural \"inventory\")
   => \"inventories\"
 
   (plural \"moose\")
   => \"moose\""
  {:added "3.0"}
  ([s]
   (cond (uncountable? s)
         s

         :else
         (or (@*plural-irregular* s)
             (resolve-rules @*plural-rules* s)
             s))))

(def ^:dynamic *initalized*
  (do (swap! *uncountable* into +base-uncountable+)
      (swap! *plural-irregular* into +base-irregular+)
      (swap! *singular-irregular* into (coll/transpose +base-irregular+))
      (swap! *plural-rules* into (reverse +base-plural+))
      (swap! *singular-rules* into (reverse +base-singular+))
      true))
