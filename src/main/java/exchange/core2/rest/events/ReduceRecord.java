/*
 * Copyright 2019 Maksim Zheravin
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
 */
package exchange.core2.rest.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Reduced (update or cancel)
 */
@Getter
@Builder
@AllArgsConstructor
public final class ReduceRecord implements OrderSizeChangeRecord {
    private final String type = "reduce";
    private final long reducedSize;

    @Override
    public long getAffectedSize() {
        return reducedSize;
    }
}