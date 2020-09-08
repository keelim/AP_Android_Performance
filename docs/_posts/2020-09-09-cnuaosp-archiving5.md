---
layout: post
title: "CNU AOSP 아카이빙3"
date: 2020-08-30 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## 1. 문자열을 연결할 때 + 연산자와 concat 메소드의 성능차이

concat 메소드를 적용해볼 수 있는 안드로이드 파일

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image01.png)

Java에서 String 은 불변(immutable)한 특성을 가진다.

연산자의 경우 Java 1.5 이전에는 concat 메소드와 동일하게 새로운 String 인스턴스를 생성했지만, Java 1.5 이후에는 내부적으로 StringBuilder로 변환해서 처리하기 때문에 StringBuilder와 동일하다고 보면 된다.

### 각 함수의 특성

- concat :
  일단 concat를 쓸 때 초기값이 null이면 안 된다.
  문자열을 계속해서 붙인다고 가정한다면, 붙일 때마다 주소값을 각자 할당받게 되는 것이다.

- StringBuilder :
  StringBuilder는 append를 통해서 문자열을 붙여준다.
  concat과의 차이는 문자열을 계속 붙여도 주소값이 변하지 않는다는 것이다.

### Test1

안드로이드 파일의 + 연산자 부분을 테스트 하기 위한 코드이다.
![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image02.png)

### Test2

연산자 대신 concat 메소드를 활용해서 바꾼 코드이다.
![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image03.png)

### 테스트 결과

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image04.png)

Test1의 바이트코드이다.
바이트코드를 보면 + 연산자를 쓸 때, StringBuilder.append 메소드가 호출된 것을 확인할 수 있다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image05.png)

Test2의 바이트코드이다.
concat 메소드가 호출된 것을 확인할 수 있다.

바이트코드 결과에 따르면, Test1은 필드들을 처리하는데만 380줄이 필요했고,
Test2는 185줄만이 쓰였다.

(참고자료)
<https://stackoverflow.com/questions/47605/string-concatenation-concat-vs-operator>

<https://stackoverflow.com/questions/1532461/stringbuilder-vs-string-concatenation-in-tostring-in-Java>

결과적으로는 특정한 상황에서는 +보다는 concat 메소드를 사용하는 것이 더욱 성능적으로 이점이 있다고 볼 수 있다고 생각했다.

### <외국 블로그의 테스트 결과 >

(출처 : <https://stackoverflow.com/questions/1532461/stringbuilder-vs-string-concatenation-in-tostring-in-Java> )

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image06.png)

java8 이후로, 사실상 concat 메소드와 + 연산자와의 성능 차이는 거의 없어 보인다.
더구나 String의 필드의 제어자가 static final이어서 변수를 초기화를 한 이후에 해당 변수값이 바뀌는 일이 없고 호출만 되므로 사실상 성능 차이가 없다고 본다.

## 2. 삼항연산자와 Math 클래스의 함수와의 성능 차이

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image07.png)

정수의 대소를 비교하기 위해, Math 클래스의 Math.max나 Math.min 정적 함수를 대신하여,
삼항연산자로 정수의 대소 비교를 대체하면 성능상의 이점이 있을까 하여 테스트를 진행했다.

테스트 결과 Math 클래스의 함수를 쓰는 것이 삼항연산자를 쓰는 것보다 빨랐다.
삼항연산자의 중첩이 if-else의 중첩으로 이어지면서 시간이 늦어진다고 추측되었다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image08.png)

## 3. copyFrom 메소드 대신 clone 메소드 이용

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image09.png)

클래스의 모든 필드를(객체 필드 포함) 복사하는 copyFrom이라는 메소드 대신, clone 메소드를 써서 클래스의 객체 자체를 복사하는 것이 성능상의 이점이 있을 것 같아서 진행했다.

clone은 원본 객체의 필드 값과 같은 값을 가지는 새로운 객체를 생성한다.

