package com.coeater.android

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    KakaoSdk.init(this, "5a266dea13fcf275e773bc6392f74fe7")
  }
}
