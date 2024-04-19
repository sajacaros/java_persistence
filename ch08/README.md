# 08. 컬렉션과 엔티티 연관관계 매핑
* 8.1 세트, 백, 리스트, 값 타입의 맵
  - 8.1.1 데이터베이스 스키마
    - 경매 품목에 이미지 첨부
    - 그림 8.1 참고
  - 8.1.2 컬렉션 프로퍼티 생성과 매핑
    - `Item#images`를 만들지는 선택사항(영속성 클렉션은 선택사항)
    - `Item#images`라는 컬렉션을 만들 시 이점
      - 자동 조회
        - `Item`이 표시될때 모든 이미지 조회
      - 자동으로 영속화
      - 이미지의 수명주기 관리
  - 8.1.3 컬렉션 인터페이스 선택
    - 인터페이스를 사용해 프로퍼티의 타입 선언
    - `java.util.HashSet`으로 초기화되는 `java.util.Set`
    - `java.util.TreeSet`으로 초기화되는 `java.util.SortedSet`
    - `java.util.ArrayList`로 초기화되는 `java.util.List`
    - `java.util.ArrayList`로 초기화되는 `java.util.Collection`
    - `java.util.HashMap`으로 초기화되는 `java.util.Map`
    - `java.util.TreeMap`으로 초기화되는 `java.util.SortedMap`
  - 8.1.4 세트 매핑
    ```
    @ElementCollection
    @CollectionTable(
            name = "IMAGE", // Defaults to ITEM_IMAGES
            joinColumns = @JoinColumn(name = "ITEM_ID")) // Default, actually
    @Column(name = "FILENAME") // Defaults to IMAGES
    private Set<String> images = new HashSet<>(); // Initialize field here 
    ```
    - 값 타입 요소의 컬렉션
      - `@ElementCollection` JPA 애너테이션 사용
    ``` 
    @Query("select i from Item i inner join fetch i.images where i.id = :id")
    Item findItemWithImages(@Param("id") Long id);

    @Query(value = "SELECT FILENAME FROM IMAGE WHERE ITEM_ID = ?1",
            nativeQuery = true)
    Set<String> findImagesNative(Long id); 
    ```
  - 8.1.5 식별자 백 매핑
    - 중복 요소를 허용하는 정렬되지 않은 컬렉션
    ``` 
    @ElementCollection
    @CollectionTable(name = "IMAGE")
    @Column(name = "FILENAME")
    @org.hibernate.annotations.GenericGenerator(
            name = "sequence_gen",
            type = org.hibernate.id.enhanced.SequenceStyleGenerator.class
    )
    @org.hibernate.annotations.CollectionId(
            column = @Column(name = "IMAGE_ID"),
            generator = "sequence_gen"
            // 기존의 type 인자는 org.hibernate.annotations.CollectionIdJavaType 애너테이션을 사용하는 것으로 변경
    )
    @org.hibernate.annotations.CollectionIdJavaType(org.hibernate.type.descriptor.java.LongJavaType.class)
    private Collection<String> images = new ArrayList<>();
    ```
  - 8.1.6 리스트 매핑
    - 같은 순서로 데이터 요소를 표시해야 하는 경우
      ``` 
      @ElementCollection
      @CollectionTable(name = "IMAGE") // Default, actually
      @OrderColumn // Enables persistent order, Defaults to IMAGES_ORDER
      @Column(name = "FILENAME") // Defaults to IMAGES
      private List<String> images = new ArrayList<>();
      ```
      - A,B,C,D 파일이 있을때 A를 삭제하면 B,C,D에 대한 `update` 쿼리가 3번 일어남
  - 8.1.7 맵 매핑
    ``` 
    @ElementCollection
    @CollectionTable(name = "IMAGE")
    @MapKeyColumn(name = "FILENAME")
    @Column(name = "IMAGENAME")
    private Map<String, String> images = new HashMap<>();
    ```
    - 정렬되지 않음(그림 8.5)
  - 8.1.8 정렬 컬렉션과 순차 컬렉션
    ``` 
    @ElementCollection
    @CollectionTable(name = "IMAGE")
    @MapKeyColumn(name = "FILENAME")
    @Column(name = "IMAGENAME")
    @org.hibernate.annotations.SortComparator(ReverseStringComparator.class)
    private SortedMap<String, String> images = new TreeMap<>();
    ```
    - 정렬된 컬렉션은 하이버네이트 기능
    - 8.1.7의 스키마와 동일
