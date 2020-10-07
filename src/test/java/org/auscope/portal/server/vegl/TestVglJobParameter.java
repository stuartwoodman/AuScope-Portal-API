package org.auscope.portal.server.vegl;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestVglJobParameter extends PortalTestClass {
    /**
     * Unit test to ensure 'equal' objects return the same hashcode
     */
    @Test
    public void testEqualsMatchesHashcode() {
    	VEGLJob veglJob1 = new VEGLJob();
    	veglJob1.setId(1);
    	VEGLJob veglJob2 = new VEGLJob();
    	veglJob2.setId(1);
    	VEGLJob veglJob3 = new VEGLJob();
    	veglJob3.setId(2);
    	VEGLJob veglJob4 = new VEGLJob();
    	veglJob4.setId(1);
    	VEGLJob veglJob5 = new VEGLJob();
    	veglJob5.setId(3);
        VglParameter p1 = new VglParameter("name1", "v1"/*, "number", veglJob1, "solution1"*/);
        VglParameter p2 = new VglParameter("name1", "v2"/*, "string", veglJob2, "solution2"*/);
        VglParameter p3 = new VglParameter("name1", "v3"/*, "number", veglJob3, "solution1"*/);
        VglParameter p4 = new VglParameter("name2", "v4"/*, "string", veglJob4, "solution2"*/);
        VglParameter p5 = new VglParameter("name3", "v5"/*, "number", veglJob5, "solution1"*/);

        //Test general equality
        Assert.assertTrue(equalsWithHashcode(p1, p1));
        Assert.assertTrue(equalsWithHashcode(p1, p2));
        Assert.assertFalse(equalsWithHashcode(p1, p3));
        Assert.assertFalse(equalsWithHashcode(p1, p4));
        Assert.assertFalse(equalsWithHashcode(p1, p5));
        Assert.assertFalse(equalsWithHashcode(p2, p3));
        Assert.assertFalse(equalsWithHashcode(p2, p4));
        Assert.assertFalse(equalsWithHashcode(p2, p5));
        Assert.assertFalse(equalsWithHashcode(p3, p4));
        Assert.assertFalse(equalsWithHashcode(p3, p5));
        Assert.assertFalse(equalsWithHashcode(p4, p5));
    }
}
