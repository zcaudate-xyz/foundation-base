(ns code.doc.link.number)

(def new-counter
  {:chapter 0
   :section 0
   :subsection 0
   :subsubsection 0
   :code 0
   :image 0
   :equation 0
   :citation 0})

(defn increment
  "increment string representation
 
   (increment 1)
   = \"A\"
 
   (increment \"1\")
   => \"2\""
  {:added "3.0"}
  ([count]
   (if (number? count)
     "A"
     (->> count
          first
          char
          int inc char str))))

(defn link-numbers-loop
  "iterates and assigns sequential numbers to elements, used as a helper for `link-numbers`"
  {:added "3.0"}
  ([elements auto-number]
   (link-numbers-loop elements auto-number new-counter []))
  ([[{:keys [type origin] :as ele} & more :as elements]
    auto-number
    {:keys [chapter section subsection subsubsection code image equation citation] :as counter}
    output]
   (if (empty? elements)
     output
     (let [[numstring counter]
           (case type
             :citation
             [(str (inc citation))
              (assoc counter :citation (inc citation))]

             :chapter
             [(str (inc chapter))
              (assoc counter
                     :chapter (if (number? chapter)
                                (inc chapter)
                                0)
                     :section 0 :subsection 0 :subsubsection 0 :code 0)]
             :section
             [(str chapter "." (inc section))
              (assoc counter :section (inc section) :subsection 0 :subsubsection 0)]

             :subsection
             [(str chapter "." section "." (inc subsection))
              (assoc counter :subsection (inc subsection) :subsubsection 0)]

             :subsubsection
             [(str chapter "." section "." subsection "." (inc subsubsection))
              (assoc counter :subsubsection (inc subsubsection))]

             :appendix
             [(str (increment chapter))
              (assoc counter
                     :chapter (increment chapter)
                     :section 0 :subsection 0 :subsubsection 0 :code 0)]

             (if (and (#{:code :image :equation :block} type)
                      (or (auto-number type)
                          (auto-number origin)
                          (:numbered ele))
                      (or (:tag ele) (:title ele))
                      (not (or (:hidden ele)
                               (false? (:numbered ele)))))
               (case type

                 :code
                 [(str chapter "." (inc code))
                  (assoc counter :code (inc code))]

                 :block
                 [(str chapter "." (inc code))
                  (assoc counter :code (inc code))]

                 :image
                 [(str (inc image))
                  (assoc counter :image (inc image))]

                 :equation
                 [(str (inc equation))
                  (assoc counter :equation (inc equation))])

               [nil counter]))
           ele (if numstring
                 (assoc ele :number numstring)
                 ele)]
       (recur more auto-number counter (conj output ele))))))

(defn link-numbers
  "creates numbers for each of the elements in the list"
  {:added "3.0"}
  ([interim name]
   (update-in interim [:articles name :elements]
              (fn [elements]
                (let [auto-number (->> (list (get-in interim [:articles name :meta :link :auto-number])
                                             (get-in interim [:meta :link :auto-number])
                                             true)
                                       (drop-while nil?)
                                       (first))
                      auto-number  (cond (set? auto-number) auto-number
                                         (false? auto-number) #{}
                                         (true? auto-number) #{:image :equation :code :block})]
                  (link-numbers-loop elements auto-number))))))
