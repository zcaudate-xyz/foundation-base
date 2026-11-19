export function definition(pages,id){
  let page = pages.find(function (page){
    return page.id == id;
  });
  if(!page){
    throw new Error("Unknown page definition.");
  }
  return page;
}

export function entry(pages,id,kind,member){
  let page = definition(pages,id);
  let entries = (kind == "action") ? page.actions : page.models;
  let found = (entries || {})[member];
  if(!found){
    throw new Error("Unknown page member.");
  }
  return found;
}

export function request(entry,values){
  let spec = entry.transport;
  if(!spec){
    throw new Error("No request binding declared.");
  }
  let body = Object.assign({},spec.constants || {});
  for(let key of Object.keys(spec.bindings || {})){
    body[key] = values[spec.bindings[key]];
  };
  return {
    "path":spec.path,
    "method":spec.method,
    "body":(spec.method == "GET") ? null : body
  };
}

export function resolveRefs(input,results){
  if((input == null) || ((typeof input) != "object")){
    return input;
  }
  if(Array.isArray(input)){
    return input.map(function (value){
      return resolveRefs(value,results);
    });
  }
  if(Array.isArray(input["$ref"])){
    let value = results;
    for(let part of input["$ref"]){
      if((value == null) || !Object.hasOwn(value,part)){
        throw new Error("Scenario reference is not available.");
      }
      value = value[part];
    };
    return value;
  }
  let value = {};
  for(let key of Object.keys(input)){
    value[key] = resolveRefs(input[key],results);
  };
  return value;
}

export async function execute(pages,adapters,input){
  let page = definition(pages,input.page);
  let order = page.order || Object.keys(page.models || {});
  if((input.kind == "page") && (order.length > 1)){
    let output = {};
    let state = {};
    for(let member of order){
      let actual = await execute(
        pages,
        adapters,
        Object.assign({},input,{"kind":"model","member":member})
      );
      if(actual.error || actual.blocked){
        return actual;
      }
      output[member] = actual.result;
      state[member] = actual.state;
    };
    return {"result":output,"state":state};
  }
  let member = input.member || (page.order || [])[0] || Object.keys(page.models)[0];
  let selected = entry(pages,input.page,input.kind,member);
  let values = Object.assign({},selected.defaults || {},input.payload || {});
  let inputs = input.inputs || {};
  for(let target of Object.keys(selected.bindings || {})){
    let source = selected.bindings[target];
    if(!Object.hasOwn(inputs,source) || (inputs[source] == null)){
      throw new Error("Missing page binding: " + source);
    }
    if(target == "*"){
      Object.assign(values,inputs[source]);
    }
    else{
      values[target] = inputs[source];
    }
  };
  input = Object.assign({},input,{"payload":values});
  if(input.kind == "resolve"){
    return {
      "result":{
            "page":input.page,
            "name":member,
            "values":input.payload || {},
            "operation":selected.operation
          }
    };
  }
  if(!["page","model","action"].includes(input.kind)){
    return {"blocked":"Choose resolve, model, action or page."};
  }
  let adapter = adapters[selected.operation];
  if(!adapter){
    return {
      "blocked":"This operation has no live workbench adapter yet."
    };
  }
  return await adapter(input,selected);
}

export async function scenario(request,run){
  let results = {};
  if(Array.isArray(request.steps)){
    let steps = [];
    for(let step of request.steps){
      let actual = await run(resolveRefs(
        Object.assign({"actor":request.actor || "alice"},step),
        results
      ));
      actual.id = step.id;
      results[step.id] = actual;
      steps.push(actual);
    };
    return {"steps":steps};
  }
  return await run(request);
}

export function operation(pages,id){
  for(let page of pages){
    for(let kind of ["models","actions"]){
      for(let entry of Object.values(page[kind] || {})){
        if(entry.operation == id){
          return entry;
        }
      };
    };
  };
  throw new Error("Unknown declared operation.");
}