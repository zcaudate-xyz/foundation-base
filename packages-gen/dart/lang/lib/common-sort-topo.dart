sort_edges_build(nodes, edge) {
  var n_from = edge[0];
  var n_to = edge[1];
  if(!nodes.containsKey(n_from)){
    nodes[n_from] = <dynamic, dynamic>{"id":n_from,"links":<dynamic>[]};
  }
  if(!nodes.containsKey(n_to)){
    nodes[n_to] = <dynamic, dynamic>{"id":n_to,"links":<dynamic>[]};
  }
  var links = nodes[n_from]["links"];
  return links.add(n_to);
}

sort_edges_visit(nodes, visited, sorted, id, ancestors) {
  if(true == visited[id]){
    return;
  }
  var node = nodes[id];
  if(null == node){
    throw "Not available: " + id;
  }
  if(null == ancestors){
    ancestors = <dynamic>[];
  }
  ancestors.add(id);
  visited[id] = true;
  var input = node["links"];
  var arr_39661 = input;
  for(var i39662 = 0; i39662 < arr_39661.length; ++i39662){
    var link = arr_39661[i39662];
    sort_edges_visit(
      nodes,
      visited,
      sorted,
      link,
      xt.lang.common_data.arr_clone(ancestors)
    );
  };
  return sorted.insert(0,id);
}

sort_edges(edges) {
  var nodes = <dynamic, dynamic>{};
  var sorted = <dynamic>[];
  var visited = <dynamic, dynamic>{};
  var arr_39683 = edges;
  for(var i39684 = 0; i39684 < arr_39683.length; ++i39684){
    var e = arr_39683[i39684];
    sort_edges_build(nodes,e);
  };
  for(var id in nodes.keys){
    sort_edges_visit(nodes,visited,sorted,id,null);
  };
  return sorted;
}

sort_topo(input) {
  var edges = <dynamic>[];
  var arr_39705 = input;
  for(var i39706 = 0; i39706 < arr_39705.length; ++i39706){
    var link = arr_39705[i39706];
    var root = link[0];
    var deps = link[1];
    var arr_39727 = deps;
    for(var i39728 = 0; i39728 < arr_39727.length; ++i39728){
      var d = arr_39727[i39728];
      edges.add(<dynamic>[root,d]);
    };
  };
  var sorted = sort_edges(edges);
  var out = <dynamic>[];
  for(var i = sorted.length; i > 0; i = (i + -1)){
    out.add(sorted[i + -1]);
  };
  return out;
}