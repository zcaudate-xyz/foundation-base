package newlisp.lib.foundation;

public class Counter implements clojure.lang.IDeref{

    private int value;

    public Counter(int value){
      this.value = value;
    }
    
    public int inc(){
      return this.value += 1;
    }
    
    public int inc(int n){
      return this.value += n;
    }
    
    public int dec(){
      return this.value -= 1;
    }
    
    public int dec(int n){
      return this.value -= n;
    }
    
    public int reset(int value){
      this.value = value;
      return value;
    }
    
    public Object deref(){
      return this.value;
    }

}
