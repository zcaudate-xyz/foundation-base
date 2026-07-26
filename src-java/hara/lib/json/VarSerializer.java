package hara.lib.json;

import clojure.lang.Var;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class VarSerializer extends StdSerializer<Var> {

  public VarSerializer() {
    super(VarSerializer.class, true);
  }

  @Override
  public void serialize(Var value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.toString());
  }
}
