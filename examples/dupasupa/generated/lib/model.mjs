import * as bindings from './bindings.mjs'

import * as page from './libs/xt/substrate/page-core.mjs'

import * as runtime from './libs/app/manage/page-runtime.mjs'

import * as substrate from './libs/xt/substrate.mjs'

export var GROUP_ID = runtime.definition(bindings.DEFINITIONS,"campaigns").group_id;

export function ensureOpen(handle){
  if(handle.closed){
    throw new Error("Campaign page is closed.");
  }
}

export function open(transport){
  let node = substrate.node_create({"id":crypto.randomUUID()});
  page.group_add_attach(node,"app",GROUP_ID,{
    "rows":{
        "handler":function (_context){
            return transport.list();
          },
        "defaults":{"args":[],"output":[]}
      }
  });
  return {"node":node,"transport":transport,"closed":false};
}

export function snapshot(handle){
  ensureOpen(handle);
  return page.model_get_output(handle.node,"app",GROUP_ID,"rows");
}

export async function loadRows(handle){
  ensureOpen(handle);
  let acc = await page.model_refresh(handle.node,"app",GROUP_ID,"rows",{},null);
  if(acc.error){
    let failed = Object.values(acc).find(function (value){
      return Array.isArray(value) && (value[2] == true);
    });
    let error = failed ? failed[1] : null;
    throw error || new Error("Could not load campaigns.");
  }
  ensureOpen(handle);
  return snapshot(handle);
}

export async function save(handle,id,title){
  ensureOpen(handle);
  await handle.transport.call("save-campaign",{"i_id":id,"i_title":title});
  return await loadRows(handle);
}

export async function create(handle,title){
  ensureOpen(handle);
  let created = await handle.transport.call("create-owned-campaign",{"i_title":title});
  await loadRows(handle);
  return created;
}

export function close(handle){
  if(!handle.closed){
    handle.closed = true;
    page.group_remove(handle.node,"app",GROUP_ID);
  }
  return true;
}

export default {
  ["open"]:open,
  ["loadRows"]:loadRows,
  ["save"]:save,
  ["create"]:create,
  ["close"]:close,
  ["snapshot"]:snapshot
}