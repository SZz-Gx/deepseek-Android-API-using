-if class com.example.deepseekchat.data.api.Choice
-keepnames class com.example.deepseekchat.data.api.Choice
-if class com.example.deepseekchat.data.api.Choice
-keep class com.example.deepseekchat.data.api.ChoiceJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.deepseekchat.data.api.Choice
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.example.deepseekchat.data.api.Choice
-keepclassmembers class com.example.deepseekchat.data.api.Choice {
    public synthetic <init>(int,com.example.deepseekchat.data.api.Message,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
