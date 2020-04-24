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

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.locator.SeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class ExternalFileSeedProvider implements SeedProvider {

    private static final Logger logger = LoggerFactory.getLogger(ExternalFileSeedProvider.class);

    private Optional<String> seedFile = Optional.empty();

    public ExternalFileSeedProvider(Map<String, String> args) {
        String filename = args.get("filename");
        if (filename == null) {
            logger.warn("External file seed provider initialized with no filename, ignoring");
        } else {
            seedFile = Optional.of(filename);
            logger.info("Will read seeds from: {}", seedFile);
        }
    }

    @Override
    public List<InetAddress> getSeeds() {
        Optional<String> filename = getFileName();
        if (filename.isPresent()) {
            return unmodifiableList(readSeeds(filename.get()));
        } else {
            logger.error("No external seed file has been configured, no seeds available.");
            return emptyList();
        }
    }

    private Optional<String> getFileName() {
        if (seedFile.isPresent()) {
            return seedFile;
        }

        Config conf = DatabaseDescriptor.loadConfig();
        return Optional.ofNullable(conf.seed_provider.parameters.get("filename"));
    }

    private List<InetAddress> readSeeds(String filename) {
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            return lines.map(line -> line.replaceAll("#.*$", "").trim())
                    .filter(line -> !line.isEmpty())
                    .map(this::getIPByName)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());

        } catch (IOException e) {
            logger.warn("Unable to read seed file '{}'", filename, e);
            return emptyList();
        }
    }

    private Optional<InetAddress> getIPByName(String address) {
        try {
            InetAddress ip = InetAddress.getByName(address);
            logger.debug("Adding host {}", address);
            return Optional.of(ip);
        } catch (UnknownHostException ex) {
            logger.warn("Seed provider couldn't lookup host {}", address);
            return Optional.empty();
        }
    }
}
