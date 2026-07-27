package hara.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import java.util.function.Consumer;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class WSUtil {

    public static ByteBuffer WSEncode(byte opcode, byte[] data, int length) {
        byte b0 = 0;
        b0 |= 1 << 7; // FIN
        b0 |= opcode;
        ByteBuffer buffer = ByteBuffer.allocate(length + 10); // max
        buffer.put(b0);

        if (length <= 125) {
            buffer.put((byte) (length));
        } else if (length <= 0xFFFF) {
            buffer.put((byte) 126);
            buffer.putShort((short) length);
        } else {
            buffer.put((byte) 127);
            buffer.putLong(length);
        }
        buffer.put(data, 0, length);
        buffer.flip();
        return buffer;
    }

    public static void WSHandshake(
				InputStream inputStream, 
				OutputStream outputStream
		) throws UnsupportedEncodingException {
        String data = new Scanner(inputStream,"UTF-8").useDelimiter("\\r\\n\\r\\n").next();

        Matcher get = Pattern.compile("^GET").matcher(data);

        if (get.find()) {
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();                 

            byte[] response = null;
            try {
                response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + DatatypeConverter.printBase64Binary(
                                MessageDigest
                                .getInstance("SHA-1")
                                .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                        .getBytes("UTF-8")))
                        + "\r\n\r\n")
                        .getBytes("UTF-8");
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                outputStream.write(response, 0, response.length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {

        }
    }
}