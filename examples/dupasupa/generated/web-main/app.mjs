export function escapeText(text){
  return String(text).replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\"","&quot;");
}

export function renderCampaigns(rows){
  return "<h1>Dupasupa</h1><p>Campaigns · local demonstration</p>" + rows.map(function (row){
    return "<form data-id=\"" + escapeText(row.id) + "\"><label>Campaign title" + "<input name=\"title\" required value=\"" + escapeText(row.title) + "\"></label><button>Save</button></form>";
  }).join("") + "<p data-status role=\"status\" aria-live=\"polite\"></p>";
}

export function setStatus(root,message){
  let status = root.querySelector("[data-status]");
  if(status){
    status.textContent = message;
  }
}

export function mount(root){
  let worker = new Worker("./worker.mjs",{"type":"module"});
  let closed = false;
  root.innerHTML = "<p data-status role=\"status\" aria-live=\"polite\">Loading campaigns…</p>";
  let onMessage = function (event){
    if(closed){
      return;
    }
    if(event.data.error){
      setStatus(root,event.data.error);
    }
    else{
      root.innerHTML = renderCampaigns(event.data.result);
      setStatus(root,(event.data.request == "save") ? "Saved." : "");
    }
  };
  let onSubmit = function (event){
    event.preventDefault();
    let form = event.target;
    setStatus(root,"Saving…");
    worker.postMessage({
      "action":"save",
      "request":"save",
      "id":form.dataset.id,
      "title":new FormData(form).get("title")
    });
  };
  worker.addEventListener("message",onMessage);
  worker.addEventListener("error",function (event){
    if(!closed){
      setStatus(root,event.message || "Campaign worker failed.");
    }
  });
  root.addEventListener("submit",onSubmit);
  worker.postMessage({"action":"load","request":"load"});
  return function (){
    closed = true;
    root.removeEventListener("submit",onSubmit);
    worker.terminate();
  };
}

if(!((typeof document) == "undefined")){
  mount(document.getElementById("app"));
}

export default {["renderCampaigns"]:renderCampaigns,["mount"]:mount}