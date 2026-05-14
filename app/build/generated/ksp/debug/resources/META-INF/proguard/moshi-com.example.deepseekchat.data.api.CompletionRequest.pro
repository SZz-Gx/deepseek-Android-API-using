-if class com.example.deepseekchat.data.api.CompletionRequest
-keepnames class com.example.deepseekchat.data.api.CompletionRequest
-if class com.example.deepseekchat.data.api.CompletionRequest
-keep class com.example.deepseekchat.data.api.CompletionRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.deepseekchat.data.api.CompletionRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.example.deepseekchat.data.api.CompletionRequest
-keepclassmembers class com.example.deepseekchat.data.api.CompletionRequest {
    public synthetic <init>(java.lang.String,java.util.List,boolean,double,int,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
