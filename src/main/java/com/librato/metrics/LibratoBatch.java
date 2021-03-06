package com.librato.metrics;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * User: mihasya
 * Date: 6/14/12
 * Time: 1:51 PM
 * A class that represents an aggregation of metric data from a given run
 */
public class LibratoBatch {
    private static final Logger LOG = LoggerFactory.getLogger(LibratoBatch.class);
    private static final String libVersion;
    static {
        InputStream pomIs = LibratoBatch.class.getClassLoader().getResourceAsStream("META-INF/maven/com.librato.metrics/librato-java/pom.properties");
        BufferedReader b = new BufferedReader(new InputStreamReader(pomIs));
        String version = "unknown";
        try {
            String line = b.readLine();
            while (line != null)  {
                if (line.startsWith("version")) {
                    version = line.split("=")[1];
                    break;
                }
                line = b.readLine();
            }
        } catch (IOException e) {
            LOG.error("Failure reading package version for librato-java", e);
        }
        libVersion = version;
    }

    public static final int DEFAULT_BATCH_SIZE = 500;

    private static final ObjectMapper mapper = new ObjectMapper();

    private final List<Measurement> measurements = new ArrayList<Measurement>();

    private final int postBatchSize;
    private final APIUtil.Sanitizer sanitizer;
    private final long timeout;
    private final TimeUnit timeoutUnit;
    private final String userAgent;


    /**
     *
     * @param postBatchSize size at which to break up the batch
     * @param sanitizer the sanitizer to use for cleaning up metric names to comply with librato api requirements
     * @param timeout time allowed for post
     * @param timeoutUnit unit for timeout
     * @param agentIdentifier a string that identifies the poster (such as the name of a library/program using librato-java)
     */
    public LibratoBatch(int postBatchSize, final APIUtil.Sanitizer sanitizer, long timeout, TimeUnit timeoutUnit, String agentIdentifier) {
        this.postBatchSize = postBatchSize;
        this.sanitizer = new APIUtil.Sanitizer() {
            @Override
            public String apply(String name) {
                return APIUtil.lastPassSanitizer.apply(sanitizer.apply(name));
            }
        };
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.userAgent = String.format("%s librato-java/%s", agentIdentifier, libVersion);
    }

    /**
     * for advanced measurement fu
     */
    public void addMeasurement(Measurement measurement) {
        measurements.add(measurement);
    }

    public void addCounterMeasurement(String name, Long value) {
        measurements.add(new CounterMeasurement(name, value));
    }

    public void addGaugeMeasurement(String name, Number value) {
        measurements.add(new SingleValueGaugeMeasurement(name, value));
    }

    public void post(AsyncHttpClient.BoundRequestBuilder builder, String source, long epoch) {
        Map<String, Object> resultJson = new HashMap<String, Object>();
        resultJson.put("source", source);
        resultJson.put("measure_time", epoch);
        List<Map<String, Object>> gaugeData = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> counterData = new ArrayList<Map<String, Object>>();

        int counter = 0;

        Iterator<Measurement> measurementIterator = measurements.iterator();
        while (measurementIterator.hasNext()) {
            Measurement measurement = measurementIterator.next();
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("name", sanitizer.apply(measurement.getName()));
            // check if this measurement has a source value included
            if(measurement.getSource() != null && !measurement.getSource().trim().isEmpty())
            {
            	data.put("source", sanitizer.apply(measurement.getSource()));
            }
            
            data.putAll(measurement.toMap());
            if (measurement instanceof CounterMeasurement) {
                counterData.add(data);
            } else {
                gaugeData.add(data);
            }
            counter++;
            if (counter % postBatchSize == 0 || (!measurementIterator.hasNext() && (!counterData.isEmpty() || !gaugeData.isEmpty()))) {
                resultJson.put("counters", counterData);
                resultJson.put("gauges", gaugeData);
                postPortion(builder , resultJson);
                resultJson.remove("gauges");
                resultJson.remove("counters");
                gaugeData = new ArrayList<Map<String, Object>>();
                counterData = new ArrayList<Map<String, Object>>();
            }
        }
        LOG.debug("Posted {} measurements", counter);
    }

    private void postPortion(AsyncHttpClient.BoundRequestBuilder builder, Map<String, Object> chunk) {
        try {
            String chunkStr = mapper.writeValueAsString(chunk);
            builder.setBody(chunkStr);
            builder.setHeader("User-Agent", userAgent);
            Future<Response> response = builder.execute();
            Response result = response.get(timeout, timeoutUnit);
            if (result.getStatusCode() < 200 || result.getStatusCode() >= 300) {
                LOG.error("Received an error from Librato API. Code : {}, Message: {}", result.getStatusCode(), result.getResponseBody());
            }
        } catch (Exception e) {
            LOG.error("Unable to post to Librato API", e);
        }
    }
}
