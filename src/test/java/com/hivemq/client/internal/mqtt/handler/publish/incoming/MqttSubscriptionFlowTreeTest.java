/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttSubscriptionFlowTreeTest extends MqttSubscriptionFlowsTest {

    MqttSubscriptionFlowTreeTest() {
        super(MqttSubscriptionFlowTree::new);
    }

    @ParameterizedTest
    @CsvSource({
            "unsubscribe, test/topic1, test/topic2, test/topic3, test/topic1, test/topic2, test/topic3",
            "unsubscribe, test/+, test/topic2, test/topic3, test/topic1, test/topic2, test/topic3",
            "unsubscribe, test/topic/filter1, test/topic/filter2, test/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            "unsubscribe, test/topic/+, test/topic/filter2, test/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            "unsubscribe, test/topic1/filter, test/topic2/filter, test/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            "unsubscribe, test/+/filter, test/topic2/filter, test/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            "unsubscribe, test/topic/filter, test/topic//filter, test/topic///filter, test/topic/filter, test/topic//filter, test/topic///filter",
            "remove, test/topic1, test/topic2, test/topic3, test/topic1, test/topic2, test/topic3",
            "remove, test/+, test/topic2, test/topic3, test/topic1, test/topic2, test/topic3",
            "remove, test/topic/filter1, test/topic/filter2, test/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            "remove, test/topic/+, test/topic/filter2, test/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            "remove, test/topic1/filter, test/topic2/filter, test/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            "remove, test/+/filter, test/topic2/filter, test/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            "remove, test/topic/filter, test/topic//filter, test/topic///filter, test/topic/filter, test/topic//filter, test/topic///filter",
    })
    void branching_compaction(
            final @NotNull String compactOperation, final @NotNull String filter1, final @NotNull String filter2,
            final @NotNull String filter3, final @NotNull String topic1, final @NotNull String topic2,
            final @NotNull String topic3) {

        flows.subscribe(MqttTopicFilterImpl.of(filter1), null);
        flows.subscribe(MqttTopicFilterImpl.of(filter2), null);
        flows.subscribe(MqttTopicFilterImpl.of(filter3), null);

        final MqttMatchingPublishFlows matching1 = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic1), matching1);
        assertTrue(matching1.subscriptionFound);
        final MqttMatchingPublishFlows matching2 = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic2), matching2);
        assertTrue(matching2.subscriptionFound);
        final MqttMatchingPublishFlows matching3 = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic3), matching3);
        assertTrue(matching3.subscriptionFound);

        switch (compactOperation) {
            case "unsubscribe":
                flows.unsubscribe(MqttTopicFilterImpl.of(filter1), null);
                flows.unsubscribe(MqttTopicFilterImpl.of(filter2), null);
                flows.unsubscribe(MqttTopicFilterImpl.of(filter3), null);
                break;
            case "remove":
                flows.remove(MqttTopicFilterImpl.of(filter1), null);
                flows.remove(MqttTopicFilterImpl.of(filter2), null);
                flows.remove(MqttTopicFilterImpl.of(filter3), null);
                break;
            default:
                fail();
        }

        final MqttMatchingPublishFlows matching4 = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic1), matching4);
        assertFalse(matching4.subscriptionFound);
        final MqttMatchingPublishFlows matching5 = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic2), matching5);
        assertFalse(matching5.subscriptionFound);
        final MqttMatchingPublishFlows matching6 = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic3), matching6);
        assertFalse(matching6.subscriptionFound);
    }
}
