package it.at.oiml.bitbucket.lfs.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import at.oiml.bitbucket.lfs.rest.Lfs;
import at.oiml.bitbucket.lfs.rest.LfsModel;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class LfsFuncTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {

        // String baseUrl = System.getProperty("baseurl");
        // String resourceUrl = baseUrl + "/rest/lfs/1.0/message";
        //
        // RestClient client = new RestClient();
        // Resource resource = client.resource(resourceUrl);
        //
        // LfsModel message = resource.get(LfsModel.class);
        //
        // assertEquals("wrong message","Hello World",message.getMessage());
    }
}
