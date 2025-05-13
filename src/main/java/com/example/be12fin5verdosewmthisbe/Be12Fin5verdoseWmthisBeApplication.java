package com.example.be12fin5verdosewmthisbe;

import com.example.be12fin5verdosewmthisbe.common.CoordinateConverter;
import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.inventory.repository.InventoryRepository;
import com.example.be12fin5verdosewmthisbe.inventory.repository.StoreInventoryRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.category.model.Category;
import com.example.be12fin5verdosewmthisbe.menu_management.category.model.CategoryOption;
import com.example.be12fin5verdosewmthisbe.menu_management.category.repository.CategoryOptionRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.category.repository.CategoryRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.MenuRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.repository.RecipeRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.OptionValue;
import com.example.be12fin5verdosewmthisbe.menu_management.option.repository.OptionRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.option.repository.OptionValueRepository;
import com.example.be12fin5verdosewmthisbe.order.model.dto.OrderDto;
import com.example.be12fin5verdosewmthisbe.order.repository.OrderRepository;
import com.example.be12fin5verdosewmthisbe.order.service.OrderService;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.store.repository.StoreRepository;
import com.example.be12fin5verdosewmthisbe.user.model.User;
import com.example.be12fin5verdosewmthisbe.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
@EnableScheduling
@AllArgsConstructor
public class Be12Fin5verdoseWmthisBeApplication {
    /*private final StoreInventoryRepository storeInventoryRepository;
    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final OptionRepository optionRepository;
    private final RecipeRepository recipeRepository;
    private final OptionValueRepository optionValueRepository;
    private final CategoryOptionRepository categoryOptionRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;*/

    public static void main(String[] args) {
        SpringApplication.run(Be12Fin5verdoseWmthisBeApplication.class, args);
    }

