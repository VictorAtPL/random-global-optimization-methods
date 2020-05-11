package pl.piotrpodbielski.pw.mini.mlog.loggers;

import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

public interface IOptimizationLogger {
    void flushSamples();
    void resetLogger();
    void logSample(ValuedSample sample);
}
