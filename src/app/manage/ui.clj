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

(defn.js previewRoute [id]
  (return (+ "#/previews/" (encodeURIComponent id))))

(defn.js readRoute []
  (try
    (var parts (. window.location.hash (replace (new RegExp "^#/?") "") (split "/")))
    (var view (decodeURIComponent (. parts [0])))
    (when (. ["units" "slices" "pages" "pipeline" "servers" "previews" "sources" "outputs" "checks" "runs"] (includes view))
      (:= (. -/state view) view)
      (when (== view "previews")
        (:= (. -/state preview) (:? (. parts [1]) (decodeURIComponent (. parts [1])) nil))))
    (catch error (return nil))))

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
  (return (+ "<button class='file-row' data-file='" (-/escapeText file.path)
             "' data-owner='" (-/escapeText target) "' data-kind='" kind "' title='"
             (-/escapeText file.path) " · " file.bytes " B'><span>"
             (-/escapeText (. (. file.path (split "/")) (pop))) "</span></button>")))

(defn.js fileNodes [files]
  (var root (Object.create nil))
  (. files (forEach (fn [file]
    (var nodes root)
    (var parts (. file.path (split "/")))
    (. parts (forEach (fn [name index]
      (when (not (. nodes [name])) (:= (. nodes [name]) {"name" name "path" (. (. parts (slice 0 (+ index 1))) (join "/")) "count" 0 "children" (Object.create nil)}))
      (var node (. nodes [name]))
      (:= node.count (+ node.count 1))
      (when (== index (- parts.length 1)) (:= node.file file))
      (:= nodes node.children)))))))
  (return root))
(defn.js renderFileNodes [nodes target kind group depth]
  (return (. (. (Object.values nodes) (sort (fn [a b] (return (. a.name (localeCompare b.name)))))
    (map (fn [node]
      (when node.file (return (-/fileButton node.file target kind)))
      (var label node.name)
      (while (and (== (. (Object.keys node.children) length) 1) (not (. (Object.values node.children) [0] file)))
        (:= node (. (Object.values node.children) [0]))
        (:= label (+ label "/" node.name)))
      (var key (+ group ":" node.path))
      (var saved (and (. -/state fileFolders) (. -/state fileFolders [key])))
      (return (+ "<details class='file-folder' data-folder='" (-/escapeText key) "' "
                 (:? (:? (== (typeof saved) "undefined") (== depth 0) saved) "open" "") "><summary>"
                 (-/escapeText label) "<small>" node.count "</small></summary><div class='file-children'>"
                 (-/renderFileNodes node.children target kind group (+ depth 1)) "</div></details>"))))) (join ""))))
(defn.js fileBrowser [files target kind scope]
  (var group (+ scope ":" target ":" kind))
  (var query (or (and (. -/state fileFilters) (. -/state fileFilters [group])) ""))
  (return (+ "<div class='file-browser'><input type='search' aria-label='Filter " (-/escapeText target) " " kind " files' placeholder='Filter files…' data-file-filter='"
             (-/escapeText group) "' value='" (-/escapeText query) "'><div class='file-tree'>"
             (-/renderFileNodes (-/fileNodes files) target kind group 0)
             "</div><p class='file-empty' hidden>No matching files.</p></div>")))
