const esc = (value) => String(value == null ? "" : value).replace(/[&<>"']/g, (ch) => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[ch]));
const count = (run, key) => Number(run?.tests?.[key] || 0);
const runNumber = (run) => run?.run?.number ?? "–";

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

function render(root, data, props) {
  const runs = allRuns(data).slice(0, Number(props.limit || 100) * Math.max(1, Object.keys(data.workflows || {}).length));
  const workflows = [...new Set(runs.map((r) => r.workflow))].sort();
  const languages = [...new Set(runs.flatMap((r) => (r.jobs || []).map((j) => j.language)).filter(Boolean))].sort();
  root.innerHTML = `<div class="xtbench-toolbar"><label>Workflow<select data-filter="workflow"><option value="">All</option>${workflows.map(x=>`<option>${esc(x)}</option>`).join("")}</select></label><label>Language<select data-filter="language"><option value="">All</option>${languages.map(x=>`<option>${esc(x)}</option>`).join("")}</select></label></div><div class="xtbench-cards"></div><div class="xtbench-table-wrap"><table class="xtbench-table"><thead><tr><th>Run</th><th>Workflow</th><th>Status</th><th>Passed</th><th>Failed</th><th>Recorded</th></tr></thead><tbody></tbody></table></div>`;
  const redraw = () => {
    const wf = root.querySelector('[data-filter="workflow"]').value;
    const lang = root.querySelector('[data-filter="language"]').value;
    const filtered = runs.filter(r => (!wf || r.workflow === wf) && (!lang || (r.jobs || []).some(j => j.language === lang)));
    root.querySelector(".xtbench-cards").innerHTML = workflows.map(w => {
      const wr = filtered.filter(r => r.workflow === w); const latest = wr[0];
      const spec = (latest?.jobs || []).filter(j => !lang || j.language === lang).map(j => j.spec).find(Boolean);
      return `<section class="xtbench-card"><h3>${esc(w)}</h3><div class="xtbench-status xtbench-status-${esc(latest?.status || "unknown")}">${esc(latest?.status || "no data")}</div><div>Run ${esc(runNumber(latest))} · ${count(latest,"passed")} passed</div>${spec?`<div class="xtbench-spec">Spec ${Number(spec["spec-implemented"]||0)} implemented · ${Number(spec["spec-missing"]||0)} missing</div>`:""}<div class="xtbench-trend" aria-label="Recent run history">${wr.slice(0,30).reverse().map(r=>`<span class="${r.status === "success" ? "" : "failure"}" title="Run ${esc(runNumber(r))}: ${esc(r.status)}"></span>`).join("")}</div></section>`;
    }).join("");
    root.querySelector("tbody").innerHTML = filtered.slice(0, Number(props.limit || 100)).map(r => `<tr><td>${r.run?.url?`<a href="${esc(r.run.url)}">#${esc(runNumber(r))}</a>`:`#${esc(runNumber(r))}`}</td><td>${esc(r.workflow)}</td><td><span class="xtbench-status xtbench-status-${esc(r.status)}">${esc(r.status)}</span></td><td>${count(r,"passed")}</td><td>${count(r,"failed")+count(r,"throw")+count(r,"timeout")+count(r,"errored")}</td><td>${esc(r["recorded-at"] || "")}</td></tr>`).join("") || `<tr><td colspan="6">No matching runs.</td></tr>`;
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
