package util;

import com.fv.sdp.model.Match;
import com.fv.sdp.util.ConcurrentList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by filip on 19/05/2017.
 */
public class TestConcurrentList
{
    @Test
    public void testContain()
    {
        ConcurrentList<Match> matchList = new ConcurrentList<Match>();
        matchList.add(new Match("m1",0,0));
        assertEquals(true, matchList.contain(new Match("m1",67,3)));
        assertEquals(false, matchList.contain(new Match("g2",56,23)));
    }

    @Test
    public void testGetElement()
    {
        ConcurrentList<Match> matchList = new ConcurrentList<Match>();
        Match m1 = new Match("m1",0,0);
        matchList.add(m1);
        assertEquals(m1, matchList.getElement(m1));
        assertEquals(null, matchList.getElement(new Match("asd",0,0)));
    }

}
