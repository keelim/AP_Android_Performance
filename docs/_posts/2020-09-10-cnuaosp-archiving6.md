---
layout: post
title: "간략한 실험용 firebase storage 사용하기"
date: 2020-09-10 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## Firebase

파이어베이스는 구글에서 사용을 할 수 있는 개발 플랫폼으로써 직접적인 백엔드의 구현없이 빠르게
사용을 할 수 있는 플랫폼이다. (개인적으로 그렇게 생각한다.) 물론 비즈니스적으로 확장적으로 사용할 수 있다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/fb1.png)

위 사진처럼 다양한 서비스를 이용할 수 있다.

저는 초기 설정과 storage 데이터 파일을 어떻게 올릴 수 있는지를 포스팅합니다.

## 프로젝트 만들기

안드로이드 스튜디오를 사용을 한다면 정말 빠르게 설정을 할 수 있습니다.

저는 웹 환경에서 설명을 드립니다.

<https://console.firebase.google.com/>

파이어베이스 콘솔로 이동을 하여 웹페이지 상단에 보이는 프로젝트 추가를 눌러 줍니다.

위 설치 과정에 대로 설치를 해주시면 됩니다.

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/fb2.png)

본인의 패키지 파일 형식에 맞추어 기입을 해줍니다.

특히 디버그 서명 인증서 SHA-1 를 기입을 하는 부분을 제일 어려워 하십니다.
문서에서는 자바에서 실행을 하는 방법, 따로 keytool 를 이용하는 방법이 있지만
저는 gradle task를 이용하는 방법이 제일 간편한것 같아 이를 소개합니다.

우선 안드로이드 스튜디오를 여시고 gradle task 를 실행해줍니다.
(shift\*2 번을 누르시고 execute gradle task 를 눌러 주시면 됩니다.)

![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/fb3.png)

`signinReport` 기입해주시면 gradle task 가 실행이 되고 원하시는 값을 확인 할 수 있습니다.

Variant: debug 를 잘 확인을 하시면 됩니다.

이제 storage 를 쓰는 방법은 간단 합니다. -> 안드로이드 스튜디오의 firebase storage 관한 dependency
를 설정을 해주시고 코드를 작성해주시면 됩니다.

보안 설정이 기본인 경우는 따로 코드를 조작을 할 필요가 없이 접근만으로 파일 등을 업로드 할 수 있습니다.

onCreate 에서 인스턴스를 받아주셔야 합니다.
![first](https://raw.githubusercontent.com/cnuaosp/AP_Android_Performance/master/docs/assets/fb4.png)

그리고 원하시는 부분에 아래 함수처럼 코드 로직을 작성을 하시고 버튼이나 따른 비즈니로직에서 붙여주시면
원하시는 데이터 파일이 바로 Firebase 스토리지의 저장이 되고 이를 확인할 수 있습니다.

<script src="https://gist.github.com/keelim/7da27270ed5271f3e34c4f28b3de5bef.js"></script>

#### 🧶 모든 문서는 수정될 수 있습니다.
