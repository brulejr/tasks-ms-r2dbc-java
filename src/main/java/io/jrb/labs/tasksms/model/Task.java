/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jon Brule <brulejr@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.jrb.labs.tasksms.model;

import io.jrb.labs.common.entity.Entity;
import io.jrb.labs.common.entity.EntityBuilder;
import io.jrb.labs.tasksms.resource.TaskResource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(value = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Table(value = "t_task")
public class Task implements Entity {

    @Id
    @Column(value = "ta_id")
    Long id;

    @Column(value = "guid")
    UUID guid;

    @Column(value = "name")
    String name;

    @Column(value = "description")
    String description;

    @Column(value = "created_by")
    String createdBy;

    @Column(value = "created_on")
    Instant createdOn;

    @Column(value = "modified_by")
    String modifiedBy;

    @Column(value = "modified_on")
    Instant modifiedOn;

    public static TaskBuilder fromResource(final TaskResource taskResource) {
        return Task.builder()
                .guid(taskResource.getGuid())
                .name(taskResource.getName())
                .description(taskResource.getDescription());
    }

    public static class TaskBuilder implements EntityBuilder<Task, TaskBuilder> {
    }

}
