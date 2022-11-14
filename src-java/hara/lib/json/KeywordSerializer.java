package hara.lib.json;

import clojure.lang.Keyword;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class KeywordSerializer extends StdSerializer<Keyword> {
  private final boolean writeFieldName;

  public KeywordSerializer(boolean writeFieldName) {
    super(KeywordSerializer.class, true);
    this.writeFieldName = writeFieldName;
  }

  @Override
  public void serialize(Keyword value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    String text = value.sym.toString();
    if (writeFieldName) {
      gen.writeFieldName(text);
    } else {
      gen.writeString(text);
    }
  }
}
