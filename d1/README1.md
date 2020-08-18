# keelim Android Performance enhancement

## Role: Build and Test

- - -
0. 글로벌 락 해결 방법
    - 글로벌 락 해결이 어렵다. 
    - 대체 가능 방안이 없음 -> 코루틴 시도를 했지만 (코틀린 도입 방법 error)
    
1. 익명 클래스 vs 람다 
    - 성능 측정이 어렵다. 
    - 메모리 누수를 찾기 어렵다. 
    
2. Hashmap with WindowManagerGlobal 
    - 구현 중 
    - 테스트 오류가 있어서 클래스 작업 중
    - Optional + Stream API
- - -

1. 테스트 현황

    - 일반 빌드 테스트 완료
    - 커스텀 빌드 테스트 완료 (빌드 아이디 수정 반영 가능)
    - 1 HashMap 1차 테스트 실패, error 다수 발견 (수정중), unit 은 통과
    - (1)copyFrom with clone 실패 -> final error
    - (2)copyFrom with clone 실패-> contructor error 
    - (3)copyFrom compile and build 성공 -> 포팅 작업 중
    