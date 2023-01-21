/*
 * Copyright 2023 Aleksey Popov <alexnerd.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package alexnerd.content.posts.boundary;

import alexnerd.content.posts.control.Lang;
import alexnerd.content.posts.control.PostStore;
import alexnerd.content.posts.entity.ContentType;
import alexnerd.content.posts.entity.Post;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.metrics.annotation.Timed;

import java.util.List;

@Path("/")
public class PostsResource {

    @Inject
    PostStore store;

    @Timed
    @GET
    @Path("post/{date}/{title}")
    @Produces(MediaType.APPLICATION_JSON)
    public Post findPost(@DefaultValue("ru") @QueryParam("lang") Lang lang,
                         @DefaultValue("POST") @QueryParam("type") ContentType type,
                         @PathParam("date") String date,
                         @PathParam("title") String title) {
        return this.store.read(lang, type, date, title);
    }

    @GET
    @Path("last")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Post> findLast(@DefaultValue("ru") @QueryParam("lang") Lang lang,
                               @DefaultValue("POST") @QueryParam("type") ContentType type,
                               @QueryParam("limit") @Min(1) @Max(10) int limit) {
        return this.store.readLast(lang, type, limit);
    }
}