(ns code.manage.unit.snapto
  (:require [clojure.string :as str]
            [code.framework :as base]
            [code.project :as project]
            [std.block :as block]
            [std.block.navigate :as nav]
            [std.lib.result :as res]
            [std.string.prose :as prose]
            [std.task :as task]))

(def ^:dynamic *test-forms*
  '#{fact})

(defn parse-body
  "partitions a fact body into plain forms and expression/check pairs"
  {:added "4.1"}
  ([forms]
   (loop [forms forms
          out   []]
     (cond
       (empty? forms)
       out

       :else
       (let [[expr arrow expected & more] forms]
         (cond
           (= '=> arrow)
           (recur more
                  (conj out {:type :check
                             :expr expr
                             :expected expected}))

           :else
           (recur (rest forms)
                  (conj out {:type :form
                             :expr expr}))))))))

(defn render-form
  "formats an arbitrary form"
  {:added "4.1"}
  ([form]
   (pr-str form)))

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
   (let [m          (meta form)
         [op & more] form
         [intro more] (if (string? (first more))
                        [(first more) (next more)]
                        [nil more])
         items      (parse-body more)
         blocks     (concat [(str "(" (name op)
                                   (when intro
                                     (str " " (pr-str intro))))]
                            (map render-item items))
         body       (str/join "\n\n" (rest blocks))]
     (str (when (seq m)
            (str "^" (pr-str m) "\n"))
          (first blocks)
          (when (seq body)
            (str "\n" body))
          ")"))))

(defn snap-block-string
  "formats a top-level test block when it is a fact form"
  {:added "4.1"}
  ([node]
   (let [form (read-string (block/string node))]
     (if (and (seq? form)
              (*test-forms* (first form)))
       (snap-form-string form)
       (block/string node)))))

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
