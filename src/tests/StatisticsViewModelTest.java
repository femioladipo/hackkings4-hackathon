package tests;

import junit.framework.TestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import viewmodel.StatisticsViewModel;

import java.util.HashSet;


public class StatisticsViewModelTest extends TestCase {

    StatisticsViewModel view = new StatisticsViewModel();

    public void testVisibleSize() {
        assertEquals("There should be only four visible elements.", view.visible.size(), 4);
    }

    public void testDistinct() {
        assertEquals("All elements in the list should be different.", view.visible.size(), new HashSet(view.visible).size());
    }

    public void testNext() {
        for(int idx = 0; idx <= 3; idx++) {
            view.getNext(idx);
            testDistinct();
            testVisibleSize();
        }

    }

    public void testPrevious() {
        for(int idx = 0; idx <= 3; idx++) {
            view.getPrevious(idx);
            testDistinct();
            testVisibleSize();
        }

    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }


}
