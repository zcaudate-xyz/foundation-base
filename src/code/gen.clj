(ns code.gen
  (:require [code.edit :as nav]
            [std.block :as b]
            [clojure.set :as set]
            [std.lib :as h]))

(defn get-template-params
  [code]
  (->> (nav/navigator code)
       (iterate (fn [nav]
                  (nav/find-next nav
                                 (fn [block]
                                   (or (= :unquote-splice (:tag (std.block/info block)))
                                       (= :unquote (:tag (std.block/info block))))))))
       (drop 1)
       (take-while identity)
       (map nav/value)))

(defn get-template
  [code-str & [input-fn multi]]
  (let [code   (if multi
                 (b/parse-root code-str)
                 (b/parse-first code-str))
        params (get-template-params code)]
    {:code code
     :params params
     :input-fn (or input-fn identity)
     :multi multi}))

(defn fill-template
  [template input]
  (let [{:keys [code
                params
                input-fn]} template
        input (input-fn input)
        missing  (set/difference (set (map second params))
                                 (set (keys input)))
        _ (when (not-empty missing)
            (h/error "Missing params: " {:missing missing
                                         :input input}))]
    (nav/root-string
     (reduce (fn [nav param]
               
               (cond-> (nav/find-next-token nav param)
                 (= 'unquote (first param)) (nav/replace (get input (second param)))
                 (= 'unquote-splicing (first param)) (nav/replace-splice (get input (second param)))))
             (nav/navigator code)
             params))))





(comment
  (def PUBLIC_QUERY
    "
(defn.pg ^{:%% :sql
           :- ~return
           :api/meta ~meta-entry}
  ~sym 
  [] 
  (~name (szndb.core.fn-util/auth-uid) ~@hello))")
  
  (fill-template
   (get-template PUBLIC_QUERY)
   '{return :json
     meta-entry {:sb/grant :all}
     sym hello
     name hello-name
     hello (1 2 3)}))




(comment
  (-> (nav/parse-first
       PUBLIC_QUERY)
      (nav/find-next-token (list 'unquote 'name))
      (nav/replace 'hello-world)
      zip/step-outside-most
      zip/step-inside
      (nav/string))
  
  (-> (nav/parse-first
       PUBLIC_QUERY)
      (nav/find-next-token 'unquote)
      )


  (comment
    (->> (nav/parse-first PUBLIC_QUERY)
         (iterate (fn [nav]
                    (nav/find-next nav
                                   (fn [block]
                                     (= :unquote (:tag (std.block/info block)))))))
         (drop 1)
         (take-while identity)
         (map (comp nav/value nav/down))))
  
  
  
  (-> (nav/parse-first
       PUBLIC_QUERY)
      (nav/find-next (fn [block]
                       (= :unquote (:tag (std.block/info block)))))
      (nav/find-next (fn [block]
                       (= :unquote (:tag (std.block/info block)))))
      (nav/find-next (fn [block]
                       (= :unquote (:tag (std.block/info block)))))
      (nav/find-next (fn [block]
                       (= :unquote (:tag (std.block/info block)))))
      (nav/find-next (fn [block]
                       (= :unquote (:tag (std.block/info block))))))

  (nav/root-string)
  (nav/parse-string
   PUBLIC_QUERY)

  (def PUBLIC_QUERY
    "
(defn.pg ^{:%% :sql
           :- ~return
           :api/meta ~meta-entry}
  ~sym 
  [] 
  (~name (szndb.core.fn-util/auth-uid)))")




  (second
   (b/children
    (b/parse-root PUBLIC_QUERY)))
  )
