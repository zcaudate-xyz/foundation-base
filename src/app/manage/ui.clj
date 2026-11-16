(ns app.manage.ui
  "Shared Clojure/XTalk management frontend. No project-specific graph or build simulation."
  (:require [lang.core :as l]))

(l/script :js {:import [["./ui-model.mjs" :as model]]})

(def.js state {"view" "pipeline" "verifyDraft" "" "trace" false "model" nil "file" nil
              "preview" nil "viewport" "desktop" "fileSequence" 0 "busy" false "withChecks" true "polling" false})

(defn.js escapeText [value]
  (return (. (String (or value "")) (replaceAll "&" "&amp;") (replaceAll "<" "&lt;")
             (replaceAll ">" "&gt;") (replaceAll "\"" "&quot;") (replaceAll "'" "&#39;"))))

(defn.js el [selector] (return (document.querySelector selector)))

(defn.js ^{:- [:async]} request [path options]
  (var response (await (fetch path (or options {}))))
  (var result (await (. response (json))))
  (when (not response.ok) (throw (new Error (or result.error "Request failed"))))
  (return result))

(defn.js statusLabel [status]
  (return (or (. {"passed" "Passed" "failed" "Failed" "blocked" "Blocked" "executed" "Executed" "stale" "Stale" "running" "Running" "ok" "Done" "error" "Failed" "queued" "Queued"} [status]) "Not run")))

(defn.js eventText [event]
  (return (+ (or event.target event.check "") " · "
             (:? (. event.type (endsWith "started")) "Started"
                 (+ (-/statusLabel event.status) " · " (or event.duration_ms (. event ["duration-ms"]) 0) " ms"))
             (:? event.message (+ " · " event.message) ""))))

(defn.js fileButton [file target kind]
  (return (+ "<button class=\"file-row\" data-file=\"" (-/escapeText file.path)
             "\" data-owner=\"" (-/escapeText target) "\" data-kind=\"" kind "\">"
             "<span>" (-/escapeText file.path) "</span><small>" file.bytes " B</small></button>")))

(defn.js renderInspector [data]
  (var target (model.selected (. -/state model)))
  (when (not target) (return "<p class=\"muted\">Select a target.</p>"))
  (return (+ "<div class=\"eyebrow\">TARGET</div><h2>" (-/escapeText target.label) "</h2>"
             "<code>:" (-/escapeText target.id) "</code>"
             "<h3>Prerequisites</h3><div class=\"chips\">"
             (:? target.depends.length
                 (. (. target.depends (map (fn [id] (return (+ "<button data-select=\"" (-/escapeText id) "\">:" (-/escapeText id) "</button>"))))) (join ""))
                 "<span class=\"muted\">None</span>") "</div>"
             "<h3>Source files <small>" (. target ["source-files"] length) "</small></h3>"
             (. (. target ["source-files"] (map (fn [file] (return (-/fileButton file target.id "source"))))) (join ""))
             "<h3>Generated files <small>" (. target ["output-files"] length) "</small></h3>"
             (. (. target ["output-files"] (map (fn [file] (return (-/fileButton file target.id "output"))))) (join ""))
             (:? (== (. target ["output-files"] length) 0) "<p class=\"muted\">No emitted files.</p>" "")
             "<div class=\"inspector-actions\"><button data-op=\"build\" data-target=\"" target.id "\">Build target</button>"
             "<button data-action=\"impact\">Trace impact</button></div>")))

(defn.js renderGraph [data]
  (var maxDepth (Math.max 0 (:.. (. data.project.targets (map (fn [target] (return target.depth)))))))
  (var impact (new Set (model.affected (. -/state model))))
  (var levels [])
  (var i 0)
  (for [(:= i 0) (<= i maxDepth) (:= i (+ i 1))]
    (var targets (. data.project.targets (filter (fn [target] (return (== target.depth i))))))
    (. levels (push (+ "<div class=\"graph-level\">"
       (. (. targets (map (fn [target]
         (var entry (or (. data ["target-status"] [target.id]) {}))
         (var status (or entry.status "idle"))
         (return (+ "<button class=\"target " status
                    (:? (and (. -/state trace) (. impact (has target.id))) " impacted" "")
                    (:? (and (. -/state trace) (not (. impact (has target.id)))) " dimmed" "")
                    "\" data-select=\"" target.id "\" data-node=\"" target.id
                    "\" aria-pressed=\"" (== target.id (. -/state model selected)) "\">"
                    "<strong>" (-/escapeText target.label) "</strong><code>:" target.id "</code>"
                    "<span class=\"node-status\">" (-/statusLabel status) "</span></button>"))))) (join ""))
       "</div>"))))
  (return (+ "<div class=\"graph\"><svg class=\"connections\" aria-hidden=\"true\"></svg>"
             (. levels (join "")) "</div><div class=\"graph-caption\">"
             (:? (. -/state trace) (+ "Affected targets: " (. (model.affected (. -/state model)) (join " → ")))
                 "Dependency graph from the project declaration") "</div>")))

