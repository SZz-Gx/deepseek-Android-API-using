-if class com.example.deepseekchat.data.api.Usage
-keepnames class com.example.deepseekchat.data.api.Usage
-if class com.example.deepseekchat.data.api.Usage
-keep class com.example.deepseekchat.data.api.UsageJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.deepseekchat.data.api.Usage
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.example.deepseekchat.data.api.Usage
-keepclassmembers class com.example.deepseekchat.data.api.Usage {
    public synthetic <init>(int,int,int,int,int,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
