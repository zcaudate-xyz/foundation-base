(ns std.image.base.display.gradient
  (:require [std.string.common]))

(def ^:dynamic *default-ratio* 0.4)
(def ^:dynamic *default-total* 256)
(def ^:dynamic *default-len* 3)

(def ramp-dark (->> ["ÆÑÊŒØMÉËÈÃÂWQBÅæ#NÁþEÄÀHKRŽœXg"
                     "ÐêqÛŠÕÔA€ßpmãâG¶øðé8ÚÜ$ëdÙýèÓÞ"
                     "ÖåÿÒb¥FDñáZPäšÇàhû§ÝkŸ®S9žUTe6"
                     "µOyxÎ¾f4õ5ôú&aü2ùçw©Y£0VÍL±3ÏÌ"]
                    (std.string.common/joinl)))

(def ramp-light (->>  ["óC@nöòs¢u‰½¼‡zJƒ%¤Itocîrjv1lí="
                       "ïì<>i7†[¿?×}*{+()\\/»«•¬|!¡÷¦¯—"
                       "^ª„”“~³º²–°­¹‹›;:’‘‚’˜ˆ¸…·¨´`."
                       "                  "]
                      (std.string.common/joinl)))

(defn create-lookup
  "creates a lookup based on the range
 
   (-> (create-lookup \"01234\" 5 5)
       (str-vals))
   => {5 \"0\", 6 \"1\", 7 \"2\", 8 \"3\", 9 \"4\"}"
  {:added "3.0"}
  ([elems offset n]
   (let [total (count elems)
         frac  (/ total n)]
     (->> (for [i (range n)]
            [(+ i offset) (nth elems (int (* i frac)))])
          (into {})))))

(defn create-single
  "creates a single gradient map
 
   (-> (create-single)
       (select-keys [0 1 2 3])
       (str-vals))
   => {0 \"Æ\", 1 \"Ñ\", 2 \"Ê\", 3 \"Œ\"}"
  {:added "3.0"}
  ([] (create-single *default-ratio* *default-total*))
  ([ratio total]
   (let [midpoint (long (* ratio total))]
     (merge (create-lookup ramp-dark
                           0
                           midpoint)
            (create-lookup ramp-light
                           midpoint
                           (- total midpoint))))))

(defn create-multi
  "creates a multi gradient map
 
   (-> (create-multi 3)
       (select-keys [0 3 6])
       (str-vals))
   => {0 \"ÆÑÊ\", 3 \"ŒØM\", 6 \"ÉËÈ\"}"
  {:added "3.0"}
  ([len] (create-multi len *default-ratio* *default-total*))
  ([len ratio total]
   (let [midpoint (long (* ratio total))]
     (merge (create-lookup (partition len ramp-dark)
                           0
                           midpoint)
            (create-lookup (partition len ramp-light)
                           midpoint
                           (- total midpoint))))))

(def ^:dynamic *default-table*
  (create-multi *default-len*))

(defn lookup-char
  "look up a character based on value
 
   (str (lookup-char 0))
   => #{\"Æ\" \"Ñ\" \"Ê\"}
 
   (str (lookup-char 6))
   => #{\"É\" \"Ë\" \"È\"}"
  {:added "3.0"}
  ([n]
   (lookup-char *default-table* *default-len* n))
  ([table len n]
   (if (= len 1)
     (table n)
     (lookup-char table len n (rand-int len))))
  ([table len n i]
   (nth (table n) i)))
