package com.example.be12fin5verdosewmthisbe.order.service;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.MenuRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.menu_management.option.repository.OptionRepository;
import com.example.be12fin5verdosewmthisbe.order.model.Order;
import com.example.be12fin5verdosewmthisbe.order.model.OrderMenu;
import com.example.be12fin5verdosewmthisbe.order.model.OrderOption;
import com.example.be12fin5verdosewmthisbe.order.model.dto.*;
import com.example.be12fin5verdosewmthisbe.order.repository.OrderMenuRepository;
import com.example.be12fin5verdosewmthisbe.order.repository.OrderRepository;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.be12fin5verdosewmthisbe.order.model.dto.OrderDto.OrderCreateResponse.toOrderCreateResponse;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public OrderDto.OrderCreateResponse createOrder(OrderDto.OrderCreateRequest request, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));

        Order order = Order.builder()
                .tableNumber(request.getTableNumber())
                .status(Order.OrderStatus.PAID)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .store(store)
                .orderType(Order.OrderType.valueOf(request.getOrderType()))
                .orderMenuList(new ArrayList<>())
                .build();

        int totalPrice = 0;

        for (OrderDto.OrderMenuRequest menuReq : request.getOrderMenus()) {
            Menu menu = menuRepository.findById(menuReq.getMenuId())
                    .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
            OrderMenu orderMenu = OrderMenu.builder()
                    .order(order)
                    .price(menuReq.getPrice())
                    .quantity(menuReq.getQuantity())
                    .menu(menu)
                    .build();

            int menuTotal = menuReq.getPrice() * menuReq.getQuantity();

            for (Long optionId : menuReq.getOptionIds()) {
                Option option = optionRepository.findById(optionId)
                        .orElseThrow(() -> new RuntimeException("Option not found"));

                OrderOption orderOption = OrderOption.builder()
                        .orderMenu(orderMenu)
                        .option(option)
                        .build();

                orderMenu.getOrderOptionList().add(orderOption);
                menuTotal += option.getPrice() * menuReq.getQuantity(); // 수량 고려
            }

            order.getOrderMenuList().add(orderMenu);
            totalPrice += menuTotal;
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        return toOrderCreateResponse(savedOrder); // DTO로 변환하여 리턴
    }

    public List<OrderDto.AllOrderList> getOrdersByStoreId(Long storeId) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        List<OrderDto.AllOrderList> allOrders = new ArrayList<>();
        for (Order order : orders) {
            OrderDto.AllOrderList list = OrderDto.AllOrderList.toAllOrderList(order);
            allOrders.add(list);
        }
        return allOrders;
    }

    public Order getOrderById(long orderId) {
        return orderRepository.findById(orderId).orElseThrow(()-> new RuntimeException("Order not found"));
    }

    public OrderTodayDto.OrderTodayResponse getTodaySales(String storeId) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        List<Order> orderList = orderRepository.findTodayOrderByStoreIdx(storeId, today);
        List<Order> beforeList = orderRepository.findTodayOrderByStoreIdx(storeId, sevenDaysAgo);

        int todayTotal = orderList.stream().mapToInt(Order::getTotalPrice).sum();
        int minusSevendaysTotal = beforeList.stream().mapToInt(Order::getTotalPrice).sum();
        int interval = todayTotal - minusSevendaysTotal;

        List<OrderTodayDto.OrderTodayTime> timeList = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            int finalHour = hour;

            // 이 시간대의 주문 필터링
            List<Order> ordersInHour = orderList.stream()
                    .filter(order -> order.getCreatedAt().toLocalDateTime().getHour() == finalHour)
                    .toList();

            // hall 매출
            int hallSales = ordersInHour.stream()
                    .filter(order -> order.getOrderType() == Order.OrderType.hall)
                    .mapToInt(Order::getTotalPrice)
                    .sum();

            // 배달(나머지) 매출
            int deliverySales = ordersInHour.stream()
                    .filter(order -> order.getOrderType() != Order.OrderType.hall)
                    .mapToInt(Order::getTotalPrice)
                    .sum();



            timeList.add(OrderTodayDto.OrderTodayTime.of(hour, hallSales, deliverySales));
        }
        return(OrderTodayDto.OrderTodayResponse.of(
                todayTotal, interval,timeList
        ));

    }

    public OrderTopMenuDto.TopWeekResponse getTopWeekSales(Long storeId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        Timestamp startTimestamp = Timestamp.valueOf(startOfWeek.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endOfWeek.plusDays(1).atStartOfDay());

        List<Object[]> result = orderMenuRepository.findBestSellingMenusByStoreAndPeriod(storeId, startTimestamp, endTimestamp);
        int temp = 0;
        String first ="";
        String second ="";
        String third ="";
        for (Object[] row : result) {
            String menuName = (String) row[0];
            if(temp>2){
                break;
            }
            if(temp ==0){
                first = menuName;
            }
            else if(temp ==1){
                second = menuName;
            }
            else if(temp ==2){
                third = menuName;
            }
            else{
                break;
            }
            temp++;
        }
        return OrderTopMenuDto.TopWeekResponse.of(first, second, third);
    }

    public List<OrderMonthDto.TotalSaleResponse> getMonthSales(Long storeId, int year, int month) {

        List<OrderMonthDto.TotalSaleResponse> monthSaleList = new ArrayList<>();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endDate.atStartOfDay());

        Map<LocalDate, Integer[] > monthSales = new HashMap<>();
        LocalDate currentDate = startDate;
        while(!currentDate.equals(endDate)) {
            monthSales.put(currentDate, new Integer[]{0,0});
            currentDate = currentDate.plusDays(1);
        }

        List<Order> orderList = orderRepository.findByCreatedAtBetween(storeId, startTimestamp, endTimestamp);
        for (Order order : orderList) {
            LocalDate date = order.getCreatedAt().toLocalDateTime().toLocalDate();
            Integer price = order.getTotalPrice();
            Integer[] stat = monthSales.get(date);
            stat[0] += price;
            stat[1] += 1;
        }
        for (Map.Entry<LocalDate, Integer[]> entry : monthSales.entrySet()) {
            LocalDate date = entry.getKey();
            Integer[] data = entry.getValue();
            OrderMonthDto.TotalSaleResponse monthsale = OrderMonthDto.TotalSaleResponse.of(Date.valueOf(date), data[0], data[1]);
            monthSaleList.add(monthsale);
        }
        return(monthSaleList);
    }


    private List<OrderSaleDetailDto.OneTimeResponse> initSaleOneTimeList() {
        List<OrderSaleDetailDto.OneTimeResponse> list = new ArrayList<>();
        list.add(OrderSaleDetailDto.OneTimeResponse.of("hall", 0, 0));
        list.add(OrderSaleDetailDto.OneTimeResponse.of("baemin", 0, 0));
        list.add(OrderSaleDetailDto.OneTimeResponse.of("coupang", 0, 0));
        list.add(OrderSaleDetailDto.OneTimeResponse.of("yogiyo", 0, 0));
        return list;
    }

    public List<OrderSaleDetailDto.TotalResponse> getSalesDetail(Long storeId, LocalDate startDate, LocalDate endDate) {

        List<OrderSaleDetailDto.TotalResponse> saleDetailList = new ArrayList<>();

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate calDate = endDate.plusDays(1);
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(calDate.atStartOfDay());

        List<Order> orderList = orderRepository.findByCreatedAtBetween(storeId, startTimestamp, endTimestamp);


        if(days ==0){
            Map<String, List<OrderSaleDetailDto.OneTimeResponse>> hourlyMap = new LinkedHashMap<>();

            for (int i = 0; i < 24; i++) {
                String hourKey = String.format("%02d", i);
                List<OrderSaleDetailDto.OneTimeResponse> saleOneTimeList = initSaleOneTimeList();
                hourlyMap.put(hourKey, saleOneTimeList);
            }

            // 주문 데이터를 시간별로 분류 및 누적
            for (Order order : orderList) {
                Timestamp createdAt = order.getCreatedAt();
                String hour = String.format("%02d", createdAt.toLocalDateTime().getHour()); // 예: "09", "15"
                String orderType = order.getOrderType().name(); // 예: baemin, hall 등

                List<OrderSaleDetailDto.OneTimeResponse> saleOneTimeList = hourlyMap.get(hour);
                for (int i = 0; i < saleOneTimeList.size(); i++) {
                    OrderSaleDetailDto.OneTimeResponse response = saleOneTimeList.get(i);
                    if (response.getSaleMethod().equalsIgnoreCase(orderType)) {
                        int updatedQuantity = response.getSaleQuantity() + 1;
                        int updatedPrice = response.getSalePrice() + order.getTotalPrice();
                        saleOneTimeList.set(i, OrderSaleDetailDto.OneTimeResponse.of(orderType, updatedQuantity, updatedPrice));
                        break;
                    }
                }
            }

            // 결과를 TotalResponse 리스트에 담기
            for (Map.Entry<String, List<OrderSaleDetailDto.OneTimeResponse>> entry : hourlyMap.entrySet()) {
                saleDetailList.add(OrderSaleDetailDto.TotalResponse.of(entry.getKey(), entry.getValue()));
            }
        }// 하루 검색

        else if (Math.abs(days) <= 30) {
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                List<OrderSaleDetailDto.OneTimeResponse> saleOneTimeList = initSaleOneTimeList();

                // ✨ 현재 날짜에 해당하는 주문만 필터링
                LocalDate finalCurrentDate = currentDate;
                List<Order> filteredOrders = new ArrayList<>();
                for (Order order : orderList) {
                    LocalDate orderDate = order.getCreatedAt().toLocalDateTime().toLocalDate();
                    if (orderDate.equals(finalCurrentDate)) {
                        filteredOrders.add(order);
                    }
                }

                // ✨ OrderType별로 수량과 금액 누적
                for (Order order : filteredOrders) {
                    String type = order.getOrderType().name(); // hall, baemin 등
                    int totalPrice = Optional.ofNullable(order.getTotalPrice()).orElse(0);
                    for (OrderSaleDetailDto.OneTimeResponse response : saleOneTimeList) {
                        if (response.getSaleMethod().equalsIgnoreCase(type)) {
                            // 리플렉션 안 쓰고 새로 객체 생성해야 하므로 리스트를 새로 구성해야 함
                            int updatedQuantity = response.getSaleQuantity() + 1;
                            int updatedPrice = response.getSalePrice() + totalPrice;
                            saleOneTimeList.set(
                                    saleOneTimeList.indexOf(response),
                                    OrderSaleDetailDto.OneTimeResponse.of(type, updatedQuantity, updatedPrice)
                            );
                            break;
                        }
                    }
                }

                // ✨ 날짜별 전체 결과 리스트에 추가
                OrderSaleDetailDto.TotalResponse saleDetail = OrderSaleDetailDto.TotalResponse.of(String.valueOf(currentDate), saleOneTimeList);
                saleDetailList.add(saleDetail);

                currentDate = currentDate.plusDays(1);
            }
        } // 한달 이내

        else if (Math.abs(days) <= 365) {
            YearMonth currentMonth = YearMonth.from(startDate);
            YearMonth endMonth = YearMonth.from(endDate);

            while (!currentMonth.isAfter(endMonth)) {
                List<OrderSaleDetailDto.OneTimeResponse> saleOneTimeList = initSaleOneTimeList();

                // 월별로 주문 필터링 (for문 사용)
                List<Order> filteredOrders = new ArrayList<>();
                for (Order order : orderList) {
                    LocalDate orderDate = order.getCreatedAt().toLocalDateTime().toLocalDate();
                    YearMonth orderMonth = YearMonth.from(orderDate);
                    if (orderMonth.equals(currentMonth)) {
                        filteredOrders.add(order);
                    }
                }

                // OrderType 별로 수량, 가격 누적
                for (Order order : filteredOrders) {
                    String type = order.getOrderType().name();
                    int totalPrice = Optional.ofNullable(order.getTotalPrice()).orElse(0);
                    for (int i = 0; i < saleOneTimeList.size(); i++) {
                        OrderSaleDetailDto.OneTimeResponse response = saleOneTimeList.get(i);
                        if (response.getSaleMethod().equalsIgnoreCase(type)) {
                            int updatedQuantity = response.getSaleQuantity() + 1;
                            int updatedPrice = response.getSalePrice() + totalPrice;
                            saleOneTimeList.set(i, OrderSaleDetailDto.OneTimeResponse.of(type, updatedQuantity, updatedPrice));
                            break;
                        }
                    }
                }

                // 월별 결과 추가
                OrderSaleDetailDto.TotalResponse saleDetail = OrderSaleDetailDto.TotalResponse.of(currentMonth.toString(), saleOneTimeList);
                saleDetailList.add(saleDetail);

                // 다음 달로 이동
                currentMonth = currentMonth.plusMonths(1);
            }
        } // 일년 이내

        else{
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }
        return(saleDetailList);
    }



}
        