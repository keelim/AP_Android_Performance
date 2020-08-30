---
layout: post
title: "CNU AOSP 아카이빙 "
date: 2020-08-30 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## 모든 아이디어

- `if-else staement` vs `switch statement` vs `HashMap statement` vs `EnuMap statement`
    - EnumMap 의 경우 변하고자 하는 Enum을 만들어주어야 하고 
    - 1, 2, 3 이 아닌 일정 상수를 넣어주기 위해서는 더 큰 작업이 필요함으로 `HashMap`
- 익명 클래스(run) vs lambda 메모리 관련 문제
- 글로벌 락의 관한 문제
- 중첩 if else -> HashMap + Functional inter face pattern
    - WindowManagerService addWindow
    - PhoneWindowManagerPolicy - class PolicyHandler - handleMessage
- String vs StringBuffer vs StrinBuilder(concat) > LOG.e에서 String 과 같이 사용하는 문제
    - String < StringBuffer, StringBuilder 가 훨씬 좋다. 하지만 감싸야 하는 문제
- DisplayInfo.java clone vs copyFrom > final 문제, 해결할 수 있는 방법?
    - `new` 이외 OID 를 만드는 방법
- LayoutParams > 텔레스코핑 패턴
    - 변경하는 방법은 보통 빌더 클래스 > 하지만 안드로이드 워낙 이런식으로 호출하는 방법이 많다. 
- View.java 5391 internal.R.class switch case > R class 리소스 특성
    - 보통 R class 는 Resouece 를 mapping 을 하는 클래스로 이런식으로 호출을 하는 것이 옳을 수 있다. 
- Window.java 781 if else 중첩
    - HashMap 변경 가능 + Function interface
- WindowManager.java 3115 StringBuilder > String > concat 으로 사용
- WindowManager.java 2895 연속된 if 비교
- 글로벌 락 해결을 위한 코루틴 접근 방안 (코틀린 사용제한, 빌드 문제) > 함수형 프로그래밍 (StreamAPI, Function Interface 아이디어)
    - 코틀린은 Java 호환이 가능하지만 프레임워크 안에 넣는 것은 다른 문제
    - 코루틴은 비동기적인 글로벌락을 해결할 수 있는 문제로 생각했슴 like `Gorouine`
- size 관련 부분
    - 사용예에서는 size 크기가 워낙 작아서 size() 를 해도 무난 했음

```java
public void setStoppedState(IBinder token, boolean stopped) {
        synchronized (mLock) {
            int count = mViews.size();
            for (int i = 0; i < count; i++) {
                if (token == null || mParams.get(i).token == token) {
                    ViewRootImpl root = mRoots.get(i);
                    root.setWindowStopped(stopped);
                }
            }
        }
    }
```
- 화면 회전 관련 >
- 멀티쓰레드 환경에서 사용할 수 있는 자료구조 도입
    - CopyOnWriteArrayList
    - SynchronizedArrayList

#### 🧶 모든 문서는 수정될 수 있습니다.
