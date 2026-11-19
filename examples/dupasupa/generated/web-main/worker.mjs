import model from '../lib/model.mjs'

export var rows = [{"id":"campaign-1","title":"First campaign"}];

export var transport = {
  "list":function (){
    return JSON.parse(JSON.stringify(rows));
  },
  "call":function (operation,values){
    if(!(operation == "save-campaign")){
      throw new Error("Unknown action");
    }
    if(!values.i_title.trim()){
      throw new Error("Campaign title is required");
    }
    let row = rows.find(function (row){
      return row.id == values.i_id;
    });
    if(!row){
      throw new Error("Campaign unavailable");
    }
    row.title = values.i_title;
    return row;
  }
};

export var owner = model.open(transport);

export async function handleMessage(message){
  try{
    let result = null;
    if(message.action == "load"){
      result = await model.loadRows(owner);
    }
    else if(message.action == "save"){
      result = await model.save(owner,message.id,message.title);
    }
    else if(message.action == "close"){
      result = model.close(owner);
    }
    else{
      throw new Error("Unknown action");
    }
    return {"request":message.request,"result":result};
  }
  catch(error){
    return {"request":message.request,"error":error.message};
  }
}

if(!((typeof self) == "undefined")){
  self.onmessage = async function (event){
    self.postMessage(await handleMessage(event.data));
  };
}

export default handleMessage