특징 :
Object 클래스에 clone 메소드는 protected로 정의되어 있으므로 public으로 override를 해야만 한다.
clone 메소드를 사용하려면 해당 클래스는 Cloneable 인터페이스를 implements 해야 한다.
(Cloneable 인터페이스에는 아무런 메소드도 정의되어 있지 않다.)

참조 :
<https://taetaetae.github.io/2018/08/21/how-to-use-cloneUtils/>
<https://gwbb.tistory.com/16>

객체를 복사하는 방법에는 2가지로, Shallow Copy 와 Deep Copy가 있다.
한국어로 표현하면 얕은 복사와 깊은 복사라고 이야기를 한다.
Shallow Copy는 객체의 주소값을 복사하는 방식이고, Deep Copy는 객체의 실제 값을 복사하는 방법이다.

1. 얕은 복사(Shallow Copy)

객체를 복사할 때, 해당 객체만 복사하여 새 객체를 생성한다.
복사된 객체의 인스턴스 변수는 원본 객체의 인스턴스 변수와 같은 메모리 주소를 참조한다.
따라서, 해당 메모리 주소의 값이 변경되면 원본 객체 및 복사 객체의 인스턴스 변수값은 같이 변경이 된다.
만약 객체 필드를 clone할 때, 해당 객체의 클래스에 clone 메소드의 오버라이드 처리를 하지 않으면, 객체가 shallow copy 처리가 되어 심각한 오류를 발생할 수도 있다.

<https://taetaetae.github.io/2018/08/21/how-to-use-cloneUtils/>
 해당 링크에 자세히 설명되어있다.

2. 깊은 복사(Deep Copy)

객체를 복사할 때, 해당 객체와 인스턴스 변수까지 복사하는 방식이다.
보통 clone 메소드는 Deep Copy를 의미한다.
전부를 복사하여 새 주소에 담기 때문에 참조를 공유하지 않는다.

깊은 복사를 한 것은 데이터는 같지만, 주소가 다르고 값을 변경해도 영향을 주지 않는다.
(완전히 서로 다른 객체인 것을 증명한다)

클래스 내에 clone이 안되는 변수는(객체 변수) 별도 처리가 필요하다.

클래스 내에 있는 멤버 변수가 원시 변수(int, char, float 등), Immutable Class (String, Boolean, Integer 등) 또는 Enum 형식일 때는 원본의 값을 바로 대입해도 되지만, 그렇지 않을 때는 멤버 변수의 clone을 호출하여 복사해야 한다.

위에서 말한 것과 같이 clone이 안되는 경우는 (다시 말하면 원시변수나 Immutable Class, enum 등 clone을 지원하는 객체가 아닐 경우) 별도로 clone이 되도록 설정이 필요하다.

즉 복사하려는 객체의 클래스에 Cloneable를 implements하고 clone 메소드를 override 해준 다음,
최상위 객체의 clone 메소드를 알맞게 수정을 해주면 객체의 깊은 복사가 가능하다.

수정한 copyFrom메소드와 override한 clone메소드
객체 필드들을 deep copy 하기 위한 구성예시

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image09.png)


또한, 바뀐 copyFrom메소드를 적용하기 위해, copyFrom이 호출되는 모든 파일의 호출 코드를 바꿨다
예시
![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image11.png)

문제점

DisplayInfo.java 파일에서 copyFrom메소드가 포함된 DisplayInfo 클래스는 final 클래스로 선언돼있다.
안드로이드의 대부분 자바 파일들의 class들도 대부분 final로 선언돼있다.

바꾼 코드대로 빌드를 하는 도중에, final과 관련된 에러가 난다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image12.png)

에러 메시지를 보면 DisplayInfo 클래스는 final 클래스이기 때문에, DisplayInfo 클래스의 객체인 mDisplayInfo, mTempDisplayInfo는 copyFrom 즉 clone이 된 객체를 대입할 수 없다는 내용이다.

