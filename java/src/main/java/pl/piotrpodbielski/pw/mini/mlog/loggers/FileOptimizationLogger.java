package pl.piotrpodbielski.pw.mini.mlog.loggers;

import org.json.JSONObject;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FileOptimizationLogger implements IOptimizationLogger {
    final private Object addingLock = new Object();
    final private Object storingLock = new Object();
    final private static int SAMPLES_BUFFER = 10001;

    private List<ValuedSample> samplesToStore;
    private List<ValuedSample> samples;
    private boolean isFirstSample;

    private String fileName;
    private String loggedAlgorithmName;
    private Map<String, Object> attrs;

    public FileOptimizationLogger(String fileName, String loggedAlgorithmName, Map<String, Object> attrs) {
        this.fileName = fileName;
        this.loggedAlgorithmName = loggedAlgorithmName;
        this.attrs = attrs;
    }

    @Override
    public void resetLogger() {
        synchronized (addingLock)
        {
            samples = new ArrayList<ValuedSample>();
        }
        synchronized (storingLock)
        {
            isFirstSample = true;

            File file = new File(fileName);

            if (file.exists()) {
                if (!file.delete()) {
                    throw new RuntimeException("File couldn't be deleted.");
                }
            }

            file = new File(fileName.replace(".csv", ".json"));

            if (file.exists()) {
                if (!file.delete()) {
                    throw new RuntimeException("File couldn't be deleted.");
                }
            }

            try (FileWriter fileWriter = new FileWriter(file, false); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(new JSONObject(this.attrs).toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            samplesToStore = new ArrayList<ValuedSample>();
        }
    }

    @Override
    public void flushSamples() {
        synchronized (addingLock)
        {
            synchronized (storingLock)
            {
                samplesToStore.addAll(samples);
                Thread storingThread = new Thread(new StoreSamples());
                storingThread.start();
            }
            samples.clear();
        }
    }

    @Override
    public void logSample(ValuedSample sample) {
        synchronized (addingLock)
        {
            samples.add(sample);
        }

        if (samples.size() >= SAMPLES_BUFFER)
        {
            flushSamples();
        }
    }

    private class StoreSamples implements Runnable {
        @Override
        public void run() {
            synchronized (storingLock) {
                File file = new File(fileName);

                List<String> lines = new ArrayList<>();

                if (isFirstSample && !samplesToStore.isEmpty()) {
                    String separatedVariablesList = IntStream.rangeClosed(1, samplesToStore.get(0).getX().length)
                            .mapToObj(Integer::toString)
                            .map(s -> "X" + s)
                            .collect(Collectors.joining("\t"));

                    lines.add(String.format("Iteration\tStep\tName\tValue\tBest value\t%s", separatedVariablesList));
                    isFirstSample = false;
                }

                List<String> samplesLined = samplesToStore.parallelStream()
                        .map(sample -> {
                            String separatedVariablesList = Arrays.stream(sample.getX()).boxed()
                                    .map(Object::toString)
                                    .collect(Collectors.joining("\t"));

                            return String.format("%d\t%d\t%s\t%f\t%f\t%s", sample.getIteration(), sample.getStep(), loggedAlgorithmName, sample.getValue(), sample.getBestValue(), separatedVariablesList);
                        }).collect(Collectors.toList());

                lines.addAll(samplesLined);

                try (FileWriter fileWriter = new FileWriter(file, true); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

                    for (String line : lines) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                samplesToStore.clear();
            }
        }
    }
}