(defn.js filterFiles [input]
  (when (not (. -/state fileFilters)) (:= (. -/state fileFilters) (Object.create nil)))
  (:= (. -/state fileFilters [input.dataset.fileFilter]) input.value)
  (var browser (input.closest ".file-browser"))
  (var query (. input.value (toLowerCase)))
  (var count 0)
  (. (browser.querySelectorAll ".file-row") (forEach (fn [file]
    (:= file.hidden (not (. (. file.dataset.file (toLowerCase)) (includes query))))
    (when (not file.hidden) (:= count (+ count 1))))))
  (. (browser.querySelectorAll ".file-folder") (forEach (fn [folder]
    (:= folder.hidden (not (. (Array.from (folder.querySelectorAll ".file-row")) (some (fn [file] (return (not file.hidden)))))))
    (var saved (and (. -/state fileFolders) (. -/state fileFolders [folder.dataset.folder])))
    (:= folder.open (:? query.length true (:? (== (typeof saved) "undefined") (not (folder.parentElement.closest ".file-folder")) saved))))))
  (:= (. (browser.querySelector ".file-empty") hidden) (> count 0)))
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
             (-/fileBrowser (. target ["source-files"]) target.id "source" "inspector")
             "<details class='generated-files'><summary>Generated files · " (. target ["output-files"] length) "</summary>"
             (-/fileBrowser (. target ["output-files"]) target.id "output" "inspector") "</details>"
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
               (-/fileBrowser files target.id kind "list")
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

(defn.js renderTestResults [run]
  (var report (and run.result run.result.report))
  (when (not report) (return ""))
  (return (+ "<section class='test-results'><h3>Test results</h3><p>" (-/escapeText (or report.message "")) "</p>"
    (. (. (or report.namespaces []) (map (fn [ns]
      (var facts (Object.create nil))
      (. (or ns.assertions []) (forEach (fn [a]
        (var key (or (. a ["fact-id"]) a.desc a.name "Assertion"))
        (when (not (. facts [key])) (:= (. facts [key]) {"name" (or a.desc a.name key) "assertions" []}))
        (. (. facts [key] assertions) (push a)))))
      (return (+ "<details class='test-namespace' open><summary>" (-/escapeText ns.namespace) " · " (-/escapeText ns.status)
        "</summary><p>" (-/escapeText (or ns.message "")) "</p>"
        (. (. (Object.values facts) (map (fn [fact]
          (var key (+ "fact:" run.id ":" ns.namespace ":" fact.name))
          (var saved (and (. -/state fileFolders) (. -/state fileFolders [key])))
          (return (+ "<details class='test-fact' data-folder='" (-/escapeText key) "' " (:? saved "open" "") "><summary>" (-/escapeText fact.name) "</summary>"
            (. (. fact.assertions (map (fn [a]
              (return (+ "<article class='test-assertion'><button data-source='" (-/escapeText (or a.path ""))
                "' data-line='" (or a.line 1) "'>" (-/escapeText a.status) " · " (-/escapeText (or a.path "")) ":" (or a.line 1)
                "</button><div class='assertion-values'><section><h4>Expected / checker</h4><pre>"
                (-/escapeText (JSON.stringify a.check nil 2)) "</pre></section><section><h4>Actual</h4><pre>"
                (-/escapeText (JSON.stringify a.data nil 2)) "</pre></section></div></article>"))))) (join ""))
            "</details>"))))) (join "")) "</details>"))))) (join "")) "</section>")))