(defn.js renderList [data kind]
  (return (. (. data.project.targets (map (fn [target]
    (var files (. target [(:? (== kind "sources") "source-files" "output-files")]))
    (return (+ "<section class=\"file-group\"><h3><button class=\"text-button\" data-select=\"" target.id "\">"
               (-/escapeText target.label) "</button><small>" files.length " files</small></h3>"
               (. (. files (map (fn [file] (return (-/fileButton file target.id kind))))) (join ""))
               (:? (not files.length) "<p class=\"muted\">No files.</p>" "") "</section>"))))) (join ""))))

(defn.js renderChecks [data]
  (return (+ "<h2>Declared checks</h2><p class=\"muted\">These execute the project's actual verification functions.</p>"
             (. (. data.project.checks (map (fn [check]
               (var entry (or (. data ["check-status"] [check.id]) {}))
               (var status (or entry.status "idle"))
               (return (+ "<section class=\"check-row\"><div><strong>" (-/escapeText check.id)
                          "</strong><code>Requires: " (. check.targets (join ", ")) "</code></div>"
                          "<span class=\"status " status "\">" (-/statusLabel status) "</span></section>"))))) (join ""))
             "<button data-op=\"test\">Run project checks</button>")))

(defn.js renderRuns [data]
  (return (+ "<h2>Run history</h2>"
             (:? (not data.runs.length) "<p class=\"muted\">No operations have run in this server session.</p>" "")
             (. (. data.runs (map (fn [run]
               (return (+ "<details class=\"run\"><summary><strong>" (-/escapeText run.operation)
                          "</strong><span class=\"status " run.status "\">" (-/statusLabel run.status) "</span>"
                          "<code>" (. run.id (slice 0 8)) "</code></summary><pre>"
                          (-/escapeText (JSON.stringify run.result nil 2)) "</pre>"
                          (:? run.stdout (+ "<pre>" (-/escapeText run.stdout) "</pre>") "") "</details>"))))) (join "")))))

