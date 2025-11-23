(ns code.file-task
  (:require [std.task :as task]
            [std.lib :as h]
            [std.fs :as fs]
            [std.text.diff :as diff]
            [std.print.ansi :as ansi]
            [std.lib.result :as res]
            [code.heal.level :as level]))

(defmethod task/task-defaults :file.transform
  ([_]
   {:construct {:input   (fn [_] :list)
                :lookup  (fn [_] {})
                :env     (fn [_] 
                           {:root "."
                            :target ""
                            :include [fs/file?]})}
    :params    {:print {:item true
                        :result true
                        :summary true}
                :return :summary}
    :arglists '([] [path] [path params] [path params lookup] [path params lookup env])
    :main      {:arglists '([] [key] [key params] [key params lookup] [key params lookup env])
                :count 4}
    :item      {:list    (fn [_ env]
                           (let [root-path (fs/path (:root env) (:target env))]
                             (->> (fs/list root-path env)
                                  (keys)
                                  (sort)
                                  (map #(str (fs/relativize root-path %))))))
                :display (fn [data]
                           (if (or (:deletes data)
                                   (:inserts data))
                             (select-keys data [:deletes
                                                :inserts
                                                :verified])
                             (res/result {:status :info
                                          :data :no-change})))}
    :result    {:keys {:inserts :inserts
                       :deletes :deletes
                       :verified :verified}
                :columns [{:key    :key
                           :align  :left}
                          {:key    :inserts
                           :length 8
                           :align  :center
                           :color  #{:bold}}
                          {:key    :deletes
                           :length 8
                           :align  :center
                           :color  #{:bold}}
                          {:key    :verified
                           :length 30
                           :align  :left
                           :color  #{:bold}}]
                :ignore (fn [{:keys [deletes inserts verified] :as m}]
                          (and (true? verified)
                               (zero? (+ (or inserts 0)
                                         (or deletes 0)))))}
    :summary  {:aggregate {:deletes   [:deletes + 0]
                           :inserts   [:inserts + 0]
                           :written   [:updated #(if %2 (inc %1) %1) 0]}}}))

(defmethod task/task-defaults :file.extract
  ([_]
   {:construct {:input   (fn [_] :list)
                :lookup  (fn [_] {})
                :env     (fn [_] 
                           {:root "."
                            :target ""
                            :include [fs/file?]})}
    :params    {:print {:item true
                        :result true
                        :summary true}}
    :arglists '([] [path] [path params] [path params lookup] [path params lookup env])
    :main      {:arglists '([] [key] [key params] [key params lookup] [key params lookup env])
                :count 4}
    :item      {:list    (fn [_ env]
                           (let [root-path (fs/path (:root env) (:target env))]
                             (->> (fs/list root-path env)
                                  (keys)
                                  (sort)
                                  (map #(str (fs/relativize root-path %))))))
                :display (fn [data]
                           (get-in data [:data :ns]))}
    :result    {:keys    {:ns    (fn [m]
                                   (get-in m [:data :ns]))
                          :deps  (fn [m] (sort (vec (get-in m [:data :deps]))))}
                :columns [{:key    :ns
                           :length 80
                           :align  :left}
                          {:key    :deps
                           :length 80
                           :align  :right
                           :color  #{:bold}}]}}))

(defn process-file
  "transforms the code and performs a diff to see what has changed
 
   ;; options include :skip, :full and :write
   (project/in-context (transform-code 'code.framework {:transform identity}))
   => (contains {:changed []
                 :updated false
                 :path any})"
  {:added "3.0"}
  ([path {:keys [write print process verify full] :as params} _ {:keys [root target]}]
   (let [full-path  (fs/path root target path)
         original   (slurp full-path)
         processed  (process original)]
     processed)))

(defn transform-file
  "transforms the code and performs a diff to see what has changed
 
   ;; options include :skip, :full and :write
   (project/in-context (transform-code 'code.framework {:transform identity}))
   => (contains {:changed []
                 :updated false
                 :path any})"
  {:added "3.0"}
  ([path {:keys [write print transform verify full] :as params} _ {:keys [root target]}]
   (let [full-path  (fs/path root target path)
         original   (slurp full-path)
         revised    (transform original)
         verified   (or (first (keep
                                (fn [[k f]]
                                  (try
                                    (f revised)
                                    nil
                                    (catch Throwable t
                                      (h/prn t)
                                      [k false])))
                                verify))
                        true)
         deltas     (diff/diff original revised)
         updated    (when (and write (seq deltas))
                      (spit full-path revised)
                      true)
         _          (when (and (:function print) (seq deltas))
                      (h/local :print (str "\n" (ansi/style path #{:bold :blue :underline}) "\n\n"))
                      (h/local :print (diff/->string deltas) "\n"))]
     (cond-> (diff/summary deltas)
       full  (assoc :deltas deltas)
       :then (assoc :updated (boolean updated)
                    :verified verified
                    :path (str full-path))))))

(h/definvoke file-transform
  "processes all files in a directory"
  {:added "4.0"}
  [:task {:template :file.transform
          :params {:title (fn [params env]
                            (str (:name params) " - " (fs/path (:root env))))}
          :main {:fn #'transform-file}}])

(h/definvoke file-process
  "processes all files in a directory"
  {:added "4.0"}
  [:task {:template :file.extract
          :params {:title (fn [params env]
                            (str (:name params) " - " (fs/path (:root env))))}
          :main {:fn #'process-file}}])

(defn get-deps
  [content]
  (let [nav      (code.edit/parse-root content)
        ns-name  (code.edit/value
                  (first (code.query/select nav
                                            '[(ns | _ & _)])))
        ns-deps  (set (map first
                           (:require (code.edit/value
                                      (first
                                       (code.query/select nav
                                                          ['(l/script & _)
                                                           '|
                                                           map?]))))))]
    {:ns ns-name
     :deps ns-deps}))



(comment
  

  (file-transform :all
                  {:transform level/heal-content
                   :verify {:read-string read-string
                            :block std.block/parse-root}
                   :name "HEAL CONTENT"
                   :write true
                   :print {:function true}}
                  nil
                  {:root "../Szncampaigncenter/"
                   :target  "src-translated/"
                   :include [".clj$"]
                   :recursive true})
  
  (def +deps+
    (file-process :all
                  {:process get-deps
                   :name "GET DEPS"
                   :item {:display (fn [data]
                                     (get data [:data :ns]))}}
                  nil
                  {:root "../Szncampaigncenter/"
                   :target  "src-translated/"
                   :include [".clj$"]
                   :recursive true}))
  
  (defn get-load-order
    [input-deps]
    (let [all-deps   (apply clojure.set/union
                            (map :deps (vals input-deps)))
          lu-ns      (h/map-vals :ns input-deps)
          lu-path    (h/transpose lu-ns)
          load-deps  (set (keys lu-path))
          sys-deps   (clojure.set/difference
                      all-deps load-deps)
          mdeps      (h/map-entries
                      (fn [[path {:keys [ns deps]}]]
                        [ns (clojure.set/difference
                             deps sys-deps)])
                      input-deps)
          ordered    (h/topological-sort-order-by-deps
                      mdeps
                      (h/topological-sort mdeps))]
      (mapv lu-path ordered)))
  
  (def +loaded+
    (doall
     (for [f (get-load-order +deps+)]
       (let [s (slurp (fs/path "../Szncampaigncenter/"
                               "src-translated/"
                               f))]
         (try
           [f (load-string s)]
           (catch Throwable t
             [f :failed]))))))
  

  [(count +loaded+)
   (count (filter (comp keyword? second) +loaded+))]

  [49 21]
  [49 21]
  
  (def all-deps
    (apply clojure.set/union
           (map :deps (vals +deps+))))

  (def sub-deps
    (set (map :ns (vals +deps+))))

  (def other-deps
    (clojure.set/difference
     all-deps sub-deps))
  
  (def recovered-deps
    (h/map-entries
     (fn [[path {:keys [ns deps]}]]
       [ns (clojure.set/difference
            deps other-deps)])
     +deps+))

  (h/topological-sort-order-by-deps
   recovered-deps
   (h/topological-sort recovered-deps))
  




  (defn process-links
    [content]
    (let [forms (map std.block/value
                     (filter std.block/expression?
                             (std.block/children
                              (std.block/parse-root
                               (slurp "../Szncampaigncenter/src-translated/lib/brands.clj")))))
          ns-form (first (filter #(and (list? %)
                                       (= (first %) 'ns)) forms))])))

(comment
  
  (map process-content
       (map slurp
            (keys 
             (fs/list "../Szncampaigncenter/src-translated/lib/"
                      {:include [fs/file?]})))
       )

  (map process-content
       (map slurp
            (keys 
             (fs/list "../Szncampaigncenter/src-translated/"
                      {:include [fs/file?]
                       :recursive true})))
       )
  
  
  (map std.block/value
       (filter std.block/expression?
               (std.block/children
                (std.block/parse-root
                 content))))
  (code.query/$ (code.edit/parse-root
                 (slurp "../Szncampaigncenter/src-translated/lib/brands.clj"))
      [{:is form?}]))


(code.query/select
 (code.edit/parse-root (slurp "../Szncampaigncenter/src-translated/lib/brands.clj"))
 '[(ns | _ & _)])

(code.query/select
 (code.edit/parse-root (prn-str
                        '(do (ns oeuoeu hoeue))))
 '[(ns | _ & _)]
 )

(map code.edit/value
     (code.query/select
      (code.edit/navigator (std.block/parse-root
                            (slurp "../Szncampaigncenter/src-translated/lib/brands.clj")))
      ['(ns | _ & _)
       ]))



(map code.edit/value
     (code.query/select
      (code.edit/navigator (std.block/parse-root
                            (slurp "../Szncampaigncenter/src-translated/lib/brands.clj")))
      ['ns
       '(:require & _)
       '|
       vector?]))

(map code.edit/value
     (code.query/select
      (code.edit/navigator (std.block/parse-root
                            (slurp "../Szncampaigncenter/src-translated/lib/brands.clj")))
      ['ns
       '(:require & _)
       '|
       vector?]))

(map code.edit/value
     (code.query/select
      (code.edit/navigator (std.block/parse-root
                            (slurp "../Szncampaigncenter/src-translated/lib/brands.clj")))
      ['l/script
       '|
       map?]))

(code.query/select (code.edit/parse-root
                    (pr-str '([1 2 3 4 5]))
                    )
                   [vector?
                    #_[_ _ | & _]])

(std.block/value
 (std.block/parse-root
  (slurp "../Szncampaigncenter/src-translated/lib/brands.clj")))





(comment
  (file-transform :all
                  {:transform level/heal-content
                   :verify {:read-string read-string
                            :block s/layout}
                   :name "HEAL CONTENT"
                   #_#_:write true
                   :print {:function true}}
                  nil
                  {:root "../Szncampaigncenter/"
                   :target  "src-translated/components"
                   :include [".clj$"]
                   #_#_:recursive true})
  
  (file-transform :all
                  {:transform level/heal-content
                   :verify {:read-string read-string
                            :block std.block/parse-root}
                   :name "HEAL CONTENT"
                   :write true
                   :print {:function true}}
                  nil
                  {:root "../Szncampaigncenter/"
                   :target  "src-translated/"
                   :include [".clj$"]
                   :recursive true}))





(comment
  ((level/wrap-print-diff level/heal-content)
 (slurp "../Szncampaigncenter/src-translated/components/token_creator.clj"))
  
  (szncampaigncenter.lib.brands/brands)
  
  (load-string
   (slurp "../Szncampaigncenter/src-translated/lib/brands.clj"))
  
  (s/layout
   (h/p (std.block/parse-root
         (slurp "../Szncampaigncenter/src-translated/lib/brands.clj"))))
  
  (s/layout ["\"hello\""])
  
  (std.block/value-string
   
   (std.block/info (s/layout "\"hello\""))
   {:type :token, :tag :string, :string "\"\"hello\"\"", :height 0, :width 9})
  
  (pr-str (s/layout "\"hello\""))
  )

(comment


  (code.manage/refactor-code
   :all
   {:title "HEALING"
    :transform identity}
   {:source-paths ["src-translated"]
    :root "../Szncampaigncenter/"})

  (code.manage/refactor-code
   :all
   {:title "HEALING"
    :transform code.heal/heal
    :no-analysis true}
   {:source-paths ["src-translated"]
    :root "../Szncampaigncenter/"})
  
  (code.manage/refactor-code
   :all
   {:title "HEALING"
    :no-analysis true}
   {:source-paths ["src-translated"]
    :root "../Szncampaigncenter/"})
  )

(comment
  (fs/list (fs/path "../Szncampaigncenter/src-dsl/"
                    ""))
  
  :result    {:keys    {:path :path
                        :params :params}
              :columns [{:key    :key
                         :align  :left}
                        {:key    :path
                         :align  :left
                         :length 60
                         :color  #{:green}}]}
  
  )

(comment
  ;; To run this example, you can load this namespace and then run:
  ;; (process-files "src")
  ;; or to process the current directory:
  ;; (process-files)
  


  (defmethod task/task-defaults :publish
    ([_]
     {:construct {:input    (fn [_] :list)
                  :lookup   (fn [_ project]
                              (executive/all-pages project))
                  :env      make-project}
      :params    {:print {:item true
                          :result true
                          :summary true}
                  :return :summary}
      :main      {:arglists '([] [key] [key params])
                  :count 2}
      :item      {:list     (fn [lookup _] (sort (keys lookup)))
                  :display  (fn [data] (format "%.2f s" (/ (:time data) 1000.0)))}
      :result    {:keys    {:path :path
                            :updated :updated}
                  :columns [{:key    :key
                             :align  :left}
                            {:key    :updated
                             :align  :left
                             :length 10
                             :color  #{:bold}}
                            {:key    :path
                             :align  :left
                             :length 60
                             :color  #{:green}}]}
      :summary  {:written   [:updated #(if %2 (inc %1) %1) 0]}})))
