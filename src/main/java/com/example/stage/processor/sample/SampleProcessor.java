/**
 * Copyright 2015 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.stage.processor.sample;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.example.stage.lib.sample.Errors;

import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.FileRef;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.OnRecordErrorException;
import com.streamsets.pipeline.api.base.SingleLaneRecordProcessor;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class SampleProcessor extends SingleLaneRecordProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(SampleProcessor.class);
  /**
   * Gives access to the UI configuration of the stage provided by the {@link SampleDProcessor} class.
   */
  public abstract String getConfig();

  public RedissonClient redisson;

  /** {@inheritDoc} */
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

      LOG.info("Init  after validate:");

      Config config = new Config();
      config.useSingleServer().setAddress("redis://redis:6379");
      redisson = Redisson.create(config);

      LOG.info("Init  rediss config init1:");
      LOG.info("Init  rediss config init2: {}", redisson);

    // If issues is not empty, the UI will inform the user of each configuration issue in the list.
    return issues;
  }

  /** {@inheritDoc} */
  @Override
  public void destroy() {
      if (redisson != null)
          redisson.shutdown();

      LOG.info("!!!!!!   Test Destroy ");
    // Clean up any open resources.
    super.destroy();
  }

  /** {@inheritDoc} */
  @Override
  protected void process(Record record, SingleLaneBatchMaker batchMaker) throws StageException {
    LOG.info("Input record: {}", record);

      for (String fieldPath : record.getEscapedFieldPaths()) {
          Field field = record.get(fieldPath);
          if (field.getType() == Field.Type.STRING) {
              String reversed = (new StringBuilder(field.getValueAsString())).reverse().toString();
              record.set(fieldPath + ".reversed", Field.create(reversed));
          }
      }

      RBucket<String> bucket = redisson.getBucket("simpleObject");
      bucket.set("This is object value");
      RMap<String, String> map = redisson.getMap("simpleMap");
      map.put("mapKey", "This is map value");
      String objectValue = bucket.get();
      System.out.println("stored object value: " + objectValue);
      String mapValue = map.get("mapKey");
      System.out.println("stored map value: " + mapValue);


      LOG.info("Output record: {}", record);

    batchMaker.addRecord(record);
  }

}