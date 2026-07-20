const xtd = require("@xtalk/lang/common-data.js")

function sort_edges_build(nodes,edge){
  let n_from = edge[0];
  let n_to = edge[1];
  if(!(null != nodes[n_from])){
    nodes[n_from] = {"id":n_from,"links":[]};
  }
  if(!(null != nodes[n_to])){
    nodes[n_to] = {"id":n_to,"links":[]};
  }
  let links = nodes[n_from]["links"];
  links.push(n_to);
}

function sort_edges_visit(nodes,visited,sorted,id,ancestors){
  if(true == visited[id]){
    return;
  }
  let node = nodes[id];
  if(null == node){
    throw "Not available: " + id;
  }
  if(null == ancestors){
    ancestors = [];
  }
  ancestors.push(id);
  visited[id] = true;
  let input = node["links"];
  for(let link of input){
    sort_edges_visit(nodes,visited,sorted,link,xtd.arr_clone(ancestors));
  };
  sorted.unshift(id);
}

function sort_edges(edges){
  let nodes = {};
  let sorted = [];
  let visited = {};
  for(let e of edges){
    sort_edges_build(nodes,e);
  };
  for(let id of Object.keys(nodes)){
    sort_edges_visit(nodes,visited,sorted,id,null);
  };
  return sorted;
}

function sort_topo(input){
  let edges = [];
  for(let link of input){
    let root = link[0];
    let deps = link[1];
    for(let d of deps){
      edges.push([root,d]);
    };
  };
  let sorted = sort_edges(edges);
  let out = [];
  for(let i = sorted.length; i > 0; i = (i + -1)){
    out.push(sorted[i + -1]);
  };
  return out;
}

module.exports = {
  ["sort_edges_build"]:sort_edges_build,
  ["sort_edges_visit"]:sort_edges_visit,
  ["sort_edges"]:sort_edges,
  ["sort_topo"]:sort_topo
}