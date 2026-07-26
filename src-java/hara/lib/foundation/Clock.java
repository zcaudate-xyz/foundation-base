package newlisp.lib.foundation;

public class Clock {

    private final static Clock _clock;

    static {
        _clock = new Clock();
    }

    // Wall clock set at initialization time
    private final long tw0;
    // monotonic clock start to calculate offset
    private final long tm0;

    private Clock(){
        tw0 = System.currentTimeMillis() * 1000000;
        // typically 36 nanos, between these two lines.
        tm0 = System.nanoTime();
    }

    public static final long currentTimeNanos(){
        return _clock.tw0 + (System.nanoTime() - _clock.tm0);
    }

    public static final long currentTimeMicros(){
        return currentTimeNanos() / 1000;
    }

    public static final long currentTimeMillis(){
        return currentTimeNanos() / 1000000;
    }

}
