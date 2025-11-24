(ns code.gen-test
  (:use code.test)
  (:require [code.gen :as gen]
            [std.block :as b]
            [std.string :as str]))

(def PUBLIC_QUERY
  (str/join-lines
   ["(defn.pg ^{:%% :sql"
    "           :- :json"
    "           :api/meta {:sb/grant :all}}"
    "  hello"
    "  []"
    "  (hello-name (szndb.core.fn-util/auth-uid) 1 2 3))"]))

(def TEMPLATE_QUERY
  (str/join-lines
   ["(defn.pg ^{:%% :sql"
    "           :- ~return"
    "           :api/meta ~meta-entry}"
    "  ~sym"
    "  []"
    "  (~name (szndb.core.fn-util/auth-uid) ~@hello))"]))

^{:refer code.gen/get-template-params :added "4.0"}
(fact "gets the template params in the query"
  ^:hidden
  
  (gen/get-template-params
   (b/parse-first TEMPLATE_QUERY))
  => '((unquote return)
       (unquote meta-entry)
       (unquote sym)
       (unquote name)
       (unquote-splicing hello)))

^{:refer code.gen/get-template :added "4.0"}
(fact "gets the template"

  (gen/get-template TEMPLATE_QUERY)
  => map?)

^{:refer code.gen/fill-template :added "4.0"}
(fact "fills out the template"
  ^:hidden
  
  (str/split-lines
   (gen/fill-template
    (gen/get-template TEMPLATE_QUERY)
    '{return :json
      meta-entry {:sb/grant :all}
      sym hello
      name hello-name
      hello (1 2 3)}))
  => ["(defn.pg ^{:%% :sql"
      "           :- :json"
      "           :api/meta {:sb/grant :all}}"
      "  hello"
      "  []"
      "  (hello-name (szndb.core.fn-util/auth-uid) 1 2 3))"])


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
