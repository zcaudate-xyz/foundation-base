(ns code.manage.unit.snapto
  (:require [clojure.string :as str]
            [std.block.base :as block.base]
            [code.framework :as base]
            [code.project :as project]
            [std.block :as block]
            [std.block.navigate :as nav]
            [std.lib.result :as res]
            [std.lib.zip :as zip]
            [std.string.prose :as prose]
            [std.task :as task]))

(def ^:dynamic *test-forms*
  '#{fact})

(def ^:dynamic *meta-tags*
  #{:meta :hash-meta})

(def ^:dynamic *ns-forms*
  '#{ns})

(def ^:dynamic *script-forms*
  '#{l/script l/script-})

(def ^:dynamic *script-option-order*
  [:runtime
   :config
   :layout
   :emit
   :lang
   :context
   :module
   :namespace
   :id
   :require
   :import
   :macro-only
   :bundle
   :file
   :export
   :static])

(defn unwrap-fact-block
  "returns the exact metadata prefix and inner block"
  {:added "4.1"}
  ([block]
   (loop [prefix ""
          block  block]
     (if (*meta-tags* (block/tag block))
       (let [children   (block/children block)
             expr       (last (filter block/expression? children))
             block-str  (block/string block)
             expr-str   (block/string expr)
             prefix-str (subs block-str 0 (- (count block-str)
                                             (count expr-str)))]
         (recur (str prefix prefix-str) expr))
       {:prefix prefix
        :block  block}))))

(defn spaces
  "returns n spaces"
  {:added "4.1"}
  ([n]
   (apply str (repeat n " "))))

(defn form-op
  "returns the top-level operator of a list form"
  {:added "4.1"}
  ([block]
   (let [{:keys [block]} (unwrap-fact-block block)
         children        (remove block/void? (block/children block))
         op              (first children)]
     (when (and (= :list (block/tag block))
                op)
       (block/value op)))))

(defn fact-block?
  "checks if a block is a fact form, preserving reader/block structure"
  {:added "4.1"}
  ([block]
   (*test-forms* (form-op block))))

(defn ns-block?
  "checks if a block is an ns form"
  {:added "4.1"}
  ([block]
   (*ns-forms* (form-op block))))

(defn script-block?
  "checks if a block is an l/script* form"
  {:added "4.1"}
  ([block]
   (*script-forms* (form-op block))))

(defn leading-indent
  "counts leading spaces and tabs in a line"
  {:added "4.1"}
  ([^String line]
   (loop [i 0]
     (if (and (< i (count line))
              (#{\space \tab} (.charAt line i)))
       (recur (inc i))
       i))))

(defn trim-indent
  "removes up to n leading spaces/tabs from a line"
  {:added "4.1"}
  ([^String line n]
   (let [limit (min n (leading-indent line))]
     (subs line limit))))

(defn normalise-block-string
  "removes the parent indentation from the rest of a block string"
  {:added "4.1"}
  ([^String s]
   (normalise-block-string s 0))
  ([^String s indent]
   (let [[head & rest] (str/split-lines s)]
     (if (empty? rest)
       s
       (str/join "\n"
                 (cons head
                       (map (fn [line]
                              (if (str/blank? line)
                                line
                                (trim-indent line indent)))
                            rest)))))))

(defn child-entries
  "returns non-void child blocks together with their starting column"
  {:added "4.1"}
  ([block]
   (->> (-> block nav/navigator nav/down)
        (iterate zip/step-right)
        (take-while zip/get)
        (map (fn [z]
               {:block (nav/block z)
                :col   (-> z nav/line-info :col)}))
        (remove (comp block/void? :block)))))

(defn entry-block
  "returns the block for an entry"
  {:added "4.1"}
  ([entry]
   (if (map? entry)
     (:block entry)
     entry)))

(defn entry-col
  "returns the starting column for an entry"
  {:added "4.1"}
  ([entry]
   (if (map? entry)
     (:col entry)
     1)))

(defn parse-body
  "partitions fact body blocks into plain forms and expression/check pairs"
  {:added "4.1"}
  ([blocks]
   (loop [blocks blocks
          out    []]
     (cond
       (empty? blocks)
       out

       :else
       (let [[expr arrow expected & more] blocks]
         (cond
           (and arrow
                (= :symbol (block/tag (entry-block arrow)))
                (= "=>" (block/string (entry-block arrow))))
           (recur more
                  (conj out {:type :check
                             :expr expr
                             :expected expected}))

           :else
           (recur (rest blocks)
                  (conj out {:type :form
                             :expr expr}))))))))

(defn render-form
  "formats an arbitrary block or form"
  {:added "4.1"}
  ([form]
   (let [block (entry-block form)
         col   (entry-col form)]
     (if (block.base/block? block)
       (normalise-block-string (block/string block)
                               (max 0 (dec col)))
       (pr-str form)))))

(defn render-prefixed-items
  "renders items with aligned continuation"
  {:added "4.1"}
  ([prefix items suffix separator]
   (render-prefixed-items prefix items suffix separator render-form))
  ([prefix items suffix separator render-item-fn]
   (if-let [items (seq items)]
      (let [align     (+ (count prefix)
                         (count separator))
            first-str (render-item-fn (first items))
            rest-strs (rest items)]
        (str prefix
             separator
             (prose/indent-rest first-str align)
             (when (seq rest-strs)
               (str "\n"
                    (->> rest-strs
                         (map (fn [item]
                                (str (spaces align)
                                     (prose/indent-rest (render-item-fn item)
                                                        align))))
                         (str/join "\n"))))
             suffix))
      (str prefix suffix))))

(defn parse-map-pairs
  "partitions map entries into key/value pairs"
  {:added "4.1"}
  ([entries]
   (loop [entries entries
          out     []
          index   0]
     (if-let [[k v & more] (seq entries)]
       (recur more
              (conj out {:index index
                         :key   k
                         :value v})
              (inc index))
       out))))

(defn script-option-rank
  "returns the sort rank for a script option"
  {:added "4.1"}
  ([entry]
   (let [value (some-> entry :key entry-block block/value)
         idx   (.indexOf *script-option-order* value)]
     (if (neg? idx)
       (+ 1000 (:index entry))
       idx))))

(defn render-script-value
  "formats a script option value"
  {:added "4.1"}
  ([key-entry value-entry]
   (let [key (some-> key-entry entry-block block/value)]
     (case key
       (:require :import)
       (let [block (entry-block value-entry)]
         (if (= :vector (block/tag block))
           (render-prefixed-items "[" (child-entries block) "]" "")
           (render-form value-entry)))

       (render-form value-entry)))))

(defn render-script-map-pair
  "formats a single script option"
  {:added "4.1"}
  ([key value prefix-indent]
   (let [key-str   (render-form key)
         value-str (render-script-value key value)]
     (str key-str
          " "
          (prose/indent-rest value-str
                             (+ prefix-indent
                                (count key-str)
                                1))))))

(defn render-script-map
  "formats a script config map"
  {:added "4.1"}
  ([form]
   (let [block (entry-block form)
         pairs (->> (child-entries block)
                    (parse-map-pairs)
                    (sort-by (juxt script-option-rank :index)))]
     (if-let [pairs (seq pairs)]
       (str "{"
            (render-script-map-pair (:key (first pairs))
                                    (:value (first pairs))
                                    1)
            (when-let [rest-pairs (seq (rest pairs))]
              (str "\n"
                   (->> rest-pairs
                        (map (fn [{:keys [key value]}]
                               (str " "
                                    (render-script-map-pair key value 1))))
                        (str/join "\n"))))
            "}")
       "{}"))))

(defn render-ns-clause
  "formats a single ns clause"
  {:added "4.1"}
  ([form]
   (let [block      (entry-block form)
         children   (child-entries block)
         [op & more] children]
     (if op
       (let [op-block (entry-block op)
             prefix   (str "(" (block/string op-block))
             render-require-entry
             (let [width (->> more
                              (keep (fn [entry]
                                      (let [block (entry-block entry)]
                                        (when (= :vector (block/tag block))
                                          (some->> (first (child-entries block))
                                                   render-form
                                                   count)))))
                              (reduce max 0))]
               (fn [entry]
                 (let [block (entry-block entry)]
                   (if (= :vector (block/tag block))
                     (let [[lib & mods] (child-entries block)
                           lib-str       (some-> lib render-form)
                           mods-str      (seq (map render-form mods))]
                       (cond
                         (nil? lib-str)
                         (render-form entry)

                         mods-str
                         (str "[" lib-str
                              (spaces (+ 1 (- width (count lib-str))))
                              (str/join " " mods-str)
                              "]")

                         :else
                         (str "[" lib-str "]")))
                     (render-form entry)))))]
         (if (= :require (block/value op-block))
           (render-prefixed-items prefix more ")" " " render-require-entry)
           (render-prefixed-items prefix more ")" " ")))
       (render-form form)))))

(defn render-item
  "formats a plain form or expression/check pair"
  {:added "4.1"}
  ([{:keys [type expr expected]}]
   (let [expr-str (render-form expr)]
     (case type
       :form
       (prose/indent expr-str 2)

       :check
       (let [expected-str (render-form expected)]
         (str (prose/indent expr-str 2)
              "\n  => "
              (prose/indent-rest expected-str 5)))))))

(defn snap-ns-string
  "formats a single ns form into snap-to layout"
  {:added "4.1"}
  ([form]
   (let [block                  (cond (block.base/block? form)
                                      form

                                      (string? form)
                                      (block/parse-first form)

                                      :else
                                      (block/block form))
         {:keys [prefix block]}  (unwrap-fact-block block)
         children                (child-entries block)
         [op name & clauses]     children
         head                    (str "("
                                      (block/string (entry-block op))
                                      " "
                                      (render-form name))
         body                    (->> clauses
                                     (map (comp #(prose/indent % 2)
                                                render-ns-clause))
                                     (str/join "\n"))]
     (str prefix
          head
          (when (seq body)
            (str "\n" body))
          ")"))))

(defn snap-script-string
  "formats a single l/script* form into snap-to layout"
  {:added "4.1"}
  ([form]
   (let [block                  (cond (block.base/block? form)
                                      form

                                      (string? form)
                                      (block/parse-first form)

                                      :else
                                      (block/block form))
         {:keys [prefix block]}  (unwrap-fact-block block)
         children                (child-entries block)
         [op lang config & more] children
         head                    (str "("
                                      (block/string (entry-block op))
                                      " "
                                      (render-form lang))
         render-entry            (fn [entry]
                                   (let [block (entry-block entry)]
                                     (if (= :map (block/tag block))
                                       (render-script-map entry)
                                       (render-form entry))))
         body                    (->> (concat (when config [config]) more)
                                     (map (comp #(prose/indent % 2)
                                                render-entry))
                                     (str/join "\n"))]
     (str prefix
          head
          (when (seq body)
            (str "\n" body))
          ")"))))

(defn snap-form-string
  "formats a single fact form into snap-to layout"
  {:added "4.1"}
  ([form]
   (let [block                 (cond (block.base/block? form)
                                     form

                                     (string? form)
                                     (block/parse-first form)

                                     :else
                                     (block/block form))
         {:keys [prefix block]} (unwrap-fact-block block)
         children               (child-entries block)
         [op & more]            children
         [intro more]           (if (= :string (some-> (first more) entry-block block/tag))
                                  [(first more) (next more)]
                                  [nil more])
         items                  (parse-body more)
         head                   (str "(" (block/string (entry-block op))
                                     (when intro
                                       (str " " (block/string (entry-block intro)))))
         body                   (->> items
                                     (map render-item)
                                     (str/join "\n\n"))]
     (str prefix
          head
          (when (seq body)
            (str (if intro "\n\n" "\n")
                 body))
          ")"))))

(defn snap-block-string
  "formats supported top-level blocks into snap-to layout"
  {:added "4.1"}
  ([node]
   (cond
     (fact-block? node)
     (snap-form-string node)

     (ns-block? node)
     (snap-ns-string node)

     (script-block? node)
     (snap-script-string node)

     :else
     (block/string node))))

(defn snapto-string
  "formats supported top-level forms in a file"
  {:added "4.1"}
  ([original]
   (let [all-nodes (->> (nav/parse-root original)
                        (nav/down)
                        (iterate nav/right)
                        (take-while identity)
                        (map nav/block))]
     (->> all-nodes
          (map snap-block-string)
          (str/join "\n\n")))))

(defn snapto
  "formats fact tests into a consistent snap-to layout

   (project/in-context (snapto {:write false}))
   => map?"
  {:added "4.1"}
  ([ns params lookup project]
   (if (not (base/no-test ns params lookup project))
     (let [test-ns   (project/test-ns ns)
           test-file (lookup test-ns)
           params    (task/single-function-print params)]
       (cond
         (nil? test-file)
         (res/result {:status :error
                      :data :no-test-file})

         :else
         (base/transform-code test-ns
                              (assoc params :transform snapto-string)
                              lookup
                              project))))))
