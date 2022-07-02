/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.multani.cassandra;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.locator.SeedProvider;
import org.apache.cassandra.locator.InetAddressAndPort;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jballet
 */
public class ExternalFileSeedProviderTest {

    @Test
    public void testGetSeedsFromGlobalConfig() throws IOException {
        String configFile = getResourcePath("external-seeds.yaml");
        System.setProperty("cassandra.config", "file://" + configFile);

        // TODO works only once?
        //could be initlize once in @BeforeClass
        DatabaseDescriptor.daemonInitialization();

        SeedProvider p = new ExternalFileSeedProvider(Collections.emptyMap());

        List<InetAddressAndPort> seeds = p.getSeeds();
        assertEquals(3, seeds.size());
        assertEquals(InetAddressAndPort.getByName("127.0.0.42"), seeds.get(0));
        assertEquals(InetAddressAndPort.getByName("127.0.0.43"), seeds.get(1));
        assertEquals(InetAddressAndPort.getByName("127.0.0.44"), seeds.get(2));
    }

    @Test
    public void testGetSeedsWithComments() throws IOException {
        String configFile = getResourcePath("seeds-comments.list");

        Map<String, String> parameters = Collections.singletonMap("filename", configFile);

        SeedProvider p = new ExternalFileSeedProvider(parameters);

        List<InetAddressAndPort> seeds = p.getSeeds();
        assertEquals(4, seeds.size());
        assertEquals(InetAddressAndPort.getByName("127.1.0.42"), seeds.get(0));
        assertEquals(InetAddressAndPort.getByName("127.1.0.44"), seeds.get(1));
        assertEquals(InetAddressAndPort.getByName("127.1.0.43"), seeds.get(2));
        assertEquals(InetAddressAndPort.getByName("127.1.0.45"), seeds.get(3));
    }

    String getResourcePath(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(resourceName).getFile();
    }
}
