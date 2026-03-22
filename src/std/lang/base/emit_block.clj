(ns std.lang.base.emit-block
  (:require [clojure.string]
            [std.lang.base.emit-common :as common]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]))

;;
;; BLOCK BODY
;;

(defn emit-statement
  "emits a statement given grammar"
  {:added "3.0"}
  ([form grammar mopts]
   (let [[_ type] (common/form-key form grammar)
         {:keys [statement]} (get-in grammar [:default :common])
         body (common/*emit-fn*  form grammar mopts)]
     (if (#{:block :free :fn :def} type)
       body
       (str body statement)))))

(defn emit-do
  "emits a do block"
  {:added "3.0"}
  ([args grammar mopts]
   (let [spacing (or (get-in grammar [:default :common :line-spacing]) 1)
         cols    (repeat spacing (common/newline-indent))
         lines   (apply str cols)]
     (clojure.string/join lines
               (filter (comp not empty?)
                       (map (fn [form] (emit-statement form grammar mopts))
                            args))))))

(defn emit-do*
  "like do but removes the statment at the end, useful for macros"
  {:added "3.0"}
  ([args grammar mopts]
   (let [out (emit-do args grammar mopts)
         statement (get-in grammar [:default :common :statement])]
     (if (clojure.string/ends-with? out statement)
       (f/lsubs out (count statement))
       out))))

(defn block-options
  "gets the block options"
  {:added "3.0"}
  ([key block segment grammar]
   (merge (get-in grammar [:default :common])
          (get-in grammar [:default :block segment])
          (get-in grammar [:block key segment])
          (get-in block   [segment]))))

(defn emit-block-body
  "helper to emit a block body"
  {:added "3.0"}
  ([key block args grammar mopts]
   (let [{:keys [start end append]} (block-options key block :body grammar)]
     (str start
          (common/with-indent [(get-in grammar [:default :common :indent])]
            (str (common/newline-indent)
                 (emit-do args grammar mopts)))
          (if (not append)
            (common/newline-indent))
          end))))

;;
;; BLOCK PARAMS
;;

(defn parse-params
  "parses params for a block"
  {:added "3.0"}
  ([params]
   (cond (vector? params)
         (if (some vector? params)
           (mapv (fn [x] [:statement (if (vector? x) x [x])]) params)
           [:statement params])

         :else
         [:raw params])))

(defn emit-params-statement
  "emits the params for statement"
  {:added "3.0"}
  ([key block args grammar mopts]
   (let [acc (loop [acc  []
                    curr []
                    [x & more :as args] args]
               (cond (empty? args) (conj acc curr)
                     (keyword? x) (recur (conj acc curr) [x] more)
                     :else (recur acc (conj curr x) more)))
         acc (remove empty? acc)
         {:keys [sep space]} (block-options key block :parameter grammar)]
     (clojure.string/join space (map (fn [[k & more]]
                            (let [[k args] (if (keyword? k)
                                             [(.replaceAll (f/strn k) "-" " ") more]
                                             [nil (cons k more)])]
                              (cond->> (common/emit-array args grammar mopts)
                                :then (map clojure.string/trimr)
                                :then (clojure.string/join (str sep space))
                                k  (str k space))))
                          acc)))))

(defn emit-params
  "constructs string to for loop args"
  {:added "3.0"}
  ([key block params grammar mopts]
   (let [parsed (parse-params params)
         {:keys [statement space start end]} (block-options key block :parameter grammar)
         pstr (case (first parsed)
                :raw (common/*emit-fn*  (second parsed) grammar mopts)
                :statement (emit-params-statement key block (second parsed) grammar mopts)
                (clojure.string/join (str statement space)
                          (map #(emit-params-statement key block % grammar mopts)
                               (map second parsed))))]
     (str start pstr end))))

;;
;; BLOCK CONTROLS
;;

(defn emit-block-control
  "emits a control form code"
  {:added "3.0"}
  ([ck {:keys [main] :as cblock} opts [tag & args] grammar mopts]
   (let [main (or main #{:parameter :body})
         [params body] (if (:parameter main)
                         [(first args) (rest args)]
                         [nil args])
         cblock (collection/merge-nested cblock opts)]
     (str (or (:raw opts) tag)
          (if (:parameter main)
            (emit-params ck cblock params grammar mopts))
          (if (:body main)
            (emit-block-body ck cblock body grammar mopts))))))

(defn emit-block-controls
  "emits control blocks for a form"
  {:added "3.0"}
  ([key block control ctl-args grammar mopts]
   (let [{:keys [begin append main]} (merge (get-in grammar [:default :block])
                                            block)
         sep (cond append " "
                   :else (common/newline-indent))]
     (->> (mapcat (fn [[ck {:keys [required] :as cblock}]]
                    (let [margs (get ctl-args ck)
                          _     (if (and required (empty? margs))
                                  (f/error "Form is required in body" {:main key :control ck}))
                          opts  (merge (get-in grammar [:default :block])
                                       (get-in grammar [:block key :control :default])
                                       (get-in grammar [:block key :control ck]))]
                      (map (fn [args]
                             (emit-block-control ck cblock opts args grammar mopts))
                           margs)))
                  control)
          (clojure.string/join sep)
          (str (if (empty? main) "" sep))))))

;;
;; BLOCK
;;

(defn emit-block-setup
  "parses main and control blocks"
  {:added "4.0"}
  ([key {:keys [raw main control] :as block} [tag & args] grammar mopts]
   (let [main (or main #{:parameter :body})
         [params args] (if (:parameter main)
                         [(first args) (rest args)]
                         [nil args])
         ctl       (collection/map-juxt [(comp symbol name) identity] (map first control))
         ctl-fn    (fn [form] (and (collection/form? form) (ctl (first form))))
         ctl-args  (->> (filter ctl-fn args)
                        (group-by (comp ctl first)))
         args      (remove ctl-fn args)
         raw   (or (get-in grammar [:block key :raw])
                   raw
                   tag)
         {:keys [start end append] :as wrap} (get-in grammar [:block key :wrap])]
     [raw params args ctl-args wrap])))

(defn emit-block-inner
  "returns the inner block"
  {:added "4.0"}
  ([key {:keys [main control] :as block} form grammar mopts]
   (let [[raw params args ctl-args wrap] (emit-block-setup key block form grammar mopts)
         {:keys [start end append]} (block-options key block :body grammar)]
     (str (:start wrap)
          raw
          (if (:parameter main)
            (emit-params key block params grammar mopts))
          start
          (clojure.string/trimr
           (common/with-indent [(get-in grammar [:default :common :indent])]
             (emit-block-controls key block control ctl-args grammar mopts)))
          (common/newline-indent)
          end
          (if (and wrap (not (:append wrap)))
            (common/newline-indent))
          
          (:end wrap)))))

(defn emit-block-standard
  "emits a generic block"
  {:added "3.0"}
  ([key {:keys [main control] :as block} form grammar mopts]
   (let [[raw params args ctl-args wrap] (emit-block-setup key block form grammar mopts)
         {:keys [start end append]} wrap]
     (str start
          raw
          (if (:parameter main)
            (emit-params key block params grammar mopts))
          (if (:body main)
            (emit-block-body key block args grammar mopts))
          (if control
            (emit-block-controls key block control ctl-args grammar mopts))
          (if (and wrap (not append)) (common/newline-indent))
          end))))

(defn emit-block
  "emits a minimal block expression"
  {:added "3.0"}
  ([key [sym & args :as form] {:keys [reserved] :as grammar} mopts]
   (let [{:keys [emit block macro] :as op} (get reserved sym)]
     (case emit
       :do    (emit-do  args grammar mopts)
       :do*   (emit-do* args grammar mopts)
       :macro (common/emit-macro key form grammar mopts)
       :inner (emit-block-inner key block form grammar mopts)
       (if (or (fn? emit) (var? emit))
         (emit form grammar mopts)
         (emit-block-standard key block form grammar mopts))))))


;;
;; TESTING
;;

(defn test-block-loop
  "emits with blocks"
  {:added "4.0"}
  [form grammar mopts]
  (common/emit-common-loop form
                           grammar
                           mopts
                           (assoc common/+emit-lookup+ :block emit-block)
                           common/emit-op))

(defn test-block-emit
  "emit with blocks"
  {:added "4.0"}
  [form grammar mopts]
  (binding [common/*emit-fn* test-block-loop]
    (test-block-loop form grammar mopts)))


(comment
  (./create-tests)
  )
