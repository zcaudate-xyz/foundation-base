function mod_pos(val,modulo){
  let out = val % modulo;
  return (out < 0) ? (out + modulo) : out;
}

function mod_offset(pval,nval,modulo){
  let offset = (nval - pval) % modulo;
  if(Math.abs(offset) > (modulo / 2)){
    if(offset > 0){
      return offset - modulo;
    }
    else{
      return offset + modulo;
    }
  }
  else{
    return offset;
  }
}

function gcd(a,b){
  return (0 == b) ? a : gcd(b,a % b);
}

function lcm(a,b){
  return (a * b) / gcd(a,b);
}

function mix(x0,x1,v){
  return x0 + ((x1 - x0) * v);
}

function sign(x){
  if(x == 0){
    return 0;
  }
  else if(x < 0){
    return -1;
  }
  else{
    return 1;
  }
}

function round(x){
  return Math.floor(x + 0.5);
}

function clamp(min,max,v){
  if(v < min){
    return min;
  }
  else if(max < v){
    return max;
  }
  else{
    return v;
  }
}

module.exports = {
  ["mod_pos"]:mod_pos,
  ["mod_offset"]:mod_offset,
  ["gcd"]:gcd,
  ["lcm"]:lcm,
  ["mix"]:mix,
  ["sign"]:sign,
  ["round"]:round,
  ["clamp"]:clamp
}