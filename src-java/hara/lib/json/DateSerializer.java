package hara.lib.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

public class DateSerializer extends StdSerializer<Date> {
  private final SimpleDateFormat formatter;

  public DateSerializer(String dateFormat) {
    super(DateSerializer.class, true);
    formatter = new SimpleDateFormat(dateFormat);
    formatter.setTimeZone(new SimpleTimeZone(0, "UTC"));
  }

  public DateSerializer() {
    this("yyyy-MM-dd'T'HH:mm:ss'Z'");
  }

  @Override
  public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    // TODO: use something like jackson-datatype-jsr310?
    // SimpleDateFormat is not thread-safe, so we must synchronize it.
    synchronized (formatter) {
      gen.writeString(formatter.format(value));
    }
  }
}
