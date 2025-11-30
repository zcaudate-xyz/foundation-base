(ns code.manage.unit
  (:require [code.framework :as base]
            [code.framework.docstring :as docstring]
            [code.manage.unit.scaffold :as scaffold]
            [code.manage.unit.walk :as walk]
            [std.lib.diff.seq :as diff.seq]
            [std.fs :as fs]
            [code.project :as project]
            [std.task :as task]
            [std.block :as block]
            [std.lib :as h]
            [std.string :as str]
            [std.lib.result :as res])
  (:refer-clojure :exclude [import]))

(defn import
  "imports unit tests as docstrings
 
   (project/in-context (import {:print {:function true}}))
   => map?"
  {:added "3.0"}
  ([ns params lookup project]
   (let [source-ns   (project/source-ns ns)
         test-ns     (project/test-ns ns)
         source-file (lookup source-ns)
         test-file   (lookup test-ns)
         params  (task/single-function-print params)]
     (cond (nil? source-file)
           (res/result {:status :error
                        :data :no-source-file})

           (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})

           :else
           (let [import-fn    (fn [nsp refers]
                                (fn [zloc]
                                  (docstring/insert-docstring zloc nsp refers)))
                 refers       (base/analyse test-ns params lookup project)
                 transform-fn (fn [text] (walk/walk-string text '_ refers import-fn))]
             (base/transform-code source-ns (assoc params :transform transform-fn) lookup project))))))

(defn purge
  "purge docstrings and meta from file
 
   (project/in-context (purge {:print {:function true}}))
   => map?"
  {:added "3.0"}
  ([ns params lookup project]
   (let [source-ns  (project/source-ns ns)
         source-file (lookup source-ns)
         params  (task/single-function-print params)]
     (cond (nil? source-file)
           (res/result {:status :info
                        :data :no-source-file})

           :else
           (let [purge-fn (fn [nsp references] identity)
                 transform-fn (fn [text] (walk/walk-string text '_ {} purge-fn))]
             (base/transform-code source-ns (assoc params :transform transform-fn) lookup project))))))

(defn missing
  "returns all functions missing unit tests
 
   (project/in-context (missing))"
  {:added "3.0"}
  ([ns params lookup project]
   (let [source-ns (project/source-ns ns)
         test-ns   (project/test-ns ns)
         source-file (lookup source-ns)
         test-file   (lookup test-ns)]
     (cond (nil? source-file)
           (res/result {:status :error
                        :data :no-source-file})

           :else
           (let [source-vars (if source-file (base/vars source-ns params lookup project))
                 test-vars   (if test-file   (base/vars test-ns params lookup project))]
             (vec (sort (h/difference (set source-vars) (set test-vars)))))))))

(defn todos
  "returns all unit tests with TODOs
 
   (project/in-context (todos))"
  {:added "3.0"}
  ([ns params lookup project]
   (let [source-ns (project/source-ns ns)
         test-ns   (project/test-ns ns)
         test-file (lookup test-ns)]
     (cond (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})
           
           :else
           (let [analysis  (base/analyse test-ns params lookup project)
                 entries (get analysis source-ns)]
             (vec (keep (fn [[var entry]]
                          (if (= "TODO" (second  (get-in entry [:test :sexp])))
                            (with-meta var (get-in entry [:test :line]))))
                        entries)))))))

(defn incomplete
  "returns functions with todos all missing tests
 
   (project/in-context (incomplete))"
  {:added "3.0"}
  ([ns params lookup project]
   (if (not (base/no-test ns params lookup project))
     (let [source-ns   (project/source-ns ns)
           test-ns     (project/test-ns ns)
           source-file (lookup source-ns)
           test-file   (lookup test-ns)]
       (-> (concat
            (if source-file (missing source-ns params lookup project))
            (if test-file   (todos test-ns params lookup project)))
           sort
           vec)))))

(defn orphaned-meta
  "returns true if meta satisfies the orphaned criteria
 
   (orphaned-meta {} {}) => false
 
   (orphaned-meta {:strict true}
                  {:meta {:adopt true} :ns 'clojure.core :var 'slurp})
   => true
 
   (orphaned-meta {:strict true}
                  {:meta {:adopt true} :ns 'clojure.core :var 'NONE})
   => false"
  {:added "3.0"}
  ([params m]
   (boolean (and (-> m :meta :adopt)
                 (or (not (:strict params))
                     (try
                       (require (:ns m))
                       (resolve (symbol (str (:ns m))
                                        (first (str/split (str (:var m)) #"\."))))
                       (catch Exception e)))))))

(defn orphaned
  "returns unit tests that do not have an associated function
 
   (project/in-context (orphaned))"
  {:added "3.0"}
  ([ns params lookup project]
   (let [source-ns (project/source-ns ns)
         test-ns (project/test-ns ns)
         source-file (lookup source-ns)
         test-file   (lookup test-ns)
         params (merge {:full true} params)]
     (cond (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})

           :else
           (let [source-vars (if source-file (base/vars source-ns params lookup project))
                 test-vars   (if test-file   (base/vars test-ns params lookup project))
                 orphaned    (h/difference (set test-vars) (set source-vars))
                 orphaned    (remove (comp (partial orphaned-meta params)
                                           meta)
                                     orphaned)]
             (mapv (fn [sym]
                     (if (= (namespace sym) (str source-ns))
                       (symbol (name sym))
                       sym))
                   (sort orphaned)))))))

(defn mark-vars
  "captures changed vars in a set
 
   (mark-vars '[a1 a2 a3 a4 a5]
              '[a1 a4 a3 a2 a5])
   => '[2 [a1 #{a2} #{a3} a4 a5]]"
  {:added "3.0"}
  ([vars comp-vars]
   (let [marked-set (->> (second (diff.seq/diff vars comp-vars))
                         (filter (comp #{:+} first))
                         (mapcat #(nth % 2))
                         set)
         results (mapv (fn [var]
                         (let [marked? (marked-set var)
                               sym (symbol (name var))]
                           (if marked?
                             (set [sym])
                             sym)))
                       vars)]
     [(count (filter set? results)) results])))

(defn in-order?
  "determines if the test code is in the same order as the source code
 
   (project/in-context (in-order?))"
  {:added "3.0"}
  ([ns params lookup project]
   (if (not (base/no-test ns params lookup project))
     (let [source-ns (project/source-ns ns)
           test-ns   (project/test-ns ns)
           source-file (lookup source-ns)
           test-file   (lookup test-ns)
           params (merge {:full true} params)]
       (cond (nil? (or test-file source-file))
             (res/result {:status :error
                          :data :invalid-namespace})

             (nil? test-file)
             (res/result {:status :error
                          :data :no-test-file})

             (nil? source-file)
             (res/result {:status :error
                          :data :no-source-file})

             :else
             (let [source-vars (base/vars source-ns params lookup project)
                   test-vars   (base/vars test-ns params lookup project)
                   orphaned    (h/difference (set test-vars) (set source-vars))
                   test-vars   (vec (remove orphaned test-vars))]
               (cond (= source-vars test-vars)
                     []

                     :else
                     (let [[count source-marked] (mark-vars source-vars test-vars)]
                       (if (pos? count)
                         [count source-marked (second (mark-vars test-vars source-vars))]
                         [])))))))))

(defn scaffold
  "creates a set of tests for a given source
 
   (project/in-context (scaffold))"
  {:added "3.0"}
  ([ns {:keys [write print] :as params} lookup project]
   (if (not (base/no-test ns params lookup project))
     (let [source-ns (project/source-ns ns)
           test-ns   (project/test-ns ns)
           source-file (lookup source-ns)
           test-file   (lookup test-ns)
           version (->> (str/split (:version project) #"\.")
                        (take 2)
                        (str/join "."))
           source-vars (if source-file (base/vars source-ns {:sorted false} lookup project))
           test-vars   (if test-file   (base/vars test-ns {:sorted false} lookup project))
           new-vars    (seq (remove (set test-vars) source-vars))
           params      (task/single-function-print params)]
       (cond (nil? source-file)
             (res/result {:status :error
                          :data :no-source-file})
             
             (empty? source-vars)
             (res/result {:status :info
                          :data :no-source-vars})

             :else
             (let [transform-fn (if test-file
                                  (fn [original] (scaffold/scaffold-append original source-ns new-vars version))
                                  (fn [_] (scaffold/scaffold-new source-ns test-ns source-vars version)))
                   [original test-file]  (if test-file
                                           [(slurp test-file) test-file]
                                           ["" (scaffold/new-filename test-ns project write)])
                   params (assoc params :transform transform-fn)
                   result (base/transform-code test-ns params (assoc lookup test-ns test-file) project)]
               (assoc result :new (vec new-vars))))))))

(defn arrange
  "arranges the test code to be in the same order as the source code
 
   (project/in-context (arrange))"
  {:added "3.0"}
  ([ns params lookup project]
   (if (not (base/no-test ns params lookup project))
     (let [ret (in-order? ns params lookup project)
           source-ns   (project/source-ns ns)
           test-ns     (project/test-ns ns)
           source-file (lookup source-ns)
           test-file   (lookup test-ns)
           params  (task/single-function-print params)]
       (cond (res/result? ret)
             ret

             (empty? ret)
             {:changes [] :updated false :path test-file}

             (nil? source-file)
             (res/result {:status :error
                          :data :no-source-file})

             (nil? test-file)
             (res/result {:status :error
                          :data :no-test-file})
             :else
             (let [source-vars  (base/vars source-ns {:sorted false} lookup project)
                   transform-fn (fn [original]
                                  (scaffold/scaffold-arrange original source-vars))]
               (base/transform-code test-ns (assoc params :transform transform-fn) lookup project)))))))

(defn create-tests
  "scaffolds and arranges the test file
 
   (project/in-context (create-tests))"
  {:added "3.0"}
  ([ns params lookup project]
   (if (not (base/no-test ns params lookup project))
     (let [new (scaffold ns params lookup project)]
       (arrange ns params lookup project)
       new))))

(defn unchecked
  "returns tests that does not contain a `=>`
 
   (project/in-context (unchecked))"
  {:added "3.0"}
  ([ns params lookup project]
   (if (not (base/no-test ns params lookup project))
       (let [source-ns (project/source-ns ns)
             test-ns   (project/test-ns ns)
             test-file (lookup test-ns)
             analysis  (base/analyse test-ns params lookup project)]
         (cond (res/result? analysis)
               analysis

               :else
               (let [entries (get analysis source-ns)]
                 (vec (keep (fn [[var entry]]
                              (if (and (->> (get-in entry [:test :form])
                                            (not= 'comment))
                                       (->> (get-in entry [:test :sexp])
                                            (flatten)
                                            (filter '#{=>})
                                            (empty?))
                                       (not (:unchecked (:meta entry))))
                                (with-meta var (get-in entry [:test :line]))))
                            entries))))))))

(defn commented
  "returns tests that are in a comment block
 
   (project/in-context (commented))"
  {:added "3.0"}
  ([ns params lookup project]
   (if (not (base/no-test ns params lookup project))
     (let [source-ns (project/source-ns ns)
           test-ns   (project/test-ns ns)
           analysis  (base/analyse test-ns params lookup project)]
       (cond (res/result? analysis)
             analysis

             :else
             (let [entries (get analysis source-ns)]
               (vec (keep (fn [[var entry]]
                            (if (->> (get-in entry [:test :form])
                                     (= 'comment))
                              (with-meta var (get-in entry [:test :line]))))
                          entries))))))))

(defn pedantic
  "returns all probable improvements on tests
 
   (project/in-context (pedantic))"
  {:added "3.0"}
  ([ns params lookup project]
   (if (not (base/no-test ns params lookup project))
     (let [source-ns (project/source-ns ns)
           tag (fn [k inputs]
                 (if-not (res/result? inputs)
                   (mapv (fn [sym]
                           (with-meta sym
                             (assoc (meta sym) :tag k)))
                         inputs)))]
       (-> (concat (tag :M (missing ns params lookup project))
                   (tag :T (todos ns params lookup project))
                   (tag :N (unchecked ns params lookup project))
                   (tag :C (commented ns params lookup project))))))))
