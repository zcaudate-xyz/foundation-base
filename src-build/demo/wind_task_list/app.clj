(ns demo.wind-task-list.app
  "Portable controller and view for the Wind task-list demonstration."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.ui.core :as ui]
             [xt.ui.state.core :as state]]})

(defn.xt make-controller
  "creates the task-list controller"
  []
  (return
   (state/controller-create
    {"items" [{"id" "task-1" "value" "Learn xt.ui"}
              {"id" "task-2" "value" "Run the Wind demo"}]
     "draft" ""
     "next" 3}
    {"set_draft"
     (fn [controller value _deps]
       (return
        (state/update-state!
         controller
         (fn [current]
           (return (xt/x:obj-assign current {"draft" (or value "")}))))))

     "add_item"
     (fn [controller _payload _deps]
       (return
        (state/update-state!
         controller
         (fn [current]
           (var draft (xt/x:str-trim (or (xt/x:get-key current "draft") "")))
           (when (== "" draft)
             (return current))
           (var next-id (or (xt/x:get-key current "next") 1))
           (var item {"id" (xt/x:cat "task-" (xt/x:to-string next-id))
                      "value" draft})
           (return
            (xt/x:obj-assign
             current
             {"items" (xt/x:arr-concat
                       (or (xt/x:get-key current "items") [])
                       [item])
              "draft" ""
              "next" (+ 1 next-id)}))))))

     "remove_item"
     (fn [controller item-id _deps]
       (return
        (state/update-state!
         controller
         (fn [current]
           (return
            (xt/x:obj-assign
             current
             {"items" (xt/x:arr-filter
                       (or (xt/x:get-key current "items") [])
                       (fn [item]
                         (return (not= item-id (xt/x:get-key item "id")))))}))))))}
    {}
    {})))

(defn.xt view
  "renders task-list state and actions as portable UI nodes"
  [current actions]
  (var items (or (xt/x:get-key current "items") []))
  (var draft (or (xt/x:get-key current "draft") ""))
  (var task-nodes
       (xtd/arr-map
        items
        (fn [item]
          (var item-id (xt/x:get-key item "id"))
          (return
           (ui/node
            "ui/row"
            {"key" item-id
             "class" "items-center justify-between gap-3 p-3 bg-slate-50 rounded-lg"}
            [(ui/text (xt/x:get-key item "value")
                      {"class" "flex-1 text-slate-800"})
             (ui/node
              "ui/button"
              {"class" "px-3 py-2 rounded-md bg-rose-600 hover:bg-rose-700 text-white"
               "aria_label" (xt/x:cat "Remove " (xt/x:get-key item "value"))
               "on_press" (fn [_]
                            (return ((xt/x:get-key actions "remove_item") item-id)))}
              [(ui/text "Remove" {"class" "text-white"})])])))))
  (when (== 0 (xt/x:len task-nodes))
    (:= task-nodes
        [(ui/node "ui/description"
                  {"value" "No tasks yet. Add one above."
                   "class" "p-4 text-center text-slate-500 bg-slate-50 rounded-lg"}
                  [])]))
  (return
   (ui/node
    "ui/column"
    {"class" "min-h-screen items-center justify-center p-6 bg-slate-100"}
    [(ui/node
      "ui/card"
      {"class" "w-full max-w-xl gap-5 p-6 bg-white rounded-2xl shadow-lg"}
      [(ui/node "ui/card-header" {"class" "gap-2"}
                [(ui/node "ui/title"
                          {"value" "xt.ui Wind Task List"
                           "class" "text-2xl font-bold text-slate-900"}
                          [])
                 (ui/node "ui/description"
                          {"value" "Portable XTalk state and UI rendered by Flutter WDynamic."
                           "class" "text-slate-600"}
                          [])])
       (ui/node
        "ui/card-content"
        {"class" "gap-4"}
        [(ui/node
          "ui/row"
          {"class" "items-center gap-3"}
          [(ui/node "ui/input"
                    {"value" draft
                     "placeholder" "Add a task"
                     "class" "flex-1 px-3 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                     "aria_label" "New task"
                     "on_change" (xt/x:get-key actions "set_draft")}
                    [])
           (ui/node "ui/button"
                    {"class" "px-4 py-3 rounded-lg bg-blue-600 hover:bg-blue-700 text-white disabled:opacity-50"
                     "disabled" (== "" (xt/x:str-trim draft))
                     "on_press" (xt/x:get-key actions "add_item")}
                    [(ui/text "Add" {"class" "text-white font-medium"})])])
         (ui/node "ui/column" {"class" "gap-2"} task-nodes)])])])))
