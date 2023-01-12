/*
 * Copyright The Cryostat Authors
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or data
 * (collectively the "Software"), free of charge and under any and all copyright
 * rights in the Software, and any and all patent rights owned or freely
 * licensable by each licensor hereunder covering either (i) the unmodified
 * Software as contributed to or provided by such licensor, or (ii) the Larger
 * Works (as defined below), to deal in both
 *
 * (a) the Software, and
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software (each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 * The above copyright notice and either this complete permission notice or at
 * a minimum a reference to the UPL must be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.cryostat.rules;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import io.cryostat.V2Response;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/api/v2/rules")
public class Rules {

    @Inject EventBus bus;

    @GET
    @RolesAllowed("read")
    public V2Response list() {
        return V2Response.json(Rule.listAll());
    }

    @GET
    @RolesAllowed("read")
    @Path("/{name}")
    public V2Response get(@RestPath String name) {
        return V2Response.json(Rule.getByName(name));
    }

    @Transactional
    @POST
    @RolesAllowed("write")
    @Consumes("application/json")
    public V2Response create(Rule rule) {
        // TODO validate the incoming rule
        rule.persist();
        return V2Response.json(rule);
    }

    @Transactional
    @PATCH
    @RolesAllowed("write")
    @Path("/{name}")
    @Consumes("application/json")
    public V2Response update(@RestPath String name, @RestQuery boolean clean, JsonObject body) {
        Rule rule = Rule.getByName(name);
        rule.enabled = body.getBoolean("enabled");
        rule.persist();

        return V2Response.json(rule);
    }

    @Transactional
    @POST
    @RolesAllowed("write")
    public V2Response create(
            @RestForm String name,
            @RestForm String description,
            @RestForm String matchExpression,
            @RestForm String eventSpecifier,
            @RestForm int archivalPeriodSeconds,
            @RestForm int initialDelaySeconds,
            @RestForm int preservedArchives,
            @RestForm int maxAgeSeconds,
            @RestForm int maxSizeBytes,
            @RestForm boolean enabled) {
        Rule rule = new Rule();
        rule.name = name;
        rule.description = description;
        rule.matchExpression = matchExpression;
        rule.eventSpecifier = eventSpecifier;
        rule.archivalPeriodSeconds = archivalPeriodSeconds;
        rule.initialDelaySeconds = initialDelaySeconds;
        rule.preservedArchives = preservedArchives;
        rule.maxAgeSeconds = maxAgeSeconds;
        rule.maxSizeBytes = maxSizeBytes;
        rule.enabled = enabled;
        return create(rule);
    }

    @Transactional
    @DELETE
    @RolesAllowed("write")
    @Path("/{name}")
    public V2Response delete(@RestPath String name, @RestQuery boolean clean) {
        Rule rule = Rule.getByName(name);
        rule.delete();
        return V2Response.json(rule);
    }
}