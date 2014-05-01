/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.tasmo.lib;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jivesoftware.os.tasmo.event.api.write.EventBuilder;
import com.jivesoftware.os.tasmo.id.Id;
import com.jivesoftware.os.tasmo.id.ObjectId;
import org.testng.annotations.Test;

/**
 *
 */
public class RefAsLeafValueTest extends BaseTasmoTest {

    @Test
    public void testRefAsLeafValue() throws Exception {
        String viewClassName = "Values";
        String viewFieldName = "ref";
        Expectations expectations = initModelPaths(viewClassName + "::" + viewFieldName + "::User.ref_followed");
        ObjectId user1 = write(EventBuilder.create(idProvider, "User", tenantId, actorId)
            .set("ref_followed", new ObjectId("Place", new Id(2))).build()); //2


        expectations.addExpectation(user1, viewClassName, viewFieldName, new ObjectId[]{ user1 }, "ref_followed",
            "Place_" + new Id(2).toStringForm());

        ObjectNode view = readView(tenantIdAndCentricId, actorId, new ObjectId(viewClassName, user1.getId()));
        System.out.println(mapper.writeValueAsString(view));

        expectations.assertExpectation(tenantIdAndCentricId);
        expectations.clear();

    }
}
