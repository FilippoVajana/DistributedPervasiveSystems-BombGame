package util;

import com.fv.sdp.util.RandomIdGenerator;
import org.junit.Test;

/**
 * Created by filip on 6/16/2017.
 */
public class RandomIdGeneratorTest
{
    @Test
    public void getRndTest()
    {
        System.out.println(new RandomIdGenerator().getRndId());

        System.out.println(new RandomIdGenerator().getRndId());

        System.out.println(new RandomIdGenerator().getRndId());
    }
}
