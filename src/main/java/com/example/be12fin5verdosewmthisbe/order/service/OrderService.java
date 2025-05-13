package com.example.be12fin5verdosewmthisbe.order.service;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.ModifyInventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.UsedInventory;
import com.example.be12fin5verdosewmthisbe.inventory.repository.InventoryRepository;
import com.example.be12fin5verdosewmthisbe.inventory.repository.ModifyInventoryRepository;
import com.example.be12fin5verdosewmthisbe.inventory.repository.StoreInventoryRepository;
import com.example.be12fin5verdosewmthisbe.inventory.repository.UsedInventoryRepository;
import com.example.be12fin5verdosewmthisbe.inventory.service.InventoryService;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.MenuCount;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.MenuCountRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.MenuRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.RecipeRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.OptionValue;
import com.example.be12fin5verdosewmthisbe.menu_management.option.repository.OptionRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.option.repository.OptionValueRepository;
import com.example.be12fin5verdosewmthisbe.order.model.Order;
import com.example.be12fin5verdosewmthisbe.order.model.OrderMenu;
import com.example.be12fin5verdosewmthisbe.order.model.OrderOption;
import com.example.be12fin5verdosewmthisbe.order.model.dto.*;
import com.example.be12fin5verdosewmthisbe.order.repository.OrderMenuRepository;
import com.example.be12fin5verdosewmthisbe.order.repository.OrderRepository;
import com.example.be12fin5verdosewmthisbe.redis.dto.RedisMenuDto;
import com.example.be12fin5verdosewmthisbe.redis.dto.RedisOptionDto;
import com.example.be12fin5verdosewmthisbe.redis.dto.RedisOptionValueDto;
import com.example.be12fin5verdosewmthisbe.redis.dto.RedisRecipeDto;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.be12fin5verdosewmthisbe.order.model.dto.OrderDto.OrderCreateResponse.toOrderCreateResponse;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final MenuRepository menuRepository;
    private final RecipeRepository recipeRepository;
    private final InventoryRepository inventoryRepository;
    private final ModifyInventoryRepository modifyInventoryRepository;
    private final UsedInventoryRepository usedInventoryRepository;
    private final MenuCountRepository menuCountRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private ValueOperations<String, Object> valueOps;

    @PostConstruct
    public void init() {
        valueOps = redisTemplate.opsForValue();
    }

    @Transactional
    public OrderDto.OrderCreateResponse createOrder(OrderDto.OrderCreateRequest request, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_EXIST));

        List<Long> menuIds = request.getOrderMenus().stream()
                .map(OrderDto.OrderMenuRequest::getMenuId)
                .distinct().toList();

        List<Long> optionIds = request.getOrderMenus().stream()
                .flatMap(m -> m.getOptionIds().stream())
                .distinct().toList();

        List<Menu> menus = menuRepository.load(menuIds);
        Map<Long, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        List<Option> options = optionRepository.findAllById(optionIds);
        Map<Long, Option> optionMap = options.stream()
                .collect(Collectors.toMap(Option::getId, Function.identity()));

        Order order = Order.builder()
                .tableNumber(request.getTableNumber())
                .status(Order.OrderStatus.PAID)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .store(store)
                .orderType(Order.OrderType.valueOf(request.getOrderType()))
                .orderMenuList(new ArrayList<>())
                .build();

        int totalPrice = 0;
        Map<Long, BigDecimal> usedInventoryQty = new HashMap<>();
        Map<Long, BigDecimal> modifyInventoryQty = new HashMap<>();
        Map<Long, Integer> menuCountMap = new HashMap<>();

        for (OrderDto.OrderMenuRequest menuReq : request.getOrderMenus()) {
            Long menuId = menuReq.getMenuId();
            String menuKey = "menu:" + menuId;

            RedisMenuDto redisMenuDto = (RedisMenuDto) redisTemplate.opsForValue().get(menuKey);
            Menu menu;
            if (redisMenuDto == null) {
                menu = menuRepository.findById(menuId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
                redisMenuDto = RedisMenuDto.fromMenu(menu);
                redisTemplate.opsForValue().set(menuKey, redisMenuDto, Duration.ofHours(6));
            } else {
                menu = redisMenuDto.toMenu(storeInventoryRepository);
            }

            OrderMenu orderMenu = OrderMenu.builder()
                    .order(order)
                    .price(menu.getPrice())
                    .quantity(menuReq.getQuantity())
                    .menu(menu)
                    .orderOptionList(new ArrayList<>())
                    .build();

            int menuTotal = menu.getPrice() * menuReq.getQuantity();

            // ✅ 레시피 로드 및 재고 차감
            String recipeKey = "recipe:menu:" + menuId;
            List<RedisRecipeDto> redisRecipeDtos = (List<RedisRecipeDto>) redisTemplate.opsForValue().get(recipeKey);
            List<Recipe> recipes;

            if (redisRecipeDtos == null) {
                recipes = recipeRepository.findAllByMenu(menu);
                redisRecipeDtos = recipes.stream().map(Recipe::toRedisRecipeDto).toList();
                redisTemplate.opsForValue().set(recipeKey, redisRecipeDtos, Duration.ofHours(6));
            } else {
                recipes = redisRecipeDtos.stream()
                        .map(dto -> dto.toRecipe(storeInventoryRepository))
                        .toList();
            }

            for (Recipe recipe : recipes) {
                Long invId = recipe.getStoreInventory().getId();
                BigDecimal deduct = recipe.getQuantity()
                        .multiply(BigDecimal.valueOf(menuReq.getQuantity()));
                usedInventoryQty.merge(invId, deduct, BigDecimal::add);
                modifyInventoryQty.merge(invId, deduct.negate(), BigDecimal::add);
            }

            // ✅ 옵션 처리
            for (Long optionId : menuReq.getOptionIds()) {
                String optionKey = "option:" + optionId;
                RedisOptionDto redisOptionDto = (RedisOptionDto) redisTemplate.opsForValue().get(optionKey);

                if (redisOptionDto == null) {
                    Option option = optionRepository.findById(optionId)
                            .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));
                    redisOptionDto = RedisOptionDto.fromOption(option);
                    redisTemplate.opsForValue().set(optionKey, redisOptionDto, Duration.ofHours(6));
                }

                Option option = optionRepository.findById(redisOptionDto.getOptionId())
                        .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));

                OrderOption orderOption = OrderOption.builder()
                        .orderMenu(orderMenu)
                        .option(option)
                        .build();
                orderMenu.getOrderOptionList().add(orderOption);
                menuTotal += redisOptionDto.getPrice() * menuReq.getQuantity();

                for (RedisOptionValueDto valueDto : redisOptionDto.getOptionValues()) {
                    Long invId = valueDto.getStoreInventoryId();
                    BigDecimal quantityToDeduct = valueDto.getQuantity().multiply(BigDecimal.valueOf(menuReq.getQuantity()));
                    usedInventoryQty.merge(invId, quantityToDeduct, BigDecimal::add);
                    modifyInventoryQty.merge(invId, quantityToDeduct.negate(), BigDecimal::add);
                }
            }

            order.getOrderMenuList().add(orderMenu);
            totalPrice += menuTotal;
            menuCountMap.merge(menuId, menuReq.getQuantity(), Integer::sum);
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        List<UsedInventory> usedList = usedInventoryQty.entrySet().stream()
                .map(e -> {
                    StoreInventory si = storeInventoryRepository.getReferenceById(e.getKey());
                    return UsedInventory.builder()
                            .storeInventory(si)
                            .totalquantity(e.getValue())
                            .name(si.getName())
                            .usedDate(now)
                            .status(true)
                            .build();
                }).toList();
        usedInventoryRepository.saveAll(usedList);

        List<ModifyInventory> modList = modifyInventoryQty.entrySet().stream()
                .map(e -> ModifyInventory.builder()
                        .storeInventory(storeInventoryRepository.getReferenceById(e.getKey()))
                        .modifyQuantity(e.getValue())
                        .modifyDate(now)
                        .build())
                .toList();
        modifyInventoryRepository.saveAll(modList);

        List<MenuCount> countList = menuCountMap.entrySet().stream()
                .map(e -> {
                    MenuCount mc = new MenuCount();
                    mc.setStore(store);
                    mc.setMenu(menuMap.get(e.getKey()));
                    mc.setCount(e.getValue());
                    mc.setUsedDate(now);
                    return mc;
                }).toList();
        menuCountRepository.saveAll(countList);

        return toOrderCreateResponse(savedOrder);
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

        Timestamp start = Timestamp.valueOf(startOfWeek.atStartOfDay());
        Timestamp end = Timestamp.valueOf(endOfWeek.plusDays(1).atStartOfDay());

        // PageRequest.of(0, 3) → 첫 페이지(0)에서 3개만
        List<Object[]> rows = orderMenuRepository
                .findBestSellingMenusByStoreAndPeriod(
                        storeId, start, end,
                        PageRequest.of(0, 3)
                );

        String first  = rows.size() > 0 ? (String) rows.get(0)[0] : "";
        String second = rows.size() > 1 ? (String) rows.get(1)[0] : "";
        String third  = rows.size() > 2 ? (String) rows.get(2)[0] : "";

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