    /*private Timestamp getRandomRecentTimestamp() {
        long now = System.currentTimeMillis();
        long oneMonthsAgo = now - Duration.ofDays(30).toMillis();
        long randomTime = ThreadLocalRandom.current().nextLong(oneMonthsAgo, now);
        return new Timestamp(randomTime);
    }
    @Bean
    CommandLineRunner generateDummyData(UserRepository userRepository, StoreRepository storeRepository) {
        return args -> {
            Faker faker = new Faker(new Locale("ko"));
            Faker engfaker = new Faker();
            Random random = new Random();

            RestTemplate restTemplate = new RestTemplate();
            String BASE_URL = "http://openapi.seoul.go.kr:8088/524d5a4663746a633435416d695259/xml/LOCALDATA_072404_GN/{start}/{end}/";

            List<ParsedStore> validStores = new ArrayList<>();

            int desiredStoreCount = 300;
            int batchSize = 100;
            int start = 1;
            int end = start + batchSize - 1;

            while (validStores.size() < desiredStoreCount) {
                try {
                    String url = BASE_URL.replace("{start}", String.valueOf(start)).replace("{end}", String.valueOf(end));
                    String xmlResponse = restTemplate.getForObject(url, String.class);
                    if (xmlResponse == null || xmlResponse.isEmpty()) break;

                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .parse(new ByteArrayInputStream(xmlResponse.getBytes()));

                    NodeList rows = document.getElementsByTagName("row");
                    if (rows.getLength() == 0) break; // 더 이상 데이터가 없음

                    for (int i = 0; i < rows.getLength() && validStores.size() < desiredStoreCount; i++) {
                        Element row = (Element) rows.item(i);

                        String trdState = getTagValue(row, "TRDSTATEGBN");
                        if ("03".equals(trdState)) continue;

                        String xStr = getTagValue(row, "X");
                        String yStr = getTagValue(row, "Y");
                        String name = getTagValue(row, "BPLCNM");
                        String addr = getTagValue(row, "RDNWHLADDR");

                        if (xStr == null || yStr == null || name == null || addr == null) continue;

                        try {
                            double x = Double.parseDouble(xStr);
                            double y = Double.parseDouble(yStr);

                            double[] latLon = CoordinateConverter.convertToLatLon(x, y);


                            validStores.add(new ParsedStore(name, addr, latLon[1], latLon[0]));
                        } catch (Exception e) {
                            // 무시
                        }
                    }

                    start += batchSize;
                    end = start + batchSize - 1;

                } catch (Exception e) {
                    break;
                }
            }

            System.out.println("📦 유효한 매장 수: " + validStores.size());


            // 🧑‍ 생성할 유저 수 만큼 반복
            int limit = Math.min(200, validStores.size()); // 매장이 부족하면 유저 수 제한
            System.out.println(validStores.size());
            for (int k = 0; k < limit; k++) {
                String email = engfaker.internet().emailAddress();
                String password = new BCryptPasswordEncoder().encode("q1w2e3r4Q!");
                String businessNumber = faker.regexify("\\d{3}-\\d{2}-\\d{5}");
                String phoneNumber = faker.regexify("010-\\d{4}-\\d{4}");
                String storePhoneNumber = faker.regexify("02-\\d{3}-\\d{4}");
                String ssn = faker.regexify("\\d{6}-\\d");

                User user = userRepository.save(User.builder()
                        .name(faker.name().fullName())
                        .email(email)
                        .password(password)
                        .businessNumber(businessNumber)
                        .phoneNumber(phoneNumber)
                        .ssn(ssn)
                        .build());

                ParsedStore storeData = validStores.get(k);
                Store store = storeRepository.save(Store.builder()
                        .user(user)
                        .name(storeData.name())
                        .address(storeData.address())
                        .phoneNumber(storePhoneNumber)
                        .latitude(storeData.longitude())
                        .longitude(storeData.latitude())
                        .build());

                List<Category> categories = new ArrayList<>();
                List<Menu> menus = new ArrayList<>();
                List<Option> options = new ArrayList<>();

                // 1. 카테고리, 메뉴(20개), 옵션 생성
                String[] koreanCategories = {"한식", "중식", "양식", "일식", "분식"};
                String[] koreanMenus = {"불고기 덮밥", "김치찌개", "된장찌개", "제육볶음", "비빔밥", "돈까스", "냉면", "칼국수", "떡볶이", "라면", "오므라이스", "부대찌개", "해물파전", "순두부찌개", "치즈돈까스", "짜장면", "짬뽕", "마라탕", "탕수육", "초밥"};
                String[] koreanOptions = {"곱빼기", "매운맛 추가", "치즈 추가", "계란 추가", "김치 추가", "국물 추가", "밥 추가", "면 추가", "마늘 추가", "토핑 추가"};
                String[] ingredientNames = {
                        "간장", "소금", "설탕", "식초", "참기름", "고추장", "된장", "다진 마늘", "생강", "후추",
                        "양파", "대파", "당근", "감자", "무", "쌀", "밀가루", "계란", "우유", "김치"
                };
                // 카테고리 생성
                for (int i = 0; i < 5; i++) {
                    Category category = Category.builder()
                            .store(store)
                            .name(koreanCategories[i % koreanCategories.length])
                            .build();
                    categoryRepository.save(category);
                    categories.add(category);
                }

                // 메뉴와 옵션 생성
                for (int i = 0; i < 20; i++) {
                    Category randomCategory = categories.get(random.nextInt(categories.size()));

                    String menuName = koreanMenus[i % koreanMenus.length];

                    Menu menu = Menu.builder()
                            .store(store)
                            .category(randomCategory)
                            .name(menuName)
                            .price(faker.number().numberBetween(7000, 15000))
                            .build();
                    menuRepository.save(menu);
                    menus.add(menu);

                    String optionName = koreanOptions[i % koreanOptions.length];

                    Option option = Option.builder()
                            .store(store)
                            .name(optionName)
                            .price(faker.number().numberBetween(500, 2000))
                            .build();
                    optionRepository.save(option);
                    options.add(option);
                }

                // 2. StoreInventory + Inventory (20개)
                List<StoreInventory> storeInventories = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    String inventoryName = ingredientNames[i % ingredientNames.length];

                    StoreInventory storeInventory = StoreInventory.builder()
                            .name(inventoryName)
                            .unit(faker.options().option("kg", "g", "ml"))
                            .expiryDate(faker.number().numberBetween(3, 30))
                            .minQuantity(BigDecimal.valueOf(faker.number().randomDouble(2, 1, 30)))
                            .quantity(BigDecimal.ZERO)
                            .store(store)
                            .build();

                    List<Inventory> inventoryList = new ArrayList<>();
                    BigDecimal totalQuantity = BigDecimal.ZERO;
                    int count = faker.number().numberBetween(5, 10);

                    for (int j = 0; j < count; j++) {
                        BigDecimal quantity = BigDecimal.valueOf(faker.number().randomDouble(2, 10, 50));
                        Inventory inventory = Inventory.builder()
                                .purchaseDate(Timestamp.valueOf(LocalDate.now().minusDays(faker.number().numberBetween(1, 15)).atStartOfDay()))
                                .expiryDate(LocalDate.now().plusDays(faker.number().numberBetween(10, 60)))
                                .unitPrice(faker.number().numberBetween(500, 3000))
                                .quantity(quantity)
                                .storeInventory(storeInventory)
                                .build();

                        totalQuantity = totalQuantity.add(quantity);
                        inventoryList.add(inventory);
                    }

                    storeInventory.setQuantity(totalQuantity);
                    storeInventory.setInventoryList(inventoryList);
                    storeInventoryRepository.save(storeInventory);
                    storeInventories.add(storeInventory);
                }


                // 3. MenuRecipe 연결 (재료 2~4개)
                for (Menu menu : menus) {
                    int recipeCount = faker.number().numberBetween(2, 5);
                    Set<StoreInventory> usedInventories = new HashSet<>();

                    for (int i = 0; i < recipeCount; i++) {
                        StoreInventory randomInventory = storeInventories.get(random.nextInt(storeInventories.size()));
                        if (usedInventories.contains(randomInventory)) continue;
                        usedInventories.add(randomInventory);

                        Recipe recipe = Recipe.builder()
                                .menu(menu)
                                .storeInventory(randomInventory)
                                .quantity(BigDecimal.valueOf(faker.number().randomDouble(2, 1, 2)))
                                .build();
                        recipeRepository.save(recipe);
                    }
                }

                // 4. OptionValue 연결 (재료 2~3개)
                for (Option option : options) {
                    int valueCount = faker.number().numberBetween(2, 4);
                    Set<StoreInventory> usedInventories = new HashSet<>();

                    for (int i = 0; i < valueCount; i++) {
                        StoreInventory randomInventory = storeInventories.get(random.nextInt(storeInventories.size()));
                        if (usedInventories.contains(randomInventory)) continue;
                        usedInventories.add(randomInventory);

                        OptionValue optionValue = OptionValue.builder()
                                .option(option)
                                .storeInventory(randomInventory)
                                .quantity(BigDecimal.valueOf(faker.number().randomDouble(2, 1, 2)))
                                .build();
                        optionValueRepository.save(optionValue);
                    }
                }

                // 5. Option을 Category에 연결
                for (Option option : options) {
                    Category randomCategory = categories.get(random.nextInt(categories.size()));
                    CategoryOption categoryOption = CategoryOption.builder()
                            .category(randomCategory)
                            .option(option)
                            .build();
                    categoryOptionRepository.save(categoryOption);
                }

                // 6. 주문 생성
                for (int i = 0; i < 500; i++) {
                    OrderDto.OrderCreateRequest request = new OrderDto.OrderCreateRequest();

                    String orderType = faker.options().option("hall", "baemin", "yogiyo", "coupang");
                    request.setOrderType(orderType);

                    if ("hall".equals(orderType)) {
                        request.setTableNumber(faker.number().numberBetween(1, 10));
                    } else {
                        request.setTableNumber(null);
                    }

                    List<OrderDto.OrderMenuRequest> orderMenus = new ArrayList<>();

                    int menuCount = faker.number().numberBetween(1, 4);
                    for (int j = 0; j < menuCount; j++) {
                        Menu randomMenu = menus.get(faker.random().nextInt(menus.size()));

                        OrderDto.OrderMenuRequest menuRequest = new OrderDto.OrderMenuRequest();
                        menuRequest.setMenuId(randomMenu.getId());
                        menuRequest.setQuantity(faker.number().numberBetween(1, 3));
                        menuRequest.setPrice(randomMenu.getPrice());

                        List<CategoryOption> matchedOptions = categoryOptionRepository.findAllByCategoryId(randomMenu.getCategory().getId());
                        Collections.shuffle(matchedOptions);
                        List<Long> optionIds = matchedOptions.stream()
                                .limit(faker.number().numberBetween(0, 3))
                                .map(co -> co.getOption().getId())
                                .collect(Collectors.toList());

                        menuRequest.setOptionIds(optionIds);
                        orderMenus.add(menuRequest);
                    }

                    request.setOrderMenus(orderMenus);

                    try {
                        OrderDto.OrderCreateResponse response = orderService.createOrder(request, store.getId());

                        // 주문 날짜 랜덤 설정
                        orderRepository.findById(response.getId()).ifPresent(order -> {
                            Timestamp randomTime = getRandomRecentTimestamp();
                            order.setCreatedAt(randomTime);
                            orderRepository.save(order);
                        });

                    } catch (Exception e) {
                        System.out.println("Order creation failed: " + e.getMessage());
                    }
                }
            }
        };
    }



    // 내부용 레코드 클래스
    private record ParsedStore(String name, String address, double latitude, double longitude) {}

    private String getTagValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        return (nodeList.getLength() > 0 && nodeList.item(0) != null) ? nodeList.item(0).getTextContent().trim() : null;
    }*/


}