그래서 copyFrom 메소드를 포함하는 DisplayInfo 파일과 해당 메소드를 호출하는 클래스들의 모든 final 제어자를 삭제하고 빌드를 했다.

빌드와 포팅 시 에러는 나지 않았으나, 포팅 된 핸드폰의 전원을 켜도 다시 꺼지는 에러가 발생했다.
(아마도 final 제어자와 관련된 에러 추정)

문제점
final로 선언된 경우 컴파일러의 조건부 컴파일로 인해 성능의 향상이 있다.
final 클래스로 인한 성능 향상을 포기할 만큼 변경된 copyFrom 메소드의 성능 개선 효과가
좋지는 않기에 그만두기로 했다.

안드로이드 개발진도 final로 인한 성능 향상을 포기할 수 없어서, 기존 copyFrom메소드로
DisplayInfo 클래스 파일의 모든 필드를 복사한 것으로 여겨진다.

4. Builder Pattern

안드로이드 파일들은 여러 개발자가 모여서 짠 것이기 때문에 알아보기 힘든 코드가 있을 수 있다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image13.png)

WindowManager.java 에 텔레스코핑 패턴을 형성하는 생성자들을 확인할 수 있었다.

텔레스코핑 패턴이란 매개변수 수를 달리하는 생성자들을 여러 개 만들어 호출된 Constructor 안에서 다른 Constructor를 호출하는 구조이다.

단점은 매개변수의 수가 증가하면 무척 번거로워지고, 생성자 작성에 대한 비용뿐만 아니라, 가독성까지도 떨어진다. 코드를 파악하는 사람으로서도, 그 값들이 어떤 의미인지를 알아야 하며, 개수도 세야 하는 단점도 있다.

그래서 텔레스코핑 생성자 패턴을 대신해서 만든 것이 builder 생성자 패턴이다.

특징은 원하는 객체를 바로 생성하는 대신, 필수 매개변수들을 parameter로 받는 builder의 생성자를 호출하여 builder 객체를 얻는다.

장점으로는 생성객체를 불변으로 만들 수 있고, 코드 작성이 쉬우며, 가독성이 좋다.
별도의 setter 메소드들을 사용하므로, 여러 개의 가변 인자 매개변수를 가지는 효과를 낼 수 있고,
유연성이 좋다.

매개변수들의 값이 미리 설정된 builder는 훌륭한 추상 팩토리 역할을 한다.

문제점
어떤 객체를 생성하려면 우선 그것의 builder를 생성해야 한다.
객체의 생성 비용이 그렇게 큰 정도는 아니어도, 성능이 매우 중요한 상황에서는 문제가 될 수 있다.

또한, 텔레스코핑 패턴보다 코드가 길어지기 쉽다.

애초에 이 프로젝트가 성능 개선이 목적이기에 코드 작성이 쉽고, 가독성이 좋은 것은 목표로 한 것이 아니다.

실제 위의 코드로 빌더 패턴을 유사하게 만든 후 테스트한 결과 성능은 더 나빠진 것을 확인할 수 있었다.

출처: <https://aroundck.tistory.com/152>

5. Hash Map으로 바꿀 수 있는 부분

총 2가지 부분을 개선할 수 있었다.

1. frameworks/base/services/core/java/com/android/server/wm/WindowManagerService.java
1. frameworks/base/services/core/java/com/android/server/policy/PhoneWindowManager.java

위에 부분들은 로직 상, if ~ else가 중첩으로 되어 있는 부분이다.
이를 Functional interface와 HashMap을 이용을 하여, 코드 길이를 줄이고 성능 향상이 가능하다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image14.png)

위와 같이 HashMap에서 key 값으로 Integer 값, value 값으로 함수형 인터페이스 중 2개의 파라미터는 받고 리턴값이 없는 BiConsumer를 사용하여 작성했다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image15.png)

