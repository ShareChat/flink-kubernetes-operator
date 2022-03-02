/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.kubernetes.operator.observer;

import org.apache.flink.kubernetes.operator.crd.FlinkDeployment;

import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

import java.util.concurrent.TimeUnit;

import static org.apache.flink.kubernetes.operator.reconciler.BaseReconciler.PORT_READY_DELAY_SECONDS;
import static org.apache.flink.kubernetes.operator.reconciler.BaseReconciler.REFRESH_SECONDS;

/** Status of the Flink JobManager Kubernetes deployment. */
public enum JobManagerDeploymentStatus {

    /** JobManager is running and ready to receive REST API calls. */
    READY,

    /** JobManager is running but not ready yet to receive REST API calls. */
    DEPLOYED_NOT_READY,

    /** JobManager process is starting up. */
    DEPLOYING,

    /** JobManager deployment not found, probably not started or killed by user. */
    MISSING;

    public UpdateControl<FlinkDeployment> toUpdateControl(FlinkDeployment flinkDeployment) {
        switch (this) {
            case DEPLOYING:
            case READY:
                return UpdateControl.updateStatus(flinkDeployment)
                        .rescheduleAfter(REFRESH_SECONDS, TimeUnit.SECONDS);
            case DEPLOYED_NOT_READY:
                return UpdateControl.updateStatus(flinkDeployment)
                        .rescheduleAfter(PORT_READY_DELAY_SECONDS, TimeUnit.SECONDS);
            case MISSING:
            default:
                return null;
        }
    }
}