* 8.2 컴포넌트 컬렉션
  - `Image`에 속성을 추가하여 임베드 가능한 컴포넌트로 만듬
    ``` 
    @Embeddable
    public class Image{
      @Column(nullable=false)
      private String filename;
      private int width;
      private int height;  
    }
    ```
    - 동등성 고려해야 함
  - 8.2.1 컴포넌트 인스턴스의 동등성
    - `someItem`이 `HashSet`이라면 몇 개의 이미지가 있을까요?
      ``` 
      someItem.addImage(new Image("backgroud.jpg", 640, 480));
      someItem.addImage(new Image("backgroud.jpg", 640, 480));
      someItem.addImage(new Image("backgroud.jpg", 640, 480)); 
      ```
      - `equals()`, `hashCode()` 메서드를 재정의해야함
  - 8.2.2 컴포넌트의 세트
    ```
    @ElementCollection
    @CollectionTable(name = "IMAGE")
    @AttributeOverride(
            name = "filename",
            column = @Column(name = "FNAME", nullable = false)
    )
    private Set<Image> images = new HashSet<>(); 
    ```
  - 8.2.3 컴포넌트의 백
    - 대리키를 만들고 Image 클래스는 널 허용 프로퍼티 포함
    - `equals()`, `hashCode()` 오버라이딩
    ```
    @Embeddable
    public class Image {
      @Column(nullable = true) // Can be null if we have surrogate PK!
      private String title;
      @Column(nullable = false)
      private String filename;
      private int width;
      private int height;
    
      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return width == image.width &&
                height == image.height &&
                Objects.equals(title, image.title) &&
                filename.equals(image.filename);
      }

      @Override
      public int hashCode() {
        return Objects.hash(title, filename, width, height);
      }
    }
    
    @ElementCollection
    @CollectionTable(name = "IMAGE")
    @GenericGenerator(name = "sequence_gen", type = org.hibernate.id.enhanced.SequenceStyleGenerator.class)
    @org.hibernate.annotations.CollectionIdJavaType(org.hibernate.type.descriptor.java.LongJavaType.class)
    @org.hibernate.annotations.CollectionId(
            column = @Column(name = "IMAGE_ID"),
            generator = "sequence_gen"
    )
    private Collection<Image> images = new ArrayList<>(); 
    ```
  - 8.2.4 컴포넌트 값의 맵
    ```
    @ElementCollection
    @CollectionTable(name = "IMAGE")
    @MapKeyColumn(name = "TITLE") // Optional, defaults to IMAGES_KEY
    private Map<String, Image> images = new HashMap<>(); 
    ```
  - 8.2.5 맵 키로서의 컴포넌트
    - 키와 값이 모드 `@Embeddable` 타입인 경우
    ``` 
    @Embeddable
    public class Filename {
      @Column(nullable = false) // Must be NOT NULL, part of PK!
      private String name;
    }
    
    @Embeddable
    public class Image {
      @Column(nullable = true) // Can be null, not part of PK!
      private String title;

      @NotNull
      private int width;

      @NotNull
      private int height;
    }
    
    @Entity
    public class Item {
      @ElementCollection
      @CollectionTable(name = "IMAGE")
      private Map<Filename, Image> images = new HashMap<>();
    }
    ```
  - 8.2.6 임베드 가능한 컴포넌트의 컬렉션
    - User에 여러 Address가 있고 각 Address에는 연락처가 여러개 있는 경우
    ``` 
    @Embeddable
    public class Address {
      @NotNull
      @Column(nullable = false)
      private String street;

      @ElementCollection(fetch = FetchType.EAGER)
      @CollectionTable(
            name = "CONTACT", // Defaults to USER_CONTACTS
            joinColumns = @JoinColumn(name = "USER_ID")) // Default, actually
      @Column(name = "NAME", nullable = false) // Defaults to CONTACTS
      private Set<String> contacts = new HashSet<>();
    } 
    ```
