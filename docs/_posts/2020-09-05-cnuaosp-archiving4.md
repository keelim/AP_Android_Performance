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

##  HashMap testing 결과

Android Application 을 통하여 WindowMangerHashMap 성능 측정 결과 확인

## overview

1. WindowManagerService addWindow -> HashMap version 으로 변경한 이미지와 pure AOSP 이미지 비교
2. 같은 어플리케이션을 종합을 하고 이 데이터 값을 Serverlesss 서비스인 Google firebase storage csv 파일을 업로드
3. 데이터 결과 값 확인

## 어플리케이션 로직

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/test%20(1).png)

Alert Dialog 를 활용을 하여 addWindow 버전이 얼마나 빨리 향상을 하는지를 확인한다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/test%20(2).png)

테스트는 AlertDialog 를 show, dismiss 를 10000 회 반복 4세트를 진행한다.

`10000회` 반복 이상으로 넘어갈 경우 MemoryExceptio 이 발생함으로 세트의 수를 늘린다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/test%20(3).png)

샘플 다이알로그 호출

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/test%20(4).png)

데이터 값은 Android Widget 중 하나인 Recycler View를 사용을 하였습니다. Adapter 를 정의를 하여 만들어지느 test 값이
올바르게 적용될 수 있도록 한다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/test%20(5).png)

Google serverless 플랫폼인 firebase 를 활용을 하여 측정 값들을 바로 서버로 보내 확인 할 수 있도록 한다.

### 결과

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/test%20(6).png)

테스트 결과를 보면 지금 `10000` 회씩 주기성을 띄고 있는데 이는 테스트 1세트가 `10000` 회를 기준으로 하기 때문이다. 
`10000회` 이상을 실험해봐야지 해시 충돌 현상까지 관측할 수 있을 것 같은데 메모리가 실기기에서 부족하다. 
하지만 HashMap 자체로 실기기에서 성능향상을 하는 점은 결과로 나타나고 있다.


### 문제 발생



#### 🧶 모든 문서는 수정될 수 있습니다.
