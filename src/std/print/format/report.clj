(ns std.print.format.report
  (:require [std.print.format.common :as common]
            [std.string :as str]
            [std.print.ansi :as ansi]
            [std.lib :as h]
            [std.lib.result :as res]))

(defn lines:elements
  "layout an array of elements as a series of rows of a given length
 
   (lines:elements [\"A\" \"BC\" \"DEF\" \"GHIJ\" \"KLMNO\"]
                   {:align :left
                    :length 9}
                   0
                   1)
   => [\"[A BC DEF\"
       \" GHIJ    \"
       \" KLMNO]  \"]"
  {:added "3.0"}
  ([arr {:keys [align length]} padding spacing]
   (let [pad   (apply str (repeat padding common/+pad+))
         space (apply str (repeat spacing common/+space+))]
     (if (empty? arr)
       [(common/justify align (str pad "[]" pad) length)]
       (-> (reduce (fn [{:keys [current rows] :as out} row-item]
                     (let [s (str row-item)
                           l (count s)]
                       (cond (nil? current)
                             {:rows rows :current (str pad "[" s)}

                             (nil? row-item)
                             {:rows (conj rows (common/justify align
                                                               (str current "]")
                                                               length))}

                             (> (+ l spacing (count current))
                                length)
                             {:rows (conj rows (common/justify align
                                                               current
                                                               length))
                              :current (str pad " " s)}

                             :else
                             {:rows rows
                              :current (str current space s)})))
                   {:rows []}
                   (conj arr nil))
           (get :rows))))))

(defn lines:row-basic
  "layout raw elements based on alignment and length properties
 
   (lines:row-basic
    [\"hello\" :world [:a :b :c :d :e :f]]
    {:padding 0
     :spacing 1
     :columns [{:align :right :length 10}
               {:align :center :length 10}
               {:align :left :length 10}]})
   => [[\"     hello\"] [\"  :world  \"] [\"[:a :b :c \"
                                     \" :d :e :f]\"]]"
  {:added "3.0"}
  ([row {:keys [padding spacing columns] :as params}]
   (let [pad   (apply str (repeat padding common/+pad+))
         prep-fn (fn [row-item {:keys [format align length] :as column}]
                   (let [row-item (cond (string? format)
                                        (clojure.core/format format row-item)
                                        
                                        (fn? format)
                                        (format row-item)
                                        
                                        :else
                                        row-item)]
                     (cond (sequential? row-item)
                           (lines:elements row-item column padding spacing)

                           (string? row-item)
                           (mapv (fn [s]
                                   (common/justify align (str pad s) length))
                                 (str/split-lines row-item))

                           :else
                           [(common/justify align (str pad row-item) length)])))]
     (mapv (fn [row-item column]
             (let [data (if (res/result? row-item)
                          (:data row-item)
                          row-item)]
               (prep-fn data column)))
           row columns))))

(defn lines:row
  "same as row-elements but allows for colors and results
 
   (lines:row [\"hello\" :world [:a :b :c :d]]
              {:padding 0
               :spacing 1
               :columns [{:align :right :length 10}
                         {:align :center :length 10}
                         {:align :left :length 10}]})
 
   => [[\"     hello\" \"          \"]
       [\"  :world  \" \"          \"]
      [\"[:a :b :c \" \" :d]      \"]]"
  {:added "3.0"}
  ([row {:keys [padding columns] :as params}]
   (let [pad      (apply str (repeat padding common/+pad+))
         
         elements (lines:row-basic row params)
         height   (if (not-empty elements)
                    (apply max (map count elements))
                    0)
         style    (fn [lines color]
                    (if color
                      (map #(ansi/style % color) lines)
                      lines))
         lines    (mapv (fn [s {:keys [align length color suppress] :as col} row-item]
                          (let [color (if (res/result? row-item)
                                        #{(:status row-item)}
                                        color)]
                            (-> (common/pad:lines s length height)
                                (style color))))
                        elements
                        columns
                        row)]
     lines)))

(defn report:header
  "prints a header for the row
 
   (report:header [:id :name :value]
                  {:padding 0
                   :spacing 1
                  :columns [{:align :right :length 10}
                             {:align :center :length 10}
                             {:align :left :length 10}]})"
  {:added "3.0"}
  ([keys {:keys [padding columns] :as params}]
   (let [pad    (common/pad padding)
         header (h/-> keys
                      (mapv (fn [title {:keys [align length]}]
                              (str pad
                                   (common/justify align
                                                   (name title)
                                                   length)))
                            %
                            columns)
                      (apply str %)
                      (ansi/style #{:bold}))]
     header)))

(defn report:row
  "prints a row to output
 
   (report:row [\"hello\" :world (res/result {:data [:a :b :c :d :e :f]
                                            :status :info})]
               {:padding 0
                :spacing 1
                :columns [{:align :right :length 10}
                          {:align :center :length 10}
                          {:align :left :length 10}]})
 
   => \"hello   :world   [34m[:a :b :c [0m\\n                      [34m :d :e :f][0m\""
  {:added "3.0" :tags #{:print}}
  ([row params]
   (->> (lines:row row params)
        (apply vector)
        (map (partial str/join " "))
        (str/join "\n"))))

(defn report:title
  "prints the title
 
   (report:title \"Hello World\")"
  {:added "3.0" :tags #{:print}}
  ([title]
   (let [line (apply str (repeat (count title) \-))]
     (ansi/style (format "\n%s\n%s\n%s" line title line)
                 #{:bold}))))

(defn report:bold
  "prints the subtitle
 
   (report:bold \"Hello Again\")"
  {:added "3.0"}
  ([text]
   (ansi/style text #{:bold})))

(defn report:column
  "prints the column
 
   (report:column [[:id.a {:data 100}] [:id.b {:data 200}]]
                  :data
                  #{})"
  {:added "3.0"}
  ([items name color]
   (let [key-len  (or (->> items (map (comp count str first)) sort last)
                      20)
         display {:padding 1
                  :spacing 1
                  :columns [{:id :key  :length (inc key-len) :align :left}
                            {:id name  :length 60 :color color}]}
         header (report:header [:key name] display)
         lines  (map (fn [[key m]]
                       (report:row [key (get m name)] display))
                     items)]
     ;;lines
     (->> (cons header lines)
          (str/join "\n")))))
