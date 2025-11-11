(ns code.dev.client.page-main
  (:require [std.lib :as h]
            [std.lang :as l]))

(def MainLayout
  [[:sidebar] [[:header]
               [:body] 
               [:footer]]])

(def *current-level* :vertical)

(defn classify-layout
  [])

(defn compile-layout
  [layout context]
  (cond (and (vector? layout)
             (first ))))

(defn compile-layout
  [layout props]
  (cond (vector? )))


(comment
  
  (compile-layout
   [[:section/actions]
    [:section/tasks]]
   
   {:ui/layout     {:section/actions
                    {:query    {:id :api/list-actions
                                :params []
                                :return :multi}
                     :display  'ActionComponent
                     :props    {:onUpdate :api/update-action
                                :onDelete :api/delete-action}}
                    :section/tasks
                    {:query    {:id :api/list-tasks
                                :return :multi}
                     :display  'TaskComponent
                     :props    {:onUpdate :update-task
                                :onDelete :delete-task}}}

    
    :ui/api        {:queries   {:api/list-actions  (fn [])
                                :api/list-tasks    (fn [])}
                    :mutations {:api/delete-action (fn [])}}
    :ui/controls   {:show   #{:sidemenu
                              :topmenu}
                    :inputs #{:action-type}}

    :ui/components {:sidebar 'PageSideBar
                    :header  'PageHeader
                    :footer  'PageFooter}}))


(comment
  (compile-layout
   [[:sidebar] [[:header]
                [:body
                 [:actions
                  {:query    {:id :list-actions
                              :return :multi}
                   :display  'ActionComponent}]
                 [:tasks
                  {:query    {:id :list-tasks
                              :return :multi}
                   :display  'TaskComponent}]] 
                [:footer]]]


   
   {:api        {:queries   {:list-actions  (fn [])}
                 :mutations {:delete-action (fn [])}}
    :controls   {:show   #{:sidemenu
                           :topmenu}}
    :components {:sidebar 'PageSideBar
                 :header  'PageHeader
                 :footer  'PageFooter}}))

(comment
  (compile-page
   [[:title]
    [:login-form]
    []]
   {:name       :login-page
    :api        {:queries   {:check-email   }
                 :mutations {:delete-action (fn [])}}
    :forms      {:login-form }
    :controls   {:show   #{:sidemenu
                           :topmenu}}
    :components {:title   'AuthTitle
                 :header  'LoginForm}})
  
  )