(defn.js renderPages [data]
  (var pages (or data.project.pages []))
  (var saved (or (and data.verification data.verification.saved data.verification.saved.cases) {}))
  (var example (or (. (Object.values saved) [0]) {"page" (and (. pages [0]) (. pages [0] ["id"])) "kind" "resolve" "member" "choose-model" "inputs" {} "payload" {}}))
  (var draft (or (. -/state verifyDraft) (JSON.stringify example nil 2)))
  (return (+ "<h2>Pages &amp; models</h2>"
    "<section class=\"verification-workbench\"><h3>Verification workbench</h3><p>Resolve bindings for any definition. Live execution uses the project fixture adapter; unsupported operations are blocked. Load a saved check to try an ordered scenario.</p>"
    "<label>Execution request (JSON; optional expect: equals, contains, or error)<textarea id=\"verification-request\">" (-/escapeText draft) "</textarea></label>"
    "<button data-verification=\"verify-page\">Run / verify</button> <button data-verification=\"verify-definitions\">Check all definitions</button> <input id=\"verification-case-id\" placeholder=\"case-id\" aria-label=\"Case ID\"> <button data-verification=\"save-page-case\">Save case</button> <button data-verification=\"run-page-cases\">Run saved checks</button>"
    "<h3>Saved checks</h3>"
    (. (. (Object.keys (or (and data.verification data.verification.saved data.verification.saved.cases) {})) (map (fn [id] (return (+ "<button data-load-case=\"" (-/escapeText id) "\">" (-/escapeText id) "</button>"))))) (join " "))
    "<h3>Latest execution <span class=\"status\">" (-/escapeText (-/statusLabel (and data.verification data.verification.latest data.verification.latest.status))) "</span></h3>"
    (. (. (or (and data.verification data.verification.latest (or data.verification.latest.cases data.verification.latest.steps)) []) (map (fn [item]
      (return (+ "<details class=\"verification-result\"><summary>" (-/escapeText item.id) " · " (-/escapeText (-/statusLabel item.status)) "</summary><pre>" (-/escapeText (JSON.stringify item nil 2)) "</pre></details>"))))) (join ""))
    "<details><summary>Full execution, expected values, trace and cache</summary><pre>" (-/escapeText (JSON.stringify (and data.verification data.verification.latest) nil 2)) "</pre></details></section>"
    "<p class=\"muted\">Authored definitions from the application registry. Run a supported model above to inspect an execution snapshot.</p>"
    (. (. pages (map (fn [page]
      (return (+ "<details class=\"page-definition\"><summary><strong>" (-/escapeText page.id)
        "</strong> <code>" (-/escapeText (. (or page.routes []) (join ", "))) "</code> · " (-/escapeText page.area) "</summary>"
        "<p class=\"muted\">" (-/escapeText page.runtime) "</p>"
        (:? page.migration (+ "<p class=\"migration-status\">Migration group: <strong>" (-/escapeText page.migration.group)
          "</strong> · " (-/escapeText (-/statusLabel page.migration.status)) " · " page.migration.required " required capabilities · "
          (. page.migration ["native-contracts"]) " native contracts<p>" (-/escapeText page.migration.blocker) "</p>") "")
        "<h3>Models</h3>"
        (. (. (Object.entries (or page.models {})) (map (fn [pair]
          (return (+ "<details class=\"model-definition\"><summary>" (-/escapeText (. pair [0])) " · "
            (-/escapeText (or (. pair [1] ["operation"]) "local model")) "</summary><button data-verify-model=\"" (-/escapeText (. pair [0])) "\" data-verify-page=\"" (-/escapeText page.id) "\">Try model</button><pre>"
            (-/escapeText (JSON.stringify (. pair [1]) nil 2)) "</pre></details>"))))) (join ""))
        "<h3>Actions</h3><pre>" (-/escapeText (JSON.stringify (or page.actions {}) nil 2)) "</pre>"
        "<h3>Auth and SDK bindings</h3><pre>" (-/escapeText (JSON.stringify {"auth" (. page ["auth-actions"]) "sdk" (. page ["sdk-actions"])} nil 2))
        "</pre><button data-select=\"" (-/escapeText page.target) "\">Inspect build target</button></details>"))))) (join "")))))

(defn.js renderSlices [data]
  (return (. (. (or data.slices []) (map (fn [slice]
    (return (+ "<section class=\"slice\"><h2>" (-/escapeText slice.label) "</h2><p>" (-/escapeText slice.description) "</p>"
      "<button data-slice-build=\"" slice.id "\">Build slice</button> <button data-slice-copy=\"" slice.id "\">Copy agent context</button>"
      (. (. slice.stages (map (fn [stage]
        (return (+ "<article class=\"slice-stage\"><strong>" (-/escapeText stage.label) "</strong><span class=\"status\">"
          (-/escapeText stage.status) "</span><p>" (-/escapeText (or stage.blocker "")) "</p>"
          (:? (and stage.targets.length (not (. stage ["not-applicable"])))
            (+ "<button data-slice=\"" slice.id "\" data-stage=\"" stage.id "\">Build through stage</button>") "") "</article>"))))) (join ""))
      "<label>Work notes<textarea data-note=\"" slice.id "\">" (-/escapeText (or (and slice.work slice.work.note) "")) "</textarea></label>"
      "<button data-save-note=\"" slice.id "\">Save notes</button></section>"))))) (join ""))))

