mod_pos(val, modulo) {
  var out = val % modulo;
  return (out < 0) ? (out + modulo) : out;
}

mod_offset(pval, nval, modulo) {
  var offset = nval - pval % modulo;
  if((offset).abs() > (modulo / 2)){
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

gcd(a, b) {
  return (0 == b) ? a : gcd(b,a % b);
}

lcm(a, b) {
  return (a * b) / gcd(a,b);
}

mix(x0, x1, v) {
  return x0 + ((x1 - x0) * v);
}

sign(x) {
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

round(x) {
  return (x + 0.5).floor();
}

clamp(min, max, v) {
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