function get_char(s,i){
  return s.charCodeAt(i);
}

function split(s,tok){
  return s.split(tok);
}

function join(s,arr){
  return arr.join(s);
}

function replace(s,tok,replacement){
  return s.replace(new RegExp(tok,"g"),replacement);
}

function index_of(s,tok){
  return s.indexOf(tok) - 0;
}

function substring(s,start,finish){
  return s.substring(start,finish);
}

function to_uppercase(s){
  return s.toUpperCase();
}

function to_lowercase(s){
  return s.toLowerCase();
}

function to_fixed(n,digits){
  return n.toFixed(digits);
}

function trim(s){
  return s.trim();
}

function trim_left(s){
  return s.trimLeft();
}

function trim_right(s){
  return s.trimRight();
}

function sym_full(ns,name){
  if(null == ns){
    return name;
  }
  else{
    return ns + "/" + name;
  }
}

function sym_name(sym){
  let idx = sym.indexOf("/");
  if(0 < idx){
    return (sym.split("/"))[sym.split("/").length + -1];
  }
  else{
    return sym;
  }
}

function sym_ns(sym){
  let idx = sym.indexOf("/");
  if(0 < idx){
    return (sym.split("/"))[0];
  }
  else{
    return null;
  }
}

function sym_pair(sym){
  return [sym_ns(sym),sym_name(sym)];
}

function starts_withp(s,match){
  if(match.length > s.length){
    return false;
  }
  return s.substring(0,match.length) == match;
}

function ends_withp(s,match){
  if(match.length > s.length){
    return false;
  }
  return match == s.substring(s.length - match.length,s.length);
}

function capitalize(s){
  return s.substring(0,1).toUpperCase() + s.substring(1);
}

function decapitalize(s){
  return s.substring(0,1).toLowerCase() + s.substring(1);
}

function pad_left(s,n,ch){
  let l = n - s.length;
  let out = s;
  for(let i = 0; i < l; i = (i + 1)){
    out = (ch + out);
  };
  return out;
}

function pad_right(s,n,ch){
  let l = n - s.length;
  let out = s;
  for(let i = 0; i < l; i = (i + 1)){
    out = (out + ch);
  };
  return out;
}

function pad_lines(s,n,ch){
  let lines = s.split("\n");
  let out = "";
  for(let line of lines){
    if(0 < out.length){
      out = (out + "\n");
    }
    out = (out + pad_left("",n," ") + line);
  };
  return out;
}

function split_long(s,line_len){
  if((null == s) || (0 == s.length)){
    return [];
  }
  line_len = ((null == line_len) ? 50 : line_len);
  let total = s.length;
  let lines = Math.ceil(total / line_len);
  let out = [];
  for(let i = 0; i < lines; i = (i + 1)){
    let line = substring(s,i * line_len,(i + 1) * line_len);
    if(0 < line.length){
      out.push(line);
    }
  };
  return out;
}

function str_rand(n){
  let choices = [
    "A",
    "B",
    "C",
    "D",
    "E",
    "F",
    "G",
    "H",
    "I",
    "J",
    "K",
    "L",
    "M",
    "N",
    "O",
    "P",
    "Q",
    "R",
    "S",
    "T",
    "U",
    "V",
    "W",
    "X",
    "Y",
    "Z",
    "a",
    "b",
    "c",
    "d",
    "e",
    "f",
    "g",
    "h",
    "i",
    "j",
    "k",
    "l",
    "m",
    "n",
    "o",
    "p",
    "q",
    "r",
    "s",
    "t",
    "u",
    "v",
    "w",
    "x",
    "y",
    "z",
    "0",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8"
  ];
  let out = "";
  for(let i = 0; i < n; i = (i + 1)){
    let rand_idx = Math.random() * choices.length;
    let idx = Math.floor(rand_idx);
    out = (out + choices[idx]);
  };
  return out;
}

function tag_string(tag){
  let [ns,name] = sym_pair(tag);
  let parts = ((null == ns) ? "" : ns).split(".");
  let part_count = parts.length;
  let desc = (null != ns) ? (parts[part_count + -1] + " ") : "";
  let clean_name = ((null == name) ? "" : name).replace(new RegExp("_","g")," ");
  clean_name = clean_name.replace(new RegExp("-","g")," ");
  clean_name = clean_name.replace(new RegExp(desc.trim(),"g"),"");
  return desc + clean_name;
}

module.exports = {
  ["get_char"]:get_char,
  ["split"]:split,
  ["join"]:join,
  ["replace"]:replace,
  ["index_of"]:index_of,
  ["substring"]:substring,
  ["to_uppercase"]:to_uppercase,
  ["to_lowercase"]:to_lowercase,
  ["to_fixed"]:to_fixed,
  ["trim"]:trim,
  ["trim_left"]:trim_left,
  ["trim_right"]:trim_right,
  ["sym_full"]:sym_full,
  ["sym_name"]:sym_name,
  ["sym_ns"]:sym_ns,
  ["sym_pair"]:sym_pair,
  ["starts_withp"]:starts_withp,
  ["ends_withp"]:ends_withp,
  ["capitalize"]:capitalize,
  ["decapitalize"]:decapitalize,
  ["pad_left"]:pad_left,
  ["pad_right"]:pad_right,
  ["pad_lines"]:pad_lines,
  ["split_long"]:split_long,
  ["str_rand"]:str_rand,
  ["tag_string"]:tag_string
}