(defn.js renderRuns [data]
  (return (+ "<h2>Run history</h2>"
             (:? (not data.runs.length) "<p class=\"muted\">No operations have run in this server session.</p>" "")
             (. (. data.runs (map (fn [run index]
               (var key (+ "run:" run.id))
               (var saved (and (. -/state fileFolders) (. -/state fileFolders [key])))
               (return (+ "<details class='run' data-folder='" (-/escapeText key) "' "
                          (:? (:? (== (typeof saved) "undefined") (== index 0) saved) "open" "") "><summary><strong>" (-/escapeText run.operation)
                          "</strong><span class=\"status " run.status "\">" (-/statusLabel run.status) "</span>"
                          "<code>" (. run.id (slice 0 8)) "</code></summary>" (-/renderTestResults run) "<details><summary>Run details</summary><pre>"
                          (-/escapeText (JSON.stringify run.result nil 2)) "</pre></details>"
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
        (:? page.migration (+ "<div class=\"migration-status\">Migration group: <strong>" (-/escapeText page.migration.group)
          "</strong> · " (-/escapeText (-/statusLabel page.migration.status)) " · " page.migration.required " required capabilities · "
          (. page.migration ["native-contracts"]) " native contracts<p>" (-/escapeText page.migration.blocker) "</p></div>") "")
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

(defn.js renderServer [item active]
  (var disabled (:? active "disabled" ""))
  (var canStart (and (not item.managed) (not item.ready)))
  (var key (+ "server-log:" item.id))
  (var expanded (and (. -/state fileFolders) (. -/state fileFolders [key])))
  (return (+ "<section class='dev-server' data-server-card='" (-/escapeText item.id) "'><header><strong>" (-/escapeText item.label)
    "</strong><span class='status'>" (-/escapeText item.status) "</span>"
    (:? item.managed (+ "<code>PID " item.pid "</code>") "") "</header><p><a target='_blank' rel='noopener' href='" (-/escapeText item.url) "'>" (-/escapeText item.url) " ↗</a></p>"
    "<div class='server-actions'><button data-server-action='server-start' data-server='" item.id "' " (:? canStart disabled "disabled") ">Start</button>"
    "<button data-server-action='server-stop' data-server='" item.id "' " (:? item.managed disabled "disabled") ">Stop</button>"
    "<button data-server-action='server-restart' data-server='" item.id "' " (:? item.managed disabled "disabled") ">Restart</button></div>"
    (:? (== item.status "external") "<p class='muted'>Running outside this workbench. Stop it in its original terminal, then use Start here to manage it.</p>" "")
    (:? (== item.status "starting") "<p class='muted'>Starting… waiting for the listening port.</p>" "")
    (:? (== item.status "error") (+ "<p class='server-error'>" (-/escapeText (or item.message (+ "Process exited with code " item.exit ". Inspect the output below."))) "</p>") "")
    "<details data-folder='" key "' " (:? expanded "open" "") "><summary>Command &amp; output</summary><code>" (-/escapeText (. item.command (join " ")))
    "</code><pre class='server-output'>" (-/escapeText (. (or item.logs []) (join "\n"))) "</pre></details></section>")))

(defn.js renderServers [data]
  (return (+ "<h2>Development servers</h2><p class='muted'>Start, stop and restart the actual project processes. Owned servers stop when this workbench shuts down.</p>"
    (. (. (or (. data ["dev-servers"]) []) (map (fn [item] (return (-/renderServer item data.active))))) (join "")))))

(defn.js renderPreviews [data]
  (var previews (or data.project.previews []))
  (var servers (or (. data ["dev-servers"]) []))
  (var selected (or (. previews (find (fn [item] (return (== item.id (. -/state preview)))))) (. previews [0])))
  (when (not selected) (return "<h2>Project previews</h2><p class=\"muted\">Declare :previews in the project map or build an HTML output.</p>"))
  (:= (. -/state preview) selected.id)
  (when (not= window.location.hash (-/previewRoute selected.id))
    (window.history.replaceState nil "" (-/previewRoute selected.id)))
  (var service (. servers (find (fn [item] (return (== item.id selected.server))))))
  (return (+ (:? service (-/renderServer service data.active) "") "<div class=\"preview-tabs\">"
    (. (. previews (map (fn [item] (return (+ "<button data-preview=\"" (-/escapeText item.id)
      "\" aria-pressed=\"" (== selected.id item.id) "\">" (-/escapeText item.label) "</button>"))))) (join ""))
    "</div><div class=\"preview-controls\"><code>" (-/escapeText selected.url) "</code>"
    "<a href=\"" (-/escapeText (-/previewRoute selected.id)) "\">Preview link</a>"
    "<button data-action=\"reload-preview\">Reload</button>"
    "<button data-viewport=\"desktop\" aria-pressed=\"" (== (. -/state viewport) "desktop") "\">Desktop</button>"
    "<button data-viewport=\"mobile\" aria-pressed=\"" (== (. -/state viewport) "mobile") "\">Mobile</button>"
    (:? selected.ready (+ "<a target=\"_blank\" rel=\"noopener\" href=\"" (-/escapeText selected.url) "\">Open in new tab ↗</a>") "")
    "</div>"
    (:? (and (== selected.kind "server") (not service)) (+ "<p class=\"muted\">Development server: "
      (:? selected.command (+ "start with <code>" (-/escapeText selected.command) "</code>. ") "")
      "If the frame cannot connect, start that server and click Reload.</p>") "")
    (:? (and selected.ready (or (not service) service.ready))
      (+ "<div class=\"preview-stage " (. -/state viewport) "\"><iframe title=\"" (-/escapeText selected.label)
         "\" src=\"" (-/escapeText selected.url) "\"></iframe></div>")
      (+ (:? service "<p class=\"muted\">Start the development server to open this preview.</p>" (+ "<p class=\"muted\">This page has not been built.</p><button data-op=\"build\" data-target=\""
         (-/escapeText selected.target) "\">Build preview</button>")))))))

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

(defn.js renderUnits
  []
  (var report (or (. -/state unitReport) {"units" [] "summary" {}}))
  (var selected (. -/state unitSelected))
  (return
   (+
    "<h2>Source &amp; tests</h2><p class=muted>Inspect application logic, assertions and l/script- scaffolds through code.framework/code.manage. Structural evidence is not a proof of semantic coverage.</p>"
    "<div class=unit-controls><input id=unit-query aria-label=\"Find functional unit\" placeholder=\"Namespace or function\" value=\""
    (-/escapeText (or (. -/state unitQuery) ""))
    "\">"
    "<button data-unit-scan=all>Scan / search</button><button data-unit-scan=issues>Needs attention</button></div>"
    "<p role=status>"
    (-/escapeText (or (. -/state unitError)
                      (:? (. -/state unitBusy) "Scanning source and tests…" "")))
    "</p>"
    "<p>"
    (or report.summary.units 0)
    " units · "
    (or report.summary.attention 0)
    " need attention · "
    (or report.summary.linked 0)
    " linked, not run · "
    (or report.summary.excluded 0)
    " excluded</p>"
    "<details><summary>Analysis diagnostics ("
    (or (and report.diagnostics report.diagnostics.length) 0)
    ")</summary><pre>"
    (-/escapeText (JSON.stringify (or report.diagnostics []) nil 2))
    "</pre></details>"
    "<div class=unit-workspace><section class=unit-list>"
    (. (. report.units
          (map (fn [unit]
                 (return (+ "<button data-unit=\""
                            (-/escapeText unit.id)
                            "\"><strong>"
                            (-/escapeText unit.id)
                            "</strong><span>"
                            (-/escapeText
                             (:? unit.flags.length (. unit.flags (join ", ")) unit.status))
                            "</span></button>")))))
       (join ""))
    "</section><section class=unit-detail>"
    (:? selected
        (+ "<h3>"
           (-/escapeText selected.id)
           "</h3><div class=unit-panes><section><h4>Source</h4><p><button data-source='" (-/escapeText selected.source.path) "' data-line='" selected.source.line.row "'>"
           (-/escapeText selected.source.path)
           ":"
           selected.source.line.row
           "</button></p><pre>"
           (-/escapeText selected.source.code)
           "</pre></section><section><h4>Linked tests</h4>"
           (:? (not selected.tests.length) "<p>No linked test found.</p>" "")
           (. (. selected.tests
                 (map (fn [test]
                        (return (+ "<article><p><button data-source='" (-/escapeText test.path) "' data-line='" test.line.row "'>"
                                   (-/escapeText test.path)
                                   ":"
                                   test.line.row
                                   " · "
                                   test.assertions
                                   " assertion markers</button> <button data-run-namespace='" (-/escapeText test.namespace) "'>Run namespace</button></p><pre>"
                                   (-/escapeText test.code)
                                   "</pre><details open><summary>Runtime and calls</summary>"
                                   (. (. (. test ["runtime-evidence"] scripts)
                                         (map (fn [script]
                                                (return (+ "<p>" (-/escapeText script.language) " · "
                                                           (-/escapeText script.runtime) "</p><pre>"
                                                           (-/escapeText script.code) "</pre>"))))) (join ""))
                                   "<p>Direct unit invocation: "
                                   (:? (. test ["runtime-evidence"] ["invokes-unit"]) "yes" "not detected; inspect helpers")
                                   "</p><pre>"
                                   (-/escapeText (. (. test ["runtime-evidence"] calls) (join "\n")))
                                   "</pre></details></article>")))))
              (join ""))
           "</section></div>")
        "<p>Select a functional unit to compare its source and tests.</p>")
    "</section></div>")))

(defn.js language [path]
  (var ext (. (. path (split ".")) (pop)))
  (return (or (. {"clj" "clojure" "cljs" "clojure" "cljc" "clojure" "edn" "clojure"
                 "js" "javascript" "mjs" "javascript" "jsx" "javascript" "ts" "typescript" "tsx" "typescript"
                 "json" "json" "sql" "sql" "css" "css" "html" "html" "md" "markdown" "py" "python"} [ext]) "plaintext")))
(defn.js loadEditor []
  (when window.monaco (return (Promise.resolve window.monaco)))
  (when (. -/state editorLoading) (return (. -/state editorLoading)))
  (:= (. -/state editorLoading) (new Promise (fn [resolve reject]
    (var script (document.createElement "script"))
    (:= script.src "/monaco/vs/loader.js")
    (:= script.onerror (fn [] (:= (. -/state editorLoading) nil) (reject (new Error "Could not load the code viewer"))))
    (:= script.onload (fn []
      (window.require.config {"paths" {"vs" "/monaco/vs"}})
      (window.require ["vs/editor/editor.main"] (fn [] (resolve window.monaco)) reject)))
    (document.head.appendChild script))))
  (return (. -/state editorLoading)))
(defn.js jumpToLine [line]
  (var editor (. -/state editor))
  (when editor
    (var row (Math.max 1 (Math.min (or (parseInt line 10) 1) (. (editor.getModel) (getLineCount)))))
    (editor.setPosition {"lineNumber" row "column" 1})
    (editor.revealLineInCenter row)
    (var input (-/el "#file-line"))
    (when input (:= input.value row))))
(defn.js renderFile []
  (var file (. -/state file))
  (var overlay (-/el "#file-viewer"))
  (when (and overlay (== file (. -/state displayedFile))) (return nil))
  (when (. -/state editor) (. (. -/state editor) (dispose)) (:= (. -/state editor) nil))
  (when (. -/state editorModel) (. (. -/state editorModel) (dispose)) (:= (. -/state editorModel) nil))
  (when overlay (overlay.remove))
  (:= (. -/state displayedFile) file)
  (when (not file) (return nil))
  (:= overlay (document.createElement "section"))
  (:= overlay.id "file-viewer") (:= overlay.className "file-overlay")
  (overlay.setAttribute "role" "dialog") (overlay.setAttribute "aria-modal" "true") (overlay.setAttribute "aria-label" "File viewer")
  (:= overlay.innerHTML (+ "<div class='file-window'><header><strong>" (-/escapeText file.path)
    "</strong><label>Line <input id='file-line' aria-label='Go to line' type='number' min='1' value='" (or file.line 1)
    "'></label><button data-action='file-line'>Go</button><button data-action='close-file'>Close</button></header>"
    "<p class='muted code-status'>Loading code viewer…</p><div id='file-code'></div></div>"))
  (document.body.appendChild overlay)
  (. (-/loadEditor) (then (fn [monaco]
    (when (or (not overlay.isConnected) (not= file (. -/state file))) (return nil))
    (var model (monaco.editor.createModel file.content (-/language file.path)))
    (:= (. -/state editorModel) model)
    (:= (. -/state editor) (monaco.editor.create (overlay.querySelector "#file-code")
      {"model" model "readOnly" true "automaticLayout" true "fontSize" 13
       "theme" (:? (. (window.matchMedia "(prefers-color-scheme: dark)") matches) "vs-dark" "vs")
       "minimap" {"enabled" false} "scrollBeyondLastLine" false}))
    (. (overlay.querySelector ".code-status") (remove))
    (-/jumpToLine (or file.line 1))))
    (catch (fn [error]
      (when overlay.isConnected (:= (. (overlay.querySelector ".code-status") textContent) error.message))))))
(defn.js render []
  (var data (model.snapshot (. -/state model)))
  (when (not data) (return nil))
  (var root (-/el "#app-manage-root"))
  ;; Updating process output must not reload a live preview iframe.
  (var frame (-/el ".preview-stage iframe"))
  (var selected (. (or data.project.previews []) (find (fn [item] (return (== item.id (. -/state preview)))))))
  (var service (and selected (. (or (. data ["dev-servers"]) []) (find (fn [item] (return (== item.id selected.server)))))))
  (when (and (== (. -/state view) "previews") frame selected (== (. frame (getAttribute "src")) selected.url) (or (not service) service.ready))
    (var card (-/el ".dev-server"))
    (when (and card service) (:= card.outerHTML (-/renderServer service data.active)))
    (:= frame.parentElement.className (+ "preview-stage " (. -/state viewport)))
    (. (document.querySelectorAll "[data-viewport]") (forEach (fn [button] (button.setAttribute "aria-pressed" (== button.dataset.viewport (. -/state viewport))))))
    (-/renderFile)
    (return nil))
  (var focused document.activeElement)
  (var filterKey (and focused focused.dataset focused.dataset.fileFilter))
  (var caret (and filterKey focused.selectionStart))
  (. root.classList (toggle "units-view" (== (. -/state view) "units")))
  (var active (. data.runs (find (fn [run] (return (== run.id data.active))))))
  (var latest (. data.runs [0]))
  (var files (. data.project.targets (reduce (fn [sum target] (return (+ sum (. target ["output-files"] length)))) 0)))
  (var content (:? (== (. -/state view) "pipeline") (-/renderGraph data)
                  (== (. -/state view) "sources") (-/renderList data "sources")
                  (== (. -/state view) "outputs") (-/renderList data "outputs")
                  (== (. -/state view) "checks") (-/renderChecks data)
                  (== (. -/state view) "servers") (-/renderServers data)
                  (== (. -/state view) "previews") (-/renderPreviews data)
                  (== (. -/state view) "slices") (-/renderSlices data)
                  (== (. -/state view) "pages") (-/renderPages data)
                  (== (. -/state view) "units") (-/renderUnits)
                  :else (-/renderRuns data)))
  (:= document.title (+ data.project.id " · app.manage"))
  (:= root.innerHTML
    (+ "<header class=\"appbar\"><strong>app.manage</strong><span>/</span>" (-/escapeText data.project.id)
       "<span class=\"local\">● LOCAL PROJECT</span></header><div class=\"shell\"><nav class=\"sidebar\"><div class=\"eyebrow\">WORKSPACE</div>"
       (. (. (:? (. data.project ["unit-analysis"]) [["units" "Source & tests"] ["slices" "Slices"] ["pages" "Pages & models"] ["pipeline" "Pipeline"] ["servers" "Servers"] ["previews" "Previews"] ["sources" "Sources"] ["outputs" "Outputs"] ["checks" "Checks"] ["runs" "Run history"]] [["slices" "Slices"] ["pages" "Pages & models"] ["pipeline" "Pipeline"] ["servers" "Servers"] ["previews" "Previews"] ["sources" "Sources"] ["outputs" "Outputs"] ["checks" "Checks"] ["runs" "Run history"]])
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
       ))
  (-/renderFile)
  (. (document.querySelectorAll "[data-file-filter]") (forEach -/filterFiles))
  (when filterKey
    (var input (. (Array.from (document.querySelectorAll "[data-file-filter]")) (find (fn [input] (return (== input.dataset.fileFilter filterKey))))))
    (when input (input.focus) (input.setSelectionRange caret caret)))
  (requestAnimationFrame -/drawEdges))

(defn.js navigate [view preview]
  (var hash (:? (and (== view "previews") preview) (-/previewRoute preview) (+ "#/" view)))
  (when (not= window.location.hash hash) (window.history.pushState nil "" hash))
  (:= (. -/state view) view)
  (when preview (:= (. -/state preview) preview))
  (-/render))

(defn.js ^{:- [:async]} unitAction
  [button]
  (try
    (:= (. -/state unitError) nil)
    (when button.dataset.unitScan
      (var input (-/el "#unit-query"))
      (:= (. -/state unitQuery) (:? input input.value "")))
    (:= (. -/state unitBusy) true)
    (-/render)
    (when button.dataset.unitScan
      (:= (. -/state unitReport)
          (await (-/request (+ "/api/units?q=" (encodeURIComponent (. -/state unitQuery))
                               "&issues=" (:? (== button.dataset.unitScan "issues") "true" "false"))
                            nil))))
    (when button.dataset.unit
      (:= (. -/state unitSelected)
          (await (-/request (+ "/api/units?unit=" (encodeURIComponent button.dataset.unit)) nil))))
    (catch error (:= (. -/state unitError) error.message))
    (finally (:= (. -/state unitBusy) false) (-/render))))

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

(defn.js ^{:- [:async]} serverAction [button]
  (try
    (await (model.command (. -/state model) button.dataset.serverAction {"request" {"server" button.dataset.server}}))
    (-/render)
    (catch error (-/showError error.message))))

(defn.js ^{:- [:async]} openFile [target path]
  (var sequence (+ (. -/state fileSequence) 1)) (:= (. -/state fileSequence) sequence)
  (try
    (var file (await (-/request (+ "/api/file?target=" (encodeURIComponent target) "&path=" (encodeURIComponent path)) nil)))
    (when (== sequence (. -/state fileSequence)) (:= (. -/state file) file) (-/render))
    (catch error (-/showError error.message))))

(defn.js ^{:- [:async]} openSource [path line]
  (var sequence (+ (. -/state fileSequence) 1)) (:= (. -/state fileSequence) sequence)
  (try
    (var file (await (-/request (+ "/api/source?path=" (encodeURIComponent path)) nil)))
    (when (== sequence (. -/state fileSequence))
      (:= file.line (or (parseInt line 10) 1)) (:= (. -/state file) file) (-/render))
    (catch error (-/showError error.message))))

(defn.js ^{:- [:async]} runNamespace [namespace]
  (try
    (var profiles (await (-/request "/api/test-profiles" nil)))
    (var profile (. profiles (find (fn [p] (return (. p.namespaces (includes namespace)))))))
    (when (not profile) (throw (new Error "No execution profile is configured for this namespace")))
    (await (model.command (. -/state model) "run-test-namespaces" {"request" {"profile" profile.id "namespaces" [namespace]}}))
    (-/navigate "runs" nil)
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
      (when (. document.body.classList (contains "embedded")) (. document.body.classList (add (+ "embedded-" (. -/state view)))))
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
  (. window (addEventListener "message" (fn [event]
    (when (and (== event.origin window.location.origin) event.data (== (typeof event.data.appManageChecks) "boolean"))
      (:= (. -/state withChecks) event.data.appManageChecks)))))
  (when (. (new URLSearchParams window.location.search) (get "embedded")) (. document.body.classList (add "embedded")))
  (:= (. -/state view) (or (. (new URLSearchParams window.location.search) (get "view")) "pipeline"))
  (-/readRoute)
  (. window (addEventListener "hashchange" (fn [] (-/readRoute) (-/render))))
  (var token (. (document.querySelector "meta[name=app-manage-token]") content))
  (:= (. -/state model)
      (model.open {"state" (fn [] (return (-/request "/api/state" nil)))
                   "action" (fn [value] (return (-/request "/api/actions"
                      {"method" "POST" "headers" {"Content-Type" "application/json" "X-App-Manage-Token" token}
                       "body" (JSON.stringify value)})))}))
  (. document (addEventListener "click" (fn [event]
    (var button (. event.target (closest "button")))
    (when (not button) (return nil))
    (cond button.dataset.serverAction (-/serverAction button)
          (or button.dataset.unitScan button.dataset.unit) (-/unitAction button)
          (or button.dataset.verification button.dataset.loadCase button.dataset.verifyModel) (-/verifyAction button)
          (or button.dataset.sliceBuild button.dataset.sliceCopy button.dataset.saveNote button.dataset.slice) (-/sliceAction button)
          button.dataset.view (-/navigate button.dataset.view (:? (== button.dataset.view "previews") (. -/state preview) nil))
          button.dataset.preview (-/navigate "previews" button.dataset.preview)
          button.dataset.viewport (do (:= (. -/state viewport) button.dataset.viewport) (-/render))
          (== button.dataset.action "reload-preview") (do
            (var frame (-/el ".preview-stage iframe"))
            (when frame (:= frame.src frame.src)))
          button.dataset.select (do (model.selectTarget (. -/state model) button.dataset.select) (-/render))
          button.dataset.op (-/action button.dataset.op (or button.dataset.target nil))
          button.dataset.source (-/openSource button.dataset.source button.dataset.line)
          button.dataset.runNamespace (-/runNamespace button.dataset.runNamespace)
          (== button.dataset.action "file-line") (-/jumpToLine (. (-/el "#file-line") value))
          button.dataset.file (-/openFile button.dataset.owner button.dataset.file)
          (== button.dataset.action "close-file") (do (:= (. -/state fileSequence) (+ (. -/state fileSequence) 1)) (:= (. -/state file) nil) (-/render))
          (== button.dataset.action "trace") (do (:= (. -/state trace) (not (. -/state trace))) (-/render))
          (== button.dataset.action "impact") (do (:= (. -/state trace) true) (-/navigate "pipeline" nil))))))
  (. document (addEventListener "toggle" (fn [event]
    (when event.target.dataset.folder
      (var browser (event.target.closest ".file-browser"))
      (when (not (and browser (. (browser.querySelector "[data-file-filter]") value length)))
        (when (not (. -/state fileFolders)) (:= (. -/state fileFolders) (Object.create nil)))
        (:= (. -/state fileFolders [event.target.dataset.folder]) event.target.open)))) true))
  (. document (addEventListener "input" (fn [event]
    (when event.target.dataset.fileFilter (-/filterFiles event.target))
    (when (== event.target.id "verification-request") (:= (. -/state verifyDraft) event.target.value)))))
  (. document (addEventListener "change" (fn [event]
    (when (== event.target.dataset.option "checks") (:= (. -/state withChecks) event.target.checked)))))
  (. document (addEventListener "keydown" (fn [event]
    (when (and (== event.key "Enter") (== event.target.id "file-line")) (event.preventDefault) (-/jumpToLine event.target.value))
    (when (and (== event.key "Escape") (. -/state file))
      (:= (. -/state fileSequence) (+ (. -/state fileSequence) 1))
      (:= (. -/state file) nil) (-/render)))))
  (. window (addEventListener "resize" -/drawEdges))
  (-/poll)
  (var interval (setInterval -/poll 1000))
  (. window (addEventListener "beforeunload" (fn [] (clearInterval interval) (model.close (. -/state model))))))