* 8.3 엔티티 연관관계 매핑
  - 두 엔티티 클래스 간의 연관관계 매핑
  - 그림 8.13
  - 8.3.1 가능한 가장 간단한 연관관계
    - 단방향 다대일 연관관계
    ```
    @Entity
    public class Bid {
      @ManyToOne(fetch = FetchType.LAZY) // Defaults to EAGER
      @JoinColumn(name = "ITEM_ID", nullable = false)
      private Item item;
    } 
    ```
  - 8.3.2 양방향으로 만들기
    ``` 
    @Entity
    public class Item {

      @OneToMany(mappedBy = "item", // Required for bidirectional association
            fetch = FetchType.LAZY) // The default
      private Set<Bid> bids = new HashSet<>();
    }
    ```
  - 8.3.3 상태 연쇄 적용
    - 엔티티 상태 변화가 다른 엔티티와의 연관관계에 걸쳐 연쇄 적용 방식으로 처리
      - 심각한 결과를 초래할 수 있으므로 조심해야 함
    ``` 
    Item somItem = new Item("some item");
    Bid someBid = new Bid(new BigDecimal("123.00"), someItem);
    someItem.addBid(someBid); 
    ```
    - 전이적 영속성 활성화
      ```
      Item item = new Item("Foo");
      Bid bid = new Bid(BigDecimal.valueOf(100), item);
      Bid bid2 = new Bid(BigDecimal.valueOf(200), item);

      itemRepository.save(item);
      item.addBid(bid);
      item.addBid(bid2);
      bidRepository.save(bid);
      bidRepository.save(bid2); 
      ```
      - 새 인스턴스는 비영속(transient) 상태이므로 영속화 해야함
      - `CascadeType.PERSIST`를 사용하여 단순화 
        ```
        @Entity
        public class Item {
          @OneToMany(mappedBy = "item", cascade = CascadeType.PERSIST)
          private Set<Bid> bids = new HashSet<>();
        }
      
        Item item = new Item("Foo");
        Bid bid = new Bid(BigDecimal.valueOf(100), item);
        Bid bid2 = new Bid(BigDecimal.valueOf(200), item);

        item.addBid(bid);
        item.addBid(bid2);
        itemRepository.save(item);
        ```
    - 삭제 연쇄 적용
      ```
      Item retrievedItem = itemRepository.findById(item.getId()).get();

      for (Bid someBid : bidRepository.findByItem(retrievedItem)) {
          bidRepository.delete(someBid);
      }
      ```
      - `CascadeType.REMOVE`를 사용하여 단순화
        ```
        @Entity
        public class Item {
          @OneToMany(mappedBy = "item", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
          private Set<Bid> bids = new HashSet<>();
        }
        
        itemRepository.delete(item);
        ```
        - `Item` 삭제로 연관된 모든 `Bid` 삭제
        - 실제로는 비효율적, Bid에서 하나하나 삭제함
        - `someItem.getBids().clear()`를 통해 한번에 삭제 할 수 있으나 `Bid`에 대한 참조가 없으므로 실제로는 고아객체가 됨
    - 고아 객체 제거 활성화
      - `orphanRemoval=true` 지정
        - 컬렉션에서 Bid가 제거될 때 하이버네이트가 Bid를 영구적으로 제거
      ```
      @Entity
      public class Item {
        @OneToMany(mappedBy = "item", cascade = CascadeType.PERSIST,
            orphanRemoval = true)
        private Set<Bid> bids = new HashSet<>();
      }
      ```
  - 여전히 처리 못한 것들
    - 한번에 삭제
    - 고아 객체 찜찜함(그림 8.16)
      - `Item`에서 `Bid`를 삭제해서 제거해도 `User`에서는 고아 객체로 남아버림
      - `User`에서 `@OntToMany`를 가져야 하는지도 검토
  - 외래키에 대한 `ON DELETE CASCADE` 활성화
    - 메모리 상에 `Bid`를 로드하지 않고 삭제
    - DDL에 `foreign key (ITEM_ID) references ITEM on delete cascade` 선언
    - `@OnDelete` 애너테이션 사용
    ``` 
    @Entity
    public class Item {
      @OneToMany(mappedBy = "item", cascade = CascadeType.PERSIST)
      // Hibernate quirk: Schema options usually on the 'mappedBy' side
      @org.hibernate.annotations.OnDelete(
            action = org.hibernate.annotations.OnDeleteAction.CASCADE
      )
      private Set<Bid> bids = new HashSet<>();
    }
    ```
  - 어떤 방식이든 `cascade`를 사용할 땐 주의해야함
    - `@OnDelete` 이용시 2차 전역 캐시를 자동으로 비우지 않음
  - `@Emabeddable`과 `@ElementCollection` 활용 권장
  - `@OneToMany`가 필요한가도 고민해야함 <- `@ManyToOne`을 통한 단방향 권장