(defn.js renderPreviews [data]
  (var previews (or data.project.previews []))
  (var selected (or (. previews (find (fn [item] (return (== item.id (. -/state preview)))))) (. previews [0])))
  (when (not selected) (return "<h2>Project previews</h2><p class=\"muted\">Declare :previews in the project map or build an HTML output.</p>"))
  (:= (. -/state preview) selected.id)
  (return (+ "<div class=\"preview-tabs\">"
    (. (. previews (map (fn [item] (return (+ "<button data-preview=\"" (-/escapeText item.id)
      "\" aria-pressed=\"" (== selected.id item.id) "\">" (-/escapeText item.label) "</button>"))))) (join ""))
    "</div><div class=\"preview-controls\"><code>" (-/escapeText selected.url) "</code>"
    "<button data-action=\"reload-preview\">Reload</button>"
    "<button data-viewport=\"desktop\" aria-pressed=\"" (== (. -/state viewport) "desktop") "\">Desktop</button>"
    "<button data-viewport=\"mobile\" aria-pressed=\"" (== (. -/state viewport) "mobile") "\">Mobile</button>"
    (:? selected.ready (+ "<a target=\"_blank\" rel=\"noopener\" href=\"" (-/escapeText selected.url) "\">Open in new tab ↗</a>") "")
    "</div>"
    (:? (== selected.kind "server") (+ "<p class=\"muted\">Development server: "
      (:? selected.command (+ "start with <code>" (-/escapeText selected.command) "</code>. ") "")
      "If the frame cannot connect, start that server and click Reload.</p>") "")
    (:? selected.ready
      (+ "<div class=\"preview-stage " (. -/state viewport) "\"><iframe title=\"" (-/escapeText selected.label)
         "\" src=\"" (-/escapeText selected.url) "\"></iframe></div>")
      (+ "<p class=\"muted\">This page has not been built.</p><button data-op=\"build\" data-target=\""
         (-/escapeText selected.target) "\">Build preview</button>")))))

(defn.js drawEdges []
  (var graph (-/el ".graph"))
  (when (not graph) (return nil))
  (var data (model.snapshot (. -/state model)))
  (var rect (graph.getBoundingClientRect))
  (var paths [])
  (. data.project.targets (forEach (fn [target]
    (. target.depends (forEach (fn [id]
      (var source (-/el (+ "[data-node=\"" id "\"]")))
      (var destination (-/el (+ "[data-node=\"" target.id "\"]")))
      (when (and source destination)
        (var a (source.getBoundingClientRect)) (var b (destination.getBoundingClientRect))
        (var x1 (- (+ a.left (/ a.width 2)) rect.left)) (var x2 (- (+ b.left (/ b.width 2)) rect.left))
        (var y1 (- a.bottom rect.top)) (var y2 (- b.top rect.top)) (var mid (/ (+ y1 y2) 2))
        (. paths (push (+ "<path d=\"M " x1 " " y1 " C " x1 " " mid "," x2 " " mid "," x2 " " y2 "\"/>"))))))))))
  (:= (. (graph.querySelector "svg") ["innerHTML"]) (. paths (join ""))))

