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
package io.jrb.labs.tasksms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.jrb.labs.common.service.crud.CrudServiceSupport;
import io.jrb.labs.tasksms.model.EntityType;
import io.jrb.labs.tasksms.model.Task;
import io.jrb.labs.tasksms.model.LookupValue;
import io.jrb.labs.tasksms.model.LookupValueType;
import io.jrb.labs.tasksms.model.Projection;
import io.jrb.labs.tasksms.repository.TaskRepository;
import io.jrb.labs.tasksms.repository.LookupValueRepository;
import io.jrb.labs.tasksms.resource.TaskResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TaskServiceImpl extends CrudServiceSupport<Task, Task.TaskBuilder> implements TaskService {

    private final TaskRepository taskRepository;
    private final LookupValueRepository lookupValueRepository;

    public TaskServiceImpl(
            final TaskRepository taskRepository,
            final LookupValueRepository lookupValueRepository,
            final ObjectMapper objectMapper
    ) {
        super(Task.class, taskRepository, objectMapper);
        this.taskRepository = taskRepository;
        this.lookupValueRepository = lookupValueRepository;
    }

    @Override
    @Transactional
    public Mono<TaskResource> createTask(final TaskResource task) {
        return createEntity(Task.fromResource(task))
                .flatMap(taskEntity -> {
                    final long taskId = taskEntity.getId();
                    return Mono.zip(
                            Mono.just(taskEntity),
                            createLookupValues(taskId, LookupValueType.GROUP, task.getGroups()),
                            createLookupValues(taskId, LookupValueType.TAG, task.getTags())
                    );
                })
                .map(tuple -> TaskResource.fromEntity(tuple.getT1())
                        .groups(tuple.getT2())
                        .tags(tuple.getT3())
                        .build());
    }

    @Override
    @Transactional
    public Mono<Void> deleteTask(final UUID taskGuid) {
        return deleteEntity(taskGuid, taskEntity -> {
            final long taskId = taskEntity.getId();
            return lookupValueRepository.deleteByEntityTypeAndEntityId(EntityType.TASK, taskId)
                    .then(taskRepository.deleteById(taskId));
        });
    }

    @Override
    @Transactional
    public Mono<TaskResource> findTaskByGuid(final UUID taskGuid, final Projection projection) {
        return findEntityByGuid(taskGuid)
                .zipWhen(task -> (projection == Projection.DEEP)
                        ? findTaskValueList(task.getId())
                        : Mono.just(List.<LookupValue>of()))
                .map(tuple -> {
                    final TaskResource.TaskResourceBuilder builder = TaskResource.fromEntity(tuple.getT1());
                    tuple.getT2().forEach(lookupValue -> {
                        final String value = lookupValue.getValue();
                        switch (lookupValue.getValueType()) {
                            case GROUP:
                                builder.group(value);
                                break;
                            case TAG:
                                builder.tag(value);
                                break;
                        }
                    });
                    return builder.build();
                });
    }

    @Override
    @Transactional
    public Flux<TaskResource> listAllTasks() {
        return retrieveEntities()
                .map(entity -> TaskResource.fromEntity(entity).build());
    }

    @Override
    @Transactional
    public Mono<TaskResource> updateTask(final UUID guid, final JsonPatch patch) {
        return updateEntity(guid, entity -> {
            final TaskResource resource = TaskResource.fromEntity(entity).build();
            final TaskResource updatedResource = applyPatch(guid, patch, resource, TaskResource.class);
            return Task.fromResource(updatedResource);
        }).flatMap(entity -> findTaskByGuid(entity.getGuid(), Projection.DETAILS));
    }

    private Mono<List<String>> createLookupValues(
            final long taskId,
            final LookupValueType type,
            final List<String> values
    ) {
        return Flux.fromIterable(values)
                .map(value -> LookupValue.builder()
                        .entityType(EntityType.TASK)
                        .entityId(taskId)
                        .valueType(type)
                        .value(value)
                        .build())
                .flatMap(lookupValueRepository::save)
                .map(LookupValue::getValue)
                .collectList();
    }

    private Mono<List<LookupValue>> findTaskValueList(final long taskId) {
        return lookupValueRepository.findByEntityTypeAndEntityId(EntityType.TASK, taskId)
                .collectList();
    }

}
