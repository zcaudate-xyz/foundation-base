package hara.lib.image;

import java.awt.image.DataBuffer;
import java.nio.ByteBuffer;

public class BufferedDataBuffer extends DataBuffer {
    protected ByteBuffer buffer;

    public BufferedDataBuffer(ByteBuffer buffer) {
        super(TYPE_BYTE, buffer.capacity());
        this.buffer = buffer;   
    }

    public int getElem(int bank, int i) {
        return buffer.get(i);
    }

    public void setElem(int bank, int i, int val) {
        buffer.put(i, (byte)val);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}