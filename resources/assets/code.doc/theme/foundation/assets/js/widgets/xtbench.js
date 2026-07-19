const esc = (value) => String(value == null ? "" : value).replace(/[&<>"']/g, (ch) => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[ch]));
const count = (run, key) => Number(run?.tests?.[key] || 0);
const runNumber = (run) => run?.run?.number ?? "–";
const missingArtifacts = (run) => run?.["metrics-status"] === "missing-artifacts";
const countLabel = (run, key) => missingArtifacts(run) ? "–" : count(run, key);
const failureTotal = (run) => count(run,"failed")+count(run,"throw")+count(run,"timeout")+count(run,"errored");
const failureLabel = (run) => missingArtifacts(run) ? "–" : failureTotal(run);
const labels = {failed:"failed",throw:"throws",timeout:"timeouts",errored:"errors",skipped:"skipped",passed:"passed"};

function stylesheet() {
  const href = new URL("../../css/widgets/xtbench.css", import.meta.url).href;
  if (!document.querySelector(`link[data-xtbench-style="${href}"]`)) {
    const link = document.createElement("link"); link.rel = "stylesheet"; link.href = href;
    link.dataset.xtbenchStyle = href; document.head.appendChild(link);
  }
}

function allRuns(data) {
  return Object.entries(data.workflows || {}).flatMap(([workflow, stream]) =>
    (stream.runs || []).map((run) => ({...run, workflow: run["workflow-key"] || workflow})));
}

function chips(value, keys = ["failed","throw","timeout","errored"]) {
  return `<span class="xtbench-counts">${keys.map(key => `<span class="xtbench-count xtbench-count-${key}">${countLabel(value,key)} ${labels[key]}</span>`).join("")}</span>`;
}

function sourceLink(record, location) {
  if (!location.path) return "";
  const line = location.line ? `:${location.line}` : "";
  const base = record?.run?.url?.split("/actions/runs/")[0];
  const sha = record?.git?.sha;
  if (!base || !sha) return `<code>${esc(location.path + line)}</code>`;
  const href = encodeURI(`${base}/blob/${sha}/${location.path}${location.line ? `#L${location.line}` : ""}`);
  return `<a href="${esc(href)}" target="_blank"><code>${esc(location.path + line)}</code></a>`;
}

function locations(record, items = []) {
  return items.map(item => `<li><span class="xtbench-failure-type">${esc(labels[item.type] || item.type)}</span>${item.count > 1 ? ` × ${item.count}` : ""}${item.path ? ` · ${sourceLink(record,item)}` : ""}</li>`).join("");
}

function failureTree(record, language) {
  if (missingArtifacts(record)) return `<div class="xtbench-detail-note">${esc(record?.error?.message || "No job metrics were published.")}</div>`;
  const jobs = (record.jobs || []).filter(job => !language || job.language === language);
  if (!failureTotal(record)) return `<div class="xtbench-detail-note">No failures in this run.</div>`;
  if (!jobs.some(job => Array.isArray(job.failures))) return `<div class="xtbench-detail-note">Failure details unavailable for this historical run. Use the Actions run link for logs.</div>`;
  return `<div class="xtbench-failure-tree">${jobs.map(job => {
    const namespaces = job.failures || [];
    return `<details open><summary><strong>${esc(job.language || "unknown")}</strong> · ${esc(job.suite || "suite")} ${chips(job)}</summary><div class="xtbench-tree-level">${namespaces.length ? namespaces.map(ns => `<details><summary><code>${esc(ns.namespace)}</code> ${chips({tests:ns.counts})}</summary><div class="xtbench-tree-level">${(ns.functions || []).map(fn => `<details><summary><code>${esc(fn.function)}</code> ${chips({tests:fn.counts})}</summary><ul>${locations(record,fn.locations)}</ul></details>`).join("")}${(ns["namespace-errors"] || []).length ? `<div class="xtbench-namespace-errors"><strong>Namespace load</strong><ul>${locations(record,ns["namespace-errors"])}</ul></div>` : ""}</div></details>`).join("") : `<div class="xtbench-detail-note">No failures recorded for this job.</div>`}</div></details>`;
  }).join("")}</div>`;
}

function render(root, data, props) {
  const runs = allRuns(data).slice(0, Number(props.limit || 100) * Math.max(1, Object.keys(data.workflows || {}).length));
  const workflows = [...new Set(runs.map((r) => r.workflow))].sort();
  const languages = [...new Set(runs.flatMap((r) => (r.jobs || []).map((j) => j.language)).filter(Boolean))].sort();
  const detailCache = new Map();
  let expanded = null;
  root.innerHTML = `<div class="xtbench-toolbar"><label>Workflow<select data-filter="workflow"><option value="">All</option>${workflows.map(x=>`<option>${esc(x)}</option>`).join("")}</select></label><label>Language<select data-filter="language"><option value="">All</option>${languages.map(x=>`<option>${esc(x)}</option>`).join("")}</select></label></div><div class="xtbench-cards"></div><div class="xtbench-table-wrap"><table class="xtbench-table"><thead><tr><th>Run</th><th>Workflow</th><th>Status</th><th>Passed</th><th>Failed</th><th>Throws</th><th>Timeouts</th><th>Errors</th><th>Skipped</th><th>Recorded</th></tr></thead><tbody></tbody></table></div>`;
  const redraw = () => {
    const wf = root.querySelector('[data-filter="workflow"]').value;
    const lang = root.querySelector('[data-filter="language"]').value;
    const filtered = runs.filter(r => (!wf || r.workflow === wf) && (!lang || (r.jobs || []).some(j => j.language === lang)));
    root.querySelector(".xtbench-cards").innerHTML = workflows.map(w => {
      const wr = filtered.filter(r => r.workflow === w); const latest = wr[0];
      const spec = (latest?.jobs || []).filter(j => !lang || j.language === lang).map(j => j.spec).find(Boolean);
      const result = missingArtifacts(latest) ? "No job metrics" : `${count(latest,"passed")} passed`;
      return `<section class="xtbench-card"><h3>${esc(w)}</h3><div class="xtbench-status xtbench-status-${esc(latest?.status || "unknown")}">${esc(missingArtifacts(latest) ? "infrastructure failure" : latest?.status || "no data")}</div><div>Run ${esc(runNumber(latest))} · ${esc(result)}</div>${latest ? chips(latest) : ""}${spec?`<div class="xtbench-spec">Spec ${Number(spec["spec-implemented"]||0)} implemented · ${Number(spec["spec-missing"]||0)} missing</div>`:""}<div class="xtbench-trend" aria-label="Recent run history">${wr.slice(0,30).reverse().map(r=>`<span class="${r.status === "success" ? "" : "failure"}" title="Run ${esc(runNumber(r))}: ${esc(missingArtifacts(r) ? "infrastructure failure" : r.status)}"></span>`).join("")}</div></section>`;
    }).join("");
    root.querySelector("tbody").innerHTML = filtered.slice(0, Number(props.limit || 100)).map(r => {
      const key = r.path || `${r.workflow}-${runNumber(r)}-${r.run?.attempt || 1}`;
      const open = expanded === key;
      const cached = detailCache.get(key);
      const detail = cached?.record ? failureTree(cached.record,lang) : cached?.error ? `<div class="xtbench-detail-note xtbench-detail-error">Unable to load failure details: ${esc(cached.error)}</div>` : `<div class="xtbench-detail-note">Loading failure details…</div>`;
      return `<tr><td><button class="xtbench-run-toggle" data-run-key="${esc(key)}" aria-expanded="${open}">${open ? "▾" : "▸"}</button> ${r.run?.url?`<a href="${esc(r.run.url)}">#${esc(runNumber(r))}</a>`:`#${esc(runNumber(r))}`}</td><td>${esc(r.workflow)}</td><td><span class="xtbench-status xtbench-status-${esc(r.status)}">${esc(missingArtifacts(r) ? "infrastructure failure" : r.status)}</span></td><td>${countLabel(r,"passed")}</td><td>${countLabel(r,"failed")}</td><td>${countLabel(r,"throw")}</td><td>${countLabel(r,"timeout")}</td><td>${countLabel(r,"errored")}</td><td>${countLabel(r,"skipped")}</td><td>${esc(r["recorded-at"] || "")}</td></tr>${open ? `<tr class="xtbench-detail-row"><td colspan="10">${detail}</td></tr>` : ""}`;
    }).join("") || `<tr><td colspan="10">No matching runs.</td></tr>`;
    root.querySelectorAll("[data-run-key]").forEach(button => button.addEventListener("click", async () => {
      const key = button.dataset.runKey;
      expanded = expanded === key ? null : key;
      redraw();
      const run = runs.find(item => (item.path || `${item.workflow}-${runNumber(item)}-${item.run?.attempt || 1}`) === key);
      if (expanded === key && run?.path && !detailCache.has(key)) {
        try {
          const response = await fetch(new URL(run.path, props["data-url"]), {cache:"no-cache"});
          if (!response.ok) throw new Error(`request failed (${response.status})`);
          detailCache.set(key,{record:await response.json()});
        } catch (error) { detailCache.set(key,{error:error.message}); }
        redraw();
      } else if (expanded === key && !run?.path && !detailCache.has(key)) {
        detailCache.set(key,{record:run}); redraw();
      }
    }));
  };
  root.querySelectorAll("select").forEach(el => el.addEventListener("change", redraw)); redraw();
}

export async function mount(root, props = {}) {
  stylesheet();
  if (!props["data-url"]) throw new Error("xtbench widget requires props.data-url");
  const response = await fetch(props["data-url"], {cache: "no-cache"});
  if (!response.ok) throw new Error(`metrics request failed (${response.status})`);
  const data = await response.json();
  if (Number(data["schema-version"]) !== 1) throw new Error(`unsupported metrics schema ${data["schema-version"]}`);
  render(root, data, props);
}
