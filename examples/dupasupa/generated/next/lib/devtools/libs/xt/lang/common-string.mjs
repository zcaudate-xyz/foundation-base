export function get_char(s,i){
  return s.charCodeAt(i);
}

export function split(s,tok){
  return s.split(tok);
}

export function join(s,arr){
  return arr.join(s);
}

export function replace(s,tok,replacement){
  return s.replace(new RegExp(tok,"g"),replacement);
}

export function index_of(s,tok){
  return s.indexOf(tok) - 0;
}

export function substring(s,start,finish){
  return s.substring(start,finish);
}

export function to_uppercase(s){
  return s.toUpperCase();
}

export function to_lowercase(s){
  return s.toLowerCase();
}

export function to_fixed(n,digits){
  return n.toFixed(digits);
}

export function trim(s){
  return s.trim();
}

export function trim_left(s){
  return s.trimLeft();
}

export function trim_right(s){
  return s.trimRight();
}

export function sym_full(ns,name){
  if(null == ns){
    return name;
  }
  else{
    return ns + "/" + name;
  }
}

export function sym_name(sym){
  let idx = sym.indexOf("/");
  if(0 < idx){
    return (sym.split("/"))[sym.split("/").length + -1];
  }
  else{
    return sym;
  }
}

export function sym_ns(sym){
  let idx = sym.indexOf("/");
  if(0 < idx){
    return (sym.split("/"))[0];
  }
  else{
    return null;
  }
}

export function sym_pair(sym){
  return [sym_ns(sym),sym_name(sym)];
}

export function starts_withp(s,match){
  if(match.length > s.length){
    return false;
  }
  return s.substring(0,match.length) == match;
}

export function ends_withp(s,match){
  if(match.length > s.length){
    return false;
  }
  return match == s.substring(s.length - match.length,s.length);
}

export function capitalize(s){
  return s.substring(0,1).toUpperCase() + s.substring(1);
}

export function decapitalize(s){
  return s.substring(0,1).toLowerCase() + s.substring(1);
}

export function pad_left(s,n,ch){
  let l = n - s.length;
  let out = s;
  for(let i = 0; i < l; i = (i + 1)){
    out = (ch + out);
  };
  return out;
}

export function pad_right(s,n,ch){
  let l = n - s.length;
  let out = s;
  for(let i = 0; i < l; i = (i + 1)){
    out = (out + ch);
  };
  return out;
}

export function pad_lines(s,n,ch){
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

export function split_long(s,line_len){
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

export function str_rand(n){
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

export function tag_string(tag){
  let pair = sym_pair(tag);
  let [ns,name] = pair;
  let parts = ((null == ns) ? "" : ns).split(".");
  let part_count = parts.length;
  let desc = (null != ns) ? (parts[part_count + -1] + " ") : "";
  let clean_name = ((null == name) ? "" : name).replace(new RegExp("_","g")," ");
  clean_name = clean_name.replace(new RegExp("-","g")," ");
  clean_name = clean_name.replace(new RegExp(desc.trim(),"g"),"");
  return desc + clean_name;
}