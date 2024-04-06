# 04. 스프링 데이터 JPA 다루기
* 4.1 스프링 데이터 JPA 소개
  - 데이터 소스 빈 구성
  - 엔티티 매니저 팩터리 빈 구성
  - 트랜잭션 매니져 빈 구성
  - 애너테이션을 통한 트랜잭션 관리
* 4.2 스프링 데이터 JPA 프로젝트 생성
* 4.3 스프링 데이터 JPA 프로젝트 구성을 위한 첫 단계
* 4.4 스프링 데이터 JPA를 이용한 쿼리 메서드 정의
* 4.5 쿼리 결과 제한, 정렬, 페이징
  - 쿼리 메서드의 결과 제한
    - first 키워드와 top 키워드
  - 정렬
    - Sort
    - ex> `Sort.by("registrationDate")`
  - 페이징
    - 페이징 정보를 위한 인터페이스 : Pageable
    - ex> `PageRequest.of(1, 3)`
* 4.6 결과 스프트리밍
  - Iterable 또는 모든 컬렉션 타입의 대안으로 Streamable 지원 
* 4.7 @Query 애너테이션
  - `@Query` 애너테이션을 이용해 사용자 정의 쿼리 지원
  ``` 
  @Query("select count(u) from User u where u.active = ?1")
  int findNumberOfUsersByActivity(boolean active);

  @Query("select u from User u where u.level = :level and u.active = :active")
  List<User> findByLevelAndActive(@Param("level") int level, @Param("active") boolean active);

  @Query(value = "SELECT COUNT(*) FROM USERS WHERE ACTIVE = ?1", nativeQuery = true)
  int findNumberOfUsersByActivityNative(boolean active);

  @Query("select u.username, LENGTH(u.email) as email_length from #{#entityName} u where u.username like %?1%")
  List<Object[]> findByAsArrayAndSort(String text, Sort sort); 
  ```
* 4.8 프로젝션
  - 일부 속성에만 접근
  - Projection
  ``` 
  public class Projection {
    public interface UserSummary {
        String getUsername();
        @Value("#{target.username} #{target.email}")
        String getInfo();
    }

    public static class UsernameOnly {
        private String username;
        public UsernameOnly(String username) {
            this.username = username;
        }
        public String getUsername() {
            return username;
        }
    }
  } 
  ```
  - UserRepostiroy
  ``` 
  List<Projection.UserSummary> findByRegistrationDateAfter(LocalDate date);
  List<Projection.UsernameOnly> findByEmail(String email);
  <T> List<T> findByEmail(String username, Class<T> type);
  ```
  - Test
  ``` 
  List<Projection.UsernameOnly> usernames = userRepository.findByEmail("mike@somedomain.com", Projection.UsernameOnly.class);
  List<User> users = userRepository.findByEmail("mike@somedomain.com", User.class); 
  ```
* 4.9 수정 쿼리
  - deleteByLevel 메서드
    - 쿼리를 실행한 다음, 반환된 인스턴스를 하나씩 제거
  - deleteBulkByLevel 메서드
    - 사용자를 일괄적으로 제거
    - 단 하나의 JPQL 쿼리만 싱핼
    - 생명주기와 관련된 콜백 메서드를 실행하지 않음
* 4.10 예제 기반 쿼리
  - 프로브
    - 프로퍼티의 값이 미리 설정된(already-set) 도메인 객체
  - ExmapleMatcher
    - 특정 프로퍼티를 매칭하기 위한 규칙 제공
  - Example
    - 프로브와 ExampleMatcher를 결합해 쿼리 생성
  ``` 
  @Test
  void testEmailWithQueryByExample() {
      User user = new User();
      user.setEmail("@someotherdomain.com");

      ExampleMatcher matcher = ExampleMatcher.matching()
              .withIgnorePaths("level", "active")
              .withMatcher("email", match -> match.endsWith());

      Example<User> example = Example.of(user, matcher);

      List<User> users = userRepository.findAll(example);

      assertEquals(4, users.size());

  }

  @Test
  void testUsernameWithQueryByExample() {
      User user = new User();
      user.setUsername("J");

      ExampleMatcher matcher = ExampleMatcher.matching()
              .withIgnorePaths("level", "active")
              .withStringMatcher(ExampleMatcher.StringMatcher.STARTING)
              .withIgnoreCase();

      Example<User> example = Example.of(user, matcher);

      List<User> users = userRepository.findAll(example);

      assertEquals(3, users.size());

  } 
  ```