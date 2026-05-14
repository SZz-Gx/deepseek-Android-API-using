-if class com.example.deepseekchat.data.api.StreamChoice
-keepnames class com.example.deepseekchat.data.api.StreamChoice
-if class com.example.deepseekchat.data.api.StreamChoice
-keep class com.example.deepseekchat.data.api.StreamChoiceJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.deepseekchat.data.api.StreamChoice
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.example.deepseekchat.data.api.StreamChoice
-keepclassmembers class com.example.deepseekchat.data.api.StreamChoice {
    public synthetic <init>(int,com.example.deepseekchat.data.api.StreamDelta,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
