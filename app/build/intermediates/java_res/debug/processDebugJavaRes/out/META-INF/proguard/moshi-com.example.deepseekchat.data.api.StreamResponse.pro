-if class com.example.deepseekchat.data.api.StreamResponse
-keepnames class com.example.deepseekchat.data.api.StreamResponse
-if class com.example.deepseekchat.data.api.StreamResponse
-keep class com.example.deepseekchat.data.api.StreamResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.deepseekchat.data.api.StreamResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.example.deepseekchat.data.api.StreamResponse
-keepclassmembers class com.example.deepseekchat.data.api.StreamResponse {
    public synthetic <init>(java.lang.String,java.util.List,com.example.deepseekchat.data.api.Usage,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