(defn.js render []
  (var data (model.snapshot (. -/state model)))
  (when (not data) (return nil))
  (var root (-/el "#app-manage-root"))
  (var active (. data.runs (find (fn [run] (return (== run.id data.active))))))
  (var latest (. data.runs [0]))
  (var files (. data.project.targets (reduce (fn [sum target] (return (+ sum (. target ["output-files"] length)))) 0)))
  (var content (:? (== (. -/state view) "pipeline") (-/renderGraph data)
                  (== (. -/state view) "sources") (-/renderList data "sources")
                  (== (. -/state view) "outputs") (-/renderList data "outputs")
                  (== (. -/state view) "checks") (-/renderChecks data)
                  (== (. -/state view) "previews") (-/renderPreviews data)
                  (== (. -/state view) "slices") (-/renderSlices data)
                  (== (. -/state view) "pages") (-/renderPages data)
                  :else (-/renderRuns data)))
  (:= document.title (+ data.project.id " · app.manage"))
  (:= root.innerHTML
    (+ "<header class=\"appbar\"><strong>app.manage</strong><span>/</span>" (-/escapeText data.project.id)
       "<span class=\"local\">● LOCAL PROJECT</span></header><div class=\"shell\"><nav class=\"sidebar\"><div class=\"eyebrow\">WORKSPACE</div>"
       (. (. [["slices" "Slices"] ["pages" "Pages & models"] ["pipeline" "Pipeline"] ["previews" "Previews"] ["sources" "Sources"] ["outputs" "Outputs"] ["checks" "Checks"] ["runs" "Run history"]]
           (map (fn [item] (return (+ "<button data-view=\"" (. item [0]) "\" aria-pressed=\"" (== (. -/state view) (. item [0])) "\">" (. item [1]) "</button>"))))) (join ""))
       "<small class=\"project-root\">" (-/escapeText data.project.root) "</small></nav><main><div class=\"projectbar\"><div class=\"project-icon\">d/</div><div><h1>"
       (-/escapeText data.project.id) "</h1><p class=\"muted\">" data.project.targets.length " targets · " files " generated files</p></div>"
       "<div class=\"actions\"><label class=\"with-checks\"><input type=\"checkbox\" data-option=\"checks\" " (:? (. -/state withChecks) "checked" "") ">Include checks</label>"
       "<button data-op=\"" (:? data.watching "watch-stop" "watch-start") "\">" (:? data.watching "Stop watch" "Watch") "</button>"
       "<button data-op=\"check\">Check outputs</button><button class=\"primary\" data-op=\"build\">Build project</button></div></div>"
       "<div class=\"workspace\"><div class=\"workspace-main\"><div class=\"toolbar\"><strong>"
       (-/escapeText (. -/state view)) "</strong><button class=\"small\" data-action=\"trace\" aria-pressed=\"" (. -/state trace) "\">Trace impact</button>"
       "<span class=\"session-status\">" (:? active (+ "Running " active.operation) (:? latest (+ (-/statusLabel latest.status) " · " latest.operation) "Project loaded")) "</span></div>"
       "<div class=\"content\">" content "</div><section class=\"activity\"><h3>Activity"
       (:? active "<span class=\"status running\">Running</span>" "") "</h3>"
       (:? latest
          (+ "<div class=\"event-list\">"
             (. (. (or latest.events []) (slice -12) (map (fn [event] (return (+ "<div>" (-/escapeText (-/eventText event)) "</div>"))))) (join ""))
             "</div>" (:? (== latest.status "error") (+ "<pre class=\"error-detail\">" (-/escapeText (JSON.stringify latest.result nil 2)) "</pre>") ""))
          "<p class=\"muted\">No operations have run in this session.</p>") "</section></div>"
       "<aside class=\"inspector\"><div class=\"inspector-heading\">Target inspector</div>" (-/renderInspector data) "</aside></div>"
       "<footer><span>" (:? data.watching "● Watching sources" "○ Watch off") "</span><span>Actual project state · revision " data.revision
       "</span>"
       (. (. (or data.project.previews []) (filter (fn [item] (return item.ready)))
            (map (fn [item] (return (+ "<a href=\"" (-/escapeText item.url)
              "\" target=\"_blank\" rel=\"noopener\">" (-/escapeText item.label) " ↗</a>"))))) (join ""))
       "</footer></main></div>"
       (:? (. -/state file) (+ "<section class=\"file-overlay\" role=\"dialog\" aria-modal=\"true\" aria-label=\"File viewer\"><div class=\"file-window\"><header><strong>"
                           (-/escapeText (. -/state file path)) "</strong><button data-action=\"close-file\">Close</button></header><pre>"
                           (-/escapeText (. -/state file content)) "</pre></div></section>") "")))
  (requestAnimationFrame -/drawEdges))

(defn.js showError [message]
  (var element (-/el ".session-status"))
  (if element (:= element.textContent message)
      (:= (. (-/el "#app-manage-root") ["textContent"]) message)))

(defn.js ^{:- [:async]} poll []
  (when (. -/state polling) (return nil))
  (:= (. -/state polling) true)
  (try
    (var before (. -/state model revision))
    (await (model.refresh (. -/state model)))
    (when (not (== before (. -/state model revision))) (-/render))
    (catch error (-/showError (+ "Connection error: " error.message)))
    (finally (:= (. -/state polling) false))))

(defn.js ^{:- [:async]} action [operation target]
  (when (. -/state busy) (return nil))
  (:= (. -/state busy) true)
  (try
    (await (model.command (. -/state model) operation {"target" target "tests" (. -/state withChecks)}))
    (-/render)
    (catch error (-/showError error.message))
    (finally (:= (. -/state busy) false))))

(defn.js ^{:- [:async]} openFile [target path]
  (var sequence (+ (. -/state fileSequence) 1)) (:= (. -/state fileSequence) sequence)
  (try
    (var file (await (-/request (+ "/api/file?target=" (encodeURIComponent target) "&path=" (encodeURIComponent path)) nil)))
    (when (== sequence (. -/state fileSequence)) (:= (. -/state file) file) (-/render))
    (catch error (-/showError error.message))))

