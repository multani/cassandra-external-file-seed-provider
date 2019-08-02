/*
 * Licensed to the Apache Software Foundation (ASF) under one
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
package info.multani.cassandra;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.locator.SeedProvider;
import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalFileSeedProvider implements SeedProvider {

    private static final Logger logger = LoggerFactory.getLogger(ExternalFileSeedProvider.class);

    String seedFile;

    public ExternalFileSeedProvider(Map<String, String> args) {
        String filename = args.get("filename");
        if (filename == null) {
            logger.warn("External file seed provider initialized with no filename, ignoring");
        } else {
            seedFile = filename;
            logger.info("Will read seeds from: {}", seedFile);
        }
    }

    @Override
    public List<InetAddress> getSeeds() {
        String filename = seedFile;

        if (filename == null) {
            Config conf = DatabaseDescriptor.loadConfig();

            filename = conf.seed_provider.parameters.get("filename");
            if (filename == null) {
                logger.error("No external seed file has been configured, no seeds available.");
                return Collections.emptyList();
            }
        }

        List<InetAddress> seeds = new ArrayList<>();
        logger.info("Reading seeds from file: {}", filename);

        try {
            seeds = readSeeds(filename);
        } catch (IOException e) {
            logger.warn("Unable to read seed file '{}'", filename, e);
        }

        return Collections.unmodifiableList(seeds);
    }

    private List<InetAddress> readSeeds(String filename) throws IOException {
        List<InetAddress> seeds = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(filename));

        String line;
        while ((line = reader.readLine()) != null) {
            String address = line.replaceAll("#.*$", "").trim();

            if (address.length() == 0) {
                // Skipping empty lines
                continue;
            }

            try {
                seeds.add(InetAddress.getByName(address));
                logger.debug("Adding host {}", address);
            } catch (UnknownHostException ex) {
                logger.warn("Seed provider couldn't lookup host {}", address);
            }
        }

        reader.close();
        return seeds;
    }
}
