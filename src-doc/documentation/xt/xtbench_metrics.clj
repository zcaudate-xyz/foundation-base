(ns documentation.xtbench-metrics)

[[:hero {:title "xtbench metrics"
         :subtitle "Runtime parity and specification coverage"
         :lead "Results are published by the XTBench GitHub Actions workflows and loaded directly from their metrics branch."}]]

[[:widget/js
  {:src "assets/js/widgets/xtbench.js"
   :class "xtbench-dashboard"
   :props {:data-url "https://raw.githubusercontent.com/zcaudate-xyz/foundation-base/xtbench-metrics/index.json"
           :limit 100}}]]
