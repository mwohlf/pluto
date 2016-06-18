package net.wohlfart.pluto.ai.btree;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.wohlfart.pluto.ai.btree.IBehavior.State;
import net.wohlfart.pluto.entity.IEntityCommand;

@SuppressWarnings({ "FieldCanBeLocal", "InstanceofInterfaces" })
public class SequenceTreeExecutorTest {

    private BehaviorExecutor executor;
    private StateSensor stateHolder1;
    private StateSensor stateHolder2;
    private AbstractBehaviorNode sequence;

    @Before
    public void before() {
        executor = new BehaviorExecutor();
        Assert.assertTrue(executor.getLastState() == State.SUCCESS);

        stateHolder1 = new StateSensor();
        final TriggerBehavior trigger1 = new TriggerBehavior(stateHolder1);
        stateHolder2 = new StateSensor();
        final TriggerBehavior trigger2 = new TriggerBehavior(stateHolder2);

        sequence = new Sequential();
        sequence.addChild(trigger1);
        sequence.addChild(trigger2);

        executor.attachBehavior(IEntityCommand.NULL_ENTITY, sequence);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        stateHolder1.setState(State.RUNNING);
        stateHolder2.setState(State.RUNNING);

        Assert.assertTrue(executor.getLastState() == State.RUNNING);
    }

    @Test
    public void failExecutorTest() {
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        stateHolder1.setState(State.FAILURE);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.FAILURE);
    }

    @Test
    public void successFailureExecutorTest() {
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        stateHolder1.setState(State.SUCCESS);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        stateHolder2.setState(State.FAILURE);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.FAILURE);
    }

    @Test
    public void successSuccessExecutorTest() {
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        stateHolder1.setState(State.SUCCESS);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        stateHolder2.setState(State.SUCCESS);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.SUCCESS);
    }

    @Test
    public void invalidExecutorTest() {
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        stateHolder1.setState(State.INVALID);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.INVALID);
    }

    @Test
    public void createdExecutorTest() {
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        stateHolder1.setState(State.RUNNING);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
    }

}
