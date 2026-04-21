(ns code.manage.unit.snapto
  (:require [clojure.string :as str]
            [std.block.base :as block.base]
            [code.framework :as base]
            [code.project :as project]
            [std.block :as block]
            [std.block.navigate :as nav]
            [std.lib.result :as res]
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

(defn normalise-block-string
  "removes shared indentation from the rest of a block string"
  {:added "4.1"}
  ([s]
   (let [[head & rest] (str/split-lines s)]
     (if (empty? rest)
       s
       (let [indent (->> rest
                         (remove str/blank?)
                         (map #(count (re-find #"^\s*" %)))
                         (reduce min ##Inf))
             indent (if (number? indent) indent 0)]
         (str/join "\n"
                   (cons head
                         (map (fn [line]
                                (if (str/blank? line)
                                  line
                                  (subs line (min indent (count line)))))
                              rest))))))))

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
                 (= :symbol (block/tag arrow))
                 (= "=>" (block/string arrow)))
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
   (if (block.base/block? form)
     (normalise-block-string (block/string form))
     (pr-str form))))

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
         children                  (remove block/void? (block/children block))
         [op & more]               children
         [intro more]              (if (= :string (some-> (first more) block/tag))
                                     [(first more) (next more)]
                                     [nil more])
         items                     (parse-body more)
         blocks                    (concat [(str "(" (block/string op)
                                                 (when intro
                                                   (str " " (block/string intro))))]
                                           (map render-item items))
          body       (str/join "\n\n" (rest blocks))]
      (str prefix
           (first blocks)
           (when (seq body)
             (str "\n" body))
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
