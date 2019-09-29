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
package exchange.core2.rest.model.internal;

import exchange.core2.core.common.Order;
import exchange.core2.rest.commands.RestApiPlaceOrder;
import exchange.core2.rest.events.MatchingRole;
import exchange.core2.rest.model.api.OrderState;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Thread safe
 */
@Slf4j
public class GatewayUserProfile {

    // orders in status NEW/ACTIVE status
    private final Map<Long, GatewayOrder> openOrders = new HashMap<>();

    // orders in other statuses
    private final Map<Long, GatewayOrder> ordersHistory = new HashMap<>();

    public synchronized Map<Long, Integer> findUserCookies(final Stream<Order> activeOrders) {
        return activeOrders
                .map(Order::getOrderId)
                .collect(Collectors.toMap(
                        orderId -> orderId,
                        orderId -> openOrders.get(orderId).getUserCookie()));
    }

    public synchronized void addNewOrder(long orderId, RestApiPlaceOrder restApiPlaceOrder) {

        GatewayOrder order = GatewayOrder.builder()
                .orderId(orderId)
                .userCookie(restApiPlaceOrder.getUserCookie())
                .price(restApiPlaceOrder.getPrice())
                .size(restApiPlaceOrder.getSize())
                .orderType(restApiPlaceOrder.getOrderType())
                .action(restApiPlaceOrder.getAction())
                .filled(0)
                .state(OrderState.NEW)
                .build();

        openOrders.put(orderId, order);
    }

    public synchronized void activateOrder(long orderId) {
        GatewayOrder gatewayOrder = openOrders.get(orderId);
        gatewayOrder.setState(OrderState.ACTIVE);
    }

    public synchronized void tradeOrder(
            long orderId,
            long size,
            BigDecimal price,
            MatchingRole matchingRole,
            long timestamp,
            long counterOrderId,
            long counterPartyUid) {

        GatewayOrder gatewayOrder = openOrders.get(orderId);

        gatewayOrder.setFilled(gatewayOrder.getFilled() + size);

        if (gatewayOrder.getState() == OrderState.ACTIVE) {
            gatewayOrder.setState(OrderState.FILLED);
        }

        gatewayOrder.getDeals().add(GatewayDeal.builder()
                .size(size)
                .price(price)
                .matchingRole(matchingRole)
                .timestamp(timestamp)
                .counterOrderId(counterOrderId)
                .counterPartyUid(counterPartyUid)
                .build());

    }

    public synchronized void rejectOrder(long orderId) {
        GatewayOrder gatewayOrder = openOrders.get(orderId);
        gatewayOrder.setState(OrderState.REJECTED);
        ordersHistory.put(orderId, gatewayOrder);
    }

    public synchronized void cancelOrder(long orderId) {
        GatewayOrder gatewayOrder = openOrders.get(orderId);
        gatewayOrder.setState(OrderState.CANCELLED);
        ordersHistory.put(orderId, gatewayOrder);
    }

}