(defn.js ^{:- [:async]} sliceAction [button]
  (try
    (var data (model.snapshot (. -/state model)))
    (var id (or button.dataset.sliceBuild button.dataset.sliceCopy button.dataset.saveNote button.dataset.slice))
    (var slice (. data.slices (find (fn [item] (return (== item.id id))))))
    (when button.dataset.sliceCopy
      (await (navigator.clipboard.writeText (JSON.stringify slice nil 2)))
      (return nil))
    (when button.dataset.saveNote
      (var token (. (document.querySelector "meta[name=app-manage-token]") content))
      (await (-/request "/api/slice-work" {"method" "POST" "headers" {"Content-Type" "application/json" "X-App-Manage-Token" token}
        "body" (JSON.stringify {"slice" id "revision" slice.revision "state" "active"
          "note" (. (-/el (+ "[data-note=\"" id "\"]")) value)})}))
      (await (model.refresh (. -/state model))) (-/render) (return nil))
    (await (model.command (. -/state model) "build"
      {"slice" id "stage" (or button.dataset.stage nil) "tests" (. -/state withChecks)}))
    (-/render)
    (catch error (-/showError error.message))))

(defn.js ^{:- [:async]} verifyAction [button]
  (try
    (var data (model.snapshot (. -/state model)))
    (when button.dataset.verifyModel
      (:= (. -/state verifyDraft) (JSON.stringify {"page" button.dataset.verifyPage "kind" "model" "member" button.dataset.verifyModel "inputs" {} "payload" {}} nil 2))
      (-/render) (return nil))
    (when button.dataset.loadCase
      (:= (. -/state verifyDraft) (JSON.stringify (. data.verification.saved.cases [button.dataset.loadCase]) nil 2))
      (-/render) (return nil))
    (var request nil)
    (when (not (. ["run-page-cases" "verify-definitions"] (includes button.dataset.verification)))
      (var text (. (-/el "#verification-request") value))
      (:= (. -/state verifyDraft) text)
      (:= request (JSON.parse text)))
    (when (== button.dataset.verification "save-page-case")
      (:= request {"id" (. (-/el "#verification-case-id") value)
                   "revision" data.verification.saved.revision "case" request}))
    (await (model.command (. -/state model) button.dataset.verification {"request" request}))
    (-/render)
    (catch error (-/showError error.message))))

(defrun.js boot
  (var token (. (document.querySelector "meta[name=app-manage-token]") content))
  (:= (. -/state model)
      (model.open {"state" (fn [] (return (-/request "/api/state" nil)))
                   "action" (fn [value] (return (-/request "/api/actions"
                      {"method" "POST" "headers" {"Content-Type" "application/json" "X-App-Manage-Token" token}
                       "body" (JSON.stringify value)})))}))
  (. document (addEventListener "click" (fn [event]
    (var button (. event.target (closest "button")))
    (when (not button) (return nil))
    (cond (or button.dataset.verification button.dataset.loadCase button.dataset.verifyModel) (-/verifyAction button)
          (or button.dataset.sliceBuild button.dataset.sliceCopy button.dataset.saveNote button.dataset.slice) (-/sliceAction button)
          button.dataset.view (do (:= (. -/state view) button.dataset.view) (-/render))
          button.dataset.preview (do (:= (. -/state preview) button.dataset.preview) (-/render))
          button.dataset.viewport (do (:= (. -/state viewport) button.dataset.viewport) (-/render))
          (== button.dataset.action "reload-preview") (do
            (var frame (-/el ".preview-stage iframe"))
            (when frame (:= frame.src frame.src)))
          button.dataset.select (do (model.selectTarget (. -/state model) button.dataset.select) (-/render))
          button.dataset.op (-/action button.dataset.op (or button.dataset.target nil))
          button.dataset.file (-/openFile button.dataset.owner button.dataset.file)
          (== button.dataset.action "close-file") (do (:= (. -/state fileSequence) (+ (. -/state fileSequence) 1)) (:= (. -/state file) nil) (-/render))
          (== button.dataset.action "trace") (do (:= (. -/state trace) (not (. -/state trace))) (-/render))
          (== button.dataset.action "impact") (do (:= (. -/state trace) true) (:= (. -/state view) "pipeline") (-/render))))))
  (. document (addEventListener "input" (fn [event] (when (== event.target.id "verification-request") (:= (. -/state verifyDraft) event.target.value)))))
  (. document (addEventListener "change" (fn [event]
    (when (== event.target.dataset.option "checks") (:= (. -/state withChecks) event.target.checked)))))
  (. document (addEventListener "keydown" (fn [event]
    (when (and (== event.key "Escape") (. -/state file))
      (:= (. -/state fileSequence) (+ (. -/state fileSequence) 1))
      (:= (. -/state file) nil) (-/render)))))
  (. window (addEventListener "resize" -/drawEdges))
  (-/poll)
  (var interval (setInterval -/poll 1000))
  (. window (addEventListener "beforeunload" (fn [] (clearInterval interval) (model.close (. -/state model))))))
