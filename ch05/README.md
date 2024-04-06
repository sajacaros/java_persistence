# 05. 영속성 클래스 매핑
* 5.1 엔티티와 값 타입 이해
  * 5.1.1 잘게 세분화된 도메인
    - 잘게 세분화되고 풍부한 도메인 모델을 지원
  * 5.1.2 애플리케이션 개념 정의
    - 시나리오에 따른 설계 변경
      - 그림 5.2, 5.3 참고
  * 5.1.3 엔티티와 값 타입 구분
    - 모든 것을 값 타입 클래스로 만들고 꼭 필요한 경우에만 엔티티로 승격
    - 클래스 구현시 주의사항
      - 공유 참조
        - 값 타입 인스턴스에 대한 공유 참조를 피해야 합
          - 생성자를 활용해 불변 클래스로 만드는 것 고려
      - 수명주기 의존성
        - cascade 규칙 고려
      - 식별자
        - 엔티티 클래스는 식별자 프로퍼티 필요
        - 값 타입 클래스는 식별자 프로퍼티 없음
* 5.2 식별자가 있는 엔티티 매핑
  * 5.2.1 자바 동일성과 동등성 이해
    - 객체 동일성
      - 두 객체가 JVM에서 같은 메모리 위치를 차지하는 경우
      - `a == b`로 확인 가능
    - 객체 동등성
      - `a.equals(b)`로 확인 가능
    - 데이터베이스 동일성
      - 동일한 테이블과 기본키 값으로 확인 가능
  * 5.2.2 첫 번째 엔티티 클래스와 매핑
  ``` 
  @Entity
  public class Item {
    @Id
    @GeneratedValue(generator = "ID_GENERATOR")
    private Long id;

    public Long getId() { // Optional but useful
        return id;
    }
  } 
  ```
  * 5.2.3 기본키 선정
    - 후보키(candidate key)
    - 자연키(natural key)
      - 복합 자연키(composite natural key)
    - 대리키(surrogate key)
  * 5.2.4 키 생성기 구성
    - `GenerationType.AUTO`
      - 데이터베이스의 SQL 방언을 통해 적절한 전략 선택
    - `GenerationType.SEQUENCE`
      - 하이버네이트가 데이터베이스에 `HIBERNATE_SEQUENCE`라는 시퀀스로 관리
      - INSERT가 실행되기 전 개별적으로 호출
    - `GenerationType.IDENTITY`
      - 하이버네이트가 INSERT가 실행될 때 숫자값을 자동으로 생성하는 특별한 자동 증가 기본키 칼럼이 데이터베이스 있을 것으로 기대
    - `GENERATIONType.TABLE`
      - 하이버네이트가 데이터베이스 스키마 내의 다음 숫자 기본키 값이 담긴 별도 테이블 사용
      - INSERT 전에 읽혀지고 업데이트 됨
      - 기본 이름은 `HIBERNATE_SEQUENCES`
    - `package-info.java`파일에 공통적으로 적용하고 싶다면?
      - `@org.hibernate.annotations.GenericGenerator` 애너테이션 사용
      ``` 
      @org.hibernate.annotations.GenericGenerator(
      name = "ID_GENERATOR",
        type = org.hibernate.id.enhanced.SequenceStyleGenerator.class,
        parameters = {
          @org.hibernate.annotations.Parameter(
            name = "sequence_name",
            value = "JPWHSD_SEQUENCE"
          ),
          @org.hibernate.annotations.Parameter(
            name = "initial_value",
            value = "1000"
        )
      })
      ```
      - 기본키 값들이 인접하지 않더라도 한 테이블 내에서 고유하면 상관 없음
  * 5.2.5 식별자 생성기 전략
    - INSERT 전에 식별자 값을 독립적으로 생성하는 삽입 전 전략 사용 권고
    - 네이티브 데이터베이스 시퀀스가 지원되는 경우 이를 사용
    - 지원되지 않을 경우 enhanced-sequence 사용(`package-info.java`에 선언하는 전략)
* 5.3 엔티티 매핑 옵션
  * 5.3.1 이름 제어
  * 5.3.2 동적 SQL 생성
  * 5.3.3 엔티티를 불변으로 만들기
  * 5.3.4 엔티티를 서브쿼리에 매핑