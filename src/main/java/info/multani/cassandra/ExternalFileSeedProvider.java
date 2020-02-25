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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public ExternalFileSeedProvider(Map<String, String> args) {
    }

    @Override
    public List<InetAddress> getSeeds() {
        Config conf = DatabaseDescriptor.loadConfig();

        String filename = conf.seed_provider.parameters.get("filename");
        if (filename == null) {
            throw new AssertionError("no external seed file configured, check your configuration!");
        }

        logger.info("Reading seeds from file: {}", filename);
        FileSystem fs = FileSystems.getDefault();
        Path path = fs.getPath(filename);

        Charset charset = Charset.forName("UTF-8");
        List<String> hosts = new ArrayList<>();
        try {
            hosts = Files.readAllLines(path, charset);
        } catch (IOException e) {
            logger.warn("Unable to read seed file '{}'", path, e);
        }

        List<InetAddress> seeds = new ArrayList<>(hosts.size());
        for (String host : hosts) {
            String address = host.trim();
            if (address.length() == 0) {
                continue;
            }

            logger.debug("Adding host {}", address);
            try {
                seeds.add(InetAddress.getByName(address));
            } catch (UnknownHostException ex) {
                // not fatal... DD will bark if there end up being zero seeds.
                logger.warn("Seed provider couldn't lookup host {}", address);
            }
        }
        return Collections.unmodifiableList(seeds);
    }
}
