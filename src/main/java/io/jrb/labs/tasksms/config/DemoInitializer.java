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
package io.jrb.labs.tasksms.config;

import io.jrb.labs.tasksms.resource.TaskResource;
import io.jrb.labs.tasksms.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;

@Slf4j
public class DemoInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final TaskService taskService;

    public DemoInitializer(final TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        log.info("Setting up demo data in in-memory database...");

        Flux.fromIterable(Arrays.asList(
                TaskResource.builder().name("Task1").description("DESC").build(),
                TaskResource.builder().name("Task2").tag("A").build(),
                TaskResource.builder().name("Task3").tag("A").tag("B").group("1").build()
        ))
                .flatMap(taskService::createTask)
                .doOnNext(task -> log.info("Created {}", task))
                .blockLast(Duration.ofSeconds(10));
    }

}
