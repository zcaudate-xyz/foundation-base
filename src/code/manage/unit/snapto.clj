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

(defn unwrap-fact-block
  "returns the exact metadata prefix and inner fact block"
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

(defn fact-block?
  "checks if a block is a fact form, preserving reader/block structure"
  {:added "4.1"}
  ([block]
   (let [{:keys [block]} (unwrap-fact-block block)
         children        (remove block/void? (block/children block))
         op              (first children)]
     (and (= :list (block/tag block))
          op
          (*test-forms* (block/value op))))))

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
           out   []]
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

(defn snap-form-string
  "formats a single fact form into snap-to layout"
  {:added "4.1"}
  ([form]
   (let [block                    (cond (block.base/block? form)
                                        form

                                        (string? form)
                                        (block/parse-first form)

                                        :else
                                        (block/block form))
         {:keys [prefix block]}    (unwrap-fact-block block)
         children                  (child-entries block)
         [op & more]               children
         [intro more]              (if (= :string (some-> (first more) entry-block block/tag))
                                     [(first more) (next more)]
                                     [nil more])
         items                     (parse-body more)
         head                      (str "(" (block/string (entry-block op))
                                         (when intro
                                           (str " " (block/string (entry-block intro)))))
         body                      (->> items
                                        (map render-item)
                                        (str/join "\n\n"))]
      (str prefix
           head
           (when (seq body)
             (str (if intro "\n\n" "\n")
                  body))
           ")"))))

(defn snap-block-string
  "formats a top-level test block when it is a fact form"
  {:added "4.1"}
  ([node]
   (if (fact-block? node)
     (snap-form-string node)
     (block/string node))))

(defn snapto-string
  "formats all top-level fact forms in a test file"
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
