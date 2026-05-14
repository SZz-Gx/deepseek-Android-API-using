-if class com.example.deepseekchat.data.api.CompletionResponse
-keepnames class com.example.deepseekchat.data.api.CompletionResponse
-if class com.example.deepseekchat.data.api.CompletionResponse
-keep class com.example.deepseekchat.data.api.CompletionResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.deepseekchat.data.api.CompletionResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.example.deepseekchat.data.api.CompletionResponse
-keepclassmembers class com.example.deepseekchat.data.api.CompletionResponse {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.Long,java.lang.String,java.util.List,com.example.deepseekchat.data.api.Usage,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
