# 02. 프로젝트 시작
* 2.1 하이버네이트 소개
* 2.2 스프링 데이터 소개
* 2.3 JPA를 이용한 "Hello World" 예제
  - 2.3.1 영속성 단위 구성
  - 2.3.2 영속성 클래스 작성
  - 2.3.3 메시지 저장과 로딩
* 2.4 네이티브 하이버네이트 구성
  - `SessionFactory` 사용
* 2.5 JPA와 하이버네이트 간 전환
  - `EntityManagerFactory`에서 `SessionFactory` 사용
    ```
    private static SessionFactory getSessionFactory(EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }
    ```
  - `SessionFactory`에서 `EntityManagerFactory` 사용
    ``` 
    private static EntityManagerFactory createEntityManagerFactory() {
        Configuration configuration = new Configuration();
        configuration.configure().addAnnotatedClass(Message.class);

        Map<String, String> properties = new HashMap<>();
        Enumeration<?> propertyNames = configuration.getProperties().propertyNames();
        while (propertyNames.hasMoreElements()) {
            String element = (String) propertyNames.nextElement();
            properties.put(element, configuration.getProperties().getProperty(element));
        }

        return Persistence.createEntityManagerFactory("ch02", properties);
    }
    ```
* 2.6 스프링 데이터 JPA를 이용한 "Hello World" 예제
* 2.7 엔티티 영속화에 대한 접근 방식 비교
  - JPA
    - JPA API를 사용하며 영속성 공급자가 필요
    - EntityManagerFactory, EntityManager, 트랜잭션에 대한 명시적 관리 필요
  - 네이티브 하이버네이트
    - 네이티브 하이버네이트 API 사용
    - 기본 하이버네이트 구성파일(hibernate.cfg.xml)로 구성 작업
    - SessionFactory, Session, 트랜잭션에 대한 명시적인 관리가 필요
  - 스프링 데이터 JPA
    - 프로젝트에 추가적인 스프링 데이터 의존성 필요
    - 트랜잭션 매니저를 포함해서 프로젝트에 필요한 빈의 생성도 처리
    - 리파지터리 인터페이스를 선언해 두기만 하면 스프링 데이터가 데이터베이스와 상호작용하는 구현체 생성
  - 속도 비교
    - 하이버네이트와 JPA는 비슷하고 스프링 데이터 JPA가 가장 오래 걸림