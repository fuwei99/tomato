# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# 本 demo 未使用反射 / JNI / 序列化，暂无需保留规则。
# M1 接入 Room 后需要补充：
# -keep class com.tomato.app.data.db.** { *; }
