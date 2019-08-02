/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.multani.cassandra;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.locator.SeedProvider;
import org.junit.Test;

/**
 *
 * @author jballet
 */
public class ExternalFileSeedProviderTest {

    public ExternalFileSeedProviderTest() {
    }

    String getResourcePath(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource(resourceName).getFile();
    }

    /**
     * Test of getSeeds method, of class ExternalFileSeedProvider.
     * 
     * @throws IOException
     */
    @Test
    public void testGetSeedsFromGlobalConfig() throws IOException {
        String configFile = getResourcePath("external-seeds.yaml");
        System.setProperty("cassandra.config", "file://" + configFile);

        // TODO works only once?
        DatabaseDescriptor.daemonInitialization();

        SeedProvider p = new ExternalFileSeedProvider(Collections.emptyMap());

        List<InetAddress> seeds = p.getSeeds();
        assertEquals(3, seeds.size());
        assertEquals(InetAddress.getByName("127.0.0.42"), seeds.get(0));
        assertEquals(InetAddress.getByName("127.0.0.43"), seeds.get(1));
        assertEquals(InetAddress.getByName("127.0.0.44"), seeds.get(2));
    }

    @Test
    public void testGetSeedsWithComments() throws IOException {
        String configFile = getResourcePath("seeds-comments.list");

        Map<String, String> parameters = Collections.singletonMap("filename", configFile);

        SeedProvider p = new ExternalFileSeedProvider(parameters);

        List<InetAddress> seeds = p.getSeeds();
        assertEquals(4, seeds.size());
        assertEquals(InetAddress.getByName("127.1.0.42"), seeds.get(0));
        assertEquals(InetAddress.getByName("127.1.0.44"), seeds.get(1));
        assertEquals(InetAddress.getByName("127.1.0.43"), seeds.get(2));
        assertEquals(InetAddress.getByName("127.1.0.45"), seeds.get(3));
    }
}
