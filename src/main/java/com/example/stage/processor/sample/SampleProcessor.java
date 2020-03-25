
package com.example.stage.processor.sample;

import com.example.stage.lib.sample.Errors;

import com.example.stage.model.Card;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.SingleLaneRecordProcessor;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public abstract class SampleProcessor extends SingleLaneRecordProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(SampleProcessor.class);

    /**
     * Gives access to the UI configuration of the stage provided by the {@link SampleDProcessor} class.
     */
    public abstract String getConfig();

    public RedissonClient redisson;
    private RestTemplate restTemplate;

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected List<ConfigIssue> init() {
        // Validate configuration values and open any required resources.
        List<ConfigIssue> issues = super.init();
        LOG.info("Init  start:");

        if (getConfig().equals("invalidValue")) {
            issues.add(
                getContext().createConfigIssue(
                    Groups.SAMPLE.name(), "config", Errors.SAMPLE_00, "Here's what's wrong..."
                )
            );
        }

        restTemplate = new RestTemplate();

        LOG.info("Init  after validate:");

//      Config config = new Config();
//      config
//          .useSingleServer()
//          .setAddress("redis://redis:6379")
//          .setConnectTimeout(50000);
//
//      redisson = Redisson.create(config);

        LOG.info("Init  rediss config init1:");
        LOG.info("Init  rediss config init2: {}", redisson);

        // If issues is not empty, the UI will inform the user of each configuration issue in the list.
        return issues;
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        super.destroy();

        if (redisson != null)
            redisson.shutdown();
    }

    /** {@inheritDoc} */
    @Override
    protected void process(Record record, SingleLaneBatchMaker batchMaker) throws StageException {
        LOG.info("Input record: {}", record);

        final Field creditNumberField = record.get("/credit_card");

        if (creditNumberField != null && creditNumberField.getValueAsString() != null
            && !creditNumberField.getValueAsString().equals("")) {
            LOG.info("Credit card url is: {}", "http://cardservice:8080/card/" + creditNumberField);

            final Card card = restTemplate.getForObject("http://cardservice:8080/card/" + creditNumberField.getValueAsString(),
                Card.class);

            assert card != null;
            LOG.info("Credit card issuedBy: {}", card.getIssuedBy());

            record.set("/issued_by", Field.create(card.getIssuedBy()));
        }

//      RBucket<String> bucket = redisson.getBucket("simpleObject");
//      bucket.set("This is object value");
//      RMap<String, String> map = redisson.getMap("simpleMap");
//      map.put("mapKey", "This is map value");
//      String objectValue = bucket.get();
//      System.out.println("stored object value: " + objectValue);
//      String mapValue = map.get("mapKey");
//      System.out.println("stored map value: " + mapValue);

        LOG.info("Output record: {}", record);

        batchMaker.addRecord(record);
    }
}