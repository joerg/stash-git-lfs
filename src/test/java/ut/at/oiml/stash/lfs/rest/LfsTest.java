package ut.at.oiml.bitbucket.lfs.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import at.oiml.bitbucket.lfs.rest.Lfs;
import at.oiml.bitbucket.lfs.rest.LfsModel;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;

public class LfsTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {
        /*Lfs resource = new Lfs();

        Response response = resource.getMessage("Hello World");
        final LfsModel message = (LfsModel) response.getEntity();

        assertEquals("wrong message","Hello World",message.getMessage());
        */
    }
}