호출 부분은, key 값을 주고 value 값을 받아오는 것은 null 체크를 하여 null이 아닐 경우
함수형 인터페이스를 실행하고 리턴 값으로 bad token을 넘긴다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image16.png)

PhoneWindowManger에 inner class인 PolicyHandler의 경우 handleMessage(Message message)의
메소드 1개만을 가지고 있다.

이에 HashMap을 정의를 하고 이를 생성자에서 초기화를 하여 handleMessage scope 에서는 아래와 같이 구현했다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image17.png)

WindowManagerService와는 다르게 PhoneWindowManager에서는 Functional interface인
Consumer를 사용하여 파라미터 1개를 가지고 처리를 할 수 있도록 하였다.

2개의 HashMap으로 변경한 Android r-17 이미지는 포팅이 가능한 것을 테스트 완료를 하였고,
성능측정을 하기 위한 애플리케이션 로직을 구상하는 중이다.

HashMap version test에 따른 어플리케이션 코드
<https://github.com/cnuaosp/AOSPTesting/blob/feature%2Fhandler/app/src/main/java/com/keelim/testing/ui/addwindowtest/AddWindowTestActivity.kt>

주요 테스트 로직

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image18.png)
show() dismiss ()를 반복하면서 테스트 실행결과를 보일 수 있도록 넘긴다.

테스트 실행 결과

주기 같이 나타난 이유는 테스트 1번 세트가 10000번 이기 때문이다.
10000번 이상으로 넘어가면 Memory Exception이 발생해, 실험 데이터가 없어진다.
이에 세트를 반복으로 진행을 하려 했으나, 세트 반복 시, 해시 충돌까지의 값을 확인할 수가 없었다.



## 6. 지워도 되는 코드 탐색

### 1. 의미 없는 boolean 연산

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image19.png)

위와 같이 사실상 true==true나 ture==false 꼴을 가지는 코드가 있다.
딱히 의미는 없지만, 가독성이 좋아서 쓰이는 코드이다.
자바 바이트 코드상에서 4바이트 정도를 더 차지한다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image20.png)

컴파일러가 최적화를 해주는지 알아내기 위해서 테스트를 진행하였다.
테스트 결과 컴파일러가 미리 최적화를 하는 것으로 드러났다.

### 2. 빈 try catch 문


![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image21.png)

예외를 무시하는 용도로, 예외가 치명적이지 않을 때 사용한다.
프로그래머들이 주석 용도로 쓰는듯하다.

## 7. 회전 최적화 시도

안드로이드가 landscpae <-> portrait 전환 시, 평소 상당한 끊김을 느껴서 시도를 해봤다.

예상과 다르게 어떤 코드의 지연이 있는 것이 아니라,
디자인상에서 애초에 안드로이드 회전처리가 앱을 파괴하고 다시 생성한다는 것을 알게
되었다.

공식문서에서도, 끊기는 것이 당연하다고 서술되어있으며,
이 부분은 수정이 힘든 것을 깨달았다.

1. 앱 행동수정
   최초 회전 명령부터 완료 시점까지의 과정 중에 필요 없거나 합쳐질 수 있는 과정을
   제거해보려 했으나, 너무 복잡했고, 안드로이드는 설정이 어느 한 개만 바뀌어도 앱을 종료시키고 다시 시작하기 때문에 이런 설정변경 관련 행동을 수정하려면 관련된 모든 flag를 숙지해야 했고, 프레임워크 외 레벨까지 같이 조정돼야 함을 깨닫고 중단했다.

2. 비트맵처리
   그래서 설정 처리 후 비트맵을 그리는 부분을 최적화할 수 없을까 하고 생각을 했다.

비트맵 관련 함수는 속도를 위해 전부 jni를 통한 c call로 처리된다.
여러 영상처리 들이 matrix를 이용해서 처리됨을 파악했다.
matrix가 필요 없는 단순한 rotate와 transpose를 따로 처리할 수 있고 성능이 좋아지지만, 코드의 일관성이 깨진다는 것을 확인했다.

