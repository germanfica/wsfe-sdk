/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * Modificado por German Fica en 2025 para su integración en un proyecto MIT.
 * Este archivo conserva la licencia Apache 2.0 conforme a sus términos.
 */

package com.germanfica.wsfe.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An decorator for {@link ThreadFactory} that allows naming threads based on a format.
 */
public class NamedThreadFactory implements ThreadFactory {
    private final ThreadFactory delegate;
    private final String namePrefix;
    private final AtomicInteger threadCount = new AtomicInteger(0);

    public NamedThreadFactory(ThreadFactory delegate, String namePrefix) {
        this.delegate = Validate.notNull(delegate, "delegate must not be null");
        this.namePrefix = Validate.notBlank(namePrefix, "namePrefix must not be blank");
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = delegate.newThread(runnable);
        thread.setName(namePrefix + "-" + threadCount.getAndIncrement());
        return thread;
    }
}
