---
layout: post
title: "CNU AOSP 안드로이드 구글 픽셀 순정 이미지 플래싱"
date: 2020-08-30 00:00:01
author: Keelim
categories: AOSP
comments: true
toc: true
toc_sticky: true
---

## 안드로이드 이미지 입히는 방법 (Google Pixel Version)

구글에서는 픽셀폰에서 안드로이드 순정 이미지를 다운로드 할 수 있도록 링크를 제공합니다. 

<https://developers.google.com/android/images>

위 링크를 가셔서 자신이 원하는 이미지를 다운로드 받아 준비를 하시면 됩니다.

## 안드로이드 부트로더 화면

이제 안드로이드 픽셀 기기를 PC 연결을 하시고 터미널을 여시면 됩니다. 

Window -> cmd, powershell

Linux -> terminal

안드로이드 핸드폰은 환경설정 - 개발자 메뉴 - USB 디버깅 허용

체크를 확인 하시기 바랍니다. 

```sh
adb reboot bootloader
```

위 명령어는 안드로이드 기기의 부트로더로 재부팅하는 명령어 입니다.  부트로더 상태로 진입을 해야 이미지 파일을 `플래싱` 할 수 있습니다. 보통 상태로 USB를 연결하시면 이미지 파일이 포팅 되지 않습니다. 

이제 다운로드를 하신 압축 파일을 해제하시고 그 폴더로 들어가겠습니다. 

제가 다운로드 받은 파일은 PIXEL `android 10 r 017 version` 입니다.

압축을 해제 하시면

- bootloader-sailfish-~~.img
- flash-all.bat
- flash-all.sh
- flasg-base.sh
- image-sailfish~~.zip
- radio-sailfish~~.img

등의 파일 형태가 보입니다. 
Window 환경에서는 sh 가 아닌 bat 파일을 실행을 해주세요

다른 Linux 나 MAC OS 같은 경우는 sh 파일을 실행을 해주시면 됩니다. 

그러면 플래싱 과정이 완료되고 정상적인 이미지로 플래싱된 안드로이들 확인을 하실 수 있습니다.



### other

수동 플래싱 하는 방법도 있습니다. 이는 나중에 포스팅 하겠습니다. 




#### 🧶 모든 문서는 수정될 수 있습니다.
