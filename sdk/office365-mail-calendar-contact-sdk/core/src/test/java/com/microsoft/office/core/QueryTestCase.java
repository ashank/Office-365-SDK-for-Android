/**
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 */
package com.microsoft.office.core;

import static org.hamcrest.Matchers.greaterThan;

import org.junit.Ignore;
import org.junit.Test;

import com.microsoft.exchange.services.odata.model.DefaultFolder;
import com.microsoft.exchange.services.odata.model.IMessages;
import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.exchange.services.odata.model.types.IMessage;
import com.microsoft.exchange.services.odata.model.types.IMessageCollection;
import com.microsoft.office.proxy.OfficeEntityContainerFactory;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.uri.filter.ODataFilter;
import com.msopentech.odatajclient.proxy.api.NonUniqueResultException;
import com.msopentech.odatajclient.proxy.api.Query;
import com.msopentech.odatajclient.proxy.api.Sort;

/**
 * Validates Exchange OData Query logic e.g. "<url>?$filter=endswith(Subject, sample-ending)".
 */
public class QueryTestCase extends AbstractTest {

    /** Message to use as a stub. It'll be pushed to the server and deleted after the test is done.**/
    private com.microsoft.exchange.services.odata.model.types.IMessage message;

    public void prepareMessage() {
        ODataEntity sourceMessage = getEntityFromResource("queryMessage.json");
        message = com.microsoft.exchange.services.odata.model.Messages.newMessage(DefaultFolder.DRAFTS);
        String subject = sourceMessage.getProperty("Subject").getPrimitiveValue().toString();
        message.setSubject(subject);
        Me.flush();
    }

    public void removeMessage() {
        Me.getMessages().delete(message.getId());
        Me.flush();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void singleResultItemTest() {
        prepareMessage();

        IMessages drafts = Me.getDrafts().getMessages();
        // Query can be declared w/o template params.
        final Query query = drafts.createQuery().setFilter("endswith(Subject, 'jabberwocky')");

        Exception exception = null;
        try {
            query.getSingleResult();
            fail();
        } catch (NonUniqueResultException e) {
            exception = e;
        } finally {
            removeMessage();
        }
        assertNotNull(exception);
    }

    @Test
    public void resultsTest() {
        prepareMessage();

        final Query<IMessage, IMessageCollection> query = Me.getDrafts().getMessages().createQuery();

        query.setFilter("Subject eq 'test message jabberwocky'");
        query.setOrderBy(new Sort("Subject", Sort.Direction.DESC)) // ASC is default.
             .setFirstResult(20) // skips first item on the server. Only items after it are retrieved ($skip).
             .setMaxResults(30);// retrieves at most 30 items ($top).

        IMessageCollection result;
        try {
            result = query.getResult();
        } finally {
            removeMessage();
        }
        assertNotNull(result);
        assertThat(result.size(), greaterThan(0));
    }

    @Test
    @Ignore
    public void filterEqualsTest() {
        prepareMessage();

        final Query<IMessage, IMessageCollection> query = Me.getDrafts().getMessages().createQuery();

        ODataFilter equalsFilter = OfficeEntityContainerFactory.getInstance().getFilterFactory().eq("Subject", "test message jabberwocky");
        query.setFilter(equalsFilter);

        // TODO:
        // Exchange impl of parenthesis contradicts BOTH v3 & v4 OData specs:
        // V4: 5.1.1.3 Grouping (http://docs.oasis-open.org/odata/odata/v4.0/cos01/part2-url-conventions/odata-v4.0-cos01-part2-url-conventions.html#_Toc372793813)
        // V3: 5.1.2.3. Parenthesis Operator (http://www.odata.org/documentation/odata-v3-documentation/url-conventions/#5123_Parenthesis_Operator)
        IMessageCollection result;
        try {
            result = query.getResult();
        } finally {
            removeMessage();
        }

        assertNotNull(result);
        assertThat(result.size(), greaterThan(0));
    }
}
