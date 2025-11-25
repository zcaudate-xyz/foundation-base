(ns std.block.editor-demo
  (:require [std.block.editor :as editor]
            [std.dom.common :as dom]))

(defn -main []
  (println "=== Structured Editor Demo ===")
  (let [ed (editor/create-editor "(+ 1 2)")]

    (println "\n--- Initial State ---")
    (println (dom/dom-format (editor/render-view ed)))

    (println "\n--- Move In (Select +) ---")
    (let [ed (editor/move-in ed)]
      (println (dom/dom-format (editor/render-view ed)))

      (println "\n--- Move Next (Select 1) ---")
      (let [ed (editor/move-next ed)]
        (println (dom/dom-format (editor/render-view ed)))

        (println "\n--- Wrap 1 in Vector ([1]) ---")
        (let [ed (editor/wrap ed :vector)]
          (println (dom/dom-format (editor/render-view ed)))

          (println "\n--- Move In (Select 1 inside vector) ---")
          (let [ed (editor/move-in ed)]
            (println (dom/dom-format (editor/render-view ed)))

            (println "\n--- Insert 99 ---")
            (let [ed (editor/insert-right ed 99)]
              (println (dom/dom-format (editor/render-view ed)))))))))))