진행하다가 안드로이드에서 이런 일관성을 해칠 수 있는 특수한 사례, 성능을 위해
코드를 수정하고 싶은 사람들이 개인적으로 custom을 해서 사용하라고 ndk를 제공한다는
생각이 들어서 중단하기로 했다.

## 8. 멀티 스레드 관련

안드로이드의 모든 service, ui는 메인 스레드 하나에서 처리된다.
메인 스레드에서 지연이 생기면 사용자 경험에 치명적인 타격이 되므로 꼭 필요한 일만
해야 한다.

이 외의 다른 작업을 위한 스레드가 다른 작업을 하기 위해선
Handler, Activity.runOnUIThread(Runnable), View.post(Runnable)나 AsyncTask를 이용해야한다.

또한, 스레드의 충돌을 반드시 처리해야 한다.

1. 반복 시 size method 매번 호출하는 문제

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image22.png)

이렇게 반복문에서 매번 size를 구하는 코드를 보고 알게 되었다.
arraylist의 동기화 때문이다.
확인해보니 호출 이전에 synchronized(object)로 이미 동기화를 한다.

필요 없어 보이지만 존재하는 이유를 추측해 봤다.

1. 어떤 앱이 억지로 접근할 수도 있다.
2. 어떤 오류 상황에서 예외적인 에러가 발생할 수도 있다.

지우면 어떻게 될지 예측할 수 없다. <<<아직 찾는 중

1.1 매번 size()를 계산하지 않기 위한 시도
자료구조의 add, get 코드 자체에 동기화가 걸려있으면 된다.
custom하게 구현하는 것은 배제하고 자바에서 공식 지원되는 concurrency 자료구조는 2가지가 있다.

## 1.1.1 CopyOnWriteArrayList
concurrent collection >> 동기화 + 동시성에 강점이 있다.
사본을 뜨는 것으로 스레드 충돌을 막는 형식이다.

동시성이 있어 읽기가 매우 빠르지만, copy 시의 오버헤드가 크기 때문에 쓰기는 매우 느리다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image23.png)

스레드 4개,
읽기, 쓰기 테스트 결과이며 모든 스레드가 작업을 마치는데 걸리는 시간을 확인할 수 있다.

### 1.1.2 Synchronized arraylist
synchronized collection >> 동기화
자료구조의 method들에 동기화 처리를 해준 것이다.

동기화가 필요 없는 읽기 작업도 잠가서 읽기 성능이 떨어진다.

### 1.2 기존코드와의 성능 비교 (Synchronized block)

현재 안드로이드 코드에서 보이는 형식으로
arraylist의 읽기 작업은 놔두고 쓰기에만 lock을 걸기 위한 제일 간단한 방법이다.
arraylist의 쓰기 작업이 발생하는 메소드마다 lock을 거는 것이다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image24.png)

4개의 스레드, 쓰기 / 총 작업시간 (Integer 10000개로 테스트)

read에는 비동기 arraylist method와 비교해
+size()를 매번 계산하는 오버헤드를 측정하기로 함.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image25.png)

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/image26.png)

4개의 스레드, 쓰기 / 총 작업시간

> 10000개의 integer로 작업, 그다지 지연이 크지 않았다.
> 위의 자료구조들이 이 오버헤드보다 크다
> 제일 합리적인 동기화 방법으로 보인다.

### 1.3 CopyOnWriteArrayList 활용방안
동시성을 살리기위해

1.데이터가 적고 2.쓰는걸 적게하고 3.읽는걸 많이하는

data를 저장할 때 사용하면 좋을거같음.

적당히 크고 정적인 구조의 data를 찾아서
적용하는게 그나마 현실적

> > 찾는중...

#### 🧶 모든 문서는 수정될 수 있습니다.
