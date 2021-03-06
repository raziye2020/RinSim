/**
 * 
 */
package rinde.sim.scenario;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static rinde.sim.scenario.ScenarioTest.TestEvents.EVENT_A;
import static rinde.sim.scenario.ScenarioTest.TestEvents.EVENT_B;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import rinde.sim.core.graph.Point;
import rinde.sim.util.IO;

/**
 * @author Rinde van Lon (rinde.vanlon@cs.kuleuven.be)
 * 
 */
public class ScenarioTest {

    enum TestEvents {
        EVENT_A, EVENT_B;
    }

    // @Ignore
    @Test
    public void testReadWrite() {
        final List<Point> points = new ArrayList<Point>();
        for (int i = 0; i < 1000; i++) {
            points.add(new Point(Math.random(), Math.random()));
        }

        final Scenario original = randomScenario(new MersenneTwister(123), 10, points);

        assertEquals(10, original.size());

        IO.serialize(original, "files/original.scen");
        final Scenario copied = IO
                .deserialize("files/original.scen", Scenario.class);

        assertEquals(10, copied.size());

        assertEquals(original, copied);

        IO.serialize(copied, "files/copied.scen");
        assertEquals(IO.deserialize("files/original.scen", Scenario.class), IO.deserialize("files/copied.scen", Scenario.class));

        (new File("files/original.scen")).delete();
        (new File("files/copied.scen")).delete();
    }

    @Test
    public void equals() {
        final List<TimedEvent> events1 = newArrayList(new TimedEvent(EVENT_A, 0));
        final List<TimedEvent> events2 = newArrayList(new TimedEvent(EVENT_A, 0));
        final List<TimedEvent> events3 = newArrayList(new TimedEvent(EVENT_A, 1));
        final List<TimedEvent> events4 = newArrayList(new TimedEvent(EVENT_A, 1), new TimedEvent(
                EVENT_A, 2));

        assertFalse(new Scenario(events1).equals(new Object()));
        assertTrue(new Scenario(events1).equals(new Scenario(events2)));
        assertFalse(new Scenario(events1).equals(new Scenario(events3)));
        assertFalse(new Scenario(events1).equals(new Scenario(events4)));
    }

    @Test
    public void testSorting() {
        final List<TimedEvent> events = new ArrayList<TimedEvent>(10);
        final AddObjectEvent A1 = new AddObjectEvent(0, new Point(1, 0));
        final AddObjectEvent A2 = new AddObjectEvent(0, new Point(2, 0));
        final AddObjectEvent B = new AddObjectEvent(1, new Point(1, 1));
        final AddObjectEvent C = new AddObjectEvent(2, new Point(1, 0));
        final AddObjectEvent D1 = new AddObjectEvent(3, new Point(1, 2));
        final AddObjectEvent D2 = new AddObjectEvent(3, new Point(1, 3));
        final AddObjectEvent E = new AddObjectEvent(4, new Point(2, 0));
        final AddObjectEvent F = new AddObjectEvent(5, new Point(4, 0));
        events.addAll(asList(A1, A2, B, C, D1, D2, E, F));
        Collections.reverse(events);

        final ScenarioBuilder builder = new ScenarioBuilder(A1.getEventType());
        builder.addEvents(events);

        final Scenario s = builder.build();
        final List<TimedEvent> res = s.asList();

        assertEquals(asList(A2, A1, B, C, D2, D1, E, F), res);
        assertFalse(res.equals(events));
        assertEquals(events.size(), res.size());
        Collections.reverse(res);
        assertEquals(res, events);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorFail1() {
        final List<TimedEvent> events = newArrayList();
        new Scenario(events, new HashSet<Enum<?>>());
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorFail2() {
        new Scenario(asList(new TimedEvent(EVENT_A, 1L)),
                new HashSet<Enum<?>>());
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorFail3() {
        new Scenario(new ArrayList<TimedEvent>());
    }

    @Test
    public void testCreateScenarioByCopying() {

        final Scenario s = new ScenarioBuilder(EVENT_A)
                .addEvent(new AddObjectEvent(100, new Point(0, 0)))
                .addEvent(new AddObjectEvent(200, new Point(0, 0)))
                .addEvent(new AddObjectEvent(300, new Point(0, 0))).build();

        assertEquals(3, s.asList().size());

        final Scenario s2 = new Scenario(s);

        assertEquals(3, s.asList().size());
        assertEquals(3, s2.asList().size());

        // assertEquals(s.peek(), s2.peek());
        // final TimedEvent sP0 = s.poll();
        //
        // assertEquals(2, s.asList().size());
        // assertEquals(3, s2.asList().size());
        //
        // final TimedEvent s2P0 = s2.poll();
        //
        // assertEquals(2, s.asList().size());
        // assertEquals(2, s2.asList().size());
        //
        // assertEquals(sP0, s2P0);

    }

    @Test
    public void timedEventEquals() {
        assertFalse(new AddObjectEvent(10, new Point(10, 0))
                .equals(new TimedEvent(EVENT_A, 10)));
        assertFalse(new TimedEvent(EVENT_A, 10).equals(null));
        assertFalse(new TimedEvent(EVENT_A, 10).equals(new TimedEvent(EVENT_B,
                10)));
        assertTrue(new TimedEvent(EVENT_B, 10).equals(new TimedEvent(EVENT_B,
                10)));
    }

    public static Scenario randomScenario(RandomGenerator gen, int numTrucks,
            List<Point> positions) {
        final ScenarioBuilder res = new ScenarioBuilder(EVENT_A);
        final int size = positions.size();
        for (int i = 0; i < numTrucks; i++) {
            res.addEvent(new AddObjectEvent(0L,
                    positions.get(gen.nextInt(size))));
        }
        return res.build();
    }
}

class AddObjectEvent extends TimedEvent {

    private static final long serialVersionUID = 5946753206998904050L;

    public final Point pos;

    public AddObjectEvent(String[] parts) {
        this(Long.parseLong(parts[1]), Point.parsePoint(parts[2]));
    }

    public AddObjectEvent(long pTime, Point pPos) {
        super(EVENT_A, pTime);
        pos = pPos;
        hashCode();
        toString();
    }

    @Override
    public String toString() {
        return super.toString() + "|" + pos;
    }

}
