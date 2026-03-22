(ns code.doc.link.tag
  (:require [std.string.case :as case]))

(def tag-required?
  #{:chapter :section :subsection :subsubsection :appendix :citation})

(def tag-optional?
  #{:ns :reference :image :equation})

(defn inc-candidate
  "creates additional candidates
 
   (inc-candidate \"hello\")
   => \"hello-0\"
 
   (inc-candidate \"hello-10\")
   => \"hello-11\""
  {:added "3.0"}
  ([candidate]
   (if-let [[_ counter] (re-find #"-(\d+)$" candidate)]
     (let [len (count counter)
           num (Long/parseLong counter)]
       (str (subs candidate 0 (- (count candidate) len)) (inc num)))
     (str candidate "-0"))))

(defn tag-string
  "creates a tag based on string
 
   (tag-string \"hello2World/this.Rocks\")
   => \"hello2-world--this-rocks\""
  {:added "3.0"}
  ([^String s]
   (-> (case/spear-case s)
       (.replaceAll "\\." "-")
       (.replaceAll "/" "--")
       (.replaceAll "[^\\d^\\w^-]" ""))))

(defn create-candidate
  "creates a candidate based on element
 
   (create-candidate {:origin :ns
                      :ns \"code.doc\"})
   => \"ns-code-doc\""
  {:added "3.0"}
  ([{:keys [origin title type] :as element}]
   (cond origin
         (case origin
           :ns (tag-string (str "ns-" (:ns element)))
           :reference (tag-string (str (name (:mode element)) "-" (:refer element))))

         title
         (tag-string title)

         (= :image type)
         (tag-string (str "img-" (:src element))))))

(defn create-tag
  "creates a tag based on element and existing tags
 
   (create-tag {:title \"hello\"}
               (atom #{\"hello\" \"hello-0\" \"hello-1\" \"hello-2\"}))
   => {:title \"hello\", :tag \"hello-3\"}"
  {:added "3.0"}
  ([element tags]
   (create-tag element tags (create-candidate element)))
  ([element tags candidate]
   (cond (nil? candidate)
         element

         (get @tags candidate)
         (create-tag element tags (inc-candidate candidate))

         :else
         (do (swap! tags conj candidate)
             (assoc element :tag candidate)))))

(defn link-tags
  "link tags to all elements for a particular article"
  {:added "3.0"}
  ([{:keys [articles] :as interim} name]
   (let [tags (atom (get-in articles [name :tags]))]
     (let [auto-tag (->> (list (get-in articles [name :link :auto-tag])
                               (get-in interim [:meta :link :auto-tag])
                               true)
                         (drop-while nil?)
                         (first))
           auto-tag (cond (set? auto-tag) auto-tag
                          (false? auto-tag) #{}
                          (true? auto-tag) tag-optional?)]
       (->> (get-in articles [name :elements])
            (mapv (fn [element]
                    (cond (and (or (tag-required? (:type element))
                                   (auto-tag      (:type element))
                                   (auto-tag      (:origin element)))
                               (nil? (:tag element))
                               (not  (:hidden element)))
                          (create-tag element tags)

                          :else element)))
            (assoc-in interim [:articles name :elements]))))))
