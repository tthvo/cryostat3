/*
 * Copyright The Cryostat Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cryostat.expressions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.cryostat.expressions.MatchExpression.MatchedExpression;
import io.cryostat.targets.Target;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;
import org.projectnessie.cel.tools.ScriptException;

@Path("/api/v4/matchExpressions")
public class MatchExpressions {

    @Inject MatchExpression.TargetMatcher targetMatcher;
    @Inject Logger logger;

    @POST
    @RolesAllowed("read")
    @Blocking
    public MatchedExpression test(RequestData requestData) throws ScriptException {
        var targets = new HashSet<Target>();
        if (requestData.targetIds == null) {
            targets.addAll(Target.<Target>listAll());
        } else {
            requestData.targetIds.forEach(id -> targets.add(Target.getTargetById(id)));
        }
        return targetMatcher.match(new MatchExpression(requestData.matchExpression), targets);
    }

    @GET
    @RolesAllowed("read")
    @Blocking
    public Multi<Map<String, Object>> list() {
        List<MatchExpression> exprs = MatchExpression.listAll();
        // FIXME hack so that this endpoint renders the response as the entity object with id and
        // script fields, rather than allowing Jackson serialization to handle it normally where it
        // will be encoded as only the script as a raw string
        return Multi.createFrom()
                .items(exprs.stream().map(expr -> Map.of("id", expr.id, "script", expr.script)));
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("read")
    @Blocking
    public MatchedExpression get(@RestPath long id) throws ScriptException {
        return targetMatcher.match(MatchExpression.find("id", id).singleResult());
    }

    static record RequestData(String matchExpression, List<Long> targetIds) {
        RequestData {
            Objects.requireNonNull(matchExpression);
        }
    }
}
